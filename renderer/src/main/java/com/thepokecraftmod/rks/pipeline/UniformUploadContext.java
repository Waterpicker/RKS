package com.thepokecraftmod.rks.pipeline;

import com.thepokecraftmod.rks.rendering.ObjectInstance;
import com.thepokecraftmod.rks.scene.RenderObject;

public record UniformUploadContext(RenderObject object, ObjectInstance instance, Uniform uniform) {}
