package com.example.frontend.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.frontend.MainActivity;
import com.example.frontend.R;
import com.example.frontend.ServerCommunicationAsync;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class HomeFragment extends Fragment implements View.OnClickListener {
    private String userToken;
    private HomeViewModel homeViewModel;
    private String TAG = "HomeFragment";
    private static final String EMPTY_STRING = "";

    private String icon_today,icon_tmr;
    private String monthDesc_today,dayDesc_today,date_today;
    private String monthDesc_tmr,dayDesc_tmr,date_tmr;
    private String temp_min_today,temp_max_today,temp_min_tmr,temp_max_tmr;

    TextView tv_date_today,tv_date_tmr,tv_temp_today,tv_temp_tmr;
    ImageView iv_icon_today,iv_icon_tmr;
    TextView tv_outfit1;
    ImageView iv_outfit1_cloth1,iv_outfit1_cloth2,iv_outfit1_cloth3;
    Button outfitIdea_btn;
    LinearLayout ll_outfit;

    private String message = EMPTY_STRING;
    private String cloth_id = EMPTY_STRING;
    private String category= EMPTY_STRING;
    private String color= EMPTY_STRING;
    private String name= EMPTY_STRING;
    private String updated= EMPTY_STRING;
    private String cloth_user= EMPTY_STRING;
    private ArrayList<String> seasons = new ArrayList<>();
    private ArrayList<String> occasions = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        tv_date_today = root.findViewById(R.id.tv_date_today);
        tv_date_tmr = root.findViewById(R.id.tv_date_tmr);
        tv_temp_today = root.findViewById(R.id.tv_temp_today);
        tv_temp_tmr = root.findViewById(R.id.tv_temp_tmr);
        iv_icon_today = root.findViewById(R.id.iv_icon_today);
        iv_icon_tmr = root.findViewById(R.id.iv_icon_tmr);

        tv_outfit1 = root.findViewById(R.id.tv_outfit1);
        iv_outfit1_cloth1 = root.findViewById(R.id.iv_outfit1_cloth1);
        iv_outfit1_cloth2 = root.findViewById(R.id.iv_outfit1_cloth2);
        iv_outfit1_cloth3 = root.findViewById(R.id.iv_outfit1_cloth3);
        ll_outfit = root.findViewById(R.id.ll_outfit);
        ll_outfit.setVisibility(View.GONE);
        outfitIdea_btn = root.findViewById(R.id.outfitIdea_btn);
        outfitIdea_btn.setOnClickListener(this);

        //get User's data from MainActivity and display them on fragment
        MainActivity activity = (MainActivity) getActivity();
        userToken = activity.getUser().getUserToken();
        getWeatherData(userToken);

        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.outfitIdea_btn:
                getOutfitData(userToken);
                ll_outfit.setVisibility(View.VISIBLE);
                break;
        }
    }
    private void getWeatherData(String userToken) {
        ServerCommunicationAsync serverCommunication = new ServerCommunicationAsync();
        Log.d(TAG,"prepared to sendUserDataToServer");

        serverCommunication.getWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/weather/vancouver",userToken, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d(TAG,"Fail to send weather request to server");
                Log.d(TAG, String.valueOf(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                if (response.isSuccessful()) {
                    Log.d(TAG,"weather request is successful"+responseStr);
                    JSONObject responseJson = null;
                    try {
                        //retrieve weather data from OpenWeather's response and display on UI
                        responseJson = new JSONObject(responseStr);
                        extractResponseData(responseJson);
                        updateWeatherOnUI();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Request not successful
                    Log.d(TAG,"weather request is unsuccessful"+responseStr);
                }
            }
        });
    }

    private void updateWeatherOnUI() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //update weather and time data on the UI
                tv_date_today.setText(monthDesc_today+" "+date_today+" ("+dayDesc_today+")");
                tv_temp_today.setText("Max "+temp_max_today+"°C "+"Min "+temp_min_today+"°C");
                tv_date_tmr.setText(monthDesc_tmr+" "+date_tmr +" ("+dayDesc_tmr+")");
                tv_temp_tmr.setText("Max "+temp_max_tmr+"°C "+"Min "+temp_min_tmr+"°C");
                Picasso.get().load("http://openweathermap.org/img/wn/"+icon_today+"@2x.png").resize(50, 50).centerCrop().into(iv_icon_today);
                Picasso.get().load("http://openweathermap.org/img/wn/"+icon_tmr+"@2x.png").resize(50, 50).centerCrop().into(iv_icon_tmr);
            }
        });
    }

    private void extractResponseData(JSONObject responseJson) {
        JSONObject today,tomorrow, time_today, time_tmr,temp_today,temp_tmr;
        JSONArray weather_today,weather_tmr;
        try {
            today = responseJson.getJSONObject("today");
            tomorrow = responseJson.getJSONObject("tomorrow");
            weather_today = today.getJSONArray("weather");
            weather_tmr = tomorrow.getJSONArray("weather");
            for(int i = 0; i < weather_today.length();i++){
                icon_today =weather_today.getJSONObject(i).getString("icon");
                icon_tmr =weather_tmr.getJSONObject(i).getString("icon");
            }
            temp_today = today.getJSONObject("temp");
            temp_min_today = temp_today.getString("min");
            temp_max_today = temp_today.getString("max");
            temp_tmr = tomorrow.getJSONObject("temp");
            temp_min_tmr = temp_tmr.getString("min");
            temp_max_tmr = temp_tmr.getString("max");

            //retrieve time data from OpenWeather's response
            time_today = today.getJSONObject("time");
            time_tmr = tomorrow.getJSONObject("time");
            monthDesc_today = time_today.getJSONObject("month").getString("monthDesc");
            dayDesc_today = time_today.getJSONObject("day").getString("dayDesc");
            date_today = time_today.getString("date");
            monthDesc_tmr = time_tmr.getJSONObject("month").getString("monthDesc");
            dayDesc_tmr = time_tmr.getJSONObject("day").getString("dayDesc");
            date_tmr = time_tmr.getString("date");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void getOutfitData(String userToken) {
        ServerCommunicationAsync serverCommunication = new ServerCommunicationAsync();
        Log.d(TAG,"prepared to sendUserDataToServer");

        serverCommunication.getWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/outfits/one",userToken, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d(TAG,"Fail to send request to server");
                Log.d(TAG, String.valueOf(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseStr = response.body().string();
                if (response.isSuccessful()) {

                    Log.d(TAG,"Outfit request is successful"+responseStr);
                    JSONObject responseJson = null;
                    try {
                        //retrieve outfit data from server's response
                        responseJson = new JSONObject(responseStr);
                        extractResponseData(responseJson);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    // Request not successful
                    Log.d(TAG,"Outfit request is unsuccessful: "+responseStr);
                }
            }
        });
    }


}