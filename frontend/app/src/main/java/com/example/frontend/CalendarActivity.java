package com.example.frontend;

import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.services.calendar.Calendar;
import com.riontech.calendar.CustomCalendar;
import com.riontech.calendar.dao.EventData;
import com.riontech.calendar.dao.dataAboutDate;
import com.riontech.calendar.utils.CalendarUtils;

public class CalendarActivity extends AppCompatActivity {

    private CustomCalendar calendar;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_calendar);

//        String[] arr = {"2016-06-10", "2016-06-11", "2016-06-15", "2016-06-16", "2016-06-25"};
//        for (int i = 0; i < 5; i++) {
//            int eventCount = 3;
//            customCalendar.addAnEvent(arr[i], eventCount, getEventDataList(eventCount));
//        }
        dataAboutDate date = new dataAboutDate();
        date.setTitle("test");
        ArrayList<dataAboutDate> data = new ArrayList<>();
        data.add(date);
        EventData event = new EventData();
        event.setData(data);
        ArrayList<EventData> events = new ArrayList<>();
        events.add(event);

        calendar.addAnEvent("2020-10-25", 1, events);
//        TextView test = new TextView(this);
//        test.setText(data.get(0).getTitle());
//        test.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
//        calendar.addView(test);
    }

    public ArrayList<EventData> getEventDataList(ArrayList<EventData> eventDataList) {
//        ArrayList<EventData> eventDataList = new ArrayList();
//
//        for (int i = 0; i < count; i++) {
//            EventData dateData = new EventData();
//            ArrayList<dataAboutDate> dataAboutDates = new ArrayList();
//
//            dateData.setSection(CalendarUtils.getNAMES()[new Random().nextInt(CalendarUtils.getNAMES().length)]);
//            dataAboutDate dataAboutDate = new dataAboutDate();
//
//            int index = new Random().nextInt(CalendarUtils.getEVENTS().length);
//
//            dataAboutDate.setTitle(CalendarUtils.getEVENTS()[index]);
//            dataAboutDate.setSubject(CalendarUtils.getEventsDescription()[index]);
//            dataAboutDates.add(dataAboutDate);
//
//            dateData.setData(dataAboutDates);
//            eventDataList.add(dateData);
//        }

        return eventDataList;
    }

//        // obtain OAuth 2.0 access token
//        GoogleCredentials credentials = null;
//        try {
//            credentials = GoogleCredentials.fromStream(new FileInputStream("/credentials.json")) //?why doesn't work
//                    .createScoped("https://www.googleapis.com/auth/calendar",
//                            "https://www.googleapis.com/auth/calendar.events");
//            try {
//                credentials.refreshIfExpired();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            AccessToken token = credentials.getAccessToken();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        // call Calendar API
//        assert credentials != null;
//        Calendar calendar = new Calendar.Builder(new NetHttpTransport(),
//                new JacksonFactory(),
//                new HttpCredentialsAdapter(credentials))
//                .build();
//
//        // example: insert event
//        Event insertEvent = new Event()
//                .setSummary("test");
//
//        DateTime startDateTime = new DateTime("2020-10-24T15:00:00");
//        EventDateTime start = new EventDateTime()
//                .setDateTime(startDateTime)
//                .setTimeZone("Canada/Vancouver");
//        insertEvent.setStart(start);
//
//        DateTime endDateTime = new DateTime("2020-10-24T17:00:00");
//        EventDateTime end = new EventDateTime()
//                .setDateTime(endDateTime)
//                .setTimeZone("Canada/Vancouver");
//        insertEvent.setEnd(end);
//
//        try {
//            insertEvent = calendar.events().insert("primary", insertEvent).execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.printf("Event created: %s\n", insertEvent.getHtmlLink());
//
//        // example: get event
//        Event getEvent = null;
//        try {
//            getEvent = calendar.events().get("primary", insertEvent.getId()).execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println(getEvent.getSummary());
//    }
}
