package com.codingcoderscode.evolving.net.request.callback;

import com.codingcoderscode.evolving.net.request.canceler.CCCanceler;

/**
 * Created by ghc on 2017/11/5.
 */

public abstract class CCNetCallback {

    /**
     * you can call the method canceler.cancel() to cancel the request when you need.
     *
     * @param canceler
     */
    public <T> void onStartRequest(Object reqTag, CCCanceler canceler) {
    }

    public <T> void onMemoryCacheQuerySuccess(Object reqTag, T response) {
    }

    public <T> void onDiskCacheQuerySuccess(Object reqTag, T response) {
    }

    public <T> void onCacheQuerySuccess(Object reqTag, T response) {
    }

    public <T> void onNetSuccess(Object reqTag, T response) {
    }

    public <T> void onSuccess(Object reqTag, T response) {
    }

    public <T> void onError(Object reqTag, Throwable t) {
    }

    public <T> void onComplete(Object reqTag) {
    }

    /**
     * 进度更新，在UI线程回调
     *
     * @param reqTag        文件标识
     * @param progress      下载进度 or 上传进度
     * @param netSpeed      网络下载速度 or 网络上传速度
     * @param completedSize 已下载大小 or 已上传大小
     * @param fileSize      文件大小
     * @param <T>
     */
    public <T> void onProgress(Object reqTag, int progress, long netSpeed, long completedSize, long fileSize) {
    }

    /**
     * 进度保存回调，在非UI线程回调，目前只用于下载进度保存回调
     *
     * @param reqTag        文件标识
     * @param progress      下载进度
     * @param netSpeed      网络下载速度
     * @param completedSize 已下载大小
     * @param fileSize      文件大小
     * @param <T>
     */
    public <T> void onProgressSave(Object reqTag, int progress, long netSpeed, long completedSize, long fileSize) {
    }


}
