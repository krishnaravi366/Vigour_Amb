package com.vishrut.vigour.ui.Tracking;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suredigit.inappfeedback.FeedbackDialog;
import com.suredigit.inappfeedback.FeedbackSettings;
import com.vishrut.vigour.Alarm.AlarmListActivity;
import com.vishrut.vigour.Chat.UserListActivity;
import com.vishrut.vigour.Chat.adapter.UsersChatAdapter;
import com.vishrut.vigour.FireBase.ReferenceUrl;
import com.vishrut.vigour.R;
import com.vishrut.vigour.Service.FetchAddressIntentService;
import com.vishrut.vigour.ViewPager.ViewPagerActivity;
import com.vishrut.vigour.ui.BMI.BMIActivity;
import com.vishrut.vigour.ui.History.HistoryActivity;
import com.vishrut.vigour.ui.MotivationBlog.BlogActivity;
import com.vishrut.vigour.ui.Profile.ActivityProfile;
import com.vishrut.vigour.ui.Setting.SettingActivity;

import java.io.IOException;
import java.util.List;

import cn.hiroz.uninstallfeedback.FeedbackUtils;

public class StartTrackActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private static final int RC_FINE = 4788;
    private GoogleApiClient mGoogleApiClient;
    private FloatingActionButton fa;
    private Location mLastLocation;
    private TextView textLocation;
    private LocationRequest mLocationRequest;
    private AddressResultReceiver mReceiver;
    private TextView textAddress;
    private GoogleMap map;
    private Chronometer mChronometer;
    long timeElapsed; //For example 7564000
    // TextView threadView;
    TextView speedView;
    TextView myDistance;
    TextView actualCalories;
    int threadTimer = 0;
    int totalTime = 0;
    double lat;
    double speed;
    double distanceForSpeed = 0;
    double lng;
    float weight = 65;//(65 kg avg weight)
    boolean running = false;
    boolean flag;
    boolean toggleCheck = false;
    String distance;
    String[] latlng = new String[1];
    String allStats = "";
    String messageText;
    Location location;
    private Location lastLocation = null;
    private double distanceInMe = 0;
    public final static String EXTRA_MESSAGE = "message";
    static boolean onceOnly = true;
    private int counter = 1;
    Geocoder geocoder;
    List<Address> list;
    Address address;
    ToggleButton startStop;
    Intent intent;
    ShareActionProvider shareActionProvider;
    private static final String TAG = UserListActivity.class.getSimpleName();
    /* Reference to firebase */
    private DatabaseReference mFirebaseChatRef;
    /* Reference to users in firebase */
    private DatabaseReference mFireChatUsersRef;

    /* Listener for Firebase session changes */
    private FirebaseAuth.AuthStateListener mAuthStateListener;


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
    SoundPool soundpoolStart;
    public static final String EXTRA_TEXT = "com.vishrut.vigour";
    private String time;
    private int onlyMinutes;
    private float avgSpeed;
    private float avgDistance;
    private float avgTime;
    private float onlySeconds;
    private String resultSpeed;
    private String calorie;
    private float calories;
    private float calcHour;
    private int hours;
    private int minutes;
    private int seconds;
    private int total_seconds;

    private InterstitialAd mInterstitialAd;

    private FirebaseDatabase mDbase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDbase = FirebaseDatabase.getInstance();

