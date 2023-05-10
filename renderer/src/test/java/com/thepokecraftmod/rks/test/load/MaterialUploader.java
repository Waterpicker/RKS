package com.thepokecraftmod.rks.test.load;

import com.thebombzen.jxlatte.JXLDecoder;
import com.thebombzen.jxlatte.JXLOptions;
import com.thepokecraftmod.rks.FileLocator;
import com.thepokecraftmod.rks.model.Model;
import com.thepokecraftmod.rks.model.config.TextureFilter;
import com.thepokecraftmod.rks.model.config.variant.SetTextureModifier;
import com.thepokecraftmod.rks.model.config.variant.VariantModifier;
import com.thepokecraftmod.rks.model.texture.TextureType;
import com.thepokecraftmod.rks.pipeline.Shader;
import com.thepokecraftmod.rks.texture.GpuTexture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.thepokecraftmod.rks.model.texture.TextureType.ALBEDO;

public class MaterialUploader {
    private static final Logger LOGGER = LoggerFactory.getLogger("Material Uploader");
    public final Map<String, Material> defaultMaterials = new HashMap<>();

    public final Map<String, Map<String, Material>> variantMaterials = new HashMap<>();
    public final List<Runnable> mainThreadUploads = new ArrayList<>();

    public String currentVariant = null;

    public MaterialUploader(Model model, FileLocator locator, Function<String, Shader> shaderFunction) {
        var filter = model.config().textureFiltering;

        var variants = model.config().variants;

        var variantMap = new HashMap<String, Map<String, Map<TextureType, List<String>>>>();

        if(variants != null && variants.isEmpty()) {
            variants.forEach((name, modifiers) -> {
                var textures = new HashMap<String, Map<TextureType, List<String>>>();

                for (VariantModifier mod : modifiers) {

                    if (mod instanceof SetTextureModifier setTextureModifier) {
                        textures.computeIfAbsent(setTextureModifier.material, a -> new HashMap<>()).put(setTextureModifier.textureType, setTextureModifier.texture.layers());
                    }
                }

                variantMap.put(name, textures);
            });
        }


        for (var entry : model.config().materials.entrySet()) {
            var name = entry.getKey();
            var meshMaterial = entry.getValue();
            var shader = shaderFunction.apply(name);
            var material = new Material(name, shader);

            for (var type : shader.texturesUsed()) {
                var texture = meshMaterial.getTextures(type);

                if (texture.size() < 1) LOGGER.debug("Shader expects " + type + " but the texture is missing");
                else mainThreadUploads.add(() -> upload(material, type, mergeAndLoad(locator, filter, texture)));
            }

            defaultMaterials.put(name, material);
        }

        for (String variant : variantMap.keySet()) {
            var variantEntry = variantMaterials.computeIfAbsent(variant, a -> new HashMap<>());

            var function = variantMap.get(variant);

            for (var entry : model.config().materials.entrySet()) {
                var name = entry.getKey();
                var meshMaterial = entry.getValue();
                var shader = shaderFunction.apply(name);
                var material = new Material(name, shader);

                var variantTypes = function.get(name);

                for (var type : shader.texturesUsed()) {

                    List<String> texture = variantTypes != null && variantTypes.containsKey(type) ? variantTypes.get(type) : meshMaterial.getTextures(type);

                    if (texture.size() < 1) LOGGER.debug("Shader expects " + type + " but the texture is missing");
                    else {
                        mainThreadUploads.add(() -> upload(material, type, mergeAndLoad(locator, filter, texture)));
                    }
                }

                variantEntry.put(name, material);
            }

        }
    }

