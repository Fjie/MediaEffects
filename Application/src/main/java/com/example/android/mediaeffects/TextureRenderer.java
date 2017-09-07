/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.mediaeffects;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class TextureRenderer {

    private int mProgram;
    /**
     * 绑定着色语言 uniform 对象的地址
     */
    private int mTexSamplerHandle;
    private int mTexSamplerHandle2;
    private int mTexCoordHandle;
    private int mTexCoordHandle2;
    private int mPosCoordHandle;

    private FloatBuffer mTexVertices;
    private FloatBuffer mTexVertices2;
    private FloatBuffer mPosVertices;

    private int mViewWidth;
    private int mViewHeight;

    private int mTexWidth;
    private int mTexHeight;

    private static final String VERTEX_SHADER =
            "attribute vec4 a_position;\n" +
                    "attribute vec2 a_texcoord;\n" +
                    "attribute vec2 a_texcoord2;\n" +
                    "varying vec2 v_texcoord;\n" +
                    "varying vec2 v_texcoord2;\n" +
                    "void main() {\n" +
                    "  gl_Position = a_position;\n" +
                    "  v_texcoord = a_texcoord;\n" +
                    "  v_texcoord2 = a_texcoord2;\n" +
                    "}\n";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "uniform sampler2D tex_sampler;\n" +
                    "uniform sampler2D tex_sampler2;\n" +
                    "varying vec2 v_texcoord;\n" +
                    "varying vec2 v_texcoord2;\n" +
                    "void main() {\n" +
                    "  vec4 a = texture2D(tex_sampler, v_texcoord + vec2(0.5, 0.0));\n" +
                    "  vec4 b = texture2D(tex_sampler2, v_texcoord2 + vec2(-0.5, 0.0));\n" +
                    "  vec4 s = a + b;\n" +
                    "  gl_FragColor = vec4(s.r * 0.299 + s.g*0.587 + s.b*0.114);\n" + //手工加个灰度滤镜
                    "}\n";

    private static final float[] TEX_VERTICES = {
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
    };
    private static final float[] TEX_VERTICES_2 = {
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
    };
//    private static final float[] TEX_VERTICES = {
//            0.0f, 1.0f, 0.5f, 1.0f, 0.5f, 0.0f, 1.0f, 0.0f
//    };
//    private static final float[] TEX_VERTICES_2 = {
//            0.5f, 1.0f, 1.0f, 1.0f, 0.5f, 0.0f, 1.0f, 0.0f
//    };

    private static final float[] POS_VERTICES = {
            -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f
    };

    private static final int FLOAT_SIZE_BYTES = 4;

    public void init() {
        // Create program
        mProgram = GLToolbox.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);

        // Bind attributes and uniforms
        mTexSamplerHandle = GLES20.glGetUniformLocation(mProgram, "tex_sampler");
        mTexSamplerHandle2 = GLES20.glGetUniformLocation(mProgram, "tex_sampler2");
        System.out.println("mTexSamplerHandle = " + mTexCoordHandle);
        System.out.println("mTexSamplerHandle2 = " + mTexCoordHandle2);
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_texcoord");
        mTexCoordHandle2 = GLES20.glGetAttribLocation(mProgram, "a_texcoord2");
        mPosCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_position");

        // Setup coordinate buffers
        mTexVertices = ByteBuffer.allocateDirect(TEX_VERTICES.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTexVertices2 = ByteBuffer.allocateDirect(TEX_VERTICES.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTexVertices.put(TEX_VERTICES).position(0);
        mTexVertices2.put(TEX_VERTICES_2).position(0);

        mPosVertices = ByteBuffer.allocateDirect(POS_VERTICES.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mPosVertices.put(POS_VERTICES).position(0);
    }

    public void tearDown() {
        GLES20.glDeleteProgram(mProgram);
    }

    public void updateTextureSize(int texWidth, int texHeight) {
        mTexWidth = texWidth;
        mTexHeight = texHeight;
        computeOutputVertices();
    }

    public void updateViewSize(int viewWidth, int viewHeight) {
        mViewWidth = viewWidth;
        mViewHeight = viewHeight;
        computeOutputVertices();
    }

    public void renderTexture(int texId, int texId2) {
        // Bind default FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        // Use our shader program
        GLES20.glUseProgram(mProgram);
        GLToolbox.checkGlError("glUseProgram");

        // Set viewport
        GLES20.glViewport(0, 0, mViewWidth, mViewHeight);
        GLToolbox.checkGlError("glViewport");

        // Disable blending
//        GLES20.glDisable(GLES20.GL_BLEND);

        // Set the vertex attributes
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTexVertices);
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);

        GLES20.glVertexAttribPointer(mTexCoordHandle2, 2, GLES20.GL_FLOAT, false, 0, mTexVertices2);
        GLES20.glEnableVertexAttribArray(mTexCoordHandle2);

        GLES20.glVertexAttribPointer(mPosCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mPosVertices);
        GLES20.glEnableVertexAttribArray(mPosCoordHandle);
        GLToolbox.checkGlError("vertex attribute setup");

        // Set the input texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLToolbox.checkGlError("glActiveTexture");
//        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
        GLToolbox.checkGlError("glBindTexture");
        GLES20.glUniform1i(mTexSamplerHandle, 0);


        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLToolbox.checkGlError("glActiveTexture");
//        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId2);
        GLToolbox.checkGlError("glBindTexture");
        GLES20.glUniform1i(mTexSamplerHandle2, 1);

        // Draw
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    private void computeOutputVertices() {
        if (mPosVertices != null) {
            float imgAspectRatio = mTexWidth / (float) mTexHeight;
            float viewAspectRatio = mViewWidth / (float) mViewHeight;
            float relativeAspectRatio = viewAspectRatio / imgAspectRatio;
            float x0, y0, x1, y1;
            if (relativeAspectRatio > 1.0f) {
                x0 = -1.0f / relativeAspectRatio;
                y0 = -1.0f;
                x1 = 1.0f / relativeAspectRatio;
                y1 = 1.0f;
            } else {
                x0 = -1.0f;
                y0 = -relativeAspectRatio;
                x1 = 1.0f;
                y1 = relativeAspectRatio;
            }
            float[] coords = new float[]{x0, y0, x1, y0, x0, y1, x1, y1};
            mPosVertices.put(coords).position(0);
        }
    }

}
