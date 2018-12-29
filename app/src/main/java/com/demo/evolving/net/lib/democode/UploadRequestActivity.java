package com.demo.evolving.net.lib.democode;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.codingcoderscode.evolving.base.CCBaseRxAppCompactActivity;
import com.codingcoderscode.evolving.net.CCRxNetManager;
import com.codingcoderscode.evolving.net.cache.mode.CCCMode;
import com.codingcoderscode.evolving.net.request.callback.CCNetCallback;
import com.codingcoderscode.evolving.net.request.canceler.CCCanceler;
import com.codingcoderscode.evolving.net.request.entity.CCFile;
import com.codingcoderscode.evolving.net.response.CCBaseResponse;
import com.codingcoderscode.evolving.net.util.NetLogUtil;
import com.demo.evolving.net.lib.GlideImageLoader;
import com.demo.evolving.net.lib.R;
import com.demo.evolving.net.lib.TestRespObj;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by CodingCodersCode on 2017/12/1.
 */

public class UploadRequestActivity extends CCBaseRxAppCompactActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private final String LOG_TAG = getClass().getCanonicalName();

    private TextView tv_start_upload;
    private TextView tv_file_source;
    private TextView tv_file_upload_status;
    private TextView tv_file_upload_progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_request);

        initView();
    }

    private void initView(){

        this.tv_start_upload = (TextView)findViewById(R.id.tv_start_upload);
        this.tv_start_upload.setOnClickListener(this);

        this.tv_file_source = (TextView)findViewById(R.id.tv_file_source);

        this.tv_file_upload_status = (TextView)findViewById(R.id.tv_file_upload_status);

        this.tv_file_upload_progress = (TextView)findViewById(R.id.tv_file_upload_progress);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_start_upload:
                onSelectImg();
                break;
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

    /**
     * 测试上传方法
     * @param images
     */
    private void onStartUploadMethodTest(List<ImageItem> images){

        try {

            Map<String, String> specifyHeaderMap = new HashMap<>();
            specifyHeaderMap.put("specify_header_param1", "specify_header_value1");
            specifyHeaderMap.put("specify_header_param2", "specify_header_value2");
            specifyHeaderMap.put("specify_header_param3", "specify_header_value3");


            Map<String, Object> txtParamMap = new HashMap<>();
            txtParamMap.put("logic_txt_param1", "logic_txt_value1");
            txtParamMap.put("logic_txt_param2", "logic_txt_value2");
            txtParamMap.put("logic_txt_param3", "logic_txt_value3");

            Map<String, CCFile> fileParamMap = new HashMap<>();

            String fileInfoStr = "";

            for (int i = 0; i < images.size(); i++){

                ImageItem imageItem = images.get(i);
                fileParamMap.put("fileKey" + i, new CCFile(imageItem.path));

                fileInfoStr = fileInfoStr + "fileKey" + i + "===>" + imageItem.path + "\n\n";
            }

            this.tv_file_source.setText(fileInfoStr);

            Map<String, String> pathMap = new HashMap<String, String>();
            pathMap.put("{path1}", "path1_value1");
            pathMap.put("{path2}", "path1_value2");
            pathMap.put("{path3}", "path1_value3");
            pathMap.put("{path4}", "path1_value4");
            pathMap.put("{path5}", "path1_value5");

            CCRxNetManager.<String>upload("upload")
                    .setHeaderMap(specifyHeaderMap)
                    .setPathMap(pathMap)
                    .setTxtParamMap(txtParamMap)
                    .setFileParamMap(fileParamMap)
                    .setRetryCount(0)
                    .setCacheQueryMode(CCCMode.QueryMode.MODE_NET)
                    .setCacheSaveMode(CCCMode.SaveMode.MODE_NONE)
                    .setReqTag("test_login_req_tag")
                    .setCacheKey("test_login_req_cache_key")
                    .setCCNetCallback(new RxNetUploadProgressCallback())
                    .setNetLifecycleComposer(this.<CCBaseResponse<String>>bindUntilEvent(ActivityEvent.DESTROY))
                    .setResponseBeanType(String.class)
                    .executeAsync();

        } catch (Exception e) {
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
     * 上传进度回调
     */
    private class RxNetUploadProgressCallback extends CCNetCallback {
        @Override
        public <T> void onStartRequest(Object reqTag, CCCanceler canceler) {
            NetLogUtil.printLog("d", LOG_TAG, "调用了RxNetUploadProgressCallback.onStartRequest方法，调用者reqTag=" + reqTag);
            tv_file_upload_status.setText("开始上传");
            tv_file_upload_progress.setText("文件标识：" + "" + "\n上传进度：" + 0 + "%\n上传速度：" + "0B/s" + "\n已上传大小：" + 0 + "B\n文件大小：" + 0 + "B");
        }

        @Override
        public <T> void onRequestSuccess(Object reqTag, T response) {
            if (response != null) {

                if (response instanceof TestRespObj) {
                    NetLogUtil.printLog("d", LOG_TAG, "调用了onSuccess方法，调用者reqTag=" + reqTag + ",响应数据是TestRespObj类型,response=" + ((TestRespObj) response).toString());
                } else {
                    NetLogUtil.printLog("d", LOG_TAG, "调用了onSuccess方法，调用者reqTag=" + reqTag + ",但响应数据不是TestRespObj类型");
                }

            } else {
                NetLogUtil.printLog("d", LOG_TAG, "调用了onSuccess方法，调用者reqTag=" + reqTag + ",但响应数据response == null");
            }
            tv_file_upload_status.setText("上传成功");
            tv_file_upload_progress.setText("文件标识：" + "" + "\n上传进度：" + 0 + "%\n上传速度：" + "0B/s" + "\n已上传大小：" + 0 + "B\n文件大小：" + 0 + "B");
        }

        @Override
        public <T> void onRequestFail(Object reqTag, Throwable t) {
            tv_file_upload_status.setText("上传失败，详细失败信息见log，log窗口类型：Error");
            tv_file_upload_progress.setText("文件标识：" + reqTag + "\n上传进度：" + 0 + "%\n上传速度：" + "0B/s" + "\n已上传大小：" + 0 + "B\n文件大小：" + 0 + "B");
            NetLogUtil.printLog("d", LOG_TAG, "调用了RxNetUploadProgressCallback.onError方法，调用者reqTag=" + reqTag, t);
        }

        @Override
        public <T> void onComplete(Object reqTag) {
            tv_file_upload_status.setText("上传完成");
            tv_file_upload_progress.setText("文件标识：" + "" + "\n上传进度：" + 0 + "%\n上传速度：" + "0B/s" + "\n已上传大小：" + 0 + "B\n文件大小：" + 0 + "B");
            NetLogUtil.printLog("d", LOG_TAG, "调用了RxNetUploadProgressCallback.onComplete方法，调用者reqTag=" + reqTag);
        }

        @Override
        public <T> void onProgress(Object tag, int progress, long netSpeed, long completedSize, long fileSize) {

            gProgress = progress;
            gCompletedSize = completedSize;
            gFileSize = fileSize;

            if (netSpeed > 1024 * 1024){
                gNetSpeed = netSpeed / (1024 * 1024) + "M/s";
            }else if (netSpeed > 1024){
                gNetSpeed = netSpeed / 1024 + "KB/s";
            }else {
                gNetSpeed = netSpeed + "B/s";
            }

            NetLogUtil.printLog("d", LOG_TAG, "调用了RxNetUploadProgressCallback.onProgress方法，调用者tag=" + tag + ",progress=" + gProgress + ",netSpeed=" + gNetSpeed + ",completedSize=" + gCompletedSize + ",fileSize=" + gFileSize);
            tv_file_upload_progress.setText("文件标识：" + tag + "\n上传进度：" + gProgress + "%\n上传速度：" + gNetSpeed + "\n已上传大小：" + gCompletedSize + "B\n文件大小：" + gFileSize + "B");
        }
    }

}
