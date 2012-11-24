package ro.cornholio.wallpaper.cloth;

import java.io.File;
import java.util.List;

import com.webimageloader.ImageLoader;
import com.webimageloader.ext.ImageHelper;
import com.webimageloader.ext.ImageLoaderApplication;

import android.app.ActivityManager;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;

public class PatternAdapter extends ArrayAdapter<String>{

	private GalleryActivity context;
	ImageLoader imageLoader;
	
	public PatternAdapter(GalleryActivity context, List<String> patterns) {
		super(context, R.layout.gridcell, patterns);
		this.context = context;
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		int memClass = am.getMemoryClass();

		// Use part of the available memory for memory cache.
		final int memoryCacheSize = 1024 * 1024 * memClass / 8;

		File cacheDir = new File(context.getExternalCacheDir(), "images");
		this.imageLoader = new ImageLoader.Builder(context)
		        .enableDiskCache(cacheDir, 10 * 1024 * 1024)
		        .enableMemoryCache(memoryCacheSize).build();
		//this.imageLoader = ImageLoaderApplication.getLoader(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imgView = null;
		if(convertView == null) {
			 imgView = new ImageView(context);
			 imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			 imgView.setLayoutParams(new GridView.LayoutParams(150, 150)); 
			 imgView.setPadding(5, 5, 5, 5);
		}else {
			imgView = (ImageView) convertView;
		}
		
		if(position >= getCount() - 10) {
			//get some more
			new PatternsTask(context).execute(position);
		}
		
		new ImageHelper(context, imageLoader).setFadeIn(true)
			.setLoadingResource(R.drawable.cloth_logo).load(imgView, getItem(position));
		return imgView;
	}
	
	

}
