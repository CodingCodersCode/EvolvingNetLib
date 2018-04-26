package com.codingcoderscode.evolving.net;


import com.codingcoderscode.evolving.net.request.CCDeleteRequest;
import com.codingcoderscode.evolving.net.request.CCDownloadRequest;
import com.codingcoderscode.evolving.net.request.CCGetRequest;
import com.codingcoderscode.evolving.net.request.CCHeadRequest;
import com.codingcoderscode.evolving.net.request.CCMultiDownladRequest;
import com.codingcoderscode.evolving.net.request.CCOptionsRequest;
import com.codingcoderscode.evolving.net.request.CCPostRequest;
import com.codingcoderscode.evolving.net.request.CCPutRequest;
import com.codingcoderscode.evolving.net.request.CCUploadRequest;
import com.codingcoderscode.evolving.net.request.api.CCNetApiService;
import com.codingcoderscode.evolving.net.request.interceptor.CCHeaderInterceptor;
import com.codingcoderscode.evolving.net.request.interceptor.CCHttpLoggingInterceptor;
import com.codingcoderscode.evolving.net.request.ssl.HttpsUtil;
import com.codingcoderscode.evolving.net.util.NetLogUtil;
import com.codingcoderscode.evolving.net.util.Utils;
import com.google.gson.Gson;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by CodingCodersCode on 2017/10/26.
 * <p>
 * 一切网络请求及相关配置的发源地
 */

public class CCRxNetManager {

    private static final String LOG_TAG = CCRxNetManager.class.getCanonicalName();

    private static CCNetApiService ccNetApiService = null;
    private static Gson gsonParser;
    private static Retrofit retrofitInstance;
    private static Converter.Factory converterFactory;

    /**
     * 初始化
     *
     * @param retrofit
     * @param factory
     */
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

        /**
         * Set the API base URL.
         *
         * @param baseUrl host路径
         * @return
         */
        public Builder baseUrl(String baseUrl) {
            this.retrofitBuilder.baseUrl(Utils.checkNotNull(baseUrl, "baseUrl == null"));
            return this;
        }

        /**
         * Add converter factory for serialization and deserialization of objects.
         *
         * @param factory
         * @return
         */
        public Builder converterFactory(Converter.Factory factory) {
            this.converterFactory = factory;
            this.retrofitBuilder.addConverterFactory(factory);
            return this;
        }

        /**
         * Add a call adapter factory for supporting service method return types
         *
         * @param factory
         * @return
         */
        public Builder callAdapterFactory(CallAdapter.Factory factory) {
            this.retrofitBuilder.addCallAdapterFactory(factory);
            return this;
        }

        /**
         * Sets the default connect timeout for new connections. A value of 0 means no timeout,
         * otherwise values must be between 1 and {@link Integer#MAX_VALUE} when converted to
         * milliseconds.
         *
         * @param timeout
         * @param unit
         * @return
         */
        public Builder connectTimeout(long timeout, TimeUnit unit) {
            this.okHttpClientBuilder.connectTimeout(timeout, unit);
            return this;
        }

        /**
         * Sets the default read timeout for new connections. A value of 0 means no timeout, otherwise
         * values must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
         *
         * @param timeout
         * @param unit
         * @return
         */
        public Builder readTimeout(long timeout, TimeUnit unit) {
            this.okHttpClientBuilder.readTimeout(timeout, unit);
            return this;
        }

        /**
         * Sets the default read timeout for new connections. A value of 0 means no timeout, otherwise
         * values must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
         *
         * @param timeout
         * @param unit
         * @return
         */
        public Builder writeTimeout(long timeout, TimeUnit unit) {
            this.okHttpClientBuilder.readTimeout(timeout, unit);
            return this;
        }

        /**
         * Add headers for all request
         *
         * @param headerMap
         * @param <T>
         * @return
         */
        public <T> Builder commonHeaders(Map<String, T> headerMap) {
            //OkHttp添加header拦截器
            this.okHttpClientBuilder.addInterceptor(new CCHeaderInterceptor<T>(headerMap));
            return this;
        }

