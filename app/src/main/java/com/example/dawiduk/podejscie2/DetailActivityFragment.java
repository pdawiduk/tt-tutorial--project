package com.example.dawiduk.podejscie2;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.dawiduk.podejscie2.data.WeatherContract;
import com.example.dawiduk.podejscie2.data.WeatherContract.WeatherEntry;

public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private String forecast;
    private Uri helpUri;
    private ShareActionProvider shareActionProvider;

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";
    private static final String FORECAST_SHARE_HASHTAG = " #WeatherAPP";

    private static final int DETAIL_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_DEGREES,
            WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;

    private ImageView iconView;
    private TextView friendlyDateView;
    private TextView dateView;
    private TextView descriptionView;
    private TextView highTempView;
    private TextView lowTempView;
    private TextView humidityView;
    private TextView windView;
    private TextView pressureView;


    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();

        if (arguments != null) {
            helpUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        iconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        dateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        friendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        descriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        highTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        lowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        humidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        windView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        pressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);

        return rootView;

    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                forecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != helpUri) {

            return new CursorLoader(
                    getActivity(),
                    helpUri,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()) {

            int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);

            iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));


            long date = data.getLong(COL_WEATHER_DATE);
            String friendlyDateText = Utility.getDayName(getActivity(), date);
            String dateText = Utility.getFormattedMonthDay(getActivity(), date);
            friendlyDateView.setText(friendlyDateText);
            dateView.setText(dateText);

            String description = data.getString(COL_WEATHER_DESC);
            descriptionView.setText(description);

            boolean isMetric = Utility.isMetric(getActivity());

            double high = data.getDouble(COL_WEATHER_MAX_TEMP);
            String highString = Utility.formatTemperature(getActivity(), high, isMetric);
            highTempView.setText(highString);


            double low = data.getDouble(COL_WEATHER_MIN_TEMP);
            String lowString = Utility.formatTemperature(getActivity(), low, isMetric);
            lowTempView.setText(lowString);


            float humidity = data.getFloat(COL_WEATHER_HUMIDITY);

            float windSpeedStr = data.getFloat(COL_WEATHER_WIND_SPEED);
            float windDirStr = data.getFloat(COL_WEATHER_DEGREES);
            windView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));


            float pressure = data.getFloat(COL_WEATHER_PRESSURE);
            pressureView.setText(getActivity().getString(R.string.format_pressure, pressure));


            forecast = String.format("%s - %s - %s/%s", dateText, description, high, low);


            if (shareActionProvider != null) {
                shareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }

    }

    void onLocationChanged(String newLocation) {
        Uri uri = helpUri;

        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            helpUri = updateUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onResume() {
        super.onResume();


        YoYo.with(Techniques.Shake).duration(1000).playOn(iconView);

    }
}
