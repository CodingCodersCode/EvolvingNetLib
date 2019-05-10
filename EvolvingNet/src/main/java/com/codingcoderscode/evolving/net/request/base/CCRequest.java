package com.codingcoderscode.evolving.net.request.base;


import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.codingcoderscode.evolving.net.cache.mode.CCMode;
import com.codingcoderscode.evolving.net.request.api.CCNetApiService;
import com.codingcoderscode.evolving.net.request.canceler.CCCanceler;
import com.codingcoderscode.evolving.net.response.CCBaseResponse;
import com.codingcoderscode.evolving.net.util.CCLogUtil;
import com.codingcoderscode.evolving.net.util.CCUtils;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BooleanSupplier;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;


/**
 * Created by CodingCodersCode on 2017/10/26.
 * <p>
 * 请求基类
 */

public abstract class CCRequest<T, R extends CCRequest> {

    private final String LOG_TAG = CCRequest.class.getCanonicalName();

    private CCNetApiService mApiService;

    //当前请求额外header信息
    protected Map<String, String> mHeaderMap;

    //当前请求参数信息
    protected Map<String, Object> mRequestParam;

    //当前请求restful api路径替换信息
    private Map<String, String> mPathMap;

    //失败重试次数
    protected int mRetryCount;

    //每次重试前的延迟时间，单位：毫秒
    private int mRetryDelayTimeMillis;

    //请求标识
    private Object mReqTag;

    //api url
    private String mApiUrl;

    //网络取消对象
    private CCCanceler netCCCanceler;

    //取消网络请求对象
    private Subscription netCancelSubscription;


    //请求是否运行
    private boolean requestRunning;

    //是否被强制退出
    private boolean forceCanceled;


    //磁盘缓存是否已经返回
    private boolean hasDiskRequestResped = false;

    //网络请求是否已经返回
    private boolean hasNetRequestResped = false;

    //是否在网络请求返回前，以固定时间间隔发送回调
    private boolean mNeedIntervalCallback = false;

    //发送网络较差回调的时间间隔 单位：毫秒
    private int mIntervalMilliSeconds = 5000;

    //请求生命周期自动管理对象
    private FlowableTransformer<CCBaseResponse<T>, CCBaseResponse<T>> mRequestLifecycleDisposeComposer;

    protected abstract int getHttpMethod();

    protected abstract Call<ResponseBody> getRequestCall();

    /**
     * 进行数据缓存处理
     *
     * @param tccBaseResponse 响应结果包装对象
     */
    @VisibleForTesting
    protected abstract void onSaveToCache(CCBaseResponse<T> tccBaseResponse);

    /**
     * 获取磁盘缓存请求Flowable对象
     *
     * @return 磁盘缓存查询Flowable对象
     */
    protected abstract Flowable<CCBaseResponse<T>> getDiskQueryFlowable();

    protected abstract Flowable<CCBaseResponse<T>> getRequestFlowable();

    public abstract int getCacheQueryMode();

    public abstract int getCacheSaveMode();

