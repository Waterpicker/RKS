package com.thepokecraftmod.rks.model;

import org.joml.Vector2f;

public record Mesh(
        String name,
        int material,
        Vector2f texCoords0
) {}
