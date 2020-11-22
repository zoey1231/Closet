package com.example.frontend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

    private TextView text;
    private GridLayout clothesLayout;
    private Button button;

    private List<String> clothesIdList = new ArrayList<>();
    private HashMap<Integer, String> clothesIdMap = new HashMap<Integer, String>();

    private String attributes; //change this

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_outfit);

        user = MainActivity.getUser();

        clothesLayout = findViewById(R.id.gl_outfit);
        button = findViewById(R.id.btn_save_outfit);
        button.setOnClickListener(this);
        text = findViewById(R.id.tv_outfit);

        getClothesFromServer("upperClothes");
        addClothesToCloset();
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

    private void addClothesToCloset() {
        for (int i = 0; i < clothesIdList.size(); i++) {
            String userId = user.getUserId();
            String clothesId = clothesIdList.get(i);
            Bitmap bitmap = getClothesImage(userId, clothesId);

            ImageView clothes = new ImageView(this);
            GridLayout.LayoutParams clothesParams = new GridLayout.LayoutParams();
            clothesParams.width = 300;
            clothesParams.height = 300;
            clothes.setLayoutParams(clothesParams);
            clothes.setImageBitmap(bitmap);
            clothes.setOnClickListener(this);
            clothesLayout.addView(clothes);

            //need adapter here
//            clothesIdMap.put(clothes.getId(), clothesId);
        }
    }

    private void getClothesFromServer(String category) {
        ServerCommAsync serverComm = new ServerCommAsync();

        serverComm.getWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/clothes" + user.getUserId() + "/?category=" + category, user.getUserToken(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseStr = Objects.requireNonNull(response.body().string());
                if (response.isSuccessful()) {
                    JSONObject responseJSON;
                    try {
                        responseJSON = new JSONObject(responseStr);
                        extractResponseClothesData(responseJSON);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void extractResponseClothesData(JSONObject responseJSON) throws JSONException {
        JSONArray clothesArray = responseJSON.getJSONArray("clothes");
        for (int i = 0; i < clothesArray.length(); i++) {
            JSONObject clothes = clothesArray.getJSONObject(i);
            if (clothes.has("id")) {
                clothesIdList.add(clothes.getString("id"));
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
