package com.codingcoderscode.evolving.net.request.callback;

import okhttp3.Headers;
import okhttp3.ResponseBody;

/**
 * Created by ghc on 2017/11/2.
 * <p>
 * 文件下载回调，用以回调用户自己实现的文件写入保存逻辑
 */

public abstract class CCDownloadFileWritterCallback {

    public abstract void onWriteToDisk(ResponseBody responseBody, Headers headers, CCNetCallback progressCallback);
}
