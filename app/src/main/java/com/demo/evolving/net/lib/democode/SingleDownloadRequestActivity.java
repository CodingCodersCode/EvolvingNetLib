package com.demo.evolving.net.lib.democode;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.codingcoderscode.evolving.base.CCBaseRxAppCompactActivity;
import com.codingcoderscode.evolving.net.request.base.CCSimpleDownloadRequest;
import com.codingcoderscode.evolving.net.request.listener.CCSingleDownloadProgressListener;
import com.codingcoderscode.evolving.net.request.listener.CCNetResultListener;
import com.codingcoderscode.evolving.net.request.canceler.CCCanceler;
import com.codingcoderscode.evolving.net.request.entity.CCDownloadTask;
import com.codingcoderscode.evolving.net.util.CCLogUtil;
import com.demo.evolving.net.lib.CCApplication;
import com.demo.evolving.net.lib.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by CodingCodersCode on 2017/12/1.
 */

public class SingleDownloadRequestActivity extends CCBaseRxAppCompactActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private final String LOG_TAG = getClass().getCanonicalName();

    private TextView tv_start_download;
    private TextView tv_pause_download;
    private TextView tv_resume_download;
    private TextView tv_file_source;
    private TextView tv_file_download_status;
    private TextView tv_file_download_progress;

    private CCSimpleDownloadRequest downloadRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_download_request);

        initView();
    }

    private void initView(){
        this.tv_start_download = (TextView)findViewById(R.id.tv_start_download);
        this.tv_start_download.setOnClickListener(this);

        this.tv_pause_download = (TextView)findViewById(R.id.tv_pause_download);
        this.tv_pause_download.setOnClickListener(this);

        this.tv_resume_download = (TextView)findViewById(R.id.tv_resume_download);
        this.tv_resume_download.setOnClickListener(this);

        this.tv_file_source = (TextView)findViewById(R.id.tv_file_source);

        this.tv_file_download_status = (TextView)findViewById(R.id.tv_file_download_status);

        this.tv_file_download_progress = (TextView)findViewById(R.id.tv_file_download_progress);

        this.tv_file_source.setText("http://server.jeasonlzy.com/OkHttpUtils/download");
        this.tv_file_download_status.setText("未开始下载");
        this.tv_file_download_progress.setText("0%");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_start_download:
                onStartDownloadMethodTest();
                break;
            case R.id.tv_pause_download:
                downloadRequest.cancel();
                break;
            case R.id.tv_resume_download:
                downloadRequest.executeAsync();
                break;
        }
    }

    /**
     * 测试下载功能
     */
    @AfterPermissionGranted(2001)
    private void onStartDownloadMethodTest(){

        try{

            String[] perms = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (EasyPermissions.hasPermissions(this, perms)) {

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

                String downloadUrl = "/16891/371C7C353C7B87011FB3DE8B12BCBCA5.apk?fsname=com.tencent.mm_7.0.0_1380.apk&csr=1bbd";

                downloadRequest = ((CCApplication)this.getApplicationContext()).getCcRxNetManager().<Void>download(downloadUrl)//创建指定下载路径的下载请求
                        .setFileSaveName("test_OkGo_apk_file_download.apk")//设置下载文件本地保存名称
                        .setSupportRage(true)//设置是否支持断点
                        .setDownloadProgressListener(new DownloadProgressListenerImpl())
                        .setCCDownloadFileWriteListener(null)//设置自定义的文件下载数据本地写入回调
                        .setHeaderMap(specifyHeaderMap)//设置当前请求的特别header信息
                        .setPathMap(pathMap)//设置restful api的路径替换信息，作用同Retrofit的@Path
                        .setRetryCount(3)//设置失败重试次数，具体重试次数根据RxJava/Android对异常类型的判断有关
                        //.setCacheQueryMode(CCCMode.QueryMode.MODE_NET)//设置缓存查询策略
                        //.setCacheSaveMode(CCCMode.SaveMode.MODE_NONE)//设置缓存保存策略
                        .setReqTag("test_login_req_tag");//设置请求标识

                downloadRequest.executeAsync();//异步执行请求

            }else {
                EasyPermissions.requestPermissions(this, "This operation needs access to write and read external storage.", 2001, perms);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    private int gProgress = 0;
    private String gNetSpeed = "0B/s";
    private long gCompletedSize = 0;
    private long gFileSize = 0;

    /**
     * 下载进度回调
     */
    private class RxNetDownloadCalback implements CCNetResultListener {
        @Override
        public <T> void onStartRequest(Object reqTag, CCCanceler canceler) {
            tv_file_download_status.setText("开始下载");
            tv_file_download_progress.setText("下载进度：" + gProgress + "%\n下载速度：" + gNetSpeed + "\n文件大小：" + gFileSize + "B\n已下载大小：" + gCompletedSize + "B");
        }

        @Override
        public <T> void onDiskCacheQuerySuccess(Object reqTag, T response) {

        }

        @Override
        public <T> void onDiskCacheQueryFail(Object reqTag, Throwable t) {

        }

        @Override
        public <T> void onNetSuccess(Object reqTag, T response) {

        }

        @Override
        public <T> void onNetFail(Object reqTag, Throwable t) {

        }

        @Override
        public <T> void onRequestSuccess(Object reqTag, T response, int dataSourceMode) {
            tv_file_download_status.setText("下载成功");
            tv_file_download_progress.setText("下载进度：" + gProgress + "%\n下载速度：0B/s\n文件大小：" + gFileSize + "B\n已下载大小：" + gCompletedSize + "B");
        }

        @Override
        public <T> void onRequestFail(Object reqTag, Throwable t) {
            tv_file_download_status.setText("下载失败，详细信息见log，log窗口:Error");
            tv_file_download_progress.setText("下载进度：" + gProgress + "%\n下载速度：0B/s\n文件大小：" + gFileSize + "B\n已下载大小：" + gCompletedSize + "B");
            CCLogUtil.printLog("e", LOG_TAG, "调用了RxNetDownloadCalback.onError方法，调用者reqTag=" + reqTag, t);
        }

        @Override
        public <T> void onRequestComplete(Object reqTag) {
            tv_file_download_status.setText("下载完成");
            tv_file_download_progress.setText("下载进度：" + gProgress + "%\n下载速度：0B/s\n文件大小：" + gFileSize + "B\n已下载大小：" + gCompletedSize + "B");
        }

        @Override
        public <T> void onProgress(Object tag, int progress, long netSpeed, long completedSize, long fileSize) {


        }

        @Override
        public <T> void onProgressSave(Object reqTag, int progress, long netSpeed, long completedSize, long fileSize) {

        }

        @Override
        public void onIntervalCallback() {

        }
    }


    private class DownloadProgressListenerImpl implements CCSingleDownloadProgressListener {

        @Override
        public void onStart(Object tag, CCDownloadTask downloadTask, CCCanceler canceler) {

        }

        @Override
        public void onProgressSave(Object reqTag, CCDownloadTask downloadTask, int progress, long netSpeed, long completedSize, long fileSize) {

        }

        @Override
        public void onProgress(Object tag, CCDownloadTask downloadTask, int progress, long netSpeed, long downloadedSize, long fileSize) {
            gProgress = progress;
            gCompletedSize = downloadedSize;
            gFileSize = fileSize;

            if (netSpeed > 1024 * 1024){
                gNetSpeed = netSpeed / (1024 * 1024) + "M/s";
            }else if (netSpeed > 1024){
                gNetSpeed = netSpeed / 1024 + "KB/s";
            }else {
                gNetSpeed = netSpeed + "B/s";
            }

            tv_file_download_progress.setText("下载进度：" + gProgress + "%\n" + "下载速度：" + gNetSpeed + "\n文件大小：" + gFileSize + "B\n已下载大小：" + gCompletedSize + "B");

            //CCLogUtil.printLog("d", LOG_TAG, "调用了RxNetDownloadCalback.onProgress方法，调用者tag=" + tag + ",progress=" + progress + "，netSpeed=" + netSpeed + "，completedSize=" + completedSize + "，fileSize=" + fileSize);

        }

        @Override
        public void onSuccess(Object tag, CCDownloadTask downloadTask) {

        }

        @Override
        public void onError(Object tag, CCDownloadTask downloadTask, Throwable t) {

        }

        @Override
        public void onComplete(Object tag, CCDownloadTask downloadTask) {

        }
    }

}
