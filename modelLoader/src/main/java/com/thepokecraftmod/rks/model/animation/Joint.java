package com.thepokecraftmod.rks.model.animation;

import org.joml.Matrix4f;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AINode;

import java.util.ArrayList;
import java.util.List;

public class Joint {
    public final String name;
    public final Joint parent;
    public final Matrix4f transform;
    public final List<Joint> children = new ArrayList<>();

    private Joint(AINode aiNode, Joint parent) {
        this.name = aiNode.mName().dataString();
        this.parent = parent;
        this.transform = from(aiNode.mTransformation());

        for (int i = 0; i < aiNode.mNumChildren(); i++)
            children.add(new Joint(AINode.create(aiNode.mChildren().get(i)), this));
    }

    @Override
    public String toString() {
        return "Joint{" + "name='" + name + '\'' + '}';
    }

    public static Joint create(AINode aiRoot) {
        return new Joint(aiRoot, null);
    }

    private static void populateJoints(Joint joint, ArrayList<Joint> jointList) {
        jointList.add(joint);
        for (var child : joint.children) populateJoints(child, jointList);
    }

    public static Matrix4f from(AIMatrix4x4 aiMat4) {
        return new Matrix4f().m00(aiMat4.a1()).m10(aiMat4.a2()).m20(aiMat4.a3()).m30(aiMat4.a4()).m01(aiMat4.b1()).m11(aiMat4.b2()).m21(aiMat4.b3()).m31(aiMat4.b4()).m02(aiMat4.c1()).m12(aiMat4.c2()).m22(aiMat4.c3()).m32(aiMat4.c4()).m03(aiMat4.d1()).m13(aiMat4.d2()).m23(aiMat4.d3()).m33(aiMat4.d4());
    }
}
