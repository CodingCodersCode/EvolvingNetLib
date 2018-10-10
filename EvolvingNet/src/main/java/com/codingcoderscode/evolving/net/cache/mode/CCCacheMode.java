package com.codingcoderscode.evolving.net.cache.mode;

/**
 * Created by CodingCodersCode on 2017/10/27.
 */

public class CCCacheMode {

    public static class QueryMode {
        //只从网络获取
        public static final int MODE_ONLY_NET = 0;
        //只能内存获取
        public static final int MODE_ONLY_MEMORY = 1;
        //只从磁盘获取
        public static final int MODE_ONLY_DISK = 2;
        //从内存和磁盘获取并返回
        public static final int MODE_MEMORY_AND_DISK = 3;
        //先从磁盘获取并返回，然后从网络获取并返回
        public static final int MODE_DISK_THEN_NET = 4;
        //先从内存获取并返回，然后从网络获取并返回
        public static final int MODE_MEMORY_THEN_NET = 5;
        //从内存、磁盘和网络获取并返回
        public static final int MODE_MEMORY_AND_DISK_AND_NET = 6;

    }

    public static class SaveMode {
        //内存缓存数据
        public static final int MODE_SAVE_MEMORY = 0;
        //磁盘缓存数据
        public static final int MODE_SAVE_DISK = 1;
        //内存和磁盘缓存数据
        public static final int MODE_SAVE_MEMORY_AND_DISK = 2;
        //不缓存数据
        public static final int MODE_NO_CACHE = 3;

    }

}
