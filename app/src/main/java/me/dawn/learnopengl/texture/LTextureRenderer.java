package me.dawn.learnopengl.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import me.dawn.learnopengl.LEGLSurfaceView;
import me.dawn.learnopengl.R;

/**
 * @author : LeeZhaoXing
 * @date : 2021/5/6
 * @desc : 渲染纹理
 */
public class LTextureRenderer implements LEGLSurfaceView.LGLRender {

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
    private int mProgram;
    private int mVPosition;
    private int mFPosition;
    private int mTextureId;
    private int sampler;
    private int vboId;

    public LTextureRenderer(Context context) {
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

    @Override
    public void onSurfaceCreated() {

        initProgram();

        getArrtribAndUniform();

        initVBO();

        initTexture();

    }

    private void getArrtribAndUniform() {
        mVPosition = GLES20.glGetAttribLocation(mProgram, "v_Position");
        mFPosition = GLES20.glGetAttribLocation(mProgram, "f_Position");
        sampler = GLES20.glGetUniformLocation(mProgram, "sTexture");
    }

    /**
     * 创建渲染程序
     */
    private void initProgram() {
        String vertexSource = ShaderUtil.getRawResource(mContext, R.raw.vertex_shader);
        String fragmentSource = ShaderUtil.getRawResource(mContext, R.raw.fragment_shader);
        mProgram = ShaderUtil.createProgram(vertexSource, fragmentSource);
    }

    /**
     * 初始化纹理
     */
    private void initTexture() {
        //生成纹理
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        mTextureId = textureIds[0];

        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        //激活纹理单元unit0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //通过glUniform1i的设置，我们保证每个uniform采样器对应着正确的纹理单元
        GLES20.glUniform1i(sampler, 0);

        //设置环绕方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        //设置过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ava);
        //设置图片
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    /**
     * 初始化VBO
     */
    private void initVBO() {
        //创建VBO
        int[] vbos = new int[1];
        GLES20.glGenBuffers(1, vbos, 0);
        vboId = vbos[0];

        //绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        //分配VBO需要缓存的大小
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexArray.length * 4 + fragmentArray.length * 4,
                null, GLES20.GL_STATIC_DRAW);
        //设置顶点数据的值
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexArray.length * 4, fbVertex);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexArray.length * 4, fragmentArray.length * 4, fbFragment);
        //解绑VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame() {
        //清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1.0f, 0f, 0f, 0f);

        //使用源程序
        GLES20.glUseProgram(mProgram);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        //绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);

        //使顶点属性数组有效
        GLES20.glEnableVertexAttribArray(mVPosition);
        //为顶点属性赋值，使用VBO缓存后，mVPosition的offset从0开始
        GLES20.glVertexAttribPointer(mVPosition, 2, GLES20.GL_FLOAT, false, 8, 0);
        //使顶点属性数组有效
        GLES20.glEnableVertexAttribArray(mFPosition);
        //为顶点属性赋值，，mFPosition的offset从vertexArray.length*4开始
        GLES20.glVertexAttribPointer(mFPosition, 2, GLES20.GL_FLOAT, false, 8, vertexArray.length * 4);

        //绘制图形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        //解绑VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }
}
