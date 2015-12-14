package com.example.dawiduk.podejscie2;

import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;
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
class InternetConnection extends AsyncTask<String, Void, String[]> {
    private final String LOG_TAG = InternetConnection.class.getSimpleName();
    private ArrayAdapter<String> adapter;

    public InternetConnection(ArrayAdapter<String> adapter){
        this.adapter=adapter;
    }

    HttpURLConnection connectUrl = null;
    BufferedReader reader = null;
    String JSONline = null;

    private String getReadableDateString(long time){

        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {
        String[] ids = TimeZone.getAvailableIDs(1 * 60 * 60 * 1000);

        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        SimpleTimeZone pdt = new SimpleTimeZone(1 * 60 * 60 * 1000,ids[0]);


        GregorianCalendar dayTime = new GregorianCalendar(pdt);

        // now we work exclusively in UTC
        dayTime = new GregorianCalendar();

        String[] resultStrs = new String[numDays];
        for(int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dayTime.add(Calendar.DATE,1);
            dateTime = dayTime.get(Calendar.DATE);

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


        String foo= new String();
        Uri ApiAdress=Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, params[0])
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                .appendQueryParameter(APPID_PARAM, "76dcef570419156ab818440e3a4f3311")
                .build();

        String endAndress=ApiAdress.toString();

//// "76dcef570419156ab818440e3a4f3311"

        try {
            URL url = new URL(endAndress);

            connectUrl = (HttpURLConnection) url.openConnection();//Otwieramy polaczenie
            connectUrl.setRequestMethod("GET");// Ustawiamy metode pobierania
            connectUrl.connect();// laczymy sie

            InputStream input = connectUrl.getInputStream();// ustawiamy strumien wejsciowy
            StringBuffer buffer = new StringBuffer();
            if (input == null) return null;

            reader = new BufferedReader(new InputStreamReader(input));

            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line + '\n');
            }

            if (buffer.length() == 0) return null; //jesli buffer pusty

            JSONline = buffer.toString();


        } catch (IOException e) {

            Log.e("Zjebalo sie w ", "ERROR Kurwa!!!:", e);

            return null;
        } finally {
            if (connectUrl != null) connectUrl.disconnect();

            if (reader != null) {

                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("Zjebalo sie ERROR KURWA", "nie moge zamknac bufferReadera", e);
                }
            }
        }

        try{
            return getWeatherDataFromJson(JSONline,numDays);

        }catch(JSONException e){
            Log.e(LOG_TAG,e.getMessage(),e);
            e.printStackTrace();

        }


        return null;
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