package ro.cornholio.wallpaper.cloth;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by rares on 6/10/2014.
 */
public class PatternAdapter extends ArrayAdapter<Pattern> {
    private static final String TAG = PatternAdapter.class.getName();
    private List<Pattern> patterns;
    private Context context;
    int patternSizePx;

    public PatternAdapter(Context context, int resource, List<Pattern> patterns, boolean locked) {
        super(context, resource, patterns);
        this.patterns = patterns;
        this.context = context;

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
        ImageView result;
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            result = (ImageView) inflater.inflate(R.layout.pattern, viewGroup, false);
        }else {
            result = (ImageView) view;
        }
        Picasso.with(context).load(getItem(i).imageUrl).placeholder(R.drawable.ic_photo_white_24dp).resize(patternSizePx,patternSizePx).into(result);
        return result;
    }

    public void addAll(List<Pattern> newItems) {
        if(patterns != null) {
            patterns.addAll(newItems);
            notifyDataSetChanged();
        }
    }

    public void setPatterns(List<Pattern> patterns) {
        this.patterns = patterns;
        notifyDataSetChanged();
    }
}
