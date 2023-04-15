package com.thepokecraftmod.rks.model.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thepokecraftmod.rks.model.config.animation.AnimationGroup;
import com.thepokecraftmod.rks.model.config.animation.AnimationInfo;
import com.thepokecraftmod.rks.model.config.variant.VariantModifier;
import com.thepokecraftmod.rks.model.material.Material;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ModelConfig {
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(VariantModifier.class, new VariantModifier.Adapter())
            .create();
    public String shadingMethod;
    public String modelLocation;
    public TextureFilter textureFiltering = TextureFilter.LINEAR;
    public List<String> hiddenMeshes;
    public Map<String, Material> materials;
    public Map<String, List<VariantModifier>> variants;
    public Map<AnimationGroup, Map<String, AnimationInfo>> animations;
    @Nullable
    public PokemonConfig pokemonConfig;
}
