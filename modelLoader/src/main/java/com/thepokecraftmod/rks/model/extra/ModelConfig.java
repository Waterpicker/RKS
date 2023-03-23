package com.thepokecraftmod.rks.model.extra;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thepokecraftmod.rks.model.extra.variant.VariantModifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ModelConfig {
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(VariantModifier.class, new VariantModifier.Adapter())
            .create();
    public boolean usesTransparency;
    public Map<String, List<VariantModifier>> variantModifiers;
    @Nullable
    public PokemonConfig pokemonConfig;
}
