package com.naimsplanet.buddychat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
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
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mDislayImage;
    private TextView mDisplayName;
    private TextView mStatusText;
    private TextView mTotalFriends;
    private Button mSendRequestButton;
    private Button mDeclineRequestButton;
    private ProgressDialog mProgressDialog;
    private int mCurrentState;

    //Database referance
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private FirebaseUser mCurrent_user;
    private DatabaseReference mRootRef;

    private String user_id;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();


        mDislayImage = findViewById(R.id.profile_display_image);
        mDisplayName = findViewById(R.id.profile_display_name);
        mStatusText = findViewById(R.id.profile_display_status);
        mTotalFriends = findViewById(R.id.profile_total_friends);
        mSendRequestButton = findViewById(R.id.send_friend_request_button);
        mDeclineRequestButton = findViewById(R.id.decline_friend_request_button);

        mDeclineRequestButton.setVisibility(View.INVISIBLE);
        mDeclineRequestButton.setEnabled(false);

        mCurrent_state = "not_friends";

        mDeclineRequestButton.setVisibility(View.INVISIBLE);
        mDeclineRequestButton.setEnabled(false);


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load the user data.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mDisplayName.setText(display_name);
                mStatusText.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.default_profile).into(mDislayImage);

                if (mCurrent_user.getUid().equals(user_id)) {

                    mDeclineRequestButton.setEnabled(false);
                    mDeclineRequestButton.setVisibility(View.INVISIBLE);

                    mSendRequestButton.setEnabled(false);
                    mSendRequestButton.setVisibility(View.INVISIBLE);

                }


                //--------------- FRIENDS LIST / REQUEST FEATURE -----

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)) {

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (req_type.equals("received")) {

                                mCurrent_state = "req_received";
                                mSendRequestButton.setText("Accept Friend Request");

                                mDeclineRequestButton.setVisibility(View.VISIBLE);
                                mDeclineRequestButton.setEnabled(true);


                            } else if (req_type.equals("sent")) {

                                mCurrent_state = "req_sent";
                                mSendRequestButton.setText("Cancel Friend Request");

                                mDeclineRequestButton.setVisibility(View.INVISIBLE);
                                mDeclineRequestButton.setEnabled(false);

                            }

                            mProgressDialog.dismiss();


                        } else {


                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_id)) {

                                        mCurrent_state = "friends";
                                        mSendRequestButton.setText("Unfriend this Person");

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
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mSendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mSendRequestButton.setEnabled(false);

                // --------------- NOT FRIENDS STATE ------------

                if (mCurrent_state.equals("not_friends")) {


                    DatabaseReference newNotificationref = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrent_user.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + user_id + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {

                                Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();

                            } else {

                                mCurrent_state = "req_sent";
                                mSendRequestButton.setText("Cancel Friend Request");

                            }

                            mSendRequestButton.setEnabled(true);


                        }
                    });

                }


                // - -------------- CANCEL REQUEST STATE ------------

                if (mCurrent_state.equals("req_sent")) {

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {


                                    mSendRequestButton.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mSendRequestButton.setText("Send Friend Request");

                                    mDeclineRequestButton.setVisibility(View.INVISIBLE);
                                    mDeclineRequestButton.setEnabled(false);


                                }
                            });

                        }
                    });

                }


                // ------------ REQ RECEIVED STATE ----------

                if (mCurrent_state.equals("req_received")) {

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date", currentDate);


                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);


                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if (databaseError == null) {

                                mSendRequestButton.setEnabled(true);
                                mCurrent_state = "friends";
                                mSendRequestButton.setText("Unfriend this Person");

                                mDeclineRequestButton.setVisibility(View.INVISIBLE);
                                mDeclineRequestButton.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                            }

                        }
                    });

                }


                // ------------ UNFRIENDS ---------

                if (mCurrent_state.equals("friends")) {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if (databaseError == null) {

                                mCurrent_state = "not_friends";
                                mSendRequestButton.setText("Send Friend Request");

                                mDeclineRequestButton.setVisibility(View.INVISIBLE);
                                mDeclineRequestButton.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                            }

                            mSendRequestButton.setEnabled(true);

                        }
                    });

                }


            }
        });


    }


}
