package ro.cornholio.wallpaper.cloth;

import android.app.Activity;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.squareup.picasso.Picasso;

import org.lucasr.twowayview.TwoWayView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ro.cornholio.wallpaper.cloth.api.ColourLoversClient;
import ro.cornholio.wallpaper.cloth.render.ClothRenderer;

/**
 * Created by rares on 7/1/2014.
 */
public class PatternFragment extends Fragment{
    private static final String TAG = PatternFragment.class.getName();
    private static final String TYPE = "type";
    TwoWayView patternList;
    PatternObserver observer;
    private GLSurfaceView clothView;
    private ClothRenderer renderer;
    private Bitmap pattern;

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
        clothView = (GLSurfaceView) root.findViewById(R.id.clothView);
        clothView.setEGLContextClientVersion(2);
        renderer = new ClothRenderer(getActivity());
        clothView.setRenderer(renderer);
        final int type = getArguments().getInt(TYPE);

        Callback<List<Pattern>> callback = new Callback<List<Pattern>>() {
            @Override
            public void success(List<Pattern> patterns, Response response) {
                if(isAdded()){
                    patternList.setAdapter(new PatternAdapter(getActivity(), patterns, type>0));
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "could not get patterns", error);
            }
        };
        switch (type){
            case 0:
                ColourLoversClient.getInstance().listPatterns(0,callback);
                patternList.setOnItemClickListener(freeListener);
                break;
            case 1:
                ColourLoversClient.getInstance().listLatestPatterns(0,callback);
                patternList.setOnItemClickListener(paidListener);
                break;
            case 2:
                RandomPatternsTask randomPatternsTask = new RandomPatternsTask(getActivity(), patternList);
                randomPatternsTask.execute();
                patternList.setOnItemClickListener(paidListener);
                break;
        }
        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof PatternObserver) {
            observer = (PatternObserver) activity;
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
                result = Picasso.with(getActivity()).load(strings[0]).resize(512,512).get();
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
            FileOutputStream fos = null;
            try {
                fos = getActivity().openFileOutput("pattern", Activity.MODE_PRIVATE);
                pattern.compress(Bitmap.CompressFormat.PNG, 90, fos);
            } catch (IOException e) {
                Log.e(TAG, "could not download pattern", e);
            } finally {
                if(fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        Log.e(TAG, "could not flush pattern to disk", e);
                    }
                }
            }
        }
    }
    public interface PatternObserver {
        void itemSelected(boolean paid);
    }
}
