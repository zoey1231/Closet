package com.example.frontend.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.frontend.MainActivity;
import com.example.frontend.R;

public class NotificationsFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "NotificationFrag";
    private NotificationsViewModel notificationsViewModel;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        final TextView textView = root.findViewById(R.id.text_notifications);
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        TextView userEmail = root.findViewById(R.id.tv_userEmail);
        TextView userId = root.findViewById(R.id.tv_userId);
        Button logOutBtn = root.findViewById(R.id.logOut_btn);
        logOutBtn.setOnClickListener(this);

        //get Uer's data from Mainactivity and display them on fragment
        MainActivity activity = (MainActivity) getActivity();
        userEmail.setText("Email: "+activity.getUser().getEmail());
        userId.setText("UserId: "+activity.getUser().getuserId());
        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.logOut_btn:
                Log.d(TAG, "Try to log out");

                break;
        }
    }
}