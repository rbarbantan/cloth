package ro.cornholio.wallpaper.cloth;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class GalleryActivity extends Activity {
	private static final String AD_UNIT_ID = "a14e77762f9ba57";
	List<String> paths;
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		
		try {
			paths = (List<String>) getLastNonConfigurationInstance();
			if(paths == null) {
				paths = getItems(0, this);
			}
			GridView g = (GridView) findViewById(R.id.gridview);
			g.setAdapter(new ImageAdapter(this, paths));
			g.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					Log.d("ClothGallery", "selected: " + paths.get(arg2));
					SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(GalleryActivity.this).edit();
					editor.putString("preference_bkg", paths.get(arg2));
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

	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return paths;
	}


	// *Image Adapter code*
	private class ImageAdapter extends BaseAdapter {
		private List<String> items;
		private ThreadPoolExecutor pool;
		private Bitmap defaultBitmap;
		
		public ImageAdapter(Context c, List<String> items) {
			mContext = c;
			this.items = items;
			BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
			this.pool = new ThreadPoolExecutor(10, 20, 60, TimeUnit.SECONDS, queue);
			defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cloth_logo);
		}

		public int getCount() {
			return items.size();
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
				pool.execute(new DownloadTask(i,items.get(position), true));
				
				if(position >= items.size() -10) {
					//get some more
					items.addAll(getItems(position, mContext));
				}
			}catch (RejectedExecutionException ex) {
				//could not handle the load, ignore for now
				//TODO  retry downloading the image
			}
			
            return i; 
        }
		private Context mContext;
	}
	
	List<String> getItems(int position, Context context) {
		List<String> paths = new ArrayList<String>();
		try{
			String jsonList = HttpUtils.getJsonPatterns(position);
			JSONArray items = new JSONArray(jsonList);
			for (int i = 0; i < items.length(); i++) {
				JSONObject item = items.getJSONObject(i);
				paths.add(item.getString("imageUrl"));
			}
		}catch(Exception e) {
			Log.e(GalleryActivity.class.getName(), "could not get more items", e);
			Toast.makeText(this, R.string.error_pattern_list, 100).show();
		}
		
		return paths;
	}

}
