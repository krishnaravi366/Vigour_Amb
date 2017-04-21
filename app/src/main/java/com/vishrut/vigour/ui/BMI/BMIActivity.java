package com.vishrut.vigour.ui.BMI;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.suredigit.inappfeedback.FeedbackDialog;
import com.suredigit.inappfeedback.FeedbackSettings;
import com.vishrut.vigour.FireBase.ReferenceUrl;
import com.vishrut.vigour.R;
import com.vishrut.vigour.Startup.LoginActivity;
import com.vishrut.vigour.chatApp.MainActivity;
import com.vishrut.vigour.ui.Profile.ActivityProfile;
import com.vishrut.vigour.ui.Tracking.StartTrackActivity;

import java.util.List;

public class BMIActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextView bmiHeight;
    private TextView bmiWeight;
    private TextView bmi;
    private TextView bmiCatagory;
    private TextView bmiNormal;
    private DatabaseReference mFirebaseRef;
    String Bmiweight;
    String Bmiheight;


    private static final String TAG = BMIActivity.class.getSimpleName();


    private FirebaseAuth mAuth;

    /* recyclerView for mchat users */
    private RecyclerView mUsersFireChatRecyclerView;

    /* progress bar */
    private View mProgressBarForUsers;


    /* current user uid */
    private String mCurrentUserUid;

    /* current user email */
    private String mCurrentUserEmail;


    /* List holding user key */
    private List<String> mUsersKeyList;
    private FloatingActionButton fab;
    private FeedbackDialog feedback;
    private InterstitialAd mInterstitialAd;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    mCurrentUserEmail = user.getEmail();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        mInterstitialAd = newInterstitialAd();
        loadInterstitial();
        showInterstitial();


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                startActivity(new Intent(BMIActivity.this, BMInormalActivity.class));

            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.addDrawerListener(toggle);
        }
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        mFirebaseRef = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.firebase_profile));

        bmiHeight = (TextView) findViewById(R.id.bmi_detail_hieght);
        bmiWeight = (TextView) findViewById(R.id.bmi_detail_weight);
        bmi = (TextView) findViewById(R.id.bmi_detail_bmi);
        bmiCatagory = (TextView) findViewById(R.id.bmi_detail_catagory);
        bmiNormal = (TextView) findViewById(R.id.bmi_detail_normal);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarBmi);
        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userUid = null;
        if (currentUser != null) {
            userUid = currentUser.getUid();
        }

        mFirebaseRef.child(userUid).addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                Bmiweight = (String) dataSnapshot.child(ReferenceUrl.KEY_WEIGHT).getValue();
                Bmiheight = (String) dataSnapshot.child(ReferenceUrl.KEY_HEIGHT).getValue();
                bmiWeight.setText(Bmiweight);
                bmiHeight.setText(Bmiheight);
                String weight = bmiWeight.getText().toString();
                String height = bmiHeight.getText().toString();
                if (weight.length() > 0 && height.length() > 0) {
                    calcBMI(weight, height);

                    progressBar.setVisibility(View.GONE);

//                            if (progressBar != null) {
//                                progressBar.setVisibility(View.GONE);
//                            }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BMIActivity.this);
                builder.setMessage(databaseError.getMessage()).setTitle("Error").setPositiveButton("OK", null);
                AlertDialog dialog = builder.create();
                dialog.show();
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

    }

    private void calcBMI(String weight, String height) {
        float b = Float.parseFloat(weight) * 10000 / (Float.parseFloat(height) * Float.parseFloat(height));
        bmi.setText(String.valueOf(b));
        if (b > 0 && b < 15.99) {
            bmiCatagory.setText("Severe Thinness");
        } else if (b > 15.99 && b < 17.00) {
            bmiCatagory.setText("Moderate thinness");
        } else if (b > 16.99 && b < 18.50) {
            bmiCatagory.setText("Mild thinness");
        } else if (b > 18.49 && b < 25.00) {
            bmiCatagory.setText("Normal weight");
        } else if (b > 24.99 && b < 30.00) {
            bmiCatagory.setText("Overweight");
        } else if (b > 29.99 && b < 35.00) {
            bmiCatagory.setText("Obese - class I");
        } else if (b > 34.99 && b < 40.00) {
            bmiCatagory.setText("Obese - class II");
        } else if (b > 40) {
            bmiCatagory.setText("Obese - class III");
        }
        int z = (int) ((Float.parseFloat(height) * 18.50 * Float.parseFloat(height)) / 10000);
        int x = (int) ((Float.parseFloat(height) * 24.99 * Float.parseFloat(height)) / 10000);


        bmiNormal.setText(Float.valueOf(z) + "-" + Float.valueOf(x));

//        progressBar.setVisibility(View.GONE);

    }

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_Activity) {
            startActivity(new Intent(BMIActivity.this, StartTrackActivity.class));   // Handle the camera action

        } else if (id == R.id.nav_Profile) {
            startActivity(new Intent(BMIActivity.this, ActivityProfile.class));   // Handle the camera action

        } else if (id == R.id.nav_Bmi) {
//            startActivity(new Intent(BMIActivity.this, BMIActivity.class));   // Handle the camera action


        } else if (id == R.id.nav_History) {

//        } else if (id == R.id.nav_Statistics) {

        } else if (id == R.id.nav_Chat) {
            startActivity(new Intent(BMIActivity.this, MainActivity.class));   // Handle the Chat action
        } else if (id == R.id.nav_Setting) {

        } else if (id == R.id.nav_Feedback) {

            feedback.show();

        } else if (id == R.id.nav_Signout) {
            mAuth.signOut();

            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onBackPressed() {
        showInterstitial();
        super.onBackPressed();
    }

    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
            }

            @Override
            public void onAdClosed() {
            }
        });
        return interstitialAd;
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
        }
    }

    private void loadInterstitial() {
        // Disable the next level button and load the ad.
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        mInterstitialAd.loadAd(adRequest);
    }

}
