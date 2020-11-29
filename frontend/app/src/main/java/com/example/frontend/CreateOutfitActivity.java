package com.example.frontend;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

    private JSONObject clothAttribute = new JSONObject();
    String[] clothesID = new String[3];
    JSONArray clothes = new JSONArray();
    private String upperClothesId = EMPTY_STRING;
    private String trousersId = EMPTY_STRING;
    private String shoesId = EMPTY_STRING;
    private Spinner spinner_occasion;
    private String message = EMPTY_STRING;

    private ImageView upperClothesImage;
    private ImageView trousersImage;
    private ImageView shoesImage;

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

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.btn_save_outfit) {
            constructClothAttributeFromCheckBoxes();
            constructClothAttributeClothID();
            sendOutfitToServer(clothAttribute);
            Intent intent = new Intent();
            //TODO:  intent.putExtra() etc.
            setResult(RESULT_OK, intent);
            finish();
        }
        else {
            Drawable highlight = getResources().getDrawable(R.drawable.highlight);

            if (upperClothesIdMap.containsKey(viewId)) {
                if (upperClothesImage != null) {
                    upperClothesImage.setBackground(null);
                }
                upperClothesId = upperClothesIdMap.get(viewId);
                clothesID[0] = upperClothesId;
                upperClothesImage = view.findViewById(viewId);
                upperClothesImage.setBackground(highlight);
            }
            else if (trousersIdMap.containsKey(viewId)) {
                if (trousersImage != null) {
                    trousersImage.setBackground(null);
                }
                trousersId = trousersIdMap.get(viewId);
                clothesID[1] = trousersId;
                trousersImage = view.findViewById(viewId);
                trousersImage.setBackground(highlight);
            }
            else if (shoesIdMap.containsKey(viewId)){
                if (shoesImage != null) {
                    shoesImage.setBackground(null);
                }
                shoesId = shoesIdMap.get(viewId);
                clothesID[2] = shoesId;
                shoesImage = view.findViewById(viewId);
                shoesImage.setBackground(highlight);
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


    private ImageView addClothesOnUI(Bitmap bitmap) {
        ImageView image = new ImageView(this);
        GridLayout.LayoutParams clothesParams = new GridLayout.LayoutParams();
        clothesParams.width = 300;
        clothesParams.height = 300;
        image.setLayoutParams(clothesParams);
        image.setImageBitmap(bitmap);
        image.setOnClickListener(this);

        return image;
    }

    private void addAllClothesOnUI() {
        String userId = user.getUserId();

        for (int i = 0; i < upperClothesIdList.size(); i++) {
            String clothesId = upperClothesIdList.get(i);
            Bitmap bitmap = getClothesImage(userId, clothesId);
            ImageView image = addClothesOnUI(bitmap);
            image.setId(View.generateViewId());
            upperClothesLayout.addView(image);
            upperClothesIdMap.put(image.getId(), clothesId);
        }

        for (int i = 0; i < trousersIdList.size(); i++) {
            String clothesId = trousersIdList.get(i);
            Bitmap bitmap = getClothesImage(userId, clothesId);
            ImageView image = addClothesOnUI(bitmap);
            image.setId(View.generateViewId());
            trousersLayout.addView(image);
            trousersIdMap.put(image.getId(), clothesId);
        }

        for (int i = 0; i < shoesIdList.size(); i++) {
            String clothesId = shoesIdList.get(i);
            Bitmap bitmap = getClothesImage(userId, clothesId);
            ImageView image = addClothesOnUI(bitmap);
            image.setId(View.generateViewId());
            shoesLayout.addView(image);
            shoesIdMap.put(image.getId(), clothesId);
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
                if (clothes.getString("category").equals("Outwear") || clothes.getString("category").equals("Shirts")) {
                    upperClothesIdList.add(clothes.getString("id"));
                }
                else if (clothes.getString("category").equals("Trousers")) {
                    trousersIdList.add(clothes.getString("id"));
                }
                else {
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
                Log.d(TAG,responseStr);
                JSONObject responseJson = null;
                try {
                    responseJson = new JSONObject(responseStr);
                    if(responseJson.has("message"))
                        message = responseJson.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (response.isSuccessful()) {
                    //make a toast to let the server's message display to the user

                    if(Objects.requireNonNull(responseJson).has("message") ){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(CreateOutfitActivity.this,message,Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });
                    }
                    else{
                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(CreateOutfitActivity.this,"Successfully create an outfit!",Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                    }
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
