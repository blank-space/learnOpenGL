package me.dawn.learnopengl.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;

import me.dawn.learnopengl.LEGLSurfaceView;

public class LCameraView extends LEGLSurfaceView {
    private LCameraRenderer cameraRenderer;
    private LCamera lCamera;

    public LCameraView(Context context) {
        this(context, null);
    }

    public LCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        cameraRenderer = new LCameraRenderer(context);
        lCamera = new LCamera();
        cameraRenderer.setOnSurfaceCreateListener(new LCameraRenderer.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(SurfaceTexture surfaceTexture) {
                lCamera.startPreview(Camera.CameraInfo.CAMERA_FACING_BACK, surfaceTexture);
            }
        });
        setLRenderer(cameraRenderer);

    }

    public void onDestroy() {
        if (lCamera != null) {
            lCamera.stopPreview();
        }
    }
}
