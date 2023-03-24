package com.thepokecraftmod.rks.scene;

import com.thepokecraftmod.rks.model.GLModel;
import com.thepokecraftmod.rks.pipeline.ShaderPipeline;
import com.thepokecraftmod.rks.rendering.ObjectInstance;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Map;

public class MeshObject extends RenderObject {

    public GLModel model;

    public void setup(List<Material> glMaterials, Map<String, Material> variants, GLModel model, ShaderPipeline shaderPipeline) {
        this.materials = glMaterials;
        this.variants = variants;
        this.model = model;
        this.shaderPipeline = shaderPipeline;
        this.ready = true;
    }

    public void render(List<ObjectInstance> instances) {
        shaderPipeline.bind();

        for (var instance : instances) {
            instance.update();
            shaderPipeline.updateOtherUniforms(instance, this);
            shaderPipeline.updateTexUniforms(instance, this);
            if (disableBackfaceCull) GL11.glDisable(GL11.GL_CULL_FACE);
            model.runDrawCalls();
            if (disableBackfaceCull) GL11.glEnable(GL11.GL_CULL_FACE);
        }

        shaderPipeline.unbind();
    }
}