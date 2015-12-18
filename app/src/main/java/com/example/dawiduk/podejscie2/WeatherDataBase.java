package com.example.dawiduk.podejscie2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 *
 * Created by dawiduk on 16-12-15.
 */


public class WeatherDataBase extends SQLiteOpenHelper {

    public WeatherDataBase(Context context){
        super (context,"WeatherBase.db",null,1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table WeatherContract("+
        "id integer primary key autoincrement,"+
        "integer weather_condition_id,"+
        "");
        db.execSQL("create table WeatherDbHelper");
        db.execSQL("create table LocationDb");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
