package com.vkpapps.wifimanager;

/**
 * WifiPasswordHolder
 * Created By:Chuck
 * Des:
 * on 2022/11/15 22:32
 */
public class WifiPasswordHolder {
    private static final WifiPasswordHolder ourInstance = new WifiPasswordHolder();

    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    static WifiPasswordHolder getInstance() {
        return ourInstance;
    }

    private WifiPasswordHolder() {
    }


}
