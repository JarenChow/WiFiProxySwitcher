package com.jiaen.wifiproxyswitch;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.jiaen.wifiproxyswitch.util.ToastUtil;
import com.jiaen.wifiproxyswitch.util.WiFiUtil;

public class WiFiConnectionFragment extends Fragment implements View.OnClickListener {

    public interface IOnWiFiSettingFinished {

        void onWiFiSetSuccess();

        void onWiFiSetError();
    }

    private IOnWiFiSettingFinished onWiFiSettingFinished;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // 强制类型转换, 要求 Fragment 的容器必须实现此接口
        onWiFiSettingFinished = (IOnWiFiSettingFinished) context;
        wiFiUtil = new WiFiUtil(context);
        this.context = context;
    }

    private EditText editTextSSID;

    private EditText editTextPassword;

    private Spinner spinnerCipherType;

    private WiFiUtil wiFiUtil;

    private Context context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi_connection, container, false);
        editTextSSID = (EditText) view.findViewById(R.id.edit_text_ssid);
        editTextPassword = (EditText) view.findViewById(R.id.edit_text_password);
        view.findViewById(R.id.connect_wifi).setOnClickListener(this);
        spinnerCipherType = (Spinner) view.findViewById(R.id.spinner_cipher_type);
        String[] items = getResources().getStringArray(R.array.wifi_cipher_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, items);
        spinnerCipherType.setAdapter(adapter);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connect_wifi:
                connectWiFi();
                break;
        }
    }

    private void connectWiFi() {
        String ssid = editTextSSID.getText().toString();
        String password = editTextPassword.getText().toString();
        if (TextUtils.isEmpty(ssid) | TextUtils.isEmpty(password)) {
            ToastUtil.showToast(context, "请填写正确的无线名称以及密码");
            return;
        }
        boolean b = wiFiUtil.connectWiFi(ssid, password, spinnerCipherType.getSelectedItemPosition());
        if (b) {
            onWiFiSettingFinished.onWiFiSetSuccess();
        } else {
            onWiFiSettingFinished.onWiFiSetError();
        }
    }
}
