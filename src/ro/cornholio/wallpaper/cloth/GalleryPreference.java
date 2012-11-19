package ro.cornholio.wallpaper.cloth;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class GalleryPreference extends Preference { 
    public GalleryPreference(Context context, AttributeSet attrs, 
            int defStyle) { 
        super(context, attrs, defStyle); 
        Initliazed(); 
    } 

    public GalleryPreference(Context context, AttributeSet attrs) { 
        super(context, attrs); 
        Initliazed(); 
    } 

    public GalleryPreference(Context context) { 
        super(context); 
 
        Initliazed(); 
    } 
     private void Initliazed() { 
            setPersistent(true); 
            //setDialogLayoutResource(R.layout.pref_marble_list); 
            setLayoutResource(R.layout.gallery); 
            //setDialogTitle("Dialog Title Marble Select"); 
            //setDialogMessage("Thanks for choosing my custom dialog preference"); 
              setEnabled(true); 
              setSelectable(true); 

        } 
    @Override 
    protected void onBindView(View view) { 
        // TODO Auto-generated method stub 
        super.onBindView(view); 
         GridView g = (GridView) view.findViewById(R.id.gridview); 
         //g.setFocusable(true); 
         //g.setFocusableInTouchMode(true); 
         //g.setUnselectedAlpha(0.5f); 
         g.setAdapter(new ImageAdapter(getContext())); 
         //g.setSpacing(2); 
    } 

    // *Image Adapter code* 
     private class ImageAdapter extends BaseAdapter { 
            int mGalleryItemBackground; 

            public ImageAdapter(Context c) { 
                mContext = c; 
            } 

            public int getCount() { 
                return 10;
            } 

            public Object getItem(int position) { 
                return position; 
            } 

            public long getItemId(int position) { 
                return position; 
            } 

            public View getView(int position, View convertView, ViewGroup parent) { 
                ImageView i = new ImageView(mContext); 

                i.setImageResource(R.drawable.cloth_logo); 
                i.setScaleType(ImageView.ScaleType.CENTER_CROP);
                i.setLayoutParams(new GridView.LayoutParams(75, 75)); 
                i.setPadding(5, 5, 5, 5);
               //i.setFocusableInTouchMode(true); 
                // The preferred Gallery item background 
                //i.setBackgroundResource(mGalleryItemBackground); 

                return i; 
            } 

            private Context mContext; 

            /*private Integer[] mImageIds = { 
                    R.drawable.marble_blue, 
                    R.drawable.marble_green, 
                    R.drawable.marble_lgreen, 
                    R.drawable.marble_purple, 
                    R.drawable.marble_red 
            };*/ 
        }
}