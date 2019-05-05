package com.codingcoderscode.evolving.net.request.base;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.codingcoderscode.evolving.net.cache.exception.CCDiskCacheQueryException;
import com.codingcoderscode.evolving.net.cache.mode.CCMode;
import com.codingcoderscode.evolving.net.request.api.CCNetApiService;
import com.codingcoderscode.evolving.net.request.exception.CCSampleHttpException;
import com.codingcoderscode.evolving.net.request.exception.CCUnExpectedException;
import com.codingcoderscode.evolving.net.request.listener.CCCacheQueryListener;
import com.codingcoderscode.evolving.net.request.listener.CCCacheSaveListener;
import com.codingcoderscode.evolving.net.request.listener.CCNetResultListener;
import com.codingcoderscode.evolving.net.request.retry.CCFlowableRetryWithDelay;
import com.codingcoderscode.evolving.net.response.CCBaseResponse;
import com.codingcoderscode.evolving.net.response.convert.CCConvert;
import com.codingcoderscode.evolving.net.response.convert.CCDefaultResponseBodyConvert;
import com.codingcoderscode.evolving.net.util.CCLogUtil;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.lang.reflect.Type;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Function;
import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Date：2019/4/30 13:50
 * <p>
 * author: CodingCodersCode
 */
public abstract class CCSimpleRequest<T> extends CCRequest<T, CCSimpleRequest<T>> {

    //网络结果回调
    private CCNetResultListener mResultListener;

    //缓存保存回调，非ui线程，位于io线程
    private CCCacheSaveListener mCacheSaveListener;

    //缓存查询回调，非ui线程，位于io线程
    private CCCacheQueryListener mCacheQueryListener;

    //缓存查找策略
    private int cacheQueryMode;

    //缓存保存策略
    private int cacheSaveMode;

    //是否以@Body形式传递参数，用于@POST和@PUT请求
    private boolean useBodyParamStyle;

    //结果转换，用户自定义
    private CCConvert ccConvert;

    //响应结果所对应的具体Java实体类类型
    private Type responseBeanType;

    //缓存标识
    private String mCacheTag;

    //额外信息
    private Object mExtInfo;

    public CCSimpleRequest(String url, CCNetApiService apiService) {
        super(url, apiService);
        setCacheQueryMode(CCMode.QueryMode.MODE_NET);
    }

