package com.codingcoderscode.lib.net.cache.mode;

/**
 * Created by CodingCodersCode on 2017/10/27.
 */

public class CCMode {

    public static class QueryMode {
        //只从网络获取
        public static final int MODE_NET = 0;
        //只从磁盘获取
        public static final int MODE_DISK = 1;
        //先从磁盘获取并返回，然后从网络获取并返回
        public static final int MODE_DISK_AND_NET = 2;
    }

    @Deprecated
    public static class SaveMode {
        //默认缓存策略——OkHttp负责缓存
        public static final int MODE_DEFAULT = 0;
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
