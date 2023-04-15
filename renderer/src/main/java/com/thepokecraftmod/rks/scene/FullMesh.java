package com.thepokecraftmod.rks.scene;

import com.thepokecraftmod.rks.scene.MeshObject;
import com.thepokecraftmod.rks.scene.RenderObject;
import com.thepokecraftmod.rks.storage.ObjectInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class FullMesh extends RenderObject {

    public final List<MeshObject> objects = new ArrayList<>();

    public void add(MeshObject obj) {
        objects.add(obj);
    }

    @Override
    public void update() {
        for (var t : objects) t.update();
        super.update();
    }

    @Override
    public void render(List<ObjectInstance> instances) {
        for (var object : this.objects) if (!object.hidden) object.render(instances);
    }
}
