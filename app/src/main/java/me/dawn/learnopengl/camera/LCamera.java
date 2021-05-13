package me.dawn.learnopengl.camera;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import java.io.IOException;

public class LCamera {
    private Camera mCamera;
    public LCamera() {
    }

    public void  startPreview(int cameraId, SurfaceTexture surfaceTexture){
        mCamera = Camera.open(cameraId);
        try {
            mCamera.setPreviewTexture(surfaceTexture);
        }catch (IOException e){
            e.printStackTrace();
        }

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode("off");
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setPictureSize(parameters.getSupportedPictureSizes().get(0).width,parameters.getSupportedPictureSizes().get(0).height);
        parameters.setPreviewSize(parameters.getSupportedPreviewSizes().get(0).width,parameters.getSupportedPreviewSizes().get(0).height);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }


    public void stopPreview(){
        if(mCamera!=null){
            mCamera.stopPreview();
        }
    }
}
