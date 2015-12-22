package com.example.dawiduk.podejscie2;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.dawiduk.podejscie2.data.ForecastAdapter;
import com.example.dawiduk.podejscie2.data.WeatherContract;

import java.util.ArrayList;
import java.util.List;

import static com.example.dawiduk.podejscie2.R.id.list_item_forecast_textview;

public class WeatherCalendarFragment extends Fragment {


    public static final String MESSAGE_ALLIAS = "info";
    public static final String ZIP_CODE_LODZ = "94043";
    private final String LOG_TAG = WeatherCalendarFragment.class.getSimpleName();
    private ForecastAdapter adapter;

    public WeatherCalendarFragment() {

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            BackgroundTask weatherTask = new BackgroundTask(adapter, getContext());
            weatherTask.execute(ZIP_CODE_LODZ);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + "ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                locationSetting, System.currentTimeMillis()
        );
        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri, null, null, null, sortOrder);
        List<String> list = new ArrayList<>();

        adapter = new ForecastAdapter(getActivity(), cur, 0);

        View rootview = inflater.inflate(R.layout.fragment_weather_calendar, container, false);
        ListView listView2 = (ListView) rootview.findViewById(R.id.listview_forecast);
        listView2.setAdapter(adapter);

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(MESSAGE_ALLIAS, adapter.getItem(position));

                startActivity(intent);
            }
        });


        setHasOptionsMenu(true);
        return rootview;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }


}


