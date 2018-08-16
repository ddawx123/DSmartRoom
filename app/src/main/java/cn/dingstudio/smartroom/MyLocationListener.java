package cn.dingstudio.smartroom;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyLocationListener extends BDAbstractLocationListener {
    @Override
    public void onReceiveLocation(BDLocation location){
        //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
        //以下只列举部分获取经纬度相关（常用）的结果信息
        //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

        double latitude = location.getLatitude();    //获取纬度信息
        double longitude = location.getLongitude();    //获取经度信息
        float radius = location.getRadius();    //获取定位精度，默认值为0.0f

        String coorType = location.getCoorType();
        //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

        int errorCode = location.getLocType();


        //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://api.dingstudio.cn/api?format=json&mod=Location&latitude=" + String.valueOf(latitude) + "&longitude=" + String.valueOf(longitude))
                .get()
                .removeHeader("User-Agent")
                .addHeader("User-Agent", "DHomeAssistant/1.0.1")
                .addHeader("Cache-Control", "no-cache")
                .build();
        try {
            Response response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
