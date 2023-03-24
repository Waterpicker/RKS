package com.thepokecraftmod.rks.animation;

import com.thepokecraftmod.rks.model.animation.Animation;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages all Animations
 */
public class AnimationController {
    private static final Logger LOGGER = LoggerFactory.getLogger("AnimationController");
    public static final Matrix4f[] NO_ANIMATION = new Matrix4f[220];
    public final List<AnimationInstance> playingInstances = new ArrayList<>();
    public final Map<Animation, Matrix4f[]> instanceIgnoringAnimTransforms = new HashMap<>();

    public void render(double globalSecondsPassed) {
        var instancesToRemove = new ArrayList<AnimationInstance>();
        instanceIgnoringAnimTransforms.clear();

        for (var playingInstance : playingInstances) {
            if (playingInstance.animation == null) {
                LOGGER.error("Animation instance has null animation");
                continue;
            }

            if (playingInstance.shouldDestroy()) instancesToRemove.add(playingInstance);
            if (playingInstance.animation.ignoreInstancedTime)
                instanceIgnoringAnimTransforms.put(playingInstance.animation, playingInstance.animation.getFrameTransform(globalSecondsPassed));

            if (instanceIgnoringAnimTransforms.containsKey(playingInstance.animation)) {
                playingInstance.matrixTransforms = instanceIgnoringAnimTransforms.get(playingInstance.animation);
                continue;
            }

            if (playingInstance.startTime == -1) playingInstance.startTime = globalSecondsPassed;
            playingInstance.update(globalSecondsPassed);
            playingInstance.matrixTransforms = playingInstance.getFrameTransform(playingInstance);
        }

        playingInstances.removeAll(instancesToRemove);
    }

    static {
        var identity = new Matrix4f().identity();
        Arrays.fill(NO_ANIMATION, identity);
    }
}
