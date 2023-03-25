package com.thepokecraftmod.rks.storage;

import com.thepokecraftmod.rks.animation.AnimationController;
import com.thepokecraftmod.rks.animation.AnimationInstance;
import com.thepokecraftmod.rks.model.animation.Animation;
import com.thepokecraftmod.rks.scene.AnimatedMeshObject;
import com.thepokecraftmod.rks.scene.MultiRenderObject;
import com.thepokecraftmod.rks.texture.RenderMaterial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AnimatedObjectInstance extends ObjectInstance {

    @Nullable
    public AnimationInstance currentAnimation;

    public AnimatedObjectInstance(Matrix4f transformationMatrix, RenderMaterial materialId) {
        super(MAT4F_SIZE * 220 + MAT4F_SIZE, transformationMatrix, materialId);
    }

    public void update() {
        try (var stack = MemoryStack.stackPush()) {
            var pTransformationMatrix = stack.nmalloc(MAT4F_SIZE);
            transformationMatrix.getToAddress(pTransformationMatrix);
            upload(0, MAT4F_SIZE, pTransformationMatrix);

            var pAnimTransforms = stack.nmalloc(MAT4F_SIZE * 220);
            var transforms = getTransforms();
            for (int i = 0; i < transforms.length; i++)
                transforms[i].getToAddress(pAnimTransforms + (long) i * MAT4F_SIZE);

            upload(MAT4F_SIZE, MAT4F_SIZE * 220, pAnimTransforms);
        }
    }

    @NotNull
    public Map<String, Animation> getAnimationsIfAvailable() {
        try {
            return getAnimatedMesh().animations;
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }

    public AnimatedMeshObject getAnimatedMesh() {
        if (object instanceof MultiRenderObject<?> mro) {
            return ((List<AnimatedMeshObject>) mro.objects).get(0);
        }
        return (AnimatedMeshObject) object;
    }

    public Matrix4f[] getTransforms() {
        if (currentAnimation == null || currentAnimation.matrixTransforms == null)
            return AnimationController.NO_ANIMATION;
        return currentAnimation.matrixTransforms;
    }

    public void changeAnimation(AnimationInstance newAnimation) {
        if (currentAnimation != null) currentAnimation.destroy();
        this.currentAnimation = newAnimation;
    }
}
