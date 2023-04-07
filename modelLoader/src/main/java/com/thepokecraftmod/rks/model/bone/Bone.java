package com.thepokecraftmod.rks.model.bone;

import com.thepokecraftmod.rks.model.animation.Joint;
import org.joml.Matrix4f;
import org.lwjgl.assimp.AIBone;

import java.util.Objects;

public class Bone {

    public String name;
    public VertexWeight[] weights;
    public Matrix4f offsetMatrix;

    @Override
    public String toString() {
        return name;
    }

    public static Bone from(AIBone bone) {
        var b = new Bone();
        b.offsetMatrix = Joint.from(bone.mOffsetMatrix());
        b.name = bone.mName().dataString();

        var aiWeights = Objects.requireNonNull(bone.mWeights());
        var vertexWeights = new Bone.VertexWeight[aiWeights.capacity()];
        for (int i = 0; i < aiWeights.capacity(); i++) {
            var aiWeight = aiWeights.get(i);
            vertexWeights[i] = new Bone.VertexWeight(aiWeight.mVertexId(), aiWeight.mWeight());
        }

        b.weights = vertexWeights;
        return b;
    }

    public static class VertexWeight {

        public int vertexId;
        public float weight;

        public VertexWeight(int vertexId, float weight) {
            this.vertexId = vertexId;
            this.weight = weight;
        }
    }
}
