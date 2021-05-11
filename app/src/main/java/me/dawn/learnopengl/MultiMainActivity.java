package me.dawn.learnopengl;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import me.dawn.learnopengl.texture.LTextureView;

public class MultiMainActivity extends AppCompatActivity {

    private LTextureView lTextureView;
    private LinearLayout mLL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lTextureView = findViewById(R.id.surface);
        mLL = findViewById(R.id.ll);
        mLL.removeAllViews();
        for (int i = 0; i < 3; i++) {
            MultiSurfaceView multiSurfaceView = new MultiSurfaceView(this);
            multiSurfaceView.setTextureId(lTextureView.getTextureId(), i);
            mLL.addView(multiSurfaceView);
        }
    }


}