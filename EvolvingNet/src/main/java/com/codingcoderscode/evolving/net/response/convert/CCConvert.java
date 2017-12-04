package com.codingcoderscode.evolving.net.response.convert;

import java.lang.reflect.Type;

import okhttp3.ResponseBody;

/**
 * Created by CodingCodersCode on 2017/11/4.
 */

public abstract class CCConvert {

    public abstract <T> T convert(ResponseBody responseBody, Type type);

}
