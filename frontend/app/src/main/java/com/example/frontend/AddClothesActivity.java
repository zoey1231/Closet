package com.example.frontend;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaSync;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.frontend.ui.clothes.ClothesFragment;

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
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


import static android.widget.Toast.makeText;
import static com.google.common.io.Files.copy;


public class AddClothesActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "AddClothesActivity";
    private static final String EMPTY_STRING = "";
    private User user;

    private ImageView image;
    private ImageButton buttonImage;
    private Button buttonSave;
    private TextView textAdd;

    private static final int ADD = 1;

    private static final int IMAGE = 1;
    private JSONObject clothAttribute = new JSONObject();
    private Spinner spinner_category, spinner_color, spinner_occasion;
    private EditText et_clothName;

    private String message = EMPTY_STRING;
    private String cloth_id = EMPTY_STRING;
    private String category= EMPTY_STRING;
    private String color= EMPTY_STRING;
    private String name= EMPTY_STRING;
    private String updated= EMPTY_STRING;
    private String cloth_user= EMPTY_STRING;
    private ArrayList<String> seasons = new ArrayList<>();
    private ArrayList<String> occasions = new ArrayList<>();

    private java.util.HashMap<String,Cloth> clothHashMap =new HashMap<String,Cloth>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_clothes);

        Bundle data = getIntent().getExtras();
        user = (User) data.getParcelable("user");
        Log.d(TAG,"get user at addClothActivity: ");
        Log.d(TAG,user.getEmail());
        Log.d(TAG,user.getuserId());
        Log.d(TAG,user.getUserToken());

        image = findViewById(R.id.image_add);
        buttonImage = findViewById(R.id.button_image_add);
        buttonSave = findViewById(R.id.button_save_add);
        textAdd = findViewById(R.id.text_add);

        buttonImage.setOnClickListener(this);
        buttonSave.setOnClickListener(this);

        //spinners
        spinner_category = findViewById(R.id.spinner_category);
        spinner_color = findViewById(R.id.spinner_color);
        spinner_occasion = findViewById(R.id.spinner_occasion);

        //optional editable tex box for ClothName input
        et_clothName = findViewById(R.id.et_clothName);

        //supply the spinners with the String array defined in resource using instances of ArrayAdapter
        setAdapter(R.array.category_array,spinner_category);
        setAdapter(R.array.color_array,spinner_color);
        setAdapter(R.array.occasion_array,spinner_occasion);

        spinner_category.setOnItemSelectedListener(this);
        spinner_color.setOnItemSelectedListener(this);
        spinner_occasion.setOnItemSelectedListener(this);
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long l) {
        // retrieve the selected item using parent.getItemAtPosition(pos)
        String text = parent.getItemAtPosition(pos).toString();
        Toast.makeText(parent.getContext(),text,Toast.LENGTH_SHORT).show();

        //construct the clothAttribute JSONObject we want to send to server
        constructClothAttribute(parent,view,pos);
    }

    private void constructClothAttribute(AdapterView<?> parent, View view, int pos) {
        Log.d(TAG,"VIEW: "+ view.getId());
        switch (parent.getId()) {
            case R.id.spinner_category:
                try {
                    String selection = parent.getItemAtPosition(pos).toString();
                    clothAttribute.put("category", selection);
                    Log.d(TAG, "category:"+selection);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.spinner_color:
                try {
                    String selection = parent.getItemAtPosition(pos).toString();
                    clothAttribute.put("color", selection);
                    Log.d(TAG, "color:"+selection);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.spinner_occasion:

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
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        //Toast.makeText(parent.getContext(),"You must select one of the options",Toast.LENGTH_SHORT).show();
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
            case R.id.button_image_add:
                if (ContextCompat.checkSelfPermission(AddClothesActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddClothesActivity.this, new String[]
                            {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                }

                buttonImage.setVisibility(View.GONE);
                textAdd.setVisibility(View.GONE);
                Intent intentAdd = new Intent(Intent.ACTION_PICK);
                intentAdd.setType("image/*");
                startActivityForResult(intentAdd, ADD);

                break;

            case R.id.button_save_add:
                constructClothAttributeFromCheckBoxes();
                constructClothAttribute_clothName();
                //send the cloth data to server
                sendClothDataToServer(clothAttribute);

//                FragmentManager manager = getSupportFragmentManager();
//                FragmentTransaction transaction = manager.beginTransaction();
//                ClothesFragment fragment = new ClothesFragment();
////                fragment.setArguments(bundle);
//                transaction.replace(R.id.nav_host_fragment_container, fragment);
//                transaction.commit();

                break;
        }
    }

    private void constructClothAttribute_clothName() {
        String clothName = et_clothName.getText().toString().trim();

        try {
            clothAttribute.put("name", clothName);
            Log.d(TAG, "name: "+clothName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void constructClothAttributeFromCheckBoxes() {
        CheckBox checkBox_spring = findViewById(R.id.checkBox_spring);
        CheckBox checkBox_summer = findViewById(R.id.checkBox_summer);
        CheckBox checkBox_fall = findViewById(R.id.checkBox_fall);
        CheckBox checkBox_winter = findViewById(R.id.checkBox_winter);
        CheckBox checkBox_all = findViewById(R.id.checkBox_all);

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
        ServerCommunicationAsync serverCommunication = new ServerCommunicationAsync();
        final String data = userData.toString();
        Log.d(TAG,"prepared to sendClothDataToServer");
        Log.d(TAG,"data: "+data);

        serverCommunication.postWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/clothes/"+user.getuserId(), data,user.getUserToken(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d(TAG,"Fail to send request to server");
                Log.d(TAG, String.valueOf(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseStr = response.body().string();
                Log.d(TAG,responseStr);

                JSONObject responseJson = null;
                try {
                    responseJson = new JSONObject(responseStr);
                    extractResponseData(responseJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (response.isSuccessful()) {
                    //make a toast to let the server's message display to the user
                    if(responseJson.has("message") ){
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
                                final Toast toast = makeText(AddClothesActivity.this,"Successfully add a cloth!",Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });

                        //create a new cloth instance and add it the the clothes' collection
                        Cloth cloth = new Cloth(cloth_id,category,color,name,updated,cloth_user,seasons,occasions);
                        clothHashMap.put(cloth_id,cloth);

                        //startActivity(new Intent(getApplicationContext(),MainActivity.class).putExtra("user",new User(userId,userToken,email)));
                    }

                } else {
                    // Request not successful
                    if(responseJson.has("message") ){
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

    private void extractResponseData(JSONObject responseJson) {
        JSONArray seasons_jsonArray,occasions_jsonArray;
        try {
            if(responseJson.has("message"))
                message = responseJson.getString("message");
            if(responseJson.has("seasons")){
                seasons_jsonArray = responseJson.getJSONArray("seasons");
                for (int i=0;i<seasons_jsonArray.length();i++){
                    seasons.add(seasons_jsonArray.getString(i));
                }
            }
            if(responseJson.has("occasions")){
                occasions_jsonArray = responseJson.getJSONArray("occasions");
                for (int i=0;i<occasions_jsonArray.length();i++){
                    occasions.add(occasions_jsonArray.getString(i));
                }
            }
            if(responseJson.has("category"))
                category = responseJson.getString("category");
            if(responseJson.has("color"))
                color = responseJson.getString("color");
            if(responseJson.has("name"))
                name = responseJson.getString("name");
            if(responseJson.has("user"))
                cloth_user = responseJson.getString("user");
            if(responseJson.has("updated"))
                updated = responseJson.getString("updated");
            if(responseJson.has("id"))
                cloth_id = responseJson.getString("id");

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD) {
            if (resultCode == RESULT_OK) {
                try {
                    final Uri uri = data.getData();
                    final InputStream stream = getContentResolver().openInputStream(uri);
                    final Bitmap bitmap = BitmapFactory.decodeStream(stream);
                    image.setImageBitmap(bitmap);
                    String path = getPath(uri);
                    File file = new File(path);
                    sendImageToServer(file);

                    Bundle bundle = new Bundle();
                    bundle.putString("userToken", user.getUserToken());
                    bundle.putString("clothingId", cloth_id);
                    ClothesFragment clothesFragment = new ClothesFragment();
                    clothesFragment.setArguments(bundle);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(AddClothesActivity.this, "Something went wrong", Toast.LENGTH_LONG);
                }
            } else {
                Toast.makeText(AddClothesActivity.this, "You haven't picked image", Toast.LENGTH_LONG);
            }
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
                .addFormDataPart("ClothingImage", file.getName(), RequestBody.create(MediaType.parse("image/*"), file))
                .build();
        Request request = new Request.Builder()
                .url("http://closet-cpen321.westus.cloudapp.azure.com/api/images/" + user.getuserId() + "/" + cloth_id)
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
                String responseStr = response.body().string();
                Log.d(TAG, responseStr);
            }
        });

        Log.d(TAG,"finished sendImageToServer");
    }


}
