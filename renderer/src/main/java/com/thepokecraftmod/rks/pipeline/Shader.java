package com.thepokecraftmod.rks.pipeline;

import com.thepokecraftmod.rks.model.texture.TextureType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL33C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Shader(
        List<TextureType> texturesUsed,
        Runnable preDrawBatch,
        Runnable postDrawBatch,
        int program,
        Map<String, Integer> uniformLocationCache
) {
    private static final Logger LOGGER = LoggerFactory.getLogger("RKS Shader");

    public void bind() {
        GL20C.glUseProgram(program);
        preDrawBatch.run();
    }

    public int getUniform(String name) {
        return uniformLocationCache.computeIfAbsent(name, s -> GL20C.glGetUniformLocation(program, s));
    }

    public void uploadInt(String name, int value) {
        int loc = getUniform(name);
        GL20C.glUniform1i(loc, value);
    }

    public void uploadVec3f(String name, Vector3f value) {
        int loc = getUniform(name);
        GL20C.glUniform3f(loc, value.x(), value.y(), value.z());
    }

    public void uploadVec3fs(String name, List<Vector3f> values) {
        for (int i = 0; i < values.size(); i++) {
            var loc = getUniform(name + "[" + i + "]");
            var value = values.get(i);

            GL20C.glUniform3f(loc, value.x(), value.y(), value.z());
        }
    }

    public void unbind() {
        postDrawBatch.run();
    }

    public static class Builder {

        private int program;
        private final List<TextureType> typeToSlotMap = new ArrayList<>();
        private Runnable preDrawBatch = () -> {
        };
        private Runnable postDrawRunBatch = () -> {
        };

        public Builder() {
        }

        private void addShader(String text, int type, int programId) {
            var shader = GL20C.glCreateShader(type);
            if (shader == 0) LOGGER.error("an error occurred creating the shader object. We don't know what it is.");
            GL20C.glShaderSource(shader, text);
            GL20C.glCompileShader(shader);
            if (GL20C.glGetShaderi(shader, GL20C.GL_COMPILE_STATUS) == 0)
                LOGGER.error(GL20C.glGetShaderInfoLog(shader, 1024));
            GL20C.glAttachShader(programId, shader);
        }

        private void compileShader(int programId) {
            GL20C.glLinkProgram(programId);
            if (GL20C.glGetProgrami(programId, GL20C.GL_LINK_STATUS) == 0)
                throw new RuntimeException(GL20C.glGetProgramInfoLog(programId, 1024));
            GL20C.glValidateProgram(programId);
            if (GL20C.glGetProgrami(programId, GL20C.GL_VALIDATE_STATUS) == 0)
                throw new RuntimeException(GL20C.glGetProgramInfoLog(programId, 1024));
        }

        public Builder prePostDraw(Runnable preDrawBatch, Runnable postDrawRunBatch) {
            this.preDrawBatch = preDrawBatch;
            this.postDrawRunBatch = postDrawRunBatch;
            return this;
        }

        public Builder texture(TextureType type) {
            typeToSlotMap.add(type);
            return this;
        }

        public Builder uniform(UniformBlockReference blockName) {
            var index = GL33C.glGetUniformBlockIndex(program, blockName.name());
            GL33C.glUniformBlockBinding(program, index, blockName.binding());
            return this;
        }

        public Builder shader(@NotNull String vs, @NotNull String fs) {
            this.program = GL20C.glCreateProgram();
            addShader(vs, GL20C.GL_VERTEX_SHADER, program);
            addShader(fs, GL20C.GL_FRAGMENT_SHADER, program);
            compileShader(program);
            return this;
        }

        public Shader build() {
            if (program == 0) throw new RuntimeException("Shader not created");
            return new Shader(typeToSlotMap, preDrawBatch, postDrawRunBatch, program, new HashMap<>());
        }
    }
}
