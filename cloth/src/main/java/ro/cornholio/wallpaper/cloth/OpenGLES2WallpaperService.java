package ro.cornholio.wallpaper.cloth;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import ro.cornholio.wallpaper.cloth.render.ClothRenderer;

/**
 * Created by rares on 6/11/2014.
 */
public abstract class OpenGLES2WallpaperService extends GLWallpaperService {
    private static final String TAG = OpenGLES2WallpaperService.class.getName();

    @Override
    public Engine onCreateEngine() {
        return new OpenGLES2Engine();
    }

    class OpenGLES2Engine extends GLEngine {
        private ClothRenderer renderer;

        public OpenGLES2Engine() {
            super();
            setTouchEventsEnabled(true);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            // Check if the system supports OpenGL ES 2.0.
            final ActivityManager activityManager =
                    (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            final ConfigurationInfo configurationInfo =
                    activityManager.getDeviceConfigurationInfo();
            final boolean supportsEs2 =
                    configurationInfo.reqGlEsVersion >= 0x20000;

            if (supportsEs2)
            {
                // Request an OpenGL ES 2.0 compatible context.
                setEGLContextClientVersion(2);

                // On Honeycomb+ devices, this improves the performance when
                // leaving and resuming the live wallpaper.
                setPreserveEGLContextOnPause(true);
                renderer = (ClothRenderer) getNewRenderer();
                // Set the renderer to our user-defined renderer.
                setRenderer(renderer);
            }
            else
            {
                // This is where you could create an OpenGL ES 1.x compatible
                // renderer if you wanted to support both ES 1 and ES 2.
                return;
            }
        }

        @Override
        protected void setRenderer(GLSurfaceView.Renderer renderer) {
            super.setRenderer(renderer);
            if(renderer instanceof ClothRenderer){
                this.renderer = (ClothRenderer) renderer;
            }
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            if(event.getActionMasked() == MotionEvent.ACTION_MOVE ||
                    event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                renderer.touch(event.getX(), event.getY());
            }
        }
    }

    abstract GLSurfaceView.Renderer getNewRenderer();
}