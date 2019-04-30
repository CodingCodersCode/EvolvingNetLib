package com.codingcoderscode.evolving.net.request.callback;

import com.codingcoderscode.evolving.net.request.entity.CCDownloadTask;

/**
 * Created by CodingCodersCode on 2017/10/31.
 * <p>
 * 下载进度回调类
 */

public interface CCDownloadProgressListener {

    /**
     * 开始下载
     *
     * @param tag 文件标识
     */
    public void onStart(Object tag, CCDownloadTask downloadTask);

    /**
     * 进度保存回调
     * <p>
     * 回调线程：非UI线程，目前只用于下载进度保存回调
     *  @param reqTag        文件标识
     * @param downloadTask
     * @param progress      下载进度
     * @param netSpeed      网络下载速度
     * @param completedSize 已下载大小
     * @param fileSize      文件大小
     */
    public void onProgressSave(Object reqTag, CCDownloadTask downloadTask, int progress, long netSpeed, long completedSize, long fileSize);

    /**
     * 进度更新
     *
     * @param tag            文件标识
     * @param progress       下载进度
     * @param netSpeed       网络下载速度
     * @param downloadedSize 已下载大小
     * @param fileSize       文件大小
     */
    public void onProgress(Object tag, CCDownloadTask downloadTask, int progress, long netSpeed, long downloadedSize, long fileSize);

    public void onSuccess(Object tag, CCDownloadTask downloadTask);

    /**
     * 下载出错
     *
     * @param tag 文件标识
     * @param t   异常信息
     */
    public void onError(Object tag, CCDownloadTask downloadTask, Throwable t);

    /**
     * 下载完成
     *
     * @param tag 文件标识
     */
    public void onComplete(Object tag, CCDownloadTask downloadTask);

}
