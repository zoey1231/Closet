package com.example.frontend;

import androidx.appcompat.app.AppCompatActivity;

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


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "RegisterActivity";
    private static final String EMPTY_STRING = "";

    private EditText eName, eEmail, ePassword;
    private Button btn_signup;
    private TextView linkToLogin;
    private ProgressBar progressBar_register;
    private String message = EMPTY_STRING;
    private String userId = EMPTY_STRING;
    private String userToken = EMPTY_STRING;
    private String email = EMPTY_STRING;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        eName = findViewById(R.id.etName);
        eEmail = findViewById(R.id.etEmail);
        ePassword = findViewById(R.id.etPassword);
        btn_signup = findViewById(R.id.btn_register);
        linkToLogin = findViewById(R.id.linkToLogin);
        progressBar_register = findViewById(R.id.progressBar_register);
        btn_signup.setOnClickListener(this);
        linkToLogin.setOnClickListener(this);


    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_register:
                Log.d(TAG,"clicked register button");
                progressBar_register.setVisibility(View.VISIBLE);
                register();
                break;
            case R.id.linkToLogin:
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                break;
        }
    }

    private void register() {

        String inputName = eName.getText().toString().trim();
        String inputEmail = eEmail.getText().toString().trim();
        String inputPassword = ePassword.getText().toString().trim();

        //ensure all input fields are not empty
        if(TextUtils.isEmpty(inputEmail) || TextUtils.isEmpty(inputName) || TextUtils.isEmpty(inputPassword)){
            makeText(RegisterActivity.this,"Please enter all the fields correctly",Toast.LENGTH_SHORT).show();
            return;
        }else {
            //send the input fields to the server
            JSONObject postUserData = new JSONObject();
            try {
                postUserData.put("name", inputName);
                postUserData.put("email", inputEmail);
                postUserData.put("password", inputPassword);

                Log.d(TAG,"prepared to send registration data to server");
                sendUserDataToServer(postUserData);

            } catch (JSONException e ) {
                e.printStackTrace();
            }
            return;
        }
    }

    private void sendUserDataToServer(final JSONObject userData) {
        ServerCommunicationAsync serverCommunication = new ServerCommunicationAsync();
        final String data = userData.toString();
        Log.d(TAG,"prepared to sendUserDataToServer");

        serverCommunication.post("http://closet-cpen321.westus.cloudapp.azure.com/api/users/signup", data, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d(TAG,"Fail to send request to server");
                Log.d(TAG, String.valueOf(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String inputEmail = EMPTY_STRING;
                String responseStr = response.body().string();
                //retrieve user data from server's response
                JSONObject responseJson = null;
                try {
                    responseJson = new JSONObject(responseStr);
                    inputEmail = userData.getString("email");
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
                                final Toast toast = makeText(RegisterActivity.this,"Failed to create the account",Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });

                    }
                    //make a toast to let the server's message display to the user
                    else if(responseJson.has("message") ){

                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(RegisterActivity.this,message,Toast.LENGTH_LONG);
                                toast.show();

                            }
                        });
                        startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
                    }
                    //start the app's main activity if register successfully
                    else{
                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(RegisterActivity.this,"User created successfully",Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                        Log.d(TAG,"email: "+email+" userId: "+ userId+ " userToken: "+ userToken);
                        startActivity(new Intent(getApplicationContext(),MainActivity.class).putExtra("user",new User(userId,userToken,email)));

                    }

                } else {
                    // Request not successful

                    if(responseJson.has("message") ){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = makeText(RegisterActivity.this,message,Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });
                        startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
                    }
                }
            }
        });
    }


}