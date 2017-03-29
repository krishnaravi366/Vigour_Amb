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


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vishrut.vigour.R;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private DatabaseReference mFirebaseRef;
    private String userUid;
    private TextView result;
    public static final String TWEETS = "CalorieBurn";
    private ListView resultlv;
    private ArrayList resultArray;
    private ResultAdapter adapter;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);


        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        userUid = mAuth.getCurrentUser().getUid();
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

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HistoryActivity.this, "History failed to load", Toast.LENGTH_SHORT).show();
            }
        });

        // Attach an listener to read the data at our posts reference


    }


    private class ResultAdapter extends BaseAdapter {

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


}
