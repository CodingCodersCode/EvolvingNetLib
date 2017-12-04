package com.codingcoderscode.evolving.net.request.callback;

/**
 * Created by CodingCodersCode on 2017/10/31.
 * <p>
 * 上传进度回调类
 */

public abstract class CCUploadProgressCallback {

    /**
     * 说明：
     *
     *
     * 调用顺序：
     *          正常：onStart()  ==>  onProgress()  ==>  onComplete()
     *          异常：onStart()  ==>  onProgress()  ==>  onError()  ==>  onComplete()
     */


    /**
     * 上传开始
     *
     * @param tag 上传请求标识，由调用者传递，若用户传值为null，系统默认传递上传文件的文件路径绝对值
     * @param <T> 标识类型，默认为Object
     */
    public <T> void onStart(T tag) {

    }

    /**
     * 上传进度更新
     *
     * @param tag          上传请求标识，由调用者传递，若用户传值为null，系统默认传递上传文件的文件路径绝对值
     * @param progress     上传进度
     * @param netSpeed     网络上传速度
     * @param uploadedSize 已上传大小
     * @param fileSize     文件大小
     * @param <T>          标识类型，默认为Object
     */
    public <T> void onProgress(T tag, int progress, long netSpeed, long uploadedSize, long fileSize) {

    }

    /**
     * 上传出错
     *
     * @param tag 上传请求标识，由调用者传递，若用户传值为null，系统默认传递上传文件的文件路径绝对值
     * @param t   异常信息
     * @param <T> 标识类型，默认为Object
     */
    public <T> void onError(T tag, Throwable t) {

    }

    /**
     * 上传完成
     *
     * @param tag 上传请求标识，由调用者传递，若用户传值为null，系统默认传递上传文件的文件路径绝对值
     * @param <T> 标识类型，默认为Object
     */
    public <T> void onComplete(T tag) {

    }

}
