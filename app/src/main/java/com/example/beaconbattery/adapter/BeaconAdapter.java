package com.example.beaconbattery.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.beaconbattery.data.Beacon;
import com.example.beaconbattery.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class BeaconAdapter extends BaseAdapter {
    private ArrayList<Beacon> beacons;
    private LayoutInflater layoutInflater;
    private SimpleDateFormat simpleDataFormat = new SimpleDateFormat("MM/dd HH:mm:ss SS", Locale.KOREAN);

    public BeaconAdapter(ArrayList<Beacon> beacons, LayoutInflater layoutInflater) {
        this.beacons = beacons;
        this.layoutInflater = layoutInflater;
    }

    @Override
    public int getCount() {
        return beacons.size();
    }

    @Override
    public Object getItem(int position) {
        return beacons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BeaconHolder beaconHolder;
        if(convertView == null){
            beaconHolder = new BeaconHolder();
            convertView = layoutInflater.inflate(R.layout.item_layout, parent, false);
            beaconHolder.address = convertView.findViewById(R.id.address_tv);
            beaconHolder.name = convertView.findViewById(R.id.name_tv);
            beaconHolder.time = convertView.findViewById(R.id.time_tv);
            convertView.setTag(beaconHolder);

        }else {
            beaconHolder = (BeaconHolder)convertView.getTag();
        }

        String str_time = simpleDataFormat.format(beacons.get(position).getNow());
        beaconHolder.time.setText(str_time);
        beaconHolder.address.setText(beacons.get(position).getAddress());
        beaconHolder.name.setText(beacons.get(position).getName());
        return convertView;
    }
    private class BeaconHolder {
        TextView address;
        TextView name;
        TextView time;

    }
}
