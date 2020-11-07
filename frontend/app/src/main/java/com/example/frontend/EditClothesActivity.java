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

public class EditClothesActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    // commented for codacy issue

//    private ImageView image;
//    private ImageButton buttonImage;
//    private Button buttonSave;
//    private TextView textUpdate;
//
//    private Spinner spinner_category, spinner_color, spinner_occasion;
//    private CheckBox checkBox_spring, checkBox_summer, checkBox_fall, checkBox_winter, checkBox_all;
//    private EditText clothName;
//
//    private Clothes clothes;
//    private AddClothesActivity activity;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_edit_clothes);
//
//        image = findViewById(R.id.iv_edit);
//        buttonImage = findViewById(R.id.btn_image_edit);
//        buttonSave = findViewById(R.id.btn_save_edit);
//        textUpdate = findViewById(R.id.tv_edit);
//
//        //spinners
//        spinner_category = findViewById(R.id.sp_category_edit);
//        spinner_color = findViewById(R.id.sp_color_edit);
//        spinner_occasion = findViewById(R.id.sp_occasion_edit);
//
//        //seasons checkBoxes
//        checkBox_spring = findViewById(R.id.cb_spring_edit);
//        checkBox_summer = findViewById(R.id.cb_summer_edit);
//        checkBox_fall = findViewById(R.id.cb_fall_edit);
//        checkBox_winter = findViewById(R.id.cb_winter_edit);
//        checkBox_all = findViewById(R.id.cb_all_edit);
//
//        //optional editable tex box for ClothName input
//        clothName = findViewById(R.id.et_name_edit);
//
//        //supply the spinners with the String array defined in resource using instances of ArrayAdapter
//        activity = new AddClothesActivity();
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

//    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}