package com.thepokecraftmod.rks.pipeline;

import com.pokemod.rarecandy.components.RenderObject;
import com.pokemod.rarecandy.rendering.ObjectInstance;

public record UniformUploadContext(RenderObject object, ObjectInstance instance, Uniform uniform) {}