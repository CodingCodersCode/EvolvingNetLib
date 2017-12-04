package com.codingcoderscode.evolving.net.request.interceptor;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by CodingCodersCode on 2017/10/16.
 * <p>
 * Retrofit Header信息添加
 */

public class CCHeaderInterceptor<T> implements Interceptor {

    private Map<String, T> headers;

    public CCHeaderInterceptor(Map<String, T> headers) {
        this.headers = headers;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        if (headers != null && headers.size() > 0) {
            Set<String> keys = headers.keySet();
            for (String headerKey : keys) {
                builder.addHeader(headerKey, headers.get(headerKey) == null ? "" : headers.get(headerKey).toString()).build();
            }
        }
        return chain.proceed(builder.build());
    }
}
