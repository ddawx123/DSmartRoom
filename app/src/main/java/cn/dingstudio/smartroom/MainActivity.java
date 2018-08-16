package cn.dingstudio.smartroom;

import android.annotation.TargetApi;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView textStatusView;
    private TextView textStatusView2;
    private TextView textTemperatureView;
    private TextView textAuthModeView;
    private TextView textBrightnessValue;
    private SeekBar brightnessValue;
    private EditText editPasswordText;
    private Switch switchStatus;
    private Switch switchStatus2;
    private Button btnGetUserBySensor;
    private Context mContext;
    private DevicePolicyManager dpm;
    private ComponentName mDeviceAdminDefault;
    private Spinner spinnerColorValue;

    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();
    //BDAbstractLocationListener为7.2版本新增的Abstract类型的监听接口
    //原有BDLocationListener接口暂时同步保留。具体介绍请参考后文中的说明

    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String createPlainToken() {
        String temp_str = "";
        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        temp_str = sdf.format(dt);
        return temp_str;
    }

    public String buildToken() {
        return md5(createPlainToken());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        //注册监听函数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，设置定位模式，默认高精度
        //LocationMode.Hight_Accuracy：高精度；
        //LocationMode. Battery_Saving：低功耗；
        //LocationMode. Device_Sensors：仅使用设备；
        option.setCoorType("bd09ll");
        //可选，设置返回经纬度坐标类型，默认gcj02
        //gcj02：国测局坐标；
        //bd09ll：百度经纬度坐标；
        //bd09：百度墨卡托坐标；
        //海外地区定位，无需设置坐标类型，统一返回wgs84类型坐标
        option.setScanSpan(30000);
        //可选，设置发起定位请求的间隔，int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0
        //如果设置非0，需设置1000ms以上才有效
        option.setOpenGps(true);
        //可选，设置是否使用gps，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true
        option.setLocationNotify(true);
        //可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false
        option.setIgnoreKillProcess(true);
        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)
        option.SetIgnoreCacheException(true);
        //可选，设置是否收集Crash信息，默认收集，即参数为false
        option.setWifiCacheTimeOut(5*60*1000);
        //可选，7.2版本新增能力
        //如果设置了该接口，首次启动定位时，会先判断当前WiFi是否超出有效期，若超出有效期，会先重新扫描WiFi，然后定位
        option.setEnableSimulateGps(false);
        //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
        mLocationClient.setLocOption(option);
        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        //更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明
        mLocationClient.start();

        textStatusView = (TextView) findViewById(R.id.textStatusView);
        textStatusView2 = (TextView) findViewById(R.id.textStatusView2);
        switchStatus = (Switch) findViewById(R.id.switchStatus);
        switchStatus2 = (Switch) findViewById(R.id.switchStatus2);
        textTemperatureView = (TextView) findViewById(R.id.textTemperatureView);
        brightnessValue = (SeekBar) findViewById(R.id.brightnessValue);
        editPasswordText = (EditText) findViewById(R.id.editPasswordText);
        textAuthModeView = (TextView) findViewById(R.id.textAuthModeView);
        textBrightnessValue = (TextView) findViewById(R.id.textBrightnessValue);
        btnGetUserBySensor = (Button) findViewById(R.id.btnGetUserBySensor);
        spinnerColorValue = (Spinner) findViewById(R.id.spinnerColorValue);
        mContext = this;
        //buildToken();
        setupAdmin();
        initView();
        getSysTemperature();
        loadFingerPrint();

        spinnerColorValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] colorStringListArray = getResources().getStringArray(R.array.miio_light_colorlist);
                if (switchStatus2.isChecked()) {
                    OkHttpClient client = new OkHttpClient();
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    String brightness = "";
                    if (brightnessValue.getProgress() == 0) {
                        brightness = "3";
                        brightnessValue.setProgress(3);
                    }
                    else if (brightnessValue.getProgress() > 95) {
                        brightness = "3";
                        brightnessValue.setProgress(3);
                    }
                    else {
                        brightness = String.valueOf(brightnessValue.getProgress());
                    }
                    String colorName = colorStringListArray[i];
                    RequestBody body = RequestBody.create(mediaType, "entity_id=light.gateway_light_7c49eb17912e&device_type=light&operation_type=on&brightness=" + brightness + "&color_name=" + colorName);
                    Request request = new Request.Builder()
                            .url("http://api.dingstudio.cn/api?format=json&mod=HomeAssistant")
                            .post(body)
                            .addHeader("Token", buildToken())
                            .removeHeader("User-Agent")
                            .addHeader("User-Agent", "DHomeAssistant/1.0.1")
                            .addHeader("pkey", editPasswordText.getText().toString())
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .addHeader("Cache-Control", "no-cache")
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        try {
                            JSONObject jo = new JSONObject(response.body().string());
                            if (jo.getInt("code") == 200) {
                                textStatusView2.setText("房间彩灯状态：已启动");
                            }
                            else if (jo.getInt("code") == 401) {
                                initView();
                                showToast(jo.getString("message"));
                            }
                            else if (jo.getInt("code") == -16) {
                                showToast(jo.getString("data"));
                            }
                            else {
                                initView();
                            }
                        } catch (Exception e) {
                            showToast(e.getMessage());
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        showToast(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //TODO
            }
        });

        textTemperatureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast("中控板温度重新获取中");
                getSysTemperature();
            }
        });

        brightnessValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textBrightnessValue.setText("彩灯亮度：" + String.valueOf(i) + "% 选择区间[+1,+95]");
                String[] colorStringListArray = getResources().getStringArray(R.array.miio_light_colorlist);
                if (switchStatus2.isChecked()) {
                    OkHttpClient client = new OkHttpClient();
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    String brightness = String.valueOf(i);
                    String colorName = colorStringListArray[spinnerColorValue.getSelectedItemPosition()];
                    RequestBody body = RequestBody.create(mediaType, "entity_id=light.gateway_light_7c49eb17912e&device_type=light&operation_type=on&brightness=" + brightness + "&color_name=" + colorName);
                    Request request = new Request.Builder()
                            .url("http://api.dingstudio.cn/api?format=json&mod=HomeAssistant")
                            .post(body)
                            .addHeader("Token", buildToken())
                            .removeHeader("User-Agent")
                            .addHeader("User-Agent", "DHomeAssistant/1.0.1")
                            .addHeader("pkey", editPasswordText.getText().toString())
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .addHeader("Cache-Control", "no-cache")
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        try {
                            JSONObject jo = new JSONObject(response.body().string());
                            if (jo.getInt("code") == 200) {
                                textStatusView2.setText("房间彩灯状态：已启动");
                            }
                            else if (jo.getInt("code") == 401) {
                                initView();
                                showToast(jo.getString("message"));
                            }
                            else if (jo.getInt("code") == -16) {
                                showToast(jo.getString("data"));
                            }
                            else {
                                initView();
                            }
                        } catch (Exception e) {
                            showToast(e.getMessage());
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        showToast(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("On-Start-Tracking-Touch", String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("On-Stop-Tracking-Touch", String.valueOf(seekBar.getProgress()));
            }
        });

        btnGetUserBySensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userOnHomeInfo = "";
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://api.dingstudio.cn/api?format=json&mod=HomeAssistant&act=states")
                        .get()
                        .addHeader("Token", buildToken())
                        .removeHeader("User-Agent")
                        .addHeader("User-Agent", "DHomeAssistant/1.0.1")
                        .addHeader("Cache-Control", "no-cache")
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    //textView.setText(response.body().string());
                    try {
                        JSONObject jo = new JSONObject(response.body().string());
                        JSONArray ja = jo.getJSONArray("data");
                        for (int i = 0; i < ja.length(); i++) {
                            jo = ja.getJSONObject(i);
                            String entity_id = jo.getString("entity_id");
                            System.out.println(i + "[-]" + entity_id);
                            switch (entity_id) {
                                case "device_tracker.dlx":
                                    String dev_status2 = jo.getString("state");
                                    if (dev_status2.equals("not_home")) {
                                        userOnHomeInfo = userOnHomeInfo + "丁立新尚未在家";
                                    }
                                    else {
                                        userOnHomeInfo = userOnHomeInfo + "丁立新已在家";
                                    }
                                    break;
                                case "device_tracker.wzc":
                                    String dev_status3 = jo.getString("state");
                                    if (dev_status3.equals("not_home")) {
                                        userOnHomeInfo = userOnHomeInfo + "，王中晨尚未在家";
                                    }
                                    else {
                                        userOnHomeInfo = userOnHomeInfo + "，王中晨已在家";
                                    }
                                    break;
                                case "device_tracker.dd":
                                    String dev_status1 = jo.getString("state");
                                    if (dev_status1.equals("not_home")) {
                                        userOnHomeInfo = userOnHomeInfo + "，丁鼎尚未在家";
                                    }
                                    else {
                                        userOnHomeInfo = userOnHomeInfo + "，丁鼎已在家";
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        showToast(e.getMessage());
                        e.printStackTrace();
                    }
                }
                catch (IOException e) {
                    showToast(e.getMessage());
                    e.printStackTrace();
                }
                showToast(userOnHomeInfo);
            }
        });

        switchStatus2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (switchStatus2.isChecked()) {
                    OkHttpClient client = new OkHttpClient();
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    String brightness = "";
                    if (brightnessValue.getProgress() == 0) {
                        brightness = "3";
                        brightnessValue.setProgress(3);
                        showToast("亮度值不能为0，如需关闭彩灯请直接移动滑块。设置区间：1-95，本次系统将自动使用默认值（3）完成设备操作。");
                    }
                    else if (brightnessValue.getProgress() > 95) {
                        brightness = "3";
                        brightnessValue.setProgress(3);
                        showToast("您超出了设置区间（1-95），系统将自动使用默认值（3）完成设备操作。");
                    }
                    else {
                        brightness = String.valueOf(brightnessValue.getProgress());
                    }
                    String[] colorStringListArray = getResources().getStringArray(R.array.miio_light_colorlist);
                    String colorName = colorStringListArray[spinnerColorValue.getSelectedItemPosition()];
                    RequestBody body = RequestBody.create(mediaType, "entity_id=light.gateway_light_7c49eb17912e&device_type=light&operation_type=on&brightness=" + brightness + "&color_name=" + colorName);
                    Request request = new Request.Builder()
                            .url("http://api.dingstudio.cn/api?format=json&mod=HomeAssistant")
                            .post(body)
                            .addHeader("Token", buildToken())
                            .removeHeader("User-Agent")
                            .addHeader("User-Agent", "DHomeAssistant/1.0.1")
                            .addHeader("pkey", editPasswordText.getText().toString())
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .addHeader("Cache-Control", "no-cache")
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        try {
                            JSONObject jo = new JSONObject(response.body().string());
                            if (jo.getInt("code") == 200) {
                                textStatusView2.setText("房间彩灯状态：已启动");
                            }
                            else if (jo.getInt("code") == 401) {
                                initView();
                                showToast(jo.getString("message"));
                            }
                            else if (jo.getInt("code") == -16) {
                                showToast(jo.getString("data"));
                            }
                            else {
                                initView();
                            }
                        } catch (Exception e) {
                            showToast(e.getMessage());
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        showToast(e.getMessage());
                        e.printStackTrace();
                    }
                }
                else {
                    OkHttpClient client = new OkHttpClient();

                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    RequestBody body = RequestBody.create(mediaType, "entity_id=light.gateway_light_7c49eb17912e&device_type=light&operation_type=off");
                    Request request = new Request.Builder()
                            .url("http://api.dingstudio.cn/api?format=json&mod=HomeAssistant")
                            .post(body)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .addHeader("Token", buildToken())
                            .removeHeader("User-Agent")
                            .addHeader("User-Agent", "DHomeAssistant/1.0.1")
                            .addHeader("pkey", editPasswordText.getText().toString())
                            .addHeader("Cache-Control", "no-cache")
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        try {
                            JSONObject jo = new JSONObject(response.body().string());
                            if (jo.getInt("code") == 200) {
                                textStatusView2.setText("房间彩灯状态：已关闭");
                            }
                            else if (jo.getInt("code") == 401) {
                                initView();
                                showToast(jo.getString("message"));
                            }
                            else if (jo.getInt("code") == -16) {
                                showToast(jo.getString("data"));
                            }
                            else {
                                initView();
                            }
                        } catch (Exception e) {
                            showToast(e.getMessage());
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        showToast(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        switchStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switchStatus.isChecked()) {
                    OkHttpClient client = new OkHttpClient();

                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    RequestBody body = RequestBody.create(mediaType, "entity_id=switch.original_xiaomi_mi_smart_wifi_socket&operation_type=on&device_type=switch");
                    Request request = new Request.Builder()
                            .url("http://api.dingstudio.cn/api?format=json&mod=HomeAssistant")
                            .post(body)
                            .addHeader("Token", buildToken())
                            .removeHeader("User-Agent")
                            .addHeader("User-Agent", "DHomeAssistant/1.0.1")
                            .addHeader("pkey", editPasswordText.getText().toString())
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .addHeader("Cache-Control", "no-cache")
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        try {
                            JSONObject jo = new JSONObject(response.body().string());
                            if (jo.getInt("code") == 200) {
                                textStatusView.setText("书房电源状态：已通电");
                            }
                            else if (jo.getInt("code") == 401) {
                                initView();
                                showToast(jo.getString("message"));
                            }
                            else if (jo.getInt("code") == -16) {
                                showToast(jo.getString("data"));
                            }
                            else {
                                showToast("出错了！错误定位：" + response.body().string());
                                initView();
                            }
                        } catch (Exception e) {
                            showToast(e.getMessage());
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        showToast(e.getMessage());
                        e.printStackTrace();
                    }
                }
                else {
                    OkHttpClient client = new OkHttpClient();

                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    RequestBody body = RequestBody.create(mediaType, "entity_id=switch.original_xiaomi_mi_smart_wifi_socket&operation_type=off&device_type=switch");
                    Request request = new Request.Builder()
                            .url("http://api.dingstudio.cn/api?format=json&mod=HomeAssistant")
                            .post(body)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .addHeader("Token", buildToken())
                            .removeHeader("User-Agent")
                            .addHeader("User-Agent", "DHomeAssistant/1.0.1")
                            .addHeader("pkey", editPasswordText.getText().toString())
                            .addHeader("Cache-Control", "no-cache")
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        try {
                            JSONObject jo = new JSONObject(response.body().string());
                            if (jo.getInt("code") == 200) {
                                textStatusView.setText("书房电源状态：已断电");
                            }
                            else if (jo.getInt("code") == 401) {
                                initView();
                                showToast(jo.getString("message"));
                            }
                            else if (jo.getInt("code") == -16) {
                                showToast(jo.getString("data"));
                            }
                            else {
                                showToast("出错了！错误定位：" + response.body().string());
                                initView();
                            }
                        } catch (Exception e) {
                            showToast(e.getMessage());
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        showToast(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        switchStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (switchStatus.isChecked()) {
                    OkHttpClient client = new OkHttpClient();

                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    RequestBody body = RequestBody.create(mediaType, "entity_id=switch.original_xiaomi_mi_smart_wifi_socket&operation_type=on&device_type=switch");
                    Request request = new Request.Builder()
                            .url("http://api.dingstudio.cn/api?format=json&mod=HomeAssistant")
                            .post(body)
                            .addHeader("Token", buildToken())
                            .removeHeader("User-Agent")
                            .addHeader("User-Agent", "DHomeAssistant/1.0.1")
                            .addHeader("pkey", editPasswordText.getText().toString())
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .addHeader("Cache-Control", "no-cache")
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        try {
                            JSONObject jo = new JSONObject(response.body().string());
                            if (jo.getInt("code") == 200) {
                                textStatusView.setText("书房电源状态：已通电");
                            }
                            else if (jo.getInt("code") == 401) {
                                initView();
                            }
                            else if (jo.getInt("code") == -16) {
                                showToast(jo.getString("data"));
                            }
                            else {
                                initView();
                            }
                        } catch (Exception e) {
                            showToast(e.getMessage());
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        showToast(e.getMessage());
                        e.printStackTrace();
                    }
                }
                else {
                    OkHttpClient client = new OkHttpClient();

                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    RequestBody body = RequestBody.create(mediaType, "entity_id=switch.original_xiaomi_mi_smart_wifi_socket&operation_type=off&device_type=switch");
                    Request request = new Request.Builder()
                            .url("http://api.dingstudio.cn/api?format=json&mod=HomeAssistant")
                            .post(body)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .addHeader("Token", buildToken())
                            .removeHeader("User-Agent")
                            .addHeader("User-Agent", "DHomeAssistant/1.0.1")
                            .addHeader("pkey", editPasswordText.getText().toString())
                            .addHeader("Cache-Control", "no-cache")
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        try {
                            JSONObject jo = new JSONObject(response.body().string());
                            if (jo.getInt("code") == 200) {
                                textStatusView.setText("书房电源状态：已断电");
                            }
                            else if (jo.getInt("code") == 401) {
                                initView();
                            }
                            else if (jo.getInt("code") == -16) {
                                showToast(jo.getString("data"));
                            }
                            else {
                                initView();
                            }
                        } catch (Exception e) {
                            showToast(e.getMessage());
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        showToast(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private void getSysTemperature() {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "format=json&mod=CpuTemperature");
        Request request = new Request.Builder()
                .url("http://api.dingstudio.cn/api")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .removeHeader("User-Agent")
                .addHeader("User-Agent", "DHomeAssistant/1.0.1")
                .addHeader("Cache-Control", "no-cache")
                .build();

        try {
            Response response = client.newCall(request).execute();
            try {
                JSONObject jo = new JSONObject(response.body().string());
                String temperature = String.valueOf(jo.getDouble("data"));
                textTemperatureView.setText("中控板温度：" + temperature + " ℃");
            } catch (Exception e) {
                showToast(e.getMessage());
                e.printStackTrace();
            }
        } catch (IOException e) {
            showToast(e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupAdmin() {
        dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        mDeviceAdminDefault = new ComponentName(getApplicationContext(), mDeviceAdminReceiver.class);
        if (dpm.isAdminActive(mDeviceAdminDefault)) {
            dpm.setPasswordMinimumLength(mDeviceAdminDefault, 5);
        }
        else {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminDefault);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getResources().getString(R.string.device_admin_description));
            startActivityForResult(intent, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setupAdmin();
    }

    private void initView() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://api.dingstudio.cn/api?format=json&mod=HomeAssistant&act=states")
                .get()
                .addHeader("Token", buildToken())
                .removeHeader("User-Agent")
                .addHeader("User-Agent", "DHomeAssistant/1.0.1")
                .addHeader("Cache-Control", "no-cache")
                .build();

        try {
            Response response = client.newCall(request).execute();
            //textView.setText(response.body().string());
            try {
                JSONObject jo = new JSONObject(response.body().string());
                JSONArray ja = jo.getJSONArray("data");
                for (int i = 0; i < ja.length(); i++) {
                    jo = ja.getJSONObject(i);
                    String entity_id = jo.getString("entity_id");
                    //System.out.println(i + "[-]" + entity_id);
                    if (entity_id.equals("switch.original_xiaomi_mi_smart_wifi_socket")) {
                        String dev_status = jo.getString("state");
                        //System.out.println(dev_status);
                        if (dev_status.equals("on")) {
                            textStatusView.setText("书房电源状态：已通电");
                            switchStatus.setChecked(true);
                        }
                        else {
                            textStatusView.setText("书房电源状态：已断电");
                            switchStatus.setChecked(false);
                        }
                    }
                    else if (entity_id.equals("light.gateway_light_7c49eb17912e")) {
                        String dev_status = jo.getString("state");
                        //System.out.println(dev_status);
                        if (dev_status.equals("on")) {
                            textStatusView2.setText("房间彩灯状态：已启动");
                            switchStatus2.setChecked(true);
                        }
                        else {
                            textStatusView2.setText("房间彩灯状态：已关闭");
                            switchStatus2.setChecked(false);
                        }
                    }
                }
            } catch (Exception e) {
                showToast(e.getMessage());
                e.printStackTrace();
            }
        }
        catch (IOException e) {
            showToast(e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadFingerPrint(){

        FingerPrintUtil.callFingerPrint(new FingerPrintUtil.OnCallBackListenr() {
            @Override
            public void onSupportFailed() {
                textAuthModeView.setText("认证模式：在线密码");
            }

            @Override
            public void onInsecurity() {
                textAuthModeView.setText("认证模式：在线密码（锁屏未设置）");
            }

            @Override
            public void onEnrollFailed() {
                textAuthModeView.setText("认证模式：在线密码（指纹未设置）");
            }

            @Override
            public void onAuthenticationStart() {
                textAuthModeView.setText("认证模式：生物指纹特征识别");
            }

            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                showToast(errString.toString());
            }

            @Override
            public void onAuthenticationFailed() {
                showToast("生物特征比对失败，请重试。");
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                showToast(helpString.toString());
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                editPasswordText.setText("954759397");
                btnGetUserBySensor.setVisibility(View.VISIBLE);
                showToast("生物特征比对成功，已为您通过身份验证。");
            }
        }, mContext);
    }
    private Handler handler= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0){
                int i = postion % 5;
                if (i == 0){
                    tv[4].setBackground(null);
                    tv[i].setBackgroundColor(getResources().getColor(R.color.colorAccent));
                }
                else{
                    tv[i].setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    tv[i-1].setBackground(null);
                }
                postion++;
                handler.sendEmptyMessageDelayed(0,100);
            }
        }
    };
    TextView[] tv = new TextView[5];
    private int postion = 0;
    private void initView(View view) {
        postion = 0;
        handler.sendEmptyMessageDelayed(0,100);
    }


    public void showToast(String name ){
        Toast.makeText(MainActivity.this,name,Toast.LENGTH_LONG).show();
    }
}
