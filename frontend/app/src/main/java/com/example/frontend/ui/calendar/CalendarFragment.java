package com.example.frontend.ui.calendar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.frontend.CalendarAdapter;
import com.example.frontend.Event;
import com.example.frontend.EventDecorator;
import com.example.frontend.GetAuthActivity;
import com.example.frontend.MainActivity;
import com.example.frontend.R;
import com.example.frontend.ServerCommAsync;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CalendarFragment extends Fragment implements OnDateSelectedListener, View.OnClickListener {
    private static final String TAG ="CalendarFragment";
    private String userToken;

    private MaterialCalendarView calendar;
    private ListView events;
    private Button button;
    private CalendarAdapter adapter;
    private HashMap<CalendarDay, List<Event>> eventMap = new HashMap<>();
    private List<Event> eventList = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_calendar, container, false);
        userToken = MainActivity.getUser().getUserToken();

        calendar = root.findViewById(R.id.calendar);
        calendar.setDateTextAppearance(View.ACCESSIBILITY_LIVE_REGION_ASSERTIVE);
        calendar.setSelectedDate(LocalDate.now());
        calendar.setOnDateChangedListener(this);

        events = root.findViewById(R.id.lv_events);
        events.setAdapter(adapter);
        events.setVisibility(View.INVISIBLE);

        button = root.findViewById(R.id.btn_calendar);
        button.setOnClickListener(this);

        adapter = new CalendarAdapter(getActivity(), eventList);
        makeJsonObjectRequest();
        //add small dots on event days
        EventDecorator eventDecorator = new EventDecorator(Color.RED, eventMap.keySet());
        calendar.addDecorator(eventDecorator);

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


                if(!eventMap.containsKey(day))
                {
                    List<Event> events = new ArrayList<>();
                    events.add(event);
                    eventMap.put(day,events);
                }else
                {
                    List<Event> events = eventMap.get(day);
                    events.add(event);
                    eventMap.put(day,events);

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // after parsing
        List<Event> event =  eventMap.get(CalendarDay.from(LocalDate.now()));
        if(event!=null && event.size()>0) {
            adapter.addItems(event);
        }else {
            adapter.clear();
        }

        //add small dots on event days
        EventDecorator eventDecorator = new EventDecorator(Color.RED, eventMap.keySet());
        calendar.addDecorator(eventDecorator);


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

        calendar.setHeaderTextAppearance(R.style.AppTheme);

        List<Event> event =  eventMap.get(date);
        if(event!=null && event.size()>0) {
            adapter.addItems(event);
        }else {
            adapter.clear();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(CalendarFragment.this.getContext(), GetAuthActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            // here you can retrieve your bundle data.
            String code = data.getStringExtra("code");
            JSONObject JSONcode = new JSONObject();
            try {
                JSONcode.put("code", code);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendCodeToServer(JSONcode, userToken);

            button.setVisibility(View.GONE);
            events.setVisibility(View.VISIBLE);
        }
    }

    private void sendCodeToServer(final JSONObject JSONcode, String userToken) {
        ServerCommAsync serverComm = new ServerCommAsync();

        serverComm.postWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/calendar/Nov-2020", JSONcode.toString(), userToken, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {

                }
                else {

                }
            }
        });
    }
}