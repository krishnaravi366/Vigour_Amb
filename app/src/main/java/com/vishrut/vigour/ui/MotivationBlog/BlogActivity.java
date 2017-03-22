package com.vishrut.vigour.ui.MotivationBlog;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.vishrut.vigour.FireBase.ReferenceUrl;
import com.vishrut.vigour.R;

import java.util.ArrayList;

public class BlogActivity extends AppCompatActivity {

    public static final String TWEETS = "post";
    private Firebase firebase;
    private ListView lvTweet;
    private ProgressBar bar;
    ArrayList<String> allTweets;
    private InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_main);

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        mInterstitialAd = newInterstitialAd();
        loadInterstitial();
        showInterstitial();
        mInterstitialAd.show();

      //  Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        lvTweet = (ListView) findViewById(R.id.lvTweets);
        final EditText etTweet = (EditText) findViewById(R.id.etTweet);
        bar = (ProgressBar) findViewById(R.id.progressBar);
        //setSupportActionBar(toolbar);
        firebase = new Firebase(ReferenceUrl.FIREBASE_CHAT_URL);

//        firebase.getDefaultConfig().setPersistenceEnabled(true);

//        Firebase blogRef=new Firebase("https://vigourapp.firebaseio.com/post");
//		blogRef.keepSynced(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        allTweets = new ArrayList<>();

        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String newTweet = etTweet.getText().toString();
                    if (!newTweet.isEmpty()) {
                        updateFireBase(newTweet);
                        etTweet.setText("");

                    }
                }
            });
        }
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, allTweets);
        lvTweet.setAdapter(adapter);
        firebase.child(TWEETS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allTweets.clear();

                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    allTweets.add(child.getValue().toString());
                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Snackbar.make(lvTweet, firebaseError.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void updateFireBase(String tweet) {
        bar.setVisibility(View.VISIBLE);
        Firebase tweets = firebase.child(TWEETS).push();
        tweets.setValue(tweet, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError == null) {
//                    Toast.makeText(BlogActivity.this, "...", Toast.LENGTH_SHORT).show();
                } else {
                }
                Toast.makeText(BlogActivity.this, "failed to tweet", Toast.LENGTH_SHORT).show();

                bar.setVisibility(View.GONE);
            }
        });
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
