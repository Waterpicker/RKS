package com.thepokecraftmod.rks.test.load;

import com.thepokecraftmod.rks.Pair;
import com.thepokecraftmod.rks.model.Model;
import com.thepokecraftmod.rks.model.texture.Texture;
import com.thepokecraftmod.rks.model.texture.TextureType;
import com.thepokecraftmod.rks.pipeline.Shader;
import com.thepokecraftmod.rks.texture.Gpu2DTexture;
import org.lwjgl.opengl.GL20C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MaterialUploader {
    private static final Logger LOGGER = LoggerFactory.getLogger("Material Uploader");
    public final Map<String, Map<Integer, Gpu2DTexture>> materialMap = new HashMap<>();
    public final Map<String, Shader> shaderMap = new HashMap<>();
    private int usedSlots = 0;

    public MaterialUploader(Model model, Function<String, Shader> shaderFunction) {
        for (var material : model.materials()) {
            var shader = shaderMap.computeIfAbsent(material.name(), shaderFunction::apply);

            var existingTextures = material.textureMap().entrySet().stream()
                    .filter(e -> e.getValue().isPresent())
                    .map(e -> new Pair<>(e.getKey(), e.getValue().get()))
                    .collect(Collectors.toMap(Pair::a, Pair::b));

            for (var type : shader.typeToSlotMap()) {
                if (!existingTextures.containsKey(type))
                    LOGGER.error("Shader expects " + type + " but the texture is missing");
                else upload(material.name(), type, existingTextures.get(type));
            }
        }
    }

    private void upload(String material, TextureType type, Texture texture) {
        var slot = usedSlots++;
        var gpuTexture = new Gpu2DTexture(texture.data, texture.width, texture.height, texture.reference);

        var gpuTexMap = materialMap.computeIfAbsent(material, s -> new HashMap<>());
        gpuTexMap.put(slot, gpuTexture);
    }

    public void handle(String materialName) {
        // Bind Textures
        var material = materialMap.getOrDefault(materialName, materialMap.get("body"));
        for (var entry : material.entrySet()) entry.getValue().bind(entry.getKey());

        // Update Uniforms
        var diffuseLocation = shaderMap.get(materialName).getUniform("diffuse");
        GL20C.glUniform1i(diffuseLocation, 0);
    }
}
