package com.thepokecraftmod.rks.test.load;

import com.thebombzen.jxlatte.imageio.JXLImageReader;
import com.thepokecraftmod.rks.FileLocator;
import com.thepokecraftmod.rks.model.Model;
import com.thepokecraftmod.rks.model.texture.TextureType;
import com.thepokecraftmod.rks.pipeline.Shader;
import com.thepokecraftmod.rks.texture.Gpu2DTexture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MaterialUploader {
    private static final Logger LOGGER = LoggerFactory.getLogger("Material Uploader");
    public final Gpu2DTexture blank;
    public final Map<String, Material> materials = new HashMap<>();

    public MaterialUploader(Model model, FileLocator locator, Function<String, Shader> shaderFunction) {
        this.blank = Gpu2DTexture.create(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "blank");

        for (var entry : model.config().materials.entrySet()) {
            var name = entry.getKey();
            var meshMaterial = entry.getValue();
            var shader = shaderFunction.apply(name);
            var material = new Material(name, shader);

            for (var type : shader.texturesUsed()) {
                var texture = meshMaterial.getTextures(type);

                if (texture.size() < 1) LOGGER.debug("Shader expects " + type + " but the texture is missing");
                else upload(material, type, mergeAndLoad(model, locator, meshMaterial.getTextures(type)));
            }

            materials.put(name, material);
        }
    }

    private Gpu2DTexture mergeAndLoad(Model model, FileLocator locator, List<String> textures) {
        var imageReferences = textures.stream().map(s -> model.rootPath() + "/textures/" + s).map(locator::getFile).toList();

        var loadedImages = imageReferences.stream().map(bytes -> {
            try {
                var reader = new JXLImageReader(null, bytes);
                return reader.read(0, null);
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
            } else processedImages.add(image);
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

        return Gpu2DTexture.create(baseImage, "test.jxl");
    }

    private void upload(Material material, TextureType type, Gpu2DTexture texture) {
        var slot = material.usedSlots++;
        material.textures.add(texture);
        material.typeMap.put(texture, type);
        material.slotMap.put(texture, slot);
    }

    public void handle(String materialName) {
        // Bind Textures
        var material = materials.get(materialName);
        if (material == null) {
            LOGGER.error("Missing material \"" + materialName + "\". Not binding");
            return;
        }

        // Update Uniforms
        for (var texture : material.textures) {
            var slot = material.slotMap.get(texture);
            var type = material.typeMap.get(texture);

            var uniformName = switch (type) {
                case DIFFUSE -> "albedo";
                case NORMALS -> "normal";
                case ROUGHNESS -> "roughness";
                case METALNESS -> "metallic";
                case AMBIENT_OCCLUSION -> "ao";
                default -> throw new IllegalStateException("Unexpected value: " + type);
            };

            texture.bind(slot);
            material.shader.uploadInt(uniformName, slot);
        }
    }

    public static class Material {
        public final String name;
        public final Shader shader;
        public final List<Gpu2DTexture> textures = new ArrayList<>();
        public final Map<Gpu2DTexture, Integer> slotMap = new HashMap<>();
        public final Map<Gpu2DTexture, TextureType> typeMap = new HashMap<>();
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
