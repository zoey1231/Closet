package com.example.frontend;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class GetAuthActivity extends AppCompatActivity implements View.OnClickListener {

    private Button authButton;
    private Button backButton;
    private EditText codeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_auth);

        authButton = findViewById(R.id.btn_auth);
        authButton.setOnClickListener(this);
        backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(this);
        codeText = findViewById(R.id.et_auth);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_auth:
                Uri uri = Uri.parse("https://accounts.google.com/o/oauth2/v2/auth?access_type=offline&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fcalendar.readonly&response_type=code&client_id=182189361244-ksjd89i6b15mst0tn96ujg155gm7sh97.apps.googleusercontent.com&redirect_uri=urn%3Aietf%3Awg%3Aoauth%3A2.0%3Aoob");
                Intent getAuthIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(getAuthIntent);
                break;

            case R.id.btn_back:
                String code = codeText.getText().toString();
                Intent sendAuthIntent = new Intent();
                sendAuthIntent.putExtra("code", code);
                setResult(RESULT_OK, sendAuthIntent);
                finish();
                break;

            default:
        }
    }
}
