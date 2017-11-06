package com.codingcoderscode.evolving.net.util;

/**
 * Created by ghc on 2017/10/24.
 */

public class Utils {

    public static <T> T checkNotNull(T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }

}
