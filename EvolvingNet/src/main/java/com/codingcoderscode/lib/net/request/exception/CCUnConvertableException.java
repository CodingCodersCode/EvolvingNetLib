package com.codingcoderscode.lib.net.request.exception;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * Date：2018/10/30 16:33
 * <p>
 * author: CodingCodersCode
 */
public class CCUnConvertableException extends Exception {
    private String originRawResponse;

    public CCUnConvertableException(String originRawResponse) {
        super();
        this.originRawResponse = originRawResponse;
    }

    public CCUnConvertableException(String message, String originRawResponse) {
        super(message);
        this.originRawResponse = originRawResponse;
    }

    public CCUnConvertableException(String message, Throwable cause, String originRawResponse) {
        super(message, cause);
        this.originRawResponse = originRawResponse;
    }

    public CCUnConvertableException(Throwable cause, String originRawResponse) {
        super(cause);
        this.originRawResponse = originRawResponse;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public CCUnConvertableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String originRawResponse) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.originRawResponse = originRawResponse;
    }
}
