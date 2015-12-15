package com.example.dawiduk.podejscie2;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import static com.example.dawiduk.podejscie2.R.id.list_item_forecast_textview;

public class WeatherCalendarFragment extends Fragment {

    private final String LOG_TAG = WeatherCalendarFragment.class.getSimpleName();
    public static final String ZIP_CODE_LODZ ="94043";
    private ArrayAdapter<String> adapter;

    public WeatherCalendarFragment() {

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            BackgroundTask weatherTask = new BackgroundTask(adapter,getContext());
            weatherTask.execute(ZIP_CODE_LODZ);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        List<String> list = new ArrayList<>();

        adapter = new ArrayAdapter(getActivity(),R.layout.list_item_forecast, list_item_forecast_textview,list);

        View rootview=inflater.inflate(R.layout.fragment_weather_calendar, container, false);
        ListView listView2=(ListView) rootview.findViewById(R.id.listview_forecast);
        listView2.setAdapter(adapter);

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getActivity(),DetailActivity.class).putExtra("info",adapter.getItem(position));
                startActivity(intent);
            }
        });
//        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//
//               // Toast.makeText(getContext(), getContext().getString(R.string.toast_text), Toast.LENGTH_SHORT).show();
//
//
//
//                // Snackbar.make(view, getContext().getString(R.string.toast_text), Snackbar.LENGTH_LONG).setAction("click me ", new ShowSnackBar()).show();
//
//            }
//        });
//
//        TextView displayInfo = (TextView) rootview.findViewById(R.id.textView_forecast);



        setHasOptionsMenu(true);
        return rootview;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }




}


