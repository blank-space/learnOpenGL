package me.dawn.learnopengl.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import me.dawn.learnopengl.LEGLSurfaceView;
import me.dawn.learnopengl.R;
import me.dawn.learnopengl.fbo.FBORenderer;
import me.dawn.learnopengl.texture.ShaderUtil;

public class LCameraRenderer implements LEGLSurfaceView.LGLRender, SurfaceTexture.OnFrameAvailableListener {

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
    private int fboId;
    private final FBORenderer mFBORenderer;
    private int mProgram;
    private int cameraTextureId;

    public LCameraRenderer(Context mContext) {
        this.mContext = mContext;
        mFBORenderer = new FBORenderer(mContext);
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
        Log.d("@@", "onSurfaceCreated()");
        mFBORenderer.onCreate();
        initProgram();
        getAttribAndUniform();
        initVBO();
        initFBO();
        initTexture();
        setupFBO();
        loadCameraTextureId();
    }

    /**
     * 创建渲染程序
     */
    private void initProgram() {
        String vertexSource = ShaderUtil.getRawResource(mContext, R.raw.matrix_vertex_shader);
        String fragmentSource = ShaderUtil.getRawResource(mContext, R.raw.fragment_shader);
        mProgram = ShaderUtil.createProgram(vertexSource, fragmentSource);
    }

    private void getAttribAndUniform() {
        mVPosition = GLES20.glGetAttribLocation(mProgram, "v_Position");
        mFPosition = GLES20.glGetAttribLocation(mProgram, "f_Position");
        sampler = GLES20.glGetUniformLocation(mProgram, "sTexture");
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

    private void initFBO() {
        int[] fbos = new int[1];
        GLES20.glGenBuffers(1, fbos, 0);
        fboId = fbos[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
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
    }


    private void setupFBO() {
        //设置fbo分配内存大小
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 1080, 1920, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        //mTextureId绑定到fbo
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTextureId, 0);
        //检查fbo是否绑定成功
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("@@", "FBO绑定失败");
        } else {
            Log.d("@@", "FBO绑定成功");
        }
        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        //解绑fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private SurfaceTexture surfaceTexture;

    private void loadCameraTextureId() {
        int[] cameraIds = new int[1];
        GLES20.glGenTextures(1, cameraIds, 0);
        cameraTextureId = cameraIds[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTextureId);
        //设置环绕方式
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        //设置过滤方式
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        surfaceTexture = new SurfaceTexture(cameraTextureId);
        surfaceTexture.setOnFrameAvailableListener(this);
        if (onSurfaceCreateListener != null) {
            onSurfaceCreateListener.onSurfaceCreate(surfaceTexture);
        }
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

    }


    @Override
    public void onSurfaceChanged(int width, int height) {
        mFBORenderer.onChange(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame() {
        surfaceTexture.updateTexImage();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f, 0f, 0f, 1f);

        GLES20.glUseProgram(program);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        GLES20.glEnableVertexAttribArray(mVPosition);
        GLES20.glVertexAttribPointer(mVPosition, 2, GLES20.GL_FLOAT, false, 8, 0);
        GLES20.glEnableVertexAttribArray(mFPosition);
        GLES20.glVertexAttribPointer(mFPosition, 2, GLES20.GL_FLOAT, false, 8, vertexArray.length * 4);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        //解绑VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        //解绑fbo，切换到窗口模式
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        //拿到后的mTextureId，在窗口上绘制
        mFBORenderer.onDraw(mTextureId);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    private OnSurfaceCreateListener onSurfaceCreateListener;

    public void setOnSurfaceCreateListener(OnSurfaceCreateListener onSurfaceCreateListener) {
        this.onSurfaceCreateListener = onSurfaceCreateListener;
    }

    public interface OnSurfaceCreateListener {
        void onSurfaceCreate(SurfaceTexture surfaceTexture);
    }
}
