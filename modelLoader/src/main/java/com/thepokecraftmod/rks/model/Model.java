package com.thepokecraftmod.rks.model;

import com.thepokecraftmod.rks.model.animation.Joint;
import com.thepokecraftmod.rks.model.extra.ModelConfig;
import com.thepokecraftmod.rks.model.material.Material;

public record Model(
        Material[] materials,
        Mesh[] meshes,
        Joint root,
        ModelConfig config
) {}
