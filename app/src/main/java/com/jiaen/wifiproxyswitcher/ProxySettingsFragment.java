package com.jiaen.wifiproxyswitcher;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jiaen.wifiproxyswitcher.util.SharedPreferenceUtil;
import com.jiaen.wifiproxyswitcher.util.ToastUtil;
import com.jiaen.wifiproxyswitcher.util.WiFiProxyUtil;

public class ProxySettingsFragment extends Fragment implements View.OnClickListener {

    public interface IOnProxySettingFinished {

        void onProxySetSuccess();

        void onProxySetError();
    }

    private IOnProxySettingFinished onProxySettingFinished;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // 强制类型转换, 要求 Fragment 的容器必须实现此接口
        onProxySettingFinished = (IOnProxySettingFinished) context;
        wiFiProxyUtil = new WiFiProxyUtil(context);
        this.context = context;
    }

    private EditText editTextIp;

    private EditText editTextPort;

    private Button buttonSetProxy;

    private static final String PROMPT_1 = "设置 WiFi Http Proxy";

    private static final String PROMPT_2 = "取消 Proxy 设置";

    private boolean onProxy;

    private WiFiProxyUtil wiFiProxyUtil;

    private Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_proxy_settings, container, false);
        editTextIp = (EditText) view.findViewById(R.id.edit_text_ip);
        editTextPort = (EditText) view.findViewById(R.id.edit_text_port);
        buttonSetProxy = (Button) view.findViewById(R.id.set_wifi_proxy_settings);
        onProxy = (boolean) SharedPreferenceUtil.get(context, "onProxy", false);
        buttonSetProxy.setText(onProxy ? PROMPT_2 : PROMPT_1);
        buttonSetProxy.setOnClickListener(this);
        editTextIp.setText((String) SharedPreferenceUtil.get(context, "ip", ""));
        editTextPort.setText((String) SharedPreferenceUtil.get(context, "port", ""));
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_wifi_proxy_settings:
                if (!onProxy) {
                    setWiFiProxySettings();
                } else {
                    unsetWiFiProxySettings();
                }
                break;
        }
    }

    private void setWiFiProxySettings() {
        String ip = editTextIp.getText().toString();
        String port = editTextPort.getText().toString();
        SharedPreferenceUtil.put(context, "ip", ip);
        SharedPreferenceUtil.put(context, "port", port);
        if (TextUtils.isEmpty(ip) | TextUtils.isEmpty(port) | !TextUtils.isDigitsOnly(port)) {
            ToastUtil.showToast(context, "请填写正确的 IP 以及 Port");
            return;
        }
        boolean b = wiFiProxyUtil.setWiFiProxySettings(ip, Integer.valueOf(port));
        if (b) {
            buttonSetProxy.setText(PROMPT_2);
            onProxy = !onProxy;
            SharedPreferenceUtil.put(context, "onProxy", onProxy);
            onProxySettingFinished.onProxySetSuccess();
        } else {
            onProxySettingFinished.onProxySetError();
        }
    }

    private void unsetWiFiProxySettings() {
        boolean b = wiFiProxyUtil.unsetWiFiProxySettings();
        if (b) {
            buttonSetProxy.setText(PROMPT_1);
            onProxy = !onProxy;
            SharedPreferenceUtil.put(context, "onProxy", onProxy);
            onProxySettingFinished.onProxySetSuccess();
        } else {
            onProxySettingFinished.onProxySetError();
        }
    }
}