//        AdView adView = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder()
//                .setRequestAgent("android_studio:ad_template").build();
//        adView.loadAd(adRequest);


        mInterstitialAd = newInterstitialAd();
        loadInterstitial();

        soundpoolStart = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
        final int soundStart = soundpoolStart.load(StartTrackActivity.this, R.raw.session_started, 1);
        final int soundStop = soundpoolStart.load(StartTrackActivity.this, R.raw.session_completed, 1);


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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FeedbackUtils.openUrlWhenUninstall(this, "https://www.facebook.com/Vigour-233131403713198/reviews/");


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Initialize firebase

        mFirebaseChatRef = mDbase.getReference(); // Get app main firebase url

        // Get a reference to users child in firebase
        mFireChatUsersRef = mDbase.getReference(ReferenceUrl.CHILD_USERS);


        // Get a reference to progress bar
        mProgressBarForUsers = findViewById(R.id.progress_bar_users);

        // Listen for changes in the authentication state
        // Because probably token expire after 24hrs or
        // user log out
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        // Register the authentication state listener
        //mFirebaseChatRef.addAuthStateListener(this);

        buildGoogleApiClient();
        mReceiver = new AddressResultReceiver(new Handler());
        textLocation = (TextView) findViewById(R.id.textLocation);
        textAddress = (TextView) findViewById(R.id.tvAddress);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (savedInstanceState != null) {
            distanceInMe = savedInstanceState.getDouble("distanceInMe");
            running = savedInstanceState.getBoolean("running");
            distanceForSpeed = savedInstanceState.getDouble("distanceForSpeed");
            speed = savedInstanceState.getDouble("speed");
            totalTime = savedInstanceState.getInt("totalTime");
            threadTimer = savedInstanceState.getInt("threadTimer");
            onceOnly = false;
            toggleCheck = savedInstanceState.getBoolean("toggleCheck");
        }
        allStats = "NOT AVAILABLE";
        // threadView = (TextView) findViewById(R.id.myTimer);
        myDistance = (TextView) findViewById(R.id.myDis);
        speedView = (TextView) findViewById(R.id.mySpeed);
        startStop = (ToggleButton) findViewById(R.id.startStop);
        //startStop.setText("Start Activity");
        actualCalories = (TextView) findViewById(R.id.actualCalories);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); //First we asked for the location service
        geocoder = new Geocoder(this);
        String provider = LocationManager.GPS_PROVIDER;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        location = locationManager.getLastKnownLocation(provider); //getting the last known location if the Location is ON on the device
        lastLocation = location;
        if (location == null) {
//            Toast.makeText(this, "Please start your GPS if its off  ", Toast.LENGTH_SHORT).show();
            flag = false;
        } else {   //else flag is set true and latitude and longitude are taken ,we need them here as if we call thread first then
            flag = true;  //then there will be null at lat lng and calling geocoder methods would return an empty list causing bugs
            lat = location.getLatitude();  //ALSO IF ELSE IS USED because without them ..if location is null and if we try to call
            lng = location.getLongitude();     //getLatitude() on a null reference then it causes errors and app crashes
        }
        oneGiantButLightThread();
        locationManager.requestLocationUpdates(provider, 0, 0, listener);
        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStop.getAnimation();

                if (counter == 1) {
                    mStartListener(startStop);
                    calories = 0;
                    fetchAddressButtonHandler();
                    counter = 2;
                }
//                else if (counter == 2) {
//                    mStopListener(Start_Stop);
//                }
                else {
                    counter = 1;
                    mStopListener(startStop);
                }
            }


            private void mStopListener(Button start_stop) {
                calories = 0;
                startStop.setChecked(false);

                running = false;


                timeElapsed = SystemClock.elapsedRealtime() - mChronometer.getBase();
//                hours = (int) (timeElapsed / 3600000);    //hours = 2
//                minutes = (int) (timeElapsed - hours * 3600000) / 60000;   //minutes = 6
//                seconds = (int) (timeElapsed - hours * 3600000 - minutes * 60000) / 1000;  //seconds = 4
                //hours = 2
                hours = (int) (timeElapsed / 3600000);
                //minutes = 6
                minutes = (int) (timeElapsed - hours * 3600000) / 60000;
                //seconds = 4
                seconds = (int) (timeElapsed - hours * 3600000 - minutes * 60000) / 1000;
                //seconds = 7564
                total_seconds = (int) (timeElapsed) / 1000;

                onlyMinutes = (hours * 60) + minutes;
//                time = hours + " h  " + minutes + " m  " + seconds+ " s ";  // Total Time- + timeElapsed
                time = onlyMinutes + " m  " + seconds + " s ";
                onlySeconds = total_seconds;
//                avgTime = Float.parseFloat(time);
//                avgSpeed = onlySeconds;
                calcHour = onlySeconds / 60;

                avgSpeed = (float) ((distanceInMe / 1000000) / onlySeconds);
                if (avgSpeed < 0) {
                    avgSpeed = 0;
                }
//

                calories = (float) ((0.0215 * ((distanceInMe / (1000 * calcHour)) * (distanceInMe / (1000 * calcHour)) * (distanceInMe / (1000 * calcHour))) - 0.1765 * ((distanceInMe / (1000 * calcHour)) * (distanceInMe / (1000 * calcHour))) + 0.8710 * (((distanceInMe / (1000 * calcHour))) + 1.4577) * weight * calcHour));
                calorie = String.format("%.1f", calories) + " Calories";
                if (distanceInMe > 100) {
                    calories = 0;
                }

                resultSpeed = avgSpeed + "m/s";
                actualCalories.setText(calorie);
                onActivitySpopped();


                totalTime = 0;
                threadTimer = 0;
                speed = 0;
                distanceInMe = 0;
                totalTime = 0;
                distanceForSpeed = 0;
                myDistance.setText("0.00 KM");
                speedView.setText("0.00 m/s");
                mChronometer.stop();
                soundpoolStart.play(soundStop, 1, 1, 1, 0, 1);
                mChronometer.setBase(SystemClock.elapsedRealtime());
                calories = 0;

            }

            private void mStartListener(Button start_stop) {

                running = true;
                soundpoolStart.play(soundStart, 1, 1, 1, 0, 1);
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.start();

                //  startStop.setText("Stop Activity");
            }
        });
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    mCurrentUserUid = user.getUid();
                    // Get current user email
                    mCurrentUserEmail = (String) user.getEmail();
                } else {
                    navigateToLogin();
                }
                // ...
            }
        };
