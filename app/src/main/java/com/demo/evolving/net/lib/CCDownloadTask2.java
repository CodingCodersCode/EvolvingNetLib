package com.demo.evolving.net.lib;

import com.codingcoderscode.evolving.net.request.entity.CCDownloadTask;
import com.codingcoderscode.evolving.net.response.CCBaseResponse;
import com.demo.evolving.net.lib.downloadmanager.CCDownloadStatus;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.trello.rxlifecycle2.components.support.RxFragment;

import io.reactivex.FlowableTransformer;

/**
 * Created by CodingCodersCode on 2017/11/14.
 */

public class CCDownloadTask2 extends CCDownloadTask {

    private String taskKey;
    private int downloadStatus;
    private long downloadedSize;
    private int progress;

    public CCDownloadTask2(String taskKey, String sourceUrl, String savePath, String saveName, int priority, long fileSize, long downloadedSize) {
        super(sourceUrl, savePath, saveName, priority, null, fileSize);
        this.taskKey = taskKey;
        this.downloadStatus = CCDownloadStatus.WAIT;
        this.downloadedSize = downloadedSize;
    }

    public CCDownloadTask2(RxAppCompatActivity activity, String taskKey, String sourceUrl, String savePath, String saveName, int priority, long fileSize, long downloadedSize) {
        super(sourceUrl, savePath, saveName, priority, null, fileSize);
        this.taskKey = taskKey;
        this.downloadStatus = CCDownloadStatus.WAIT;
        this.downloadedSize = downloadedSize;
    }

    public CCDownloadTask2(RxFragment fragment, String taskKey, String sourceUrl, String savePath, String saveName, int priority, long fileSize, long downloadedSize) {
        super(sourceUrl, savePath, saveName, priority, null, fileSize);
        this.taskKey = taskKey;
        this.downloadStatus = CCDownloadStatus.WAIT;
        this.downloadedSize = downloadedSize;
    }

    public CCDownloadTask2(String sourceUrl, String savePath, String saveName, int priority, FlowableTransformer<CCBaseResponse<Void>, CCBaseResponse<Void>> netLifecycleComposer, long fileSize) {
        super(sourceUrl, savePath, saveName, priority, netLifecycleComposer, fileSize);
    }

    @Override
    public String toString() {
        return "CCDownloadTask{" +
                "taskKey='" + taskKey + '\'' +
                ", downloadStatus=" + downloadStatus +
                '}';
    }

    public String getTaskKey() {
        return taskKey;
    }

    public void setTaskKey(String taskKey) {
        this.taskKey = taskKey;
    }

    public int getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public long getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(long downloadedSize) {
        this.downloadedSize = downloadedSize;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
