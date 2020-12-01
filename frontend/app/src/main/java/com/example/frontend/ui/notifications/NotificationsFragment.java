package com.example.frontend.ui.notifications;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
    private final int EDIT = 1;

    private User user;
    private String message = EMPTY_STRING;
    private boolean hasUserProfile;
    private  TextView userEmail;
    private  TextView userName;
    private  TextView userCity;
    private  Button editProfileBtn;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    private EditText et_userName;
    private EditText et_userEmail;
    private EditText et_city;
    private Button editProfile_save_tbn;
    private Button editProfile_cancel_btn;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        final TextView textView = root.findViewById(R.id.text_notifications);

        userEmail = root.findViewById(R.id.tv_userEmail);
        userName = root.findViewById(R.id.tv_userName);
        userCity = root.findViewById(R.id.tv_city);
        Button logOutBtn = root.findViewById(R.id.logOut_btn);
        logOutBtn.setOnClickListener(this);

        editProfileBtn = root.findViewById(R.id.editProfile_btn);
        editProfileBtn.setOnClickListener(this);

        user = MainActivity.getUser();

        getUerProfile();
        //display user profile on fragment
        userEmail.setText(user.getEmail());
        userName.setText(user.getName());
        userCity.setText(user.getCity());

        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.logOut_btn:
                Log.d(TAG, "clicked log out button");
                //delete the userToken record
                user.setUserToken("");
                Intent intent = new Intent(NotificationsFragment.this.getActivity(), RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.editProfile_btn:
                Log.d(TAG, "clicked edit profile button");
//               //jump to EditUserProfileActivity
//                Intent i = new Intent(NotificationsFragment.this.getActivity(), EditUserProfileActivity.class);
//                i.putExtra("user",user);
//                Log.d(TAG,"Send data to EditUserProfileActivity: "+"userName "+user.getName()+" Email "+user.getEmail()+" City "+user.getCity());
//                startActivityForResult(i, EDIT);
                createUpdateProfileDialog();
                break;
            case R.id.editProfile_save_tbn:
                updateProfile();
                break;
            case R.id.editProfile_cancel_btn:
                dialog.dismiss();
                break;
            default:
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == EDIT) {
            // here you can retrieve your bundle data.
            user = data.getExtras().getParcelable("user");
            getUerProfile();

            //update user profile on fragment
            userEmail.setText(user.getEmail());
            userName.setText(user.getName());
            userCity.setText(user.getCity());

        }

    }
    public void createUpdateProfileDialog(){
        dialogBuilder = new AlertDialog.Builder(getContext());
        final View updateProfileView = getLayoutInflater().inflate(R.layout.activity_edit_profile,null);
        et_userName = updateProfileView.findViewById(R.id.et_userName);
        et_userEmail = updateProfileView.findViewById(R.id.et_userEmail);
        et_city = updateProfileView.findViewById(R.id.et_city);
        editProfile_save_tbn = updateProfileView.findViewById(R.id.editProfile_save_tbn);
        editProfile_cancel_btn = updateProfileView.findViewById(R.id.editProfile_cancel_btn);

        editProfile_save_tbn.setOnClickListener(this);
        editProfile_cancel_btn.setOnClickListener(this);

        et_userName.setText(user.getName());
        et_userEmail.setText(user.getEmail());
        et_city.setText(user.getCity());

        dialogBuilder.setView(updateProfileView);
        dialog = dialogBuilder.create();
        dialog.show();

    }

    /*
    * Get User Profile
    * Find in the local User cache first, if not found, we fetch user profile information from the server
    * */
     public void getUerProfile(){
        if(user.getName() != null && !user.getName().trim().isEmpty()
                && user.getEmail() != null && !user.getEmail().trim().isEmpty()
                && user.getCity() != null && !user.getCity().trim().isEmpty()){
            Log.d(TAG,"has user profile information in local cache");

        }else{
            hasUserProfile = false;
            getUserProfileFromServer(TAG, getContext());
            while (!hasUserProfile){
                Log.d(TAG,"wait for user profile fetch from server");
            }
        }
        return;
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
                Log.d(TAG,responseStr);

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
                Log.d(TAG,"finish getUserProfileFromServer");
            }
        });
    }

    private void updateProfile() {
        String inputUserName = et_userName.getText().toString().trim();
        String inputUserEmail = et_userEmail.getText().toString().trim();
        String inputUserCity = et_city.getText().toString().trim();

        //ensure all input fields are filled
        if (TextUtils.isEmpty(inputUserName) || TextUtils.isEmpty(inputUserEmail)|| TextUtils.isEmpty(inputUserCity)) {
            makeText(getContext(),"Please enter all fields to update profile",Toast.LENGTH_SHORT).show();
        } else {
            //send the input fields to the server
            JSONObject putUserData = new JSONObject();
            try {
                putUserData.put("name", inputUserName);
                putUserData.put("email", inputUserEmail);
                putUserData.put("city", inputUserCity);

                putUserDataToServer(putUserData);
                Log.d(TAG,"user data: "+putUserData);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private void putUserDataToServer(JSONObject userData) {
        ServerCommAsync serverCommunication = new ServerCommAsync();
        final String data = userData.toString();
        Log.d(TAG,"prepared to putUserDataToServer");
        Log.d(TAG,user.getUserToken());

        serverCommunication.putWithAuthentication("http://closet-cpen321.westus.cloudapp.azure.com/api/users/me", data, user.getUserToken(),new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d(TAG,"Fail to send request to server");
                Log.d(TAG, String.valueOf(e));
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseStr = response.body().string();
                String updatedCity = EMPTY_STRING;
                String updatedName = EMPTY_STRING;
                String updatedEmail = EMPTY_STRING;
                JSONObject updatedUser = null;
                Log.d(TAG,"responseStr"+responseStr);
                //retrieve user data from server's response
                JSONObject responseJson = null;

                try {

                    responseJson = new JSONObject(responseStr);
                    if(responseJson.has("message"))
                        message = responseJson.getString("message");
                    if(responseJson.has("updatedUser")){
                        updatedUser = responseJson.getJSONObject("updatedUser");
                        updatedCity = updatedUser.getString("city");
                        updatedEmail = updatedUser.getString("email");
                        updatedName = updatedUser.getString("name");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (response.isSuccessful()) {

                    //if the update is unsuccessful, display the reason sent back from server to the user and start the edit profile activity again
                    if(responseJson.has("message") && !message.equals("Update profile successfully")){
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(getContext(),message,Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });

                    }
                    //if update profile is successful
                    else if(responseJson.has("message") && message.equals("Update profile successfully")){
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(getContext(),message,Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });

                        //update user's profile information in the local cache
                        user.setEmail(updatedEmail);
                        user.setName(updatedName);
                        user.setCity(updatedCity);

                        Log.d(TAG,"updatedEmail: "+updatedEmail+" updatedName: "+ updatedName+ " updatedCity: "+ updatedCity);

                        //update user profile on fragment
                        userEmail.setText(user.getEmail());
                        userName.setText(user.getName());
                        userCity.setText(user.getCity());

                        //close the dialog
                        dialog.dismiss();
                    }

                } else {
                    // Request not successful, start the edit profile activity again
                    if(responseJson.has("message") ){
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(getContext(),message,Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });


                    }
                }
                Log.d(TAG,"finish putUserDataToServer");
            }
        });
    }

}