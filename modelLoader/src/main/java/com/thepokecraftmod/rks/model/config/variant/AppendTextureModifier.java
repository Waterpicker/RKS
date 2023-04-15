package com.thepokecraftmod.rks.model.config.variant;

import com.google.gson.JsonObject;
import com.thepokecraftmod.rks.model.texture.TextureType;

import java.util.Locale;

public class AppendTextureModifier implements VariantModifier {

    public final String material;
    public final TextureType textureType;
    public final String append;
    public final String method;

    public AppendTextureModifier(JsonObject object) {
        this.material = object.get("material").getAsJsonPrimitive().getAsString();
        this.textureType = TextureType.valueOf(object.get("textureType").getAsJsonPrimitive().getAsString().toUpperCase(Locale.ENGLISH));
        this.append = object.get("append").getAsJsonPrimitive().getAsString();
        this.method = object.get("method").getAsJsonPrimitive().getAsString();
    }
}
