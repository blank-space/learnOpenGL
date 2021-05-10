package me.dawn.learnopengl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Bundle;

import me.dawn.learnopengl.texture.LTextureView;

public class MainActivity extends AppCompatActivity {

    private LTextureView lTextureView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         lTextureView=findViewById(R.id.surface);
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        lTextureView.onConfigurationChanged(newConfig.orientation);
    }
}