package com.example.frontend;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaSync;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.frontend.ui.clothes.ClothesFragment;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class AddClothesActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AppClothesActivity";

    private ImageView image;
    private ImageButton buttonImage;
    private Button buttonSave;
    private TextView textAdd;

    private static final int ADD = 1;

    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_clothes);

        image = findViewById(R.id.image_add);
        buttonImage = findViewById(R.id.button_image_add);
        buttonSave = findViewById(R.id.button_save_add);
        textAdd = findViewById(R.id.text_add);

        buttonImage.setOnClickListener(this);
        buttonSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_image_add:
                buttonImage.setVisibility(View.GONE);
                textAdd.setVisibility(View.GONE);
                Intent intentAdd = new Intent(Intent.ACTION_PICK);
                intentAdd.setType("image/*");
                startActivityForResult(intentAdd, ADD);
                break;

            case R.id.button_save_add:
//                FragmentManager manager = getSupportFragmentManager();
//                FragmentTransaction transaction = manager.beginTransaction();
//                ClothesFragment fragment = new ClothesFragment();
////                fragment.setArguments(bundle);
//                transaction.replace(R.id.nav_host_fragment_container, fragment);
//                transaction.commit();
                break;
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
//                    bundle.putString("uri", String.valueOf(uri));
                    String path = getPath(uri);
                    File file = new File(path);
                    sendImageToServer(file);

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
                .addFormDataPart("image_url", file.getName(), RequestBody.create(MediaType.parse("image/*"), file))
                .build();
        Request request = new Request.Builder()
                .url("http://closet-cpen321.westus.cloudapp.azure.com/api/clothes")
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

            }
        });

        Log.d(TAG,"finished sendImageToServer");
    }

}
