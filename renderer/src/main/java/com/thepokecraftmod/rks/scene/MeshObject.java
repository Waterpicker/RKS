package com.thepokecraftmod.rks.scene;

import com.thepokecraftmod.rks.draw.GLModel;
import com.thepokecraftmod.rks.pipeline.Shader;
import com.thepokecraftmod.rks.storage.ObjectInstance;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class MeshObject extends RenderObject {

    public final GLModel model = new GLModel();
    private final String materialReference;

    public MeshObject(String materialReference) {
        this.materialReference = materialReference;
    }

    public void setup(Shader shader) {
        this.shader = shader;
        this.ready = true;
    }

    public void render(List<ObjectInstance> instances) {
        shader.bind();

        for (var instance : instances) {
            instance.update();
            instance.material.uploadTextures(materialReference);
            if (disableBackfaceCull) GL11.glDisable(GL11.GL_CULL_FACE);
            model.runDrawCalls();
            if (disableBackfaceCull) GL11.glEnable(GL11.GL_CULL_FACE);
        }

        shader.unbind();
    }
}