    /**
     * 获取磁盘缓存请求Flowable对象
     *
     * @return 磁盘缓存查询Flowable对象
     */
    protected Flowable<CCBaseResponse<T>> getDiskQueryFlowable() {
        //磁盘缓存获取，包括任何形式的磁盘缓存
        return Flowable.create(new FlowableOnSubscribe<CCBaseResponse<T>>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<CCBaseResponse<T>> e) throws Exception {
                T response = null;
                CCBaseResponse<T> tccBaseResponse;
                Throwable t = null;
                try {
                    if (mCacheQueryListener != null) {
                        response = mCacheQueryListener.<T>onQueryFromDisk(getCacheTag());
                    }
                } catch (Exception exception) {
                    t = new CCDiskCacheQueryException(exception);
                }

                if (response != null) {
                    tccBaseResponse = new CCBaseResponse<T>(response, null, true, false, true, null);
                    e.onNext(tccBaseResponse);
                    e.onComplete();
                } else {
                    t = (t != null) ? t : new CCDiskCacheQueryException("data is empty");
                    if (CCMode.QueryMode.MODE_DISK == getCacheQueryMode()) {
                        e.onError(t);
                    } else {
                        tccBaseResponse = new CCBaseResponse<T>(null, null, true, false, false, t);
                        e.onNext(tccBaseResponse);
                        e.onComplete();
                    }
                }
            }
        }, BackpressureStrategy.LATEST)/*.subscribeOn(Schedulers.io())*/;
    }

    @Override
    protected Flowable<CCBaseResponse<T>> getRequestFlowable() {
        return Flowable.create(new FlowableOnSubscribe<Call<ResponseBody>>() {
            @Override
            public void subscribe(FlowableEmitter<Call<ResponseBody>> e) throws Exception {

                Call<ResponseBody> call;

                call = getRequestCall();

                e.onNext(call);
                e.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                //.subscribeOn(Schedulers.io())
                //.unsubscribeOn(Schedulers.io())
                //.observeOn(Schedulers.io())
                .retry(mRetryCount)
                .flatMap(new Function<Call<ResponseBody>, Publisher<CCBaseResponse<T>>>() {
                    @Override
                    public Publisher<CCBaseResponse<T>> apply(Call<ResponseBody> responseBodyCall) throws Exception {

                        T realResponse = null;
                        Response<ResponseBody> retrofitResponse;
                        Headers headers = null;
                        try {
                            retrofitResponse = responseBodyCall.clone().execute();

                            if (retrofitResponse.isSuccessful()) {
                                headers = retrofitResponse.headers();

                                realResponse = convertResponse(retrofitResponse.body());
                            } else {
                                throw new CCSampleHttpException(retrofitResponse, retrofitResponse.errorBody());
                            }

                        } catch (Exception exception) {
                            throw new CCUnExpectedException(exception);
                        }


                        return Flowable.just(new CCBaseResponse<T>(realResponse, headers, false, false, true, null));
                    }
                }).retryWhen(new CCFlowableRetryWithDelay(getRetryCount(), getRetryDelayTimeMillis())).onBackpressureLatest();
    }

    @Override
    protected void onSubscribeLocal(Subscription s) {
        super.onSubscribeLocal(s);
        try {
            if (mResultListener != null && isRequestRunning() && !isForceCanceled()) {
                mResultListener.<T>onStartRequest(getReqTag(), getNetCCCanceler());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNextLocal(CCBaseResponse<T> tccBaseResponse) {
        super.onNextLocal(tccBaseResponse);

        onDealWithResponse(tccBaseResponse);
    }

    @Override
    public void onErrorLocal(Throwable t) {
        super.onErrorLocal(t);
    }

    @Override
    public void onCompleteLocal() {
        super.onCompleteLocal();
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

        if (mResultListener == null) {
            return;
        }

        if (tccBaseResponse == null) {
            mResultListener.onRequestFail(getReqTag(), new CCUnExpectedException("response is null"));
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

            switch (this.getCacheQueryMode()) {
                case CCMode.QueryMode.MODE_DISK:
                    if (isRequestRunning()) {
                        if (tccBaseResponse.isSuccessful()) {
                            mResultListener.<T>onDiskCacheQuerySuccess(getReqTag(), realResponse);
                            mResultListener.<T>onRequestSuccess(getReqTag(), realResponse, CCMode.DataMode.MODE_DISK);
                        } else {
                            mResultListener.<T>onDiskCacheQueryFail(getReqTag(), tccBaseResponse.getThrowable());
                            mResultListener.<T>onRequestFail(getReqTag(), tccBaseResponse.getThrowable());
                        }
                    }
                    break;
                case CCMode.QueryMode.MODE_DISK_AND_NET:
                    if (isRequestRunning()) {
                        if (tccBaseResponse.isSuccessful()) {
                            mResultListener.<T>onDiskCacheQuerySuccess(getReqTag(), realResponse);
                        } else {
                            mResultListener.<T>onDiskCacheQueryFail(getReqTag(), tccBaseResponse.getThrowable());
                        }

                        if (!isHasNetRequestResped()) {
                            if (tccBaseResponse.isSuccessful()) {
                                mResultListener.<T>onRequestSuccess(getReqTag(), realResponse, CCMode.DataMode.MODE_DISK);
                            } else {
                                mResultListener.<T>onRequestFail(getReqTag(), tccBaseResponse.getThrowable());
                            }
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            CCLogUtil.printLog("e", getClass().getCanonicalName(), "处理磁盘数据响应发生异常", e);
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

            switch (this.getCacheQueryMode()) {
                case CCMode.QueryMode.MODE_NET:
                case CCMode.QueryMode.MODE_DISK_AND_NET:
                    if (isRequestRunning()) {
                        if (tccBaseResponse.isSuccessful()) {
                            mResultListener.<T>onNetSuccess(getReqTag(), realResponse);
                            mResultListener.<T>onRequestSuccess(getReqTag(), realResponse, CCMode.DataMode.MODE_NET);
                        } else {
                            mResultListener.<T>onNetFail(getReqTag(), tccBaseResponse.getThrowable());
                            mResultListener.<T>onRequestFail(getReqTag(), tccBaseResponse.getThrowable());
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            CCLogUtil.printLog("e", getClass().getCanonicalName(), "处理磁盘数据响应发生异常", e);
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

            if (tccBaseResponse.isIntervalCallback() && mResultListener != null) {
                mResultListener.onIntervalCallback();
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
                response = getCcConvert().<T>convert(responseBody, getResponseBeanType());
            } else {
                response = CCDefaultResponseBodyConvert.<T>convertResponse(responseBody, getResponseBeanType());
            }
        } catch (Exception e) {
            /*response = null;*/
            throw new CCUnExpectedException(e);
        }
        return response;
    }

    /**
     * 进行数据缓存处理
     *
     * @param tccBaseResponse 响应结果包装对象
     */
    @VisibleForTesting
    @Override
    protected void onSaveToCache(CCBaseResponse<T> tccBaseResponse) {
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
                case CCMode.SaveMode.MODE_DEFAULT:
                    if (mCacheSaveListener != null) {
                        mCacheSaveListener.onSaveToDisk(getCacheTag(), realResponse);
                    }
                    break;
                case CCMode.SaveMode.MODE_NONE:
                default:
                    break;
            }
        } catch (Exception exception) {
            CCLogUtil.printLog("e", getClass().getCanonicalName(), "缓存数据失败", exception);
        }
    }

    public CCSimpleRequest<T> setCCNetCallback(CCNetResultListener resultListener) {
        this.mResultListener = resultListener;
        return this;
    }

    public CCNetResultListener getCCNetResultListener() {
        return mResultListener;
    }

    @VisibleForTesting
    @SuppressWarnings("unchecked")
    public CCSimpleRequest<T> setCCCacheSaveCallback(CCCacheSaveListener cacheSaveListener) {
        this.mCacheSaveListener = cacheSaveListener;
        return this;
    }

    @SuppressWarnings("unchecked")
    public CCSimpleRequest<T> setCCCacheQueryCallback(CCCacheQueryListener cacheQueryListener) {
        this.mCacheQueryListener = cacheQueryListener;
        return this;
    }

    @SuppressWarnings("unchecked")
    public CCSimpleRequest<T> setCacheQueryMode(int cacheQueryMode) {
        this.cacheQueryMode = cacheQueryMode;
        return this;
    }

    @Override
    public int getCacheQueryMode() {
        return cacheQueryMode;
    }

    @VisibleForTesting
    @Deprecated
    @SuppressWarnings("unchecked")
    public CCSimpleRequest<T> setCacheSaveMode(int cacheSaveMode) {
        this.cacheSaveMode = cacheSaveMode;
        return this;
    }

    @Override
    public int getCacheSaveMode() {
        return this.cacheSaveMode;
    }

    //useBodyParamStyle
    public boolean isUseBodyParamStyle() {
        return useBodyParamStyle;
    }

    @SuppressWarnings("unchecked")
    public CCSimpleRequest<T> setUseBodyParamStyle(boolean useBodyParamStyle) {
        this.useBodyParamStyle = useBodyParamStyle;
        return this;
    }

    public CCConvert getCcConvert() {
        return ccConvert;
    }

    @SuppressWarnings("unchecked")
    public CCSimpleRequest<T> setCcConvert(CCConvert ccConvert) {
        this.ccConvert = ccConvert;
        return this;
    }

    @SuppressWarnings("unchecked")
    public CCSimpleRequest<T> setResponseBeanType(Type responseBeanType) {
        this.responseBeanType = responseBeanType;
        return this;
    }

    public Type getResponseBeanType() {
        return responseBeanType;
    }

    public String getCacheTag() {
        return mCacheTag;
    }

    @SuppressWarnings("unchecked")
    public CCSimpleRequest<T> setCacheTag(String cacheTag) {
        this.mCacheTag = cacheTag;
        return this;
    }

    @SuppressWarnings("unchecked")
    public CCSimpleRequest<T> setExtInfo(Object extInfo) {
        this.mExtInfo = extInfo;
        return this;
    }

    public Object getExtInfo() {
        return mExtInfo;
    }

}
