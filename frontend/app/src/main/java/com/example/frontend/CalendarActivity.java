package com.example.frontend;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.appdatasearch.GetRecentContextCall;

import java.net.MalformedURLException;
import java.net.URL;

public class CalendarActivity extends AppCompatActivity {

    // Google Calender API example
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_calendar);

        // create code verifier
        String code_verifier = "5d2309e5bb73b864f989753887fe52f79ce5270395e25862da6940d5";
        // create code challenge
        String code_challenge = "MChCW5vD-3h03HMGFZYskOSTir7II_MMTb8a9rJNhnI";

        // create authorization URL
        String url = "https://www.googleapis.com/auth/" +
                "scope=calendar&" +
                "response_type=code&" +
                "state=security_token%3D138r5719ru3e1%26url%3Dhttps%3A%2F%2Foauth2.example.com%2Ftoken&" +
                "redirect_uri=com.example.frontend%3A/oauth2redirect&" +
                "client_id=544548915743-ffk9jah2jl5frc43h7hv43b3eod7gmum.apps.googleusercontent.com";

        // send request to OAuth2.0 server
        final TextView textView = (TextView) findViewById(R.id.text);
        RequestQueue queue = new RequestQueue(this);
        StringRequest stringRequest = new StringRequest(GetRecentContextCall.Request.Method.GET, url,
                new GetRecentContextCall.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        textView.setText("Response is: " + response.substring(0, 500));
                    }
                }, new GetRecentContextCall.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textView.setText("That didn't work!");
            }
        });
    }
}
