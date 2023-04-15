package com.thepokecraftmod.rks.storage;

import com.thepokecraftmod.rks.animation.AnimationController;
import com.thepokecraftmod.rks.animation.AnimationInstance;
import com.thepokecraftmod.rks.texture.RenderMaterial;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

public class AnimatedObjectInstance extends ObjectInstance {

    private final int boneCount;
    @Nullable
    public AnimationInstance mainAnimation;
    @Nullable
    public AnimationInstance facialAnimation;

    public AnimatedObjectInstance(int boneCount, Matrix4f transformationMatrix, RenderMaterial materialId) {
        super(MAT4F_SIZE * boneCount + MAT4F_SIZE, transformationMatrix, materialId);
        this.boneCount = boneCount;
    }

    public void update() {
        if (getMainTransforms().length != getFacialTransforms().length && getFacialTransforms() != AnimationController.NO_ANIMATION)
            throw new RuntimeException("Animations are not compatible");

        try (var stack = MemoryStack.stackPush()) {
            var pTransformationMatrix = stack.nmalloc(MAT4F_SIZE);
            transformationMatrix.getToAddress(pTransformationMatrix);
            upload(0, MAT4F_SIZE, pTransformationMatrix);

            var pAnimTransforms = stack.nmalloc(MAT4F_SIZE * boneCount);
            var layer0 = getMainTransforms();
            var layer1 = getFacialTransforms();
            for (int i = 0; i < layer0.length; i++) {
                var layer0Transform = layer0[i];
                // var layer1Transform = layer1[i];

                if (layer0Transform != null) {
                    layer0Transform.getToAddress(pAnimTransforms + (long) i * MAT4F_SIZE);
                    // Disabled due to bugs
                    // if (layer1 != AnimationController.NO_ANIMATION && !layer0Transform.equals(layer1Transform)) {
                    //     var combinedTransform = layer0Transform.mul(layer1Transform, new Matrix4f());
                    //     combinedTransform.getToAddress(pAnimTransforms + (long) i * MAT4F_SIZE);
                    // } else
                    //     layer0Transform.getToAddress(pAnimTransforms + (long) i * MAT4F_SIZE);
                }
            }

            upload(MAT4F_SIZE, MAT4F_SIZE * boneCount, pAnimTransforms);
        }
    }

    public Matrix4f[] getMainTransforms() {
        if (mainAnimation == null || mainAnimation.matrixTransforms == null)
            return AnimationController.NO_ANIMATION;
        return mainAnimation.matrixTransforms;
    }

    public Matrix4f[] getFacialTransforms() {
        if (facialAnimation == null || facialAnimation.matrixTransforms == null)
            return AnimationController.NO_ANIMATION;
        return facialAnimation.matrixTransforms;
    }

    public void changeMainAnimation(AnimationInstance instance) {
        if (this.mainAnimation != null) this.mainAnimation.destroy();
        this.mainAnimation = instance;
    }

    public void changeFacialAnimation(AnimationInstance instance) {
        if (this.facialAnimation != null) this.facialAnimation.destroy();
        this.facialAnimation = instance;
    }
}
