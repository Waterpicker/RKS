package com.thepokecraftmod.rks.model.material;

import com.thepokecraftmod.rks.model.texture.TextureType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Material {
    public final List<String> diffuse;
    public final List<String> normal;
    public final List<String> roughness;
    public final List<String> metallic;
    public final List<String> ao;

    public Material() {
        this.diffuse = new ArrayList<>();
        this.normal = new ArrayList<>();
        this.roughness = new ArrayList<>();
        this.metallic = new ArrayList<>();
        this.ao = new ArrayList<>();
    }

    public List<String> getTextures(TextureType type) {
        return switch (type) {
            case SPECULAR, LIGHTMAP, UNKNOWN, TRANSMISSION, CLEARCOAT, SHEEN, EMISSION_COLOR, NORMAL_CAMERA, BASE_COLOR, REFLECTION, DISPLACEMENT, OPACITY, SHININESS, HEIGHT, EMISSIVE, AMBIENT ->
                    null;
            case DIFFUSE -> diffuse;
            case NORMALS -> normal;
            case ROUGHNESS -> roughness;
            case METALNESS -> metallic;
            case AMBIENT_OCCLUSION -> ao;
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Material) obj;
        return Objects.equals(this.diffuse, that.diffuse) && Objects.equals(this.normal, that.normal) && Objects.equals(this.roughness, that.roughness) && Objects.equals(this.metallic, that.metallic) && Objects.equals(this.ao, that.ao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diffuse, normal, roughness, metallic, ao);
    }

    @Override
    public String toString() {
        return "Material[" + "diffuse=" + diffuse + ", " + "normal=" + normal + ", " + "roughness=" + roughness + ", " + "metallic=" + metallic + ", " + "ao=" + ao + ']';
    }
}
