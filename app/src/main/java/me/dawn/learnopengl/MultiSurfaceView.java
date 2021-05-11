package me.dawn.learnopengl;

import android.content.Context;
import android.util.AttributeSet;

/**
 * @author : LeeZhaoXing
 * @date : 2021/5/11
 * @desc :
 */
class MultiSurfaceView extends LGLSurfaceView {
    private MultiRenderer multiRenderer;

    public MultiSurfaceView(Context context) {
        this(context, null);
    }

    public MultiSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        multiRenderer = new MultiRenderer(context);
        setLRenderer(multiRenderer);
    }


    public void setTextureId(int textureId, int index) {
        multiRenderer.setTextureId(textureId, index);
    }
}
