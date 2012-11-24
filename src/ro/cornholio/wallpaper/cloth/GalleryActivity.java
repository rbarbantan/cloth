package ro.cornholio.wallpaper.cloth;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

public class GalleryActivity extends Activity {
	private static final String AD_UNIT_ID = "a14e77762f9ba57";
	//private List<String> paths;
	GridView gw;
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		
		try {
			getItems(0);
			gw = (GridView) findViewById(R.id.gridview);
			gw.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					Log.d("ClothGallery", "selected: " + arg0.getItemAtPosition(arg2));
					SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(GalleryActivity.this).edit();
					editor.putString("preference_bkg", (String) arg0.getItemAtPosition(arg2));
					editor.commit();
					finish();
					
				}
			});

		} catch (Exception e) {
			Log.e("ClothPreference", "could not contact server ", e);
		}
	}

	private void getItems(int position) {
		new PatternsTask(this).execute(position);
	}

}
