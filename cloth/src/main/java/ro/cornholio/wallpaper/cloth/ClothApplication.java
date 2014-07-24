package ro.cornholio.wallpaper.cloth;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by rares on 7/24/2014.
 */
public class ClothApplication extends Application {
    private static final String PROPERTY_ID = "UA-23624714-4";
    private Tracker tracker;
    synchronized Tracker getTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(PROPERTY_ID);
        }
        return tracker;
    }
}
