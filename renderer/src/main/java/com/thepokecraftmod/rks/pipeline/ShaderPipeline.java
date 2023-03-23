package com.thepokecraftmod.rks.pipeline;

import com.pokemod.rarecandy.components.RenderObject;
import com.pokemod.rarecandy.rendering.ObjectInstance;
import com.pokemod.rarecandy.rendering.RareCandy;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public record ShaderPipeline(Map<String, Consumer<UniformUploadContext>> uniformSuppliers, Map<String, Uniform> uniforms, Runnable preDrawBatch, Runnable postDrawBatch, int program) {

    public void bind() {
        GL20C.glUseProgram(program);
        preDrawBatch.run();
    }

    public void unbind() {
        postDrawBatch.run();
    }

    public void updateOtherUniforms(ObjectInstance instance, RenderObject renderObject) {
        for (var name : uniforms.keySet()) {
            var uniform = uniforms.get(name);
            if (!uniformSuppliers.containsKey(name)) continue; // FIXME: issue introduced with new block system.
            if (uniform.type != GL20C.GL_SAMPLER_2D) uniformSuppliers.get(name).accept(new UniformUploadContext(renderObject, instance, uniform));
        }
    }

    public void updateTexUniforms(ObjectInstance instance, RenderObject renderObject) {
        for (var name : uniforms.keySet()) {
            var uniform = uniforms.get(name);
            if (uniform.type == GL20C.GL_SAMPLER_2D) uniformSuppliers.get(name).accept(new UniformUploadContext(renderObject, instance, uniform));
        }
    }
    public static class Builder {

        private Map<String, Consumer<UniformUploadContext>> uniformSuppliers = new HashMap<>();
        private int program;
        public Map<String, Uniform> uniforms = new HashMap<>();
        public Runnable preDrawBatch = () -> {};
        public Runnable postDrawRunBatch = () -> {};

        public Builder() {
        }

        public Builder(Builder base) {
            this.uniformSuppliers = new HashMap<>(base.uniformSuppliers);
            this.program = base.program;
            this.uniforms = new HashMap<>(base.uniforms);
            this.preDrawBatch = base.preDrawBatch;
            this.postDrawRunBatch = base.postDrawRunBatch;
        }

        private void addShader(String text, int type, int programId) {
            var shader = GL20C.glCreateShader(type);
            if (shader == 0) RareCandy.fatal("an error occurred creating the shader object. We don't know what it is.");
            GL20C.glShaderSource(shader, text);
            GL20C.glCompileShader(shader);
            if (GL20C.glGetShaderi(shader, GL20C.GL_COMPILE_STATUS) == 0)
                RareCandy.fatal(GL20C.glGetShaderInfoLog(shader, 1024));
            GL20C.glAttachShader(programId, shader);
        }

        private void compileShader(int programId) {
            GL20C.glLinkProgram(programId);
            if (GL20C.glGetProgrami(programId, GL20C.GL_LINK_STATUS) == 0)
                RareCandy.fatal(GL20C.glGetProgramInfoLog(programId, 1024));
            GL20C.glValidateProgram(programId);
            if (GL20C.glGetProgrami(programId, GL20C.GL_VALIDATE_STATUS) == 0)
                RareCandy.fatal(GL20C.glGetProgramInfoLog(programId, 1024));
        }

        public Builder prePostDraw(Runnable preDrawBatch, Runnable postDrawRunBatch) {
            this.preDrawBatch = preDrawBatch;
            this.postDrawRunBatch = postDrawRunBatch;
            return this;
        }

        public Builder uniform(UniformBlockReference blockName) {
            var index = GL33C.glGetUniformBlockIndex(program, blockName.name());
            GL33C.glUniformBlockBinding(program, index, blockName.binding());
            return this;
        }

        @Deprecated(forRemoval = true)
        public Builder supplyUniform(String name, Consumer<UniformUploadContext> provider) {
            uniformSuppliers.put(name, provider);
            return this;
        }

        public Builder shader(@NotNull String vs, @NotNull String fs) {
            this.program = GL20C.glCreateProgram();
            addShader(vs, GL20C.GL_VERTEX_SHADER, program);
            addShader(fs, GL20C.GL_FRAGMENT_SHADER, program);
            compileShader(program);

            // Load legacy uniforms
            try (var stack = MemoryStack.stackPush()) {
                var pUniformCount = stack.ints(1);
                GL20C.glGetProgramiv(program, GL20C.GL_ACTIVE_UNIFORMS, pUniformCount);
                var uniformCount = pUniformCount.get(0);

                var pMaxNameLength = stack.ints(1);
                GL20C.glGetProgramiv(program, GL20.GL_ACTIVE_UNIFORM_MAX_LENGTH, pMaxNameLength);

                for (int i = 0; i < uniformCount; i++) {
                    var pSize = stack.ints(1);
                    var pType = stack.ints(1);
                    var name = GL20C.glGetActiveUniform(program, i, pMaxNameLength.get(0), pSize, pType);

                    if (name.contains("[")) {
                        name = name.substring(0, name.indexOf('['));
                    }

                    this.uniforms.put(name, new Uniform(program, name, pType.get(0), pSize.get(0)));
                }
            }

            return this;
        }

        public ShaderPipeline build() {
            if (this.program == 0) throw new RuntimeException("Shader not created");
            return new ShaderPipeline(uniformSuppliers, uniforms, preDrawBatch, postDrawRunBatch, program);
        }
    }
}