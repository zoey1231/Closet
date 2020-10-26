package com.example.frontend;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;

import androidx.appcompat.app.AppCompatActivity;

import com.google.api.services.calendar.model.Event;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class CalendarActivity extends AppCompatActivity {

    ExtendedCalendarView calendar = (ExtendedCalendarView)findViewById(R.id.calendar);

//    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_calendar);

//        addEvent((Calendar)calendar);
    }

//    private void addEvent(Calendar cal) {
//        ContentValues values = new ContentValues();
//
//        values.put(CalendarProvider.COLOR, Event.COLOR_RED);
//        values.put(CalendarProvider.DESCRIPTION, "Some Description");
//        values.put(CalendarProvider.LOCATION, "Some location");
//                values.put(CalendarProvider.EVENT, "Event name");
//
//        cal.set(startDayYear, startDayMonth, startDayDay, startTimeHour, startTimeMin);
//        values.put(CalendarProvider.START, cal.getTimeInMillis());
//        values.put(CalendarProvider.START_DAY, julianDay);
//        TimeZone tz = TimeZone.getDefault();
//
//        cal.set(endDayYear, endDayMonth, endDayDay, endTimeHour, endTimeMin);
//        int endDayJulian = Time.getJulianDay(cal.getTimeInMillis(), TimeUnit.MILLISECONDS.toSeconds(tz.getOffset(cal.getTimeInMillis())));
//
//        values.put(CalendarProvider.END, cal.getTimeInMillis());
//        values.put(CalendarProvider.END_DAY, endDayJulian);
//
//        Uri uri = getContentResolver().insert(CalendarProvider.CONTENT_URI, values);
//    }
//
//        obtain OAuth 2.0 access token
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
