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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.frontend.MainActivity;
import com.example.frontend.R;
import com.example.frontend.ServerCommAsync;
import com.example.frontend.User;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CalendarFragment extends Fragment implements OnDateSelectedListener, OnMonthChangedListener, View.OnClickListener {
    private static final String TAG ="CalendarFragment";
    private static final String EMPTY_STRING = "";
    private static final List<String> months = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

    private User user;
    private String userToken;
    private String code = EMPTY_STRING;

    private MaterialCalendarView calendar;
    private ListView events;
    private Button button;
    private CalendarAdapter adapter;
    private List<Event> eventList = new ArrayList<>();
    private HashMap<CalendarDay, List<Event>> eventMap = new HashMap<>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_calendar, container, false);
        user = MainActivity.getUser();
        userToken = user.getUserToken();

        calendar = root.findViewById(R.id.calendar);
        calendar.setDateTextAppearance(View.ACCESSIBILITY_LIVE_REGION_ASSERTIVE);
        calendar.setOnDateChangedListener(this);
        calendar.setOnMonthChangedListener(this);

        events = root.findViewById(R.id.lv_events);
        adapter = new CalendarAdapter(getActivity(), eventList);
        events.setAdapter(adapter);

        button = root.findViewById(R.id.btn_calendar);
        button.setOnClickListener(this);

        if (user.getCode() == null || user.getCode().equals(EMPTY_STRING)) {
            events.setVisibility(View.INVISIBLE);
        }
        else {
            button.setVisibility(View.GONE);
        }

        if (eventMap.containsKey(CalendarDay.today())) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    List<Event> eventList = eventMap.get(CalendarDay.today());
                    List<String> summaryList = new ArrayList<>();
                    for (int i = 0; i < eventList.size(); i++) {
                        summaryList.add(eventList.get(i).getSummary());
                    }
                    String toast = "You have " + summaryList + " today, don't forget to wear appropriate clothes!";
                    Log.d(TAG, "testing: toast is " + toast);
                    Toast.makeText(getContext(), toast, Toast.LENGTH_SHORT).show();
                }
            });
        }

        return root;
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        calendar.setHeaderTextAppearance(R.style.AppTheme);

        eventList =  eventMap.get(date);
        if(eventList != null && eventList.size() > 0) {
            adapter.addItems(eventList);
        }
        else {
            adapter.clear();
        }
    }

    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
        if (!code.equals(EMPTY_STRING)) {
            String month = months.get(date.getMonth() - 1);
            int year = date.getYear();
            eventMap.clear();
            getEventsFromServer(user.getCode(), userToken, month, year);
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
            code = data.getStringExtra("code");
            JSONObject JSONcode = new JSONObject();
            try {
                JSONcode.put("code", code);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            code = JSONcode.toString();
            user.setCode(code);
            button.setVisibility(View.GONE);
            events.setVisibility(View.VISIBLE);
        }
    }

    private void getEventsFromServer(final String code, String userToken, String month, int year) {
        ServerCommAsync serverComm = new ServerCommAsync();

        serverComm.postWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/calendar/" + month + "-" + year, code, userToken, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseStr = Objects.requireNonNull(response.body()).string();
                if (response.isSuccessful()) {
                    try {
                        JSONArray responseJSON = new JSONArray(responseStr);
                        extractResponseEventData(responseJSON);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void extractResponseEventData(JSONArray responseJSON) throws JSONException {
        LocalDate startDate = LocalDate.now(), endDate = LocalDate.now();
        String summary = EMPTY_STRING;

        for (int i = 0; i < responseJSON.length(); i++) {
            JSONObject eventJSON = responseJSON.getJSONObject(i);
            if (eventJSON.has("start")) {
                if (eventJSON.getJSONObject("start").has("date")) {
                    startDate = LocalDate.parse(eventJSON.getJSONObject("start").getString("date"));
                }
                else {
                    startDate = LocalDate.parse(eventJSON.getJSONObject("start").getString("dateTime").substring(0, 10));
                }
            }

            if (eventJSON.has("end")) {
                if (eventJSON.getJSONObject("end").has("date")) {
                    endDate = LocalDate.parse(eventJSON.getJSONObject("end").getString("date"));
                }
                else {
                    endDate = LocalDate.parse(eventJSON.getJSONObject("end").getString("dateTime").substring(0, 10));
                }
            }

            if (eventJSON.has("summary")) {
                summary = eventJSON.getString("summary");
            }

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                Event event = new Event(date, summary);
                CalendarDay day = CalendarDay.from(date);
                if (!eventMap.containsKey(day)) {
                    List<Event> events = new ArrayList<>();
                    events.add(event);
                    eventMap.put(day, events);
                }
                else {
                    List<Event> events = eventMap.get(day);
                    events.add(event);
                    eventMap.put(day, events);
                }
            }
            addDotsToCalendar();
        }
    }

    private void addDotsToCalendar() {
        final EventDecorator eventDecorator = new EventDecorator(Color.RED, eventMap.keySet());
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                calendar.addDecorator(eventDecorator);
            }
        });
    }
}