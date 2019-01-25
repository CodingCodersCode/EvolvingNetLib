package com.demo.evolving.net.lib;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codingcoderscode.evolving.base.CCBaseRxAppCompactActivity;
import com.codingcoderscode.evolving.net.CCRxNetManager;
import com.codingcoderscode.evolving.net.request.CCMultiDownladRequest;
import com.codingcoderscode.evolving.net.request.callback.CCNetCallback;
import com.codingcoderscode.evolving.net.request.canceler.CCCanceler;
import com.codingcoderscode.evolving.net.util.NetLogUtil;
import com.demo.evolving.net.lib.downloadmanager.CCDownloadStatus;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by CodingCodersCode on 2017/11/14.
 */

public class DownloadListTestActivity2  extends CCBaseRxAppCompactActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private String LOG_TAG = "DownloadListTestActivity";

    private TextView tvStart;
    private TextView tvCancel;
    private TextView tvResume;
    private RecyclerView rvTask;

    private List<CCDownloadTask2> taskList = new ArrayList<>();

    private int taskKeyCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_list_test_2);

        this.tvStart = (TextView)findViewById(R.id.tv_start);
        this.tvStart.setOnClickListener(this);

        this.tvCancel = (TextView)findViewById(R.id.tv_cancel);
        this.tvCancel.setOnClickListener(this);

        this.tvResume = (TextView)findViewById(R.id.tv_resume);
        this.tvResume.setOnClickListener(this);

        this.rvTask = (RecyclerView)findViewById(R.id.rv_task);
        this.rvTask.setLayoutManager(new LinearLayoutManager(this));
        this.rvTask.setAdapter(new TaskRvAdapter());


        NetLogUtil.setDebugAble(true);


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
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.tv_start:

                onTestDownload();

                break;
            case R.id.tv_cancel:
                ccMultiDownladRequest.cancel();
                NetLogUtil.printLog("e", LOG_TAG, "点击了Cancel按钮");
                break;
            case R.id.tv_resume:
                ccMultiDownladRequest.resumeAll();
                break;
        }

    }

    private CCMultiDownladRequest ccMultiDownladRequest;

    @AfterPermissionGranted(2003)
    private void onTestDownload(){
        String[] perms = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {

            taskList.clear();

            int tmpTaskCount = taskKeyCount;

            taskKeyCount += 10;

            CCDownloadTask2 task;

            List<CCDownloadTask2> taskList2 = new ArrayList<>();

            for (; tmpTaskCount < taskKeyCount; tmpTaskCount++){

                task = new CCDownloadTask2(this, "taskKey-" + tmpTaskCount, "sw-search-sp/software/16d5a98d3e034/QQ_8.9.5.22062_setup.exe", null, "QQ_download_test_file_" + tmpTaskCount + ".apk", 1, 0, 0);

                taskList.add(task);
                taskList2.add(task);
            }



            this.rvTask.getAdapter().notifyDataSetChanged();


            if (ccMultiDownladRequest == null){
                ccMultiDownladRequest = CCRxNetManager.<Void>multiDownload("").setMaxTaskCount(2).setCCNetCallback(new RxNetDownloadCalback());
            }

            ccMultiDownladRequest.startAll(taskList);

            //CCDownloadManager.getInstance().startAll(taskList);

        }else {
            EasyPermissions.requestPermissions(this, "This operation needs access to write and read external storage.", 2003, perms);
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



    /**
     * 下载进度回调
     */
    private class RxNetDownloadCalback extends CCNetCallback {
        @Override
        public <T> void onStartRequest(Object reqTag, CCCanceler canceler) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了RxNetDownloadCalback.onStart方法，调用者reqTag=" + reqTag);
        }

        @Override
        public <T> void onRequestSuccess(Object reqTag, T response, int dataSourceMode) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了RxNetDownloadCalback.onSuccess方法，调用者reqTag=" + reqTag);
            if (reqTag instanceof CCDownloadTask2){

                CCDownloadTask2 task = (CCDownloadTask2)reqTag;

                int index = taskList.indexOf(task);
                taskList.get(index).setDownloadStatus(CCDownloadStatus.COMPLETED);
                taskList.get(index).setProgress(100);

                rvTask.getAdapter().notifyItemChanged(index);

            }
        }

        @Override
        public <T> void onRequestFail(Object reqTag, Throwable t) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了RxNetDownloadCalback.onError方法，调用者reqTag=" + reqTag, t);

            if (reqTag instanceof CCDownloadTask2){

                CCDownloadTask2 task = (CCDownloadTask2)reqTag;

                int index = taskList.indexOf(task);
                taskList.get(index).setDownloadStatus(CCDownloadStatus.PAUSED);

                rvTask.getAdapter().notifyItemChanged(index);

            }
        }

        @Override
        public <T> void onRequestComplete(Object reqTag) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了RxNetDownloadCalback.onComplete方法，调用者reqTag=" + reqTag);
        }

        @Override
        public <T> void onProgress(Object tag, int progress, long netSpeed, long completedSize, long fileSize) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了RxNetDownloadCalback.onProgress方法，调用者tag=" + tag + ",progress=" + progress + "，netSpeed=" + netSpeed + "，completedSize=" + completedSize + "，fileSize=" + fileSize);

            if (tag instanceof CCDownloadTask2){

                CCDownloadTask2 task = (CCDownloadTask2)tag;

                int index = taskList.indexOf(task);
                taskList.get(index).setProgress(progress);
                taskList.get(index).setDownloadStatus(CCDownloadStatus.DOWNLOADING);

                rvTask.getAdapter().notifyItemChanged(index);

            }

        }
    }

    public void onItemBtnCallback(CCDownloadTask2 task){

        int index = taskList.indexOf(task);


        switch (task.getDownloadStatus()){
            case CCDownloadStatus.WAIT:
                taskList.get(index).setDownloadStatus(CCDownloadStatus.PAUSED);
                ccMultiDownladRequest.pause(task);
                rvTask.getAdapter().notifyItemChanged(index);
                break;
            case CCDownloadStatus.DOWNLOADING:
                taskList.get(index).setDownloadStatus(CCDownloadStatus.PAUSED);
                ccMultiDownladRequest.pause(task);
                rvTask.getAdapter().notifyItemChanged(index);
                break;
            case CCDownloadStatus.PAUSED:
                taskList.get(index).setDownloadStatus(CCDownloadStatus.WAIT);
                ccMultiDownladRequest.resume(task);
                rvTask.getAdapter().notifyItemChanged(index);
                break;
            case CCDownloadStatus.COMPLETED:
                break;
        }
    }

    private class TaskRvAdapter extends RecyclerView.Adapter<TaskRvAdapter.MyViewHolder> {


        @Override
        public TaskRvAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TaskRvAdapter.MyViewHolder holder = new TaskRvAdapter.MyViewHolder(LayoutInflater.from(DownloadListTestActivity2.this).inflate(R.layout.item_task, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(TaskRvAdapter.MyViewHolder holder, int position) {

            final CCDownloadTask2 task = taskList.get(position);

            holder.tvTaskName.setText("任务名称:" + task.getSaveName());
            holder.tvProgress.setText("任务进度:" + task.getProgress());

            NetLogUtil.printLog("e", "****************", "任务进度:" + task.getProgress());

            switch (task.getDownloadStatus()){
                case CCDownloadStatus.WAIT:
                    holder.tvBtn.setText("等待下载");
                    break;
                case CCDownloadStatus.DOWNLOADING:
                    holder.tvBtn.setText("正在下载");
                    break;
                case CCDownloadStatus.PAUSED:
                    holder.tvBtn.setText("已暂停");
                    break;
                case CCDownloadStatus.COMPLETED:
                    holder.tvBtn.setText("下载完成");
                    break;
            }

            holder.tvBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemBtnCallback(task);
                }
            });

        }

        @Override
        public int getItemCount() {
            return taskList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tvTaskName;
            private TextView tvProgress;
            private TextView tvBtn;

            public MyViewHolder(View view) {
                super(view);
                tvTaskName = (TextView) view.findViewById(R.id.tv_task_name);
                tvProgress = (TextView) view.findViewById(R.id.tv_progress);
                tvBtn = (TextView) view.findViewById(R.id.tv_btn);
            }
        }
    }

}