    private GpuTexture.Reference mergeAndLoad(FileLocator locator, TextureFilter filter, List<String> textures) {
        var imageReferences = textures.stream().map(s -> "textures/" + s).map(locator::getFile).toList();

        var loadedImages = imageReferences.stream().map(bytes -> {
            try {
                var options = new JXLOptions();
                options.hdr = JXLOptions.HDR_OFF;
                options.threads = 2;
                var reader = new JXLDecoder(new ByteArrayInputStream(bytes), options);
                var image = reader.decode();
                return image.fillColor().asBufferedImage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).toList();
        var processedImages = new ArrayList<BufferedImage>();

        for (var image : loadedImages) {
            var width = image.getWidth();
            var height = image.getHeight();
            var needMirror = height / width == 2;

            if (needMirror) {
                var mirror = new BufferedImage(width * 2, height, BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < height; y++) {
                    for (int lx = 0, rx = width * 2 - 1; lx < width; lx++, rx--) {
                        int p = image.getRGB(lx, y);
                        mirror.setRGB(lx, y, p);
                        mirror.setRGB(rx, y, p);
                    }
                }

                processedImages.add(mirror);
            } else {
                var mirror = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int p = image.getRGB(x, y);
                        mirror.setRGB(x, y, p);
                        mirror.setRGB(x, y, p);
                    }
                }

                processedImages.add(mirror);
            }
        }

        var baseImage = processedImages.get(0);
        var topLayers = processedImages.subList(1, textures.size());
        for (var topLayer : topLayers) {
            for (int x = 0; x < baseImage.getWidth(); x++) {
                for (int y = 0; y < baseImage.getHeight(); y++) {
                    var p = topLayer.getRGB(x, y);
                    var alpha = 0xFF & (p >> 24);
                    var red = 0xFF & (p >> 16);
                    var green = 0xFF & (p >> 8);
                    var blue = 0xFF & (p);
                    // TODO: option to set bg color
                    if (green < 200) baseImage.setRGB(x, y, p);
                }
            }
        }

        return new GpuTexture.Reference(baseImage, filter, textures.get(0));
    }

    private void upload(Material material, TextureType type, GpuTexture.Reference reference) {
        var tex = reference.upload();
        var slot = material.usedSlots++;
        material.textures.add(tex);
        material.typeMap.put(tex, type);
        material.slotMap.put(tex, slot);
    }

    public void handle(String materialName) {
        if (mainThreadUploads.size() > 0) throw new RuntimeException("Textures not uploaded");
        var material = variantMaterials.getOrDefault(currentVariant, defaultMaterials).get(materialName);

        if (material == null) {
            LOGGER.error("Missing material \"" + materialName + "\". Not binding");
            return;
        }

        var unusedTexturesInShader = material.shader.texturesUsed().stream()
                .filter(textureType -> !material.typeMap.containsValue(textureType))
                .toList();

        for (var type : unusedTexturesInShader) {
            var loc = switch (type) {
                case ALBEDO -> 0;
                case NORMALS -> 1;
                case ROUGHNESS -> 2;
                case METALNESS -> 3;
                case AMBIENT_OCCLUSION -> 4;
                case EMISSIVE -> 5;
                default -> throw new IllegalStateException("Unexpected value: " + type);
            };

            material.shader.uploadInt(loc, 16);
        }

        // Update Uniforms
        for (var texture : material.textures) {
            var slot = material.slotMap.get(texture);
            var type = material.typeMap.get(texture);

            var loc = switch (type) {
                case ALBEDO -> 0;
                case NORMALS -> 1;
                case METALNESS -> 2;
                case ROUGHNESS -> 3;
                case AMBIENT_OCCLUSION -> 4;
                case EMISSIVE -> 5;
                default -> throw new IllegalStateException("Unexpected value: " + type);
            };

            texture.bind(slot);
            material.shader.uploadInt(loc, slot);
        }
    }

    public void upload() {
        for (var task : mainThreadUploads) task.run();
        mainThreadUploads.clear();
    }

    public static class Material {
        public final String name;
        public final Shader shader;
        public final List<GpuTexture.Direct> textures = new ArrayList<>();
        public final Map<GpuTexture.Direct, Integer> slotMap = new HashMap<>();
        public final Map<GpuTexture.Direct, TextureType> typeMap = new HashMap<>();
        protected int usedSlots = 0;

        public Material(String name, Shader shader) {
            this.name = name;
            this.shader = shader;
        }

        @Override
        public String toString() {
            return "Material{" + "name='" + name + '\'' + ", shader=" + shader + '}';
        }
    }
}
