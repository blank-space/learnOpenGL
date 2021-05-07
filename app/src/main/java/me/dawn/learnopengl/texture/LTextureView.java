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

    public LTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLRenderer(new LTextureRenderer(context));
    }
}
