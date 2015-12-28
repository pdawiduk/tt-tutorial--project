package com.example.dawiduk.podejscie2;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.dawiduk.podejscie2.data.ForecastAdapter;
import com.example.dawiduk.podejscie2.data.WeatherContract;

public class WeatherCalendarFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    public static final String MESSAGE_ALLIAS = "info";

    private final String LOG_TAG = WeatherCalendarFragment.class.getSimpleName();
    private ForecastAdapter adapter;

    private static final String[] FORECAST_COLUMNS = {WeatherContract.WeatherEntry.TABLE_NAME
            + "." + WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG};

    private static final int FORECAST_LOADER = 0;

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        adapter = new ForecastAdapter(getActivity(), null, 0);

        View rootview = inflater.inflate(R.layout.fragment_weather_calendar, container, false);
        ListView listView2 = (ListView) rootview.findViewById(R.id.listview_forecast);
        listView2.setAdapter(adapter);

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                if (cursor != null) {
                    String locationSettings = Utility.getPreferredLocation(getActivity());
                    Intent intent = new Intent(getActivity(), DetailActivity.class).setData(WeatherContract.
                            WeatherEntry.buildWeatherLocationWithDate(
                            locationSettings, cursor.getLong(COL_WEATHER_DATE)));
                    startActivity(intent);
                }
            }
        });


        setHasOptionsMenu(true);
        return rootview;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String locationSettings= Utility.getPreferredLocation(getActivity());
        String sortOrder= WeatherContract.WeatherEntry.COLUMN_DATE +"ASC";
        Uri weatherForLocationUri=WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSettings,System.currentTimeMillis()
        );

        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    private void updateWeather() {
        BackgroundTask task = new BackgroundTask(getActivity());
        task.execute(Utility.getPreferredLocation(getActivity()));
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }


}


