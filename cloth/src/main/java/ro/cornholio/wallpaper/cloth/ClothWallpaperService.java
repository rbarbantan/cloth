package ro.cornholio.wallpaper.cloth;

import android.opengl.GLSurfaceView;

import ro.cornholio.wallpaper.cloth.render.ClothRenderer;

/**
 * Created by rares on 6/11/2014.
 */
public class ClothWallpaperService extends OpenGLES2WallpaperService {
    @Override
    GLSurfaceView.Renderer getNewRenderer() {
        return new ClothRenderer(getApplicationContext());
    }
}
