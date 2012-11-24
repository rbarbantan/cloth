package ro.cornholio.wallpaper.cloth;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class HttpUtils {
	public static final String SERVER = "http://www.colourlovers.com/api/patterns/top?format=json&numResults=20&resultOffset=";

	private static String getStringContent(final String uri) {
		InputStream ips = null;
		BufferedReader buf = null;
		StringBuilder sb = new StringBuilder();
		
		try {
			
			HttpParams my_httpParams = new BasicHttpParams(); 
			HttpConnectionParams.setConnectionTimeout(my_httpParams, 2000); 
			HttpConnectionParams.setSoTimeout(my_httpParams, 2000); 
			HttpClient client = new DefaultHttpClient(my_httpParams);
			
			HttpGet request = new HttpGet();
			request.setURI(new URI(uri));
			HttpResponse response = client.execute(request);
			ips = response.getEntity().getContent();
			buf = new BufferedReader(new InputStreamReader(ips,	"UTF-8"));

			String s;
			while (true) {
				s = buf.readLine();
				if (s == null || s.length() == 0)
					break;
				sb.append(s);

			}
		}catch (Exception ex) {
			Log.e(HttpUtils.class.getName(), "could not get more patterns", ex);
		} finally {
			try {
				if(buf != null)buf.close();
				if(ips != null)ips.close();
			} catch (IOException e) {
				Log.e(HttpUtils.class.getName(), "could not close streams", e);
			}
		}
		return sb.toString();
	}
	
	public static Bitmap loadImage(final String imageUrl, boolean isThumbnail) {
		System.out.println("downloading " + imageUrl);
		Bitmap bitmap = null;
		if(imageUrl.contains("/")) {
			final String fileName = imageUrl.substring(imageUrl.lastIndexOf("/")+1, imageUrl.length());
			System.out.println(fileName);
			File cache = new File(Environment.getExternalStorageDirectory() + "/Android/data/ro.cornholio.wallpaper.cloth/" + (isThumbnail?"thumbnails":"bkg"));
			if(!cache.exists()) {
				cache.mkdirs();
			}
			String[] files = cache.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String localFilename) {
					return fileName.equalsIgnoreCase(localFilename);
				}
			});
			int itemsFound = 0;
			if(files != null) {
				itemsFound = files.length;
			}
			
			if(itemsFound == 1) {
				Log.d("ClothGallery", "loading from sd card");
				bitmap = decodeFile(new File(cache + "/" + fileName));
			}else {
				Log.d("ClothGallery", "loading from web");
				try {
					HttpClient client = new DefaultHttpClient();
					HttpGet request = new HttpGet();
					//request.setURI(new URI(SERVER +"images/"+ (isThumbnail?"thumbnail":"bkg") +"/" + imageUrl));
					request.setURI(new URI(imageUrl));
					HttpResponse response = client.execute(request);
					if(Integer.parseInt(response.getFirstHeader("Content-Length").getValue()) < 300000) {
						InputStream in = response.getEntity().getContent();
						
						String path = cache +"/" + fileName;
						FileOutputStream fos = new FileOutputStream(path);
						copy(in, fos);
						bitmap = decodeFile(new File(path));
					}
					
				} catch (Exception e1) {
					Log.e("ClothGallery", "could not get images", e1);
				}
			}
		}
		
		return bitmap;
	}

	public static String getJsonPatterns(int position) {
		
		String url = SERVER + position;
		return getStringContent(url);
	}

	//decodes image and scales it to reduce memory consumption
	private static Bitmap decodeFile(File f){
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(new FileInputStream(f),null,o);

	        //The new size we want to scale to
	        final int REQUIRED_SIZE=70;

	        //Find the correct scale value. It should be the power of 2.
	        int scale=1;
	        while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
	            scale*=2;

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize=scale;
	        return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	    } catch (FileNotFoundException e) {}
	    return null;
	}
	
	private static void copy(InputStream inFile, OutputStream outFile) throws IOException{
		   InputStream in = null;
		   OutputStream out = null; 
		   try {
		      in = new BufferedInputStream(inFile);
		      out = new BufferedOutputStream(outFile);
		      while (true) {
		         int data = in.read();
		         if (data == -1) {
		            break;
		         }
		         out.write(data);
		      }
		   } finally {
		      if (in != null) {
		         in.close();
		      }
		      if (out != null) {
		         out.close();
		      }
		   }
		}
}
