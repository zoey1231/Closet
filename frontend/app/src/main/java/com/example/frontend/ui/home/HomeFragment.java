package com.example.frontend.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

import com.example.frontend.Clothes;
import com.example.frontend.MainActivity;
import com.example.frontend.R;
import com.example.frontend.ServerCommunicationAsync;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class HomeFragment extends Fragment implements View.OnClickListener {
    private String userToken;
    private String userId;
    private String clothId;
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
    private String outfitId = EMPTY_STRING;
    private ArrayList<JSONObject> clothes = new ArrayList<>();

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

                } else {
                    // Request not successful
                    Log.d(TAG,"Outfit request is unsuccessful: "+responseStr);
                }
            }
        });
    }

    private void extractResponseOutfitData(JSONObject responseJson) {
        JSONArray clothes_jsonArray, seasons_jsonArray,occasions_jsonArray;
        try {
            if(responseJson.has("message"))
                message = responseJson.getString("message");
            if (responseJson.has("id"))
                outfitId = responseJson.getString("id");
            if(responseJson.has("clothes")){
                clothes_jsonArray = responseJson.getJSONArray("clothes");
                for (int i=0;i<clothes_jsonArray.length();i++){
                    clothes.add(clothes_jsonArray.getJSONObject(i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void extractClothesData(JSONObject clothesData, Clothes cloth) {
        JSONArray seasons_jsonArray,occasions_jsonArray;
        ArrayList<String> seasons = new ArrayList<>();
        ArrayList<String> occasions = new ArrayList<>();

        try {
            if(clothesData.has("seasons")){
                seasons_jsonArray = clothesData.getJSONArray("seasons");
                for (int i=0;i<seasons_jsonArray.length();i++){
                    seasons.add(seasons_jsonArray.getString(i));
                }
                cloth.setSeasons(seasons);
            }
            if(clothesData.has("occasions")){
                occasions_jsonArray = clothesData.getJSONArray("occasions");
                for (int i=0;i<occasions_jsonArray.length();i++){
                    occasions.add(occasions_jsonArray.getString(i));
                }
                cloth.setOccasions(occasions);
            }
            if(clothesData.has("category"))
                cloth.setCategory(clothesData.getString("category"));
            if(clothesData.has("color"))
                cloth.setColor(clothesData.getString("color"));
            if(clothesData.has("name"))
                cloth.setName(clothesData.getString("name"));
            if(clothesData.has("user"))
                cloth.setUser(clothesData.getString("user"));
            if(clothesData.has("updated"))
                cloth.setUpdated(clothesData.getString("updated"));
            if(clothesData.has("id"))
                cloth.setId(clothesData.getString("id"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private Drawable getOutfitImage(String userId, String clothId) {
        URL url;
        InputStream stream;
        BufferedInputStream buffer;

        try {
            url = new URL("http://closet-cpen321.westus.cloudapp.azure.com/api/UserClothingImages/" + userId + "/" + clothId + ".jpg");
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


//        OkHttpClient client = new OkHttpClient();
//
//        Request request = new Request.Builder()
//                .url("http://closet-cpen321.westus.cloudapp.azure.com/api/UserClothingImages/" + userId + "/" + clothingId + ".jpg")
//                .build();
//        Call call = client.newCall(request);
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//
//            }
//        });

    }

}