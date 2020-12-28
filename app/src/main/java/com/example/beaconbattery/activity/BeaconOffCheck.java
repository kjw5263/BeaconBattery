package com.example.beaconbattery.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.example.beaconbattery.R;
import com.example.beaconbattery.adapter.RecyclerAdapter;
import com.example.beaconbattery.data.EndTimeData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class BeaconOffCheck extends AppCompatActivity {
    private ArrayList<EndTimeData> mArrayList;
    private RecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private SharedPreferences sp;
    private int count = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_off_check);
        mArrayList = new ArrayList<>();
         mRecyclerView = findViewById(R.id.recycler);
         mLinearLayoutManager = new LinearLayoutManager(this);

         mRecyclerView.setLayoutManager(mLinearLayoutManager);

         mAdapter = new RecyclerAdapter(mArrayList);
         mRecyclerView.setAdapter(mAdapter);

        sp = getSharedPreferences("TimeData", Activity.MODE_PRIVATE);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), mLinearLayoutManager.getOrientation());

        for(int i = 0; i< MainActivity.mArrayList.size(); i++){
            String addr = MainActivity.mArrayList.get(i).getAddress();
            String user = MainActivity.mArrayList.get(i).getName();
            Long time = MainActivity.mArrayList.get(i).getNow();

           String addrSecond = addr + "second";
           long firstSensingTime = sp.getLong(addrSecond, 0);
           long intervalTime = time - firstSensingTime;

           Log.v("Jiwon_DifferenceTime", addrSecond + ">>>>" + intervalTime);

            mArrayList.add(new EndTimeData(firstSensingTime, time, user, intervalTime));
        }
        mAdapter.notifyDataSetChanged();
    }
}