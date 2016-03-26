package ro.cornholio.wallpaper.cloth.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by rares on 6/10/2014.
 */
public class ColourLoversClient {
    private static ColourLovers instance;

    private ColourLoversClient(){}

    public static ColourLovers getInstance(){
        if(instance == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://www.colourlovers.com")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            instance = retrofit.create(ColourLovers.class);
        }
        return instance;
    }
}
