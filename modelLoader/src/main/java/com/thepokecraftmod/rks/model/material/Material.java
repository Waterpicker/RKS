package com.thepokecraftmod.rks.model.material;

import com.thepokecraftmod.rks.model.texture.Texture;
import com.thepokecraftmod.rks.model.texture.TextureType;

import java.util.Map;
import java.util.Optional;

public record Material(
        String name,
        Map<TextureType, Optional<Texture>> textureMap,
        ShadingMethod shadingMethod,
        Map<String, Object> extraProperties
) {
    Optional<Texture> getTexture(TextureType type) {
        return textureMap.get(type);
    }
}
