package com.demo.evolving.net.lib.downloadmanager;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.trello.rxlifecycle2.components.support.RxFragment;

/**
 * Created by ghc on 2017/11/7.
 */

public class CCDownloadTask extends CCBaseDownloadTask{


    private String sourceUrl;
    private String savePath;
    private String saveName;
    private int priority;


    private String taskKey;
    private int downloadStatus;
    private long fileSize;
    private long downloadedSize;
    private int progress;

    public CCDownloadTask(String taskKey, String sourceUrl, String savePath, String saveName, int priority, long fileSize, long downloadedSize) {
        super();
        this.taskKey = taskKey;
        this.sourceUrl = sourceUrl;
        this.savePath = savePath;
        this.saveName = saveName;
        this.priority = priority;
        this.downloadStatus = CCDownloadStatus.WAIT;
        this.fileSize = fileSize;
        this.downloadedSize = downloadedSize;
    }

    public CCDownloadTask(RxAppCompatActivity activity, String taskKey, String sourceUrl, String savePath, String saveName, int priority, long fileSize, long downloadedSize) {
        super(activity);
        this.taskKey = taskKey;
        this.sourceUrl = sourceUrl;
        this.savePath = savePath;
        this.saveName = saveName;
        this.priority = priority;
        this.downloadStatus = CCDownloadStatus.WAIT;
        this.fileSize = fileSize;
        this.downloadedSize = downloadedSize;
    }

    public CCDownloadTask(RxFragment fragment, String taskKey, String sourceUrl, String savePath, String saveName, int priority, long fileSize, long downloadedSize) {
        super(fragment);
        this.taskKey = taskKey;
        this.sourceUrl = sourceUrl;
        this.savePath = savePath;
        this.saveName = saveName;
        this.priority = priority;
        this.downloadStatus = CCDownloadStatus.WAIT;
        this.fileSize = fileSize;
        this.downloadedSize = downloadedSize;
    }


    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }else {

            if (obj instanceof CCDownloadTask){

                CCDownloadTask objTask = (CCDownloadTask)obj;

                String thisSourceUrl = TextUtils.isEmpty(this.sourceUrl)? "" : this.sourceUrl.trim();
                String thisSavePath = TextUtils.isEmpty(this.savePath)? "" : this.savePath.trim();
                String thisSaveName = TextUtils.isEmpty(this.saveName)? "" : this.saveName.trim();

                String objSourceUrl = TextUtils.isEmpty(objTask.sourceUrl)? "" : objTask.sourceUrl.trim();
                String objSavePath = TextUtils.isEmpty(objTask.savePath)? "" : objTask.savePath.trim();
                String objSaveName = TextUtils.isEmpty(objTask.saveName)? "" : objTask.saveName.trim();

                if (thisSourceUrl.equals(objSourceUrl) && thisSavePath.equals(objSavePath) && thisSaveName.equals(objSaveName)){
                    return true;
                }else {
                    return false;
                }
            }else {
                return false;
            }
        }
    }

    @Override
    public String toString() {
        return "CCDownloadTask{" +
                "taskKey='" + taskKey + '\'' +
                ", downloadStatus=" + downloadStatus +
                '}';
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getSaveName() {
        return saveName;
    }

    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
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