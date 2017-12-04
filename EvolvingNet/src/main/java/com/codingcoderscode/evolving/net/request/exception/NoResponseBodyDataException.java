package com.codingcoderscode.evolving.net.request.exception;

/**
 * Created by CodingCodersCode on 2017/11/1.
 */

public class NoResponseBodyDataException extends Exception {

    public NoResponseBodyDataException() {
    }

    public NoResponseBodyDataException(String message) {
        super(message);
    }

    public NoResponseBodyDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoResponseBodyDataException(Throwable cause) {
        super(cause);
    }

}
