package com.thepokecraftmod.rks.model;

import com.thepokecraftmod.rks.model.animation.Skeleton;
import com.thepokecraftmod.rks.model.extra.ModelConfig;

public record Model(
        String rootPath,
        String[] materialReferences,
        Mesh[] meshes,
        Skeleton skeleton,
        ModelConfig config
) {}
