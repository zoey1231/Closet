package com.example.frontend;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend.ui.clothes.ClothesFragment;

import java.io.FileNotFoundException;
import java.io.InputStream;


public class AddClothesActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView image;
    private ImageButton buttonImage;
    private Button buttonSave;
    private TextView textAdd;
    private static final int IMAGE = 1;

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
                Intent intentImage = new Intent(Intent.ACTION_PICK);
                intentImage.setType("image/*");
                startActivityForResult(intentImage, IMAGE);
                break;
            case R.id.button_save_add:
                Intent intentSave = new Intent(AddClothesActivity.this, ClothesFragment.class);
                startActivity(intentSave);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                final Uri uri = data.getData();
                final InputStream stream = getContentResolver().openInputStream(uri);
                final Bitmap bitmap = BitmapFactory.decodeStream(stream);
                image.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(AddClothesActivity.this, "Something went wrong", Toast.LENGTH_LONG);
            }
        }
        else {
            Toast.makeText(AddClothesActivity.this, "You haven't picked image", Toast.LENGTH_LONG);
        }
    }
}
