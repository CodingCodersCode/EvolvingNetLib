package com.codingcoderscode.evolving.net.util;

import android.text.TextUtils;

import java.util.Map;

import okhttp3.Headers;

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

    /**
     * 判断是否支持Range
     * @param headers
     * @return
     */
    public static boolean isHttpSupportRange(Headers headers){
        boolean support = false;
        try{

            if (TextUtils.equals("bytes", getHeader("Accept-Ranges", headers))){
                support = true;
            }else {
                String value = getHeader("Content-Range", headers);
                support = (value != null && value.startsWith("bytes"));
            }
        }catch (Exception e){

        }
        return support;
    }

    public static String getHeader(String headerKey, Headers headers){
        if (headers == null || headerKey == null){
            return null;
        }else {
            return headers.get(headerKey);
        }
    }
}
