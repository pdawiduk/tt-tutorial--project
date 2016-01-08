package com.example.dawiduk.podejscie2.service;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.Time;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.dawiduk.podejscie2.R;
import com.example.dawiduk.podejscie2.data.WeatherContract;

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
import java.util.List;

/**
 * Created by dawiduk on 8-1-16.
 */
public class ForecastService extends IntentService {



    private ArrayAdapter<String> forecastAdapter;
    public static final String LOCATION_QUERY_EXTRA = "lqe";
    private final String LOG_TAG = ForecastService.class.getSimpleName();
    public ForecastService() {
        super("Sunshine");
    }

    HttpURLConnection connectUrl;
    BufferedReader reader;
    String forecastJsonStr;

    @Override
    protected void onHandleIntent(Intent intent) {


        String format = "JSON";
        String units = "metric";
        int numDays = 14;

        final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
        final String QUERY_PARAM = "q";
        final String FORMAT_PARAM = "mode";
        final String UNITS_PARAM = "units";
        final String DAYS_PARAM = "cnt";
        final String APPID_PARAM = "APPID";

        String locationQuery = intent.getStringExtra(LOCATION_QUERY_EXTRA);


        Uri ApiAdress = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, locationQuery)
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                .appendQueryParameter(APPID_PARAM, getString(R.string.keyApi))
                .build();

        try {
            URL url = new URL(ApiAdress.toString());

            connectUrl = (HttpURLConnection) url.openConnection();
            connectUrl.setRequestMethod("GET");
            connectUrl.connect();

            InputStream input = connectUrl.getInputStream();
            StringBuffer builider = new StringBuffer();

            if (input == null) return ;

            reader = new BufferedReader(new InputStreamReader(input));

            String line;

            while ((line = reader.readLine()) != null) {
                builider.append(line + '\n');
            }
            if(builider.length()==0) return ;


            forecastJsonStr = builider.toString();
            getWeatherDataFromJson(forecastJsonStr,locationQuery);


        } catch (IOException e) {

            Log.e(LOG_TAG, "You  have an error", e);


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


        //     return new String[0];
        return ;
    }

    long addLocation(String locationSetting, String cityName, double lat, double lon) {

        long locationId;

        Cursor locationCursor = this.getContentResolver().query(
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


            Uri insertedUri = this.getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    locationValues
            );


            locationId = ContentUris.parseId(insertedUri);
        }

        locationCursor.close();

        return locationId;
    }

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
        String cityName = cityJson.getString(OWM_CITY_NAME);

        JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);

        double cityLatiude = cityCoord.getDouble(OWM_LATITUDE);
        double cityLogitude = cityCoord.getDouble(OWM_LONGITUDE);

        List<ContentValues> contentList = new ArrayList<ContentValues>();

        long locationId = addLocation(locationSetting, cityName, cityLatiude, cityLogitude);
        Time dayTime = new Time();
        dayTime.setToNow();

        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        dayTime = new Time();

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

            dateTime = dayTime.setJulianDay(julianStartDay+i);

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

            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            contentList.add(weatherValues);


        }

        int inserted = 0;

        if (contentList.size() > 0) {
            ContentValues[] cvArray = new ContentValues[contentList.size()];
            contentList.toArray(cvArray);
            inserted = this.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);
        }

        Log.d(LOG_TAG, "BackgroundTask Complete. " + inserted + " Inserted");

    }
}
