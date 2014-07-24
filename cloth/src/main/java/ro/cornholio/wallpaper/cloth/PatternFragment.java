package ro.cornholio.wallpaper.cloth;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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
    private static final String IS_PREMIUM = "is_premium";
    TwoWayView patternList;
    PatternAdapter adapter;
    PatternObserver observer;
    private GLSurfaceView clothView;
    private ProgressBar progressBar;
    private ClothRenderer renderer;
    private Bitmap pattern;
    private SourceType listType;
    private int current;
    private String patternUrl;
    private int zoom=MIN_ZOOM;
    private String query;
    private AdView adView;

    public static PatternFragment newInstance(SourceType type, boolean isPremium){
        PatternFragment fragment = new PatternFragment();
        Bundle args = new Bundle();
        args.putString(TYPE, type.name());
        args.putBoolean(IS_PREMIUM, isPremium);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.pattern_fragment, container, false);
        patternList = (TwoWayView) root.findViewById(R.id.list);
        adView = (AdView) root.findViewById(R.id.adView);
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
        switch (listType){
            case Featured:
                patternList.setOnItemClickListener(freeListener);
                break;
            default:
                patternList.setOnItemClickListener(paidListener);
                patternList.setOnScrollListener(scrollListener);
                break;
        }
        getPatterns(true);
        updateUI(getArguments().getBoolean(IS_PREMIUM));
        // Get tracker.
        Tracker t = ((ClothApplication) getActivity().getApplication()).getTracker();

        // Set screen name.
        // Where path is a String representing the screen name.
        t.setScreenName(listType.toString());

        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());
        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof PatternObserver) {
            observer = (PatternObserver) activity;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    private void getPatterns(final boolean cleanSearch){
        progressBar.setVisibility(View.VISIBLE);
        Callback<List<Pattern>> callback = new Callback<List<Pattern>>() {
            @Override
            public void success(List<Pattern> patterns, Response response) {
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
            public void failure(RetrofitError error) {
                Log.e(TAG, "could not get patterns", error);
            }
        };

        switch (listType){
            case Featured:
                getFeatured();
                break;
            case Popular:
                ColourLoversClient.getInstance().listTopPatterns(this.query, current, callback);
                break;
            case Recent:
                ColourLoversClient.getInstance().listLatestPatterns(this.query, current,callback);
                break;
        }
    }

    public void updateUI(boolean isPremium){
        Log.d(TAG, "updating " + isPremium);
        if(adView != null) {
            if(!isPremium) {
                AdRequest adRequest = new AdRequest.Builder()
                        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                        //.addTestDevice("INSERT_YOUR_HASHED_DEVICE_ID_HERE")
                        .build();
                adView.loadAd(adRequest);
            }else {
                adView.pause();
                adView.setVisibility(View.GONE);
            }
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
            observer.itemSelected(false);

        }
    };
    AdapterView.OnItemClickListener paidListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            final String imageUrl = ((Pattern) adapterView.getAdapter().getItem(i)).imageUrl;
            Picasso.with(getActivity()).load(imageUrl).resize(512,512).into(target);
            observer.itemSelected(true);
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
        editor.putString("pattern", patternUrl);
        editor.commit();
    }

    public void search(String query, boolean cleanSearch){
        this.query = query;
        getPatterns(cleanSearch);
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
                    getPatterns(false);
                }
            }
        }

        @Override
        public void onScroll(TwoWayView twoWayView, int i, int i2, int i3) {

        }
    };
}
