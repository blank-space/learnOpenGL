package me.dawn.learnopengl;

import android.opengl.GLES20;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;

/**
 * @author : LeeZhaoXing
 * @date : 2021/5/4
 * @desc :
 */
public class LRenderer implements LEGLSurfaceView.LGLRender {

    @Override
    public void onSurfaceCreated() {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.d("@@","w:"+width+",h:"+height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame() {
        Log.d("@@","onDrawFrame");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0.5f, 0.0f, 0f, 0f);
    }


}
