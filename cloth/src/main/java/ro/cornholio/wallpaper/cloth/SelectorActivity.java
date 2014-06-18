package ro.cornholio.wallpaper.cloth;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by rares on 6/11/2014.
 */
public class SelectorActivity extends Activity{

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent();
        intent.setAction(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(this, ClothWallpaperService.class));
        startActivity(intent);
        finish();
    }
}
