package com.codingcoderscode.evolving.net.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by CodingCodersCode on 2017/10/24.
 */

public class CCUtils {

    public static <T> T checkNotNull(T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }

    /**
     * 过滤掉Map中value为空的键值对，将value为null的值置为空串("")，解决Retrofit2.x不允许传null值的问题。
     * 详情见以下链接：
     * 1：http://cache.baiducontent.com/c?m=9f65cb4a8c8507ed19fa950d100b96204a05d93e788090412189d65f93130a1c187ba0fc7063565f8e993d7a00a5485becfa36742a4377f1db95dc119bac925e2f9f27422340d05612a54cee961a32c156c809adfc0ee7ccae61cffbc5d3a90f0d94045029dea3cb0f474b9d33b6437bb2f1db0e4e024caded4631a40e297f882336e914bbf7321856dcaa9b0f179f7d867611e1f372ee6006f04ee449146413e60cec5c0a6027e03e7fac006e13939b4ae75d6e4153e81befecd6b79b5f8cabfd30e8a195f178c033b8d2eb8922022515a572e2edaab34d330f538e88984d996eabfb9cad4d9a03d76108e10a28797cce1acbd1cd40f2130ba9bb2ead727e717e1ddfc228b82b2e7831d97a4bef5897&p=c0769a47998b52ed08e2977407478e&newp=83759a46dcc917e017a4c7710f5c97231610db2151d4d3166b82c825d7331b001c3bbfb423251704d6c0786301aa425de0f13d74350923a3dda5c91d9fb4c57479de687b39&user=baidu&fm=sc&query=retrofit+java%2Elang%2EIllegalArgumentException%3A+Field+map+contained+null+value+for+key+%27appType%27%2E&qid=ec8277a00002792e&p1=1
     * 2.https://github.com/square/retrofit/commit/10ff3d97faadcd6f716447d3376f9f51992e44dd
     *
     * @param originMap
     * @return
     */
    public static Map<String, Object> requireNonNullValues(Map<String, Object> originMap) {
        Iterator<Map.Entry<String, Object>> iterator;
        Map.Entry<String, Object> entry;
        String entryKey;
        Object entryValue;
        try {
            if (originMap == null) {
                return new HashMap<String, Object>();
            } else {
                iterator = originMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    entry = iterator.next();

                    entryKey = entry.getKey();
                    entryValue = entry.getValue();
                    if (entryKey == null || entryKey.trim().equals("")) {
                        iterator.remove();
                    } else {
                        if (entryValue == null) {
                            entry.setValue("");
                        }
                    }
                }

                return originMap;
            }
        } catch (Exception e) {
            return new HashMap<String, Object>();
        }
    }

    public String requireNonNull(String origin){
        if (origin == null){
            return "";
        }else {
            return origin;
        }
    }

}
