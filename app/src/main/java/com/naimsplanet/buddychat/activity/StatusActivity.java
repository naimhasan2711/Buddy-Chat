package com.naimsplanet.buddychat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.naimsplanet.buddychat.R;

public class StatusActivity extends AppCompatActivity
{

    private Toolbar mToolbar;
    private AppCompatEditText mStatusText;
    private Button mChangeStatusButton;

    private ProgressDialog mStatusProgress;

    //firebase code
    DatabaseReference mDatabaseReference;
    FirebaseUser mCurrentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar = findViewById(R.id.status_appBar);
        mStatusText = findViewById(R.id.status_text);
        mChangeStatusButton = findViewById(R.id.mChange_Status_button);


        //firebase code
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_user = mCurrentUser.getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user);

        //toolbar code
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_text = getIntent().getStringExtra("status_key");
        mStatusText.setText(status_text);

        mChangeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                //progress dialog code
                mStatusProgress = new ProgressDialog(StatusActivity.this);
                mStatusProgress.setTitle("Saving changes");
                mStatusProgress.setMessage("Please wait while we save the change");
                mStatusProgress.show();

                //update status code
                String status = mStatusText.getText().toString();
                mDatabaseReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            mStatusProgress.dismiss();
                            Intent statusIntent = new Intent(StatusActivity.this , SettingsActivity.class);
                            startActivity(statusIntent);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(StatusActivity.this, "There was some error in save the change.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });
    }
}
