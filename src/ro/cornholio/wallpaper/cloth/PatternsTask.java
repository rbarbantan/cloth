package ro.cornholio.wallpaper.cloth;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class PatternsTask extends AsyncTask<Integer, Void, String>{
	
	private GalleryActivity ga;
	
	public PatternsTask(GalleryActivity ga) {
		this.ga = ga;
	}
	
	@Override
	protected String doInBackground(Integer... params) {
		return HttpUtils.getJsonPatterns(params[0]);
	}

	@Override
	protected void onPostExecute(String result) {
		List<String> patterns = new ArrayList<String>();
		try{
			JSONArray items = new JSONArray(result);
			for (int i = 0; i < items.length(); i++) {
				JSONObject item = items.getJSONObject(i);
				patterns.add(item.getString("imageUrl"));
			}
			if(ga.gw.getAdapter() == null) {
				ga.gw.setAdapter(new PatternAdapter(ga, patterns));
			}else {
				((PatternAdapter)ga.gw.getAdapter()).addAll(patterns);
			}
			
		}catch(Exception e) {
			Log.e(GalleryActivity.class.getName(), "could not get more items", e);
			Toast.makeText(ga, R.string.error_pattern_list, Toast.LENGTH_SHORT).show();
		}
	}

	
}
