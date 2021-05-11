package me.dawn.learnopengl;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import me.dawn.learnopengl.texture.LTextureRenderer;
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

        lTextureView.getmLTextureRenderer().setOnRenderCreateListener(new LTextureRenderer.OnRenderCreateListener() {
            @Override
            public void onCreate(int textureId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLL.removeAllViews();
                        for (int i = 0; i < 3; i++) {
                            MultiSurfaceView multiSurfaceView = new MultiSurfaceView(MultiMainActivity.this);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(200, 300);
                            if (lp != null) {
                                lp.rightMargin = 20;
                            }
                            multiSurfaceView.setLayoutParams(lp);
                            multiSurfaceView.setTextureId(textureId, i);
                            multiSurfaceView.setSurfaceAndEglContext(null, lTextureView.getEglContext());
                            mLL.addView(multiSurfaceView);
                        }

                    }
                });
            }
        });



    }


}