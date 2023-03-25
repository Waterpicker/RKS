package com.thepokecraftmod.rks.model;

import com.thepokecraftmod.rks.model.animation.Joint;
import com.thepokecraftmod.rks.model.extra.ModelConfig;
import com.thepokecraftmod.rks.model.material.Material;
import com.thepokecraftmod.rks.model.texture.Texture;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;

public record Model(
        Material[] materials,
        Mesh[] meshes,
        Joint root,
        ModelConfig config
) implements Closeable {
    @Override
    public void close() {
        for (var material : materials) {
            for (var texture : material.textureMap().values()) texture.ifPresent(Texture::close);
        }
    }
}
