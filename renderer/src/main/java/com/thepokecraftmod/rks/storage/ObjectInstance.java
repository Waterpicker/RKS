package com.thepokecraftmod.rks.storage;

import com.thepokecraftmod.rks.texture.RenderMaterial;
import com.thepokecraftmod.rks.pipeline.UniformBlockUploader;
import com.thepokecraftmod.rks.scene.RenderObject;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

public class ObjectInstance extends UniformBlockUploader {
    public final Matrix4f transformationMatrix;
    protected RenderObject object;
    public RenderMaterial material;

    public ObjectInstance(Matrix4f transformationMatrix, RenderMaterial material) {
        this(MAT4F_SIZE, transformationMatrix, material);
    }

    public ObjectInstance(int size, Matrix4f transformationMatrix, RenderMaterial material) {
        super(size, 1);
        this.transformationMatrix = transformationMatrix;
        this.material = material;
    }

    public void update() {
        try (var stack = MemoryStack.stackPush()) {
            var ptr = stack.nmalloc(MAT4F_SIZE);
            transformationMatrix.getToAddress(ptr);
            upload(0, MAT4F_SIZE, ptr);
        }
    }

    public void link(RenderObject object) {
        this.object = object;
        object.applyTransformOffset(transformationMatrix);
    }

    public RenderObject getObject() {
        return object;
    }
}
