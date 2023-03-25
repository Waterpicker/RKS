package com.thepokecraftmod.rks.scene;

import com.thepokecraftmod.rks.storage.ObjectInstance;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Stores multiple separate render objects of the same type into one {@link RenderObject}
 *
 * @param <T> the type to use
 */
public class MultiRenderObject<T extends RenderObject> extends RenderObject {

    public float scale;
    public final List<T> hiddenObjects = new ArrayList<>();
    public final List<T> objects = new ArrayList<>();
    private final List<Consumer<T>> queue = new ArrayList<>();
    private boolean dirty = true;
    private boolean smartRender = false;
    private Matrix4f rootTransformation = new Matrix4f();
    private final Vector3f dimensions = new Vector3f();

    @Override
    public boolean isReady() {
        if (objects.isEmpty()) return false;
        for (T object : objects) if (!object.isReady()) return false;
        return true;
    }

    public void onUpdate(Consumer<T> consumer) {
        queue.add(consumer);
    }

    public void add(T obj, boolean hidden) {
        if(hidden) hiddenObjects.add(obj);
        else objects.add(obj);
        dirty = true;
    }

    public void unhideAll() {
        objects.addAll(hiddenObjects);
        hiddenObjects.clear();
    }

    public void setRootTransformation(Matrix4f rootTransformation) {
        this.rootTransformation = rootTransformation;
    }

    public Matrix4f getRootTransformation() {
        return rootTransformation;
    }

    public void applyRootTransformation(ObjectInstance state) {
        state.transformationMatrix.mul(rootTransformation, state.transformationMatrix);
    }

    public Vector3f getDimensions() {
        return dimensions;
    }

    @Override
    public void update() {
        for (T t : objects) {
            t.update();
        }

        if (objects.get(0) != null && objects.get(0).isReady()) {
            for (var consumer : queue) {
                consumer.accept(objects.get(0));
            }
        }

        queue.clear();
        super.update();
    }

    @Override
    public void render(List<ObjectInstance> instances) {
        if (dirty) {
            shader = null;
            smartRender = true;
            for (T object : objects) {
                if (shader == null) shader = object.shader;
                else if (shader != object.shader) smartRender = false;
            }
        }

        if (smartRender && isReady()) {
            shader.bind();

            for (var instance : instances) {
                instance.update();
                shader.updateOtherUniforms(instance, objects.get(0));

                for (T object : objects) {
                    if (object instanceof MeshObject meshObject) {
                        shader.updateTexUniforms(instance, object);
                        meshObject.model.runDrawCalls();
                    }
                }
            }

            shader.unbind();
        } else {
            for (T object : this.objects) {
                object.render(instances);
            }
        }
    }
}
