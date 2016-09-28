package com.jiaen.wifiproxyswitcher.util;


import android.annotation.TargetApi;
import android.content.Context;
import android.net.ProxyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class WiFiProxyUtil {

    private Context context;

    public WiFiProxyUtil(Context context) {
        this.context = context;
    }

    public boolean setWiFiProxySettings(String ip, int port) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            return setWiFiProxySettings4(ip, port);
        } else {
            return setWiFiProxySettings5(ip, port);
        }
    }

    public boolean unsetWiFiProxySettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return unsetWiFiProxySettings4();
        } else {
            return unSetWiFiProxySettings5();
        }
    }

    /**
     * 低于 Android 5.0 系统
     */
    private boolean setWiFiProxySettings4(String ip, int port) {
        // 获得 WifiConfiguration
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = getCurrentWifiConfiguration(manager);
        if (null == config)
            return false;

        try {
            // 从 WifiConfiguration 中获得 linkProperties
            Object linkProperties = getField(config, "linkProperties");
            if (null == linkProperties)
                return false;

            // 获得 setHttpProxy 方法
            Class<?> proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
            Class[] setHttpProxyParams = new Class[1];
            setHttpProxyParams[0] = proxyPropertiesClass;
            Class<?> lpClass = Class.forName("android.net.LinkProperties");
            Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
            setHttpProxy.setAccessible(true);

            // 获得 ProxyProperties 构造方法
            Class[] proxyPropertiesCtorParamTypes = new Class[3];
            proxyPropertiesCtorParamTypes[0] = String.class;
            proxyPropertiesCtorParamTypes[1] = int.class;
            proxyPropertiesCtorParamTypes[2] = String.class;

            Constructor proxyPropertiesCtor = proxyPropertiesClass.getConstructor(proxyPropertiesCtorParamTypes);

            // 给构造方法创建参数
            Object[] proxyPropertiesCtorParams = new Object[3];
            proxyPropertiesCtorParams[0] = ip;
            proxyPropertiesCtorParams[1] = port;
            proxyPropertiesCtorParams[2] = null;

            // 创建 ProxyProperties 的对象
            Object proxySettings = proxyPropertiesCtor.newInstance(proxyPropertiesCtorParams);

            // 反射调用 linkProperties 的 setHttpProxy 方法, 参数为 ProxyProperties
            Object[] params = new Object[1];
            params[0] = proxySettings;
            setHttpProxy.invoke(linkProperties, params);

            setProxySettings("STATIC", config);

            // 保存设置
            manager.updateNetwork(config);
            manager.disconnect();
            manager.reconnect();

            ToastUtil.showToast(context, "保存Proxy设置成功");
            return true;
        } catch (Exception e) {
            ToastUtil.showToast(context, "保存Proxy设置失败");
            return false;
        }
    }

    /**
     * 低于 Android 5.0 系统使用
     */
    private boolean unsetWiFiProxySettings4() {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = getCurrentWifiConfiguration(manager);
        if (null == config)
            return false;

        try {
            // 获得 linkProperties
            Object linkProperties = getField(config, "linkProperties");
            if (null == linkProperties)
                return false;

            // 获得 setHttpProxy 方法
            Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
            Class[] setHttpProxyParams = new Class[1];
            setHttpProxyParams[0] = proxyPropertiesClass;
            Class<?> lpClass = Class.forName("android.net.LinkProperties");
            Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
            setHttpProxy.setAccessible(true);

            // 参数传递空, 表示取消 proxy 设置
            Object[] params = new Object[1];
            params[0] = null;
            setHttpProxy.invoke(linkProperties, params);

            setProxySettings("NONE", config);

            // 保存设置
            manager.updateNetwork(config);
            manager.disconnect();
            manager.reconnect();

            ToastUtil.showToast(context, "取消Proxy设置成功");
            return true;
        } catch (Exception e) {
            ToastUtil.showToast(context, "取消Proxy设置失败");
            return false;
        }
    }

    private WifiConfiguration getCurrentWifiConfiguration(WifiManager manager) {
        if (!manager.isWifiEnabled())
            return null;

        List<WifiConfiguration> configurationList = manager.getConfiguredNetworks();
        WifiConfiguration configuration = null;
        int networkId = manager.getConnectionInfo().getNetworkId();
        for (int i = 0; i < configurationList.size(); ++i) {
            WifiConfiguration wifiConfiguration = configurationList.get(i);
            if (wifiConfiguration.networkId == networkId)
                configuration = wifiConfiguration;
        }

        return configuration;
    }

    private static void setProxySettings(String assign, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        setEnumField(wifiConf, assign, "proxySettings");
    }


    private static void setEnumField(Object obj, String value, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class) f.getType(), value));
    }

    private static Object getField(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        f.setAccessible(true);
        return f.get(obj);
    }

    /**
     * 高于 Android 5.0 系统使用
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean setWiFiProxySettings5(String ip, int port) {
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiConfiguration config = getCurrentWifiConfiguration(manager);
            Class proxySettings = Class.forName("android.net.IpConfiguration$ProxySettings");

            Class[] setProxyParams = new Class[2];
            setProxyParams[0] = proxySettings;
            setProxyParams[1] = ProxyInfo.class;

            Method setProxy = WifiConfiguration.class.getDeclaredMethod("setProxy", setProxyParams);
            setProxy.setAccessible(true);

            ProxyInfo proxyInfo = ProxyInfo.buildDirectProxy(ip, port);

            Object[] methodParams = new Object[2];
            methodParams[0] = Enum.valueOf(proxySettings, "STATIC");
            methodParams[1] = proxyInfo;

            setProxy.invoke(config, methodParams);

            manager.updateNetwork(config);

            String result = getCurrentWifiConfiguration(manager).toString();
            String key = "Proxy settings: ";
            int start = result.indexOf(key) + key.length();
            if (result.substring(start, start + 4).equals("NONE")) {
                throw new RuntimeException("Can't update the Network, you should have the right WifiConfiguration");
            }

            manager.disconnect();
            manager.reconnect();

            ToastUtil.showToast(context, "保存Proxy设置成功");
            return true;
        } catch (Exception e) {
            ToastUtil.showToast(context, "保存Proxy设置失败");
            return false;
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean unSetWiFiProxySettings5() {
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiConfiguration config = getCurrentWifiConfiguration(manager);
            Class proxySettings = Class.forName("android.net.IpConfiguration$ProxySettings");

            Class[] setProxyParams = new Class[2];
            setProxyParams[0] = proxySettings;
            setProxyParams[1] = ProxyInfo.class;

            Method setProxy = WifiConfiguration.class.getDeclaredMethod("setProxy", setProxyParams);
            setProxy.setAccessible(true);

            Object[] methodParams = new Object[2];
            methodParams[0] = Enum.valueOf(proxySettings, "NONE");
            methodParams[1] = null;

            setProxy.invoke(config, methodParams);

            manager.updateNetwork(config);
            manager.disconnect();
            manager.reconnect();

            ToastUtil.showToast(context, "取消Proxy设置成功");
            return true;
        } catch (Exception e) {
            ToastUtil.showToast(context, "取消Proxy设置失败");
            return false;
        }
    }

/*
    public void setWiFiProxySettings5(String ip, int port) {
        // 另外一种方式
        if (null == config)
            return;
        try {
            int nID = manager.getConnectionInfo().getNetworkId();
            String ssid = manager.getConnectionInfo().getSSID();

            Constructor<ProxyInfo> pinfoConstructor = ProxyInfo.class.getConstructor(String.class, int.class, String.class);

            // api 21 + ProxyInfo.buildDirectProxy(host,port);
            Class psts = Class.forName("android.net.IpConfiguration$ProxySettings");
            Method setProxy = WifiConfiguration.class.getDeclaredMethod("setProxy", psts, ProxyInfo.class);

            List<WifiConfiguration> configuredNetworks = manager.getConfiguredNetworks();
            for (WifiConfiguration config : configuredNetworks) {
                if (nID == config.networkId && ssid.equals(config.SSID)) {
                    setProxy.invoke(config, psts.getEnumConstants()[1], pinfoConstructor.newInstance(ip, port, null));
                    manager.updateNetwork(config);
                    manager.disconnect();
                    manager.reconnect();
                    break;
                }
            }

            messageToast(ip, port, "成功");
        } catch (Exception e) {
            messageToast("失败");
        }


        // 另外一种方式
        Class proxyInfoClass = Class.forName("android.net.ProxyInfo");
        Class[] setHttpProxyParams = new Class[1];
        setHttpProxyParams[0] = proxyInfoClass;
        Class<?> wifiConfigClass = Class.forName("android.net.wifi.WifiConfiguration");
        Method setHttpProxy = wifiConfigClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
        setHttpProxy.setAccessible(true);

        Class ipConfigClass = Class.forName("android.net.IpConfiguration");
        Field f = ipConfigClass.getField("proxySettings");
        Class proxySettingsClass = f.getType();

        Class[] setProxySettingsParams = new Class[1];
        setProxySettingsParams[0] = proxySettingsClass;
        Method setProxySettings = wifiConfigClass.getDeclaredMethod("setProxySettings", setProxySettingsParams);
        setProxySettings.setAccessible(true);

        ProxyInfo pi = ProxyInfo.buildDirectProxy(ip, port);
        //ProxyInfo pacInfo = ProxyInfo.buildPacProxy(Uri.parse("http://localhost/pac"));

        Object[] paramsSetHttpProxy = new Object[1];
        paramsSetHttpProxy[0] = pi;
        setHttpProxy.invoke(config, paramsSetHttpProxy);

        Object[] paramsSetProxySettings = new Object[1];
        paramsSetProxySettings[0] = Enum.valueOf(proxySettingsClass, "STATIC");
        setProxySettings.invoke(config, paramsSetProxySettings);

        manager.updateNetwork(config);
        manager.disconnect();
        manager.reconnect();

        ToastUtil.showToast(context, "Success");
    }
*/

}