        /**
         * Add log interceptor to print http request log
         *
         * @param enableLog
         * @return
         */
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

        /**
         * Add interceptor
         *
         * @param interceptor
         * @return
         */
        public Builder interceptor(Interceptor interceptor) {
            this.okHttpClientBuilder.addInterceptor(interceptor);
            return this;
        }

        /**
         * Add SSlSocketFactory for https request
         *
         * @param sslParams
         * @return
         */
        public Builder sslSocketFactory(HttpsUtil.SSLParams sslParams) {
            this.okHttpClientBuilder.sslSocketFactory(sslParams.getSslSocketFactory(), sslParams.getX509TrustManager());
            return this;
        }

        /**
         * Add SSlSocketFactory for https request
         *
         * @param sslSocketFactory
         * @return
         */
        public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
            this.okHttpClientBuilder.sslSocketFactory(sslSocketFactory);
            return this;
        }

        /**
         * Add SSlSocketFactory for https request
         * @param sslSocketFactory
         * @param trustManager
         * @return
         */
        public Builder sslSocketFactory(
                SSLSocketFactory sslSocketFactory, X509TrustManager trustManager){
            this.okHttpClientBuilder.sslSocketFactory(sslSocketFactory, trustManager);
            return this;
        }

        public Builder hostnameVerifier(HostnameVerifier hostnameVerifier){
            this.okHttpClientBuilder.hostnameVerifier(hostnameVerifier);
            return this;
        }

        public CCRxNetManager build() {

            OkHttpClient okHttpClient = this.okHttpClientBuilder.build();

            Retrofit retrofit = this.retrofitBuilder.client(okHttpClient).build();

            return new CCRxNetManager(retrofit, converterFactory);
        }
    }

    /**
     * 创建GET类型请求
     *
     * @param url 请求url
     * @param <T> 响应结果的Java实体类类型
     * @return
     */
    public static <T> CCGetRequest<T> get(String url) {
        return new CCGetRequest<T>(url);
    }

    /**
     * 创建POST类型请求
     *
     * @param url 请求url
     * @param <T> 响应结果的Java实体类类型
     * @return
     */
    public static <T> CCPostRequest<T> post(String url) {
        return new CCPostRequest<T>(url);
    }

    /**
     * 创建HEAD类型请求
     *
     * @param url 请求url
     * @param <T> 响应结果的Java实体类类型
     * @return
     */
    public static <T> CCHeadRequest<T> head(String url) {
        return new CCHeadRequest<T>(url);
    }

    /**
     * 创建PUT类型请求
     *
     * @param url 请求url
     * @param <T> 响应结果的Java实体类类型
     * @return
     */
    public static <T> CCPutRequest<T> put(String url) {
        return new CCPutRequest<T>(url);
    }

    /**
     * 创建DELETE类型请求
     *
     * @param url 请求url
     * @param <T> 响应结果的Java实体类类型
     * @return
     */
    public static <T> CCDeleteRequest<T> delete(String url) {
        return new CCDeleteRequest<T>(url);
    }

    /**
     * 创建OPTIONS类型请求
     *
     * @param url 请求url
     * @param <T> 响应结果的Java实体类类型
     * @return
     */
    public static <T> CCOptionsRequest<T> options(String url) {
        return new CCOptionsRequest<T>(url);
    }

    /**
     * 创建上传文件请求，类型为POST
     *
     * @param url 请求url
     * @param <T> 响应结果的JAva实体类类型
     * @return
     */
    public static <T> CCUploadRequest<T> upload(String url) {
        return new CCUploadRequest<T>(url);
    }

    /**
     * 创建单文件下载请求，类型为GET
     *
     * @param url 下载文件url
     * @param <T> 传值Void
     * @return
     */
    public static <T> CCDownloadRequest<T> download(String url) {
        return new CCDownloadRequest<T>(url);
    }

    /**
     * 创建多文件下载请求，类型为GET
     *
     * @param url 忽略
     * @param <T> 传值Void
     * @return
     */
    public static <T> CCMultiDownladRequest<T> multiDownload(String url) {
        return new CCMultiDownladRequest<T>(url);
    }

}
