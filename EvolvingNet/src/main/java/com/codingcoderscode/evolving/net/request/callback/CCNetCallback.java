package com.codingcoderscode.evolving.net.request.callback;

import com.codingcoderscode.evolving.net.request.canceler.CCCanceler;

/**
 * Created by CodingCodersCode on 2017/11/5.
 * <p>
 * 网络请求回调
 */

public abstract class CCNetCallback {

    /**
     * 可以调用canceler的cancel()方法取消请求，但建议通过具体Request对象的cancel()方法取消
     * <p>
     * 回调线程：UI线程
     *
     * @param reqTag
     * @param canceler
     * @param <T>
     */
    public <T> void onStartRequest(Object reqTag, CCCanceler canceler) {
    }

    /**
     * 磁盘缓存查询成功回调
     * <p>
     * 回调线程：UI线程
     *
     * @param reqTag
     * @param response
     * @param <T>
     */
    public <T> void onDiskCacheQuerySuccess(Object reqTag, T response) {
    }

    /**
     * 磁盘缓存查询成功回调
     * <p>
     * 回调线程：UI线程
     *
     * @param reqTag
     * @param t
     * @param <T>
     */
    public <T> void onDiskCacheQueryFail(Object reqTag, Throwable t) {
    }

    /**
     * 网络请求成功回调
     * <p>
     * 回调线程：UI线程
     *
     * @param reqTag
     * @param response
     * @param <T>
     */
    public <T> void onNetSuccess(Object reqTag, T response) {
    }

    /**
     * 网络请求成功回调
     * <p>
     * 回调线程：UI线程
     *
     * @param reqTag
     * @param t
     * @param <T>
     */
    public <T> void onNetFail(Object reqTag, Throwable t) {
    }

    /**
     * 请求成功回调
     * <p>
     * 回调线程：UI线程
     *
     * @param <T>
     * @param reqTag
     * @param response
     */
    public <T> void onRequestSuccess(Object reqTag, T response) {
    }

    /**
     * 请求失败回调
     * <p>
     * 回调线程：UI线程
     *
     * @param <T>
     * @param reqTag
     * @param t
     */
    public <T> void onRequestFail(Object reqTag, Throwable t) {
    }

    /**
     * 请求完成回调
     * <p>
     * 回调线程：UI线程
     *
     * @param reqTag
     * @param <T>
     */
    public <T> void onComplete(Object reqTag) {
    }

    /**
     * 进度更新回调
     * <p>
     * 回调线程：UI线程
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
     * 进度保存回调
     * <p>
     * 回调线程：非UI线程，目前只用于下载进度保存回调
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

    /**
     * 周期回调
     */
    public void onIntervalCallback() {

    }
}
