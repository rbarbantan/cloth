package ro.cornholio.wallpaper.cloth;

import net.rbgrn.android.glwallpaperservice.GLWallpaperService;
import android.content.Context;
import android.view.MotionEvent;

// Original code provided by Robert Green
// http://www.rbgrn.net/content/354-glsurfaceview-adapted-3d-live-wallpapers
public class MyWallpaperService extends GLWallpaperService {
	public MyWallpaperService() {
		super();
	}
	public Engine onCreateEngine() {
		MyEngine engine = new MyEngine(this);
		return engine;
	}

	class MyEngine extends GLEngine {
		MyRenderer renderer;
		public MyEngine(Context context) {
			super();
			// handle prefs, other initialization
			renderer = new MyRenderer(context);
			setRenderer(renderer);
			setRenderMode(RENDERMODE_CONTINUOUSLY);
			setTouchEventsEnabled(true);
		}

		public void onDestroy() {
			super.onDestroy();
			if (renderer != null) {
				renderer.release(); // assuming yours has this method - it should!
			}
			renderer = null;
		}
		
		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
				float xOffsetStep, float yOffsetStep, int xPixelOffset,
				int yPixelOffset) {
			//Log.d("MyEngine", "offset: " + xOffset);
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep,
					xPixelOffset, yPixelOffset);
		}

		@Override
		public void onTouchEvent(MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
				if(renderer != null && renderer.system != null) {
					renderer.system.touch(event.getX(), event.getY());
				}
			}
			super.onTouchEvent(event);

		}

	}
}
