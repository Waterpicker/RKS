package com.thepokecraftmod.rks.model.config.variant;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class HideMeshModifier implements VariantModifier {

    public final List<String> meshes = new ArrayList<>();

    public HideMeshModifier(JsonObject object) {
        var jsonMeshes = object.get("meshes").getAsJsonArray();

        for (var element : jsonMeshes)
            meshes.add(element.getAsJsonPrimitive().getAsString());
    }
}
