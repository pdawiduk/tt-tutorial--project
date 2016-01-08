package com.example.dawiduk.podejscie2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements Callbackable {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private String actualLocation;
    private boolean twoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";


    @Override
        public void onItemSelected(Uri contentUri) {
                if (twoPane) {

                                                Bundle args = new Bundle();
                        args.putParcelable(DetailActivityFragment.DETAIL_URI, contentUri);

                                DetailActivityFragment fragment = new DetailActivityFragment();
                        fragment.setArguments(args);

                                getSupportFragmentManager().beginTransaction()
                                       .replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
                                        .commit();
                    } else {
                        Intent intent = new Intent(this, DetailActivity.class)
                                        .setData(contentUri);
                        startActivity(intent);
                    }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actualLocation = Utility.getPreferredLocation(this);

        setContentView(R.layout.activity_main);
        if (findViewById(R.id.weather_detail_container) != null) {

            twoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            twoPane = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_map) {
            openPrefferedLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPrefferedLocationInMap() {
        String location = Utility.getPreferredLocation(this);

        Uri geoLocation = Uri.parse("geo:0:0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(LOG_TAG, "Couldn't call " + location + ", no receiving apps installed!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation(this);

        if (location != null && !location.equals(actualLocation)) {
            WeatherCalendarFragment wcf = (WeatherCalendarFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);

            if (wcf!=null){
                wcf.onLocationChanged();
            }
            DetailActivityFragment daf= (DetailActivityFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if(daf!=null){
                daf.onLocationChanged(location);
            }
                    actualLocation=location;
        }
    }
}



