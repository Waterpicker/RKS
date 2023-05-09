package com.thepokecraftmod.rks.model.config.variant;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thepokecraftmod.rks.model.material.Material;
import com.thepokecraftmod.rks.model.texture.TextureType;

import java.util.List;
import java.util.Locale;

public class SetTextureModifier implements VariantModifier {

    public final String material;
    public final TextureType textureType;

    public final Texture texture;

    public SetTextureModifier(JsonObject object) {
        this.material = object.get("material").getAsJsonPrimitive().getAsString();
        this.textureType = TextureType.valueOf(object.get("textureType").getAsJsonPrimitive().getAsString().toUpperCase(Locale.ENGLISH));
        this.texture = Texture.of(object.get("texture").getAsJsonObject());
    }

    public static record Texture(List<String> layers) {
        public static Texture of(JsonObject object) {
            return new Texture(object.getAsJsonArray("layers").asList().stream().map(JsonElement::getAsString).toList());
        }
    }
}