    public CCRequest(String url, CCNetApiService apiService) {
        this.mApiUrl = url;
        this.mApiService = apiService;
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
                        switch (getCacheQueryMode()) {
                            case CCMode.QueryMode.MODE_DISK:
                                return isHasDiskRequestResped() || !isRequestRunning();
                            case CCMode.QueryMode.MODE_NET:
                            case CCMode.QueryMode.MODE_DISK_AND_NET:
                            default:
                                return isHasNetRequestResped() || !isRequestRunning();
                        }
                    }
                }).flatMap(new Function<Long, Publisher<CCBaseResponse<T>>>() {
                    @Override
                    public Publisher<CCBaseResponse<T>> apply(Long aLong) throws Exception {
                        switch (getCacheQueryMode()) {
                            case CCMode.QueryMode.MODE_DISK:
                                if (isHasDiskRequestResped() || !isRequestRunning()) {
                                    return Flowable.empty();
                                } else {
                                    return Flowable.just(new CCBaseResponse<T>(null, null, false, true, false, null));
                                }
                            case CCMode.QueryMode.MODE_NET:
                            case CCMode.QueryMode.MODE_DISK_AND_NET:
                            default:
                                if (isHasNetRequestResped() || !isRequestRunning()) {
                                    return Flowable.empty();
                                } else {
                                    return Flowable.just(new CCBaseResponse<T>(null, null, false, true, false, null));
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

        if (TextUtils.isEmpty(mApiUrl)) {
            mApiUrl = "";
        }

        if (mHeaderMap == null) {
            mHeaderMap = new HashMap<>();
        }

        if (mRequestParam == null) {
            mRequestParam = new HashMap<>();
        } else {
            //处理Retrofit 2.x @XXXMap注解不允许传递null值的问题，将所有null值替换为空串("")
            mRequestParam = CCUtils.requireNonNullValues(mRequestParam);
        }

        /**
         * 根据不同的数据请求类型，组合数据请求
         */
        switch (getCacheQueryMode()) {
            case CCMode.QueryMode.MODE_DISK:
                resultFlowable = getDiskQueryFlowable();
                break;
            case CCMode.QueryMode.MODE_NET:
                resultFlowable = getNetQueryFlowable();
                break;
            case CCMode.QueryMode.MODE_DISK_AND_NET:
                resultFlowable = Flowable.merge(getDiskQueryFlowable(), getNetQueryFlowable());
                break;
            default:
                //setCacheQueryMode(CCMode.QueryMode.MODE_NET);
                resultFlowable = getNetQueryFlowable();
                break;
        }

        if (isNeedIntervalCallback()) {
            resultFlowable = Flowable.merge(resultFlowable, getIntervalFlowable());
        }

        resultFlowable = resultFlowable
                /*.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())*/
                .flatMap(new Function<CCBaseResponse<T>, Publisher<CCBaseResponse<T>>>() {
                    @Override
                    public Publisher<CCBaseResponse<T>> apply(@NonNull CCBaseResponse<T> tccBaseResponse) throws Exception {

                        onSaveToCache(tccBaseResponse);

                        return Flowable.just(tccBaseResponse);
                    }
                });

        resultFlowable = resultFlowable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        //设置请求生命周期自动管理对象，composer为null时，表示不需要自动管理请求生命周期，否则，需要自动管理请求生命周期，同时，composer为null时compose()抛出异常，特此判空
        if (this.getLifecycleDisposeComposer() != null) {
            resultFlowable = resultFlowable.compose(this.getLifecycleDisposeComposer());
        }

        resultFlowable.subscribe(new Subscriber<CCBaseResponse<T>>() {
            @Override
            public void onSubscribe(Subscription s) {
                onSubscribeLocal(s);
            }

            @Override
            public void onNext(CCBaseResponse<T> tccBaseResponse) {
                onNextLocal(tccBaseResponse);
            }

            @Override
            public void onError(Throwable t) {
                onErrorLocal(t);
            }

            @Override
            public void onComplete() {
                onCompleteLocal();
            }
        });
    }

    /**
     * 需实现类根据实际情况复写
     *
     * @param s
     */
    @CallSuper
    protected void onSubscribeLocal(Subscription s) {
        s.request(Long.MAX_VALUE);
        netCancelSubscription = s;
        netCCCanceler = new CCCanceler(this);
    }

    /**
     * 需实现类根据实际情况复写
     *
     * @param tccBaseResponse
     */
    @CallSuper
    protected void onNextLocal(CCBaseResponse<T> tccBaseResponse) {

    }

    /**
     * 需实现类根据实际情况复写
     *
     * @param t
     */
    @CallSuper
    protected void onErrorLocal(Throwable t) {

    }

    /**
     * 需实现类根据实际情况复写
     */
    @CallSuper
    protected void onCompleteLocal() {
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
            CCLogUtil.printLog("e", getClass().getCanonicalName(), "发生异常", e);
        }
    }

    /**
     * 获取请求的header信息集合
     *
     * @return
     */
    public Map<String, String> getHeaderMap() {
        return mHeaderMap;
    }

    /**
     * 设置请求的header信息集合
     *
     * @param headerMap
     * @return
     */
    @SuppressWarnings("unchecked")
    public R setHeaderMap(Map<String, String> headerMap) {
        this.mHeaderMap = headerMap;
        return (R) this;
    }

    /**
     * 获取请求参数信息集合
     *
     * @return
     */
    public Map<String, Object> getRequestParam() {
        return mRequestParam;
    }

    /**
     * 设置请求参数信息集合
     *
     * @param requestParam
     * @return
     */
    @SuppressWarnings("unchecked")
    public R setRequestParam(Map<String, Object> requestParam) {
        this.mRequestParam = requestParam;
        return (R) this;
    }

    /**
     * 获取restful api的path seg替换信息
     *
     * @return
     */
    public Map<String, String> getPathMap() {
        return mPathMap;
    }

    /**
     * 设置restful api的path seg替换信息
     *
     * @param pathMap
     * @return
     */
    @SuppressWarnings("unchecked")
    public R setPathMap(Map<String, String> pathMap) {
        this.mPathMap = pathMap;
        return (R) this;
    }

    /**
     * 获取请求失败重试次数
     *
     * @return
     */
    public int getRetryCount() {
        return mRetryCount;
    }

    /**
     * 设置请求失败重试次数
     *
     * @param retryCount
     * @return
     */
    @SuppressWarnings("unchecked")
    public R setRetryCount(int retryCount) {
        this.mRetryCount = retryCount;
        return (R) this;
    }

    /**
     * 获取请求失败重试延迟间隔，单位：毫秒
     *
     * @return
     */
    public int getRetryDelayTimeMillis() {
        return mRetryDelayTimeMillis;
    }

    /**
     * 设置请求失败重试延迟间隔，单位：毫秒
     *
     * @param retryDelayTimeMillis
     * @return
     */
    @SuppressWarnings("unchecked")
    public R setRetryDelayTimeMillis(int retryDelayTimeMillis) {
        this.mRetryDelayTimeMillis = retryDelayTimeMillis;
        return (R) this;
    }

    /**
     * 获取请求标识
     *
     * @return
     */
    public Object getReqTag() {
        return mReqTag;
    }

    /**
     * 设置请求标识，强烈建议自定义类复写hashCode和equals方法
     *
     * @param reqTag
     * @return
     */
    @SuppressWarnings("unchecked")
    public R setReqTag(Object reqTag) {
        this.mReqTag = reqTag;
        return (R) this;
    }

    /**
     * 获取请求的接口地址
     *
     * @return
     */
    public String getApiUrl() {
        return mApiUrl;
    }

    /**
     * 设置请求的接口地址
     *
     * @param apiUrl
     * @return
     */
    @SuppressWarnings("unchecked")
    public R setApiUrl(String apiUrl) {
        this.mApiUrl = apiUrl;
        return (R) this;
    }

    /**
     * 获取网络请求取消or中断对象
     *
     * @return
     */
    public CCCanceler getNetCCCanceler() {
        return netCCCanceler;
    }

    /**
     * 请求是否处于运行状态
     *
     * @return
     */
    public boolean isRequestRunning() {
        return requestRunning;
    }

    /**
     * 设置请求是否处于运行状态
     *
     * @param requestRunning
     */
    protected void setRequestRunning(boolean requestRunning) {
        this.requestRunning = requestRunning;
    }

    /**
     * 是否用户强制退出
     *
     * @return
     */
    public boolean isForceCanceled() {
        return forceCanceled;
    }

    /**
     * 设置请求是否用户强制退出
     *
     * @param forceCanceled
     */
    public void setForceCanceled(boolean forceCanceled) {
        this.forceCanceled = forceCanceled;
    }

    /**
     * 磁盘缓存请求是否返回
     *
     * @return
     */
    protected boolean isHasDiskRequestResped() {
        return hasDiskRequestResped;
    }

    /**
     * 设置磁盘缓存请求是否返回
     *
     * @param hasDiskRequestResped
     */
    protected void setHasDiskRequestResped(boolean hasDiskRequestResped) {
        this.hasDiskRequestResped = hasDiskRequestResped;
    }

    /**
     * 网络请求是否返回
     *
     * @return
     */
    protected boolean isHasNetRequestResped() {
        return hasNetRequestResped;
    }

    /**
     * 设置网络请求是否返回
     *
     * @param hasNetRequestResped
     */
    protected void setHasNetRequestResped(boolean hasNetRequestResped) {
        this.hasNetRequestResped = hasNetRequestResped;
    }

    /**
     * 是否开启了周期回调
     *
     * @return
     */
    public boolean isNeedIntervalCallback() {
        return mNeedIntervalCallback;
    }

    /**
     * 设置是否需要周期回调
     *
     * @param needToCheckNetCondition
     * @return
     */
    @SuppressWarnings("unchecked")
    public R setNeedIntervalCallback(boolean needToCheckNetCondition) {
        this.mNeedIntervalCallback = needToCheckNetCondition;
        return (R) this;
    }

    /**
     * 获取周期回调间隔，单位：毫秒
     *
     * @return
     */
    public int getIntervalMilliSeconds() {
        return mIntervalMilliSeconds;
    }

    /**
     * 设置周期回调间隔 单位：毫秒
     *
     * @param intervalMilliSeconds
     * @return
     */
    @SuppressWarnings("unchecked")
    public R setIntervalMilliSeconds(int intervalMilliSeconds) {
        this.mIntervalMilliSeconds = intervalMilliSeconds;
        return (R) this;
    }

    /**
     * 获取retrofit请求的api service实例
     *
     * @return
     */
    protected CCNetApiService getCCNetApiService() {
        return mApiService;
    }

    /**
     * 设置请求生命周期自动管理对象
     *
     * @param disposeComposer
     * @return
     */
    @VisibleForTesting
    public R setLifecycleDisposeComposer(FlowableTransformer<CCBaseResponse<T>, CCBaseResponse<T>> disposeComposer) {
        this.mRequestLifecycleDisposeComposer = disposeComposer;
        return (R) this;
    }

    /**
     * 获取请求生命周期自动管理对象
     *
     * @return
     */
    @VisibleForTesting
    public FlowableTransformer<CCBaseResponse<T>, CCBaseResponse<T>> getLifecycleDisposeComposer() {
        return this.mRequestLifecycleDisposeComposer;
    }
}
