package com.codingcoderscode.evolving.net.cache.exception;

/**
 * Created by CodingCodersCode on 2017/10/27.
 */

public class CCDiskCacheQueryException extends Throwable {
    public CCDiskCacheQueryException() {
    }

    public CCDiskCacheQueryException(String message) {
        super(message);
    }

    public CCDiskCacheQueryException(String message, Throwable cause) {
        super(message, cause);
    }

    public CCDiskCacheQueryException(Throwable cause) {
        super(cause);
    }

}
