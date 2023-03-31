package com.thepokecraftmod.rks.model.material;

import com.thepokecraftmod.rks.model.texture.TextureType;

import java.util.List;

public record Material(
        List<String> diffuse,
        List<String> normal,
        List<String> roughness,
        List<String> metallic,
        List<String> ao
) {

    public List<String> getTextures(TextureType type) {
        return switch (type) {
            case SPECULAR, LIGHTMAP, UNKNOWN, TRANSMISSION, CLEARCOAT, SHEEN, EMISSION_COLOR, NORMAL_CAMERA, BASE_COLOR, REFLECTION, DISPLACEMENT, OPACITY, SHININESS, HEIGHT, EMISSIVE, AMBIENT -> null;
            case DIFFUSE -> diffuse;
            case NORMALS -> normal;
            case ROUGHNESS -> roughness;
            case METALNESS -> metallic;
            case AMBIENT_OCCLUSION -> ao;
        };
    }
}
