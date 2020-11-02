package com.example.frontend.ui.clothes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.frontend.AddClothesActivity;
import com.example.frontend.Clothes;
import com.example.frontend.MainActivity;
import com.example.frontend.R;
import com.example.frontend.User;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ClothesFragment extends Fragment implements View.OnClickListener {
    private static final String TAG ="ClothesFragment" ;
    private User user;
    private ClothesViewModel clothesViewModel;
    private ImageButton buttonAdd;
    private ImageView clothes1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        clothesViewModel =
                ViewModelProviders.of(this).get(ClothesViewModel.class);
        View root = inflater.inflate(R.layout.fragment_clothes, container, false);

        buttonAdd = root.findViewById(R.id.button_add);
        buttonAdd.setOnClickListener(this);
        clothes1 = root.findViewById(R.id.iv_clothes1);

        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_add:
                user = MainActivity.getUser();
                Intent myIntent = new Intent(ClothesFragment.this.getContext(), AddClothesActivity.class);
                myIntent.putExtra("user", user);
                Log.d(TAG,"send user to addClothActivity: ");
                Log.d(TAG,user.getEmail());
                Log.d(TAG,user.getuserId());
                Log.d(TAG,user.getUserToken());
                startActivityForResult(myIntent, 1);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            // here you can retrieve your bundle data.
            String path = data.getStringExtra("path");
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            clothes1.setImageBitmap(bitmap);
        }
    }

}