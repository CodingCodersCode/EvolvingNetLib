package com.codingcoderscode.lib.net.response;


import okhttp3.Headers;

/**
 * Created by CodingCodersCode on 2017/10/27.
 * <p>
 * 响应数据包装类
 */

public class CCBaseResponse<T> {

    private T realResponse;
    private Headers headers;
    private boolean fromCache;

    private boolean intervalCallback = false;

    private boolean successful = true;
    private Throwable throwable;

    public CCBaseResponse(T realResponse, boolean fromCache) {
        this.realResponse = realResponse;
        this.headers = null;
        this.fromCache = fromCache;
    }

    public CCBaseResponse(T realResponse, boolean fromCache, boolean successful, Throwable t) {
        this.realResponse = realResponse;
        this.headers = null;
        this.fromCache = fromCache;
        this.successful = successful;
        this.throwable = t;
    }

    public CCBaseResponse(T realResponse, Headers headers, boolean fromCache) {
        this.realResponse = realResponse;
        this.headers = headers;
        this.fromCache = fromCache;
    }

    public CCBaseResponse(T realResponse, Headers headers, boolean fromCache, boolean intervalCallback) {
        this.realResponse = realResponse;
        this.headers = headers;
        this.fromCache = fromCache;
        this.intervalCallback = intervalCallback;
    }

    public CCBaseResponse(T realResponse, Headers headers, boolean fromCache, boolean intervalCallback, boolean successful, Throwable t) {
        this.realResponse = realResponse;
        this.headers = headers;
        this.fromCache = fromCache;
        this.intervalCallback = intervalCallback;
        this.successful = successful;
        this.throwable = t;
    }

    public T getRealResponse() {
        return realResponse;
    }

    public void setRealResponse(T realResponse) {
        this.realResponse = realResponse;
    }

    public boolean isFromCache() {
        return fromCache;
    }

    public void setFromCache(boolean fromCache) {
        this.fromCache = fromCache;
    }

    public Headers getHeaders() {
        return headers;
    }

    public void setHeaders(Headers headers) {
        this.headers = headers;
    }

    public boolean isIntervalCallback() {
        return intervalCallback;
    }

    public void setIntervalCallback(boolean intervalCallback) {
        this.intervalCallback = intervalCallback;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable t) {
        this.throwable = t;
    }
}
