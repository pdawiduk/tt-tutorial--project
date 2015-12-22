package com.example.dawiduk.podejscie2.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;



/**
 * Created by dawiduk on 18-12-15.
 */
public class WeatherProvider extends ContentProvider {

    private static final UriMatcher UriPatch= null;

    private WeatherDbHelper dbHelper;

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
        return WeatherByLocationSettingQueryBuilder.query(dbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

    }

    private Cursor getWeatherByLocationSettingAndDate(Uri uri, String[] projection,String sortOrder){
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        long date= WeatherContract.WeatherEntry.getDateFromUri(uri);

        return WeatherByLocationSettingQueryBuilder.query(dbHelper.getReadableDatabase(),
                projection,
                LocationSettingAndDaySelection,
                new String[]{locationSetting, Long.toString(date)},
                null,
                null,
                sortOrder);

    }

    private void normalizeDate(ContentValues values) {

        if (values.containsKey(WeatherContract.WeatherEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
            values.put(WeatherContract.WeatherEntry.COLUMN_DATE, WeatherContract.normalizeDate(dateValue));
        }
    }

    @Override
    public boolean onCreate() {
        dbHelper= new WeatherDbHelper(getContext());
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        return null;
    }


    @Override
    public String getType(Uri uri) {
        final int match =UriPatch.match(uri);

        switch(match){
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case LOCATION:
                return WeatherContract.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknow uri: "+ uri);
        }
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = UriPatch.match(uri);
        Uri returnUri;

        switch (match) {
            case WEATHER: {
                normalizeDate(values);
                long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = WeatherContract.WeatherEntry.buildWeatherUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
         getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;

    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = UriPatch.match(uri);
        switch (match) {
            case WEATHER:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        dbHelper.close();
        super.shutdown();
    }
}

