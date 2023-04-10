package com.thepokecraftmod.rks.model.extra;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thepokecraftmod.rks.model.extra.variant.VariantModifier;
import com.thepokecraftmod.rks.model.material.Material;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ModelConfig {
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(VariantModifier.class, new VariantModifier.Adapter())
            .create();
    public String shadingMethod;
    public List<String> hiddenMeshes;
    public Map<String, Material> materials;
    public Map<String, List<VariantModifier>> variants;
    public List<Object> animations;
    @Nullable
    public PokemonConfig pokemonConfig;
}
