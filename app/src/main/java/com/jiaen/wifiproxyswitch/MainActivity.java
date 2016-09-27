package com.jiaen.wifiproxyswitch;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements
        ProxySettingsFragment.IOnProxySettingFinished,
        WiFiConnectionFragment.IOnWiFiSettingFinished {

    private ProxySettingsFragment proxySettingsFragment;

    private WiFiConnectionFragment wifiConnectionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        proxySettingsFragment = new ProxySettingsFragment();
        ft.add(R.id.fragment_container,proxySettingsFragment);
        ft.commit();

        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!manager.isWifiEnabled()) {
            manager.setWifiEnabled(true);
        }
    }

    @Override
    public void onProxySetSuccess() {
        // do nothing
    }

    @Override
    public void onProxySetError() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        wifiConnectionFragment = new WiFiConnectionFragment();
        ft.hide(proxySettingsFragment);
        ft.add(R.id.fragment_container, wifiConnectionFragment);
        ft.commit();
    }

    @Override
    public void onWiFiSetSuccess() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.remove(wifiConnectionFragment);
        ft.show(proxySettingsFragment);
        ft.commit();
    }

    @Override
    public void onWiFiSetError() {
        // do nothing
    }
}
