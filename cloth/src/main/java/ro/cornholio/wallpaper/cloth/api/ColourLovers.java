package ro.cornholio.wallpaper.cloth.api;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import ro.cornholio.wallpaper.cloth.Pattern;

/**
 * Created by rares on 6/10/2014.
 */
public interface ColourLovers {
    @GET("/api/patterns?format=json&numResults=20")
    void listPatterns(@Query("resultOffset")int offset, Callback<List<Pattern>> callback);
}
