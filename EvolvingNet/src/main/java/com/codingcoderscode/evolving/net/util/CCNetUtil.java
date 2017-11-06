package com.codingcoderscode.evolving.net.util;

import java.util.Map;

/**
 * Created by ghc on 2017/10/30.
 * <p>
 * 网络相关工具类
 */

public class CCNetUtil {

    public static String regexApiUrlWithPathParam(final String apiUrl, Map<?, ?> urlPathParamMap) {
        String pathKey;
        String pathValue;
        StringBuilder sBuilder;
        String resultApiUrl = apiUrl;
        try {

            sBuilder = new StringBuilder("");

            if (apiUrl != null && urlPathParamMap != null && urlPathParamMap.size() > 0) {
                for (Map.Entry<?, ?> entry : urlPathParamMap.entrySet()) {

                    pathKey = (entry.getKey() == null) ? "" : entry.getKey().toString();
                    pathValue = (entry.getValue() == null) ? "" : entry.getValue().toString();

                    resultApiUrl = resultApiUrl.replace(pathKey, pathValue);

                    sBuilder.delete(0, sBuilder.length());
                }
            }

        } catch (Exception e) {

        }

        return resultApiUrl;
    }
}
