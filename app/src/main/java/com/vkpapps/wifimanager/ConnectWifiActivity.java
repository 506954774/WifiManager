package com.vkpapps.wifimanager;

/**
 * ConnectWifiActivity
 * Created By:Chuck
 * Des:
 * on 2022/11/15 11:30
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.MacAddress;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;

public class ConnectWifiActivity extends Activity {
    private List<ScanResult> wifiList;
    private WifiManager wifiManager;
    private List<String> passableHotsPot;
    private WifiReceiver wifiReceiver;
    private boolean isConnected = false;
    private AppCompatTextView txt_wifi;
    private AppCompatEditText et_pwd;
    private AppCompatButton btn_connect;
    private AppCompatButton btn_connect_android10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    /* 初始化参数 */
    public void init() {
        setContentView(R.layout.activity_wifi_connect);
        txt_wifi = (AppCompatTextView) findViewById(R.id.txt_wifi);
        et_pwd = (AppCompatEditText) findViewById(R.id.et_pwd);
        btn_connect = (AppCompatButton) findViewById(R.id.btn_connect);
        btn_connect_android10 = (AppCompatButton) findViewById(R.id.btn_connect_android10);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();

        btn_connect.postDelayed(new Runnable() {
            @Override
            public void run() {
                wifiManager.startScan();
            }
        }, 1000);


        //通过按钮事件搜索热点
        btn_connect.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                wifiList = wifiManager.getScanResults();
                Log.i("wifiTest", "wifiList:" + wifiList);

                if (wifiList != null && wifiList.size() > 0) {
                    txt_wifi.setText(wifiList.stream().map(s -> s.SSID+"加密方式："+s.capabilities).collect(Collectors.toList()).toString());
                }

                onReceiveNewNetworks(wifiList);

                if (wifiList == null || wifiList.size() == 0) {
                    Toast.makeText(getApplicationContext(), "没有扫描到网络", Toast.LENGTH_LONG).show();
                    return;
                }

                if (isConnected) {
                    Toast.makeText(getApplicationContext(), "已经连接了", Toast.LENGTH_LONG).show();
                    return;
                }


                if (TextUtils.isEmpty(et_pwd.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "请先输入wifi密码", Toast.LENGTH_LONG).show();
                    return;
                }
                synchronized (this) {
                    connectToHotpot();
                }

            }
        });

        btn_connect_android10.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                connectAndroid10wifi();
            }
        });
    }

    /* 监听热点变化 */
    private final class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            wifiList = wifiManager.getScanResults();

            Log.i("wifiTest", "wifiList:" + wifiList);

            if (wifiList == null || wifiList.size() == 0 || isConnected)
                return;
            onReceiveNewNetworks(wifiList);
        }
    }

    /*当搜索到新的wifi热点时判断该热点是否符合规格*/
    public void onReceiveNewNetworks(List<ScanResult> wifiList) {
        passableHotsPot = new ArrayList<String>();
        for (ScanResult result : wifiList) {
            System.out.println(result.SSID);
            if ((result.SSID).contains("AndroidShare")) {
                //if((result.SSID).contains("ynlk")){
                //if((result.SSID).contains("llkj-ap")){
                //if((result.SSID).equals("ynlk-huawei")){
                passableHotsPot.add(result.SSID);
                txt_wifi.setText(result.SSID+",加密方式："+result.capabilities);
            }
        }

    }

    /*连接到热点*/
    public void connectToHotpot() {
        if (passableHotsPot == null || passableHotsPot.size() == 0)
            return;
        wifiManager.removeNetwork(-1);
        WifiConfiguration wifiConfig = this.setWifiParams(passableHotsPot.get(0),3);
        int wcgID = wifiManager.addNetwork(wifiConfig);
        boolean flag = wifiManager.enableNetwork(wcgID, true);
        isConnected = flag;
        System.out.println("connect success? " + flag);

        Toast.makeText(getApplicationContext(), "是否连接成功? " + flag, Toast.LENGTH_LONG).show();

    }

    /*设置要连接的热点的参数*/
    public WifiConfiguration setWifiParams2(String ssid) {
        WifiConfiguration apConfig = new WifiConfiguration();
        apConfig.SSID = "\"" + ssid + "\"";

        String pwd = et_pwd.getText().toString();

        apConfig.preSharedKey = "\"" + pwd + "\"";
        apConfig.hiddenSSID = true;
        apConfig.status = WifiConfiguration.Status.ENABLED;
        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        apConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        return apConfig;
    }


    public WifiConfiguration setWifiParams(String SSID, int Type) {

        String ssid = "\"" + SSID + "\"";

        String pwd = et_pwd.getText().toString();
        String preSharedKey = "\"" + pwd + "\"";


        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = ssid;

        WifiConfiguration tempConfig = this.IsExsits(SSID);
        if (tempConfig != null) {
            wifiManager.removeNetwork(tempConfig.networkId);
        }

        if (Type == 1) // WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 2) // WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = preSharedKey;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 3) // WIFICIPHER_WPA
        {

            /**

             config.preSharedKey = preSharedKey;
             config.hiddenSSID = true;
             config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
             config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
             config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
             config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
             //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
             config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
             config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
             config.status = WifiConfiguration.Status.ENABLED;

             */

            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.preSharedKey = "\"".concat(preSharedKey).concat("\"");
        }
        return config;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*销毁时注销广播*/
        unregisterReceiver(wifiReceiver);


    }


    private WifiConfiguration IsExsits(String SSID) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(getApplicationContext(), "没有权限：ACCESS_FINE_LOCATION "  , Toast.LENGTH_LONG).show();
            return null;
        }
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs)
        {
            if (existingConfig.SSID.equals("\""+SSID+"\""))
            {
                return existingConfig;
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void connectAndroid10wifi(){

        String pwd = et_pwd.getText().toString();
        String preSharedKey = "\"" + pwd + "\"";

        final NetworkSpecifier specifier =
                new WifiNetworkSpecifier.Builder()
                        .setSsidPattern(new PatternMatcher("AndroidShare", PatternMatcher.PATTERN_PREFIX))
                        .setWpa2Passphrase(pwd)
                        //.setBssidPattern(MacAddress.fromString("10:03:23:00:00:00"), MacAddress.fromString("ff:ff:ff:00:00:00"))
                        .build();

        final NetworkRequest request =
                new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .setNetworkSpecifier(specifier)
                        .build();

        final ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);

        final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {

            @Override
            public void onAvailable(Network network) {
                // do success processing here..

                Toast.makeText(getApplicationContext(), "onAvailable"  , Toast.LENGTH_LONG).show();
            }

            @Override
            public void onUnavailable() {
                // do failure processing here..
                Toast.makeText(getApplicationContext(), "onUnavailable"  , Toast.LENGTH_LONG).show();

            }

        };
        connectivityManager.requestNetwork(request, networkCallback);


        // Release the request when done.
        //connectivityManager.unregisterNetworkCallback(networkCallback);
    }
}