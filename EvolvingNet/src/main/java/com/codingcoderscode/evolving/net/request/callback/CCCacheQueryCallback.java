package com.codingcoderscode.evolving.net.request.callback;

/**
 * Created by ghc on 2017/10/27.
 */

public interface CCCacheQueryCallback {

    <T> T onQueryFromMemory(String cacheKey);

    <T> T onQueryFromDisk(String cacheKey);
}
