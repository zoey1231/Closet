package com.example.frontend.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.test.espresso.idling.CountingIdlingResource;

import com.example.frontend.MainActivity;
import com.example.frontend.R;
import com.example.frontend.ServerCommAsync;
import com.example.frontend.User;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeFragment extends Fragment implements View.OnClickListener {
    private String TAG = "HomeFragment";
    private static final String EMPTY_STRING = "";
    private String userToken, userId, userCity;
    private User user;
    private static String icon_today,icon_tmr;
    private static String monthDesc_today,dayDesc_today,date_today;
    private static String monthDesc_tmr,dayDesc_tmr,date_tmr;
    private static String temp_min_today,temp_max_today,temp_min_tmr,temp_max_tmr;
    private TextView tv_date_today,tv_date_tmr,tv_temp_today,tv_temp_tmr;
    private ImageView iv_icon_today,iv_icon_tmr;

    private Button getButton, createButton;
    private GridLayout outfitsLayout;
    private JSONObject outfit_opinion = new JSONObject();
    private boolean like = false;
    private boolean dislike = false;
    private boolean undoDislike = false;
    private String opinion = "unknown";

    static CountingIdlingResource idlingResource = new CountingIdlingResource("send_get_outfit_request");
    private String message = EMPTY_STRING;
    private String warning = EMPTY_STRING;
    private String success = EMPTY_STRING;

    private static List<String> outfitIdList = new ArrayList<>();
    private static List<String> clothesIdList = new ArrayList<>();
    private static HashMap<Integer, String> outfitIdMap = new HashMap<>();
    private HashMap<Integer, String[]> opinionMap = new HashMap<>();

    View root;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        root = inflater.inflate(R.layout.fragment_home, container, false);

        //get User's data from MainActivity
        user = MainActivity.getUser();
        userToken = user.getUserToken();
        userId = user.getUserId();

        tv_date_today = root.findViewById(R.id.tv_date_today);
        tv_date_tmr = root.findViewById(R.id.tv_date_tmr);
        tv_temp_today = root.findViewById(R.id.tv_temp_today);
        tv_temp_tmr = root.findViewById(R.id.tv_temp_tmr);
        iv_icon_today = root.findViewById(R.id.iv_icon_today);
        iv_icon_tmr = root.findViewById(R.id.iv_icon_tmr);

        getButton = root.findViewById(R.id.btn_get_outfit);
        getButton.setOnClickListener(this);
        createButton = root.findViewById(R.id.btn_create_outfit);
        createButton.setOnClickListener(this);
        outfitsLayout = root.findViewById(R.id.gl_outfit);

        getWeatherData();

        Log.d(TAG, "testing: outfit list size is " + outfitIdList.size());
        if (outfitIdList.size() == 0) {
            getMultipleOutfitsFromServer();
        }
        addTodayOutfitsOnUI();

        return root;
    }

    @Override
    public void onClick(View view) {

        int selectedId = view.getId();
        if(selectedId == R.id.btn_get_outfit){
                            idlingResource.increment();
            getButton.setEnabled(false);
            getOutfitFromServer(userToken);
            getButton.setEnabled(true);
        }
        else if (selectedId == R.id.btn_create_outfit) {
            createButton.setEnabled(false);

            Intent createOutfitIntent = new Intent(HomeFragment.this.getContext(), CreateOutfitActivity.class);
            startActivityForResult(createOutfitIntent, 1);
            createButton.setEnabled(true);
        }
        else if(opinionMap.containsKey(selectedId)){
            String[] valueArray = opinionMap.get(selectedId);
            String likeOrDislike = valueArray[0];
            String outfitID = valueArray[1];
            if(likeOrDislike.equals("like")){
                like = true;
                opinion = "like";
                try {
                    outfit_opinion.put("opinion", "like");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else if(likeOrDislike.equals("dislike")){
                dislike = true;
                opinion = "dislike";
                try {
                    outfit_opinion.put("opinion", "dislike");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else if(likeOrDislike.equals("unknown")){
                Log.d(TAG,"clicked undo opinion");
                undoDislike = true;
                opinion = "unknown";
                try {
                    outfit_opinion.put("opinion", "unknown");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

//            if (opinion.equals("dislike")) {
//                String outfitId = outfitIdMap.get(selectedId);
//                int outfitIndex = outfitIdList.indexOf(outfitId);
//                outfitIdList.remove(outfitIndex);
//                clothesIdList.remove(outfitIndex*3+2);
//                clothesIdList.remove(outfitIndex*3+1);
//                clothesIdList.remove(outfitIndex*3);
//            }

            //send response to server
                idlingResource.increment();
            sendOutfitOpinionToServer(outfit_opinion,outfitID);

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            // here you can retrieve your bundle data.
            String outfitId = data.getStringExtra("outfitId");
            String upperClothesId = data.getStringExtra("upperClothesId");
            String trousersId = data.getStringExtra("trousersId");
            String shoesId = data.getStringExtra("shoesId");

            outfitIdList.add(outfitId);
            clothesIdList.add(upperClothesId);
            clothesIdList.add(trousersId);
            clothesIdList.add(shoesId);

            addOutfitOnUI(outfitId, upperClothesId, trousersId, shoesId);
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

    public void getOutfitFromServer(final String userToken) {
        ServerCommAsync serverCommunication = new ServerCommAsync();
        Log.d(TAG,"prepared to getOutfitFromServer");

        serverCommunication.getWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/outfits/one",userToken, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.d(TAG,"Fail to send request to server");
                Log.d(TAG, String.valueOf(e));
                idlingResource.decrement();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseStr = Objects.requireNonNull(response.body()).string();

                JSONObject responseJson;
                try {
                    responseJson = new JSONObject(responseStr);
                    if(responseJson.has("success")){
                        success = responseJson.getString("success");
                    }
                    if(responseJson.has("message")){
                        message = responseJson.getString("message");
                    }
                    if(responseJson.has("warning")){
                        warning = responseJson.getString("warning");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (response.isSuccessful()) {
                    Log.d(TAG,"Outfit request is unsuccessful: " + responseStr);
                    try {
                        //retrieve outfit data from server's response
                        responseJson = new JSONObject(responseStr);
                        extractResponseOutfitData(responseJson.getJSONObject("outfit"));
//                            idlingResource.decrement();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG,"Outfit request is unsuccessful");
                }

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                    if (!message.equals(EMPTY_STRING)){
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    if (!warning.equals(EMPTY_STRING)) {
                        Toast.makeText(getContext(), warning, Toast.LENGTH_SHORT).show();
                    }

                    }
                });
                if(message.equals("Failed to generate an outfit, please try again later") && warning.equals(EMPTY_STRING)){
                    Log.d(TAG,"try to get an outfit again. Call getOutfitFromServer() again");
                    getOutfitFromServer(userToken);
                }
            }
        });
    }

    public void getMultipleOutfitsFromServer() {
        ServerCommAsync serverCommunication = new ServerCommAsync();
        Log.d(TAG,"prepared to getMultipleOutfitFromServer");

        serverCommunication.getWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/outfits/multiple",userToken, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.d(TAG,"Fail to send request to server");
                Log.d(TAG, String.valueOf(e));
                idlingResource.decrement();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseStr = Objects.requireNonNull(response.body()).string();
                JSONObject responseJson;

                if (response.isSuccessful()) {
                    JSONArray outfitArray = new JSONArray();
                    Log.d(TAG,"Outfit request is unsuccessful: " + responseStr);
                    try {
                        //retrieve outfit data from server's response
                        responseJson = new JSONObject(responseStr);
                        if (responseJson.has("outfits")) {
                            outfitArray = responseJson.getJSONArray("outfits");
                        }
                        for (int i = 0; i < outfitArray.length(); i++) {
                            extractResponseOutfitData(outfitArray.getJSONObject(i));
                        }
//                        idlingResource.decrement();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG,"Outfit request is unsuccessful");
                }
            }
        });
    }

    private void extractResponseOutfitData(JSONObject outfitJSON) throws JSONException{
        JSONObject upperClothesJSON = outfitJSON.getJSONObject("chosenUpperClothes");
        JSONObject trousersJSON = outfitJSON.getJSONObject("chosenTrousers");
        JSONObject shoesJSON = outfitJSON.getJSONObject("chosenShoes");

        String outfitId  = EMPTY_STRING;
        String upperClothesId = EMPTY_STRING;
        String trousersId = EMPTY_STRING;
        String shoesId = EMPTY_STRING;

        try {
            if (outfitJSON.has("_id")) {
                Log.d(TAG, "extracting outfit id");
                outfitId = outfitJSON.getString("_id");
                outfitIdList.add(outfitId);
            }
            if (upperClothesJSON.has("id")){
                upperClothesId = upperClothesJSON.getString("id");
                clothesIdList.add(upperClothesId);
            }
            if (trousersJSON.has("id")){
                trousersId = trousersJSON.getString("id");
                clothesIdList.add(trousersId);
            }
            if (shoesJSON.has("id")){
                shoesId = shoesJSON.getString("id");
                clothesIdList.add(shoesId);
            }
            if(outfitJSON.has("message")){
                message = outfitJSON.getString("message");
            }
            if(outfitJSON.has("success")){
                success = outfitJSON.getString("success");
            }

            addOutfitOnUI(outfitId,upperClothesId, trousersId, shoesId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addOutfitOnUI(String outfitId,String upperClothesId, String trousersId, String shoesId) {

        ImageView image1 = new ImageView(getContext());
        image1.setId(View.generateViewId());
        LinearLayout.LayoutParams image1Params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        image1Params.width = 300;
        image1Params.height = 300;
        image1Params.leftMargin = 45;
        image1.setLayoutParams(image1Params);
        image1.setImageBitmap(getClothesImage(upperClothesId));

        ImageView image2 = new ImageView(getContext());
        image2.setId(View.generateViewId());
        LinearLayout.LayoutParams image2Params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        image2Params.width = 300;
        image2Params.height = 300;
        image2Params.leftMargin = 45;
        image2.setLayoutParams(image2Params);
        image2.setImageBitmap(getClothesImage(trousersId));

        ImageView image3 = new ImageView(getContext());
        image3.setId(View.generateViewId());
        LinearLayout.LayoutParams image3Params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        image3Params.width = 300;
        image3Params.height = 300;
        image3Params.leftMargin = 45;
        image3.setLayoutParams(image3Params);
        image3.setImageBitmap(getClothesImage(shoesId));

        //clothes layout
        LinearLayout clothesLayout = new LinearLayout(getContext());
        clothesLayout.setOrientation(LinearLayout.HORIZONTAL);
        clothesLayout.addView(image1);
        clothesLayout.addView(image2);
        clothesLayout.addView(image3);

        //like button
        Button likeBtn = new Button(getContext());
        likeBtn.setId(View.generateViewId());
        likeBtn.setText("LIKE IT!");
        LinearLayout.LayoutParams likeBtnParams = new LinearLayout.LayoutParams(0, 100);
        likeBtnParams.weight = 1;
        likeBtnParams.leftMargin = 20;
        likeBtnParams.rightMargin = 20;
        likeBtn.setLayoutParams(likeBtnParams);
        likeBtn.setOnClickListener(this);

        //dislike button
        Button dislikeBtn = new Button(getContext());
        dislikeBtn.setId(View.generateViewId());
        dislikeBtn.setText("DISLIKE");
        LinearLayout.LayoutParams dislikeBtnParams = new LinearLayout.LayoutParams(0, 100);
        dislikeBtnParams.weight = 1;
        dislikeBtnParams.leftMargin = 20;
        dislikeBtnParams.rightMargin = 20;
        dislikeBtn.setLayoutParams(dislikeBtnParams);
        dislikeBtn.setOnClickListener(this);
        likeBtn.setEnabled(true);
        dislikeBtn.setEnabled(true);

        // buttons layout
        LinearLayout buttonsLayout = new LinearLayout(getContext());
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonsLayout.addView(likeBtn);
        buttonsLayout.addView(dislikeBtn);
        LinearLayout.LayoutParams buttonsLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,100);
        buttonsLayout.setLayoutParams(buttonsLayoutParams);


        //outfit linear layout
        final LinearLayout outfitLayout = new LinearLayout(getContext());
        outfitLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams outfitLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        outfitLayout.setLayoutParams(outfitLayoutParams);
        outfitLayout.addView(clothesLayout);
        outfitLayout.addView(buttonsLayout);

        //dislike constraint layout(undo screen)
        ConstraintLayout dislikeLayout = new ConstraintLayout(getContext());
        dislikeLayout.setId(View.generateViewId());
        dislikeLayout.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400));

        //gray filter
        View gray_filter_view  = new View(getContext());
        gray_filter_view.setId(View.generateViewId());
        gray_filter_view.setBackgroundColor(Color.parseColor("#9F5E5D5D"));//gray color
        ConstraintLayout.LayoutParams gray_filter_view_Params= new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        gray_filter_view_Params.topToTop = dislikeLayout.getId();
        gray_filter_view_Params.bottomToBottom = dislikeLayout.getId();
        gray_filter_view_Params.startToStart = dislikeLayout.getId();
        gray_filter_view_Params.endToEnd = dislikeLayout.getId();
        gray_filter_view.setLayoutParams(gray_filter_view_Params);


        //"will not display again" text
        TextView willNotDisplayText = new TextView(getContext());
        willNotDisplayText.setId(View.generateViewId());
        willNotDisplayText.setText("We will not suggest this outfit any more, OR ");
        willNotDisplayText.setPadding(20,10,20,0);
        willNotDisplayText.setTextColor(Color.parseColor("#FAFAFA"));
        willNotDisplayText.setTextSize(18);
        willNotDisplayText.setTypeface(willNotDisplayText.getTypeface(), Typeface.BOLD);
        ConstraintLayout.LayoutParams willNotDisplayTextParams= new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        willNotDisplayTextParams.topToTop = dislikeLayout.getId();
        willNotDisplayTextParams.bottomToBottom = dislikeLayout.getId();
        willNotDisplayTextParams.startToStart = dislikeLayout.getId();
        willNotDisplayTextParams.endToEnd = dislikeLayout.getId();
        willNotDisplayTextParams.verticalBias = (float) 0.10;
        willNotDisplayText.setLayoutParams(willNotDisplayTextParams);


        //Undo imageButton
        ImageView undoBtn = new ImageView(getContext());
        undoBtn.setId(View.generateViewId());
        ConstraintLayout.LayoutParams undoBtnParams= new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        undoBtnParams.height = 100;
        undoBtnParams.width = 100;
        undoBtnParams.topToBottom = willNotDisplayText.getId();
        undoBtnParams.bottomToBottom = dislikeLayout.getId();
        undoBtnParams.startToStart = dislikeLayout.getId();
        undoBtnParams.endToEnd = dislikeLayout.getId();
        undoBtnParams.verticalBias = (float) 0.15;
        undoBtn.setLayoutParams(undoBtnParams);
        undoBtn.setImageResource(R.drawable.undo);
        undoBtn.setOnClickListener(this);

        //Undo text
        TextView undoText = new TextView(getContext());
        undoText.setId(View.generateViewId());
        ConstraintLayout.LayoutParams undoTextParams= new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        undoTextParams.topToBottom = undoBtn.getId();
        undoTextParams.bottomToBottom = dislikeLayout.getId();
        undoTextParams.startToStart = dislikeLayout.getId();
        undoTextParams.endToEnd = dislikeLayout.getId();
        undoText.setLayoutParams(undoTextParams);
        undoText.setText("Undo");
        undoText.setTextColor(Color.parseColor("#5DBCD2"));
        undoText.setTextSize(20);
        undoText.setTypeface(willNotDisplayText.getTypeface(), Typeface.BOLD);
        undoText.setOnClickListener(this);

        dislikeLayout.addView(gray_filter_view);
        dislikeLayout.addView(willNotDisplayText);
        dislikeLayout.addView(undoBtn);
        dislikeLayout.addView(undoText);

        dislikeLayout.setVisibility(View.INVISIBLE);

        //Main relative layout
        final RelativeLayout mainLayout = new RelativeLayout(getContext());
        mainLayout.setId(View.generateViewId());
        mainLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 450));
        mainLayout.addView(outfitLayout);
        mainLayout.addView(dislikeLayout);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                outfitsLayout.addView(mainLayout);
                outfitsLayout.setVisibility(View.VISIBLE);
            }
        });

        //record buttons/clickable text's corresponding outfitID in hashMap outfitIdMap
        opinionMap.put(likeBtn.getId(), new String[]{"like", outfitId});
        opinionMap.put(dislikeBtn.getId(), new String[]{"dislike", outfitId});
        opinionMap.put(undoBtn.getId(), new String[]{"unknown", outfitId});
        opinionMap.put(undoText.getId(), new String[]{"unknown", outfitId});
        opinionMap.put(dislikeLayout.getId(), new String[]{"dislikeLayout", outfitId});

        outfitIdMap.put(dislikeBtn.getId(), outfitId);

        for (Map.Entry<Integer, String[]> entry : opinionMap.entrySet()) {
            Log.d(TAG,entry.getKey()+" : "+entry.getValue()[0]+" "+entry.getValue()[1]);
        }
        Log.d(TAG,"opinion:"+opinion);

    }

    private void addTodayOutfitsOnUI() {
        for (int i = 0; i < clothesIdList.size(); i += 3) {
            String upperClothesId = clothesIdList.get(i);
            String trousersId = clothesIdList.get(i+1);
            String shoesId = clothesIdList.get(i+2);
            String outfitId = outfitIdList.get(i/3);
            addOutfitOnUI(outfitId,upperClothesId, trousersId, shoesId);
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

    private void sendOutfitOpinionToServer(JSONObject outfit_opinion, final String outfitId) {
        ServerCommAsync serverCommunication = new ServerCommAsync();
        final String data = outfit_opinion.toString();
        Log.d(TAG,"prepared to sendOutfitOpinionToServer");
        Log.d(TAG,"put request: "+outfit_opinion);

        serverCommunication.putWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/outfits/"+outfitId, data,userToken, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Fail to send outfit opinion to server");
                Log.d(TAG, String.valueOf(e));
                idlingResource.decrement();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                String responseStr = Objects.requireNonNull(response.body()).string();
                Log.d(TAG, responseStr);

                if (response.isSuccessful()) {
                    if(like){
                        like = !like;
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getContext(), "Your preference has been recorded", Toast.LENGTH_SHORT).show();
                                setEnable(new String[] {"like",outfitId},false);
                                setEnable(new String[] {"dislike",outfitId},false);
                            }
                        });

                    } else if(dislike){
                        dislike = !dislike;
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {

                                setEnable(new String[] {"like",outfitId},false);
                                setEnable(new String[] {"dislike",outfitId},false);
                                setEnable(new String[] {"unknown",outfitId},true);
                                setVisibility(new String[] {"dislikeLayout",outfitId},View.VISIBLE);
                            }
                        });
                    } else if (undoDislike){
                        undoDislike = !undoDislike;
                        Log.d(TAG,"undo dislike");
                        //when finish undo dislike,jump back to main screen and let user to select again
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                setEnable(new String[] {"like",outfitId},true);
                                setEnable(new String[] {"dislike",outfitId},true);
                                setVisibility(new String[] {"dislikeLayout",outfitId},View.GONE);
                            }
                        });

                    }else {
                        Log.d(TAG," Error: invalid user's outfit opinion type");
                    }
                    idlingResource.decrement();
                }
            }


        });
    }

    /**
     * set the enabling of buttons/clickable texts whose viewID is stored in outfitIdMap as kay to valueInMap
     * @param valueInMap
     * @param enable set buttons/texts to enable if true; set to disable if false
     */
    private void setEnable(String[] valueInMap,Boolean enable) {
        Set<Integer> keySet = getKeysByValue(opinionMap,valueInMap);
        for(Integer id : keySet){
            getView().findViewById(id).setEnabled(enable);
        }
    }
    /**
     * set the visibility of view whose viewID is stored in outfitIdMap as kay to valueInMap
     * @param valueInMap
     * @param visibility
     */
    private void setVisibility(String[] valueInMap,int visibility) {
        Set<Integer> keySet = getKeysByValue(opinionMap,valueInMap);
        for(Integer id : keySet){
            getView().findViewById(id).setVisibility(visibility);
        }

    }

    /**
     * Return the set of keys mapped to value in Map map
     */
    public static  Set<Integer> getKeysByValue(Map<Integer,String[]> map, String[] value) {
        Set<Integer> keys = new HashSet<>();
        for (Map.Entry<Integer,String[]> entry : map.entrySet()) {

            if (Arrays.equals(value, entry.getValue())) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }



    public static CountingIdlingResource getRegisterIdlingResourceInTest() {
        return idlingResource;
    }

}