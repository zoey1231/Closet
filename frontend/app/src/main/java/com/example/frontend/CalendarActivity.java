package com.example.frontend;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

public class CalendarActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_calendar);

        // obtain OAuth 2.0 access token
        String oauth_response = null;
        oauthRequest(oauth_response);
        String access_token = tokenAccess(oauth_response);

        if (access_token != null) {
            // call Google Calendar API
            callApi(access_token);
            String calendar_id = getCalendarId();
            if (calendar_id != null) {
                // add event

            }

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void oauthRequest(String oauth_response) {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("scope", "calendar") //?need to pass in calendar.events as well
                .addFormDataPart("client_id", "544548915743-ffk9jah2jl5frc43h7hv43b3eod7gmum.apps.googleusercontent.com")
                .addFormDataPart("response_type", "code")
                .addFormDataPart("redirect_uri", "urn:ietf:wg:oauth:2.0:oob")
                .build();

        Request request = new Request.Builder()
                .url("https://www.googleapis.com/auth")
                .post(requestBody)
                .build();

        // get OAuth 2.0 response
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            oauth_response = response.header("code");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String tokenAccess(String oauth_response) {
        // terminate if OAuth 2.0 response is error
        if (oauth_response == null) {
            return null;
        }

        // exchange authorization code for refresh and access tokens
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("code", oauth_response)
                .addFormDataPart("client_id", "544548915743-46rmudne8rdgibohnh3nf4j1hr19sehh.apps.googleusercontent.com")
                .addFormDataPart("client_secret", "SK0BUZzc9FAGha7GvMmBPe7I")
                .addFormDataPart("client_redirect_uri", "https://closet-293003.firebaseapp.com/__/auth/handler")
                .addFormDataPart("grant_type", "authorization_code")
                .build();

        Request request = new Request.Builder()
                .url("http://oauth2.googleapis.com")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.header("access_token");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void callApi(String accress_token) {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("Authorization: Bearer", accress_token)
                .build();

        Request request = new Request.Builder()
                .url("http://www.googleapis.com")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String getCalendarId() {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .build();

        Request request = new Request.Builder()
                .url("https://www.googleapis.com/calendar/v3/users/me/calendarList")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.header("id");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
