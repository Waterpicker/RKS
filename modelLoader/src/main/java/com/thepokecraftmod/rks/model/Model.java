package com.thepokecraftmod.rks.model;

import com.thepokecraftmod.rks.model.animation.Joint;
import com.thepokecraftmod.rks.model.extra.ModelConfig;
import com.thepokecraftmod.rks.model.material.Material;

import java.io.Closeable;

public record Model(
        String rootPath,
        String[] materialReferences,
        Mesh[] meshes,
        Joint root,
        ModelConfig config
) {}
