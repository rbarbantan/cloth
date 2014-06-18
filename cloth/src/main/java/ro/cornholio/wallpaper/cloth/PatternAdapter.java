package ro.cornholio.wallpaper.cloth;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

/**
 * Created by rares on 6/10/2014.
 */
public class PatternAdapter extends BaseAdapter {
    private static final String TAG = PatternAdapter.class.getName();
    private List<Pattern> patterns;
    private Context context;
    private boolean locked;
    private Bitmap overaly;
    private Paint bitmapPaint;
    private Paint overlayPaint;
    int patternSizePx;

    public PatternAdapter(Context context, List<Pattern> patterns, boolean locked) {
        this.patterns = patterns;
        this.context = context;
        this.locked = locked;
        if(locked){
            overaly = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_device_access_secure);
            bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
            overlayPaint = new Paint(Color.BLACK);
            overlayPaint.setAlpha(30);
        }
        Resources res = context.getResources();
        float patternSizeDP = res.getDimension(R.dimen.imageHeight);
        patternSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, patternSizeDP, context.getResources().getDisplayMetrics());
    }

    @Override
    public int getCount() {
        return patterns.size();
    }

    @Override
    public Pattern getItem(int i) {
        return patterns.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView result = null;
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            result = (ImageView) inflater.inflate(R.layout.pattern, viewGroup, false);
        }else {
            result = (ImageView) view;
        }
        DownloadAndSetPattern task = new DownloadAndSetPattern(result);
        task.execute(getItem(i).imageUrl);
        return result;
    }

    private Bitmap getLockedBitmap(Bitmap original) {
        Bitmap result = original.copy(Bitmap.Config.ARGB_8888, true);

        if(result != null && overaly != null) {
            Canvas canvas = new Canvas(result);
            canvas.drawRect(0, 0, result.getWidth(), result.getHeight(), overlayPaint);
            canvas.drawBitmap(overaly, (result.getWidth()-overaly.getWidth())/2, (result.getHeight()-overaly.getHeight())/2, bitmapPaint);
        }
        return result;
    }

    class DownloadAndSetPattern extends AsyncTask<String, Void, Bitmap> {

        private ImageView imgView;
        public DownloadAndSetPattern(ImageView imgView) {
            this.imgView = imgView;
        }
        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap result = null;
            try {
                result = getLockedBitmap(Picasso.with(context).load(strings[0]).resize(patternSizePx,patternSizePx).get());
            } catch (IOException e) {
                Log.e(TAG, "could not download pattern", e);
            }
            return result;
        }

        @Override
        protected void onPostExecute(final Bitmap bitmap) {
            imgView.setImageBitmap(bitmap);
        }
    }
}
