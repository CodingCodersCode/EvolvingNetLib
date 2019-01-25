package com.codingcoderscode.evolving.net.request.base;


import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.codingcoderscode.evolving.net.cache.exception.CCDiskCacheQueryException;
import com.codingcoderscode.evolving.net.cache.mode.CCCMode;
import com.codingcoderscode.evolving.net.request.api.CCNetApiService;
import com.codingcoderscode.evolving.net.request.callback.CCCacheQueryListener;
import com.codingcoderscode.evolving.net.request.callback.CCCacheSaveListener;
import com.codingcoderscode.evolving.net.request.callback.CCNetResultListener;
import com.codingcoderscode.evolving.net.request.canceler.CCCanceler;
import com.codingcoderscode.evolving.net.request.exception.CCUnExpectedException;
import com.codingcoderscode.evolving.net.response.CCBaseResponse;
import com.codingcoderscode.evolving.net.response.convert.CCConvert;
import com.codingcoderscode.evolving.net.response.convert.CCDefaultResponseBodyConvert;
import com.codingcoderscode.evolving.net.util.NetLogUtil;
import com.codingcoderscode.evolving.net.util.Utils;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BooleanSupplier;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

//import com.codingcoderscode.evolving.net.response.callback.CCResponseCallback;

/**
 * Created by CodingCodersCode on 2017/10/26.
 * <p>
 * 请求基类
 */

public abstract class CCRequest<T, R extends CCRequest> {

    private final String LOG_TAG = CCRequest.class.getCanonicalName();

    private CCNetApiService mCCNetApiService;
    //当前请求额外header信息
    protected Map<String, String> headerMap;
    //当前请求参数信息
    protected Map<String, Object> paramMap;
    //当前请求restful api路径替换信息
    private Map<String, String> pathMap;
    //失败重试次数
    protected int retryCount;
    //每次重试前的延迟时间，单位：毫秒
    private int retryDelayTimeMillis;
    //请求标识
    private Object reqTag;
    //缓存标识
    private String cacheKey;
    //api url
    protected String apiUrl;
    //网络结果回调
    private CCNetResultListener CCNetResultListener;
    //缓存保存回调，非ui线程，位于io线程
    private CCCacheSaveListener CCCacheSaveListener;
    //缓存查询回调，非ui线程，位于io线程
    private CCCacheQueryListener CCCacheQueryListener;
    //请求生命周期管理
    private FlowableTransformer<CCBaseResponse<T>, CCBaseResponse<T>> netLifecycleComposer;
    //响应结果所对应的具体Java实体类类型
    protected Type responseBeanType;
    //缓存查找策略
    private int cacheQueryMode;
    //缓存保存策略
    private int cacheSaveMode;
    //网络取消对象
    private CCCanceler netCCCanceler;
    //取消网络请求对象
    private Subscription netCancelSubscription;
    //结果转换，用户自定义
    private CCConvert ccConvert;
    //请求是否运行
    private boolean requestRunning;
    //是否被强制退出
    private boolean forceCanceled;
    //是否以@Body形式传递参数，用于@POST和@PUT请求
    private boolean useBodyParamStyle;

    //磁盘缓存是否已经返回
    private boolean hasDiskRequestResped = false;
    //网络请求是否已经返回
    private boolean hasNetRequestResped = false;

    //是否在网络请求返回前，以固定时间间隔发送回调
    private boolean mNeedIntervalCallback = false;
    //发送网络较差回调的时间间隔 单位：毫秒
    private int mIntervalMilliSeconds = 5000;

    protected abstract Flowable<CCBaseResponse<T>> getRequestFlowable();

    protected abstract int getHttpMethod();

    public CCRequest(String url, CCNetApiService apiService) {
        this.apiUrl = url;
        this.mCCNetApiService = apiService;
    }

