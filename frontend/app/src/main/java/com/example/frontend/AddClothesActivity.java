package com.example.frontend;

import android.Manifest;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.test.espresso.idling.CountingIdlingResource;

import org.jetbrains.annotations.NotNull;
import java.io.File;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
    private ImageView image;
    private ImageButton imageButton;
    private Button saveButton;
    private TextView text;
    private String path;
    private File file;

    private JSONObject clothAttribute = new JSONObject();
    private Spinner spinner_category, spinner_color, spinner_occasion;
    private CheckBox checkBox_spring, checkBox_summer, checkBox_fall, checkBox_winter, checkBox_all;
    private EditText clothName;

    private String message = EMPTY_STRING;
    private String clothesId = EMPTY_STRING;
    private String category= EMPTY_STRING;
    private String color= EMPTY_STRING;
    private String name= EMPTY_STRING;
    private String updated= EMPTY_STRING;
    private String clothUser = EMPTY_STRING;
    private ArrayList<String> seasons = new ArrayList<>();
    private ArrayList<String> occasions = new ArrayList<>();

    private HashMap<String, Clothes> clothHashMap =new HashMap<>();

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
        setAdapter(R.array.category_array, spinner_category);
        setAdapter(R.array.color_array, spinner_color);
        setAdapter(R.array.occasion_array, spinner_occasion);

        spinner_category.setOnItemSelectedListener(this);
        spinner_color.setOnItemSelectedListener(this);
        spinner_occasion.setOnItemSelectedListener(this);
    }

    private void constructClothAttribute(AdapterView<?> parent, View view, int pos) {
        Log.d(TAG,"VIEW: "+ view.getId());
        switch (parent.getId()) {
            case R.id.sp_category_add:
                try {
                    String selection = parent.getItemAtPosition(pos).toString();
                    clothAttribute.put("category", selection);
                    Log.d(TAG, "category:"+selection);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.sp_color_add:
                try {
                    String selection = parent.getItemAtPosition(pos).toString();
                    clothAttribute.put("color", selection);
                    Log.d(TAG, "color:"+selection);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.sp_occasion_add:

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

    public void setAdapter(int textArrayResId, @NotNull Spinner spinner) {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                textArrayResId, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
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
                selectImageIntent.setType("image/*");

                startActivityForResult(selectImageIntent, 1);
                idlingResource.decrement();
                break;

            case R.id.btn_save_add:
                constructClothAttributeFromCheckBoxes();
                constructClothAttributeClothName();
                //send the cloth data to server
//                sendClothDataToServer(clothAttribute);
//
//                while (clothesId.equals(EMPTY_STRING)) {
//                    // wait for clothing id; change this
//                    Log.d(TAG, "waiting for clothing id");
//                }
//                sendImageToServer(file);

                Intent setImageIntent = new Intent();
                setImageIntent.putExtra("path", path);
                setImageIntent.putExtra("clothesId", clothesId);
                setResult(RESULT_OK, setImageIntent);

                final Toast toast = makeText(AddClothesActivity.this,"Successfully added clothes!",Toast.LENGTH_SHORT);
                toast.show();
                finish();
                break;

            default:
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

    private void constructClothAttributeClothName() {
        String name = clothName.getText().toString().trim();

        try {
            clothAttribute.put("name", name);
            Log.d(TAG, "name: "+name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void constructClothAttributeFromCheckBoxes() {
        checkBox_spring = findViewById(R.id.cb_spring_add);
        checkBox_summer = findViewById(R.id.cb_summer_add);
        checkBox_fall = findViewById(R.id.cb_fall_add);
        checkBox_winter = findViewById(R.id.cb_winter_add);
        checkBox_all = findViewById(R.id.cb_all_add);

        //List<String> seasons = new ArrayList<String>();
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

    private void sendClothDataToServer(final JSONObject userData) {
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
                    extractResponseClothesData(responseJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (response.isSuccessful()) {
                    //make a toast to let the server's message display to the user

                    if(Objects.requireNonNull(responseJson).has("message") ){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(AddClothesActivity.this,message,Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });
                        //startActivity(new Intent(getApplicationContext(),AddClothesActivity.class));
                    }
                    else{
                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(AddClothesActivity.this,"Successfully added clothes!",Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });

                        //create a new cloth instance and add it the the clothes' collection
                        Clothes clothes = new Clothes(clothesId,category,color,name,updated, clothUser,seasons,occasions);
                        clothHashMap.put(clothesId, clothes);

                        //startActivity(new Intent(getApplicationContext(),MainActivity.class).putExtra("user",new User(userId,userToken,email)));
                    }

                } else {
                    // Request not successful
                    if(Objects.requireNonNull(responseJson).has("message") ){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(AddClothesActivity.this,message,Toast.LENGTH_LONG);
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
//        JSONArray seasons_jsonArray,occasions_jsonArray;
        try {
            if(responseJson.has("message"))
                message = responseJson.getString("message");
            // commented for codacy issue

//            if(responseJson.has("seasons")){
//                seasons_jsonArray = responseJson.getJSONArray("seasons");
//                for (int i=0;i<seasons_jsonArray.length();i++){
//                    seasons.add(seasons_jsonArray.getString(i));
//                }
//            }
//            if(responseJson.has("occasions")){
//                occasions_jsonArray = responseJson.getJSONArray("occasions");
//                for (int i=0;i<occasions_jsonArray.length();i++){
//                    occasions.add(occasions_jsonArray.getString(i));
//                }
//            }
//            if(responseJson.has("category"))
//                category = responseJson.getString("category");
//            if(responseJson.has("color"))
//                color = responseJson.getString("color");
//            if(responseJson.has("name"))
//                name = responseJson.getString("name");
//            if(responseJson.has("user"))
//                cloth_user = responseJson.getString("user");
//            if(responseJson.has("updated"))
//                updated = responseJson.getString("updated");
            if(responseJson.has("id"))
                clothesId = responseJson.getString("id");

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

        if (resultCode == RESULT_OK) {
            try {
                uri = data.getData();
                stream = getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(stream);
                image.setImageBitmap(bitmap);
                image.setVisibility(View.VISIBLE);
//                path = getPath(uri);
//                file = new File(path);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                final Toast toast = makeText(AddClothesActivity.this, "Something went wrong", Toast.LENGTH_LONG);
                toast.show();
            }
        } else {
            final Toast toast = makeText(AddClothesActivity.this, "You haven't picked an image", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(index);
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
                Log.d(TAG, "Successfully upload image to server:"+responseStr);

            }
        });

        Log.d(TAG,"finished sendImageToServer");
    }

    public static CountingIdlingResource getRegisterIdlingResourceInTest() {
        return idlingResource;
    }

}
