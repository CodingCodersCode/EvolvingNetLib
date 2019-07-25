package com.codingcoderscode.lib.net.request.exception;

/**
 * Created by CodingCodersCode on 2017/11/9.
 * <p>
 * 磁盘空间不足异常，用于下载时使用
 */

public class CCNoEnoughSpaceException extends Exception {

    public CCNoEnoughSpaceException() {
    }

    public CCNoEnoughSpaceException(String message) {
        super(message);
    }

    public CCNoEnoughSpaceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CCNoEnoughSpaceException(Throwable cause) {
        super(cause);
    }

}