    /**
     * 获取磁盘缓存请求Flowable对象
     *
     * @return 磁盘缓存查询Flowable对象
     */
    private Flowable<CCBaseResponse<T>> getDiskQueryFlowable() {
        //磁盘缓存获取，包括任何形式的磁盘缓存
        return Flowable.create(new FlowableOnSubscribe<CCBaseResponse<T>>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<CCBaseResponse<T>> e) throws Exception {
                T response = null;
                CCBaseResponse<T> tccBaseResponse;
                Throwable t = null;
                try {
                    if (CCCacheQueryListener != null) {
                        response = CCCacheQueryListener.<T>onQueryFromDisk(cacheKey);
                    }
                } catch (Exception exception) {
                    t = new CCDiskCacheQueryException(exception);
                }

                if (response != null) {
                    tccBaseResponse = new CCBaseResponse<T>(response, null, true, true, true, null);
                    e.onNext(tccBaseResponse);
                    e.onComplete();
                } else {
                    t = (t != null) ? t : new CCDiskCacheQueryException("data is empty");
                    if (CCCMode.QueryMode.MODE_DISK == cacheQueryMode) {
                        e.onError(t);
                    } else {
                        tccBaseResponse = new CCBaseResponse<T>(null, null, true, true, false, t);
                        e.onNext(tccBaseResponse);
                        e.onComplete();
                    }
                }
            }
        }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io());
    }

    /**
     * 按照固定时间间隔发送信号
     *
     * @return
     */
    private Flowable<CCBaseResponse<T>> getIntervalFlowable() {
        return Flowable.intervalRange(0, 1, getIntervalMilliSeconds(), getIntervalMilliSeconds(), TimeUnit.MILLISECONDS, Schedulers.trampoline())
                .repeatUntil(new BooleanSupplier() {
                    @Override
                    public boolean getAsBoolean() throws Exception {
                        switch (cacheQueryMode) {
                            case CCCMode.QueryMode.MODE_DISK:
                                return isHasDiskRequestResped() || !isRequestRunning();
                            case CCCMode.QueryMode.MODE_NET:
                            case CCCMode.QueryMode.MODE_DISK_AND_NET:
                            default:
                                return isHasNetRequestResped() || !isRequestRunning();
                        }
                    }
                }).flatMap(new Function<Long, Publisher<CCBaseResponse<T>>>() {
                    @Override
                    public Publisher<CCBaseResponse<T>> apply(Long aLong) throws Exception {
                        switch (cacheQueryMode) {
                            case CCCMode.QueryMode.MODE_DISK:
                                if (isHasDiskRequestResped() || !isRequestRunning()) {
                                    return Flowable.empty();
                                } else {
                                    return Flowable.just(new CCBaseResponse<T>(null, null, false, false, false, null));
                                }
                            case CCCMode.QueryMode.MODE_NET:
                            case CCCMode.QueryMode.MODE_DISK_AND_NET:
                            default:
                                if (isHasNetRequestResped() || !isRequestRunning()) {
                                    return Flowable.empty();
                                } else {
                                    return Flowable.just(new CCBaseResponse<T>(null, null, false, false, false, null));
                                }
                        }

                    }
                })/*.takeUntil(new Predicate<CCBaseResponse<T>>() {
                    @Override
                    public boolean test(CCBaseResponse<T> tccBaseResponse) throws Exception {
                        return false;
                    }
                })*/;
    }

    /**
     * 获取网络请求Flowable对象
     *
     * @return 网络请求Flowable对象
     */
    private Flowable<CCBaseResponse<T>> getNetQueryFlowable() {
        //网络数据获取
        return getRequestFlowable();
    }

    /**
     * 执行请求
     */
    public synchronized void executeAsync() {
        if (isRequestRunning()) {
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
        } else {
            //处理Retrofit 2.x @XXXMap注解不允许传递null值的问题，将所有null值替换为空串("")
            paramMap = Utils.requireNonNullValues(paramMap);
        }

        switch (getCacheQueryMode()) {
            case CCCMode.QueryMode.MODE_DISK:
                resultFlowable = getDiskQueryFlowable();
                break;
            case CCCMode.QueryMode.MODE_NET:
                resultFlowable = getNetQueryFlowable();
                break;
            case CCCMode.QueryMode.MODE_DISK_AND_NET:
                resultFlowable = Flowable.merge(getDiskQueryFlowable(), getNetQueryFlowable());
                break;
            default:
                setCacheQueryMode(CCCMode.QueryMode.MODE_NET);
                resultFlowable = getNetQueryFlowable();
                break;
        }

        if (isNeedIntervalCallback()) {
            resultFlowable = Flowable.merge(resultFlowable, getIntervalFlowable());
        }

        resultFlowable = resultFlowable
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(new Function<CCBaseResponse<T>, Publisher<CCBaseResponse<T>>>() {
                    @Override
                    public Publisher<CCBaseResponse<T>> apply(@NonNull CCBaseResponse<T> tccBaseResponse) throws Exception {

                        //onSaveToCache(tccBaseResponse);

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
                if (CCNetResultListener != null && isRequestRunning() && !isForceCanceled()) {

                    netCancelSubscription = s;

                    netCCCanceler = new CCCanceler(CCRequest.this);

                    CCNetResultListener.<T>onStartRequest(reqTag, netCCCanceler);
                }
            }

            @Override
            public void onNext(CCBaseResponse<T> tccBaseResponse) {
                onDealWithResponse(tccBaseResponse);
            }

            @Override
            public void onError(Throwable t) {
                if (CCNetResultListener != null && isRequestRunning() && !isForceCanceled()) {
                    CCNetResultListener.onRequestFail(reqTag, t);
                }
                setRequestRunning(false);
                setForceCanceled(false);
            }

            @Override
            public void onComplete() {
                if (CCNetResultListener != null && isRequestRunning() && !isForceCanceled()) {
                    CCNetResultListener.onRequestComplete(reqTag);
                }
                setRequestRunning(false);
                setForceCanceled(false);
            }
        });
    }

    /**
     * 进行数据缓存处理
     *
     * @param tccBaseResponse 响应结果包装对象
     */
    @Deprecated
    private void onSaveToCache(CCBaseResponse<T> tccBaseResponse) {
        T realResponse;
        try {
            if (!isRequestRunning()) {
                return;
            }

            if (isForceCanceled()) {
                return;
            }

            if (tccBaseResponse == null) {
                return;
            }

            if (tccBaseResponse.isIntervalCallback()) {
                return;
            }

            if (tccBaseResponse.isFromCache()) {
                return;
            }

            realResponse = tccBaseResponse.getRealResponse();

            switch (getCacheSaveMode()) {
                case CCCMode.SaveMode.MODE_DEFAULT:
                    if (CCCacheSaveListener != null) {
                        CCCacheSaveListener.onSaveToDisk(cacheKey, realResponse);
                    }
                    break;
                case CCCMode.SaveMode.MODE_NONE:
                default:
                    break;
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
    private synchronized void onDealWithResponse(CCBaseResponse<T> tccBaseResponse) {
        if (!isRequestRunning()) {
            return;
        }

        if (isForceCanceled()) {
            return;
        }

        if (ifNeedIntervalCallback(tccBaseResponse)) {
            return;
        }

        if (CCNetResultListener == null) {
            return;
        }

        if (tccBaseResponse == null) {
            CCNetResultListener.onRequestFail(reqTag, new CCUnExpectedException("response is null"));
            return;
        }

        //判断响应是否是缓存返回
        if (tccBaseResponse.isFromCache()) {
            onDealWithDiskResponse(tccBaseResponse);
        } else {
            onDealWithNetResponse(tccBaseResponse);
        }
    }

    /**
     * 处理磁盘响应
     *
     * @param tccBaseResponse
     */
    private void onDealWithDiskResponse(CCBaseResponse<T> tccBaseResponse) {
        T realResponse;
        try {
            //设置 磁盘缓存返回标识
            setHasDiskRequestResped(true);

            realResponse = tccBaseResponse.getRealResponse();

            switch (this.cacheQueryMode) {
                case CCCMode.QueryMode.MODE_DISK:
                    if (isRequestRunning()) {
                        if (tccBaseResponse.isSuccessful()) {
                            CCNetResultListener.<T>onDiskCacheQuerySuccess(reqTag, realResponse);
                            CCNetResultListener.<T>onRequestSuccess(reqTag, realResponse, CCCMode.DataMode.MODE_DISK);
                        } else {
                            CCNetResultListener.<T>onDiskCacheQueryFail(reqTag, tccBaseResponse.getThrowable());
                            CCNetResultListener.<T>onRequestFail(reqTag, tccBaseResponse.getThrowable());
                        }
                    }
                    break;
                case CCCMode.QueryMode.MODE_DISK_AND_NET:
                    if (isRequestRunning()) {
                        if (tccBaseResponse.isSuccessful()) {
                            CCNetResultListener.<T>onDiskCacheQuerySuccess(reqTag, realResponse);
                        } else {
                            CCNetResultListener.<T>onDiskCacheQueryFail(reqTag, tccBaseResponse.getThrowable());
                        }
                    }
                    if (!isHasNetRequestResped()) {
                        if (tccBaseResponse.isSuccessful()) {
                            CCNetResultListener.<T>onRequestSuccess(reqTag, realResponse, CCCMode.DataMode.MODE_DISK);
                        } else {
                            CCNetResultListener.<T>onRequestFail(reqTag, tccBaseResponse.getThrowable());
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            NetLogUtil.printLog("e", getClass().getCanonicalName(), "处理磁盘数据响应发生异常", e);
        }
    }

    /**
     * 处理网络响应
     *
     * @param tccBaseResponse
     */
    private void onDealWithNetResponse(CCBaseResponse<T> tccBaseResponse) {
        T realResponse;
        try {
            //设置 网络返回标识
            setHasNetRequestResped(true);

            realResponse = tccBaseResponse.getRealResponse();

            switch (this.cacheQueryMode) {
                case CCCMode.QueryMode.MODE_NET:
                case CCCMode.QueryMode.MODE_DISK_AND_NET:
                    if (isRequestRunning()) {
                        if (tccBaseResponse.isSuccessful()) {
                            CCNetResultListener.<T>onNetSuccess(reqTag, realResponse);
                            CCNetResultListener.<T>onRequestSuccess(reqTag, realResponse, CCCMode.DataMode.MODE_NET);
                        } else {
                            CCNetResultListener.<T>onNetFail(reqTag, tccBaseResponse.getThrowable());
                            CCNetResultListener.<T>onRequestFail(reqTag, tccBaseResponse.getThrowable());
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            NetLogUtil.printLog("e", getClass().getCanonicalName(), "处理磁盘数据响应发生异常", e);
        }
    }

    /**
     * 是否需要将网络状况差的信息进行Toast
     *
     * @param tccBaseResponse
     */
    private boolean ifNeedIntervalCallback(CCBaseResponse<T> tccBaseResponse) {
        try {
            if (tccBaseResponse == null) {
                return false;
            }

            if (tccBaseResponse.isIntervalCallback() && CCNetResultListener != null) {
                CCNetResultListener.onIntervalCallback();
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    /**
     * 结果转换：json ==> JavaBean
     *
     * @param responseBody
     * @return
     */
    protected T convertResponse(ResponseBody responseBody) throws Exception {
        T response;
        try {
            if (getCcConvert() != null) {
                response = getCcConvert().<T>convert(responseBody, responseBeanType);
            } else {
                response = CCDefaultResponseBodyConvert.<T>convertResponse(responseBody, responseBeanType);
            }
        } catch (Exception e) {
            /*response = null;*/
            throw new CCUnExpectedException(e);
        }
        return response;
    }

    /**
     * 取消 or 中断请求
     */
    public void cancel() {
        try {

            setRequestRunning(false);
            setForceCanceled(true);

            if (netCancelSubscription != null) {
                netCancelSubscription.cancel();
                netCancelSubscription = null;
            }

        } catch (Exception e) {

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

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    @SuppressWarnings("unchecked")
    public R setParamMap(Map<String, Object> paramMap) {
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

    public String getApiUrl() {
        return apiUrl;
    }

    @SuppressWarnings("unchecked")
    public R setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R setCCNetCallback(CCNetResultListener CCNetResultListener) {
        this.CCNetResultListener = CCNetResultListener;
        return (R) this;
    }

    public CCNetResultListener getCCNetResultListener() {
        return CCNetResultListener;
    }

    @SuppressWarnings("unchecked")
    public R setCCCacheSaveCallback(CCCacheSaveListener CCCacheSaveListener) {
        this.CCCacheSaveListener = CCCacheSaveListener;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R setCCCacheQueryCallback(CCCacheQueryListener CCCacheQueryListener) {
        this.CCCacheQueryListener = CCCacheQueryListener;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R setNetLifecycleComposer(FlowableTransformer<CCBaseResponse<T>, CCBaseResponse<T>> netLifecycleComposer) {
        this.netLifecycleComposer = netLifecycleComposer;
        return (R) this;
    }

    public FlowableTransformer<CCBaseResponse<T>, CCBaseResponse<T>> getNetLifecycleComposer() {
        return netLifecycleComposer;
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

    @Deprecated
    @SuppressWarnings("unchecked")
    public R setCacheSaveMode(int cacheSaveMode) {
        this.cacheSaveMode = cacheSaveMode;
        return (R) this;
    }

    //useBodyParamStyle
    public boolean isUseBodyParamStyle() {
        return useBodyParamStyle;
    }

    @SuppressWarnings("unchecked")
    public R setUseBodyParamStyle(boolean useBodyParamStyle) {
        this.useBodyParamStyle = useBodyParamStyle;
        return (R) this;
    }

    public int getCacheSaveMode() {
        return cacheSaveMode;
    }

    public CCCanceler getNetCCCanceler() {
        return netCCCanceler;
    }

    public CCConvert getCcConvert() {
        return ccConvert;
    }

    @SuppressWarnings("unchecked")
    public R setCcConvert(CCConvert ccConvert) {
        this.ccConvert = ccConvert;
        return (R) this;
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

    public boolean isHasDiskRequestResped() {
        return hasDiskRequestResped;
    }

    public void setHasDiskRequestResped(boolean hasDiskRequestResped) {
        this.hasDiskRequestResped = hasDiskRequestResped;
    }

    public boolean isHasNetRequestResped() {
        return hasNetRequestResped;
    }

    public void setHasNetRequestResped(boolean hasNetRequestResped) {
        this.hasNetRequestResped = hasNetRequestResped;
    }

    public boolean isNeedIntervalCallback() {
        return mNeedIntervalCallback;
    }

    /**
     * 设置是否检测网络状态
     *
     * @param needToCheckNetCondition
     * @return
     */
    @SuppressWarnings("unchecked")
    public R setNeedIntervalCallback(boolean needToCheckNetCondition) {
        this.mNeedIntervalCallback = needToCheckNetCondition;
        return (R) this;
    }

    public int getIntervalMilliSeconds() {
        return mIntervalMilliSeconds;
    }

    /**
     * 网络状态检测间隔 单位：毫秒
     *
     * @param intervalMilliSeconds
     * @return
     */
    @SuppressWarnings("unchecked")
    public R setIntervalMilliSeconds(int intervalMilliSeconds) {
        this.mIntervalMilliSeconds = intervalMilliSeconds;
        return (R) this;
    }

    public CCNetApiService getCCNetApiService() {
        return mCCNetApiService;
    }
}
