package com.vishrut.vigour.ui.Tracking;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.vishrut.vigour.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ResultActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {

    private TextView resultSpeed;
    private TextView resultTime;
    private TextView resultDistance;
    private TextView resultCalorie;
    public static final String TIME = "O0HH 00MM 00SS";
    public static final String SPEED = "O M/S";
    public static final String DISTANCE = "O KM";
    public static final String CALORIE = "O C";
    private float shareDistance;
    private float shareTime;
    private float shareSpeed;
    private float shareCalorie;
    private String gotDistance;
    private String gotTime;
    private String gotCalorie;
    private DatabaseReference mFirebaseRef;

    private FloatingActionButton fabspeak;

    private TextToSpeech tts;
    private TextView ran;
    private TextView in;
    private TextView at;
    private TextView and;

    private InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);


        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        mInterstitialAd = newInterstitialAd();
        loadInterstitial();
        showInterstitial();

        ran = (TextView) findViewById(R.id.result_ran);
        resultDistance = (TextView) findViewById(R.id.result_distance);
        in = (TextView) findViewById(R.id.result_in);
        resultTime = (TextView) findViewById(R.id.result_time);
        at = (TextView) findViewById(R.id.result_at);
        resultSpeed = (TextView) findViewById(R.id.result_speed);
        and = (TextView) findViewById(R.id.result_and);
        resultCalorie = (TextView) findViewById(R.id.result_calorie);

        FloatingActionButton fabshare = (FloatingActionButton) findViewById(R.id.fabshare);
//        fabspeak = (FloatingActionButton) findViewById(R.id.fabspeak);

        mFirebaseRef =FirebaseDatabase.getInstance().getReference();



        //Get or Generate Date
        Date todayDate = new Date();
        //Get an instance of the formatter
//        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        //If you want to show only the date then you will use
        DateFormat dateFormat = DateFormat.getDateInstance();           //Only Date
        //Format date
        final String todayDateTimeString = dateFormat.format(todayDate);

        Calendar cal = Calendar.getInstance();
        Date currentDate = cal.getTime();                               //For Date
        DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        String formattedDateString = formatter.format(currentDate);

        Calendar calender = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
        Date currentLocalTime = calender.getTime();
        DateFormat date = new SimpleDateFormat("HH:mm:ss a");// you can get seconds by adding  "...:ss" to it
        date.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
        final String localTime = date.format(currentLocalTime);                       //For time

        final String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CalorieData data=new CalorieData(gotDistance,gotTime,gotCalorie);

        mFirebaseRef.child("CalorieBurn").child(userUid).push().setValue(data, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                mFirebaseRef.child("CalorieBurn").child(userUid).child(todayDateTimeString).child(localTime).child("Calorie Burn").setValue(gotCalorie);
                mFirebaseRef.child("CalorieBurn").child(userUid).child(todayDateTimeString).child(localTime).child("Distance").setValue(gotDistance);
                mFirebaseRef.child("CalorieBurn").child(userUid).child(todayDateTimeString).child(localTime).child("Time").setValue(gotTime);

            }
        });

        fabshare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "Hey! I found a amazing fitness app. Its name is VIGOUR. " +
                                " \n I ran " + gotDistance + " km in " + gotTime + " and burned " + gotCalorie + " with a the help of this VIGOUR app. " +
                                " \n I recommend it for you also. " +
                                " \n You can also download it from Google Play Store." +
                                " \n It is a great app you'll love it.");  // " +     + "
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        Intent onActivitySpopped = getIntent();
        if (onActivitySpopped != null) {
            String recievedTime = onActivitySpopped.getStringExtra(TIME);
            String recievedDistance = onActivitySpopped.getStringExtra(DISTANCE);
            String recievedSpeed = onActivitySpopped.getStringExtra(SPEED);
            String recievedCalorie = onActivitySpopped.getStringExtra(CALORIE);

            resultDistance.setText(recievedDistance);
            resultTime.setText(recievedTime);
            resultSpeed.setText(recievedSpeed);
            resultCalorie.setText(recievedCalorie);

            gotDistance = recievedDistance;
            gotTime = recievedTime;
            gotCalorie = recievedCalorie;
        }

//        shareDistance = Float.parseFloat(gotDistance);
//        shareTime = Float.parseFloat(gotTime);
//        shareSpeed = shareDistance / shareTime;
//
////        float speed=shareDistance/shareTime;
//        resultSpeed.setText((int) shareSpeed +"m/s");

        tts = new TextToSpeech(this, this);
        speakOut();

//        fabspeak.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                speakOut();
//
//            }
//
//        });


    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
//                fabspeak.setEnabled(true);
            } else {
                speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    private void speakOut() {

        String a = ran.getText().toString();
        String b = resultDistance.getText().toString();
        String c = in.getText().toString();
        String d = resultTime.getText().toString();
        String e = at.getText().toString();
        String f = resultSpeed.getText().toString();
        String g = and.getText().toString();
        String h = resultCalorie.getText().toString();

        String speech= a+b+c+d+e+f+g+h;

        tts.speak(speech, TextToSpeech.QUEUE_FLUSH, null);

    }



    @Override
    public void onUtteranceCompleted(String utteranceId) {

    }
    public void onPause(){
        if(tts !=null){
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
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
