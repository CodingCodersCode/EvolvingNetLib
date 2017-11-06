package com.codingcoderscode.evolving.net.request.base;


import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.codingcoderscode.evolving.net.cache.exception.CCDiskCacheQueryException;
import com.codingcoderscode.evolving.net.cache.mode.CCCacheMode;
import com.codingcoderscode.evolving.net.request.callback.CCCacheQueryCallback;
import com.codingcoderscode.evolving.net.request.callback.CCCacheSaveCallback;
import com.codingcoderscode.evolving.net.request.callback.CCNetCallback;
import com.codingcoderscode.evolving.net.request.canceler.CCCanceler;
import com.codingcoderscode.evolving.net.response.CCBaseResponse;
//import com.codingcoderscode.evolving.net.response.callback.CCResponseCallback;
import com.codingcoderscode.evolving.net.response.convert.CCConvert;
import com.codingcoderscode.evolving.net.response.convert.CCDefaultResponseBodyConvert;
import com.codingcoderscode.evolving.net.util.NetLogUtil;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * Created by ghc on 2017/10/26.
 * <p>
 * 请求基类
 */

public abstract class CCRequest<T, R extends CCRequest> {

    private final String LOG_TAG = CCRequest.class.getCanonicalName();

    protected Map<String, String> headerMap;
    protected Map<String, String> paramMap;
    private Map<String, String> pathMap;

    protected int retryCount;

    private int retryDelayTimeMillis;

    private int cacheMode;

    private Object reqTag;

    private String cacheKey;

    private long cacheValidTimeLen;

    private String baseUrl;

    private String baseUrl_backup;

    protected String apiUrl;

    private CCNetCallback ccNetCallback;

    private CCCacheSaveCallback ccCacheSaveCallback;

    private CCCacheQueryCallback ccCacheQueryCallback;

    private FlowableTransformer<CCBaseResponse<T>, CCBaseResponse<T>> netLifecycleComposer;

    /**
     * {@link Type}
     */
    protected Type responseBeanType;

    private int cacheQueryMode;
    private int cacheSaveMode;

    private CCCanceler netCCCanceler;
    private Subscription netCancelSubscription;

    private CCConvert ccConvert;

    private boolean requestRunning;

    private boolean forceCanceled;

    protected abstract Flowable<CCBaseResponse<T>> getRequestFlowable();

    protected abstract int getHttpMethod();

