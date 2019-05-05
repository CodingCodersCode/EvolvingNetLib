package com.codingcoderscode.evolving.net.request.exception;

/**
 * Created by CodingCodersCode on 2017/11/1.
 */

public class CCNoResponseBodyDataException extends Exception {

    public CCNoResponseBodyDataException() {
    }

    public CCNoResponseBodyDataException(String message) {
        super(message);
    }

    public CCNoResponseBodyDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public CCNoResponseBodyDataException(Throwable cause) {
        super(cause);
    }

}
