package ro.cornholio.wallpaper.cloth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class HttpUtils {
	public static final String SERVER = "http://clothlw.appspot.com/";


	public static String getStringContent(final String uri) throws Exception {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(new URI(uri));
			HttpResponse response = client.execute(request);
			InputStream ips = response.getEntity().getContent();
			BufferedReader buf = new BufferedReader(new InputStreamReader(ips,
					"UTF-8"));

			StringBuilder sb = new StringBuilder();
			String s;
			while (true) {
				s = buf.readLine();
				if (s == null || s.length() == 0)
					break;
				sb.append(s);

			}
			buf.close();
			ips.close();
			return sb.toString();

		} finally {
			// any cleanup code...
		}
	}
	
	public static Bitmap loadImage(final String imageUrl, boolean isThumbnail) {
		Bitmap bitmap = null;
		
		File cache = new File(Environment.getExternalStorageDirectory() + "/Android/data/ro.cornholio.wallpaper.cloth/" + (isThumbnail?"thumbnails":"bkg"));
		if(!cache.exists()) {
			cache.mkdirs();
		}
		String[] files = cache.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String filename) {
				return imageUrl.equalsIgnoreCase(filename);
			}
		});
		int itemsFound = 0;
		if(files != null) {
			itemsFound = files.length;
		}
		
		if(itemsFound == 1) {
			Log.e("ClothGallery", "loading from sd card");
			bitmap = BitmapFactory.decodeFile(cache +"/" + imageUrl);
		}else {
			Log.e("ClothGallery", "loading from web");
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet();
				request.setURI(new URI(SERVER +"images/"+ (isThumbnail?"thumbnail":"bkg") +"/" + imageUrl));
				HttpResponse response = client.execute(request);
				InputStream in = response.getEntity().getContent();
				bitmap = BitmapFactory.decodeStream(in);
				FileOutputStream fos = new FileOutputStream(cache +"/" + imageUrl);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
				
				in.close();
			} catch (Exception e1) {
				Log.e("ClothGallery", "could not get images", e1);
			}
		}

		
		return bitmap;
	}

}