    /**
     * 获取内存缓存请求Flowable对象
     *
     * @return 内存缓存查询Flowable对象
     */
    private Flowable<CCBaseResponse<T>> getMemoryCacheQueryFlowable() {
        //内存缓存数据获取
        return Flowable.create(new FlowableOnSubscribe<CCBaseResponse<T>>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<CCBaseResponse<T>> e) throws Exception {

                T response = null;

                try {

                    if (ccCacheQueryCallback != null) {
                        response = ccCacheQueryCallback.<T>onQueryFromMemory(cacheKey);
                    }

                    CCBaseResponse<T> tccBaseResponse = new CCBaseResponse<T>(response, true, true, false);

                    e.onNext(tccBaseResponse);
                    e.onComplete();

                } catch (Exception exception) {

                    switch (cacheQueryMode) {
                        case CCCacheMode.QueryMode.MODE_ONLY_MEMORY:
                            e.onError(new CCDiskCacheQueryException(exception));
                            break;
                        default:
                            e.onComplete();
                            break;
                    }

                }
            }
        }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io());

    }

    /**
     * 获取磁盘缓存请求Flowable对象
     *
     * @return 磁盘缓存查询Flowable对象
     */
    private Flowable<CCBaseResponse<T>> getDiskCacheQueryFlowable() {
        //磁盘缓存获取，包括任何形式的磁盘缓存
        return Flowable.create(new FlowableOnSubscribe<CCBaseResponse<T>>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<CCBaseResponse<T>> e) throws Exception {
                T response = null;

                try {

                    if (ccCacheQueryCallback != null) {
                        response = ccCacheQueryCallback.<T>onQueryFromDisk(cacheKey);
                    }

                    CCBaseResponse<T> tccBaseResponse = new CCBaseResponse<T>(response, true, false, true);

                    e.onNext(tccBaseResponse);
                    e.onComplete();

                } catch (Exception exception) {

                    switch (cacheQueryMode) {
                        case CCCacheMode.QueryMode.MODE_ONLY_DISK:
                        case CCCacheMode.QueryMode.MODE_MEMORY_THEN_DISK:
                            e.onError(new CCDiskCacheQueryException(exception));
                            break;
                        default:
                            e.onComplete();
                            break;
                    }

                }
            }
        }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io());

    }

    /**
     * 获取网络请求Flowable对象
     *
     * @return 网络请求Flowable对象
     */
    private Flowable<CCBaseResponse<T>> getNetworkQueryFlowable() {

        //网络数据获取
        return getRequestFlowable();

    }

    public synchronized void executeAsync() {

        if (isRequestRunning()){
            return;
        }

        setRequestRunning(true);
        setForceCanceled(false);

        Flowable<CCBaseResponse<T>> resultFlowable;

        if (TextUtils.isEmpty(apiUrl)) {
            apiUrl = "";
        }

        if (headerMap == null) {
            headerMap = new HashMap<>();
        }

        if (paramMap == null) {
            paramMap = new HashMap<>();
        }

        switch (getCacheQueryMode()) {

            case CCCacheMode.QueryMode.MODE_ONLY_MEMORY:
                resultFlowable = getMemoryCacheQueryFlowable();
                break;
            case CCCacheMode.QueryMode.MODE_ONLY_DISK:
                resultFlowable = getDiskCacheQueryFlowable();
                break;
            case CCCacheMode.QueryMode.MODE_ONLY_NET:
                resultFlowable = getNetworkQueryFlowable();
                break;
            case CCCacheMode.QueryMode.MODE_MEMORY_THEN_DISK:
                resultFlowable = Flowable.concat(getMemoryCacheQueryFlowable(), getDiskCacheQueryFlowable());
                break;
            case CCCacheMode.QueryMode.MODE_DISK_THEN_NET:
                resultFlowable = Flowable.concat(getDiskCacheQueryFlowable(), getNetworkQueryFlowable());
                break;
            case CCCacheMode.QueryMode.MODE_MEMORY_THEN_NET:
                resultFlowable = Flowable.concat(getMemoryCacheQueryFlowable(), getNetworkQueryFlowable());
                break;
            case CCCacheMode.QueryMode.MODE_MEMORY_THEN_DISK_THEN_NET:
                resultFlowable = Flowable.concat(getMemoryCacheQueryFlowable(), getDiskCacheQueryFlowable(), getNetworkQueryFlowable());
                break;
            default:
                resultFlowable = getNetworkQueryFlowable();
                break;

        }

        resultFlowable = resultFlowable
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(new Function<CCBaseResponse<T>, Publisher<CCBaseResponse<T>>>() {
                    @Override
                    public Publisher<CCBaseResponse<T>> apply(@NonNull CCBaseResponse<T> tccBaseResponse) throws Exception {

                        onSaveToCache(tccBaseResponse);

                        return Flowable.just(tccBaseResponse);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());

        if (netLifecycleComposer != null) {
            resultFlowable = resultFlowable.compose(netLifecycleComposer);
        }
        resultFlowable.subscribe(new Subscriber<CCBaseResponse<T>>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
                if (ccNetCallback != null) {

                    netCancelSubscription = s;

                    netCCCanceler = new CCCanceler(CCRequest.this);

                    ccNetCallback.<T>onStartRequest(reqTag, netCCCanceler);
                }
            }

            @Override
            public void onNext(CCBaseResponse<T> tccBaseResponse) {

                onDealWithResponse(tccBaseResponse);

            }

            @Override
            public void onError(Throwable t) {
                if (ccNetCallback != null) {
                    ccNetCallback.onError(reqTag, t);
                }
            }

            @Override
            public void onComplete() {
                if (ccNetCallback != null) {
                    ccNetCallback.onComplete(reqTag);
                }
            }
        });
    }

    /**
     * 进行数据缓存处理
     *
     * @param tccBaseResponse 响应结果包装对象
     */
    protected void onSaveToCache(CCBaseResponse<T> tccBaseResponse) {
        try {

            T realResponse = (tccBaseResponse == null) ? null : tccBaseResponse.getRealResponse();

            if (tccBaseResponse != null) {

                switch (getCacheSaveMode()) {
                    case CCCacheMode.SaveMode.MODE_SAVE_MEMORY:
                        if (ccCacheSaveCallback != null && !tccBaseResponse.isFromMemoryCache()) {
                            ccCacheSaveCallback.onSaveToMemory(cacheKey, realResponse);
                        }
                        break;
                    case CCCacheMode.SaveMode.MODE_SAVE_DISK:
                        if (ccCacheSaveCallback != null && !tccBaseResponse.isFromDiskCache()) {
                            ccCacheSaveCallback.onSaveToDisk(cacheKey, realResponse);
                        }
                        break;
                    case CCCacheMode.SaveMode.MODE_SAVE_MEMORY_AND_DISK:
                        if (ccCacheSaveCallback != null) {

                            if (!tccBaseResponse.isFromMemoryCache()) {
                                ccCacheSaveCallback.onSaveToMemory(cacheKey, realResponse);
                            }

                            if (!tccBaseResponse.isFromDiskCache()) {
                                ccCacheSaveCallback.onSaveToDisk(cacheKey, realResponse);
                            }
                        }
                        break;
                    case CCCacheMode.SaveMode.MODE_NO_CACHE:
                    default:
                        break;
                }
            }

        } catch (Exception exception) {

            NetLogUtil.printLog("e", LOG_TAG, "缓存数据失败", exception);

        }
    }

    /**
     * 处理响应数据并回调
     *
     * @param tccBaseResponse 响应结果包装对象
     */
    protected void onDealWithResponse(CCBaseResponse<T> tccBaseResponse) {
        try {
            if (ccNetCallback != null) {

                T realResponse = (tccBaseResponse == null) ? null : tccBaseResponse.getRealResponse();

                if (tccBaseResponse != null) {

                    if (tccBaseResponse.isFromCache()) {
                        if (tccBaseResponse.isFromMemoryCache()) {

                            ccNetCallback.<T>onMemoryCacheQuerySuccess(reqTag, realResponse);

                        } else if (tccBaseResponse.isFromDiskCache()) {

                            ccNetCallback.<T>onDiskCacheQuerySuccess(reqTag, realResponse);
                        }

                        ccNetCallback.<T>onCacheQuerySuccess(reqTag, realResponse);

                    } else {
                        ccNetCallback.<T>onNetSuccess(reqTag, realResponse);
                    }

                }

                ccNetCallback.<T>onSuccess(reqTag, realResponse);
            }

        } catch (Exception e) {
            if (ccNetCallback != null) {
                ccNetCallback.onError(reqTag, e);
            }
        }
    }

    protected T convertResponse(ResponseBody responseBody){
        T response;
        try {
            if (getCcConvert() != null){
                response = getCcConvert().<T>convert(responseBody, responseBeanType);
            }else {
                response = CCDefaultResponseBodyConvert.<T>convertResponse(responseBody, responseBeanType);
            }
        }catch (Exception e){
            response = null;
        }
        return response;
    }

    public void cancel(){
        try {

            setRequestRunning(false);
            setForceCanceled(true);

            if (netCancelSubscription != null){
                netCancelSubscription.cancel();
                netCancelSubscription = null;
            }

        }catch (Exception e){

        }
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    @SuppressWarnings("unchecked")
    public R setHeaderMap(Map<String, String> headerMap) {
        this.headerMap = headerMap;
        return (R) this;
    }

    public Map<String, String> getParamMap() {
        return paramMap;
    }

    @SuppressWarnings("unchecked")
    public R setParamMap(Map<String, String> paramMap) {
        this.paramMap = paramMap;
        return (R) this;
    }

    public Map<String, String> getPathMap() {
        return pathMap;
    }

    @SuppressWarnings("unchecked")
    public R setPathMap(Map<String, String> pathMap) {
        this.pathMap = pathMap;
        return (R) this;
    }

    public int getRetryCount() {
        return retryCount;
    }

    @SuppressWarnings("unchecked")
    public R setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return (R) this;
    }

    public int getRetryDelayTimeMillis() {
        return retryDelayTimeMillis;
    }

    @SuppressWarnings("unchecked")
    public R setRetryDelayTimeMillis(int retryDelayTimeMillis) {
        this.retryDelayTimeMillis = retryDelayTimeMillis;
        return (R) this;
    }

    public int getCacheMode() {
        return cacheMode;
    }

    @SuppressWarnings("unchecked")
    public R setCacheMode(int cacheMode) {
        this.cacheMode = cacheMode;
        return (R) this;
    }

    public Object getReqTag() {
        return reqTag;
    }

    @SuppressWarnings("unchecked")
    public R setReqTag(Object reqTag) {
        this.reqTag = reqTag;
        return (R) this;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    @SuppressWarnings("unchecked")
    public R setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
        return (R) this;
    }

    public long getCacheValidTimeLen() {
        return cacheValidTimeLen;
    }

    @SuppressWarnings("unchecked")
    public R setCacheValidTimeLen(long cacheValidTimeLen) {
        this.cacheValidTimeLen = cacheValidTimeLen;
        return (R) this;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    @SuppressWarnings("unchecked")
    public R setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return (R) this;
    }

    public String getBaseUrl_backup() {
        return baseUrl_backup;
    }

    @SuppressWarnings("unchecked")
    public R setBaseUrl_backup(String baseUrl_backup) {
        this.baseUrl_backup = baseUrl_backup;
        return (R) this;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    @SuppressWarnings("unchecked")
    public R setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R setCCNetCallback(CCNetCallback ccNetCallback) {
        this.ccNetCallback = ccNetCallback;
        return (R) this;
    }

    public CCNetCallback getCcNetCallback() {
        return ccNetCallback;
    }

    @SuppressWarnings("unchecked")
    public R setCCCacheSaveCallback(CCCacheSaveCallback ccCacheSaveCallback) {
        this.ccCacheSaveCallback = ccCacheSaveCallback;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R setCCCacheQueryCallback(CCCacheQueryCallback ccCacheQueryCallback) {
        this.ccCacheQueryCallback = ccCacheQueryCallback;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R setNetLifecycleComposer(FlowableTransformer<CCBaseResponse<T>, CCBaseResponse<T>> netLifecycleComposer) {
        this.netLifecycleComposer = netLifecycleComposer;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R setResponseBeanType(Type responseBeanType) {
        this.responseBeanType = responseBeanType;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R setCacheQueryMode(int cacheQueryMode) {
        this.cacheQueryMode = cacheQueryMode;
        return (R) this;
    }

    public int getCacheQueryMode() {
        return cacheQueryMode;
    }

    @SuppressWarnings("unchecked")
    public R setCacheSaveMode(int cacheSaveMode) {
        this.cacheSaveMode = cacheSaveMode;
        return (R) this;
    }

    public int getCacheSaveMode() {
        return cacheSaveMode;
    }

    public CCCanceler getNetCCCanceler() {
        return netCCCanceler;
    }

    protected void setNetCCCanceler(CCCanceler netCCCanceler) {
        this.netCCCanceler = netCCCanceler;
    }

    public CCConvert getCcConvert() {
        return ccConvert;
    }

    @SuppressWarnings("unchecked")
    public R setCcConvert(CCConvert ccConvert) {
        this.ccConvert = ccConvert;
        return (R)this;
    }

    public boolean isRequestRunning() {
        return requestRunning;
    }

    protected void setRequestRunning(boolean requestRunning) {
        this.requestRunning = requestRunning;
    }

    public boolean isForceCanceled() {
        return forceCanceled;
    }

    public void setForceCanceled(boolean forceCanceled) {
        this.forceCanceled = forceCanceled;
    }
}
