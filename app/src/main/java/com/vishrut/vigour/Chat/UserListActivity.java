package com.vishrut.vigour.Chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vishrut.vigour.Chat.adapter.UsersChatAdapter;
import com.vishrut.vigour.Chat.model.UsersChatModel;
import com.vishrut.vigour.FireBase.ReferenceUrl;
import com.vishrut.vigour.R;
import com.vishrut.vigour.Startup.LoginActivity;

import java.util.ArrayList;
import java.util.List;


public class UserListActivity extends Activity {

    private static final String TAG = UserListActivity.class.getSimpleName();

    /* Reference to firebase */
    private DatabaseReference mFirebaseChatRef;

    /* Reference to users in firebase */
    private DatabaseReference mFireChatUsersRef;

    /* Updating connection status */
    DatabaseReference myConnectionsStatusRef;


    /* Data from the authenticated user */
    private FirebaseAuth auth;

    /* progress bar */
    private View mProgressBarForUsers;

    /* fire chat adapter */
    private UsersChatAdapter mUsersChatAdapter;

    /* current user uid */
    private String mCurrentUserUid;

    /* current user email */
    private String mCurrentUserEmail;

    /* Listen to users change in firebase-remember to detach it */
    private com.google.firebase.database.ChildEventListener mListenerUsers;

    /* Listen for user presence */
    private com.google.firebase.database.ValueEventListener mConnectedListener;

    /* List holding user key */
    private List<String> mUsersKeyList;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        auth = FirebaseAuth.getInstance();
        // Initialize firebase
        mFirebaseChatRef = FirebaseDatabase.getInstance().getReference(); // Get app main firebase url

        // Get a reference to users child in firebase
        mFireChatUsersRef = FirebaseDatabase.getInstance().getReference(ReferenceUrl.CHILD_USERS);

        // Get a reference to recyclerView
        RecyclerView mUsersFireChatRecyclerView = (RecyclerView) findViewById(R.id.usersFireChatRecyclerView);

        // Get a reference to progress bar
        mProgressBarForUsers = findViewById(R.id.progress_bar_users);

        // Initialize adapter
        List<UsersChatModel> emptyListChat = new ArrayList<>();
        mUsersChatAdapter = new UsersChatAdapter(this, emptyListChat);

        // Set adapter to recyclerView
        mUsersFireChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mUsersFireChatRecyclerView.setHasFixedSize(true);
        mUsersFireChatRecyclerView.setAdapter(mUsersChatAdapter);

        // Initialize keys list
        mUsersKeyList = new ArrayList<>();

        // Listen for changes in the authentication state
        // Because probably token expire after 24hrs or
        // user log out
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    mCurrentUserUid = user.getUid();

                    // Get current user email
                    mCurrentUserEmail = user.getEmail();

                    // Query all mChat user except current user
                    queryFireChatUsers();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        // Register the authentication state listener

    }


    private void queryFireChatUsers() {

        //Show progress bar
        showProgressBarForUsers();

        mListenerUsers = mFireChatUsersRef.limitToFirst(50).addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(com.google.firebase.database.DataSnapshot dataSnapshot, String s) {
                //Log.e(TAG, "inside onChildAdded");
                //Hide progress bar
                hideProgressBarForUsers();

                if (dataSnapshot.exists()) {
                    //Log.e(TAG, "A new user was inserted");

                    String userUid = dataSnapshot.getKey();

                    if (!userUid.equals(mCurrentUserUid)) {

                        //Get recipient user name
                        UsersChatModel user = dataSnapshot.getValue(UsersChatModel.class);

                        //Add recipient uid
                        user.setRecipientUid(userUid);

                        //Add current user (or sender) info
                        user.setCurrentUserEmail(mCurrentUserEmail); //email
                        user.setCurrentUserUid(mCurrentUserUid);//uid
                        mUsersKeyList.add(userUid);
                        mUsersChatAdapter.refill(user);

                    } else {
                        UsersChatModel currentUser = dataSnapshot.getValue(UsersChatModel.class);
                        String userName = currentUser.getFirstName(); //Get current user first name
                        String createdAt = currentUser.getCreatedAt(); //Get current user date creation
                        mUsersChatAdapter.setNameAndCreatedAt(userName, createdAt); //Add it the adapter
                    }
                }
            }

            @Override
            public void onChildChanged(com.google.firebase.database.DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    String userUid = dataSnapshot.getKey();
                    if (!userUid.equals(mCurrentUserUid)) {
                        UsersChatModel user = dataSnapshot.getValue(UsersChatModel.class);

                        // Removed bug here
                        //Add recipient uid
                        user.setRecipientUid(userUid);

                        //Add current user (or sender) info
                        user.setCurrentUserEmail(mCurrentUserEmail); //email
                        user.setCurrentUserUid(mCurrentUserUid);//uid
                        int index = mUsersKeyList.indexOf(userUid);
                        Log.e(TAG, "change index " + index);
                        mUsersChatAdapter.changeUser(index, user);
                    }

                }
            }

            @Override
            public void onChildRemoved(com.google.firebase.database.DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(com.google.firebase.database.DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // // Store current user status as online
        myConnectionsStatusRef = mFireChatUsersRef.child(mCurrentUserUid).child(ReferenceUrl.CHILD_CONNECTION);

        // Indication of connection status
        mConnectedListener = mFirebaseChatRef.getRoot().child(".info/connected").addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                if (connected) {

                    myConnectionsStatusRef.setValue(ReferenceUrl.KEY_ONLINE);

                    // When this device disconnects, remove it
                    myConnectionsStatusRef.onDisconnect().setValue(ReferenceUrl.KEY_OFFLINE);
                    // Toast.makeText(UserListActivity.this, "Connected to Firebase", Toast.LENGTH_SHORT).show();

                } else {

                    // Toast.makeText(UserListActivity.this, "Disconnected from Firebase", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void navigateToLogin() {

        // Go to LogIn screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // LoginActivity is a New Task
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // The old task when coming back to this activity should be cleared so we cannot come back to it.
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //int size=mUsersKeyList.size();
        //Log.e(TAG, " size"+size);
    }


    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            auth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Log.e(TAG, "I am onDestroy");

        // If changing configurations, stop tracking firebase session.

        mUsersKeyList.clear();

        // Stop all listeners
        // Make sure to check if they have been initialized
        if (mListenerUsers != null) {
            mFireChatUsersRef.removeEventListener(mListenerUsers);
        }
        if (mConnectedListener != null) {
            mFirebaseChatRef.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
        }
    }


    /*Show and hide progress bar*/
    private void showProgressBarForUsers() {
        mProgressBarForUsers.setVisibility(View.VISIBLE);
    }


    private void hideProgressBarForUsers() {
        if (mProgressBarForUsers.getVisibility() == View.VISIBLE) {
            mProgressBarForUsers.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
