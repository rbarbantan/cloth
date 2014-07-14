package ro.cornholio.wallpaper.cloth;

import android.content.Context;
import android.os.AsyncTask;

import org.lucasr.twowayview.TwoWayView;

import java.util.ArrayList;
import java.util.List;

import ro.cornholio.wallpaper.cloth.api.ColourLovers;
import ro.cornholio.wallpaper.cloth.api.ColourLoversClient;

/**
 * Created by rares on 6/18/2014.
 */
public class RandomPatternsTask extends AsyncTask<Void, Void, List<Pattern>> {
    private final static int ITEMS = 10;
    private Context context;
    private TwoWayView view;

    public RandomPatternsTask(Context context, TwoWayView view) {
        this.context = context;
        this.view = view;
    }

    @Override
    protected List<Pattern> doInBackground(Void... voids) {
        List<Pattern> patterns = new ArrayList<Pattern>(ITEMS);
        for(int i=0; patterns.size()<=ITEMS; i++) {
            patterns.addAll(ColourLoversClient.getInstance().getRandomPattern());
        }
        return patterns;
    }

    @Override
    protected void onPostExecute(List<Pattern> patterns) {
        ((PatternAdapter)view.getAdapter()).addAll(patterns);
    }
}
