package com.codingcoderscode.lib.net.request.listener;

import com.codingcoderscode.lib.net.request.entity.CCDownloadTask;

import okhttp3.Headers;
import okhttp3.ResponseBody;

/**
 * Created by CodingCodersCode on 2017/11/2.
 * <p>
 * 文件下载回调，用以回调用户自己实现的文件写入保存逻辑
 */

public interface CCDownloadFileWriteListener {

    void onWriteToDisk(Object reqTag, CCDownloadTask downloadTask, Headers headers, ResponseBody responseBody, CCSingleDownloadProgressListener progressListener);
}
