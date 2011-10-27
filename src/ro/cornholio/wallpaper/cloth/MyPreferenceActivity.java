package ro.cornholio.wallpaper.cloth;

import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.util.Log;

public class MyPreferenceActivity extends PreferenceActivity 
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        cleanupCache();
        //getPreferenceManager().setSharedPreferencesName(
        //        LiveWallpaperService.PREFERENCES);
        addPreferencesFromResource(R.xml.settings);
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        //Intent intent = new Intent(this, GalleryActivity.class);
        //startActivity(intent);
    }

	private void cleanupCache() {
		File oldCache = new File(Environment.getExternalStorageDirectory()
				+ "/Android/data/ro.cornholio.cloth/");
		if(deleteFile(oldCache)) {
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
		            + Environment.getExternalStorageDirectory()))); 
		}
		
		File newCache = new File(Environment.getExternalStorageDirectory()
				+ "/Android/data/ro.cornholio.wallpaper.cloth/");
		if (!newCache.exists()) {
			newCache.mkdirs();
			File noMedia = new File(newCache, ".nomedia");
			try {
				noMedia.createNewFile();
			} catch (IOException e) {
				Log.e("ClothPreference", "could not tell gallery to skip ignore folder", e);
			}
		}

	}
	
	private boolean deleteFile(File fileToBeDeleted) {
		boolean deleted = false;
		if (fileToBeDeleted.exists() && fileToBeDeleted.canWrite()){
			if(fileToBeDeleted.isDirectory()) {
				String[] children = fileToBeDeleted.list();
				for (int i = 0; i < children.length; i++) {
					deleteFile(new File(fileToBeDeleted, children[i]));
				}
			}
			fileToBeDeleted.delete();
			deleted = true;
		}
		return deleted;
	}

	@Override
    protected void onDestroy() {
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {}
    
 
}