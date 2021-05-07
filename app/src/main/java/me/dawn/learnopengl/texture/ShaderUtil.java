package me.dawn.learnopengl.texture;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author : LeeZhaoXing
 * @date : 2021/5/6
 * @desc :
 */
public class ShaderUtil {

    /**
     * @param shadeType
     * @param source
     * @return 编译失败返回 -1
     */
    public static int loadShader(int shadeType, String source) {
        //1.创建shade
        int shade = GLES20.glCreateShader(shadeType);
        if (shade != 0) {
            //2.加载shade源码并编译
            GLES20.glShaderSource(shade, source);
            GLES20.glCompileShader(shade);
            //3.检查是否编译成功
            int[] compile = new int[1];
            GLES20.glGetShaderiv(shade, GLES20.GL_COMPILE_STATUS, compile, 0);
            if (compile[0] != GLES20.GL_TRUE) {
                Log.d("@@", "shade compile wrong");
                GLES20.glDeleteShader(shade);
                shade = -1;
            }

            return shade;
        } else {
            return -1;
        }
    }

    public static String getRawResource(Context context, int rawId) {
        InputStream inputStream = context.getResources().openRawResource(rawId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     *
     * @param vertexSource
     * @param fragmentSource
     * @return 创建失败返回-1
     */
    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShade = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fragmentShade = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (vertexShade != -1 && fragmentShade != -1) {
            //4.创建一个渲染程序
            int program = GLES20.glCreateProgram();
            //5.将着色器程序添加到渲染程序
            GLES20.glAttachShader(program, vertexShade);
            GLES20.glAttachShader(program, fragmentShade);
            //6.链接源程序
            GLES20.glLinkProgram(program);
            return program;
        }else {
            return -1;
        }

    }
}
