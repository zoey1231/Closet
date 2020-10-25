package com.example.frontend;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.IOException;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.services.calendar.Calendar;

public class CalendarActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_calendar);

        // obtain OAuth 2.0 access token
        GoogleCredentials credentials = null;
        try {
            credentials = GoogleCredentials.fromStream(new FileInputStream("/credentials.json")) //?why doesn't work
                    .createScoped("https://www.googleapis.com/auth/calendar",
                            "https://www.googleapis.com/auth/calendar.events");
            try {
                credentials.refreshIfExpired();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AccessToken token = credentials.getAccessToken();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // call Calendar API
        assert credentials != null;
        Calendar calendar = new Calendar.Builder(new NetHttpTransport(),
                new JacksonFactory(),
                new HttpCredentialsAdapter(credentials))
                .build();

        // example: insert event
        Event insertEvent = new Event()
                .setSummary("test");

        DateTime startDateTime = new DateTime("2020-10-24T15:00:00");
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Canada/Vancouver");
        insertEvent.setStart(start);

        DateTime endDateTime = new DateTime("2020-10-24T17:00:00");
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Canada/Vancouver");
        insertEvent.setEnd(end);

        try {
            insertEvent = calendar.events().insert("primary", insertEvent).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("Event created: %s\n", insertEvent.getHtmlLink());

        // example: get event
        Event getEvent = null;
        try {
            getEvent = calendar.events().get("primary", insertEvent.getId()).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(getEvent.getSummary());
    }
}
