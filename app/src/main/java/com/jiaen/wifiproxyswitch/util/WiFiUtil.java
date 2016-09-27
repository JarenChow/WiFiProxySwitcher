package com.jiaen.wifiproxyswitch.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

public class WiFiUtil {

    public static final int WIFI_CIPHER_WPA = 0;

    public static final int WIFI_CIPHER_WEP = 1;

    public static final int WIFI_CIPHER_NO_PASS = 2;

    private Context context;

    private WifiManager wifiManager;

    public WiFiUtil(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public boolean connectWiFi(String SSID, String password, int Type) {
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
            wifiManager.setWifiEnabled(true);
        }
        WifiConfiguration config = createWiFiInfo(SSID, password, Type);
        return addNetwork(config);
    }

    // 添加一个网络并连接
    private boolean addNetwork(WifiConfiguration config) {
        int netId = wifiManager.addNetwork(config);
        boolean b = wifiManager.enableNetwork(netId, true);
        ToastUtil.showToast(context, b ? "保存" + SSID + "成功" : "请在[设置]-[WLAN]先断开" + SSID);
        return b;
    }

    private String SSID;

    private WifiConfiguration createWiFiInfo(String SSID, String password, int Type) {
        this.SSID = SSID;
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        if (Type == WIFI_CIPHER_NO_PASS) {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == WIFI_CIPHER_WEP) {
            config.wepKeys[0] = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == WIFI_CIPHER_WPA) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            // 需要增加如下两行代码, 否则 WiFi 无法连接
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

}
