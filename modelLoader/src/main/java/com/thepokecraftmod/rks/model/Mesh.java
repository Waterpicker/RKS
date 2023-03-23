package com.thepokecraftmod.rks.model;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public record Mesh(
        String name,
        int material,
        ArrayList<Integer> indices,
        ArrayList<Vector3f> positions,
        ArrayList<Vector2f> uvs,
        ArrayList<Vector3f> normals
) {}
