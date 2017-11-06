package com.codingcoderscode.evolving.net.response.convert;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;

import okhttp3.ResponseBody;

/**
 * Created by ghc on 2017/10/27.
 */

public class CCDefaultResponseBodyConvert {

    public static <T> T convertResponse(ResponseBody responseBody, Type typeOfT) {

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
            e.printStackTrace();
        }
        return result;
    }

    public static <T> T convertResponse(String responseBody, Type typeOfT) {

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
            e.printStackTrace();
        }
        return result;
    }

}
