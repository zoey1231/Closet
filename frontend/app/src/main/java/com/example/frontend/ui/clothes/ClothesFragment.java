package com.example.frontend.ui.clothes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.frontend.R;
import com.example.frontend.ui.dashboard.DashboardViewModel;

public class ClothesFragment extends Fragment implements View.OnClickListener {

    private ClothesViewModel clothesViewModel;
    ImageView iv_shoes,iv_outwears,iv_trousers,iv_shirts;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        clothesViewModel =
                ViewModelProviders.of(this).get(ClothesViewModel.class);
        View root = inflater.inflate(R.layout.fragment_clothes, container, false);

        iv_outwears = root.findViewById(R.id.iv_outwears);
        iv_trousers = root.findViewById(R.id.iv_trousers);
        iv_shirts = root.findViewById(R.id.iv_shirts);
        iv_shoes = root.findViewById(R.id.iv_shoes);
        iv_outwears.setOnClickListener(this);
        iv_shirts.setOnClickListener(this);
        iv_shoes.setOnClickListener(this);
        iv_trousers.setOnClickListener(this);

        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_outwears:
                break;
        }
    }
}