//
        loadInterstitial();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            showInterstitial();
            mInterstitialAd.show();
            //  super.onBackPressed();
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle(null)
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            //isDestroyed();
//                            onDestroy();
                            //  stopLocationUpdates();
                        }
                    })
                    .show();

            alertDialog.setCancelable(false);//bhaag jata h agar screen pe kahi b touch hota h
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        } else {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // If changing configurations, stop tracking firebase session.
        //mFirebaseChatRef.removeAuthStateListener(mAuthStateListener);
        // Stop all listeners
        // Make sure to check if they have been initialized
        if (mListenerUsers != null) {
            //mFireChatUsersRef.removeEventListener(mListenerUsers);
        }
        if (mConnectedListener != null) {
            // mFirebaseChatRef.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        feedback.dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putDouble("distanceInMe", distanceInMe);
        savedInstanceState.putBoolean("running", running);
        savedInstanceState.putDouble("distanceForSpeed", distanceForSpeed);
        savedInstanceState.putInt("totalTime", totalTime);
        savedInstanceState.putInt("threadTimer", threadTimer);
        savedInstanceState.putDouble("speed", speed);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
            }
        });
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    ) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, RC_FINE);
                return;
            }
        }
        map.setMyLocationEnabled(true);    //for location
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        updateUI(mLastLocation);

        createLocationRequest();
        startLocationUpdates();

        if (mLastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, "no geocoder available", Toast.LENGTH_LONG).show();
                return;
            }
            startIntentService();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        updateUI(location);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Snackbar.make(fab, "connected failed", Snackbar.LENGTH_LONG).show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_Activity) {
//            startActivity(new Intent(StartTrackActivity.this, StartTrackActivity.class));   // Activity already open
        } else if (id == R.id.nav_Profile) {
            startActivity(new Intent(StartTrackActivity.this, ActivityProfile.class));   // Handle the PROFILE action
        } else if (id == R.id.nav_Bmi) {
            startActivity(new Intent(StartTrackActivity.this, BMIActivity.class));   // Handle the BMI action
        } else if (id == R.id.nav_History) {
            startActivity(new Intent(StartTrackActivity.this, HistoryActivity.class));   // Handle the BMI action
        } else if (id == R.id.nav_Alarm) {
            startActivity(new Intent(StartTrackActivity.this, AlarmListActivity.class));   // Handle the BMI action
        } else if (id == R.id.nav_Chat) {
            startActivity(new Intent(StartTrackActivity.this, UserListActivity.class));   // Handle the Chat action
        } else if (id == R.id.nav_Setting) {
            startActivity(new Intent(StartTrackActivity.this, SettingActivity.class));   // Handle the Setting action
        } else if (id == R.id.nav_Feedback) {
            feedback.show();
        } else if (id == R.id.nav_Signout) {
            logout();
        } else if (id == R.id.nav_Blog) {
            startActivity(new Intent(StartTrackActivity.this, BlogActivity.class));   // Handle the BMI action

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void navigateToLogin() {

        // Go to LogIn screen
        Intent intent = new Intent(this, ViewPagerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // LoginActivity is a New Task
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // The old task when coming back to this activity should be cleared so we cannot come back to it.
        startActivity(intent);
    }

    /*Show and hide progress bar*/
    private void showProgressBarForUsers() {
        mProgressBarForUsers.setVisibility(View.VISIBLE);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .addApi(LocationServices.API)
                .build();
    }

    private void updateUI(Location location) {
        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            long time = location.getTime();
            CharSequence formattedTime = DateFormat.format("dd-MM-yy hh:mm:ss", time);
            textLocation.setText(lat + " , " + lng + "\n" + formattedTime);
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 18f);
            map.animateCamera(update);

            MarkerOptions options = new MarkerOptions();
            options.title("here i am");
            options.position(latLng);
            map.addMarker(options);
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (LocationListener) this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (LocationListener) this
        );
    }

    public void fetchAddressButtonHandler() {
        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            startIntentService();
        }
    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    private class AddressResultReceiver extends ResultReceiver {
        private String mAddressOutput;

        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);
            textAddress.setText(mAddressOutput);
        }
    }

    private final android.location.LocationListener listener = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (running) {
                if (lastLocation == null)   //this is done to avoid app crashing if LOCATION is OFF and then STARTED...as lastLOCATIon would be null
                //so calling .dostanceTo()method would cause and error,,,,lastlocation is set to location as this method only gets called where there is a change so of course it is not null now
                {
                    lastLocation = location;
                }
                flag = true;
                lat = location.getLatitude();   //updating latitudes and longitudes
                lng = location.getLongitude();

                distanceInMe = distanceInMe + location.distanceTo(lastLocation);  //updating distance
                if (location.distanceTo(lastLocation) == 0)   //if i am not moving/standing at a place then set distance to 0 so speed becomes ~0
                {
                    distanceForSpeed = 0;
                }
                distanceForSpeed = distanceForSpeed + location.distanceTo(lastLocation);  //updating distance for speed
                lastLocation = location;

                calories = (float) ((0.0215 * ((distanceInMe / (1000 * calcHour)) * (distanceInMe / (1000 * calcHour)) * (distanceInMe / (1000 * calcHour))) - 0.1765 * ((distanceInMe / (1000 * calcHour)) * (distanceInMe / (1000 * calcHour))) + 0.8710 * (((distanceInMe / (1000 * calcHour))) + 1.4577) * weight * calcHour));
                if (distanceInMe > 100) {
                    calories = 0;
                }
                actualCalories.setText(String.valueOf(calories));


            }
            // setLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    public void oneGiantButLightThread() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!flag) {  //flag inside onCreate
                        allStats = "LOCATION NOT AVAILABLE, PLEASE TURN ON THE GPS IF ITS OFF";
                    } else {
                        list = geocoder.getFromLocation(lat, lng, 1);
                        address = list.get(0);
                        allStats = "Country code: " + address.getCountryCode() + "\nCountry Name: " + address.getCountryName() +
                                "\nAddress: " + address.getAddressLine(0) + ", " + address.getAddressLine(1) + "\nState: " + address.getAdminArea() +
                                "\nLocality: " + address.getSubLocality();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // Toast.makeText(StartTrackActivity.this, "Exception " + e, Toast.LENGTH_SHORT).show();
                }
                int hours = threadTimer / 3600;
                int minutes = (threadTimer % 3600) / 60;
                int secs = threadTimer % 60;
                String time = String.format("%d:%02d:%02d", hours, minutes, secs);
                //threadView.setText(time);
                if (running) {
                    threadTimer++;
                    totalTime++;
                }
                myDistance();  //setting distance
                mySpeed();           //for setting speed
                handler.postDelayed(this, 1000);  //runs thread with 1 sec gap

            }
        });
    }

    public void onToggleClick(View view) {
        boolean on = ((ToggleButton) view).isChecked();
        if (on) {
            running = true;
        } else {
            running = false;
            totalTime = 0;
            distanceForSpeed = 0;
            speedView.setText("0.00 m/s");
            mChronometer.stop();
        }
    }

    public void myDistance() {
        distance = String.format("%.2f KM", distanceInMe / 1000); // distanceInMe=in km*1000
        myDistance.setText(distance);


//        double disd = distanceInMe * 0.5;
//        int dis = (int) disd;
//        double steps = weight / 3500 * dis;

//        CB = [0.0171 x KPH3 - 0.1062 x KPH2 + 0.6080 x KPH + 1.8600] x WKG x T;

//        actualCalories.setText(String.format("%.2f", steps));

    }

    public void mySpeed() {
        String speedy;

        if (distanceForSpeed == 0) {
            speedy = "0.0 m/s";
        } else {
            speed = distanceForSpeed / totalTime;
            speedy = String.format("%.2f m/s", speed);
        }
        speedView.setText(speedy);
    }

    public void onActivitySpopped() {
        loadInterstitial();
        Intent onActivitySpopped = new Intent(this, ResultActivity.class);
        onActivitySpopped.putExtra(ResultActivity.SPEED, resultSpeed);
        onActivitySpopped.putExtra(ResultActivity.CALORIE, calorie);
        onActivitySpopped.putExtra(ResultActivity.DISTANCE, distance);
        onActivitySpopped.putExtra(ResultActivity.TIME, time);
        startActivity(onActivitySpopped);


    }

    private void logout() {
        if (mAuth != null) {
            /* Logout */
            // Finish token
            // mFirebaseChatRef.unauth();
            /* Update authenticated user and show login screen */
            mAuth.signOut();
        }
    }

    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
//				mNextLevelButton.setEnabled(true);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
//				mNextLevelButton.setEnabled(true);
            }

            @Override
            public void onAdClosed() {
                // Proceed to the next level.
//				goToNextLevel();
            }
        });
        return interstitialAd;
    }

    private void loadInterstitial() {
        // Disable the next level button and load the ad.
//		mNextLevelButton.setEnabled(false);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        mInterstitialAd.loadAd(adRequest);
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_FINE) {
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    askPermissions();
                }
            }

        }

    }

    private void askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    ) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, RC_FINE);
                return;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
        stopLocationUpdates();
    }
}


