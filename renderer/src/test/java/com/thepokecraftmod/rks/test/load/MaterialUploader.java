package com.thepokecraftmod.rks.test.load;

import com.thebombzen.jxlatte.imageio.JXLImageReader;
import com.thepokecraftmod.rks.FileLocator;
import com.thepokecraftmod.rks.model.Model;
import com.thepokecraftmod.rks.model.texture.TextureType;
import com.thepokecraftmod.rks.pipeline.Shader;
import com.thepokecraftmod.rks.texture.Gpu2DTexture;
import org.lwjgl.opengl.GL20C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

public class MaterialUploader {
    private static final Logger LOGGER = LoggerFactory.getLogger("Material Uploader");
    private static final ForkJoinPool COMMON_POOL = ForkJoinPool.commonPool();
    public final Map<String, Map<Integer, Gpu2DTexture>> materialMap = new HashMap<>();
    public final Map<String, Shader> shaderMap = new HashMap<>();
    private int usedSlots = 0;

    public MaterialUploader(Model model, FileLocator locator, Function<String, Shader> shaderFunction) {
        for (var entry : model.config().materials.entrySet()) {
            var name = entry.getKey();
            var material = entry.getValue();
            var shader = shaderMap.computeIfAbsent(name, shaderFunction);

            for (var type : shader.typeToSlotMap()) {
                if (material.getTextures(type).size() < 1)
                    LOGGER.error("Shader expects " + type + " but the texture is missing");
                else upload(name, type, mergeAndLoad(model, locator, material.getTextures(type)));
            }
        }
    }

    private Gpu2DTexture mergeAndLoad(Model model, FileLocator locator, List<String> textures) {
        var imageReferences = textures.stream()
                .map(s -> model.rootPath() + "/textures/" + s)
                .map(locator::getFile)
                .toList();

        var loadedImages = COMMON_POOL.invoke(new LoadImagesTask(imageReferences));
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

    private void upload(String material, TextureType type, Gpu2DTexture texture) {
        var slot = usedSlots++;

        var gpuTexMap = materialMap.computeIfAbsent(material, s -> new HashMap<>());
        gpuTexMap.put(slot, texture);
    }

    public void handle(String materialName) {
        // Bind Textures
        var material = materialMap.getOrDefault(materialName, materialMap.get("body"));
        for (var entry : material.entrySet()) entry.getValue().bind(entry.getKey());

        // Update Uniforms
        var diffuseLocation = shaderMap.getOrDefault(materialName, shaderMap.get("body")).getUniform("diffuse");
        GL20C.glUniform1i(diffuseLocation, 0);
    }
}
