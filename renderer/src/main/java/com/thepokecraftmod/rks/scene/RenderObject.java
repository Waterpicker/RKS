package com.thepokecraftmod.rks.scene;

import com.thepokecraftmod.rks.pipeline.Shader;
import com.thepokecraftmod.rks.storage.ObjectInstance;
import org.joml.Matrix4f;

import java.util.List;

public abstract class RenderObject {
    protected Shader shader;
    public boolean hidden;
    protected boolean disableBackfaceCull;

    public abstract void render(List<ObjectInstance> instances);

    public void update() {}
}

