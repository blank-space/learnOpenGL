package me.dawn.learnopengl;

import android.content.Context;
import android.util.AttributeSet;

import me.dawn.learnopengl.texture.LTextureRenderer;

/**
 * @author : LeeZhaoXing
 * @date : 2021/5/5
 * @desc :
 */
public class LGLSurfaceView extends  LEGLSurfaceView{
    public LGLSurfaceView(Context context) {
        this(context,null);
    }

    public LGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLRenderer(new LTextureRenderer(context));
    }
}
