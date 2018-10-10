package com.codingcoderscode.evolving.net.response;


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

    private boolean fromMemoryCache;
    private boolean fromDiskCache;

    private boolean netInBadCondition = false;

    public CCBaseResponse(T realResponse, boolean fromCache, boolean fromMemoryCache, boolean fromDiskCache) {
        this.realResponse = realResponse;
        this.headers = null;
        this.fromCache = fromCache;
        this.fromMemoryCache = fromMemoryCache;
        this.fromDiskCache = fromDiskCache;
    }

    public CCBaseResponse(T realResponse, Headers headers, boolean fromCache, boolean fromMemoryCache, boolean fromDiskCache) {
        this.realResponse = realResponse;
        this.headers = headers;
        this.fromCache = fromCache;
        this.fromMemoryCache = fromMemoryCache;
        this.fromDiskCache = fromDiskCache;
    }

    public CCBaseResponse(T realResponse, Headers headers, boolean fromCache, boolean fromMemoryCache, boolean fromDiskCache, boolean netInBadCondition) {
        this.realResponse = realResponse;
        this.headers = headers;
        this.fromCache = fromCache;
        this.fromMemoryCache = fromMemoryCache;
        this.fromDiskCache = fromDiskCache;
        this.netInBadCondition = netInBadCondition;
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

    public boolean isFromMemoryCache() {
        return fromMemoryCache;
    }

    public void setFromMemoryCache(boolean fromMemoryCache) {
        this.fromMemoryCache = fromMemoryCache;
        this.fromCache = this.fromMemoryCache | this.fromDiskCache;
    }

    public boolean isFromDiskCache() {
        return fromDiskCache;
    }

    public void setFromDiskCache(boolean fromDiskCache) {
        this.fromDiskCache = fromDiskCache;
        this.fromCache = this.fromMemoryCache | this.fromDiskCache;
    }

    public Headers getHeaders() {
        return headers;
    }

    public void setHeaders(Headers headers) {
        this.headers = headers;
    }

    public boolean isNetInBadCondition() {
        return netInBadCondition;
    }

    public void setNetInBadCondition(boolean netInBadCondition) {
        this.netInBadCondition = netInBadCondition;
    }
}
