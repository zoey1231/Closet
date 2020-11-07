package com.example.frontend.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.test.espresso.idling.CountingIdlingResource;

import com.example.frontend.MainActivity;
import com.example.frontend.R;
import com.example.frontend.ServerCommunicationAsync;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.widget.Toast.makeText;


public class HomeFragment extends Fragment implements View.OnClickListener {
    private String userToken, userId;
    private HomeViewModel homeViewModel;
    private String TAG = "HomeFragment";
    private static final String EMPTY_STRING = "";

    private String icon_today,icon_tmr;
    private String monthDesc_today,dayDesc_today,date_today;
    private String monthDesc_tmr,dayDesc_tmr,date_tmr;
    private String temp_min_today,temp_max_today,temp_min_tmr,temp_max_tmr;
    TextView tv_date_today,tv_date_tmr,tv_temp_today,tv_temp_tmr;
    ImageView iv_icon_today,iv_icon_tmr;

    Button outfitButton,likeButton,dislikeButton;
    RelativeLayout rl_outfit;
    ImageView cloth1, cloth2, cloth3;
    ConstraintLayout view_dislike;
    TextView tv_undo;
    ImageButton undoButton;
    private String upperClothesId = EMPTY_STRING;
    private String trousersId = EMPTY_STRING;
    private String shoesId = EMPTY_STRING;

    static CountingIdlingResource idlingResource = new CountingIdlingResource("send_get_outfit_request");

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        tv_date_today = root.findViewById(R.id.tv_date_today);
        tv_date_tmr = root.findViewById(R.id.tv_date_tmr);
        tv_temp_today = root.findViewById(R.id.tv_temp_today);
        tv_temp_tmr = root.findViewById(R.id.tv_temp_tmr);
        iv_icon_today = root.findViewById(R.id.iv_icon_today);
        iv_icon_tmr = root.findViewById(R.id.iv_icon_tmr);

        outfitButton = root.findViewById(R.id.btn_outfit);
        likeButton = root.findViewById(R.id.btn_like_outfit1);
        dislikeButton = root.findViewById(R.id.btn_dislike_outfit1);
        outfitButton.setOnClickListener(this);
        likeButton.setOnClickListener(this);
        dislikeButton.setOnClickListener(this);

        view_dislike = root.findViewById(R.id.view_dislike);
        view_dislike.setVisibility(View.GONE);
        tv_undo =  root.findViewById(R.id.tv_undo);
        undoButton =  root.findViewById(R.id.btn_undo);
        tv_undo.setOnClickListener(this);
        undoButton.setOnClickListener(this);

        rl_outfit = root.findViewById(R.id.rl_outfit);
        rl_outfit.setVisibility(View.GONE);
        cloth1 = root.findViewById(R.id.iv_clothes1_outfit1);
        cloth2 = root.findViewById(R.id.iv_clothes2_outfit1);
        cloth3 = root.findViewById(R.id.iv_clothes3_outfit1);


        //get User's data from MainActivity and display them on fragment
        userToken = MainActivity.getUser().getUserToken();
        userId = MainActivity.getUser().getuserId();
        getWeatherData(userToken);

        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_outfit:
                idlingResource.increment();

                getOutfitData(userToken);
                rl_outfit.setVisibility(View.VISIBLE);
                cloth1.setBackground(getClothesImage(userId, upperClothesId));
                cloth2.setBackground(getClothesImage(userId, trousersId));
                cloth3.setBackground(getClothesImage(userId, shoesId));
                break;
            case R.id.btn_like_outfit1:
                makeText(getContext(), "Your preference has been recorded", Toast.LENGTH_SHORT).show();
                likeButton.setEnabled(false);
                dislikeButton.setEnabled(false);
                //send response to server
                break;
            case R.id.btn_dislike_outfit1:
                view_dislike.setVisibility(View.VISIBLE);
                likeButton.setEnabled(false);
                dislikeButton.setEnabled(false);

                //send response to server

                undoButton.setEnabled(true);
                tv_undo.setEnabled(true);

                break;

            case R.id.btn_undo:
            case R.id.tv_undo:
                view_dislike.setVisibility(View.GONE);
                likeButton.setEnabled(false);
                dislikeButton.setEnabled(false);

                //send response to server

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
                        extractResponseWeatherData(responseJson);
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

    private void extractResponseWeatherData(JSONObject responseJson) {
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
                idlingResource.decrement();
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
                        extractResponseOutfitData(responseJson);
                        
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    idlingResource.decrement();

                } else {
                    // Request not successful
                    Log.d(TAG,"Outfit request is unsuccessful: "+responseStr);
                    idlingResource.decrement();
                }
            }
        });
    }

    private void extractResponseOutfitData(JSONObject responseJson) {
        try {
            if(responseJson.has("chosenUpperClothes")){
                upperClothesId = responseJson.getString("chosenUpperClothes");
            }
            if(responseJson.has("chosenTrousers")){
                upperClothesId = responseJson.getString("chosenTrousers");
            }
            if(responseJson.has("chosenShoes")){
                upperClothesId = responseJson.getString("chosenShoes");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Drawable getClothesImage(String userId, String clothId) {
        URL url;
        InputStream stream;
        BufferedInputStream buffer;

        try {
            url = new URL("http://closet-cpen321.westus.cloudapp.azure.com/UserClothingImages/" + userId + "/" + clothId + ".jpg");
            stream = url.openStream();
            buffer = new BufferedInputStream(stream);
            Bitmap bitmap = BitmapFactory.decodeStream(buffer);
            if (stream != null) {
                stream.close();
            }
            buffer.close();

            return new BitmapDrawable(bitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static CountingIdlingResource getRegisterIdlingResourceInTest() {
        return idlingResource;
    }

}