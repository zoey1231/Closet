package com.example.frontend;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
    private String oldPath, newPath;
    private File file;
    private ImageView image;
    private ImageButton imageButton;
    private Button saveButton;
    private TextView text;

    private String clothId = EMPTY_STRING;

    private Spinner spinner_category, spinner_color, spinner_occasion;
    private CheckBox checkBox_spring, checkBox_summer, checkBox_fall, checkBox_winter, checkBox_all;
    private EditText clothName;

    private AddClothesActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_clothes);

        image = findViewById(R.id.iv_edit);
        imageButton = findViewById(R.id.btn_image_edit);
        saveButton = findViewById(R.id.btn_save_edit);
        text = findViewById(R.id.tv_edit);

        Bundle data = getIntent().getExtras();
        user = data.getParcelable("user");
        oldPath = data.getString("path");
        Bitmap bitmap = BitmapFactory.decodeFile(oldPath);
        image.setImageBitmap(bitmap);

        imageButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        //spinners
        spinner_category = findViewById(R.id.sp_category_edit);
        spinner_color = findViewById(R.id.sp_color_edit);
        spinner_occasion = findViewById(R.id.sp_occasion_edit);

        //seasons checkBoxes
        checkBox_spring = findViewById(R.id.cb_spring_edit);
        checkBox_summer = findViewById(R.id.cb_summer_edit);
        checkBox_fall = findViewById(R.id.cb_fall_edit);
        checkBox_winter = findViewById(R.id.cb_winter_edit);
        checkBox_all = findViewById(R.id.cb_all_edit);

        //optional editable tex box for ClothName input
        clothName = findViewById(R.id.et_name_edit);

        //supply the spinners with the String array defined in resource using instances of ArrayAdapter
//        activity = new AddClothesActivity();
//        activity.setAdapter(R.array.category_array,spinner_category);
//        activity.setAdapter(R.array.color_array,spinner_color);
//        activity.setAdapter(R.array.occasion_array,spinner_occasion);
//
//        spinner_category.setOnItemSelectedListener(this);
//        spinner_color.setOnItemSelectedListener(this);
//        spinner_occasion.setOnItemSelectedListener(this);

        // show cloth's existing attributes as default values
        // TODO: get cloth's info, now cloth is NULL
//        String[] stringArray = getResources().getStringArray(R.array.category_array);
//        int index = Arrays.asList(stringArray).indexOf(clothes.getCategory());
//        spinner_category.setSelection(index);
//
//        stringArray = getResources().getStringArray(R.array.color_array);
//        index = Arrays.asList(stringArray).indexOf(clothes.getColor());
//        spinner_category.setSelection(index);
//
//        stringArray = getResources().getStringArray(R.array.occasion_array);
//        index = Arrays.asList(stringArray).indexOf(clothes.getOccasions().get(0));
//        spinner_category.setSelection(index);
//
//        et_clothName.setText(clothes.getName());
//
//        checkBox_spring.setChecked(clothes.getSeasons().contains("Spring")?true:false);
//        checkBox_summer.setChecked(clothes.getSeasons().contains("Summer")?true:false);
//        checkBox_fall.setChecked(clothes.getSeasons().contains("Fall")?true:false);
//        checkBox_winter.setChecked(clothes.getSeasons().contains("Winter")?true:false);
//        checkBox_all.setChecked(clothes.getSeasons().contains("All")?true:false);

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
                Intent intentEdit = new Intent(Intent.ACTION_PICK);
                intentEdit.setType("image/*");

                startActivityForResult(intentEdit, 1);
                break;

            case R.id.btn_save_edit:
//                while (clothId.equals(EMPTY_STRING)) {
//                    // change this
//                    Log.d(TAG, "waiting for clothing id");
//                }
//                sendImageToServer(file);

                Intent intentImage = new Intent();
                intentImage.putExtra("path", newPath);
                setResult(RESULT_OK, intentImage);
                finish();
                break;

            default:
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG,"something is selected");
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
                newPath = getPath(uri);
                file = new File(newPath);

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
                .url("http://closet-cpen321.westus.cloudapp.azure.com/api/images/" + user.getUserId() + "/" + clothId)
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
}