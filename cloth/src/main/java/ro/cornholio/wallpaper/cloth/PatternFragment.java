package ro.cornholio.wallpaper.cloth;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.lucasr.twowayview.TwoWayView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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
    private ProgressBar progressBar;
    private ClothRenderer renderer;
    private Bitmap pattern;
    private SourceType listType;
    private int current;
    private int zoom=MIN_ZOOM;
    private String query;

    public static PatternFragment newInstance(SourceType type){
        PatternFragment fragment = new PatternFragment();
        Bundle args = new Bundle();
        args.putString(TYPE, type.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.pattern_fragment, container, false);
        patternList = (TwoWayView) root.findViewById(R.id.list);
        progressBar = (ProgressBar) root.findViewById(R.id.progressBar);
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

        listType = SourceType.valueOf(getArguments().getString(TYPE));
        current = 0;
        adapter = new PatternAdapter(getActivity(),R.layout.pattern, new ArrayList<Pattern>(), listType!= SourceType.Featured);
        patternList.setAdapter(adapter);
        patternList.setOnItemClickListener(freeListener);
        patternList.setOnScrollListener(scrollListener);

        getPatterns(true);

        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof PatternObserver) {
            observer = (PatternObserver) activity;
        }
    }

    private void getPatterns(final boolean cleanSearch){
        progressBar.setVisibility(View.VISIBLE);
        Callback<List<Pattern>> callback = new Callback<List<Pattern>>() {
            @Override
            public void onResponse(Call<List<Pattern>> call, Response<List<Pattern>> response) {
                List<Pattern> patterns = response.body();
                if(isAdded()){
                    if(cleanSearch) {
                        current = patterns.size();
                        adapter.setPatterns(patterns);
                    }else {
                        current += patterns.size();
                        adapter.addAll(patterns);
                    }
                }
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<List<Pattern>> call, Throwable t) {
                Log.e(TAG, "could not get patterns", t);
            }
        };

        switch (listType){
            case Featured:
                getFeatured();
                break;
            case Popular:
                ColourLoversClient.getInstance().listTopPatterns(this.query, current).enqueue(callback);
                break;
            case Recent:
                ColourLoversClient.getInstance().listLatestPatterns(this.query, current).enqueue(callback);
                break;
        }
    }

    private void getFeatured() {
        String[] featured = getActivity().getResources().getStringArray(R.array.featured);
        List<Pattern> newPatterns = new ArrayList<Pattern>();
        for(String f : featured) {
            Pattern p = new Pattern();
            p.imageUrl = f;
            newPatterns.add(p);
        }
        adapter.addAll(newPatterns);
        progressBar.setVisibility(View.INVISIBLE);
    }

    AdapterView.OnItemClickListener freeListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            final String imageUrl = ((Pattern) adapterView.getAdapter().getItem(i)).imageUrl;
            Picasso.with(getActivity()).load(imageUrl).resize(512,512).into(target);
            observer.itemSelected();

        }
    };
    AdapterView.OnItemClickListener paidListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            final String imageUrl = ((Pattern) adapterView.getAdapter().getItem(i)).imageUrl;
            Picasso.with(getActivity()).load(imageUrl).resize(512,512).into(target);
            observer.itemSelected();
        }
    };

    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            pattern = bitmap.copy(bitmap.getConfig(), true);
            clothView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    renderer.updateTexture(pattern);
                }
            });
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };


    public void setPattern() {
        if(pattern != null) {
            FileUtil.saveBitmap(pattern, getActivity());
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putInt("zoom", zoom);
        editor.commit();
    }

    public void search(String query, boolean cleanSearch){
        this.query = query;
        getPatterns(cleanSearch);
    }

    public interface PatternObserver {
        void itemSelected();
    }

    private TwoWayView.OnScrollListener scrollListener = new TwoWayView.OnScrollListener() {
        private int threshold = 5;
        @Override
        public void onScrollStateChanged(TwoWayView twoWayView, int scrollState) {
            if(scrollState == SCROLL_STATE_IDLE) {
                if(twoWayView.getLastVisiblePosition() >= twoWayView.getCount() - threshold) {
                    getPatterns(false);
                }
            }
        }

        @Override
        public void onScroll(TwoWayView twoWayView, int i, int i2, int i3) {

        }
    };
}
