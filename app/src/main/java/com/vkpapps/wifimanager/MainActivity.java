package com.vkpapps.wifimanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.vkpapps.apmanager.APManager;
import com.vkpapps.apmanager.DefaultFailureListener;
import com.vkpapps.services.MyService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements APManager.OnSuccessListener {

    private static final int PERMISSION_REQUESTS = 1;
    private static final int ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION = 110;
    private int GO2OpenBluetooth=220;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        findViewById(R.id.btnTurnOn).setOnClickListener(v -> {
            //获取权限
            if (allPermissionsGranted()) {

                creatHotspot();

            } else {
                getRuntimePermissions();
            }

        });


        findViewById(R.id.btnBle).setOnClickListener(v -> {

            //获取权限
            if (allPermissionsGranted()) {
                go2BleAct();
            } else {
                getRuntimePermissions();
            }


        });



        findViewById(R.id.btnConnectWifi).setOnClickListener(v -> {


            //获取权限
            if (allPermissionsGranted()) {
                go2ConnectWifi();
            } else {
                getRuntimePermissions();
            }


        });


        findViewById(R.id.btnConnectWifiAuto).setOnClickListener(v -> {


            //获取权限
            if (allPermissionsGranted()) {
                go2ConnectWifiAuto();
            } else {
                getRuntimePermissions();
            }


        });


    }

    private void creatHotspot(){

        // 启动service
        Intent mIntent=new Intent(MainActivity.this, MyService.class) ;
        startService(mIntent);

        BluetoothManager systemService = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = systemService.getAdapter();

        if(!mBluetoothAdapter.isEnabled()){
            Toast.makeText(getApplicationContext(),"请先打开蓝牙",Toast.LENGTH_SHORT).show();
            Intent enabler =new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler,GO2OpenBluetooth);
            return;
        }

        APManager apManager = APManager.getApManager(this);
        apManager.turnOnHotspot(this,
                this,
                new DefaultFailureListener(this)
        );


    }

    private void go2ConnectWifiAuto(){
        startActivity(new Intent(MainActivity.this,ConnectWifiAutoActivity.class));
    }

    private void go2ConnectWifi(){
        startActivity(new Intent(MainActivity.this,ConnectWifiActivity.class));
    }

    private void go2BleAct(){
        startActivity(new Intent(MainActivity.this, BleBroadcastActivity.class));
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        String TAG="getRuntimePermissions";
        Log.i(TAG, "allNeededPermissions: " + allNeededPermissions);

        if (!allNeededPermissions.isEmpty()) {
            String[] strings = allNeededPermissions.toArray(new String[0]);
            Log.i(TAG, "allNeededPermissions.toArray(new String[0]): " + Arrays.toString(strings));
            ActivityCompat.requestPermissions(
                    this, strings, PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onSuccess(@NonNull String ssid, @NonNull String password) {
        Toast.makeText(this, ssid + "," + password, Toast.LENGTH_LONG).show();
        WifiPasswordHolder.getInstance().setPassword(APManager.getApManager(this).getPassword());
        //startActivity(new Intent(this, APDetailActivity.class));
        startActivity(new Intent(this, BleBroadcastActivity.class));
    }


    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        String TAG="isPermissionGranted";
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }



    private String[] getRequiredPermissions() {
        try {

            String[] result=null;
           // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            result= new String[]{
                    "android.permission.ACCESS_NETWORK_STATE",
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION",
                    "android.permission.BLUETOOTH",
                    "android.permission.BLUETOOTH_ADMIN",

            };

            Log.i("getRequiredPermissions", Arrays.toString(result));

            return result;

        } catch (Exception e) {
            return new String[0];
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(GO2OpenBluetooth==requestCode){
            creatHotspot();
        }
    }
}