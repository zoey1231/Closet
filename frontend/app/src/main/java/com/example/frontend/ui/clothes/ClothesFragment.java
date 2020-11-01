package com.example.frontend.ui.clothes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
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
import com.example.frontend.R;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ClothesFragment extends Fragment implements View.OnClickListener {

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

//        Uri uri = Uri.parse(getArguments().getString("uri"));
//        try {
//            InputStream stream = getActivity().getContentResolver().openInputStream(uri);
//            Bitmap bitmap = BitmapFactory.decodeStream(stream);
//            image.setImageBitmap(bitmap);
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

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
                Intent intent = new Intent(ClothesFragment.this.getActivity(), AddClothesActivity.class);
                startActivity(intent);
                break;
        }
    }
}