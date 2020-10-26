package com.example.frontend;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
    private Button calendarButton;
    private static final String TAG = "MainActivity";
    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Bundle data = getIntent().getExtras();
//        user = (User) data.getParcelable("user");
//        Log.d(TAG,"email: "+user.getEmail()+" userId: "+ user.getuserId()+ " userToken: "+ user.getUserToken());
//
//        //send firebase registration token to the server
//        MyFirebaseMessagingService firebaseMessagingService = new MyFirebaseMessagingService();
//        firebaseMessagingService.getTokenNSendToServer(user.getUserToken());


        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        calendarButton = findViewById(R.id.calendar_button);
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent calendarIntent = new Intent(MainActivity.this, CalendarActivity.class);
                startActivity(calendarIntent);
            }
        });
    }
    public User getUser(){
        return user;
    }
}