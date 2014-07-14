package ro.cornholio.wallpaper.cloth;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import com.crashlytics.android.Crashlytics;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import android.content.pm.Signature;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import ro.cornholio.wallpaper.cloth.util.DebugUtils;

/**
 * Created by rares on 6/11/2014.
 */
public class SelectorActivity extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!DebugUtils.isDebuggable(getApplicationContext())) {
            Crashlytics.start(this);
        }
        Intent i = new Intent();

        if(Build.VERSION.SDK_INT >= 16)
        {
            i.setAction(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(this, ClothWallpaperService.class));
        }
        else
        {
            i.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
        }
        startActivity(i);
        finish();
    }

/*
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent();
        intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
        //intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
        //        new ComponentName(this, ClothWallpaperService.class));
        startActivity(intent);
        finish();
    }*/
}
