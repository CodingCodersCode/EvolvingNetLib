package com.codingcoderscode.evolving.net.request;


import com.codingcoderscode.evolving.net.CCRxNetManager;
import com.codingcoderscode.evolving.net.request.api.CCNetApiService;
import com.codingcoderscode.evolving.net.request.base.CCRequest;
import com.codingcoderscode.evolving.net.request.exception.CCSampleHttpException;
import com.codingcoderscode.evolving.net.request.exception.CCUnExpectedException;
import com.codingcoderscode.evolving.net.request.method.CCHttpMethod;
import com.codingcoderscode.evolving.net.request.retry.FlowableRetryWithDelay;
import com.codingcoderscode.evolving.net.response.CCBaseResponse;
import com.codingcoderscode.evolving.net.util.CCNetUtil;

import org.reactivestreams.Publisher;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by CodingCodersCode on 2017/10/30.
 * <p>
 * HEAD类型请求类
 */

public class CCHeadRequest<T> extends CCRequest<T, CCHeadRequest<T>> {

    public CCHeadRequest(String url, CCNetApiService apiService) {
        super(url, apiService);
    }

    @Override
    protected Flowable<CCBaseResponse<T>> getRequestFlowable() {

        return Flowable.create(new FlowableOnSubscribe<Call<Void>>() {
            @Override
            public void subscribe(FlowableEmitter<Call<Void>> e) throws Exception {
                Call<Void> call = getCCNetApiService().executeHead(CCNetUtil.regexApiUrlWithPathParam(getApiUrl(), getPathMap()), getHeaderMap(), getParamMap());

                e.onNext(call);
                e.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .retry(retryCount)
                .flatMap(new Function<Call<Void>, Publisher<CCBaseResponse<T>>>() {
                    @Override
                    public Publisher<CCBaseResponse<T>> apply(Call<Void> voidCall) throws Exception {

                        T realResponse = null;
                        Headers headers = null;
                        try {
                            Response<Void> retrofitResponse = voidCall.execute();

                            if (retrofitResponse.isSuccessful()) {
                                headers = retrofitResponse.headers();

                                realResponse = convertResponse(null);
                            } else {
                                throw new CCSampleHttpException(retrofitResponse, retrofitResponse.errorBody());
                            }

                        } catch (Exception exception) {
                            throw new CCUnExpectedException(exception);
                        }


                        return Flowable.just(new CCBaseResponse<T>(realResponse, headers, false, false, true, null));
                    }
                }).retryWhen(new FlowableRetryWithDelay(getRetryCount(), getRetryDelayTimeMillis())).onBackpressureLatest();
    }

    @Override
    protected int getHttpMethod() {
        return CCHttpMethod.HEAD;
    }
}
