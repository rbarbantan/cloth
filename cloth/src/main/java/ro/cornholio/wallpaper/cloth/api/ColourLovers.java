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
    @GET("/api/patterns/top?format=json&numResults=20")
    void listTopPatterns(@Query("keywords")String search, @Query("resultOffset")int offset, Callback<List<Pattern>> callback);

    @GET("/api/patterns/new?format=json&numResults=20")
    void listLatestPatterns(@Query("keywords")String search, @Query("resultOffset")int offset, Callback<List<Pattern>> callback);
}
