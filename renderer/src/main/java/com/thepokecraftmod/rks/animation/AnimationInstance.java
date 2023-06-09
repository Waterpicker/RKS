package com.thepokecraftmod.rks.animation;

import com.thepokecraftmod.rks.model.animation.Animation;
import org.joml.Matrix4f;

/**
 * Instance of an animation.
 */
public abstract class AnimationInstance {

    protected Animation animation;
    public double startTime = -1;
    protected double lastRealTime;
    protected float currentTime;
    protected double timeAtPause;
    protected double timeAtUnpause;
    private boolean paused;
    private boolean unused;
    public Matrix4f[] matrixTransforms;

    public AnimationInstance(Animation animation) {
        this.animation = animation;
    }

    public void update(double secondsPassed) {
        lastRealTime = secondsPassed;
        updateStart(secondsPassed);

        if (!paused) {
            if (timeAtUnpause == -1) timeAtUnpause = secondsPassed - timeAtPause;
            float prevTime = currentTime;
            currentTime = animation.getAnimationTime(secondsPassed - timeAtUnpause);
            if (prevTime > currentTime) onLoop();
        } else if (timeAtPause == -1) timeAtPause = secondsPassed;
    }

    public void updateStart(double secondsPassed) {
        if (timeAtUnpause == 0) timeAtUnpause = secondsPassed;
        if (startTime == -1) startTime = secondsPassed;
    }

    public void pause() {
        paused = true;
        timeAtPause = -1;
    }

    public void unpause() {
        paused = false;
        timeAtUnpause = -1;
    }

    public void onLoop() {
    }

    protected Matrix4f[] getFrameTransform() {
        var boneTransforms = new Matrix4f[animation.skeleton.nodes.length];
        animation.readNodeHierarchy(getCurrentTime(), animation.skeleton.rootNode, new Matrix4f().identity(), boneTransforms);
        return boneTransforms;
    }

    public float getCurrentTime() {
        return currentTime;
    }

    public boolean isPaused() {
        return paused;
    }

    public void destroy() {
        this.unused = true;
    }

    public boolean shouldDestroy() {
        return unused;
    }

    public Animation getAnimation() {
        return animation;
    }
}
