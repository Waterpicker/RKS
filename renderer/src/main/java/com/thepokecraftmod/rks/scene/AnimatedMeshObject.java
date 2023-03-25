package com.thepokecraftmod.rks.scene;

import com.thepokecraftmod.rks.model.animation.Animation;
import com.thepokecraftmod.rks.model.GLModel;
import com.thepokecraftmod.rks.pipeline.Shader;

import java.util.Map;

public class AnimatedMeshObject extends MeshObject {

    public Map<String, Animation> animations;

    public void setup(GLModel model, Shader shader, Map<String, Animation> animations) {
        this.model = model;
        this.shader = shader;
        this.animations = animations;
        this.ready = true;
    }
}
