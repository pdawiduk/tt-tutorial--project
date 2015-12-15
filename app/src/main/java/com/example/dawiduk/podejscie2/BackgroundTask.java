package com.example.dawiduk.podejscie2;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * Created by dawiduk on 11-12-15.
 */
class BackgroundTask extends AsyncTask<String, Void, String[]> {

    private static final String LOG_TAG = BackgroundTask.class.getSimpleName();
    private static final int HOUR_IN_MILISEC=60*60*1000;
    private static final String TIME_FORMAT="EEE MMM dd";
    private ArrayAdapter<String> adapter;
    private Context context;


    public BackgroundTask(ArrayAdapter<String> adapter, Context context){
        this.adapter = adapter;
        this.context = context;

    }

    HttpURLConnection connectUrl;
    BufferedReader reader;
    String JSONline;

    private String getReadableDateString(long time){

        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat(TIME_FORMAT);
        return shortenedDateFormat.format(time);
    }

    private StringBuffer formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        return new StringBuffer(  roundedHigh + "/" + roundedLow);

    }

    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {
        String[] ids = TimeZone.getAvailableIDs(HOUR_IN_MILISEC);

        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);




        GregorianCalendar dayTime;

        // now we work exclusively in UTC
        dayTime = new GregorianCalendar();

        String[] resultStrs = new String[numDays];
        for(int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            StringBuffer highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".

            // Cheating to convert this to UTC time, which is what we want anyhow
            dayTime.add(Calendar.DATE,1);


            //day = getReadableDateString(dateTime);
            day = dayTime.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())+" "+dayTime.get(Calendar.DAY_OF_MONTH);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        for (String s : resultStrs) {
            Log.v(LOG_TAG, "Forecast entry: " + s);
        }
        return resultStrs;

    }

    @Override
    protected String[] doInBackground(String... params) {
        String format="JSON";
        String units="metric";
        int numDays=7;

        final String FORECAST_BASE_URL="http://api.openweathermap.org/data/2.5/forecast/daily?";
        final String QUERY_PARAM="q";
        final String FORMAT_PARAM="mode";
        final String UNITS_PARAM="units";
        final String DAYS_PARAM="cnt";
        final String APPID_PARAM="APPID";



        Uri ApiAdress=Uri.parse(FORECAST_BASE_URL).buildUpon()
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
            if (input == null) return new String[0] ;

            reader = new BufferedReader(new InputStreamReader(input));

            String line;

            while ((line = reader.readLine()) != null) {
                builider.append(line + '\n');
            }



            JSONline = builider.toString();


        } catch (IOException e) {

            Log.e(LOG_TAG, "You  have an error", e);

            return new String[0];
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

        try{
            return getWeatherDataFromJson(JSONline,numDays);

        }catch(JSONException e){
            Log.e(LOG_TAG,e.getMessage(),e);
            e.printStackTrace();

        }


        return new String[0];
    }

    @Override
    protected void onPostExecute(String[] result) {
        if (result != null) {
            adapter.clear();
            for(String dayForecastStr : result) {
                adapter.add(dayForecastStr);
            }
            // New data is back from the server.  Hooray!
        }
    }
}