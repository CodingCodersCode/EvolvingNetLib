package com.codingcoderscode.evolving.net;


import com.codingcoderscode.evolving.net.request.CCDeleteRequest;
import com.codingcoderscode.evolving.net.request.CCDownloadRequest;
import com.codingcoderscode.evolving.net.request.CCGetRequest;
import com.codingcoderscode.evolving.net.request.CCHeadRequest;
import com.codingcoderscode.evolving.net.request.CCOptionsRequest;
import com.codingcoderscode.evolving.net.request.CCPostRequest;
import com.codingcoderscode.evolving.net.request.CCPutRequest;
import com.codingcoderscode.evolving.net.request.CCUploadRequest;
import com.codingcoderscode.evolving.net.request.api.CCNetApiService;
import com.codingcoderscode.evolving.net.request.interceptor.CCHeaderInterceptor;
import com.codingcoderscode.evolving.net.request.interceptor.CCHttpLoggingInterceptor;
import com.codingcoderscode.evolving.net.util.NetLogUtil;
import com.codingcoderscode.evolving.net.util.Utils;
import com.google.gson.Gson;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by ghc on 2017/10/26.
 */

public class CCRxNetManager {

    private static final String LOG_TAG = CCRxNetManager.class.getCanonicalName();

    private static CCNetApiService ccNetApiService = null;
    private static Gson gsonParser;
    private static Retrofit retrofitInstance;
    private static Converter.Factory converterFactory;

    private CCRxNetManager(Retrofit retrofit, Converter.Factory factory) {
        retrofitInstance = retrofit;
        converterFactory = factory;
    }


    /**
     * 获取公共API Service实例
     *
     * @return
     */
    public static CCNetApiService getCcNetApiService() {
        if (ccNetApiService == null) {
            ccNetApiService = CCRxNetManager.retrofitInstance.create(CCNetApiService.class);
        }

        return ccNetApiService;
    }

    public static final class Builder {

        private Retrofit.Builder retrofitBuilder;
        private OkHttpClient.Builder okHttpClientBuilder;
        private Converter.Factory converterFactory;

        public Builder() {
            this.retrofitBuilder = new Retrofit.Builder();
            this.okHttpClientBuilder = new OkHttpClient.Builder();
            CCRxNetManager.ccNetApiService = null;
            CCRxNetManager.gsonParser = new Gson();
        }

        public Builder baseUrl(String baseUrl) {
            this.retrofitBuilder.baseUrl(Utils.checkNotNull(baseUrl, "baseUrl == null"));
            return this;
        }

        public Builder converterFactory(Converter.Factory factory) {
            this.converterFactory = factory;
            this.retrofitBuilder.addConverterFactory(factory);
            return this;
        }

        public Builder callAdapterFactory(CallAdapter.Factory factory) {
            this.retrofitBuilder.addCallAdapterFactory(factory);
            return this;
        }

        public Builder connectTimeout(long timeout, TimeUnit unit) {
            this.okHttpClientBuilder.connectTimeout(timeout, unit);
            return this;
        }

        public Builder readTimeout(long timeout, TimeUnit unit) {
            this.okHttpClientBuilder.readTimeout(timeout, unit);
            return this;
        }

        public Builder writeTimeout(long timeout, TimeUnit unit) {
            this.okHttpClientBuilder.readTimeout(timeout, unit);
            return this;
        }

        public <T> Builder commonHeaders(Map<String, T> headerMap) {
            //OkHttp添加header拦截器
            this.okHttpClientBuilder.addInterceptor(new CCHeaderInterceptor<T>(headerMap));
            return this;
        }

        public Builder enableLogInterceptor(boolean enableLog) {
            if (enableLog) {
                NetLogUtil.setDebugAble(true);
                CCHttpLoggingInterceptor loggingInterceptor = new CCHttpLoggingInterceptor("CCNetLib");
                loggingInterceptor.setPrintLevel(CCHttpLoggingInterceptor.Level.BODY);        //log打印级别，决定了log显示的详细程度
                loggingInterceptor.setColorLevel(Level.INFO);                               //log颜色级别，决定了log在控制台显示的颜色

                this.okHttpClientBuilder.addInterceptor(loggingInterceptor);
            }
            return this;
        }

        public CCRxNetManager build() {

            OkHttpClient okHttpClient = this.okHttpClientBuilder.build();

            Retrofit retrofit = this.retrofitBuilder.client(okHttpClient).build();

            return new CCRxNetManager(retrofit, converterFactory);
        }
    }

    /** get请求 */
    public static <T> CCGetRequest<T> get(String url) {
        return new CCGetRequest<T>(url);
    }

    /** post请求 */
    public static <T> CCPostRequest<T> post(String url) {
        return new CCPostRequest<T>(url);
    }

    /** head请求 */
    public static <T> CCHeadRequest<T> head(String url) {
        return new CCHeadRequest<T>(url);
    }

    /** put请求 */
    public static <T> CCPutRequest<T> put(String url) {
        return new CCPutRequest<T>(url);
    }

    /** delete请求 */
    public static <T> CCDeleteRequest<T> delete(String url) {
        return new CCDeleteRequest<T>(url);
    }

    /** options请求 */
    public static <T> CCOptionsRequest<T> options(String url) {
        return new CCOptionsRequest<T>(url);
    }

    /** 上传文件请求 */
    public static <T> CCUploadRequest<T> upload(String url){
        return new CCUploadRequest<T>(url);
    }

    /** 下载文件请求 */
    public static <T> CCDownloadRequest<T> download(String url){
        return new CCDownloadRequest<T>(url);
    }

}
