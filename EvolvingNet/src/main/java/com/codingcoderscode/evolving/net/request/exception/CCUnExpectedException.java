package com.codingcoderscode.evolving.net.request.exception;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * Date：2018/10/30 15:59
 * <p>
 * author: ghc
 */
public class CCUnExpectedException extends Exception {

    public CCUnExpectedException() {
        super();
    }

    public CCUnExpectedException(String message) {
        super(message);
    }

    public CCUnExpectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CCUnExpectedException(Throwable cause) {
        super(cause);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public CCUnExpectedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
