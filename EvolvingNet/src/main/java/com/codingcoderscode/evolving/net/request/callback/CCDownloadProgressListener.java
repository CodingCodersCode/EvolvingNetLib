package com.codingcoderscode.evolving.net.request.callback;

/**
 * Created by CodingCodersCode on 2017/10/31.
 * <p>
 * 下载进度回调类
 */

public interface CCDownloadProgressListener {

    /**
     * 开始下载
     *
     * @param tag      文件标识
     * @param <T>
     */
    public <T> void onStart(T tag);

    /**
     * 进度更新
     *
     * @param tag            文件标识
     * @param progress       下载进度
     * @param netSpeed       网络下载速度
     * @param downloadedSize 已下载大小
     * @param fileSize       文件大小
     * @param <T>
     */
    public <T> void onProgress(T tag, int progress, long netSpeed, long downloadedSize, long fileSize);

    public <T> void onSuccess(T tag);

    /**
     * 下载出错
     *
     * @param tag 文件标识
     * @param t   异常信息
     * @param <T>
     */
    public <T> void onError(T tag, Throwable t);

    /**
     * 下载完成
     *
     * @param tag 文件标识
     * @param <T>
     */
    public <T> void onComplete(T tag);

}
