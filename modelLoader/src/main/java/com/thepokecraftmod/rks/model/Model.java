package com.thepokecraftmod.rks.model;

import com.thepokecraftmod.rks.model.animation.Skeleton;
import com.thepokecraftmod.rks.model.config.ModelConfig;

public record Model(
        String[] materialReferences,
        Mesh[] meshes,
        Skeleton skeleton,
        ModelConfig config
) {}
