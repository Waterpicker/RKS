package com.thepokecraftmod.rks.model.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Skeleton {
    public final Joint[] boneArray;
    public final Map<String, Joint> boneMap;
    public final Joint rootNode;

    public Skeleton(Joint root) {
        var jointList = new ArrayList<Joint>();
        populateJoints(root, jointList);
        this.rootNode = root;
        this.boneArray = new Joint[jointList.size()];
        this.boneMap = new HashMap<>(jointList.size());

        for (var joint : jointList) {
            this.boneArray[joint.id] = joint;
            this.boneMap.put(joint.name, joint);
        }
    }

    @Deprecated // TODO: use bone id to animation bone id map to convert instead of having to do this.
    public Skeleton(Skeleton skeleton) {
        this(skeleton.rootNode);
    }

    private static void populateJoints(Joint joint, ArrayList<Joint> jointList) {
        jointList.add(joint);
        for (var child : joint.children) populateJoints(child, jointList);
    }

    public Joint get(String name) {
        return boneMap.get(name);
    }

    public Joint get(int id) {
        if (id > boneArray.length)
            throw new RuntimeException("Animation is referencing bones which are out of bounds. Model is missing bone " + id);
        return boneArray[id];
    }

    public String getName(int id) {
        var bone = get(id);
        for (var entry : boneMap.entrySet()) if (entry.getValue().equals(bone)) return entry.getKey();
        return "";
    }

    public int getId(Joint bone) {
        for (int i = 0; i < boneArray.length; i++)
            if (bone.equals(boneArray[i])) return i;

        return 0;
    }
}
