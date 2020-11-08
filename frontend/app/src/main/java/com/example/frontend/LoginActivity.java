package com.example.frontend;

import androidx.appcompat.app.AppCompatActivity;
import androidx.test.espresso.idling.CountingIdlingResource;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.widget.Toast.makeText;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";
    private static final String EMPTY_STRING = "";
    private EditText eEmail, ePassword;
    private Button btn_login;

    private ProgressBar progressBar_login;
    private  TextView linkToRegister;

    public String userId = EMPTY_STRING;
    public String userToken = EMPTY_STRING;
    public String message = EMPTY_STRING;
    public String email = EMPTY_STRING;

    static CountingIdlingResource idlingResource_login = new CountingIdlingResource("send_login_data");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        eEmail = findViewById(R.id.etEmail_login);
        ePassword = findViewById(R.id.etPassword_login);
        btn_login = findViewById(R.id.btn_login);
        progressBar_login = findViewById(R.id.progressBar_login);
        linkToRegister = findViewById(R.id.linkToRegister);
        btn_login.setOnClickListener(this);
        linkToRegister.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                progressBar_login.setVisibility(View.VISIBLE);
                login();
                break;
            case R.id.linkToRegister:
                startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
                break;

            default:
        }
    }

    private void login() {
        idlingResource_login.increment();
        String inputEmail = eEmail.getText().toString().trim();
        String inputPassword = ePassword.getText().toString().trim();

        //ensure all input fields are not empty
        if (TextUtils.isEmpty(inputEmail) || TextUtils.isEmpty(inputPassword)) {
            makeText(LoginActivity.this, "Please enter all the fields correctly", Toast.LENGTH_SHORT).show();
        } else {
            //send the input fields to the server
            JSONObject postUserData = new JSONObject();
            try {
                postUserData.put("email", inputEmail);
                postUserData.put("password", inputPassword);

                Log.d(TAG, "prepared to send login data to server");
                sendUserDataToServer(postUserData);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendUserDataToServer(final JSONObject userData) {
        ServerCommunicationAsync serverCommunication = new ServerCommunicationAsync();
        final String data = userData.toString();
        Log.d(TAG,"prepared to sendUserDataToServer");

        serverCommunication.post("http://128.189.215.178:8080/api/users/login", data, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d(TAG,"Fail to send request to server");
                Log.d(TAG, String.valueOf(e));
                idlingResource_login.decrement();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String inputEmail = EMPTY_STRING;
                String responseStr = response.body().string();
                //retrieve user data from server's response
                JSONObject responseJson = null;
                try {
                    inputEmail = userData.getString("email");

                    responseJson = new JSONObject(responseStr);
                    if(responseJson.has("userId"))
                        userId = responseJson.getString("userId");
                    if(responseJson.has("token"))
                        userToken = responseJson.getString("token");
                    if(responseJson.has("email"))
                        email = responseJson.getString("email");
                    if(responseJson.has("message"))
                        message = responseJson.getString("message");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (response.isSuccessful()) {

                    if(!inputEmail.equalsIgnoreCase(email)){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(LoginActivity.this,"Failed to create the account",Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });

                    }
                    //make a toast to let the server's message display to the user
                    else if(responseJson.has("message") ){

                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(LoginActivity.this,message,Toast.LENGTH_LONG);
                                toast.show();

                            }
                        });
                        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                        idlingResource_login.decrement();
                    }
                    //start the app's main activity if register successfully
                    else{
                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(LoginActivity.this,"Login successfully",Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                        Log.d(TAG,"email: "+email+" userId: "+ userId+ " userToken: "+ userToken);
                        startActivity(new Intent(getApplicationContext(),MainActivity.class).putExtra("user",new User(userId,userToken,email)));
                        idlingResource_login.decrement();
                    }

                } else {
                    // Request not successful

                    if(responseJson.has("message") ){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(LoginActivity.this,message,Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });
                        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                        idlingResource_login.decrement();
                    }
                }
            }
        });
    }

    public static CountingIdlingResource getRegisterIdlingResourceInTest() {
        return idlingResource_login;
    }
}