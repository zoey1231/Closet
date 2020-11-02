package com.example.frontend.ui.clothes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
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
    private String clothingId;
    private ClothesViewModel clothesViewModel;
    private ImageButton buttonAdd;
    private ImageView image;
    private GridLayout grid;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        clothesViewModel =
                ViewModelProviders.of(this).get(ClothesViewModel.class);
        View root = inflater.inflate(R.layout.fragment_clothes, container, false);

        buttonAdd = root.findViewById(R.id.button_add);
        image = root.findViewById(R.id.image_add);
        grid = root.findViewById(R.id.grid);
        buttonAdd.setOnClickListener(this);

//        clothingId = getArguments().getString("clothingId");
        Log.d(TAG, "clothingId: " + clothingId);

//        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
//        params.rowSpec = GridLayout.spec(0);
//        params.columnSpec = GridLayout.spec(0);
//        grid.addView(image, params);

        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_add:
                user = MainActivity.getUser();
                Intent intent = new Intent(ClothesFragment.this.getActivity(), AddClothesActivity.class);
                intent.putExtra("user", user);
                Log.d(TAG,"send user to addClothActivity: ");
                Log.d(TAG,user.getEmail());
                Log.d(TAG,user.getuserId());
                Log.d(TAG,user.getUserToken());
                startActivity(intent);
                break;
        }
    }

}