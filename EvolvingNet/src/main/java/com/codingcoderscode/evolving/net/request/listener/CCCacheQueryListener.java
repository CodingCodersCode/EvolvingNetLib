package com.codingcoderscode.evolving.net.request.listener;

/**
 * Created by CodingCodersCode on 2017/10/27.
 */

public interface CCCacheQueryListener {

    /**
     * 从内存查询缓存回调
     * <p>
     * 回调线程：非UI线程
     *
     * @param cacheKey
     * @param <T>
     * @return
     */
    <T> T onQueryFromMemory(String cacheKey);

    /**
     * 从磁盘查询缓存回调
     * <p>
     * 回调线程：非UI线程
     *
     * @param cacheKey
     * @param <T>
     * @return
     */
    <T> T onQueryFromDisk(String cacheKey);
}
