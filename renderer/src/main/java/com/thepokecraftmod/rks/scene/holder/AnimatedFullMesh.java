package com.thepokecraftmod.rks.scene.holder;

import com.thepokecraftmod.rks.model.animation.Animation;

import java.util.HashMap;
import java.util.Map;

public class AnimatedFullMesh extends FullMesh {

    public final Map<String, Animation> animations = new HashMap<>();

    public AnimatedFullMesh(Map<String, Animation> animations) {
        this.animations.putAll(animations);
    }
}
