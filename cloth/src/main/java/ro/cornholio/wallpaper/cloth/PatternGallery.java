package ro.cornholio.wallpaper.cloth;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
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

import java.io.FileDescriptor;
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
    private static final int IMAGE_PICKER_SELECT = 1982;

    IabHelper mHelper;
    private final static String SKU_PREMIUM = "android.test.purchased";
    private final static int REQUEST_CODE = 1892;
    private boolean isPremium = false;
    private boolean needsPremium = false;
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
                needsPremium = i > 0;
                invalidateOptionsMenu();

                if(i==2) {
                    Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, IMAGE_PICKER_SELECT);
                }
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

        String base64EncodedPublicKey = getPlayPublicKey();
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d(TAG, "Problem setting up In-app Billing: " + result);
                    return;
                }
                // Hooray, IAB is fully set up!
                mHelper.queryInventoryAsync(mQueryFinishedListener);
            }
        });
    }

    private String getPlayPublicKey() {
        return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqnnY0FyW9VNE/0jXEpm2OkYN9cUSUf4IZNHb6qHrgsAt9asWmRfI8GFSdFa+csZm1IuVXzge3PNbSmQshnUb2kU4oEHDcqKzi9cDHH1lc+GoDRoWKV/GhHnVJOX6ah3OO/eAe0TOlw7askwt+OhTbv8YqNRdOiowsD2rg2nzmQTm3lnBJjfTN6FYSZBcrSO+z+fPkE3JOeeDQNK946u3TYnc6sd4HLmSHfr7bqM7cSrWI1fqXtAftOSlVPak+VJvFU5DSHBHCBibEfW6gKkh3MxJ5NEmafqMXfUuiDgy6cMPmum0BaKGXqFZ/ksgrNoTEHWnpTl7DsWisITla0wQ+wIDAQAB";
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.set).setVisible(!needsPremium || isPremium);
        menu.findItem(R.id.buy).setVisible(needsPremium && !isPremium);
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
                mHelper.launchPurchaseFlow(this, SKU_PREMIUM, REQUEST_CODE,
                        mPurchaseFinishedListener, "notusedfornow");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null){
            mHelper.dispose();
        }
        mHelper = null;
    }

    IabHelper.QueryInventoryFinishedListener mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");
            if (result.isFailure()) {
                // handle error
                Log.d(TAG, "problem querying inventory: " + result.getMessage());
                return;
            }

            Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
            isPremium = (premiumPurchase != null) && verifyDeveloperPayload(premiumPurchase);
            Log.d(TAG, "User is " + (isPremium ? "PREMIUM" : "NOT PREMIUM"));

            // update the UI
            updateUi();
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    private void updateUi() {
        invalidateOptionsMenu();
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {


            if (mHelper == null) return;

            if (result.isFailure()) {
                Log.d(TAG, "failed to query inventory: " + result);
                /*if(result.getResponse() == 7){
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                }*/
                return;
            }
            Log.d(TAG, "Query inventory was successful.");

            isPremium = true;
            updateUi();

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == IMAGE_PICKER_SELECT && resultCode == Activity.RESULT_OK) {
                Bitmap bitmap = null;//getBitmapFromCameraData(data, this);
                try {
                    bitmap = getBitmapFromUri(data.getData());
                } catch (IOException e) {
                    Log.d(TAG, "could not get bitmap");
                }
                Log.d(TAG, "bitmap " + bitmap.getWidth());
            }
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }
    @Override
    public void itemSelected(boolean paid) {
        invalidateOptionsMenu();
    }
}
