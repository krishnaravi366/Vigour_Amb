package com.vishrut.vigour.ui.Profile;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suredigit.inappfeedback.FeedbackDialog;
import com.suredigit.inappfeedback.FeedbackSettings;
import com.vishrut.vigour.Chat.UserListActivity;
import com.vishrut.vigour.Chat.adapter.UsersChatAdapter;
import com.vishrut.vigour.FireBase.ReferenceUrl;
import com.vishrut.vigour.R;
import com.vishrut.vigour.ui.BMI.BMIActivity;
import com.vishrut.vigour.ui.Tracking.StartTrackActivity;

import java.util.List;

public class ActivityProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private EditText Firstname;
    private EditText Lastname;
    private EditText Age;
    private EditText Weight;
    private EditText Height;
    private Button ProfileButton;
    private DatabaseReference mFirebaseRef;
    private String firstName;
    private String lastName;
    private String age;
    private String weight;
    private String height;
    public String gotFirstName;
    public String gotLastName;
    public String gotAge;
    public String gotHeight;
    public String gotWeight;


    private static final String TAG = UserListActivity.class.getSimpleName();


    /* recyclerView for mchat users */
    private RecyclerView mUsersFireChatRecyclerView;

    /* progress bar */
    private View mProgressBarForUsers;

    /* fire chat adapter */
    private UsersChatAdapter mUsersChatAdapter;

    /* current user uid */
    private String mCurrentUserUid;

    /* current user email */
    private String mCurrentUserEmail;

    /* Listen to users change in firebase-remember to detach it */
    private ChildEventListener mListenerUsers;

    /* Listen for user presence */
    private ValueEventListener mConnectedListener;

    /* List holding user key */
    private List<String> mUsersKeyList;
    private FloatingActionButton fab;
    private FeedbackDialog feedback;


    /* Reference to firebase */
    private DatabaseReference mFirebaseChatRef;

    /* Reference to users in firebase */
    private DatabaseReference mFireChatUsersRef;

    /* Updating connection status */
    DatabaseReference myConnectionsStatusRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                startActivity(new Intent(ActivityProfile.this, HeightActivity.class));

            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }


        mFirebaseRef = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.firebase_profile));
        mAuth = FirebaseAuth.getInstance();
        Firstname = (EditText) findViewById(R.id.result_ran);
        Lastname = (EditText) findViewById(R.id.lname);
        Age = (EditText) findViewById(R.id.age);
        Weight = (EditText) findViewById(R.id.weight);
        Height = (EditText) findViewById(R.id.height);
        ProfileButton = (Button) findViewById(R.id.profilebutton);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarProfile);
        progressBar.setVisibility(View.VISIBLE);

        final String userUid = mAuth.getCurrentUser().getUid();


        mFirebaseRef.child(userUid).addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                firstName = (String) dataSnapshot.child(ReferenceUrl.KEY_NAME).getValue();
                lastName = (String) dataSnapshot.child(ReferenceUrl.KEY_LAST_NAME).getValue();
                age = (String) dataSnapshot.child(ReferenceUrl.KEY_AGE).getValue();
                weight = (String) dataSnapshot.child(ReferenceUrl.KEY_WEIGHT).getValue();
                height = (String) dataSnapshot.child(ReferenceUrl.KEY_HEIGHT).getValue();
                Firstname.setText(firstName);
                Lastname.setText(lastName);
                Age.setText(age);
                Weight.setText(weight);
                Height.setText(height);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        ProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstName = Firstname.getText().toString().trim();
                lastName = Lastname.getText().toString().trim();
                age = Age.getText().toString().trim();
                weight = Weight.getText().toString().trim();
                height = Height.getText().toString().trim();
                ProfileData data = new ProfileData(gotFirstName, gotLastName, gotAge, gotHeight, gotWeight);
                if (firstName.length() == 0 || lastName.length() == 0 || age.length() == 0 || weight.length() == 0 || height.length() == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityProfile.this);
                    builder.setMessage("Please fill all the data").setTitle("Error").setPositiveButton("OK", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    mFirebaseRef.child(ReferenceUrl.CHILD_PROFILE).child(userUid).push().setValue(data, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {

                            mFirebaseRef.child(userUid).child("firstName").setValue(firstName);
                            mFirebaseRef.child(userUid).child("lastName").setValue(lastName);
                            mFirebaseRef.child(userUid).child("weight").setValue(weight);
                            mFirebaseRef.child(userUid).child("height").setValue(height);
                            mFirebaseRef.child(userUid).child("age").setValue(age);
                        }
                    });


                }
                progressBar.setVisibility(View.GONE);
                finish();


            }
        });

        FeedbackSettings feedbackSettings = new FeedbackSettings();

