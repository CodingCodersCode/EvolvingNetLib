package com.demo.evolving.net.lib.democode;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.codingcoderscode.evolving.base.CCBaseRxAppCompactActivity;
import com.codingcoderscode.evolving.net.cache.mode.CCCMode;
import com.codingcoderscode.evolving.net.request.listener.CCCacheQueryListener;
import com.codingcoderscode.evolving.net.request.listener.CCCacheSaveListener;
import com.codingcoderscode.evolving.net.request.listener.CCNetResultListener;
import com.codingcoderscode.evolving.net.request.canceler.CCCanceler;
import com.codingcoderscode.evolving.net.util.CCLogUtil;
import com.demo.evolving.net.lib.CCApplication;
import com.demo.evolving.net.lib.R;
import com.demo.evolving.net.lib.TestRespObj;
import com.demo.evolving.net.lib.bean.SampleRespBeanWrapper;
import com.demo.evolving.net.lib.bean.SampleResponseBean;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by CodingCodersCode on 2017/11/30.
 */

public class OrdinaryRequestActivity extends CCBaseRxAppCompactActivity implements View.OnClickListener {

    private final String LOG_TAG = getClass().getCanonicalName();

    private TextView tv_btn_1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oridinary_request);

        initView();
    }

    private void initView() {
        this.tv_btn_1 = (TextView) findViewById(R.id.tv_btn_1);
        this.tv_btn_1.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_btn_1:
                onStartRequest();
                break;
        }
    }

    private void onStartRequest() {
        try {

            //指定的Http请求所独有的header信息
            Map<String, String> specifyHeaderMap = new HashMap<>();
            specifyHeaderMap.put("specify_header_param1", "specify_header_value1");
            specifyHeaderMap.put("specify_header_param2", "specify_header_value2");
            specifyHeaderMap.put("specify_header_param3", "specify_header_value3");

            //请求参数信息
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("name", "account name");
            paramMap.put("password", "password");
            paramMap.put("mobile", "15200000000");
            paramMap.put("appId", "00000000000000002:00:00:00:00:00");
            paramMap.put("appType", "android");


            //restful api中的path信息
            Map<String, String> pathMap = new HashMap<String, String>();
            pathMap.put("{path1}", "uaa");
            pathMap.put("{path2}", "app");

            TypeToken typeToken = new TypeToken<SampleRespBeanWrapper<SampleResponseBean>>() {
            };


            //SampleRespBeanWrapper<SampleResponseBean>

            //Type   TypeToken Class<?>

            Type typeToken1 = this.<SampleRespBeanWrapper<SampleResponseBean>>getTypeToken();

            //TypeToken typeToken2 = TypeToken.<SampleRespBeanWrapper<SampleResponseBean>>get();

            TestTypeTokenClass testTypeTokenClass = new TestTypeTokenClass<SampleRespBeanWrapper<SampleResponseBean>>();

            Class typeToken3 = testTypeTokenClass.getClass();


            ((CCApplication)this.getApplicationContext()).getCcRxNetManager().<SampleRespBeanWrapper<SampleResponseBean>>post("/zuul/{path1}/{path2}/biz/v1/login")
                    .setHeaderMap(specifyHeaderMap)
                    .setPathMap(pathMap)
                    .setRequestParam(paramMap)
                    .setRetryCount(10)
                    .setRetryDelayTimeMillis(3000)
                    .setCacheQueryMode(CCCMode.QueryMode.MODE_DISK_AND_NET)
                    .setCacheSaveMode(CCCMode.SaveMode.MODE_DEFAULT)

                    .setNeedIntervalCallback(true)
                    .setIntervalMilliSeconds(20)

                    .setReqTag("test_login_req_tag")
                    .setCacheTag("test_login_req_cache_key")
                    .setCCNetCallback(new RxNetManagerCallback())
                    .setCCCacheSaveCallback(new RxNetCacheSaveListener())
                    .setCCCacheQueryCallback(new RxNetCacheQueryListener())
                    .setResponseBeanType(typeToken.getType())
                    .executeAsync();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <T> Type getTypeToken(){
        /*
        Type mySuperClass = new TestTypeTokenClass<T>(){}.getClass().getGenericSuperclass();
        Type type = ((ParameterizedType) mySuperClass).getActualTypeArguments()[0];

        return type;
        */
        return new TypeToken<T>() {}.getRawType();
    }

    private static class TestTypeTokenClass<T>{
        //private Class<T> typeClass;

        public TestTypeTokenClass() {
            //typeClass = (Class<T>)getClass();
        }
    }

    /**
     * 数据请求结果回调
     */
    private class RxNetManagerCallback implements CCNetResultListener {

        @Override
        public <T> void onStartRequest(Object reqTag, CCCanceler canceler) {

            CCLogUtil.printLog("d", LOG_TAG, "调用了onStartRequest方法，调用者reqTag=" + reqTag);

        }

        @Override
        public <T> void onDiskCacheQuerySuccess(Object reqTag, T response) {

            if (response != null) {

                if (response instanceof TestRespObj) {
                    CCLogUtil.printLog("d", LOG_TAG, "调用了onDiskCacheSuccess方法，调用者reqTag=" + reqTag + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    CCLogUtil.printLog("d", LOG_TAG, "调用了onDiskCacheSuccess方法，调用者reqTag=" + reqTag + ",但响应数据不是TestRespObj类型");
                }

            } else {
                CCLogUtil.printLog("d", LOG_TAG, "调用了onDiskCacheSuccess方法，调用者reqTag=" + reqTag + ",但响应数据response == null");
            }

        }

        @Override
        public <T> void onDiskCacheQueryFail(Object reqTag, Throwable t) {

        }

        @Override
        public <T> void onNetSuccess(Object reqTag, T response) {


            if (response != null) {

                if (response instanceof TestRespObj) {
                    CCLogUtil.printLog("d", LOG_TAG, "调用了onNetSuccess方法，调用者reqTag=" + reqTag + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    CCLogUtil.printLog("d", LOG_TAG, "调用了onNetSuccess方法，调用者reqTag=" + reqTag + ",但响应数据不是TestRespObj类型");
                }

            } else {
                CCLogUtil.printLog("d", LOG_TAG, "调用了onNetSuccess方法，调用者reqTag=" + reqTag + ",但响应数据response == null");
            }

        }

        @Override
        public <T> void onNetFail(Object reqTag, Throwable t) {

        }

        @Override
        public <T> void onRequestSuccess(Object reqTag, T response, int dataSourceMode) {

            if (response != null) {

                if (response instanceof TestRespObj) {
                    CCLogUtil.printLog("d", LOG_TAG, "调用了onSuccess方法，调用者reqTag=" + reqTag + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    CCLogUtil.printLog("d", LOG_TAG, "调用了onSuccess方法，调用者reqTag=" + reqTag + ",但响应数据不是TestRespObj类型");
                }

            } else {
                CCLogUtil.printLog("d", LOG_TAG, "调用了onSuccess方法，调用者reqTag=" + reqTag + ",但响应数据response == null");
            }
        }

        @Override
        public <T> void onRequestFail(Object reqTag, Throwable t) {
            CCLogUtil.printLog("d", LOG_TAG, "调用了onError方法，调用者reqTag=" + reqTag, t);
        }

        @Override
        public <T> void onRequestComplete(Object reqTag) {
            CCLogUtil.printLog("d", LOG_TAG, "调用了onComplete方法，调用者reqTag=" + reqTag);


        }

        @Override
        public <T> void onProgress(Object reqTag, int progress, long netSpeed, long completedSize, long fileSize) {

        }

        @Override
        public <T> void onProgressSave(Object reqTag, int progress, long netSpeed, long completedSize, long fileSize) {

        }

        @Override
        public void onIntervalCallback() {
            Toast.makeText(OrdinaryRequestActivity.this, "网络状态较差123123", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 缓存保存回调，用户实现，实现自己的缓存存储策略
     */
    private class RxNetCacheSaveListener implements CCCacheSaveListener {

        @Override
        public <T> void onSaveToMemory(String cacheKey, T response) {

            if (response != null) {

                if (response instanceof TestRespObj) {

                    CCLogUtil.printLog("d", LOG_TAG, "调用了onSaveMemory方法，调用者cacheKey=" + cacheKey + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    CCLogUtil.printLog("d", LOG_TAG, "调用了onSaveMemory方法，调用者cacheKey=" + cacheKey + ",但响应数据不是TestRespObj类型");
                }

            } else {
                CCLogUtil.printLog("d", LOG_TAG, "调用了onSaveMemory方法，调用者cacheKey=" + cacheKey + ",但响应数据response == null");
            }

        }

        @Override
        public <T> void onSaveToDisk(String cacheKey, T response) {

            if (response != null) {

                if (response instanceof TestRespObj) {

                    CCLogUtil.printLog("d", LOG_TAG, "调用了onSaveDisk方法，调用者cacheKey=" + cacheKey + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    CCLogUtil.printLog("d", LOG_TAG, "调用了onSaveDisk方法，调用者cacheKey=" + cacheKey + ",但响应数据不是TestRespObj类型");
                }

            } else {
                CCLogUtil.printLog("d", LOG_TAG, "调用了onSaveDisk方法，调用者cacheKey=" + cacheKey + ",但响应数据response == null");
            }

        }
    }

    /**
     * 缓存查询回调，用户实现，实现自己的缓存查询策略，与缓存存储策略配合，实现自己的缓存机制
     */
    private class RxNetCacheQueryListener implements CCCacheQueryListener {

        @Override
        public <T> T onQueryFromMemory(String cacheKey) {

            CCLogUtil.printLog("d", LOG_TAG, "调用了onQueryFromMemory方法，调用者cacheKey=" + cacheKey);

            TestRespObj testRespObj = new TestRespObj();
            testRespObj.setStatusCode(220);
            testRespObj.setContent("data is queryed from memory");

            try {
                Thread.sleep(2000);
            } catch (Exception e) {

            }

            return (T) testRespObj;
        }

        @Override
        public <T> T onQueryFromDisk(String cacheKey) {

            CCLogUtil.printLog("d", LOG_TAG, "调用了onQueryFromDisk方法，调用者cacheKey=" + cacheKey);

            TestRespObj testRespObj = new TestRespObj();
            testRespObj.setStatusCode(221);
            testRespObj.setContent("data is queryed from disk");

            try {
                Thread.sleep(1000);
            } catch (Exception e) {

            }

            return (T) testRespObj;
        }
    }

}
