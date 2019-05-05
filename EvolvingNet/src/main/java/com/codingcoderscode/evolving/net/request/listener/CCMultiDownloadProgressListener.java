package com.codingcoderscode.evolving.net.request.listener;

import com.codingcoderscode.evolving.net.request.canceler.CCCanceler;

/**
 * Date：2019/4/30 18:17
 * <p>
 * author: CodingCodersCode
 */
public interface CCMultiDownloadProgressListener extends CCSingleDownloadProgressListener {

    public void onRequestStart(Object tag, CCCanceler canceler);

    public void onRequestSuccess(Object tag);

    public void onRequestError(Object tag, Throwable t);

    public void onRequestComplete(Object tag);

}
