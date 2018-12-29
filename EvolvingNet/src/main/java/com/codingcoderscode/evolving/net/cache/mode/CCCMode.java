package com.codingcoderscode.evolving.net.cache.mode;

/**
 * Created by CodingCodersCode on 2017/10/27.
 */

public class CCCMode {

    public static class QueryMode {
        //只从网络获取
        public static final int MODE_NET = 0;
        //只从磁盘获取
        public static final int MODE_DISK = 1;
        //先从磁盘获取并返回，然后从网络获取并返回
        public static final int MODE_DISK_AND_NET = 2;

    }

    public static class SaveMode {
        //磁盘缓存数据
        public static final int MODE_DISK = 0;
        //不缓存数据
        public static final int MODE_NONE = 1;
    }

    public static class DataMode {
        //数据响应来源类型——磁盘
        public static final int MODE_DISK = 0;
        //数据响应来源类型——网络
        public static final int MODE_NET = 1;
    }

}
