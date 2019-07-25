package com.codingcoderscode.lib.net.request.entity;

import com.codingcoderscode.lib.net.response.CCBaseResponse;

import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.FlowableTransformer;

/**
 * Created by CodingCodersCode on 2017/11/13.
 */

public class CCDownloadTask {

    private FlowableTransformer<CCBaseResponse<Void>, CCBaseResponse<Void>> netLifecycleComposer;

    private static AtomicInteger downloadTaskStampAtomicInteger;

    private int taskStamp;

    @Deprecated
    private String renamedSaveName;

    private String sourceUrl;
    private String savePath;
    private String saveName;
    private int priority;

    private long fileSize;

    static {
        downloadTaskStampAtomicInteger = new AtomicInteger(0);
    }

    public CCDownloadTask(String sourceUrl, String savePath, String saveName, int priority, FlowableTransformer<CCBaseResponse<Void>, CCBaseResponse<Void>> netLifecycleComposer, long fileSize) {
        this.netLifecycleComposer = netLifecycleComposer;
        this.sourceUrl = sourceUrl;
        this.savePath = savePath;
        this.saveName = saveName;
        this.priority = priority;
        initTaskStamp();
    }

    private void initTaskStamp(){
        this.taskStamp = downloadTaskStampAtomicInteger.getAndIncrement();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CCDownloadTask)) return false;

        CCDownloadTask that = (CCDownloadTask) o;

        if (getSourceUrl() != null ? !getSourceUrl().equals(that.getSourceUrl()) : that.getSourceUrl() != null)
            return false;
        if (getSavePath() != null ? !getSavePath().equals(that.getSavePath()) : that.getSavePath() != null)
            return false;
        return getSaveName() != null ? getSaveName().equals(that.getSaveName()) : that.getSaveName() == null;
    }

    @Override
    public int hashCode() {
        int result = getSourceUrl() != null ? getSourceUrl().hashCode() : 0;
        result = 31 * result + (getSavePath() != null ? getSavePath().hashCode() : 0);
        result = 31 * result + (getSaveName() != null ? getSaveName().hashCode() : 0);
        return result;
    }

    /*@Override
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
    }*/



    public FlowableTransformer<CCBaseResponse<Void>, CCBaseResponse<Void>> getNetLifecycleComposer() {
        return netLifecycleComposer;
    }

    public void setNetLifecycleComposer(FlowableTransformer<CCBaseResponse<Void>, CCBaseResponse<Void>> netLifecycleComposer) {
        this.netLifecycleComposer = netLifecycleComposer;
    }

    public static AtomicInteger getDownloadTaskStampAtomicInteger() {
        return downloadTaskStampAtomicInteger;
    }

    public static void setDownloadTaskStampAtomicInteger(AtomicInteger downloadTaskStampAtomicInteger) {
        CCDownloadTask.downloadTaskStampAtomicInteger = downloadTaskStampAtomicInteger;
    }

    public int getTaskStamp() {
        return taskStamp;
    }

    public void setTaskStamp(int taskStamp) {
        this.taskStamp = taskStamp;
    }

    public String getRenamedSaveName() {
        return renamedSaveName;
    }

    public void setRenamedSaveName(String renamedSaveName) {
        this.renamedSaveName = renamedSaveName;
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

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
