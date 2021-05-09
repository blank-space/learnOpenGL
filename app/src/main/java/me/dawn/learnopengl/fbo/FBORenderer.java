package me.dawn.learnopengl.fbo;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import me.dawn.learnopengl.R;
import me.dawn.learnopengl.texture.ShaderUtil;

/**
 * @author : LeeZhaoXing
 * @date : 2021/5/9
 * @desc :
 */
public class FBORenderer {
    private Context mContext;
    /**
     * 顶点着色器的坐标
     */
    private float[] vertexArray = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f
    };

    /**
     * 纹理坐标，注：每个点的位置与顶点坐标系一一对应
     */
    private float[] fragmentArray = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };
    private FloatBuffer fbVertex;
    private FloatBuffer fbFragment;
    private int program;
    private int mVPosition;
    private int mFPosition;
    private int mTextureId;
    private int sampler;
    private int vboId;


    public FBORenderer(Context context) {
        mContext = context;
        allocateMemory();
    }
    private void allocateMemory() {
        fbFragment = ByteBuffer.allocateDirect(fragmentArray.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer().put(fragmentArray);
        fbFragment.position(0);


        fbVertex = ByteBuffer.allocateDirect(vertexArray.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer().put(vertexArray);
        fbVertex.position(0);

    }

    public void onDraw(int textureId) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f,0f, 0f, 1f);

        GLES20.glUseProgram(program);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);


        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);

        GLES20.glEnableVertexAttribArray(mVPosition);
        GLES20.glVertexAttribPointer(mVPosition, 2, GLES20.GL_FLOAT, false, 8,
                0);

        GLES20.glEnableVertexAttribArray(mFPosition);
        GLES20.glVertexAttribPointer(mFPosition, 2, GLES20.GL_FLOAT, false, 8,
                vertexArray.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public void onChange(int width,int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    public void onCreate() {
        String vertexSource = ShaderUtil.getRawResource(mContext, R.raw.vertex_shader);
        String fragmentSource = ShaderUtil.getRawResource(mContext, R.raw.fragment_shader);

        program = ShaderUtil.createProgram(vertexSource, fragmentSource);

        mVPosition = GLES20.glGetAttribLocation(program, "v_Position");
        mFPosition = GLES20.glGetAttribLocation(program, "f_Position");
        sampler = GLES20.glGetUniformLocation(program, "sTexture");

        int [] vbos = new int[1];
        GLES20.glGenBuffers(1, vbos, 0);
        vboId = vbos[0];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexArray.length * 4 + fragmentArray.length * 4, null, GLES20. GL_STATIC_DRAW);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexArray.length * 4, fbVertex);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexArray.length * 4, fragmentArray.length * 4, fbFragment);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }
}
