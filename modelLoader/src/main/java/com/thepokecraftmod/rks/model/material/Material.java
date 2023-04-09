package com.thepokecraftmod.rks.model.material;

import com.thepokecraftmod.rks.model.texture.TextureType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Material {
    public final List<String> albedo;
    public final List<String> normal;
    public final List<String> roughness;
    public final List<String> metallic;
    public final List<String> ao;
    public final List<String> emission;

    public Material() {
        this.albedo = new ArrayList<>();
        this.normal = new ArrayList<>();
        this.roughness = new ArrayList<>();
        this.metallic = new ArrayList<>();
        this.ao = new ArrayList<>();
        this.emission = new ArrayList<>();
    }

    public List<String> getTextures(TextureType type) {
        return switch (type) {
            case REFLECTION -> null;
            case ALBEDO -> albedo;
            case NORMALS -> normal;
            case ROUGHNESS -> roughness;
            case METALNESS -> metallic;
            case AMBIENT_OCCLUSION -> ao;
            case EMISSIVE -> emission;
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Material) obj;
        return Objects.equals(this.albedo, that.albedo) && Objects.equals(this.normal, that.normal) && Objects.equals(this.roughness, that.roughness) && Objects.equals(this.metallic, that.metallic) && Objects.equals(this.ao, that.ao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(albedo, normal, roughness, metallic, ao, emission);
    }

    @Override
    public String toString() {
        return "Material[" + "albedo=" + albedo + ", " + "normal=" + normal + ", " + "roughness=" + roughness + ", " + "metallic=" + metallic + ", " + "ao=" + ao + ", " + "emission=" + emission + ']';
    }
}
