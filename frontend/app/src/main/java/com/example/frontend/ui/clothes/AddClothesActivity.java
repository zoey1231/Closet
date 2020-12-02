package com.example.frontend.ui.clothes;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.test.espresso.idling.CountingIdlingResource;

import com.example.frontend.MainActivity;
import com.example.frontend.R;
import com.example.frontend.ServerCommAsync;
import com.example.frontend.User;
import com.example.frontend.spinnerAdapter;

import org.jetbrains.annotations.NotNull;
import java.io.File;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


import static android.widget.Toast.makeText;


public class AddClothesActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "AddClothesActivity";
    private static final String EMPTY_STRING = "";

    private User user;
    private ImageView image, imageButton;
    private Button saveButton;
    private TextView text;
    private File file;

    private JSONObject clothAttribute = new JSONObject();
    private Spinner spinner_category, spinner_color, spinner_occasion;
    private CheckBox checkBox_spring, checkBox_summer, checkBox_fall, checkBox_winter, checkBox_all;
    private EditText clothName;

    private String message = EMPTY_STRING;
    private String clothesId = EMPTY_STRING;
    private String category= EMPTY_STRING;

    static CountingIdlingResource idlingResource = new CountingIdlingResource("send_add_clothes_data");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_clothes);

        user = MainActivity.getUser();

        image = findViewById(R.id.iv_add);
        image.setVisibility(View.INVISIBLE);
        imageButton = findViewById(R.id.btn_image_add);
        saveButton = findViewById(R.id.btn_save_add);
        text = findViewById(R.id.tv_add);

        imageButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        //spinners
        spinner_category = findViewById(R.id.sp_category_add);
        spinner_color = findViewById(R.id.sp_color_add);
        spinner_occasion = findViewById(R.id.sp_occasion_add);

        //optional editable tex box for ClothName input
        clothName = findViewById(R.id.et_name_add);

        //supply the spinners with the String array defined in resource using instances of ArrayAdapter
        spinnerAdapter.setAdapter(R.array.category_array, spinner_category,this);
        spinnerAdapter.setAdapter(R.array.color_array, spinner_color,this);
        spinnerAdapter.setAdapter(R.array.occasion_array, spinner_occasion,this);

        spinner_category.setOnItemSelectedListener(this);
        spinner_color.setOnItemSelectedListener(this);
        spinner_occasion.setOnItemSelectedListener(this);

        //checkboxes
        checkBox_spring = findViewById(R.id.cb_spring_add);
        checkBox_summer = findViewById(R.id.cb_summer_add);
        checkBox_fall = findViewById(R.id.cb_fall_add);
        checkBox_winter = findViewById(R.id.cb_winter_add);
        checkBox_all = findViewById(R.id.cb_all_add);
    }

    public void constructClothAttribute(JSONObject clothData,AdapterView<?> parent, View view, int pos,final String TAG, int resourceID_category,int resourceID_color,int resourceID_occasions) {
        Log.d(TAG,"VIEW: "+ view.getId());
        if(parent.getId() == resourceID_category){
            try {
                String selection = parent.getItemAtPosition(pos).toString();
                clothData.put("category", selection);
                Log.d(TAG, "category:"+selection);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if(parent.getId() == resourceID_color){
            try {
                String selection = parent.getItemAtPosition(pos).toString();
                clothData.put("color", selection);
                Log.d(TAG, "color:"+selection);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if(parent.getId() == resourceID_occasions){
            JSONArray occasions = new JSONArray();
            try {
                String selection = parent.getItemAtPosition(pos).toString();
                occasions.put(0,selection);
                clothData.put("occasions", occasions);
                Log.d(TAG, "occasions:"+occasions.get(0));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_image_add:
                idlingResource.increment();

                if (ContextCompat.checkSelfPermission(AddClothesActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddClothesActivity.this, new String[]
                            {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                }

                imageButton.setVisibility(View.GONE);
                text.setVisibility(View.GONE);
                Intent selectImageIntent = new Intent(Intent.ACTION_PICK);
                selectImageIntent.setType("image/jpeg");
                startActivityForResult(selectImageIntent, 1);

                idlingResource.decrement();
                break;

            case R.id.btn_save_add:
                constructClothAttributeFromCheckBoxes(clothAttribute,TAG,checkBox_spring,checkBox_summer,checkBox_fall,checkBox_winter,checkBox_all);
                constructClothAttributeClothName(clothAttribute,TAG,clothName);
//                send the cloth data to server
                sendClothDataToServer(clothAttribute,TAG,AddClothesActivity.this);

                final Toast toast = makeText(AddClothesActivity.this,"Successfully added clothes!",Toast.LENGTH_SHORT);
                toast.show();
                break;

            default:
        }
    }

    public void constructClothAttributeClothName(JSONObject clothData,String TAG,EditText clothname) {
        String name = clothname.getText().toString().trim();

        try {
            clothData.put("name", name);
            Log.d(TAG, "name: "+name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void constructClothAttributeFromCheckBoxes(JSONObject clothData,String TAG,CheckBox spring,CheckBox summer,CheckBox fall,CheckBox winter,CheckBox all) {

        JSONArray seasons = new JSONArray();
        if(spring.isChecked()){
            seasons.put("Spring");
        }
        if(summer.isChecked()){
            seasons.put("Summer");
        }
        if(fall.isChecked()){
            seasons.put("Fall");
        }
        if(winter.isChecked()){
            seasons.put("Winter");
        }
        if(all.isChecked()){
            seasons.put("All");
        }

        try {
            clothData.put("seasons", seasons);
            Log.d(TAG, "seasons:"+seasons);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

     public void sendClothDataToServer(final JSONObject userData, final String TAG, final Context context) {
        ServerCommAsync serverCommunication = new ServerCommAsync();
        final String data = userData.toString();
        Log.d(TAG,"prepared to sendClothDataToServer");
        Log.d(TAG,"data: "+data);

        serverCommunication.postWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/clothes/"+user.getUserId(), data,user.getUserToken(), new Callback() {
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (response.isSuccessful()) {
                    extractResponseClothesData(responseJson);
                    //make a toast to let the server's message display to the user
                    if(Objects.requireNonNull(responseJson).has("message") ){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(context,message,Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });
                        //startActivity(new Intent(getApplicationContext(),AddClothesActivity.class));
                    }
                } else {
                    // Request not successful
                    if(Objects.requireNonNull(responseJson).has("message") ){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(context,message,Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });
                        //startActivity(new Intent(getApplicationContext(),AddClothesActivity.class));
                    }
                }
            }
        });
    }

    private void extractResponseClothesData(JSONObject responseJson) {
        try {
            if (responseJson.has("message")) {
                message = responseJson.getString("message");
            }
            if (responseJson.has("id")) {
                clothesId = responseJson.getString("id");
            }
            if (responseJson.has("category")) {
                category = responseJson.getString("category");
            }

            sendImageToServer(file);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri;
        InputStream stream;
        Bitmap bitmap;
        String path = EMPTY_STRING;

        if (resultCode == RESULT_OK) {
            try {
                uri = data.getData();
                stream = getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(stream);
                image.setImageBitmap(bitmap);
                image.setVisibility(View.VISIBLE);
                path = getPath(uri);
                file = new File(path);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private String getPath(Uri uri) {
        ContentResolver resolver = getApplicationContext().getContentResolver();
        String path = EMPTY_STRING;

        if (resolver.getType(uri) == null) {
            path = uri.getPath();
        }
        else {
            String[] projection = {MediaStore.MediaColumns.DATA};
            Cursor cursor = resolver.query(uri, projection, null, null, null);
            int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            path = cursor.getString(index);
        }
        return path;
    }

    private void sendImageToServer(File file) {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("ClothingImage", file.getName(), RequestBody.create(file,MediaType.parse("image/*")))
                .build();
        Request request = new Request.Builder()
                .url("http://closet-cpen321.westus.cloudapp.azure.com/api/images/" + user.getUserId() + "/" + clothesId)
                .addHeader("Authorization","Bearer "+ user.getUserToken())
                .post(body)
                .build();
        Log.d(TAG,"prepared to sendImageToServer");

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseStr = Objects.requireNonNull(response.body()).string();
                if (response.isSuccessful()) {
                    Log.d(TAG, "Successfully upload image to server:"+responseStr);

                    Intent setImageIntent = new Intent();
                    setImageIntent.putExtra("clothesId", clothesId);
                    setImageIntent.putExtra("category", category);
                    setResult(RESULT_OK, setImageIntent);
                    finish();
                }
            }
        });

        Log.d(TAG,"finished sendImageToServer");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long l) {
        //construct the clothAttribute JSONObject we want to send to server
        constructClothAttribute(clothAttribute,parent,view,pos,TAG,R.id.sp_category_add,R.id.sp_color_add,R.id.sp_occasion_add);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        //Toast.makeText(parent.getContext(),"You must select one of the options",Toast.LENGTH_SHORT).show();
    }

    public static CountingIdlingResource getRegisterIdlingResourceInTest() {
        return idlingResource;
    }

}
