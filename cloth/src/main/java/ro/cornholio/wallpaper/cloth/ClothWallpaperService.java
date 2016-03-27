package ro.cornholio.wallpaper.cloth;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.util.Log;

import ro.cornholio.wallpaper.cloth.render.ClothRenderer;

/**
 * Created by rares on 6/11/2014.
 */
public class ClothWallpaperService extends OpenGLES2WallpaperService{

    @Override
    GLSurfaceView.Renderer getNewRenderer() {
        return new ClothRenderer(getApplicationContext());
    }
}
