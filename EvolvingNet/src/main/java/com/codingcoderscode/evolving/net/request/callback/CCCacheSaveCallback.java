package com.codingcoderscode.evolving.net.request.callback;

/**
 * Created by CodingCodersCode on 2017/10/27.
 */

public interface CCCacheSaveCallback {

    /**
     * 将数据保存到缓存回调
     * <p>
     * 回调线程：非UI线程
     *
     * @param cacheKey
     * @param response
     * @param <T>
     */
    <T> void onSaveToMemory(String cacheKey, T response);

    /**
     * 将数据保存到磁盘回调
     * <p>
     * 回调线程：非UI线程
     *
     * @param cacheKey
     * @param response
     * @param <T>
     */
    <T> void onSaveToDisk(String cacheKey, T response);

}
