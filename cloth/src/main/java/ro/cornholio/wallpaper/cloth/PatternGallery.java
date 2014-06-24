package ro.cornholio.wallpaper.cloth;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.squareup.picasso.Picasso;

import org.lucasr.twowayview.TwoWayView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ro.cornholio.wallpaper.cloth.R;
import ro.cornholio.wallpaper.cloth.api.ColourLoversClient;
import ro.cornholio.wallpaper.cloth.render.ClothRenderer;

/**
 * Created by rares on 6/6/2014.
 */
public class PatternGallery extends Activity {
    private static final String TAG = PatternGallery.class.getName();
    private GLSurfaceView clothView;
    private ClothRenderer renderer;
    private Bitmap pattern;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.gallery);
        clothView = (GLSurfaceView) findViewById(R.id.clothView);
        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (supportsEs2)
        {
            // Request an OpenGL ES 2.0 compatible context.
            clothView.setEGLContextClientVersion(2);

            // Set the renderer to our demo renderer, defined below.
            renderer = new ClothRenderer(this);
            clothView.setRenderer(renderer);
        }
        else
        {
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
            return;
        }
        final Button button = (Button) findViewById(R.id.set_button);

        final TwoWayView bestList = (TwoWayView) findViewById(R.id.bestList);
        ColourLoversClient.getInstance().listPatterns(0,new Callback<List<Pattern>>() {
            @Override
            public void success(List<Pattern> patterns, Response response) {
                bestList.setAdapter(new PatternAdapter(PatternGallery.this, patterns, false));
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "could not get patterns", error);
            }
        });
        final TwoWayView latestList = (TwoWayView) findViewById(R.id.latestList);
        ColourLoversClient.getInstance().listLatestPatterns(0, new Callback<List<Pattern>>() {
            @Override
            public void success(List<Pattern> patterns, Response response) {
                latestList.setAdapter(new PatternAdapter(PatternGallery.this, patterns, true));
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "could not get patterns", error);
            }
        });
        final TwoWayView randomList = (TwoWayView) findViewById(R.id.randomList);
        RandomPatternsTask randomPatternsTask = new RandomPatternsTask(this, randomList);
        randomPatternsTask.execute();

        AdapterView.OnItemClickListener freeListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String imageUrl = ((Pattern) adapterView.getAdapter().getItem(i)).imageUrl;
                new DownloadPattern().execute(imageUrl);
                button.setText(R.string.setAsBackground);
                button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_navigation_accept, 0, 0, 0);
            }
        };
        AdapterView.OnItemClickListener paidListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String imageUrl = ((Pattern) adapterView.getAdapter().getItem(i)).imageUrl;
                new DownloadPattern().execute(imageUrl);
                button.setText(R.string.unlock);
                button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_device_access_not_secure, 0, 0, 0);

            }
        };
        bestList.setOnItemClickListener(freeListener);
        latestList.setOnItemClickListener(paidListener);
        randomList.setOnItemClickListener(paidListener);
    }

    public void setPattern(View view) {
        if(pattern != null) {
            FileOutputStream fos = null;
            try {
                fos = openFileOutput("pattern", MODE_PRIVATE);
                pattern.compress(Bitmap.CompressFormat.PNG, 90, fos);
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
        finish();
    }
    class DownloadPattern extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap result = null;
            try {
                result = Picasso.with(PatternGallery.this).load(strings[0]).resize(512,512).get();
            } catch (IOException e) {
                Log.e(TAG, "could not download pattern", e);
            }
            return result;
        }

        @Override
        protected void onPostExecute(final Bitmap bitmap) {
            pattern = Bitmap.createBitmap(bitmap);
            clothView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    renderer.updateTexture(bitmap);
                }
            });
        }
    }
}
