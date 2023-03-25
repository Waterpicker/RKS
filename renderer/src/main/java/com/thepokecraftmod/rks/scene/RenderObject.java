package com.thepokecraftmod.rks.scene;

import com.thepokecraftmod.rks.pipeline.Shader;
import com.thepokecraftmod.rks.storage.ObjectInstance;
import org.joml.Matrix4f;

import java.util.List;

public abstract class RenderObject {
    protected Shader shader;
    protected boolean ready = false;
    protected Matrix4f matrixOffset = new Matrix4f().identity();
    protected boolean disableBackfaceCull = false;

    public abstract void render(List<ObjectInstance> instances);

    public void update() {}

    public boolean isReady() {
        return ready;
    }

    public void setMatrixOffset(Matrix4f mat4f) {
        matrixOffset.set(mat4f);
    }

    public void applyTransformOffset(Matrix4f currentTransform) {
        currentTransform.mul(matrixOffset);
    }
}

