package com.example.dawiduk.podejscie2.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;


/**
 * Created by dawiduk on 18-12-15.
 */
public class WeatherProvider extends ContentProvider {

    private static final UriMatcher UriPatch= null;
    private WeatherContract dbHelper;

    static final int WEATHER =100;
    static final int WEATHER_WITH_LOCATION=101;
    static final int WEATHER_WITH_LOCATION_AND_DATE=102;
    static final int LOCATION=300;

    private static final SQLiteQueryBuilder WeatherByLocationSettingQueryBuilder;
    private static final String LocationSettingSelection=WeatherContract.LocationEntry.TABLE_NAME+
            "."+WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + "=?";

    private static final String LocationSettingWithStartDateSelection =
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATE + " >= ? ";

    private static final String LocationSettingAndDaySelection =
            WeatherContract.LocationEntry.TABLE_NAME +
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ";

    static{
        WeatherByLocationSettingQueryBuilder=new SQLiteQueryBuilder();
        WeatherByLocationSettingQueryBuilder.setTables(WeatherContract.WeatherEntry.TABLE_NAME+" INNER JOIN "
        +WeatherContract.WeatherEntry.TABLE_NAME +
        " ON " + WeatherContract.WeatherEntry.TABLE_NAME+
        "." +WeatherContract.WeatherEntry.COLUMN_LOC_KEY + "+"
        + WeatherContract.LocationEntry._ID);
    }

    private Cursor getWeatherByLocationSetting(Uri uri,String[] projection ,String sortOrder){

        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        long startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if(startDate==0){
            selection=LocationSettingSelection;
            selectionArgs=new String[]{locationSetting};
        } else{
            selectionArgs=new String[]{locationSetting,Long.toString(startDate)};
            selection=LocationSettingWithStartDateSelection;
        }

    }



    @Override
    public boolean onCreate() {
        return false;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }


    @Override
    public String getType(Uri uri) {
        return null;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
//    static UriMatcher buildUriMetcher(){
//        return null;
//    }
}
