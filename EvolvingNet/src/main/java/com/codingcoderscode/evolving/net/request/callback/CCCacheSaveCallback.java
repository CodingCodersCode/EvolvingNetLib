package com.codingcoderscode.evolving.net.request.callback;

/**
 * Created by ghc on 2017/10/27.
 */

public interface CCCacheSaveCallback {

    <T> void onSaveToMemory(String cacheKey, T response);

    <T> void onSaveToDisk(String cacheKey, T response);

}