//SUBMIT-CANCEL BUTTONS
        feedbackSettings.setCancelButtonText("No");
        feedbackSettings.setSendButtonText("Send");

//DIALOG TEXT
        feedbackSettings.setText("Hey, would you like to give us some feedback so that we can improve your experience?");
        feedbackSettings.setYourComments("Type your question here...");
        feedbackSettings.setTitle("Please give Feedback");

//TOAST MESSAGE
        feedbackSettings.setToast("Thank you so much!");
        feedbackSettings.setToastDuration(Toast.LENGTH_SHORT);  // Default
        feedbackSettings.setToastDuration(Toast.LENGTH_LONG);

//RADIO BUTTONS
        feedbackSettings.setRadioButtons(true); // Disables radio buttons
        feedbackSettings.setBugLabel("Bug");
        feedbackSettings.setIdeaLabel("Idea");
        feedbackSettings.setQuestionLabel("Question");

//RADIO BUTTONS ORIENTATION AND GRAVITY
        feedbackSettings.setOrientation(LinearLayout.HORIZONTAL); // Default
        feedbackSettings.setOrientation(LinearLayout.VERTICAL);
        feedbackSettings.setGravity(Gravity.RIGHT); // Default
        feedbackSettings.setGravity(Gravity.LEFT);
        feedbackSettings.setGravity(Gravity.CENTER);

//SET DIALOG MODAL
        feedbackSettings.setModal(true); //Default is false

//DEVELOPER REPLIES
        feedbackSettings.setReplyTitle("Message from the Developer");
        feedbackSettings.setReplyCloseButtonText("Close");
        feedbackSettings.setReplyRateButtonText("RATE!");

//DEVELOPER CUSTOM MESSAGE (NOT SEEN BY THE END USER)
        feedbackSettings.setDeveloperMessage("This is a custom message that will only be seen by the developer!");

        feedback = new FeedbackDialog(this, "AF-7291A85FF651-8C", feedbackSettings);
        mAuthStateListener = new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                setAuthenticatedUser(authData);
            }
        };

    }

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_Activity) {
            startActivity(new Intent(ActivityProfile.this, StartTrackActivity.class));   // Handle the camera action
        } else if (id == R.id.nav_Profile) {
//            startActivity(new Intent(ActivityProfile.this, ActivityProfile.class));   // Handle the camera action
        } else if (id == R.id.nav_Bmi) {
            startActivity(new Intent(ActivityProfile.this, BMIActivity.class));   // Handle the camera action
        } else if (id == R.id.nav_History) {

//        } else if (id == R.id.nav_Statistics) {

        } else if (id == R.id.nav_Chat) {
            startActivity(new Intent(ActivityProfile.this, UserListActivity.class));   // Handle the Chat action
        } else if (id == R.id.nav_Setting) {

        } else if (id == R.id.nav_Feedback) {
            feedback.show();

        } else if (id == R.id.nav_Signout) {
            logout();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void setAuthenticatedUser(AuthData authData) {
        mAuthData = authData;
        if (authData != null) {

            /* User auth has not expire yet */

            // Get unique current user ID
            mCurrentUserUid = authData.getUid();

            // Get current user email
            mCurrentUserEmail = (String) authData.getProviderData().get(ReferenceUrl.KEY_EMAIL);


        } else {
            // Token expires or user log out
            // So show logIn screen to reinitiate the token

        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void logout() {

        if (this.mAuthData != null) {

            /* Logout of mChat */

            // Store current user status as offline
            myConnectionsStatusRef.setValue(ReferenceUrl.KEY_OFFLINE);

            // Finish token
            FirebaseAuth.getInstance().signOut();

            /* Update authenticated user and show login screen */
            setAuthenticatedUser(null);
        }
    }

}