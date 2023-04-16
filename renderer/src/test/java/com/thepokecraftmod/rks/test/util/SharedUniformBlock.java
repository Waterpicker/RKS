package com.thepokecraftmod.rks.test.util;

import com.thepokecraftmod.rks.ubo.UniformBlockUploader;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

public class SharedUniformBlock extends UniformBlockUploader {

    private final Matrix4f projectionMatrix;
    private final Matrix4f viewMatrix = new Matrix4f().lookAt(0.1f, 0.0f, -1, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

    public SharedUniformBlock(Window window, int fov) {
        super(MAT4F_SIZE * 2, 0);
        this.projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(fov), (float) window.width / window.height, 0.1f, 1000.0f);
    }

    public void update() {
        try (var stack = MemoryStack.stackPush()) {
            var sharedInfo = stack.nmalloc(MAT4F_SIZE * 2);
            projectionMatrix.getToAddress(sharedInfo);
            viewMatrix.getToAddress(sharedInfo + MAT4F_SIZE);
            upload(0, MAT4F_SIZE * 2, sharedInfo);
        }
    }
}
