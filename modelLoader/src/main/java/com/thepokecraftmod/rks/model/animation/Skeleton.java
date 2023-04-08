package com.thepokecraftmod.rks.model.animation;

import com.thepokecraftmod.rks.model.bone.Bone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Skeleton {
    public final Joint[] joints;
    public final Map<String, Joint> jointMap;
    public final Map<String, Bone> boneMap;
    public final Joint rootNode;
    public Bone[] bones;

    public Skeleton(Joint root) {
        var jointList = new ArrayList<Joint>();
        populateJoints(root, jointList);
        this.rootNode = root;
        this.joints = new Joint[jointList.size()];
        this.jointMap = new HashMap<>(jointList.size());
        this.boneMap = new HashMap<>(jointList.size());

        for (int i = 0; i < jointList.size(); i++) {
            var joint = jointList.get(i);
            this.joints[i] = joint;
            this.jointMap.put(joint.name, joint);
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
        return jointMap.get(name);
    }

    public Joint get(int id) {
        return joints[id];
    }

    public String getName(int id) {
        var bone = get(id);
        for (var entry : jointMap.entrySet()) if (entry.getValue().equals(bone)) return entry.getKey();
        return "";
    }

    public int getId(Bone bone) {
        for (int i = 0; i < bones.length; i++)
            if (bone.equals(bones[i])) return i;

        return 0;
    }

    public void link(Bone[] bones) {
        if (this.bones == null) {
            this.bones = bones;

            for (var bone : bones) {
                if (!boneMap.containsKey(bone.name)) boneMap.put(bone.name, bone);
            }
        }
        else {
        }
    }

    public Bone getBone(String name) {
        return boneMap.get(name);
    }
}
