package com.naimsplanet.buddychat.activity;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.naimsplanet.buddychat.R;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity
{

    private ImageView mDislayImage;
    private TextView mDisplayName;
    private TextView mStatusText;
    private TextView mTotalFriends;
    private Button mSendRequestButton;
    private Button mDeclineRequestButton;
    private ProgressDialog mProgressDialog;
    private int mCurrentState;

    //Database referance
    private DatabaseReference mUsersDatabse;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mNotificationDatabase;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //firebase database code
        final String user_id = getIntent().getStringExtra("user_id");//user id of other user
        mUsersDatabse = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_request");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();//user id of mine


        mDislayImage = findViewById(R.id.profile_display_image);
        mDisplayName = findViewById(R.id.profile_display_name);
        mStatusText = findViewById(R.id.profile_display_status);
        mTotalFriends = findViewById(R.id.profile_total_friends);
        mSendRequestButton = findViewById(R.id.send_friend_request_button);
        mDeclineRequestButton = findViewById(R.id.decline_friend_request_button);

        //progress bar code
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mCurrentState = 0;// 0 for initial state


        mUsersDatabse.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                //get data from firebase databse
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                //set data
                mDisplayName.setText(display_name);
                mStatusText.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.default_profile).into(mDislayImage);//set display image

                //------------------ FRIEND LIST / REQUEST FEATURE ------------------

                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id))
                        {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(req_type.equals("received")){
                                mCurrentState = 11;//11 for received friend request
                                mSendRequestButton.setText("Accept Friend Request");
                                mDeclineRequestButton.setVisibility(View.VISIBLE);
                                mDeclineRequestButton.setEnabled(true);
                            }
                            else if(req_type.equals("sent"))
                            {
                                mCurrentState = 1;//1 for send friend request
                                mSendRequestButton.setText("Cancel Friend Request");

                                mDeclineRequestButton.setVisibility(View.INVISIBLE);
                                mDeclineRequestButton.setEnabled(false);
                            }

                            mProgressDialog.dismiss();
                        }
                        else
                        {
                            mFriendsDatabase.child(mCurrentUser.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(user_id))
                                            {
                                                mCurrentState = 100; // 100 for accept friend request, i mean friend state
                                                mSendRequestButton.setText("unfriend this person");

                                                mDeclineRequestButton.setVisibility(View.INVISIBLE);
                                                mDeclineRequestButton.setEnabled(false);
                                            }
                                            mProgressDialog.dismiss();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            mProgressDialog.dismiss();
                                        }
                                    });
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        mSendRequestButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                mSendRequestButton.setEnabled(false);

                //----------- NOT FRIEND STATE-------------------

                if(mCurrentState == 0)
                {
                    mFriendRequestDatabase.child(mCurrentUser.getUid())
                            .child(user_id).child("request_type")
                            .setValue("sent").
                            addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                mFriendRequestDatabase.child(user_id)
                                        .child(mCurrentUser.getUid())
                                        .child("request_type")
                                        .setValue("received")
                                        .addOnSuccessListener(new OnSuccessListener<Void>()
                                        {
                                            @Override
                                            public void onSuccess(Void aVoid)
                                            {

                                               HashMap<String , String> notificationData = new HashMap<>();
                                               notificationData.put("from", mCurrentUser.getUid());
                                               notificationData.put("type","request");

                                               mNotificationDatabase.child(user_id).push().setValue(notificationData)
                                                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                           @Override
                                                           public void onComplete(@NonNull Task<Void> task) {

                                                               mCurrentState=1;// 1 for request send state
                                                               mSendRequestButton.setText("Cancel Friend Request");

                                                               mDeclineRequestButton.setVisibility(View.INVISIBLE);
                                                               mDeclineRequestButton.setEnabled(false);
                                                           }
                                                       });


                                               // Toast.makeText(ProfileActivity.this, "Request Sent Successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                            else
                            {
                                Toast.makeText(ProfileActivity.this, "Failed Sending Request.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                //------------- CANCEL FRIEND REQUEST STATE------------

                if(mCurrentState == 1)
                {
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>()
                                                {
                                                    @Override
                                                    public void onSuccess(Void aVoid)
                                                    {
                                                        mSendRequestButton.setEnabled(true);
                                                        mCurrentState = 0;//as cancel friend request
                                                        mSendRequestButton.setText("Send Friend Request");
                                                        mDeclineRequestButton.setVisibility(View.INVISIBLE);
                                                        mDeclineRequestButton.setEnabled(false);
                                                    }
                                                });
                                    }
                                }
                            });
                }

                // ------------------ ACCEPT REQUEST STATE----------------------

                if(mCurrentState == 11)
                {
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    mFriendsDatabase.child(mCurrentUser.getUid()).child(user_id).setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>()
                            {
                                @Override
                                public void onSuccess(Void aVoid)
                                {
                                    mFriendsDatabase.child(user_id).child(mCurrentUser.getUid()).setValue(currentDate)
                                            .addOnSuccessListener(new OnSuccessListener<Void>()
                                            {
                                                @Override
                                                public void onSuccess(Void aVoid)
                                                {
                                                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                            {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if(task.isSuccessful())
                                                                    {
                                                                        mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue()
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>()
                                                                                {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid)
                                                                                    {
                                                                                        mSendRequestButton.setEnabled(true);
                                                                                        mCurrentState = 100;//100 for friend state
                                                                                        mSendRequestButton.setText("unfriend this person");
                                                                                        mDeclineRequestButton.setVisibility(View.INVISIBLE);
                                                                                        mDeclineRequestButton.setEnabled(false);
                                                                                    }
                                                                                });
                                                                    }
                                                                }
                                                            });
                                                }
                                            });
                                }
                            });
                }


                //----------------- UNFRIEND STATE---------------------

                if(mCurrentState == 100)
                {
                    mFriendsDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mCurrentState = 0;
                                    mSendRequestButton.setText("Send Friend Request");

                                    mDeclineRequestButton.setVisibility(View.INVISIBLE);
                                    mDeclineRequestButton.setEnabled(false);
                                }
                            });
                }
            }
        });


    }
}
