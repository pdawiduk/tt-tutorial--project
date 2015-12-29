package com.example.dawiduk.podejscie2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.dawiduk.podejscie2.data.WeatherContract;
import com.example.dawiduk.podejscie2.data.WeatherContract.WeatherEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

/**
 * Created by dawiduk on 11-12-15.
 */
class BackgroundTask extends AsyncTask<String, Void, String[]> {

    private static final String LOG_TAG = BackgroundTask.class.getSimpleName();
    private static final int HOUR_IN_MILISEC = 60 * 60 * 1000;
    private static final String TIME_FORMAT = "EEE MMM dd";
    private ForecastAdapter adapter;
    private Context context;

    public BackgroundTask(Context context){
        this.context=context;
    }


    long addLocation(String locationSetting, String cityName, double lat, double lon) {


        long locationId;


        Cursor locationCursor = context.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null);

        if (locationCursor.moveToFirst()) {
            int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = locationCursor.getLong(locationIdIndex);
        } else {

            ContentValues locationValues = new ContentValues();

            locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);


            Uri insertedUri = context.getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    locationValues
            );


            locationId = ContentUris.parseId(insertedUri);
        }

        locationCursor.close();

        return locationId;
    }


    public BackgroundTask(ForecastAdapter adapter, Context context) {
        this.adapter = adapter;
        this.context = context;

    }

    HttpURLConnection connectUrl;
    BufferedReader reader;
    String JSONline;


    private void getWeatherDataFromJson(String forecastJsonStr, String locationSetting)
            throws JSONException {


        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";
        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";

        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";
        final String OWM_PRESSURE = "pressure";

        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";
        final String OWM_WEATHER_ID = "id";


        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        String cityname = cityJson.getString(OWM_CITY_NAME);

        JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);

        double cityLatiude = cityCoord.getDouble(OWM_LATITUDE);
        double cityLogitude = cityCoord.getDouble(OWM_LONGITUDE);

        List<ContentValues> contentList = new ArrayList<ContentValues>();

        long locationId = addLocation(locationSetting, cityname, cityLatiude, cityLogitude);
        GregorianCalendar dayTime;

        dayTime = new GregorianCalendar();


        for (int i = 0; i < weatherArray.length(); i++) {

            long dateTime;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            String description;
            int weatherId;


            JSONObject dayForecast = weatherArray.getJSONObject(i);

            dayTime.add(Calendar.DATE, 1);
            dateTime=dayTime.get(Calendar.DAY_OF_MONTH);

            pressure = dayForecast.getDouble(OWM_PRESSURE);
            humidity = dayForecast.getInt(OWM_HUMIDITY);
            windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
            windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);


            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            weatherId = weatherObject.getInt(OWM_WEATHER_ID);
            description = weatherObject.getString(OWM_DESCRIPTION);

            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            high = temperatureObject.getDouble(OWM_MAX);
            low = temperatureObject.getDouble(OWM_MIN);


            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationId);
            weatherValues.put(WeatherEntry.COLUMN_DATE, dateTime);
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            contentList.add(weatherValues);


        }

        int inserted = 0;

        if (contentList.size() > 0) {
            ContentValues[] cvArray = new ContentValues[contentList.size()];
            contentList.toArray(cvArray);
            inserted = context.getContentResolver().bulkInsert(WeatherEntry.CONTENT_URI, cvArray);
        }

        Log.d(LOG_TAG, "BackgroundTask Complete. " + inserted + " Inserted");

    }

    @Override
    protected String[] doInBackground(String... params) {



        if(params.length==0){
            return new String[0];
        }

        String format = "JSON";
        String units = "metric";
        int numDays = 7;

        final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
        final String QUERY_PARAM = "q";
        final String FORMAT_PARAM = "mode";
        final String UNITS_PARAM = "units";
        final String DAYS_PARAM = "cnt";
        final String APPID_PARAM = "APPID";

        String locationQuery = params[0];


        Uri ApiAdress = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, params[0])
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                .appendQueryParameter(APPID_PARAM, context.getString(R.string.keyApi))
                .build();

        try {
            URL url = new URL(ApiAdress.toString());

            connectUrl = (HttpURLConnection) url.openConnection();//open connection
            connectUrl.setRequestMethod("GET");// set nethod
            connectUrl.connect();

            InputStream input = connectUrl.getInputStream();// set input stream
            StringBuilder builider = new StringBuilder();

            if (input == null) return new String[0];

            reader = new BufferedReader(new InputStreamReader(input));

            String line;

            while ((line = reader.readLine()) != null) {
                builider.append(line + '\n');
            }


            JSONline = builider.toString();
            getWeatherDataFromJson(JSONline,locationQuery);


        } catch (IOException e) {

            Log.e(LOG_TAG, "You  have an error", e);

            return new String[0];
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connectUrl != null) connectUrl.disconnect();

            if (reader != null) {

                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "you have an error", e);
                }
            }
        }


        return new String[0];
    }


}