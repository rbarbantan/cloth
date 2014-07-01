package ro.cornholio.wallpaper.cloth;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.lucasr.twowayview.TwoWayView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ro.cornholio.wallpaper.cloth.R;
import ro.cornholio.wallpaper.cloth.api.ColourLoversClient;
import ro.cornholio.wallpaper.cloth.render.ClothRenderer;
import ro.cornholio.wallpaper.cloth.util.IabHelper;
import ro.cornholio.wallpaper.cloth.util.IabResult;
import ro.cornholio.wallpaper.cloth.util.Inventory;
import ro.cornholio.wallpaper.cloth.util.Purchase;

/**
 * Created by rares on 6/6/2014.
 */
public class PatternGallery extends ActionBarActivity implements PatternFragment.PatternObserver{
    private static final String TAG = PatternGallery.class.getName();

    IabHelper mHelper;
    private final static String PURCHASE_ID = "android.test.purchased";
    boolean purchase = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.gallery);

        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.pattern_sources, R.layout.spinner_item);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActionBar().setListNavigationCallbacks(mSpinnerAdapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int i, long l) {
                purchase = i > 0;
                invalidateOptionsMenu();

                PatternFragment fragment = PatternFragment.newInstance(i);
                getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
                return false;
            }
        });

        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (!supportsEs2) {
            Toast.makeText(this, R.string.opengl, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqnnY0FyW9VNE/0jXEpm2OkYN9cUSUf4IZNHb6qHrgsAt9asWmRfI8GFSdFa+csZm1IuVXzge3PNbSmQshnUb2kU4oEHDcqKzi9cDHH1lc+GoDRoWKV/GhHnVJOX6ah3OO/eAe0TOlw7askwt+OhTbv8YqNRdOiowsD2rg2nzmQTm3lnBJjfTN6FYSZBcrSO+z+fPkE3JOeeDQNK946u3TYnc6sd4HLmSHfr7bqM7cSrWI1fqXtAftOSlVPak+VJvFU5DSHBHCBibEfW6gKkh3MxJ5NEmafqMXfUuiDgy6cMPmum0BaKGXqFZ/ksgrNoTEHWnpTl7DsWisITla0wQ+wIDAQAB";
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d(TAG, "Problem setting up In-app Billing: " + result);
                }
                // Hooray, IAB is fully set up!
                List additionalSkuList = new ArrayList();
                additionalSkuList.add(PURCHASE_ID);
                mHelper.queryInventoryAsync(true, additionalSkuList, mQueryFinishedListener);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.set).setVisible(!purchase);
        menu.findItem(R.id.buy).setVisible(purchase);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.set:
                ((PatternFragment)getSupportFragmentManager().findFragmentById(R.id.container)).setPattern();
                finish();
                return true;
            case R.id.buy:
                mHelper.launchPurchaseFlow(this, PURCHASE_ID, 10001,
                        mPurchaseFinishedListener, "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    IabHelper.QueryInventoryFinishedListener mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                // handle error
                Log.d(TAG, "problem purchasing item: " + result.getMessage());
                return;
            }

            String upgradePrice = inventory.getSkuDetails(PURCHASE_ID).getPrice();
            Log.d(TAG, "upgrade price: " + upgradePrice);

            // update the UI

        }
    };

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {
            if (result.isFailure()) {
                Log.d(TAG, "Error purchasing: " + result);
                if(result.getResponse() == 7){
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                }
                return;
            }
            else if (purchase.getSku().equals(PURCHASE_ID)) {
                Log.d(TAG, "purchased!!");

            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase, IabResult result) {
                    if (result.isSuccess()) {
                        Log.d(TAG, "consumed");
                    }
                    else {
                        Log.d(TAG, "could not consume");

                    }
                }
            };


    @Override
    public void itemSelected(boolean paid) {
        purchase = paid;
        invalidateOptionsMenu();
    }
}
