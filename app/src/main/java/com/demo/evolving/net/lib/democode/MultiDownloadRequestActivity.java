package com.demo.evolving.net.lib.democode;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codingcoderscode.evolving.base.CCBaseRxAppCompactActivity;
import com.codingcoderscode.lib.net.request.CCMultiDownloadRequest;
import com.codingcoderscode.lib.net.request.canceler.CCCanceler;
import com.codingcoderscode.lib.net.request.entity.CCDownloadTask;
import com.codingcoderscode.lib.net.request.listener.CCMultiDownloadProgressListener;
import com.codingcoderscode.lib.net.util.CCLogUtil;
import com.demo.evolving.net.lib.CCApplication;
import com.demo.evolving.net.lib.CCDownloadTask2;
import com.demo.evolving.net.lib.R;
import com.demo.evolving.net.lib.downloadmanager.CCDownloadStatus;

import java.util.ArrayList;
import java.util.List;

import cn.nekocode.rxlifecycle.LifecycleEvent;
import cn.nekocode.rxlifecycle.RxLifecycle;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by CodingCodersCode on 2017/12/1.
 */

public class MultiDownloadRequestActivity extends CCBaseRxAppCompactActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private final String LOG_TAG = getClass().getCanonicalName();

    private TextView tv_start_all;
    private TextView tv_pause_all;
    private TextView tv_resume_all;
    private RecyclerView rv_task;

    private CCMultiDownloadRequest ccMultiDownladRequest;

    private List<CCDownloadTask2> taskList = new ArrayList<>();

    private int taskKeyCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_download_request);

        initView();
    }

    private void initView() {

        this.tv_start_all = (TextView) findViewById(R.id.tv_start_all);
        this.tv_start_all.setOnClickListener(this);

        this.tv_pause_all = (TextView) findViewById(R.id.tv_pause_all);
        this.tv_pause_all.setOnClickListener(this);

        this.tv_resume_all = (TextView) findViewById(R.id.tv_resume_all);
        this.tv_resume_all.setOnClickListener(this);

        this.rv_task = (RecyclerView) findViewById(R.id.rv_task);
        this.rv_task.setLayoutManager(new LinearLayoutManager(this));
        this.rv_task.setAdapter(new TaskRvAdapter());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_start_all:
                onCreateAndStartAllTask();
                break;
            case R.id.tv_pause_all:
                onPauseAllTask();
                break;
            case R.id.tv_resume_all:
                onResumeAllTask();
                break;
        }
    }

    private void onPauseAllTask() {
        ccMultiDownladRequest.cancel();
        int size = taskList.size();
        for (int i = 0; i < size; i++) {
            CCDownloadTask2 task2 = taskList.get(i);
            task2.setDownloadStatus(CCDownloadStatus.PAUSED);
        }
        rv_task.getAdapter().notifyDataSetChanged();
    }

    private void onResumeAllTask() {
        int size = taskList.size();
        for (int i = 0; i < size; i++) {
            CCDownloadTask2 task2 = taskList.get(i);
            task2.setDownloadStatus(CCDownloadStatus.WAIT);
        }
        rv_task.getAdapter().notifyDataSetChanged();
        ccMultiDownladRequest.resumeAll();
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


    @AfterPermissionGranted(2003)
    private void onCreateAndStartAllTask() {
        String[] perms = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {

            taskList.clear();

            int tmpTaskCount = taskKeyCount;

            taskKeyCount += 10;

            CCDownloadTask2 task;

            List<CCDownloadTask> taskList2 = new ArrayList<>();

            //http://24810.xc.wenpie.com/xiaz/dpkentool.dll%E6%96%87%E4%BB%B6@560_402379.exe

            String downloadUrl = "/16891/371C7C353C7B87011FB3DE8B12BCBCA5.apk?fsname=com.tencent.mm_7.0.0_1380.apk&csr=1bbd";

            for (; tmpTaskCount < taskKeyCount; tmpTaskCount++) {

                task = new CCDownloadTask2(this, "taskKey-" + tmpTaskCount, downloadUrl, null, "QQ_download_test_file_" + tmpTaskCount + ".apk", 1, 0, 0);

                taskList.add(task);
                taskList2.add(task);
            }


            this.rv_task.getAdapter().notifyDataSetChanged();


            if (ccMultiDownladRequest == null) {
                ccMultiDownladRequest = (CCMultiDownloadRequest) ((CCApplication) this.getApplicationContext()).getCcRxNetManager().<Void>multiDownload("")
                        .setDownloadProgressListener(new CCMultiDownloadProgressListener() {
                            @Override
                            public void onRequestStart(Object tag, CCCanceler canceler) {

                            }

                            @Override
                            public void onRequestSuccess(Object tag) {

                            }

                            @Override
                            public void onRequestError(Object tag, Throwable t) {

                            }

                            @Override
                            public void onRequestComplete(Object tag) {

                            }

                            @Override
                            public void onStart(Object tag, CCDownloadTask downloadTask, CCCanceler canceler) {

                            }

                            @Override
                            public void onProgressSave(Object reqTag, CCDownloadTask downloadTask, int progress, long netSpeed, long completedSize, long fileSize) {

                            }

                            @Override
                            public void onProgress(Object tag, CCDownloadTask downloadTask, int progress, long netSpeed, long downloadedSize, long fileSize) {
                                CCLogUtil.printLog("d", LOG_TAG, "调用了RxNetDownloadCalback.onProgress方法，调用者downloadTask=" + downloadTask + ",progress=" + progress + "，netSpeed=" + netSpeed + "，downloadedSize=" + downloadedSize + "，fileSize=" + fileSize);

                                /*if (downloadTask instanceof CCDownloadTask2) {

                                    CCDownloadTask2 task = (CCDownloadTask2) downloadTask;

                                    int index = taskList.indexOf(task);
                                    taskList.get(index).setProgress(progress);
                                    taskList.get(index).setDownloadStatus(CCDownloadStatus.DOWNLOADING);

                                    rv_task.getAdapter().notifyItemChanged(index);

                                }*/
                                int index = taskList.indexOf(downloadTask);

                                try {
                                    taskList.get(index).setProgress(progress);
                                    taskList.get(index).setDownloadStatus(CCDownloadStatus.DOWNLOADING);

                                    rv_task.getAdapter().notifyItemChanged(index);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onSuccess(Object tag, CCDownloadTask downloadTask) {


                                //CCDownloadTask2 task = (CCDownloadTask2) downloadTask;

                                CCLogUtil.printLog("d", LOG_TAG, "调用了CCMultiDownloadProgressListener.onSuccess方法，调用者downloadTask=" + downloadTask);

                                int index = taskList.indexOf(downloadTask);
                                taskList.get(index).setDownloadStatus(CCDownloadStatus.COMPLETED);
                                taskList.get(index).setProgress(100);

                                rv_task.getAdapter().notifyItemChanged(index);
                            }

                            @Override
                            public void onError(Object tag, CCDownloadTask downloadTask, Throwable t) {


                                //CCDownloadTask2 task = (CCDownloadTask2) downloadTask;

                                CCLogUtil.printLog("d", LOG_TAG, "调用了CCMultiDownloadProgressListener.onError方法，调用者downloadTask=" + downloadTask, t);

                                int index = taskList.indexOf(downloadTask);
                                taskList.get(index).setDownloadStatus(CCDownloadStatus.PAUSED);

                                rv_task.getAdapter().notifyItemChanged(index);
                            }

                            @Override
                            public void onComplete(Object tag, CCDownloadTask downloadTask) {
                                CCLogUtil.printLog("d", LOG_TAG, "调用了CCMultiDownloadProgressListener.onComplete方法，调用者downloadTask=" + downloadTask);
                            }
                        })
                        .setMaxTaskCount(2)
                        .setReqTag("Tag_Outer_MultiDownload");
            }

            ccMultiDownladRequest = (CCMultiDownloadRequest) ccMultiDownladRequest.setLifecycleDisposeComposer(RxLifecycle.bind(this).<Void>cancelFlowableWhen(LifecycleEvent.DESTROY_VIEW));

            ccMultiDownladRequest.startAll(taskList);

            //CCDownloadManager.getInstance().startAll(taskList);

        } else {
            EasyPermissions.requestPermissions(this, "This operation needs access to write and read external storage.", 2003, perms);
        }
    }

    public void onItemBtnCallback(CCDownloadTask2 task) {

        int index = taskList.indexOf(task);


        switch (task.getDownloadStatus()) {
            case CCDownloadStatus.WAIT:
                taskList.get(index).setDownloadStatus(CCDownloadStatus.PAUSED);
                ccMultiDownladRequest.pause(task);
                rv_task.getAdapter().notifyItemChanged(index);
                break;
            case CCDownloadStatus.DOWNLOADING:
                taskList.get(index).setDownloadStatus(CCDownloadStatus.PAUSED);
                ccMultiDownladRequest.pause(task);
                rv_task.getAdapter().notifyItemChanged(index);
                break;
            case CCDownloadStatus.PAUSED:
                taskList.get(index).setDownloadStatus(CCDownloadStatus.WAIT);
                ccMultiDownladRequest.resume(task);
                rv_task.getAdapter().notifyItemChanged(index);
                break;
            case CCDownloadStatus.COMPLETED:
                break;
        }
    }

    private class TaskRvAdapter extends RecyclerView.Adapter<TaskRvAdapter.MyViewHolder> {


        @Override
        public TaskRvAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TaskRvAdapter.MyViewHolder holder = new TaskRvAdapter.MyViewHolder(LayoutInflater.from(MultiDownloadRequestActivity.this).inflate(R.layout.item_task, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(TaskRvAdapter.MyViewHolder holder, int position) {

            final CCDownloadTask2 task = taskList.get(position);

            holder.tvTaskName.setText("任务名称:" + task.getSaveName());
            holder.tvProgress.setText("任务进度:" + task.getProgress());

            switch (task.getDownloadStatus()) {
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
