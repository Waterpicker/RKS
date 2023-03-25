package com.thepokecraftmod.rks.texture;

import com.thepokecraftmod.rks.model.texture.TextureType;

import java.util.Map;

@FunctionalInterface
public interface RenderMaterial {

    default void uploadTextures(Map<TextureType, Integer> textureToSlot) {
        for (var entry : textureToSlot.entrySet()) {
            if (entry.getValue() != -1 && entry.getValue() < 15) upload(entry.getKey(), entry.getValue());
        }
    }

    void upload(TextureType type, int slot);
}
