package com.codingcoderscode.evolving.net.request.exception;

/**
 * Created by ghc on 2017/11/9.
 * <p>
 * 磁盘空间不足异常，用于下载时使用
 */

public class NoEnoughSpaceException extends Exception {

    public NoEnoughSpaceException() {
    }

    public NoEnoughSpaceException(String message) {
        super(message);
    }

    public NoEnoughSpaceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoEnoughSpaceException(Throwable cause) {
        super(cause);
    }

}
