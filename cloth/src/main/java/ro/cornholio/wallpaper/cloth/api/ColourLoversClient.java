package ro.cornholio.wallpaper.cloth.api;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Created by rares on 6/10/2014.
 */
public class ColourLoversClient {
    private static ColourLovers instance;

    private ColourLoversClient(){}

    public static ColourLovers getInstance(){
        if(instance == null) {
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint("http://www.colourlovers.com")
                    .build();
            instance = restAdapter.create(ColourLovers.class);
        }
        return instance;
    }
}
