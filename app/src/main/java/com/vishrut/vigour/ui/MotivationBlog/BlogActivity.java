package com.vishrut.vigour.ui.MotivationBlog;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vishrut.vigour.FireBase.ReferenceUrl;
import com.vishrut.vigour.R;

import java.util.ArrayList;

public class BlogActivity extends AppCompatActivity {

    public static final String TWEETS = "post";
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference reference;
    private ListView lvTweet;
    private ProgressBar bar;
    ArrayList<String> allTweets;
    private InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_main);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
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
        reference = mDatabase.getReference();

//        reference.getDefaultConfig().setPersistenceEnabled(true);

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
        reference.child(TWEETS).addValueEventListener(new ValueEventListener() {
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
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateFireBase(String tweet) {
        bar.setVisibility(View.VISIBLE);
        DatabaseReference push = reference.child(TWEETS).push();
        push.setValue(tweet, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
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
