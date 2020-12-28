package com.example.beaconbattery.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beaconbattery.data.EndTimeData;
import com.example.beaconbattery.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> {
    private ArrayList<EndTimeData> mList;
    private SimpleDateFormat simpleDataFormat = new SimpleDateFormat("MM/dd HH:mm:ss", Locale.KOREAN);

    public class RecyclerViewHolder extends RecyclerView.ViewHolder{
        protected TextView macName;
        protected TextView name;
        protected TextView endTime;
        protected TextView lastTime;

        public RecyclerViewHolder(View view){
            super(view);
            this.macName = view.findViewById(R.id.mac_Name);
            this.name = view.findViewById(R.id.no_sensing_name);
            this.lastTime = view.findViewById(R.id.last_sensing_tv);
            this.endTime = view.findViewById(R.id.end_Time);
        }
    }

    public RecyclerAdapter(ArrayList<EndTimeData> list){
        this.mList = list;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item_layout, viewGroup, false);
        
        RecyclerViewHolder viewHolder = new RecyclerViewHolder(view);
        return viewHolder;
    }
    
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder viewHolder, int position){
        String strTime = simpleDataFormat.format(mList.get(position).getFirstTime());
        String lastTimeStr = simpleDataFormat.format(mList.get(position).getLastTime());
        long diffTime = mList.get(position).getEndTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffTime);
        long hours = TimeUnit.MINUTES.toHours(minutes);
        long remainMinutes = minutes - TimeUnit.HOURS.toMinutes(hours);
        String result = String.format("%d시간 %d분", hours, remainMinutes);

/*        // 직접 계산해서 시-분 구하면
        int calMin = (int) ((diffTime/(1000*60)))%60;
        int calHour = (int) ((diffTime/(1000*60*60)) % 24);
        //String calResult = String.format("%02d", calHour) + ":"+String.format("%02d", calRemain);
        Log.v("Jiwon_secLog", calMin + ": " +calHour );
*/

        viewHolder.macName.setText(strTime);
        viewHolder.lastTime.setText("("+lastTimeStr+")");
        viewHolder.name.setText(mList.get(position).getName());
        viewHolder.endTime.setText(result);
    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }


}
