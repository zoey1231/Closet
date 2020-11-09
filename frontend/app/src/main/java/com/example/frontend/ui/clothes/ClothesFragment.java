package com.example.frontend.ui.clothes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.test.espresso.idling.CountingIdlingResource;

import com.example.frontend.AddClothesActivity;
import com.example.frontend.MainActivity;
import com.example.frontend.R;
import com.example.frontend.EditClothesActivity;
import com.example.frontend.User;

import org.jetbrains.annotations.NotNull;

public class ClothesFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG ="ClothesFragment" ;
    private User user;
    private ImageButton buttonAdd;
    private ImageView clothes1, clothes2, clothes3;
    private Spinner spinner1, spinner2, spinner3;
    private int clickCount = 0; //for test

    static CountingIdlingResource idlingResource = new CountingIdlingResource("send_add_clothes_request");

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_clothes, container, false);

        buttonAdd = root.findViewById(R.id.btn_clothes_add);
        buttonAdd.setOnClickListener(this);
        clothes1 = root.findViewById(R.id.iv_clothes1);
        clothes2 = root.findViewById(R.id.iv_clothes2);
        clothes3 = root.findViewById(R.id.iv_clothes3);
        spinner1 = root.findViewById(R.id.sp_clothes1);
        setAdapter(R.array.edit_delete_array, spinner1);
        spinner1.setOnItemSelectedListener(this);
        spinner1.setVisibility(View.GONE);
        spinner2 = root.findViewById(R.id.sp_clothes2);
        setAdapter(R.array.edit_delete_array, spinner2);
        spinner2.setOnItemSelectedListener(this);
        spinner2.setVisibility(View.GONE);
        spinner3 = root.findViewById(R.id.sp_clothes3);
        setAdapter(R.array.edit_delete_array, spinner3);
        spinner3.setOnItemSelectedListener(this);
        spinner3.setVisibility(View.GONE);

        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_clothes_add:
                idlingResource.increment();

                user = MainActivity.getUser();
                Intent addClothesIntent = new Intent(ClothesFragment.this.getContext(), AddClothesActivity.class);
                addClothesIntent.putExtra("user", user);
                Log.d(TAG,"send user to addClothActivity: ");
                Log.d(TAG,user.getEmail());
                Log.d(TAG,user.getUserId());
                Log.d(TAG,user.getUserToken());

                startActivityForResult(addClothesIntent, 1);
                clickCount++;
                idlingResource.decrement();
                break;

            default:
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            // here you can retrieve your bundle data.
            String path = data.getStringExtra("path");
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            //for test
            if (clickCount == 1) {
                clothes1.setImageBitmap(bitmap);
                spinner1.setVisibility(View.VISIBLE);
            }
            else if (clickCount == 2) {
                clothes2.setImageBitmap(bitmap);
                spinner2.setVisibility(View.VISIBLE);
            }
            else {
                clothes3.setImageBitmap(bitmap);
                spinner3.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setAdapter(int textArrayResId, @NotNull Spinner spinner) {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ClothesFragment.this.getContext(),
                textArrayResId, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_clothes1:
                if (parent.getSelectedItem().toString().equals("Edit")) {
                    Log.d(TAG, "spinner is clicked");
                    Intent editClothesIntent = new Intent(ClothesFragment.this.getContext(), EditClothesActivity.class);
                    startActivity(editClothesIntent);
                }
                break;
            default:
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(TAG, "nothing is selected");
    }

    public static CountingIdlingResource getRegisterIdlingResourceInTest() {
        return idlingResource;
    }
}