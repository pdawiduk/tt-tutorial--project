package com.example.dawiduk.podejscie2;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.List;

import static com.example.dawiduk.podejscie2.R.id.list_item_forecast_textview;

public class WeatherCalendarFragment extends Fragment {

    private final String LOG_TAG = WeatherCalendarFragment.class.getSimpleName();
    public static final String ZIP_CODE_LODZ ="94043";
    private ArrayAdapter<String> adapte;

    public WeatherCalendarFragment() {

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            InternetConnection weatherTask = new InternetConnection(adapte);
            weatherTask.execute(ZIP_CODE_LODZ);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        List<String> lista=new ArrayList<String>();

        adapte= new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast, list_item_forecast_textview,lista);

        View dupa=inflater.inflate(R.layout.fragment_moj, container, false);
        ListView foofoo=(ListView) dupa.findViewById(R.id.listview_forecast);
        foofoo.setAdapter(adapte);

        TextView displayInfo = (TextView) dupa.findViewById(R.id.textView_forecast);



        setHasOptionsMenu(true);
        return dupa;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }




}


