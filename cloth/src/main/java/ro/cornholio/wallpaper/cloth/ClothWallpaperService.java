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
public class ClothWallpaperService extends OpenGLES2WallpaperService implements SensorEventListener{
    private static final String TAG = ClothWallpaperService.class.getSimpleName();
    SensorManager sensorManager;
    Sensor gravity;
    ClothRenderer renderer;

    @Override
    GLSurfaceView.Renderer getNewRenderer() {
        renderer = new ClothRenderer(getApplicationContext());
        return renderer;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.d(TAG,"event: " + event.values[0]+","+event.values[1]+","+event.values[2]);
        renderer.setGravity(event.values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
