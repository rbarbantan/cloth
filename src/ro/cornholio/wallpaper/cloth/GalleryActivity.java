package ro.cornholio.wallpaper.cloth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class GalleryActivity extends Activity {
	private static final String AD_UNIT_ID = "a14e77762f9ba57";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		
		try {
			String jsonList = HttpUtils.getStringContent(HttpUtils.SERVER);
			JSONObject root = new JSONObject(jsonList);
			JSONArray items = root.getJSONArray("items");
			final String[] paths = new String[items.length()];
			for (int i = 0; i < items.length(); i++) {
				JSONObject item = items.getJSONObject(i);
				paths[i] = item.getString("name");
			}
			GridView g = (GridView) findViewById(R.id.gridview);
			g.setAdapter(new ImageAdapter(this, paths));
			g.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					Log.e("ClothGallery", "selected: " + paths[arg2]);
					SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(GalleryActivity.this).edit();
					editor.putString("preference_bkg", paths[arg2]);
					editor.commit();
					finish();
					
				}
			});

		} catch (Exception e) {
			Log.e("ClothPreference", "could not contact server ", e);
		}
		//AdView adView = (AdView) findViewById(R.id.adView);
		//AdRequest request = new AdRequest();
		//request.addTestDevice("12345678");
		//adView.loadAd(request);
	}

	// *Image Adapter code*
	private class ImageAdapter extends BaseAdapter {
		private String[] items;
		int mGalleryItemBackground;
		private ThreadPoolExecutor pool;
		private Bitmap defaultBitmap;
		
		public ImageAdapter(Context c, String[] items) {
			mContext = c;
			this.items = items;
			BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
			this.pool = new ThreadPoolExecutor(10, 20, 60, TimeUnit.SECONDS, queue);
			defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.green);
		}

		public int getCount() {
			return items.length;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) { 
            Log.e("ClothGallery", "loading item "+ position);    
			ImageView i;
			if(convertView == null) {
				i = new ImageView(mContext);
				i.setScaleType(ImageView.ScaleType.CENTER_CROP);
                i.setLayoutParams(new GridView.LayoutParams(150, 150)); 
                i.setPadding(5, 5, 5, 5);
			}else {
				i = (ImageView) convertView;
			}
			i.setImageBitmap(defaultBitmap);
			try{
				pool.execute(new DownloadTask(i,items[position], true));
			}catch (RejectedExecutionException ex) {
				//could not handle the load, ignore for now
				//TODO  retry downloading the image
			}
			
            return i; 
        }
		private Context mContext;

		/*
		 * private Integer[] mImageIds = { R.drawable.marble_blue,
		 * R.drawable.marble_green, R.drawable.marble_lgreen,
		 * R.drawable.marble_purple, R.drawable.marble_red };
		 */

	}

}
