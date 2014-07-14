package ro.cornholio.wallpaper.cloth;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SeekBar;

import com.squareup.picasso.Picasso;

import org.lucasr.twowayview.TwoWayView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ro.cornholio.wallpaper.cloth.api.ColourLoversClient;
import ro.cornholio.wallpaper.cloth.render.ClothRenderer;
import ro.cornholio.wallpaper.cloth.util.FileUtil;

/**
 * Created by rares on 7/1/2014.
 */
public class PatternFragment extends Fragment{
    private static final String TAG = PatternFragment.class.getName();
    private static final String TYPE = "type";
    private static final int MIN_ZOOM = 4;
    TwoWayView patternList;
    PatternAdapter adapter;
    PatternObserver observer;
    private GLSurfaceView clothView;
    private ClothRenderer renderer;
    private Bitmap pattern;
    private int listType;
    private int current;
    private String patternUrl;

    private int zoom=MIN_ZOOM;

    public static PatternFragment newInstance(int type){
        PatternFragment fragment = new PatternFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.pattern_fragment, container, false);
        patternList = (TwoWayView) root.findViewById(R.id.list);
        patternList.setOnScrollListener(scrollListener);
        clothView = (GLSurfaceView) root.findViewById(R.id.clothView);
        clothView.setEGLContextClientVersion(2);
        renderer = new ClothRenderer(getActivity());
        clothView.setRenderer(renderer);
        SeekBar seekBar = (SeekBar) root.findViewById(R.id.seekbar);
        zoom = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("zoom", MIN_ZOOM);
        seekBar.setProgress((zoom-MIN_ZOOM)*25);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                final int newVal = i/25;
                if(zoom != newVal) {
                    Log.d(TAG, "update to " + newVal);
                    clothView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            renderer.setZoom(MIN_ZOOM + newVal);
                        }
                    });
                    zoom = MIN_ZOOM+newVal;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        listType = getArguments().getInt(TYPE);
        current = 0;
        adapter = new PatternAdapter(getActivity(), new ArrayList<Pattern>(), listType>0);
        patternList.setAdapter(adapter);
        switch (listType){
            case 0:
                patternList.setOnItemClickListener(freeListener);
                break;
            case 1:
                patternList.setOnItemClickListener(paidListener);
                break;
            case 2:
                patternList.setOnItemClickListener(paidListener);
                break;
        }
        getPatterns();
        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof PatternObserver) {
            observer = (PatternObserver) activity;
        }
    }

    private void getPatterns(){
        Callback<List<Pattern>> callback = new Callback<List<Pattern>>() {
            @Override
            public void success(List<Pattern> patterns, Response response) {
                if(isAdded()){
                    current += patterns.size();
                    adapter.addAll(patterns);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "could not get patterns", error);
            }
        };

        switch (listType){
            case 0:
                ColourLoversClient.getInstance().listPatterns(current,callback);
                break;
            case 1:
                ColourLoversClient.getInstance().listLatestPatterns(current,callback);
                break;
            case 2:
                RandomPatternsTask randomPatternsTask = new RandomPatternsTask(getActivity(), patternList);
                randomPatternsTask.execute();
                break;
        }
    }

    AdapterView.OnItemClickListener freeListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            final String imageUrl = ((Pattern) adapterView.getAdapter().getItem(i)).imageUrl;
            new DownloadPattern().execute(imageUrl);
            observer.itemSelected(false);

        }
    };
    AdapterView.OnItemClickListener paidListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            final String imageUrl = ((Pattern) adapterView.getAdapter().getItem(i)).imageUrl;
            new DownloadPattern().execute(imageUrl);
            observer.itemSelected(true);
        }
    };

    class DownloadPattern extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap result = null;
            try {
                patternUrl = strings[0];
                result = Picasso.with(getActivity()).load(patternUrl).resize(512,512).get();
            } catch (IOException e) {
                Log.e(TAG, "could not download pattern", e);
            }
            return result;
        }

        @Override
        protected void onPostExecute(final Bitmap bitmap) {
            pattern = Bitmap.createBitmap(bitmap);
            clothView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    renderer.updateTexture(bitmap);
                }
            });
        }
    }

    public void setPattern() {
        if(pattern != null) {
            FileUtil.saveBitmap(pattern, getActivity());
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putInt("zoom", zoom);
        editor.putString("pattern", patternUrl);
        editor.commit();
    }
    public interface PatternObserver {
        void itemSelected(boolean paid);
    }

    private TwoWayView.OnScrollListener scrollListener = new TwoWayView.OnScrollListener() {
        private int threshold = 5;
        @Override
        public void onScrollStateChanged(TwoWayView twoWayView, int scrollState) {
            if(scrollState == SCROLL_STATE_IDLE) {
                if(twoWayView.getLastVisiblePosition() >= twoWayView.getCount() - threshold) {
                    getPatterns();
                }
            }
        }

        @Override
        public void onScroll(TwoWayView twoWayView, int i, int i2, int i3) {

        }
    };
}
