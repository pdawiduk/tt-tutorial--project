package com.example.dawiduk.podejscie2.data;

/**
 * Created by dawiduk on 18-12-15.
 */
import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Defines table and column names for the weather database.
 */
public class WeatherContract {

    private static final int HOUR_IN_MILISEC=60*60*1000;
    public static final String CONTENT_AUTHORITY = "com.example.android.sunshine.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_WEATHER = "weather";
    public static final String PATH_LOCATION = "location";

    public static long normalizeDate(long startDate) {

        GregorianCalendar time = new GregorianCalendar();

        return time.get(Calendar.DAY_OF_MONTH);

    }


    public static final class LocationEntry implements BaseColumns {

        public static final String TABLE_NAME = "location";


        public static final String COLUMN_LOCATION_SETTING = "location_setting";


        public static final String COLUMN_CITY_NAME = "city_name";


        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";
    }


    public static final class WeatherEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;

        public static final String TABLE_NAME = "weather";


        public static final String COLUMN_LOC_KEY = "location_id";

        public static final String COLUMN_DATE = "date";

        public static final String COLUMN_WEATHER_ID = "weather_id";


        public static final String COLUMN_SHORT_DESC = "short_desc";


        public static final String COLUMN_MIN_TEMP = "min";
        public static final String COLUMN_MAX_TEMP = "max";


        public static final String COLUMN_HUMIDITY = "humidity";


        public static final String COLUMN_PRESSURE = "pressure";


        public static final String COLUMN_WIND_SPEED = "wind";


        public static final String COLUMN_DEGREES = "degrees";
        public static Uri buildWeatherLocation(String locationSetting) {
            return null;
        }

        public static Uri buildWeatherLocationWithStartDate(
                String locationSetting, long startDate) {
            long normalizedDate = normalizeDate(startDate);
            return CONTENT_URI.buildUpon().appendPath(locationSetting)
                    .appendQueryParameter(COLUMN_DATE, Long.toString(normalizedDate)).build();
        }

        public static Uri buildWeatherLocationWithDate(String locationSetting, long date) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting)
                    .appendPath(Long.toString(normalizeDate(date))).build();
        }

        public static String getLocationSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static long getDateFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(2));
        }

        public static long getStartDateFromUri(Uri uri) {
            String dateString = uri.getQueryParameter(COLUMN_DATE);
            if (null != dateString && dateString.length() > 0)
                return Long.parseLong(dateString);
            else
                return 0;
        }
    }
}
