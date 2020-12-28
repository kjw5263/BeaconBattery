package com.example.beaconbattery.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;


import com.example.beaconbattery.R;
import com.example.beaconbattery.adapter.BeaconAdapter;
import com.example.beaconbattery.data.Beacon;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button resetBtn;
    private EditText resetEt;
    public static int CHECK_INTERVAL = 10200;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private HashMap<String, Object> inputData;

    public BluetoothDevice bluetooth_device;
    public static ScanCallback scan_callback;
    public static ScanSettings scan_settings;

    // 비콘+이름 배열
    private ArrayList<HashMap> array;

    // 비콘+이름+시간 담는 배열
    private ArrayList<Beacon> beaconArray;
    private ArrayList<Beacon> noBeaconArray;

    // BeaconOffCheck 액티비티에 사용될 감지안됨 배열
    public static ArrayList<Beacon> mArrayList;

    // Adapter
    private BeaconAdapter beaconAdapter;
    private BeaconAdapter noBeaconAdapter;

    // View
    private ListView lv;
    private ListView lv2;
    private Button setBtn;
    private EditText minuteEt;
    private LinearLayout noSensingBar;

    // SharedPreferences
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    // 스캔 필터
    List<ScanFilter> scanFilters  = new ArrayList<>();
    ScanSettings.Builder mScanSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 위치 권한 허용
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("위치 권한을 허용해주세요.")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
    }

    // 퍼미션 리스너
    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            // 위치권한 허용 시 스캔 실행
            init();
            setFunction();
            mBluetoothLeScanner.startScan(scanFilters, scan_settings, scan_callback);
            Toast.makeText(MainActivity.this, "위치 권한 허용됨", Toast.LENGTH_SHORT).show();
            //mBluetoothLeScanner.startScan(scanFilters, scan_settings, scan_callback);
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            finish();
        }
    };

    // 뷰 초기화
    private void init() {
        lv = findViewById(R.id.beacon_lv);
        lv2 = findViewById(R.id.noBeacon_lv);
        setBtn = findViewById(R.id.set_btn);
        minuteEt = findViewById(R.id.minute_et);
        noSensingBar = findViewById(R.id.no_sensing_bar);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

        array = new ArrayList<>();
        beaconArray = new ArrayList<>();
        noBeaconArray = new ArrayList<>();
        mArrayList = new ArrayList<>();

        sp = getSharedPreferences("TimeData", Activity.MODE_PRIVATE);
        editor = sp.edit();

        // 스캔 모드 세팅 - 계속(약 2초 주기) 스캔 하도록 세팅
        scan_settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(0).build();
        ScanFilter.Builder scanFilter = new ScanFilter.Builder();

        // 시간 입력한 내역 있을 때, EditText 에 나타내기
        if( sp.getLong("Check_Time", 0) != 0){
            minuteEt.setText(String.valueOf(sp.getInt("InputTime", 0)));
        }


        // 비콘 목록 읽어오기
        String json = "";
        try {
            InputStream is = getAssets().open("TeamBeacon.json");

            int fileSize = is.available();
            byte[] buffer = new byte[fileSize];
            is.read(buffer);
            is.close();

            json = new String(buffer, "UTF-8");
            Log.v("Jiwon_BeaconList", "Json String >>>>>>>> " + json);

            JSONArray jsonArray = new JSONArray(json);
            for(int i=0; i<jsonArray.length(); i++){
                long time = System.currentTimeMillis();

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String MAC = jsonObject.getString("MAC");
                String name = jsonObject.getString("name");
                inputData = new HashMap<>();
                inputData.put("MAC", MAC);
                inputData.put("name", name);
                array.add(inputData);
                Log.v("Jiwon_HashMap",  i + "번째 >>>>>" +array.get(i).get("MAC"));

                //특정 비콘 스캔하도록 설정
                //scanFilter.setDeviceAddress(MAC); //ex) 00:00:00:00:00:00
                ScanFilter scan = scanFilter.build();
                //scanFilters.add(scan);

                // 앱 처음 실행 될 때 MAC 주소에 시간 저장된 값 없으면, 감지안됨 리스트에 넣기
                if(sp.getLong(MAC, 0) ==0){
                    noBeaconArray.add(new Beacon(MAC, name,0));
                }
                // 앱 처음 이후 실행일 때,
                else {
                    // MAC 주소에 시간 저장된 거 있으면 -> 사용한 적 있음
                    if( sp.getLong("Check_Time", 0) == 0){
                        // 시간 설정한 내역 없으면, 기본 설정시간
                        noBeaconArray.add(new Beacon(MAC, name, sp.getLong(MAC, 0)));
                    } else {
                        // 시간 설정한 내역 있고, 설정한 시간텀 보다 길때는 감지안됨 리스트에 넣기
                        if(time - sp.getLong(MAC, 0) >= sp.getLong("Check_Time", 0)){
                            noBeaconArray.add(new Beacon(MAC, name, sp.getLong(MAC, 0)));
                        }
                        // 시간 설정한 내역 있고, 설정한 시간텀 아직 안지났을 때 감지됨 리스트에 넣기
                        else {
                            beaconArray.add(new Beacon(MAC, name, sp.getLong(MAC, 0)));
                            beaconAdapter = new BeaconAdapter(beaconArray, getLayoutInflater());
                            lv.setAdapter(beaconAdapter);
                            beaconAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        noBeaconAdapter = new BeaconAdapter(noBeaconArray, getLayoutInflater());
        lv2.setAdapter(noBeaconAdapter);
        noBeaconAdapter.notifyDataSetChanged();

        // 시간 설정 버튼
        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(minuteEt.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "시간을 설정하세요", Toast.LENGTH_SHORT).show();
                } else {
                    try{

                        int inputInt = Integer.parseInt(minuteEt.getText().toString());
                        if(inputInt != 0){
                            int setTime = inputInt * 60000;
                            editor.putInt("InputTime", inputInt);
                            editor.putLong("Check_Time", setTime);
                            editor.apply();
                            Toast.makeText(MainActivity.this, inputInt + "분으로 설정되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            editor.putLong("Check_Time", 0);
                            editor.apply();
                            Toast.makeText(MainActivity.this, "10초로 초기화 되었습니다.", Toast.LENGTH_SHORT).show();
                        }

                    } catch (NumberFormatException e){
                        Toast.makeText(MainActivity.this, "숫자만 입력하세요", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        noSensingBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BeaconOffCheck.class);
                mArrayList.clear();
                for(int i=0; i<noBeaconArray.size(); i++){
                    mArrayList.add(noBeaconArray.get(i));
                }
                startActivity(intent);
            }
        });

    }

    private void setFunction() {
        scan_callback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                bluetooth_device = result.getDevice();
                if(bluetooth_device == null ){
                    Log.v("Jiwon_DeviceNull", ">> Device Null");
                    beaconArray.clear();
                }


                // 하나의 비콘을 스캔 해온다
                String addr = bluetooth_device.getAddress();
                String enterTime = addr + "second";

                long time = System.currentTimeMillis();
                Log.v("Jiwon_SensingDeviceMac", addr);

                // 최초로 감지된 시간 저장하기
                if(sp.getLong(enterTime, 0) == 0) {
                    editor.putLong(enterTime, time);
                    editor.commit();

                    Log.v("Test_FirstSensingMac", enterTime + sp.getLong(enterTime, 0) +"");
                }

                for(int i=0; i<noBeaconArray.size(); i++){
                    // 스캔한 비콘이, 인식안됨 리스트에 일치하는게 있을때
                    if( noBeaconArray.get(i).getAddress().equals(addr)){
                        beaconArray.add(new Beacon(addr, noBeaconArray.get(i).getName(),time));
                        noBeaconArray.remove(i);

                        beaconAdapter = new BeaconAdapter(beaconArray, getLayoutInflater());
                        lv.setAdapter(beaconAdapter);

                        if( beaconArray.isEmpty() == false )
                            lv.setSelection(beaconArray.size());

                        beaconAdapter.notifyDataSetChanged();
                        noBeaconAdapter.notifyDataSetChanged();
                    }
                }


                // 인식됨 (lv) 리스트
                for(int j=0; j<beaconArray.size(); j++){
                    // 인식된 비콘이 인식됨(lv)리스트에 있을 경우 시간 갱신
                    if( beaconArray.get(j).getAddress().equals(addr) ){
                        beaconArray.get(j).setNow(time);
                        editor.putLong(addr, time);
                        editor.apply();
                        beaconAdapter.notifyDataSetChanged();
                    }

                    // 마지막으로 인식된 시간이 CHECK_INTERVAL (milliSec 단위) 경과했을 경우 인식됨(lv)리스트에서 삭제, 인식안됨(lv2)리스트에 추가]
                    if(sp.getLong("Check_Time", 0) == 0){
                        if( System.currentTimeMillis() - beaconArray.get(j).getNow() >= CHECK_INTERVAL ){
                            noBeaconArray.add(0, new Beacon(beaconArray.get(j).getAddress(), beaconArray.get(j).getName(), beaconArray.get(j).getNow()));
                            // sharedPreferences 에 마지막으로 감지된 시간 저장
                            editor.putLong(beaconArray.get(j).getAddress(), beaconArray.get(j).getNow());
                            editor.apply();
                            Log.v("Jiwon_LastSensingTime", beaconArray.get(j).getAddress() +" >>>>>>>>>> " +sp.getLong(beaconArray.get(j).getAddress(), 0));

                            beaconArray.remove(j);
                            beaconAdapter.notifyDataSetChanged();
                            noBeaconAdapter.notifyDataSetChanged();
                        }
                    } else {
                        long savedInterval = sp.getLong("Check_Time", 0);
                        if( System.currentTimeMillis() - beaconArray.get(j).getNow() >= savedInterval ){
                            noBeaconArray.add(0, new Beacon(beaconArray.get(j).getAddress(), beaconArray.get(j).getName(), beaconArray.get(j).getNow()));
                            editor.putLong(beaconArray.get(j).getAddress(), beaconArray.get(j).getNow());
                            editor.apply();
                            Log.v("Jiwon_LastSensingTime", beaconArray.get(j).getAddress() +" >>>>>>>>>> " +sp.getLong(beaconArray.get(j).getAddress(), 0));

                            beaconArray.remove(j);
                            beaconAdapter.notifyDataSetChanged();
                            noBeaconAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d("onBatchScanResults", results.size() + "");
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d("onScanFailed()", errorCode+"");
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(scan_callback != null) {
            mBluetoothLeScanner.stopScan(scan_callback);
        }
    }
}