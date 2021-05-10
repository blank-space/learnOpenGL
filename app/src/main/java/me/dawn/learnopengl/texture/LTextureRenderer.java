package me.dawn.learnopengl.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import me.dawn.learnopengl.LEGLSurfaceView;
import me.dawn.learnopengl.R;
import me.dawn.learnopengl.fbo.FBORenderer;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

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
     * note:使用FBO后，改成FBO纹理坐标系
     */
    private float[] fragmentArray = {
            0f, 0f,
            1f, 0f,
            0f, 1f,
            1f, 1f

    };
    private FloatBuffer fbVertex;
    private FloatBuffer fbFragment;
    private int mProgram;
    private int mVPosition;
    private int mFPosition;
    private int mTextureId;
    private int sampler;
    private int vboId;
    private int fboId;
    private int imgTextureId;
    private final FBORenderer mFBORenderer;
    private int umatrix;
    private float[] matrix = new float[16];
    private int orientation=ORIENTATION_PORTRAIT;


    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public LTextureRenderer(Context context) {
        mContext = context;
        mFBORenderer = new FBORenderer(context);
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

    /**
     * 获得屏幕高度
     *
     * @return
     */
    public int getScreenHeight() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    public int getScreenWidth() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    @Override
    public void onSurfaceCreated() {
        Log.d("@@","onSurfaceCreated()");
        mFBORenderer.onCreate();
        initProgram();
        getAttribAndUniform();
        initVBO();
        initFBO();
        initTexture();
        setupFBO();
        imgTextureId = loadTexrute(R.drawable.androids);
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
        umatrix = GLES20.glGetUniformLocation(mProgram, "u_Matrix");
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
        if (orientation == ORIENTATION_PORTRAIT) {
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 1080, 2210, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        } else {
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 2210, 1080, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        }
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

    /**
     * 获取需要绘制的图片纹理，然后绘制渲染
     *
     * @param redId
     * @return
     */
    private int loadTexrute(int redId) {
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);

        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);

        /* //激活纹理单元unit0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //通过glUniform1i的设置，我们保证每个uniform采样器对应着正确的纹理单元
        GLES20.glUniform1i(sampler, 0);*/

        //设置环绕方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        //设置过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), redId);
        //设置图片
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        return textureIds[0];
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.d("@@","onSurfaceChanged()");
        GLES20.glViewport(0, 0, width, height);
        mFBORenderer.onChange(width, height);
        //1024 1820
       /* if (width > height) {
            Matrix.orthoM(matrix, 0, -width / ((height / 1820f) * 1024f), width / ((height / 1820f) * 1024f), -1f, 1f, -1f, 1f);
        } else {
            Matrix.orthoM(matrix, 0, -1, 1, -height / ((width / 1024f) * 1820f), height / ((width /  1024f) * 1820f), -1f, 1f);
        }*/

        if (width > height) {
            Matrix.orthoM(matrix, 0, -width / ((height / 702f) * 526f), width / ((height / 702f) * 526f), -1f, 1f, -1f, 1f);
        } else {
            Matrix.orthoM(matrix, 0, -1, 1, -height / ((width / 526f) * 702f), height / ((width / 526f) * 702f), -1f, 1f);
        }

    }

    @Override
    public void onDrawFrame() {
        Log.d("@@","onDrawFrame()");
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
        //清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1.0f, 0f, 0f, 0f);

        //使用源程序
        GLES20.glUseProgram(mProgram);
        GLES20.glUniformMatrix4fv(umatrix, 1, false, matrix, 0);
        //使用fbo，将mTextureId改成imgTextureId, 进行离屏渲染
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imgTextureId);
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
        //解绑fbo，切换到窗口模式
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        //拿到后的mTextureId，在窗口上绘制
        mFBORenderer.onDraw(mTextureId);
    }
}
