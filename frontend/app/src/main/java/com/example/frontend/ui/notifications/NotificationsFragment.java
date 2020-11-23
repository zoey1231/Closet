package com.example.frontend.ui.notifications;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


import com.example.frontend.MainActivity;
import com.example.frontend.R;
import com.example.frontend.RegisterActivity;
import com.example.frontend.ServerCommAsync;
import com.example.frontend.User;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.widget.Toast.makeText;

public class NotificationsFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "NotificationFrag";
    private static final String EMPTY_STRING = "";
    private User user;
    private String message = EMPTY_STRING;
    private boolean hasUserProfile;
    private  TextView userEmail;
    private  TextView userName;
    private  TextView userCity;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        final TextView textView = root.findViewById(R.id.text_notifications);

        userEmail = root.findViewById(R.id.tv_userEmail);
        userName = root.findViewById(R.id.tv_userName);
        userCity = root.findViewById(R.id.tv_city);
        Button logOutBtn = root.findViewById(R.id.logOut_btn);
        logOutBtn.setOnClickListener(this);

        user = MainActivity.getUser();

        getUerProfile();
        //display user profile on fragment
        userEmail.setText("Email: "+user.getEmail());
        userName.setText("UserName: "+user.getName());
        userCity.setText("City: "+user.getCity());

        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.logOut_btn:
                Log.d(TAG, "Try to log out");
                //delete the userToken record
                user.setUserToken("");
                Intent intent = new Intent(NotificationsFragment.this.getActivity(), RegisterActivity.class);
                startActivity(intent);
                break;
                
            default:
        }
    }
    /*
    * Get User Profile
    * Find in the local User cache first, if not found, we fetch user profile information from the server
    * */
    private void getUerProfile(){
        if(user.getName() != null && !user.getName().trim().isEmpty()
                && user.getEmail() != null && !user.getEmail().trim().isEmpty()
                && user.getCity() != null && !user.getCity().trim().isEmpty()){
            Log.d(TAG,"has user profile information in local cache");
            return;
        }else{
            hasUserProfile = false;
            getUserProfileFromServer(TAG, getContext());
            while (!hasUserProfile){
                Log.d(TAG,"wait for user profile fetch from server");
            }
            return;
        }
    }

    private void getUserProfileFromServer(final String TAG, final Context context) {
        ServerCommAsync serverCommunication = new ServerCommAsync();

        Log.d(TAG,"prepared to getUserProfileFromServer");

        serverCommunication.getWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/users/me",user.getUserToken(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.d(TAG,"Fail to fetch user data from server");
                Log.d(TAG, String.valueOf(e));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                String responseStr = Objects.requireNonNull(response.body()).string();


                JSONObject responseJson = null;
                try {
                    responseJson = new JSONObject(responseStr);
                    if(responseJson.has("message")){
                        message = responseJson.getString("message");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (response.isSuccessful()) {
                    //make a toast to let the server's message display to the user
                    if(Objects.requireNonNull(responseJson).has("message") ){
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(context,message,Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });

                    }
                    else{

                        Log.d(TAG,"successfully get User Profile information From Server");
                        Log.d(TAG,responseStr);
                        try {
                            user.setCity(responseJson.getString("city"));
                            user.setName(responseJson.getString("name"));
                            user.setId(responseJson.getString("id"));
                            user.setEmail(responseJson.getString("email"));
                            Log.d(TAG,"updated user profile:"+user);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        hasUserProfile = true;


                    }
                    Log.d(TAG,"finished getUserProfileFromServer");
                } else {
                    // Request not successful
                    if(Objects.requireNonNull(responseJson).has("message") ){
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(context,message,Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });

                    }
                }
                Log.d(TAG,"finish to getUserProfileFromServer");
            }
        });
    }
}