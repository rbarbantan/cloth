package ro.cornholio.wallpaper.cloth.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by rares on 7/14/2014.
 */
public class FileUtil {
    private static final String TAG = FileUtil.class.getName();
    public static void saveBitmap(Bitmap b, Context context) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput("pattern", Activity.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.PNG, 90, fos);
        } catch (IOException e) {
            Log.e(TAG, "could not download pattern", e);
        } finally {
            if(fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG, "could not flush pattern to disk", e);
                }
            }
        }
    }
}
