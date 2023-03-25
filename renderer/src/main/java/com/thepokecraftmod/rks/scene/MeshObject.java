package com.thepokecraftmod.rks.scene;

import com.thepokecraftmod.rks.model.GLModel;
import com.thepokecraftmod.rks.pipeline.Shader;
import com.thepokecraftmod.rks.storage.ObjectInstance;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class MeshObject extends RenderObject {

    public GLModel model;

    public void setup(GLModel model, Shader shader) {
        this.model = model;
        this.shader = shader;
        this.ready = true;
    }

    public void render(List<ObjectInstance> instances) {
        shader.bind();

        for (var instance : instances) {
            instance.update();
            shader.updateOtherUniforms(instance, this);
            shader.updateTexUniforms(instance, this);
            if (disableBackfaceCull) GL11.glDisable(GL11.GL_CULL_FACE);
            model.runDrawCalls();
            if (disableBackfaceCull) GL11.glEnable(GL11.GL_CULL_FACE);
        }

        shader.unbind();
    }
}
