package com.example.frontend.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.frontend.Clothes;
import com.example.frontend.MainActivity;
import com.example.frontend.R;
import com.example.frontend.ServerCommAsync;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeFragment extends Fragment implements View.OnClickListener {
    private String TAG = "HomeFragment";
    private static final String EMPTY_STRING = "";
    private String userToken, userId;

    private String icon_today,icon_tmr;
    private String monthDesc_today,dayDesc_today,date_today;
    private String monthDesc_tmr,dayDesc_tmr,date_tmr;
    private String temp_min_today,temp_max_today,temp_min_tmr,temp_max_tmr;
    private TextView tv_date_today,tv_date_tmr,tv_temp_today,tv_temp_tmr;
    private ImageView iv_icon_today,iv_icon_tmr;

    private Button outfitButton;
    private GridLayout outfitsLayout;

    private List<String> outfitsIdList = new ArrayList<>();
    private List<String> clothesIdList = new ArrayList<>();

    private JSONObject outfit_opinion = new JSONObject();
    private boolean like = false;
    private boolean dislike = false;
    private boolean undoDislike = false;

//    static CountingIdlingResource idlingResource = new CountingIdlingResource("send_get_outfit_request");
    private String message = EMPTY_STRING;
    private String warning = EMPTY_STRING;
    private String success = EMPTY_STRING;

    private boolean WAIT = true;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        //get User's data from MainActivity and display them on fragment
        userToken = MainActivity.getUser().getUserToken();
        userId = MainActivity.getUser().getUserId();

        tv_date_today = root.findViewById(R.id.tv_date_today);
        tv_date_tmr = root.findViewById(R.id.tv_date_tmr);
        tv_temp_today = root.findViewById(R.id.tv_temp_today);
        tv_temp_tmr = root.findViewById(R.id.tv_temp_tmr);
        iv_icon_today = root.findViewById(R.id.iv_icon_today);
        iv_icon_tmr = root.findViewById(R.id.iv_icon_tmr);

        outfitsLayout = root.findViewById(R.id.gl_outfit);

        outfitButton = root.findViewById(R.id.btn_outfit);
//        likeButton = root.findViewById(R.id.btn_like_outfit1);
//        dislikeButton = root.findViewById(R.id.btn_dislike_outfit1);
        outfitButton.setOnClickListener(this);
//        likeButton.setOnClickListener(this);
//        dislikeButton.setOnClickListener(this);
//
//        dislikeLayout = root.findViewById(R.id.view_dislike);
//        dislikeLayout.setVisibility(View.GONE);
//        undoText =  root.findViewById(R.id.tv_undo);
//        undoButton =  root.findViewById(R.id.btn_undo);
//        undoText.setOnClickListener(this);
//        undoButton.setOnClickListener(this);

        getWeatherData();
//        getTodayOutfitsFromServer();
//        while (WAIT && outfitsIdList.size() == 0) {
//            Log.d(TAG, "waiting for ids");
//        }
//        addAllOutfitsOnUI();

        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_outfit:
//                idlingResource.increment();
                outfitButton.setEnabled(false);
                getOutfitFromServer();

                //fail to generate an outfit
                if(!message.equals(EMPTY_STRING)&&!warning.equals(EMPTY_STRING)&&success.equals((EMPTY_STRING))){
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    Toast.makeText(getContext(), warning, Toast.LENGTH_SHORT).show();
                }
                else {
                    while (WAIT && outfitsIdList.size() == 0) {
                        Log.d(TAG, "waiting for ids");
                    }
                    addAllOutfitsOnUI();
//                    likeButton.setEnabled(true);
//                    dislikeButton.setEnabled(true);
                    outfitsLayout.setVisibility(View.VISIBLE);
                }
                outfitButton.setEnabled(true);

                break;

//            case R.id.btn_like_outfit1:
////                idlingResource.increment();
//                like = true;
//                //send response to server
//                try {
//                    outfit_opinion.put("opinion", "like");
//                    sendOutfitOpinionToServer(outfit_opinion);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                break;
//            case R.id.btn_dislike_outfit1:
////                idlingResource.increment();
//                dislike = true;
//                //send response to server
//                try {
//                    outfit_opinion.put("opinion", "dislike");
//                    sendOutfitOpinionToServer(outfit_opinion);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                break;
//
//            case R.id.btn_undo:
//            case R.id.tv_undo:
////                idlingResource.increment();
//                Log.d(TAG,"clicked undo opinion");
//                undoDislike = true;
//                //send response to server
//                try {
//                    outfit_opinion.put("opinion", "unknown");
//                    sendOutfitOpinionToServer(outfit_opinion);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                break;

            default:
        }
    }

    private void updateWeatherOnUI() {
        requireActivity().runOnUiThread(new Runnable() {
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

    private void addOutfitOnUI(String upperClothesId, String trousersId, String shoesId) {
        TextView outfitText = new TextView(getContext());
        outfitText.setText("outfit");

        ImageView image1 = new ImageView(getContext());
        image1.setId(View.generateViewId());
        LinearLayout.LayoutParams image1Params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        image1Params.width = 300;
        image1Params.height = 300;
        image1.setLayoutParams(image1Params);
        image1.setImageBitmap(getClothesImage(upperClothesId));

        ImageView image2 = new ImageView(getContext());
        image2.setId(View.generateViewId());
        LinearLayout.LayoutParams image2Params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        image2Params.width = 300;
        image2Params.height = 300;
        image2Params.leftMargin = 37;
        image2.setLayoutParams(image2Params);
        image2.setImageBitmap(getClothesImage(trousersId));

        ImageView image3 = new ImageView(getContext());
        image3.setId(View.generateViewId());
        LinearLayout.LayoutParams image3Params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        image3Params.width = 300;
        image3Params.height = 300;
        image3Params.leftMargin = 37;
        image3.setLayoutParams(image3Params);
        image3.setImageBitmap(getClothesImage(shoesId));

        // buttons


        LinearLayout clothesLayout = new LinearLayout(getContext());
        clothesLayout.setOrientation(LinearLayout.HORIZONTAL);
        clothesLayout.setPadding(37, 0, 37, 0);
        clothesLayout.addView(image1);
        clothesLayout.addView(image2);
        clothesLayout.addView(image3);

        // buttons layout


        LinearLayout outfitLayout = new LinearLayout(getContext());
        outfitLayout.setOrientation(LinearLayout.VERTICAL);
        outfitLayout.addView(outfitText);
        outfitLayout.addView(clothesLayout);

        outfitsLayout.addView(outfitLayout);
    }

    private void addAllOutfitsOnUI() {
        for (int i = 0; i < clothesIdList.size(); i += 3) {
            String upperClothesId = clothesIdList.get(i);
            String trousersId = clothesIdList.get(i+1);
            String shoesId = clothesIdList.get(i+2);
            addOutfitOnUI(upperClothesId, trousersId, shoesId);
        }
    }

    private void getWeatherData() {
        ServerCommAsync serverCommunication = new ServerCommAsync();
        Log.d(TAG,"prepared to getWeatherData");

        serverCommunication.getWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/weather/",userToken, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.d(TAG,"Fail to send weather request to server");
                Log.d(TAG, String.valueOf(e));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseStr = Objects.requireNonNull(response.body()).string();
                if (response.isSuccessful()) {
                    Log.d(TAG,"weather request is successful"+responseStr);
                    JSONObject responseJson;
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

    public void getOutfitFromServer() {
        ServerCommAsync serverCommunication = new ServerCommAsync();
//        Log.d(TAG,"prepared to getOutfitFromServer");

        serverCommunication.getWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/outfits/one",userToken, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.d(TAG,"Fail to send request to server");
                Log.d(TAG, String.valueOf(e));
//                idlingResource.decrement();
                WAIT = false;
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseStr = Objects.requireNonNull(response.body()).string();

                if (response.isSuccessful()) {

                    Log.d(TAG,"Outfit request is successful"+responseStr);
                    JSONObject responseJson;
                    try {
                        //retrieve outfit data from server's response
                        responseJson = new JSONObject(responseStr);
                        JSONObject outfitJSON = responseJson.getJSONObject("outfit");
                        extractResponseOutfitData(outfitJSON);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    JSONObject responseJson;
                    try {
                        //retrieve outfit data from server's response
                        responseJson = new JSONObject(responseStr);
                        // Request not successful
                        if(responseJson.has("message")){
                            message = responseJson.getString("message");
                        }
                        if(responseJson.has("warning")){
                            warning = responseJson.getString("warning");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG,"Outfit request is unsuccessful: "+message+warning);

                    WAIT = false;
                }
//                idlingResource.decrement();
            }
        });
    }

    private void getTodayOutfitsFromServer() {
        ServerCommAsync serverComm = new ServerCommAsync();

        //change this
        serverComm.getWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/outfits/multiple", userToken, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                WAIT = false;
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseStr = Objects.requireNonNull(response.body().string());
                if (response.isSuccessful()) {
                    JSONObject responseJSON;
                    try {
                        responseJSON = new JSONObject(responseStr);
                        JSONArray outfitsArray = responseJSON.getJSONArray("outfits");
                        if (outfitsArray.length() == 0) {
                            WAIT = false;
                        }
                        else {
                            outfitsIdList.clear();
                            clothesIdList.clear();
                            for (int i = 0; i < outfitsArray.length(); i++) {
                                JSONObject outfitJSON = outfitsArray.getJSONObject(i);
                                extractResponseOutfitData(outfitJSON);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    WAIT = false;
                }
            }
        });
    }

    private void extractResponseOutfitData(JSONObject outfitJSON) throws JSONException{
        JSONObject upperClothesJSON = outfitJSON.getJSONObject("chosenUpperClothes");
        JSONObject trousersJSON = outfitJSON.getJSONObject("chosenTrousers");
        JSONObject shoesJSON = outfitJSON.getJSONObject("chosenShoes");

        try {
            if (outfitJSON.has("_id")) {
                String outfitId = outfitJSON.getString("_id");
                outfitsIdList.add(outfitId);
            }
            if (upperClothesJSON.has("id")){
                String upperClothesId = upperClothesJSON.getString("id");
                clothesIdList.add(upperClothesId);
            }
            if (trousersJSON.has("id")){
                String trousersId = trousersJSON.getString("id");
                clothesIdList.add(trousersId);
            }
            if (shoesJSON.has("id")){
                String shoesId = shoesJSON.getString("id");
                clothesIdList.add(shoesId);
            }
            if(outfitJSON.has("message")){
                String message = outfitJSON.getString("message");
            }
            if(outfitJSON.has("success")){
                String success = outfitJSON.getString("success");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getClothesImage(String clothesId) {
        URL url;
        InputStream stream;
        BufferedInputStream buffer;

        try {
            url = new URL("http://closet-cpen321.westus.cloudapp.azure.com/UserClothingImages/" + userId + "/" + clothesId + ".jpg");
            stream = url.openStream();
            buffer = new BufferedInputStream(stream);
            Bitmap bitmap = BitmapFactory.decodeStream(buffer);
            if (stream != null) {
                stream.close();
            }
            buffer.close();

            return bitmap;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

//    private void sendOutfitOpinionToServer(JSONObject outfit_opinion) {
//        ServerCommAsync serverCommunication = new ServerCommAsync();
//        final String data = outfit_opinion.toString();
//        Log.d(TAG,"prepared to sendOutfitOpinionToServer");
//        Log.d(TAG,"put request: "+outfit_opinion);
//
//        serverCommunication.putWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/outfits/"+outfitId, data,userToken, new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                e.printStackTrace();
//                Log.d(TAG, "Fail to send outfit opinion to server");
//                Log.d(TAG, String.valueOf(e));
////                idlingResource.decrement();
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//
//                String responseStr = Objects.requireNonNull(response.body()).string();
//                Log.d(TAG, responseStr);
//
//                if (response.isSuccessful()) {
//                    if(like){
//                        like = !like;
//                        getActivity().runOnUiThread(new Runnable() {
//                            public void run() {
//                                Toast.makeText(getContext(), "Your preference has been recorded", Toast.LENGTH_SHORT).show();
//                                likeButton.setEnabled(false);
//                                dislikeButton.setEnabled(false);
//                            }
//                        });
//
//
//                    } else if(dislike){
//                        dislike = !dislike;
//                        getActivity().runOnUiThread(new Runnable() {
//                            public void run() {
//                                dislikeLayout.setVisibility(View.VISIBLE);
//                                likeButton.setEnabled(false);
//                                dislikeButton.setEnabled(false);
//                                undoButton.setEnabled(true);
//                                undoText.setEnabled(true);
//                            }
//                        });
//
//                    } else if (undoDislike){
//                        undoDislike = !undoDislike;
//                        Log.d(TAG,"undo dislike");
//                        //when finish undo dislike,jump back to main screen and let user to select again
//                        getActivity().runOnUiThread(new Runnable() {
//                            public void run() {
//                                dislikeLayout.setVisibility(View.GONE);
//                                likeButton.setEnabled(true);
//                                dislikeButton.setEnabled(true);
//                            }
//                        });
//
//                    }else {
//                        Log.d(TAG," Error: invalid user's outfit opinion type");
//                    }
////                    idlingResource.decrement();
//                }
//            }
//        });
//    }

//    public static CountingIdlingResource getRegisterIdlingResourceInTest() {
//        return idlingResource;
//    }

}