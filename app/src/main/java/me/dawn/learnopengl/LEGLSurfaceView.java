package me.dawn.learnopengl;

import android.content.Context;
import android.opengl.EGL14;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * @author : LeeZhaoXing
 * @date : 2021/5/4
 * @desc :
 */
public class LEGLSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    private int mRenderMode = RENDERMODE_CONTINUOUSLY;
    private LGLRender mLRenderer;
    private LGLThread mLGLThread;
    private Surface mSurface;
    private EGLContext mEGLContext;

    public LEGLSurfaceView(Context context) {
        this(context, null);
    }

    public LEGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public void setSurfaceAndEglContext(Surface surface, EGLContext eglContext) {
        this.mSurface = surface;
        this.mEGLContext = eglContext;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (mSurface == null) {
            mSurface = holder.getSurface();
        }
        mLGLThread = new LGLThread(new WeakReference(this));
        mLGLThread.isCreate = true;
        mLGLThread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

        mLGLThread.width = width;
        mLGLThread.height = height;
        mLGLThread.isChange = true;

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        mLGLThread.onDestroy();
        mLGLThread = null;
        mSurface = null;
        mEGLContext = null;
    }

    public interface LGLRender {
        void onSurfaceCreated();

        void onSurfaceChanged(int width, int height);

        void onDrawFrame();
    }


    public void setLRenderer(LGLRender renderer) {
        mLRenderer = renderer;
    }

    public EGLContext getEglContext() {
        if (mLGLThread != null) {
            return mLGLThread.getEglContext();
        }
        return null;
    }

    public void requestRenderer() {
        if (mLGLThread != null) {
            mLGLThread.requestRenderer();
        }
    }

    public void setRenderMode(int renderMode) {
        mRenderMode = renderMode;
    }

    static class LGLThread extends Thread {
        private WeakReference<LEGLSurfaceView> mGLSurfaceViewWeakRef;
        private LEglHelper mLEglHelper;
        private boolean isStart = false;
        private boolean isExit = false;
        private boolean isChange = false;
        private boolean isCreate = false;
        private int width;
        private int height;
        private Object mObject;

        public LGLThread(WeakReference<LEGLSurfaceView> weakReference) {
            mGLSurfaceViewWeakRef = weakReference;
        }

        @Override
        public void run() {
            super.run();
            isStart = false;
            isExit = false;
            mObject = new Object();
            setName("LGLThread " + getId());
            mLEglHelper = new LEglHelper();
            mLEglHelper.initEgl(mGLSurfaceViewWeakRef.get().mSurface, mGLSurfaceViewWeakRef.get().mEGLContext);

            while (true) {
                if (isExit) {
                    release();
                    break;
                }
                if (isStart) {
                    if (mGLSurfaceViewWeakRef.get().mRenderMode == RENDERMODE_WHEN_DIRTY) {
                        synchronized (mObject) {
                            try {
                                //阻塞
                                mObject.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    } else if (mGLSurfaceViewWeakRef.get().mRenderMode == RENDERMODE_CONTINUOUSLY) {
                        //自动渲染，60fps
                        synchronized (mObject) {
                            try {
                                Thread.sleep(1000 / 60);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        throw new RuntimeException("mRenderMode is wrong value");
                    }

                }

                onCreate();
                onChange(width, height);
                draw();

                isStart = true;
            }


        }

        private void draw() {
            if (mGLSurfaceViewWeakRef.get().mLRenderer != null && mLEglHelper != null) {
                mGLSurfaceViewWeakRef.get().mLRenderer.onDrawFrame();
                if (!isStart) {
                    //第一次调用onDrawFrame渲染不出画面，需要多执行一次
                    mGLSurfaceViewWeakRef.get().mLRenderer.onDrawFrame();
                }
                mLEglHelper.swapBuffers();
            }
        }

        private void onChange(int width, int height) {
            if (isChange && mGLSurfaceViewWeakRef.get().mLRenderer != null) {
                Log.d("@@", "w1:" + width + ",h1:" + height);
                isChange = false;
                mGLSurfaceViewWeakRef.get().mLRenderer.onSurfaceChanged(width, height);

            }
        }

        private void onCreate() {
            if (isCreate && mGLSurfaceViewWeakRef.get().mLRenderer != null) {
                isCreate = false;
                mGLSurfaceViewWeakRef.get().mLRenderer.onSurfaceCreated();

            }
        }

        private void release() {
            if (mLEglHelper != null) {
                mLEglHelper.destroyEgl();
                mLEglHelper = null;
                mObject = null;
                mGLSurfaceViewWeakRef = null;
            }
        }

        public void onDestroy() {
            isExit = true;
            requestRenderer();
        }

        public EGLContext getEglContext() {
            if (mLEglHelper != null) {
                return mLEglHelper.getEglContext();
            }
            return null;
        }

        /**
         * 手动唤醒锁，继续渲染
         */
        public void requestRenderer() {
            if (mObject != null) {
                synchronized (mObject) {
                    mObject.notifyAll();
                }
            }
        }
    }

    static class LEglHelper {
        private EGLSurface mEglSurface;
        private EGLContext mEglContext;
        private EGL10 mEgl;
        private EGLDisplay mEglDisplay;

        public LEglHelper() {
        }

        public void initEgl(Surface surface, EGLContext eglContext) {
            //1、得到egl实例
            mEgl = (EGL10) EGLContext.getEGL();

            //2、得到默认的显示设备
            mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new RuntimeException("eglGetDisplay failed");
            }

            //3、初始化默认显示设备
            int[] version = new int[2];
            if (!mEgl.eglInitialize(mEglDisplay, version)) {
                throw new RuntimeException("eglInitialize failed");
            }

            //4、设置显示设备的属性
            int[] attrbutes = new int[]{
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_ALPHA_SIZE, 8,
                    EGL10.EGL_DEPTH_SIZE, 8,
                    EGL10.EGL_STENCIL_SIZE, 8,
                    EGL10.EGL_RENDERABLE_TYPE, 4,
                    EGL10.EGL_NONE};

            int[] num_config = new int[1];
            if (!mEgl.eglChooseConfig(mEglDisplay, attrbutes, null, 1, num_config)) {
                throw new IllegalArgumentException("eglChooseConfig failed");
            }

            int numConfigs = num_config[0];
            if (numConfigs <= 0) {
                throw new IllegalArgumentException(
                        "No configs match configSpec");
            }

            //5、从系统中获得对应属性的配置
            EGLConfig[] configs = new EGLConfig[numConfigs];
            if (!mEgl.eglChooseConfig(mEglDisplay, attrbutes, configs, numConfigs,
                    num_config)) {
                throw new IllegalArgumentException("eglChooseConfig#2 failed");
            }

            //6、创建EglContext
            int[] attrib_list = {EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};
            if (eglContext != null) {
                mEglContext = mEgl.eglCreateContext(mEglDisplay, configs[0], eglContext, attrib_list);
            } else {
                mEglContext = mEgl.eglCreateContext(mEglDisplay, configs[0], EGL10.EGL_NO_CONTEXT, attrib_list);
            }

            //7、创建Surface
            mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay, configs[0], surface, null);

            //8、绑定EglContext和surface到显示设备
            if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
                throw new RuntimeException("eglMakeCurrent fail");
            }

        }

        public boolean swapBuffers() {
            //9、刷新数据，显示渲染场景
            if (mEgl != null) {
                return mEgl.eglSwapBuffers(mEglDisplay, mEglSurface);
            } else {
                throw new RuntimeException("egl is null");
            }
        }

        public EGLContext getEglContext() {
            return mEglContext;
        }

        public void destroyEgl() {
            if (mEgl != null) {
                mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_CONTEXT);

                mEgl.eglDestroySurface(mEglDisplay, mEglSurface);
                mEglSurface = null;
                mEgl.eglDestroyContext(mEglDisplay, mEglContext);
                mEglContext = null;
                mEgl.eglTerminate(mEglDisplay);
                mEglDisplay = null;
                mEgl = null;
            }
        }
    }
}
