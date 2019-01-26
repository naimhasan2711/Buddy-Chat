package com.naimsplanet.buddychat.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.naimsplanet.buddychat.R;
import com.naimsplanet.buddychat.authentication.LoginActivity;
import com.naimsplanet.buddychat.authentication.RegisterActivity;

public class StartActivity extends AppCompatActivity
{

    Button mRegisterButton;
    Button mLoginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mRegisterButton = findViewById(R.id.start_register_button);
        mLoginButton = findViewById(R.id.start_login_button);


        mRegisterButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent registerIntent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(StartActivity.this , LoginActivity.class);
                startActivity(loginIntent);
            }
        });
    }
}
