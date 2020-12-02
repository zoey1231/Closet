package com.example.frontend;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.widget.Toast.makeText;

public class CreateOutfitActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "CreateOutfitActivity";
    private static final String EMPTY_STRING = "";

    private User user;

    private GridLayout upperClothesLayout;
    private GridLayout trousersLayout;
    private GridLayout shoesLayout;
    private Button button;

    private List<String> upperClothesIdList = new ArrayList<>();
    private List<String> trousersIdList = new ArrayList<>();
    private List<String> shoesIdList = new ArrayList<>();

    private HashMap<Integer, String> upperClothesIdMap = new HashMap<>();
    private HashMap<Integer, String> trousersIdMap = new HashMap<>();
    private HashMap<Integer, String> shoesIdMap = new HashMap<>();
    private HashMap<Integer, Integer> iconMap = new HashMap<>();

    private JSONObject clothAttribute = new JSONObject();
    String[] clothesID = new String[3];
    JSONArray clothes = new JSONArray();
    private Spinner spinner_occasion;
    private String message = EMPTY_STRING;

    private static final int UPPERCLOTHES = 0;
    private static final int TROUSESRS = 1;
    private static final int SHOES = 2;

    private ImageView upperClothesIcon;
    private ImageView trousersIcon;
    private ImageView shoesIcon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_outfit);

        user = MainActivity.getUser();

        upperClothesLayout = findViewById(R.id.gl_upper_clothes);
        trousersLayout = findViewById(R.id.gl_trousers);
        shoesLayout = findViewById(R.id.gl_shoes);
        button = findViewById(R.id.btn_save_outfit);
        button.setOnClickListener(this);

        //occasion spinner
        spinner_occasion = findViewById(R.id.sp_occasion_outfit);

        spinnerAdapter.setAdapter(R.array.occasion_array, spinner_occasion,this);
        spinner_occasion.setOnItemSelectedListener(this);

        getAllClothesFromServer();
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onClick(View view) {
        int selectedId = view.getId();

        if (selectedId == R.id.btn_save_outfit) {
            constructClothAttributeFromCheckBoxes();
            constructClothAttributeClothID();
            sendOutfitToServer(clothAttribute);
        }
        else {
            if (upperClothesIdMap.containsKey(selectedId)) {
                if (upperClothesIcon != null && upperClothesIcon.getVisibility() == View.VISIBLE) {
                    upperClothesIcon.setVisibility(View.INVISIBLE);
                }
                String clothesId = upperClothesIdMap.get(selectedId);
                clothesID[UPPERCLOTHES] = clothesId;
                int iconId = iconMap.get(selectedId);
                upperClothesIcon = view.findViewById(iconId);
                upperClothesIcon.setVisibility(View.VISIBLE);
            }
            else if (trousersIdMap.containsKey(selectedId)) {
                if (trousersIcon != null && trousersIcon.getVisibility() == View.VISIBLE) {
                    trousersIcon.setVisibility(View.INVISIBLE);
                }
                String clothesId = trousersIdMap.get(selectedId);
                clothesID[TROUSESRS] = clothesId;
                int iconId = iconMap.get(selectedId);
                trousersIcon = view.findViewById(iconId);
                trousersIcon.setVisibility(View.VISIBLE);
            }
            else if (shoesIdMap.containsKey(selectedId)){
                if (shoesIcon != null && shoesIcon.getVisibility() == View.VISIBLE) {
                    shoesIcon.setVisibility(View.INVISIBLE);
                }
                String clothesId = shoesIdMap.get(selectedId);
                clothesID[SHOES] = clothesId;
                int iconId = iconMap.get(selectedId);
                shoesIcon = view.findViewById(iconId);
                shoesIcon.setVisibility(View.VISIBLE);
            }
        }
    }

    private void constructClothAttributeClothID() {
        for(int i = 0;i < clothesID.length;i++){
            clothes.put(clothesID[i]);
        }
        try {
            clothAttribute.put("clothes", clothes);
            Log.d(TAG, "clothes:"+clothes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long l) {
        //construct the clothAttribute JSONObject we want to send to server
        constructClothAttribute(parent,view,pos);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        //Toast.makeText(parent.getContext(),"You must select one of the options",Toast.LENGTH_SHORT).show();
    }

    private void constructClothAttribute(AdapterView<?> parent, View view, int pos) {
        Log.d(TAG,"VIEW: "+ view.getId());
        switch (parent.getId()) {
            case R.id.sp_occasion_outfit:
                JSONArray occasions = new JSONArray();
                try {
                    String selection = parent.getItemAtPosition(pos).toString();
                    occasions.put(0,selection);
                    clothAttribute.put("occasions", occasions);
                    Log.d(TAG, "occasions:"+occasions.get(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            default:
        }
    }

    private void constructClothAttributeFromCheckBoxes() {
        CheckBox checkBox_spring = findViewById(R.id.cb_spring_outfit);
        CheckBox checkBox_summer = findViewById(R.id.cb_summer_outfit);
        CheckBox checkBox_fall = findViewById(R.id.cb_fall_outfit);
        CheckBox checkBox_winter = findViewById(R.id.cb_winter_outfit);
        CheckBox checkBox_all = findViewById(R.id.cb_all_outfit);

        JSONArray seasons = new JSONArray();
        if(checkBox_spring.isChecked()){
            seasons.put("Spring");
        }
        if(checkBox_summer.isChecked()){
            seasons.put("Summer");
        }
        if(checkBox_fall.isChecked()){
            seasons.put("Fall");
        }
        if(checkBox_winter.isChecked()){
            seasons.put("Winter");
        }
        if(checkBox_all.isChecked()){
            seasons.put("All");
        }

        try {
            clothAttribute.put("seasons", seasons);
            Log.d(TAG, "seasons:"+seasons);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void addClothesOnUI(String clothesId, String category) {
        ImageView image = new ImageView(this);
        image.setId(View.generateViewId());
        ConstraintLayout.LayoutParams imageParams = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        imageParams.width = 300;
        imageParams.height = 300;
        image.setLayoutParams(imageParams);
        String userId = user.getUserId();
        Bitmap bitmap = getClothesImage(userId, clothesId);
        image.setImageBitmap(bitmap);

        ImageView icon = new ImageView(this);
        icon.setId(View.generateViewId());
        ConstraintLayout.LayoutParams iconParams = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        iconParams.width = 90;
        iconParams.height = 90;
        icon.setLayoutParams(iconParams);
        icon.setVisibility(View.INVISIBLE);
        icon.setBackgroundResource(R.drawable.checked);

        ConstraintLayout clothes = new ConstraintLayout(this);
        clothes.setId(View.generateViewId());
        ConstraintLayout.LayoutParams clothesParams = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        clothesParams.width = 300;
        clothesParams.height = 300;
        clothes.setLayoutParams(clothesParams);
        clothes.addView(image);
        clothes.addView(icon);
        ConstraintSet constraint = new ConstraintSet();
        constraint.clone(clothes);
        constraint.connect(icon.getId(), ConstraintSet.RIGHT, image.getId(), ConstraintSet.RIGHT);
        constraint.connect(icon.getId(), ConstraintSet.BOTTOM, image.getId(), ConstraintSet.BOTTOM);
        constraint.applyTo(clothes);
        clothes.setOnClickListener(this);

        if (category.equals("upperClothes")) {
            upperClothesLayout.addView(clothes);
            upperClothesIdMap.put(clothes.getId(), clothesId);
        }
        else if (category.equals("trousers")) {
            trousersLayout.addView(clothes);
            trousersIdMap.put(clothes.getId(), clothesId);
        }
        else if (category.equals("shoes")) {
            shoesLayout.addView(clothes);
            shoesIdMap.put(clothes.getId(), clothesId);
        }
        iconMap.put(clothes.getId(), icon.getId());
    }

    private void addAllClothesOnUI() {
        for (int i = 0; i < upperClothesIdList.size(); i++) {
            String clothesId = upperClothesIdList.get(i);
            addClothesOnUI(clothesId, "upperClothes");
        }
        for (int i = 0; i < trousersIdList.size(); i++) {
            String clothesId = trousersIdList.get(i);
            addClothesOnUI(clothesId, "trousers");
        }

        for (int i = 0; i < shoesIdList.size(); i++) {
            String clothesId = shoesIdList.get(i);
            addClothesOnUI(clothesId, "shoes");
        }
    }

    private void getAllClothesFromServer() {
        ServerCommAsync serverComm = new ServerCommAsync();

        serverComm.getWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/clothes/" + user.getUserId(), user.getUserToken(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseStr = Objects.requireNonNull(response.body().string());
                Log.d(TAG, responseStr);

                if (response.isSuccessful()) {
                    JSONObject responseJSON;
                    try {
                        responseJSON = new JSONObject(responseStr);
                        extractClothesDataByCategory(responseJSON);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void extractClothesDataByCategory(JSONObject responseJSON) throws JSONException {
        JSONArray clothesArray = responseJSON.getJSONArray("clothes");
        for (int i = 0; i < clothesArray.length(); i++) {
            JSONObject clothes = clothesArray.getJSONObject(i);
            if (clothes.has("id")) {
                if (clothes.getString("category").equals("outwear") || clothes.getString("category").equals("shirts")) {
                    upperClothesIdList.add(clothes.getString("id"));
                }
                else if (clothes.getString("category").equals("trousers")) {
                    trousersIdList.add(clothes.getString("id"));
                }
                else if (clothes.getString("category").equals("shoes")){
                    shoesIdList.add(clothes.getString("id"));
                }
            }
        }
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addAllClothesOnUI();
            }
        });
    }

    private Bitmap getClothesImage(String userId, String clothId) {
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

            return bitmap;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void sendOutfitToServer(JSONObject userData) {
        ServerCommAsync serverCommu = new ServerCommAsync();
        final String data = userData.toString();
        Log.d(TAG,"data: "+data);
        serverCommu.postWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/outfits/one", data, user.getUserToken(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.d(TAG,"Fail to send request to server");
                Log.d(TAG, String.valueOf(e));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseStr = Objects.requireNonNull(response.body()).string();
                String outfitId = EMPTY_STRING;
                Log.d(TAG,responseStr);
                JSONObject responseJson = null;
                try {
                    responseJson = new JSONObject(responseStr);
                    if(responseJson.has("message")) {
                        message = responseJson.getString("message");
                    }
                    if (responseJson.getJSONObject("outfit").has("_id")) {
                        outfitId = responseJson.getJSONObject("outfit").getString("_id");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (response.isSuccessful()) {
                     // make a toast to let the server's message display to the user
                     runOnUiThread(new Runnable() {
                        public void run() {
                            final Toast toast = makeText(CreateOutfitActivity.this,"Successfully create an outfit!",Toast.LENGTH_SHORT);
                            toast.show();
                        }
                     });

                    Intent setOutfitIntent = new Intent();
                    setOutfitIntent.putExtra("outfitId", outfitId);
                    setOutfitIntent.putExtra("upperClothesId", clothesID[UPPERCLOTHES]);
                    setOutfitIntent.putExtra("trousersId", clothesID[TROUSESRS]);
                    setOutfitIntent.putExtra("shoesId", clothesID[SHOES]);
                    setResult(RESULT_OK, setOutfitIntent);
                    finish();
                }else {
                    // Request not successful
                    if(Objects.requireNonNull(responseJson).has("message") ){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(CreateOutfitActivity.this,message,Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });
                    }
                }
            }
        });
    }

}
