package com.codingcoderscode.evolving.net.request.api;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.OPTIONS;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by CodingCodersCode on 2017/10/16.
 * <p>
 * 公共泛型Retrofit请求接口
 */

public interface CCNetApiService {

    /**
     * 发起GET类型请求
     *
     * @param url       接口地址
     * @param headerMap header信息map
     * @param paramMap  参数Map集合
     * @return
     */
    @GET
    Call<ResponseBody> executeGet(
            @Url String url,
            @HeaderMap Map<String, String> headerMap,
            @QueryMap Map<String, Object> paramMap);

    /*@GET
    Flowable<ResponseBody> executeGet(
            @Url String url,
            @HeaderMap Map<String, String> headerMap,
            @QueryMap Map<String, String> paramMap);*/

    /**
     * 发起POST类型请求
     *
     * @param url       接口地址
     * @param headerMap header信息map
     * @param paramMap  参数Map集合
     * @return
     */
    @FormUrlEncoded
    @POST
    Call<ResponseBody> executePost(
            @Url String url,
            @HeaderMap Map<String, String> headerMap,
            @FieldMap Map<String, Object> paramMap);

    /*@FormUrlEncoded
    @POST
    Flowable<ResponseBody> executePost(
            @Url String url,
            @HeaderMap Map<String, String> headerMap,
            @FieldMap Map<String, String> paramMap);*/

    /**
     * 发起PUT类型请求
     *
     * @param url       接口地址
     * @param headerMap header信息map
     * @param paramMap  参数Map集合
     * @return
     */
    @FormUrlEncoded
    @PUT
    Call<ResponseBody> executePut(
            @Url String url,
            @HeaderMap Map<String, String> headerMap,
            @FieldMap Map<String, Object> paramMap);

    /*@FormUrlEncoded
    @PUT
    Flowable<ResponseBody> executePut(
            @Url String url,
            @HeaderMap Map<String, String> headerMap,
            @FieldMap Map<String, String> paramMap);*/

    /**
     * 发起DELETE类型请求
     *
     * @param url       接口地址
     * @param headerMap header信息map
     * @param paramMap  参数Map集合
     * @return
     */
    @DELETE
    Call<ResponseBody> executeDelete(
            @Url String url,
            @HeaderMap Map<String, String> headerMap,
            @QueryMap Map<String, Object> paramMap);

    /*@DELETE
    Flowable<ResponseBody> executeDelete(
            @Url String url,
            @HeaderMap Map<String, String> headerMap,
            @QueryMap Map<String, String> paramMap);*/

    /**
     * 发起HEAD类型请求
     *
     * @param url
     * @param headerMap
     * @param paramMap
     * @return
     */
    @HEAD
    Call<Void> executeHead(
            @Url String url,
            @HeaderMap Map<String, String> headerMap,
            @QueryMap Map<String, Object> paramMap);

    /**
     * 发起OPTIONS类型请求
     *
     * @param url
     * @param headerMap
     * @param paramMap
     * @return
     */
    @OPTIONS
    Call<ResponseBody> executeOptions(
            @Url String url,
            @HeaderMap Map<String, String> headerMap,
            @QueryMap Map<String, Object> paramMap);

    /*@OPTIONS
    Flowable<ResponseBody> executeOptions(
            @Url String url,
            @HeaderMap Map<String, String> headerMap,
            @QueryMap Map<String, String> paramMap);*/

    /**
     * 发起PATCH类型请求
     *
     * @param url
     * @param headerMap
     * @param paramMap
     * @return
     */
    @PATCH
    Call<Void> executePatch(
            @Url String url,
            @HeaderMap Map<String, String> headerMap,
            @QueryMap Map<String, Object> paramMap);


    /**
     * 上传文件
     * @param url
     * @param headerMap
     * @param paramPartList
     * @return
     */
    @Multipart
    @POST
    Call<ResponseBody> executeUpload(
            @Url String url,
            @HeaderMap Map<String, String> headerMap,
            @Part() List<MultipartBody.Part> paramPartList
    );

    /**
     * 下载文件
     * @param url
     * @param headerMap
     * @param txtParamMap
     * @return
     */
    @Streaming
    @GET
    Call<ResponseBody> executeDownload(
            @Url String url,
            @HeaderMap Map<String, String> headerMap,
            @QueryMap Map<String, Object> txtParamMap
    );

}
