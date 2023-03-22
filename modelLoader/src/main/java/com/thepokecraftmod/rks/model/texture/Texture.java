package com.thepokecraftmod.rks.model.texture;

import java.util.HashMap;
import java.util.Map;

public class Texture {

    public final String reference;
    public final Map<String, Object> properties;

    public Texture(String reference) {
        this.reference = reference;
        this.properties = new HashMap<>();
    }
}
