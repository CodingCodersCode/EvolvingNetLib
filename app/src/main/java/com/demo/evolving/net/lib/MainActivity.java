package com.demo.evolving.net.lib;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.codingcoderscode.evolving.base.CCBaseRxAppCompactActivity;
import com.codingcoderscode.evolving.net.CCRxNetManager;
import com.codingcoderscode.evolving.net.cache.mode.CCCacheMode;
import com.codingcoderscode.evolving.net.request.CCDownloadRequest;
import com.codingcoderscode.evolving.net.request.callback.CCCacheQueryCallback;
import com.codingcoderscode.evolving.net.request.callback.CCCacheSaveCallback;
import com.codingcoderscode.evolving.net.request.callback.CCNetCallback;
import com.codingcoderscode.evolving.net.request.canceler.CCCanceler;
import com.codingcoderscode.evolving.net.request.entity.CCFile;
import com.codingcoderscode.evolving.net.response.CCBaseResponse;
import com.codingcoderscode.evolving.net.util.NetLogUtil;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.squareup.leakcanary.RefWatcher;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends CCBaseRxAppCompactActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private static final String LOG_TAG = "SecondActivity";

    private TextView tvBtn;

    private TextView tvSelect;

    private TextView tvFileInfoShow;

    private TextView tvDownload;
    private TextView tvPauseDownload;
    private TextView tvResumeDownload;

    private boolean isDestroyed = false;

    private CCDownloadRequest downloadRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //测试简单的请求
        this.tvBtn = (TextView) findViewById(R.id.tv_btn);
        this.tvBtn.setOnClickListener(this);


        //上传模块
        this.tvFileInfoShow = (TextView) findViewById(R.id.tv_file_info_show);
        this.tvSelect = (TextView) findViewById(R.id.tv_select);
        this.tvSelect.setOnClickListener(this);


        //下载模块-开始下载
        this.tvDownload = (TextView)findViewById(R.id.tv_download);
        this.tvDownload.setOnClickListener(this);
        //下载模块-暂停下载
        this.tvPauseDownload = (TextView)findViewById(R.id.tv_pause);
        this.tvPauseDownload.setOnClickListener(this);
        //下载模块-继续下载
        this.tvResumeDownload = (TextView)findViewById(R.id.tv_resume);
        this.tvResumeDownload.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        super.onDestroy();

        RefWatcher refWatcher = CCApplication.refWatcher;
        refWatcher.watch(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.tv_select:
                onSelectImg();
                break;
            case R.id.tv_btn:
                /**
                 * RxJava/Android+Retrofit+OkHttp封装库使用方法
                 */
                onStartRxRetrofitOkHttpRequest();
                break;
            case R.id.tv_download:
                onStartDownloadMethodTest();
                break;
            case R.id.tv_pause:
                downloadRequest.getNetCCCanceler().cancel();
                break;
            case R.id.tv_resume:
                downloadRequest.executeAsync();
                break;
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == 100) {
                List<ImageItem> images = (List<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);

                onStartUploadMethodTest(images);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        //Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    private void onSelectImg(){
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());
        imagePicker.setShowCamera(true);
        imagePicker.setSelectLimit(9);
        imagePicker.setCrop(false);
        Intent intent = new Intent(getApplicationContext(), ImageGridActivity.class);
        startActivityForResult(intent, 100);
    }

    /**
     * 测试下载功能
     */
    @AfterPermissionGranted(2001)
    private void onStartDownloadMethodTest(){

        try{

            String[] perms = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (EasyPermissions.hasPermissions(this, perms)) {

                Map<String, String> commonHeaderMap = new HashMap<>();
                commonHeaderMap.put("common_header_param1", "common_header_value1");
                commonHeaderMap.put("common_header_param2", "common_header_value2");
                commonHeaderMap.put("common_header_param3", "common_header_value3");

                Map<String, String> specifyHeaderMap = new HashMap<>();
                specifyHeaderMap.put("specify_header_param1", "specify_header_value1");
                specifyHeaderMap.put("specify_header_param2", "specify_header_value2");
                specifyHeaderMap.put("specify_header_param3", "specify_header_value3");


                Map<String, String> paramMap = new HashMap<>();
                paramMap.put("logic_txt_param1", "logic_txt_value1");
                paramMap.put("logic_txt_param2", "logic_txt_value2");
                paramMap.put("logic_txt_param3", "logic_txt_value3");

                Map<String, String> pathMap = new HashMap<String, String>();
                pathMap.put("{path1}", "path1_value1");
                pathMap.put("{path2}", "path1_value2");
                pathMap.put("{path3}", "path1_value3");
                pathMap.put("{path4}", "path1_value4");
                pathMap.put("{path5}", "path1_value5");


                //CCRxNetManager测试代码
                CCRxNetManager ccRxNetManager = new CCRxNetManager.Builder()
                        .baseUrl("http://sw.bos.baidu.com/")
                        .callAdapterFactory(RxJava2CallAdapterFactory.create())
                        .converterFactory(GsonConverterFactory.create())
                        .commonHeaders(commonHeaderMap)
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .enableLogInterceptor(true)
                        .build();

                downloadRequest = CCRxNetManager.<String>download("sw-search-sp/software/16d5a98d3e034/QQ_8.9.5.22062_setup.exe")
                        .setHeaderMap(specifyHeaderMap)
                        .setPathMap(pathMap)
                        //.setParamMap(paramMap)
                        .setFileSaveName("test_OkGo_apk_file_download.apk")
                        .setRetryCount(3)
                        .setCacheQueryMode(CCCacheMode.QueryMode.MODE_ONLY_NET)
                        .setCacheSaveMode(CCCacheMode.SaveMode.MODE_NO_CACHE)
                        .setReqTag("test_login_req_tag")
                        .setCacheKey("test_login_req_cache_key")
                        .setSupportRage(true)
                        .setDeleteExistFile(false)
                        //.setAutoRange(true)
                        .setCCNetCallback(new RxNetDownloadCalback())
                        //.setCcDownloadProgressCallback(new RxNetDownloadCalback())
                        .setNetLifecycleComposer(this.<CCBaseResponse<String>>bindUntilEvent(ActivityEvent.DESTROY))
                        .setResponseBeanType(TestRespObj.class);

                downloadRequest.executeAsync();

            }else {
                EasyPermissions.requestPermissions(this, "This operation needs access to write and read external storage.", 2001, perms);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 测试上传方法
     * @param images
     */
    private void onStartUploadMethodTest(List<ImageItem> images){

        try {

            //所有Http请求的公共header信息
            //指定的Http请求所独有的header信息
            //请求参数信息
            //restful api中的path信息
            Map<String, String> commonHeaderMap = new HashMap<>();
            commonHeaderMap.put("common_header_param1", "common_header_value1");
            commonHeaderMap.put("common_header_param2", "common_header_value2");
            commonHeaderMap.put("common_header_param3", "common_header_value3");

            Map<String, String> specifyHeaderMap = new HashMap<>();
            specifyHeaderMap.put("specify_header_param1", "specify_header_value1");
            specifyHeaderMap.put("specify_header_param2", "specify_header_value2");
            specifyHeaderMap.put("specify_header_param3", "specify_header_value3");


            Map<String, String> txtParamMap = new HashMap<>();
            txtParamMap.put("logic_txt_param1", "logic_txt_value1");
            txtParamMap.put("logic_txt_param2", "logic_txt_value2");
            txtParamMap.put("logic_txt_param3", "logic_txt_value3");

            Map<String, CCFile> fileParamMap = new HashMap<>();

            String fileInfoStr = "";

            for (int i = 0; i < images.size(); i++){

                ImageItem imageItem = images.get(i);
                fileParamMap.put("fileKey" + i, new CCFile(imageItem.path, "application/octet-stream"));

                fileInfoStr = fileInfoStr + "fileKey" + i + "===>" + imageItem.path + "\n";
            }

            this.tvFileInfoShow.setText(fileInfoStr);

            Map<String, String> pathMap = new HashMap<String, String>();
            pathMap.put("{path1}", "path1_value1");
            pathMap.put("{path2}", "path1_value2");
            pathMap.put("{path3}", "path1_value3");
            pathMap.put("{path4}", "path1_value4");
            pathMap.put("{path5}", "path1_value5");

            //CCRxNetManager测试代码
            CCRxNetManager ccRxNetManager = new CCRxNetManager.Builder()
                    .baseUrl("http://server.jeasonlzy.com/OkHttpUtils/")
                    .callAdapterFactory(RxJava2CallAdapterFactory.create())
                    .converterFactory(GsonConverterFactory.create())
                    .commonHeaders(commonHeaderMap)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .enableLogInterceptor(true)
                    .build();

            CCRxNetManager.<String>upload("upload")
                    .setHeaderMap(specifyHeaderMap)
                    .setPathMap(pathMap)
                    .setTxtParamMap(txtParamMap)
                    .setFileParamMap(fileParamMap)
                    .setRetryCount(0)
                    .setCacheQueryMode(CCCacheMode.QueryMode.MODE_ONLY_NET)
                    .setCacheSaveMode(CCCacheMode.SaveMode.MODE_NO_CACHE)
                    .setReqTag("test_login_req_tag")
                    .setCacheKey("test_login_req_cache_key")
                    .setCCNetCallback(new RxNetUploadProgressCallback())
                    //.setCcUploadProgressCallback(new RxNetUploadProgressCallback())
                    .setNetLifecycleComposer(this.<CCBaseResponse<String>>bindUntilEvent(ActivityEvent.DESTROY))
                    .setResponseBeanType(TestRespObj.class)
                    .executeAsync();

        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    /**
     * RxJava/Android+Retrofit+OkHttp封装库使用方法
     */
    private void onStartRxRetrofitOkHttpRequest() {
        try {

            //所有Http请求的公共header信息
            Map<String, String> commonHeaderMap = new HashMap<>();
            commonHeaderMap.put("common_header_param1", "common_header_value1");
            commonHeaderMap.put("common_header_param2", "common_header_value2");
            commonHeaderMap.put("common_header_param3", "common_header_value3");

            //指定的Http请求所独有的header信息
            Map<String, String> specifyHeaderMap = new HashMap<>();
            specifyHeaderMap.put("specify_header_param1", "specify_header_value1");
            specifyHeaderMap.put("specify_header_param2", "specify_header_value2");
            specifyHeaderMap.put("specify_header_param3", "specify_header_value3");

            //请求参数信息
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("logic_param1", "logic_value1——这是段中文文本");
            paramMap.put("logic_param2", "logic_value2");
            paramMap.put("logic_param3", "logic_value3");

            //restful api中的path信息
            Map<String, String> pathMap = new HashMap<String, String>();
            pathMap.put("{path1}", "path1_value1");
            pathMap.put("{path2}", "path1_value2");
            pathMap.put("{path3}", "path1_value3");
            pathMap.put("{path4}", "path1_value4");
            pathMap.put("{path5}", "path1_value5");

            //CCRxNetManager测试代码
            CCRxNetManager ccRxNetManager = new CCRxNetManager.Builder()
                    .baseUrl("http://mobile.huiedu.com.cn")
                    .callAdapterFactory(RxJava2CallAdapterFactory.create())
                    .converterFactory(GsonConverterFactory.create())
                    .commonHeaders(commonHeaderMap)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .enableLogInterceptor(true)
                    .build();

            CCRxNetManager.<TestRespObj>get("/web/userController/login.do")
                    .setHeaderMap(specifyHeaderMap)
                    .setPathMap(pathMap)
                    .setParamMap(paramMap)
                    .setRetryCount(3)
                    .setRetryDelayTimeMillis(3000)
                    .setCacheQueryMode(CCCacheMode.QueryMode.MODE_MEMORY_THEN_DISK_THEN_NET)
                    .setCacheSaveMode(CCCacheMode.SaveMode.MODE_SAVE_MEMORY_AND_DISK)
                    .setReqTag("test_login_req_tag")
                    .setCacheKey("test_login_req_cache_key")
                    .setCCNetCallback(new RxNetManagerCallback())
                    .setCCCacheSaveCallback(new RxNetCacheSaveCallback())
                    .setCCCacheQueryCallback(new RxNetCacheQueryCallback())
                    .setNetLifecycleComposer(this.<CCBaseResponse<TestRespObj>>bindUntilEvent(ActivityEvent.DESTROY))
                    .setResponseBeanType(TestRespObj.class)
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

            NetLogUtil.printLog("e", LOG_TAG, "调用了onStartRequest方法，调用者reqTag=" + reqTag);

        }

        @Override
        public <T> void onCacheQuerySuccess(Object reqTag, T response) {


            if (response != null) {

                if (response instanceof TestRespObj) {
                    NetLogUtil.printLog("e", LOG_TAG, "调用了onCacheSuccess方法，调用者reqTag=" + reqTag + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    NetLogUtil.printLog("e", LOG_TAG, "调用了onCacheSuccess方法，调用者reqTag=" + reqTag + ",但响应数据不是TestRespObj类型");
                }

            } else {
                NetLogUtil.printLog("e", LOG_TAG, "调用了onCacheSuccess方法，调用者reqTag=" + reqTag + ",但响应数据response == null");
            }
        }

        @Override
        public <T> void onMemoryCacheQuerySuccess(Object reqTag, T response) {

            if (response != null) {

                if (response instanceof TestRespObj) {
                    NetLogUtil.printLog("e", LOG_TAG, "调用了onMemoryCacheSuccess方法，调用者reqTag=" + reqTag + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    NetLogUtil.printLog("e", LOG_TAG, "调用了onMemoryCacheSuccess方法，调用者reqTag=" + reqTag + ",但响应数据不是TestRespObj类型");
                }

            } else {
                NetLogUtil.printLog("e", LOG_TAG, "调用了onMemoryCacheSuccess方法，调用者reqTag=" + reqTag + ",但响应数据response == null");
            }


        }

        @Override
        public <T> void onDiskCacheQuerySuccess(Object reqTag, T response) {

            if (response != null) {

                if (response instanceof TestRespObj) {
                    NetLogUtil.printLog("e", LOG_TAG, "调用了onDiskCacheSuccess方法，调用者reqTag=" + reqTag + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    NetLogUtil.printLog("e", LOG_TAG, "调用了onDiskCacheSuccess方法，调用者reqTag=" + reqTag + ",但响应数据不是TestRespObj类型");
                }

            } else {
                NetLogUtil.printLog("e", LOG_TAG, "调用了onDiskCacheSuccess方法，调用者reqTag=" + reqTag + ",但响应数据response == null");
            }

        }

        @Override
        public <T> void onNetSuccess(Object reqTag, T response) {


            if (response != null) {

                if (response instanceof TestRespObj) {
                    NetLogUtil.printLog("e", LOG_TAG, "调用了onNetSuccess方法，调用者reqTag=" + reqTag + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    NetLogUtil.printLog("e", LOG_TAG, "调用了onNetSuccess方法，调用者reqTag=" + reqTag + ",但响应数据不是TestRespObj类型");
                }

            } else {
                NetLogUtil.printLog("e", LOG_TAG, "调用了onNetSuccess方法，调用者reqTag=" + reqTag + ",但响应数据response == null");
            }

        }

        @Override
        public <T> void onSuccess(Object reqTag, T response) {

            if (response != null) {

                if (response instanceof TestRespObj) {
                    NetLogUtil.printLog("e", LOG_TAG, "调用了onSuccess方法，调用者reqTag=" + reqTag + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    NetLogUtil.printLog("e", LOG_TAG, "调用了onSuccess方法，调用者reqTag=" + reqTag + ",但响应数据不是TestRespObj类型");
                }

            } else {
                NetLogUtil.printLog("e", LOG_TAG, "调用了onSuccess方法，调用者reqTag=" + reqTag + ",但响应数据response == null");
            }
        }

        @Override
        public <T> void onError(Object reqTag, Throwable t) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了onError方法，调用者reqTag=" + reqTag, t);
        }

        @Override
        public <T> void onComplete(Object reqTag) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了onComplete方法，调用者reqTag=" + reqTag);


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

                    NetLogUtil.printLog("e", LOG_TAG, "调用了onSaveMemory方法，调用者cacheKey=" + cacheKey + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    NetLogUtil.printLog("e", LOG_TAG, "调用了onSaveMemory方法，调用者cacheKey=" + cacheKey + ",但响应数据不是TestRespObj类型");
                }

            } else {
                NetLogUtil.printLog("e", LOG_TAG, "调用了onSaveMemory方法，调用者cacheKey=" + cacheKey + ",但响应数据response == null");
            }

        }

        @Override
        public <T> void onSaveToDisk(String cacheKey, T response) {

            if (response != null) {

                if (response instanceof TestRespObj) {

                    NetLogUtil.printLog("e", LOG_TAG, "调用了onSaveDisk方法，调用者cacheKey=" + cacheKey + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    NetLogUtil.printLog("e", LOG_TAG, "调用了onSaveDisk方法，调用者cacheKey=" + cacheKey + ",但响应数据不是TestRespObj类型");
                }

            } else {
                NetLogUtil.printLog("e", LOG_TAG, "调用了onSaveDisk方法，调用者cacheKey=" + cacheKey + ",但响应数据response == null");
            }

        }
    }

    /**
     * 缓存查询回调，用户实现，实现自己的缓存查询策略，与缓存存储策略配合，实现自己的缓存机制
     */
    private class RxNetCacheQueryCallback implements CCCacheQueryCallback {

        @Override
        public <T> T onQueryFromMemory(String cacheKey) {

            NetLogUtil.printLog("e", LOG_TAG, "调用了onQueryFromMemory方法，调用者cacheKey=" + cacheKey);

            TestRespObj testRespObj = new TestRespObj();
            testRespObj.setStatusCode(220);
            testRespObj.setContent("data is queryed from memory");

            return (T) testRespObj;
        }

        @Override
        public <T> T onQueryFromDisk(String cacheKey) {

            NetLogUtil.printLog("e", LOG_TAG, "调用了onQueryFromDisk方法，调用者cacheKey=" + cacheKey);

            TestRespObj testRespObj = new TestRespObj();
            testRespObj.setStatusCode(221);
            testRespObj.setContent("data is queryed from disk");


            return (T) testRespObj;
        }
    }


    /**
     * 上传进度回调
     */
    private class RxNetUploadProgressCallback extends CCNetCallback {
        @Override
        public <T> void onStartRequest(Object reqTag, CCCanceler canceler) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了RxNetUploadProgressCallback.onStartRequest方法，调用者reqTag=" + reqTag);
        }

        @Override
        public <T> void onSuccess(Object reqTag, T response) {
            if (response != null) {

                if (response instanceof TestRespObj) {
                    NetLogUtil.printLog("e", LOG_TAG, "调用了onSuccess方法，调用者reqTag=" + reqTag + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    NetLogUtil.printLog("e", LOG_TAG, "调用了onSuccess方法，调用者reqTag=" + reqTag + ",但响应数据不是TestRespObj类型");
                }

            } else {
                NetLogUtil.printLog("e", LOG_TAG, "调用了onSuccess方法，调用者reqTag=" + reqTag + ",但响应数据response == null");
            }
        }

        @Override
        public <T> void onError(Object reqTag, Throwable t) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了RxNetUploadProgressCallback.onError方法，调用者reqTag=" + reqTag, t);
        }

        @Override
        public <T> void onComplete(Object reqTag) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了RxNetUploadProgressCallback.onComplete方法，调用者reqTag=" + reqTag);
        }

        @Override
        public <T> void onProgress(Object tag, int progress, long netSpeed, long completedSize, long fileSize) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了RxNetUploadProgressCallback.onProgress方法，调用者tag=" + tag + ",progress=" + progress + ",netSpeed=" + netSpeed + ",completedSize=" + completedSize + ",fileSize=" + fileSize);
        }
    }


    /**
     * 下载进度回调
     */
    private class RxNetDownloadCalback extends CCNetCallback {
        @Override
        public <T> void onStartRequest(Object reqTag, CCCanceler canceler) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了RxNetDownloadCalback.onStart方法，调用者reqTag=" + reqTag);
        }

        @Override
        public <T> void onSuccess(Object reqTag, T response) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了RxNetDownloadCalback.onSuccess方法，调用者reqTag=" + reqTag);
        }

        @Override
        public <T> void onError(Object reqTag, Throwable t) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了RxNetDownloadCalback.onError方法，调用者reqTag=" + reqTag, t);
        }

        @Override
        public <T> void onComplete(Object reqTag) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了RxNetDownloadCalback.onComplete方法，调用者reqTag=" + reqTag);
        }

        @Override
        public <T> void onProgress(Object tag, int progress, long netSpeed, long completedSize, long fileSize) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了RxNetDownloadCalback.onProgress方法，调用者tag=" + tag + ",progress=" + progress + "，netSpeed=" + netSpeed + "，completedSize=" + completedSize + "，fileSize=" + fileSize);
        }
    }
}
