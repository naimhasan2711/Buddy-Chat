package com.naimsplanet.buddychat.authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.naimsplanet.buddychat.activity.MainActivity;
import com.naimsplanet.buddychat.R;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity
{


    private FirebaseAuth mAuth;
    private TextInputLayout mDisplayName, mEmail, mPassword;
    private AppCompatEditText mNameText, mEmailText, mPasswordText;
    private Button mRegisterButton;
    private Toolbar mToolbar;

    private DatabaseReference mDatabase;

    private ProgressDialog mRegisterProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mDisplayName = findViewById(R.id.register_displayName);
        mEmail = findViewById(R.id.register_email);
        mPassword = findViewById(R.id.register_password);

        mNameText = findViewById(R.id.register_nameText);
        mEmailText = findViewById(R.id.register_emailText);
        mPasswordText = findViewById(R.id.register_passwordText);
        mRegisterButton = findViewById(R.id.register_button);

        mToolbar = findViewById(R.id.register_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegisterProgress = new ProgressDialog(this);


        mRegisterButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String email = mEmailText.getText().toString();
                String password = mPasswordText.getText().toString();
                String displayName = mNameText.getText().toString();

                if(!TextUtils.isEmpty(displayName) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password))
                {
                    mRegisterProgress.setTitle("Registering Account");
                    mRegisterProgress.setMessage("Please wait util we create your account...");
                    mRegisterProgress.setCanceledOnTouchOutside(false);
                    mRegisterProgress.show();
                    registerUser(displayName , email , password);
                }

            }
        });
    }

    private void registerUser(final String displayName, String email, String password)
    {
        mAuth.createUserWithEmailAndPassword(email , password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if(task.isSuccessful())
                        {

                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = current_user.getUid();
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            HashMap<String , String> userMap = new HashMap<>();
                            userMap.put("name" , displayName);
                            userMap.put("status" , "Hi there, I am using BuddyChat");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");
                            userMap.put("device_token",deviceToken);

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        mRegisterProgress.dismiss();
                                        Intent mainIntent = new Intent(RegisterActivity.this , MainActivity.class);
                                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });





                        }
                        else
                        {
                            mRegisterProgress.hide();
                            Toast.makeText(RegisterActivity.this, "Cannot Register. Please try again...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
