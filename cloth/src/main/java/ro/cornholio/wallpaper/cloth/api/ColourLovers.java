package ro.cornholio.wallpaper.cloth.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import ro.cornholio.wallpaper.cloth.Pattern;

/**
 * Created by rares on 6/10/2014.
 */
public interface ColourLovers {
    @GET("/api/patterns/top?format=json&numResults=20")
    Call<List<Pattern>> listTopPatterns(@Query("keywords")String search, @Query("resultOffset")int offset);

    @GET("/api/patterns/new?format=json&numResults=20")
    Call<List<Pattern>> listLatestPatterns(@Query("keywords")String search, @Query("resultOffset")int offset);
}
