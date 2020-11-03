package com.example.frontend;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;

public class EditClothesActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private ImageView image;
    private ImageButton buttonImage;
    private Button buttonSave;
    private TextView textUpdate;

    private Spinner spinner_category, spinner_color, spinner_occasion;
    private EditText et_clothName;

    private Clothes clothes;
    private AddClothesActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_clothes);

        image = findViewById(R.id.image_update);
        buttonImage = findViewById(R.id.button_image_update);
        buttonSave = findViewById(R.id.button_save_update);
        textUpdate = findViewById(R.id.text_update);

        //spinners
        spinner_category = findViewById(R.id.spinner_category);
        spinner_color = findViewById(R.id.spinner_color);
        spinner_occasion = findViewById(R.id.spinner_occasion);

        //optional editable tex box for ClothName input
        et_clothName = findViewById(R.id.et_clothName);

        //seasons checkBoxes
        CheckBox checkBox_spring = findViewById(R.id.checkBox_spring);
        CheckBox checkBox_summer = findViewById(R.id.checkBox_summer);
        CheckBox checkBox_fall = findViewById(R.id.checkBox_fall);
        CheckBox checkBox_winter = findViewById(R.id.checkBox_winter);
        CheckBox checkBox_all = findViewById(R.id.checkBox_all);

        //supply the spinners with the String array defined in resource using instances of ArrayAdapter
        activity = new AddClothesActivity();
//        activity.setAdapter(R.array.category_array,spinner_category);
//        activity.setAdapter(R.array.color_array,spinner_color);
//        activity.setAdapter(R.array.occasion_array,spinner_occasion);

//        spinner_category.setOnItemSelectedListener(this);
//        spinner_color.setOnItemSelectedListener(this);
//        spinner_occasion.setOnItemSelectedListener(this);

        //show cloth's existing attributes as default values
        //TODO: get cloth's info, now cloth is NULL
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
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}