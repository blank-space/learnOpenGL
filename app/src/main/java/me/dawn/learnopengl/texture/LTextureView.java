package me.dawn.learnopengl.texture;

import android.content.Context;
import android.util.AttributeSet;

import me.dawn.learnopengl.LEGLSurfaceView;

/**
 * @author : LeeZhaoXing
 * @date : 2021/5/7
 * @desc :
 */
public class LTextureView extends LEGLSurfaceView {
    public LTextureView(Context context) {
        this(context, null);
    }

    private LTextureRenderer mLTextureRenderer;
    private int mTextureId;

    public LTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLTextureRenderer = new LTextureRenderer(context);
        setLRenderer(mLTextureRenderer);
    }

    public void onConfigurationChanged(int orientation) {
        if (mLTextureRenderer != null) {
            mLTextureRenderer.setOrientation(orientation);
        }

    }

    public LTextureRenderer getmLTextureRenderer(){
        return mLTextureRenderer;
    }

    public int getTextureId() {
        return mTextureId;
    }
}
