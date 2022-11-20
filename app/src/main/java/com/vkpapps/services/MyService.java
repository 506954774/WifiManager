package com.vkpapps.services;

import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.vkpapps.apmanager.APManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * MyService
 * Created By:Chuck
 * Des:
 * on 2022/11/20 16:27
 */
public class MyService extends Service {
    private static final String TAG = "MyService";
    private NotificationManager notificationManager;
    private String notificationId = "channel_Id";
    private String notificationName = "channel_Name";

    private static final long PERIOD = 3L;
    private static final long PERIOD_MILLSECONDS = 10*1000L;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ...");
        loop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ...");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ....");
        super.onDestroy();
    }

    private ScheduledExecutorService executorService;

    /**
     * 定时轮询wifi是否开启
     */
    private void loop() {
        Log.e(TAG,"loop2" );

        executorService = new ScheduledThreadPoolExecutor(1, Executors.defaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());
        //executorService.scheduleAtFixedRate(task,PERIOD,PERIOD, TimeUnit.SECONDS);

        executorService.scheduleAtFixedRate(task,PERIOD_MILLSECONDS,PERIOD_MILLSECONDS, TimeUnit.MILLISECONDS);

    }


    TimerTask task=new TimerTask() {
        @Override
        public void run() {

            Log.e(TAG,"准备轮询wifi状态" );

            boolean wifiApOpen = isWifiApOpen(getApplicationContext());
            Log.e(TAG, "wifiApOpen=" +wifiApOpen);

            //APManager.getApManager(getApplicationContext()).getWifiManager().get

            android.net.wifi.WifiManager m = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            android.net.wifi.SupplicantState s = m.getConnectionInfo().getSupplicantState();
            NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(s);
            Log.e(TAG, "state=" +state);
        }
    };


    public static boolean isWifiApOpen(Context context) {
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            //通过放射获取 getWifiApState()方法
            Method method = manager.getClass().getDeclaredMethod("getWifiApState");
            //调用getWifiApState() ，获取返回值
            int state = (int) method.invoke(manager);
            //通过放射获取 WIFI_AP的开启状态属性
            Field field = manager.getClass().getDeclaredField("WIFI_AP_STATE_ENABLED");
            //获取属性值
            int value = (int) field.get(manager);
            //判断是否开启
            if (state == value) {
                return true;
            } else {
                return false;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return false;
    }

}