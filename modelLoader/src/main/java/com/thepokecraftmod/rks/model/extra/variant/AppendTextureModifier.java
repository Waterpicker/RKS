package com.thepokecraftmod.rks.model.extra.variant;

import com.google.gson.JsonObject;
import com.thepokecraftmod.rks.model.texture.TextureType;

public class AppendTextureModifier implements VariantModifier {

    public final String material;
    public final TextureType textureType;
    public final String replacement;

    public AppendTextureModifier(JsonObject object) {
        this.material = object.get("material").getAsJsonPrimitive().getAsString();
        var texType = object.get("material").getAsJsonPrimitive().getAsString();
    }
}
