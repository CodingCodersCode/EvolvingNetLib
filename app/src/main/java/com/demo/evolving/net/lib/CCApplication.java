package com.demo.evolving.net.lib;

import android.app.Application;

import com.codingcoderscode.evolving.net.CCRxNetManager;
import com.codingcoderscode.evolving.net.request.ssl.HttpsUtil;
import com.codingcoderscode.evolving.net.util.NetLogUtil;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by CodingCodersCode on 2017/10/30.
 */

public class CCApplication extends Application {

    private String LOG_TAG = getClass().getCanonicalName();

    public static RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);
        // Normal app init code...

        initRxNetManager();
    }

    /**
     * 初始化网络库设置
     */
    private void initRxNetManager() {


        try {
            //所有Http请求的公共header信息
            Map<String, String> commonHeaderMap = new HashMap<>();
            commonHeaderMap.put("common_header_param1", "common_header_value1");
            commonHeaderMap.put("common_header_param2", "common_header_value2");
            commonHeaderMap.put("common_header_param3", "common_header_value3");

            //CCRxNetManager测试代码
            CCRxNetManager ccRxNetManager = new CCRxNetManager.Builder()
                    .baseUrl("http://mobile.ccbscf.com")
                    .callAdapterFactory(RxJava2CallAdapterFactory.create())
                    .converterFactory(GsonConverterFactory.create())
                    .commonHeaders(commonHeaderMap)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .enableLogInterceptor(true)
                    //.interceptor(new TokenInterceptor())
                    //.sslSocketFactory(HttpsUtil.getSSlSocketFactory(getAssets().open("srca.cer")))
                    .build();
        } catch (Exception e) {
            NetLogUtil.printLog("e", LOG_TAG, "初始化EvolvingNetLib.RxNetManager失败", e);
        }
    }

}
