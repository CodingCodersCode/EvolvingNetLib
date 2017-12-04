package com.demo.evolving.net.lib;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by CodingCodersCode on 2017/10/26.
 */

public class ResponseConvertImpl /*implements IResponseConvert*/ {
    /*@Override*/
    public <T> T convertResponse(Response response, Type typeOfT) {
        if (response == null || response.body() == null) {
            return null;
        }

        T result = null;

        ResponseBody body = response.body();

        try {

            if (typeOfT == null) {
                result = null;
            } else if (body == null) {
                result = null;
            } else if (typeOfT == String.class) {
                //noinspection unchecked
                result = (T) body;
            } else if (typeOfT == JSONObject.class) {
                //noinspection unchecked
                result = (T) new JSONObject(body.string());
            } else if (typeOfT == JSONArray.class) {
                //noinspection unchecked
                result = (T) new JSONArray(body.string());
            } else {
                Gson mGson = new Gson();
                result = mGson.fromJson(body.string(), typeOfT);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
