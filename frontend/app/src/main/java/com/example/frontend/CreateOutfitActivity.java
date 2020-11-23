package com.example.frontend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

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

public class CreateOutfitActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "CreateOutfitActivity";

    private User user;

    private GridLayout upperClothesLayout;
    private GridLayout trousersLayout;
    private GridLayout shoesLayout;
    private Button button;

    private List<String> upperClothesIdList = new ArrayList<>();
    private List<String> trousersIdList = new ArrayList<>();
    private List<String> shoesIdList = new ArrayList<>();

    private String attributes; //change this

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

        getAllClothesFromServer();
        while (upperClothesIdList.size() == 0 || trousersIdList.size() == 0 || shoesIdList.size() == 0) {
            Log.d(TAG, "waiting for ids");
        }
        addAllClothesOnUI();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_save_outfit:
                sendOutfitToServer();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    private ImageView addClothesOnUI(Bitmap bitmap) {
        ImageView clothes = new ImageView(this);
        GridLayout.LayoutParams clothesParams = new GridLayout.LayoutParams();
        clothesParams.width = 300;
        clothesParams.height = 300;
        clothes.setLayoutParams(clothesParams);
        clothes.setImageBitmap(bitmap);

        return clothes;
    }

    private void addAllClothesOnUI() {
        String userId = user.getUserId();

        for (int i = 0; i < upperClothesIdList.size(); i++) {
            String clothesId = upperClothesIdList.get(i);
            Bitmap bitmap = getClothesImage(userId, clothesId);
            Log.d(TAG, "testing: bitmap is " + bitmap);
            ImageView clothes = addClothesOnUI(bitmap);
            upperClothesLayout.addView(clothes);
        }

        for (int i = 0; i < trousersIdList.size(); i++) {
            String clothesId = trousersIdList.get(i);
            Bitmap bitmap = getClothesImage(userId, clothesId);
            ImageView clothes = addClothesOnUI(bitmap);
            trousersLayout.addView(clothes);
        }

        for (int i = 0; i < shoesIdList.size(); i++) {
            String clothesId = shoesIdList.get(i);
            Bitmap bitmap = getClothesImage(userId, clothesId);
            ImageView clothes = addClothesOnUI(bitmap);
            shoesLayout.addView(clothes);
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
                if (clothes.getString("category").equals("outwear") || clothes.getString("category").equals("shirt")) {
                    upperClothesIdList.add(clothes.getString("id"));
                }
                if (clothes.getString("category").equals("trousers")) {
                    trousersIdList.add(clothes.getString("id"));
                }
                if (clothes.getString("category").equals("shoes")) {
                    shoesIdList.add(clothes.getString("id"));
                }
            }
        }
    }

    private Bitmap getClothesImage(String userId, String clothId) {
        URL url;
        InputStream stream;
        BufferedInputStream buffer;

        try {
            url = new URL("http://closet-cpen321.westus.cloudapp.azure.com/UserClothingImages/" + userId + "/" + clothId + ".png");
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

    private void sendOutfitToServer() {
        ServerCommAsync serverCommu = new ServerCommAsync();

        serverCommu.postWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/outfits/one", attributes, user.getUserToken(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }
        });
    }
}
