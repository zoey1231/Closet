package com.example.frontend;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.frontend.ui.clothes.spinnerAdapter;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.widget.Toast.makeText;

public class EditClothesActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "EditClothesActivity";
    private static final String EMPTY_STRING = "";

    private User user;
    private String path;
    private String clothesId = EMPTY_STRING;
    private File file;
    private ImageView image;
    private ImageButton imageButton;
    private Button saveButton;
    private TextView text;

    private Spinner spinner_category, spinner_color, spinner_occasion;
    private CheckBox checkBox_spring, checkBox_summer, checkBox_fall, checkBox_winter, checkBox_all;
    private EditText clothName;

    private Clothes cloth;
    private String message = EMPTY_STRING;
    private JSONObject clothAttribute = new JSONObject();
    private boolean hasUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_clothes);
        user = MainActivity.getUser();

        image = findViewById(R.id.iv_edit);
        imageButton = findViewById(R.id.btn_image_edit);
        saveButton = findViewById(R.id.btn_save_edit);
        text = findViewById(R.id.tv_edit);

        Bundle data = getIntent().getExtras();
        String userId = user.getUserId();
        clothesId = data.getString("clothesId");
        Bitmap bitmap = getClothesImage(userId, clothesId);
        image.setImageBitmap(bitmap);

        imageButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        //spinners
        spinner_category = findViewById(R.id.sp_category_edit);
        spinner_color = findViewById(R.id.sp_color_edit);
        spinner_occasion = findViewById(R.id.sp_occasion_edit);
        spinner_category.setOnItemSelectedListener(this);
        spinner_color.setOnItemSelectedListener(this);
        spinner_occasion.setOnItemSelectedListener(this);

        //setup spinners
        spinnerAdapter.setAdapter(R.array.category_array,spinner_category,this);
        spinnerAdapter.setAdapter(R.array.color_array,spinner_color,this);
        spinnerAdapter.setAdapter(R.array.occasion_array,spinner_occasion,this);

        //seasons checkBoxes
        checkBox_spring = findViewById(R.id.cb_spring_edit);
        checkBox_summer = findViewById(R.id.cb_summer_edit);
        checkBox_fall = findViewById(R.id.cb_fall_edit);
        checkBox_winter = findViewById(R.id.cb_winter_edit);
        checkBox_all = findViewById(R.id.cb_all_edit);

        //optional editable tex box for ClothName input
        clothName = findViewById(R.id.et_name_edit);

        getClothFromServer(clothesId);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_image_edit:
                if (ContextCompat.checkSelfPermission(EditClothesActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditClothesActivity.this, new String[]
                            {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                }

                imageButton.setVisibility(View.GONE);
                text.setVisibility(View.GONE);
                Intent selectImageIntent = new Intent(Intent.ACTION_PICK);
                selectImageIntent.setType("image/*");

                startActivityForResult(selectImageIntent, 1);
                break;

            case R.id.btn_save_edit:

                constructClothAttributeFromCheckBoxes(clothAttribute,TAG,checkBox_spring,checkBox_summer,checkBox_fall,checkBox_winter,checkBox_all);
                constructClothAttributeClothName(clothAttribute,TAG,clothName);
                updateClothDataToServer(clothAttribute,TAG,EditClothesActivity.this);


                while (!hasUpdate) {
                    // wait for clothing id; change this
                    Log.d(TAG, "waiting for update cloth");
                }
                sendImageToServer(file);
                Intent setImageIntent = new Intent();
                setImageIntent.putExtra("path", path);
                setImageIntent.putExtra("clothesId", clothesId);
                setResult(RESULT_OK, setImageIntent);

                finish();
                break;

            default:
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long l) {
        Log.d(TAG,"something is selected");
       constructClothAttribute(clothAttribute,parent,view,pos,TAG,R.id.sp_category_edit,R.id.sp_color_edit,R.id.sp_occasion_edit);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.d(TAG, "nothing is selected");
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
                path = getPath(uri);
                file = new File(path);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                final Toast toast = makeText(EditClothesActivity.this, "Something went wrong", Toast.LENGTH_LONG);
                toast.show();
            }
        } else {
            final Toast toast = makeText(EditClothesActivity.this, "You haven't picked an image", Toast.LENGTH_LONG);
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
                .addFormDataPart("ClothingImage", file.getName(), RequestBody.create(file, MediaType.parse("image/*")))
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
                Log.d(TAG, "Successfully upload image to server:" + responseStr);

            }
        });

        Log.d(TAG,"finished sendImageToServer");
    }
    private void getClothFromServer(final String clothesId) {
        ServerCommAsync serverCommunication = new ServerCommAsync();

        Log.d(TAG,"prepared to getClothDataToServer");

        serverCommunication.getWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/clothes/"+user.getUserId()+"/"+clothesId,user.getUserToken(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.d(TAG,"Fail to send request to server");
                Log.d(TAG, String.valueOf(e));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                String responseStr = Objects.requireNonNull(response.body()).string();
                Log.d(TAG,"Get cloth data:"+responseStr);

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
                                final Toast toast = makeText(EditClothesActivity.this,message,Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });
                    }else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                setClothDataOnUI();
                            }
                        });

                    }

                } else {
                    // Request not successful
                    if(Objects.requireNonNull(responseJson).has("message") ){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(EditClothesActivity.this,message,Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });

                    }
                }
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

    private void extractResponseClothesData(JSONObject responseJSON) throws JSONException {
        if(responseJSON.has("message"))
            message = responseJSON.getString("message");
        else{
            JSONArray seasonsArray = responseJSON.getJSONArray("seasons");
            JSONArray occasionsArray = responseJSON.getJSONArray("occasions");
            String category = responseJSON.getString("category");
            String color = responseJSON.getString("color");
            String name = responseJSON.getString("name");
            String updated = responseJSON.getString("updated");
            String user = responseJSON.getString("user");
            ArrayList<String> seasons = new ArrayList<>();
            ArrayList<String> occasions = new ArrayList<>();

            for (int i=0;i<seasonsArray.length();i++){
                seasons.add(seasonsArray.getString(i));
            }
            for (int i=0;i<occasionsArray.length();i++){
                occasions.add(occasionsArray.getString(i));
            }

            cloth = new Clothes(clothesId,category,color,name,updated,user,seasons,occasions);
        }

    }
    private void setClothDataOnUI(){
        Log.d(TAG,"cloth category:"+cloth.getCategory());
        Log.d(TAG,"cloth color:"+cloth.getColor());
        Log.d(TAG,"cloth Occasions:"+cloth.getOccasions());

        // show cloth's existing attributes as default values
        String[] stringArray = getResources().getStringArray(R.array.category_array);
        int index_category = Arrays.asList(stringArray).indexOf(cloth.getCategory());
        spinner_category.setSelection(index_category);

        stringArray = getResources().getStringArray(R.array.color_array);
        int index_color = Arrays.asList(stringArray).indexOf(cloth.getColor());
        spinner_color.setSelection(index_color);

        stringArray = getResources().getStringArray(R.array.occasion_array);
        int index_occasion = Arrays.asList(stringArray).indexOf(cloth.getOccasions().get(0));
        spinner_occasion.setSelection(index_occasion);

        clothName.setText(cloth.getName());

        checkBox_spring.setChecked(cloth.getSeasons().contains("Spring"));
        checkBox_summer.setChecked(cloth.getSeasons().contains("Summer"));
        checkBox_fall.setChecked(cloth.getSeasons().contains("Fall"));
        checkBox_winter.setChecked(cloth.getSeasons().contains("Winter"));
        checkBox_all.setChecked(cloth.getSeasons().contains("All"));
    }

    public void updateClothDataToServer(final JSONObject userData, final String TAG, final Context context) {
        ServerCommAsync serverCommunication = new ServerCommAsync();
        final String data = userData.toString();
        Log.d(TAG,"prepared to UpdateClothDataToServer");
        Log.d(TAG,"data: "+data);

        serverCommunication.putWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/clothes/"+user.getUserId()+"/"+clothesId, data,user.getUserToken(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.d(TAG,"Fail to update cloth data to server");
                Log.d(TAG, String.valueOf(e));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                String responseStr = Objects.requireNonNull(response.body()).string();
                //Log.d(TAG,responseStr);

                JSONObject responseJson = null;
                try {
                    responseJson = new JSONObject(responseStr);
                    extractResponseData(responseJson);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (response.isSuccessful()) {
                    //make a toast to let the server's message display to the user
                    if(Objects.requireNonNull(responseJson).has("message") ){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(context,message,Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });

                    }
                    else{
                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(context,"Successfully update cloth!",Toast.LENGTH_SHORT);
                                toast.show();

                            }
                        });
                        Log.d(TAG,"updated cloth information:"+cloth);
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
                    }
                }
                hasUpdate = true;
                Log.d(TAG,"finished updateClothDataToServer");
            }
        });
    }
    private void extractResponseData(JSONObject responseJson) {
        JSONArray seasons_jsonArray,occasions_jsonArray;
        ArrayList<String> seasons = new ArrayList<>();
        ArrayList<String> occasions = new ArrayList<>();
        try {
            if(responseJson.has("message"))
                message = responseJson.getString("message");

            if(responseJson.has("seasons")){
                seasons_jsonArray = responseJson.getJSONArray("seasons");
                for (int i=0;i<seasons_jsonArray.length();i++){
                    seasons.add(seasons_jsonArray.getString(i));
                }
                cloth.setSeasons(seasons);
            }
            if(responseJson.has("occasions")){
                occasions_jsonArray = responseJson.getJSONArray("occasions");
                for (int i=0;i<occasions_jsonArray.length();i++){
                    occasions.add(occasions_jsonArray.getString(i));
                }
                cloth.setSeasons(occasions);
            }
            if(responseJson.has("category"))
                cloth.setCategory(responseJson.getString("category"));
            if(responseJson.has("color"))
                cloth.setColor(responseJson.getString("color"));
            if(responseJson.has("name"))
                cloth.setName(responseJson.getString("name"));
            if(responseJson.has("updated"))
                cloth.setUpdated(responseJson.getString("updated"));

        } catch (JSONException e) {
            e.printStackTrace();
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
}