package com.demo.evolving.net.lib.democode;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.codingcoderscode.evolving.base.CCBaseRxAppCompactActivity;
import com.codingcoderscode.evolving.net.CCRxNetManager;
import com.codingcoderscode.evolving.net.cache.mode.CCCacheMode;
import com.codingcoderscode.evolving.net.request.callback.CCCacheQueryCallback;
import com.codingcoderscode.evolving.net.request.callback.CCCacheSaveCallback;
import com.codingcoderscode.evolving.net.request.callback.CCNetCallback;
import com.codingcoderscode.evolving.net.request.canceler.CCCanceler;
import com.codingcoderscode.evolving.net.response.CCBaseResponse;
import com.codingcoderscode.evolving.net.util.NetLogUtil;
import com.demo.evolving.net.lib.R;
import com.demo.evolving.net.lib.TestRespObj;
import com.demo.evolving.net.lib.bean.SampleRespBeanWrapper;
import com.demo.evolving.net.lib.bean.SampleResponseBean;
import com.google.gson.reflect.TypeToken;
import com.trello.rxlifecycle2.android.ActivityEvent;

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

            CCRxNetManager.<SampleRespBeanWrapper<SampleResponseBean>>post("/zuul/{path1}/{path2}/biz/v1/login")
                    .setHeaderMap(specifyHeaderMap)
                    .setPathMap(pathMap)
                    .setParamMap(paramMap)
                    .setRetryCount(0)
                    .setRetryDelayTimeMillis(3000)
                    .setCacheQueryMode(CCCacheMode.QueryMode.MODE_MEMORY_AND_DISK_AND_NET)
                    .setCacheSaveMode(CCCacheMode.SaveMode.MODE_SAVE_MEMORY_AND_DISK)

                    .setNeedToCheckNetCondition(true)
                    .setNetConditionCheckInterval(5)

                    .setReqTag("test_login_req_tag")
                    .setCacheKey("test_login_req_cache_key")
                    .setCCNetCallback(new RxNetManagerCallback())
                    .setCCCacheSaveCallback(new RxNetCacheSaveCallback())
                    .setCCCacheQueryCallback(new RxNetCacheQueryCallback())
                    .setNetLifecycleComposer(this.<CCBaseResponse<SampleRespBeanWrapper<SampleResponseBean>>>bindUntilEvent(ActivityEvent.DESTROY))
                    .setResponseBeanType(typeToken.getType())
                    .executeAsync();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 数据请求结果回调
     */
    private class RxNetManagerCallback extends CCNetCallback {

        @Override
        public <T> void onStartRequest(Object reqTag, CCCanceler canceler) {

            NetLogUtil.printLog("d", LOG_TAG, "调用了onStartRequest方法，调用者reqTag=" + reqTag);

        }

        @Override
        public <T> void onCacheQuerySuccess(Object reqTag, T response) {


            if (response != null) {

                if (response instanceof TestRespObj) {
                    NetLogUtil.printLog("d", LOG_TAG, "调用了onCacheSuccess方法，调用者reqTag=" + reqTag + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    NetLogUtil.printLog("d", LOG_TAG, "调用了onCacheSuccess方法，调用者reqTag=" + reqTag + ",但响应数据不是TestRespObj类型");
                }

            } else {
                NetLogUtil.printLog("d", LOG_TAG, "调用了onCacheSuccess方法，调用者reqTag=" + reqTag + ",但响应数据response == null");
            }
        }

        @Override
        public <T> void onMemoryCacheQuerySuccess(Object reqTag, T response) {

            if (response != null) {

                if (response instanceof TestRespObj) {
                    NetLogUtil.printLog("d", LOG_TAG, "调用了onMemoryCacheSuccess方法，调用者reqTag=" + reqTag + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    NetLogUtil.printLog("d", LOG_TAG, "调用了onMemoryCacheSuccess方法，调用者reqTag=" + reqTag + ",但响应数据不是TestRespObj类型");
                }

            } else {
                NetLogUtil.printLog("d", LOG_TAG, "调用了onMemoryCacheSuccess方法，调用者reqTag=" + reqTag + ",但响应数据response == null");
            }


        }

        @Override
        public <T> void onDiskCacheQuerySuccess(Object reqTag, T response) {

            if (response != null) {

                if (response instanceof TestRespObj) {
                    NetLogUtil.printLog("d", LOG_TAG, "调用了onDiskCacheSuccess方法，调用者reqTag=" + reqTag + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    NetLogUtil.printLog("d", LOG_TAG, "调用了onDiskCacheSuccess方法，调用者reqTag=" + reqTag + ",但响应数据不是TestRespObj类型");
                }

            } else {
                NetLogUtil.printLog("d", LOG_TAG, "调用了onDiskCacheSuccess方法，调用者reqTag=" + reqTag + ",但响应数据response == null");
            }

        }

        @Override
        public <T> void onNetSuccess(Object reqTag, T response) {


            if (response != null) {

                if (response instanceof TestRespObj) {
                    NetLogUtil.printLog("d", LOG_TAG, "调用了onNetSuccess方法，调用者reqTag=" + reqTag + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    NetLogUtil.printLog("d", LOG_TAG, "调用了onNetSuccess方法，调用者reqTag=" + reqTag + ",但响应数据不是TestRespObj类型");
                }

            } else {
                NetLogUtil.printLog("d", LOG_TAG, "调用了onNetSuccess方法，调用者reqTag=" + reqTag + ",但响应数据response == null");
            }

        }

        @Override
        public <T> void onSuccess(Object reqTag, T response) {

            if (response != null) {

                if (response instanceof TestRespObj) {
                    NetLogUtil.printLog("d", LOG_TAG, "调用了onSuccess方法，调用者reqTag=" + reqTag + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    NetLogUtil.printLog("d", LOG_TAG, "调用了onSuccess方法，调用者reqTag=" + reqTag + ",但响应数据不是TestRespObj类型");
                }

            } else {
                NetLogUtil.printLog("d", LOG_TAG, "调用了onSuccess方法，调用者reqTag=" + reqTag + ",但响应数据response == null");
            }
        }

        @Override
        public <T> void onError(Object reqTag, Throwable t) {
            NetLogUtil.printLog("d", LOG_TAG, "调用了onError方法，调用者reqTag=" + reqTag, t);
        }

        @Override
        public <T> void onComplete(Object reqTag) {
            NetLogUtil.printLog("d", LOG_TAG, "调用了onComplete方法，调用者reqTag=" + reqTag);


        }

        @Override
        public void onToastNetBadCondition() {
            Toast.makeText(OrdinaryRequestActivity.this, "网络状态较差", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 缓存保存回调，用户实现，实现自己的缓存存储策略
     */
    private class RxNetCacheSaveCallback implements CCCacheSaveCallback {

        @Override
        public <T> void onSaveToMemory(String cacheKey, T response) {

            if (response != null) {

                if (response instanceof TestRespObj) {

                    NetLogUtil.printLog("d", LOG_TAG, "调用了onSaveMemory方法，调用者cacheKey=" + cacheKey + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    NetLogUtil.printLog("d", LOG_TAG, "调用了onSaveMemory方法，调用者cacheKey=" + cacheKey + ",但响应数据不是TestRespObj类型");
                }

            } else {
                NetLogUtil.printLog("d", LOG_TAG, "调用了onSaveMemory方法，调用者cacheKey=" + cacheKey + ",但响应数据response == null");
            }

        }

        @Override
        public <T> void onSaveToDisk(String cacheKey, T response) {

            if (response != null) {

                if (response instanceof TestRespObj) {

                    NetLogUtil.printLog("d", LOG_TAG, "调用了onSaveDisk方法，调用者cacheKey=" + cacheKey + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    NetLogUtil.printLog("d", LOG_TAG, "调用了onSaveDisk方法，调用者cacheKey=" + cacheKey + ",但响应数据不是TestRespObj类型");
                }

            } else {
                NetLogUtil.printLog("d", LOG_TAG, "调用了onSaveDisk方法，调用者cacheKey=" + cacheKey + ",但响应数据response == null");
            }

        }
    }

    /**
     * 缓存查询回调，用户实现，实现自己的缓存查询策略，与缓存存储策略配合，实现自己的缓存机制
     */
    private class RxNetCacheQueryCallback implements CCCacheQueryCallback {

        @Override
        public <T> T onQueryFromMemory(String cacheKey) {

            NetLogUtil.printLog("d", LOG_TAG, "调用了onQueryFromMemory方法，调用者cacheKey=" + cacheKey);

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

            NetLogUtil.printLog("d", LOG_TAG, "调用了onQueryFromDisk方法，调用者cacheKey=" + cacheKey);

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
