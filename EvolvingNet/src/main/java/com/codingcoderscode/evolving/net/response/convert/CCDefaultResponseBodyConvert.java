package com.codingcoderscode.evolving.net.response.convert;

import com.codingcoderscode.evolving.net.request.exception.CCUnConvertableException;
import com.codingcoderscode.evolving.net.util.CCLogUtil;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;

import okhttp3.ResponseBody;

/**
 * Created by CodingCodersCode on 2017/10/27.
 */

public class CCDefaultResponseBodyConvert {

    public static <T> T convertResponse(ResponseBody responseBody, Type typeOfT) throws Exception {
        T result = null;
        try {
            if (typeOfT == null) {
                result = null;
            } else if (responseBody == null) {
                result = null;
            } else if (typeOfT == Void.class) {
                result = null;
            } else if (typeOfT == String.class) {
                //noinspection unchecked
                result = (T) responseBody.string();
            } else if (typeOfT == JSONObject.class) {
                //noinspection unchecked
                result = (T) new JSONObject(responseBody.string());
            } else if (typeOfT == JSONArray.class) {
                //noinspection unchecked
                result = (T) new JSONArray(responseBody.string());
            } else {
                Gson mGson = new Gson();
                result = mGson.fromJson(new String(responseBody.bytes()), typeOfT);
            }
        } catch (Exception e) {
            CCLogUtil.printLog("e", CCDefaultResponseBodyConvert.class.getCanonicalName(), "转换响应json发生异常", e);
            throw new CCUnConvertableException(e, getString(responseBody));
        }
        return result;
    }

    public static <T> T convertResponse(String responseBody, Type typeOfT) throws Exception {
        T result = null;
        try {
            if (typeOfT == null) {
                result = null;
            } else if (responseBody == null) {
                result = null;
            } else if (typeOfT == Void.class) {
                result = null;
            } else if (typeOfT == String.class) {
                //noinspection unchecked
                result = (T) responseBody;
            } else if (typeOfT == JSONObject.class) {
                //noinspection unchecked
                result = (T) new JSONObject(responseBody);
            } else if (typeOfT == JSONArray.class) {
                //noinspection unchecked
                result = (T) new JSONArray(responseBody);
            } else {
                Gson mGson = new Gson();
                result = mGson.fromJson(responseBody, typeOfT);
            }
        } catch (Exception e) {
            throw new CCUnConvertableException(e, responseBody);
        }
        return result;
    }

    private static String getString(Object object) {
        String result;
        try {
            if (object == null) {
                result = null;
            } else {
                result = object.toString();
            }
        } catch (Exception e) {
            result = null;
        }
        return result;
    }
}
