package com.vishrut.vigour.ui.History;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class HistoryActivity extends AppCompatActivity {

    private Firebase mFirebaseRef;
    private String userUid;
    private TextView result;
    public static final String TWEETS = "CalorieBurn";
    private ListView resultlv;
    private ArrayList resultArray;
    private ResultAdapter adapter;
    private ProgressBar progressBar;
    private InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        mInterstitialAd = newInterstitialAd();
        loadInterstitial();
        showInterstitial();
        mInterstitialAd.show();


        mFirebaseRef = new Firebase(ReferenceUrl.FIREBASE_CHAT_URL);
        userUid = mFirebaseRef.getAuth().getUid();
        resultlv = (ListView) findViewById(R.id.lvHistory);
        progressBar = (ProgressBar) findViewById(R.id.progressBarHistory);
//        ResultAdapter adapter=new ResultAdapter();
        final ArrayList<ResultList> resultLists = new ArrayList<>();
        mFirebaseRef.child(TWEETS).child(userUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                resultLists.clear();

                for (DataSnapshot child : snapshot.getChildren()) { //date
                    String date = child.getKey();
                    for (DataSnapshot dataSnapshot : child.getChildren()) { //time
                        String time = dataSnapshot.getKey();
//                        String calorieBurn = dataSnapshot.child(time).child("Calorie Burn").getValue();
                        String value = null;
                        String distance = null;

                        for (DataSnapshot childcal : dataSnapshot.getChildren()) { //calorie
                            String key = childcal.getKey();
                            if (key == "Calorie Burn") {
//                                Log.e("DATA",childcal.toString());
                                value = String.valueOf(childcal.getValue());


                            }
                            if (key == "Distance") {
                                distance = String.valueOf(childcal.getValue());


                            }
//                            resultLists.add(new ResultList(date, time, value, distance));

                        }
                        resultLists.add(new ResultList(date, time, value, distance));

//                        String calorieBurn = dataSnapshot.child(time).child("Calorie Burn").toString();
//                        resultLists.add(new ResultList(date, time, calorieBurn));
                    }
                }
                adapter = new ResultAdapter(resultLists);
                progressBar.setVisibility(View.GONE);
                resultlv.setAdapter(adapter);

//                resultLists.add(child.getValue().toString());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

        // Attach an listener to read the data at our posts reference


    }


    class ResultAdapter extends BaseAdapter {

        private final ArrayList<ResultList> resultLists;

        public ResultAdapter(ArrayList<ResultList> resultLists) {
            this.resultLists = resultLists;
        }

        @Override
        public int getCount() {
            return resultLists.size();
        }

        @Override
        public Object getItem(int position) {
            return resultLists.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;  // for database
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_result_layout, parent // parent already persent
                        , false);
            }
            ResultList snapshot = resultLists.get(position);

            TextView time = (TextView) convertView.findViewById(R.id.resultTime);
            TextView date = (TextView) convertView.findViewById(R.id.resultDate);
            TextView calorie = (TextView) convertView.findViewById(R.id.resultCalorie);
            TextView distance = (TextView) convertView.findViewById(R.id.resultDistance);

            date.setText(resultLists.get(position).Date);
            time.setText(resultLists.get(position).Time);
            calorie.setText(resultLists.get(position).Calorie);
            distance.setText(resultLists.get(position).Distance);


            return convertView;
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
