package com.thepokecraftmod.rks.scene;

import com.thepokecraftmod.rks.storage.ObjectInstance;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Stores multiple separate render objects of the same type into one {@link RenderObject}
 *
 * @param <T> the type to use
 */
public class MultiRenderObject<T extends RenderObject> extends RenderObject {

    public final List<T> objects = new ArrayList<>();
    private final List<Consumer<T>> queue = new ArrayList<>();

    public void onUpdate(Consumer<T> consumer) {
        queue.add(consumer);
    }

    public void add(T obj) {
        objects.add(obj);
    }


    @Override
    public void update() {
        for (T t : objects) {
            t.update();
        }

        if (objects.get(0) != null) {
            for (var consumer : queue) {
                consumer.accept(objects.get(0));
            }
        }

        queue.clear();
        super.update();
    }

    @Override
    public void render(List<ObjectInstance> instances) {
        for (T object : this.objects) if(!object.hidden) object.render(instances);
    }
}
