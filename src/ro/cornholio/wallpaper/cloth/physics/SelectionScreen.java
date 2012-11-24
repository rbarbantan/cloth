package ro.cornholio.wallpaper.cloth.physics;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;

public class SelectionScreen extends Activity{
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //setContentView(R.layout.main);
        
        //ImageButton cameraButton = (ImageButton)findViewById(R.id.camera);
        //ImageButton galleryButton = (ImageButton)findViewById(R.id.photos);
        //cameraButton.setOnClickListener(cameraListener);
        //galleryButton.setOnClickListener(galleryListener);
        
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		String imageURI = null;
		if(requestCode == TAKE_PHOTO) {
			imageURI = uri;
		}else if (requestCode == SELECT_PHOTO) {
			imageURI = data.getData().toString();
		}
		setResult(RESULT_OK, new Intent().setAction(imageURI));
		//String s = getRealPathFromURI(data.getData());
		finish();
	}
	
	private static final int SELECT_PHOTO = 0;
	private static final int TAKE_PHOTO = 1;
	
	private OnClickListener cameraListener = new OnClickListener() {
        public void onClick(View v) {
        	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
        	
        	File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
            uri = Uri.fromFile(photo).toString();
            
            startActivityForResult(Intent.createChooser(intent, "Take photo"),TAKE_PHOTO);
        }
    };
    
    private OnClickListener galleryListener = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra("return-data", true);
            startActivityForResult(Intent.createChooser(intent, "Select photo"),SELECT_PHOTO);
           
        }
    };
    
    public String getRealPathFromURI(Uri contentUri) {
    	// can post image
    	String [] proj={MediaStore.Images.Media.DATA};
    	Cursor cursor = managedQuery( contentUri,
    	proj, // Which columns to return
    	null, // WHERE clause; which rows to return (all rows)
    	null, // WHERE clause selection arguments (none)
    	null); // Order-by clause (ascending by name)
    	int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
    	cursor.moveToFirst();
    	return cursor.getString(column_index);
    	}
    
    private static String uri;
}
