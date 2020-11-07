package com.example.frontend.ui.calendar;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.frontend.CalendarAdapter;
import com.example.frontend.Event;
import com.example.frontend.EventDecorator;
import com.example.frontend.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment implements OnDateSelectedListener {

    private MaterialCalendarView calendarView;
    private HashMap<CalendarDay, List<Event>> map = new HashMap<>();
    private ListView listView;
    private CalendarAdapter adapter;

    private Calendar cal;
    private List<Event> eventList = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_calendar, container, false);

        listView = root.findViewById(R.id.listView);

        adapter = new CalendarAdapter(getActivity(), eventList);
        listView.setAdapter(adapter);


        calendarView = root.findViewById(R.id.calendarView);
        calendarView.setDateTextAppearance(View.ACCESSIBILITY_LIVE_REGION_ASSERTIVE);

        calendarView.setSelectedDate(LocalDate.now());

        calendarView.setOnDateChangedListener(this);

        makeJsonObjectRequest();

        //add small dots on event days
        EventDecorator eventDecorator = new EventDecorator(Color.RED, map.keySet());
        calendarView.addDecorator(eventDecorator);

        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void makeJsonObjectRequest() {

        String response = loadJSONFromAsset();
        try {
            JSONArray jArray = new JSONArray(response);
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jsonObject = jArray.getJSONObject(i);
                String StartDate = jsonObject.getString("StartDate");
                LocalDate date = LocalDate.parse(StartDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.US));

                String title =  jsonObject.getString("Title");

                Log.d("Date ",""+date);
                CalendarDay day = CalendarDay.from(date);
                Event event = new Event(date,title);


                if(!map.containsKey(day))
                {
                    List<Event> events = new ArrayList<>();
                    events.add(event);
                    map.put(day,events);
                }else
                {
                    List<Event> events = map.get(day);
                    events.add(event);
                    map.put(day,events);

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // after parsing
        List<Event> event =  map.get(CalendarDay.from(LocalDate.now()));
        if(event!=null && event.size()>0) {
            adapter.addItems(event);
        }else {
            adapter.clear();
        }

        //add small dots on event days
        EventDecorator eventDecorator = new EventDecorator(Color.RED, map.keySet());
        calendarView.addDecorator(eventDecorator);


    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getActivity().getAssets().open("test.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {

        calendarView.setHeaderTextAppearance(R.style.AppTheme);

        List<Event> event =  map.get(date);
        if(event!=null && event.size()>0) {
            adapter.addItems(event);
        }else {
            adapter.clear();
        }
    }

}