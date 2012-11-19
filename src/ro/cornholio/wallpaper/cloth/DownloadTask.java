package ro.cornholio.wallpaper.cloth;

import android.graphics.Bitmap;
import android.widget.ImageView;


public class DownloadTask implements Runnable {

	private String imageUrl;
	private boolean isThumbnail;
	private ImageView imgView;
	
	public DownloadTask(ImageView imgView, String imageUrl, boolean isThumbnail) {
		this.imgView = imgView;
		this.imageUrl = imageUrl;
		this.isThumbnail = isThumbnail;
	}
	
	@Override
	public void run() {
		final Bitmap img = HttpUtils.loadImage(imageUrl, false);
		imgView.post(new Runnable() {
			
			@Override
			public void run() {
				imgView.setImageBitmap(img);
				
			}
		});
	}
}