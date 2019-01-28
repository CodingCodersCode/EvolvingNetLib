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
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by CodingCodersCode on 2017/10/26.
 * <p>
 * GET类型请求类
 */

public class CCGetRequest<T> extends CCRequest<T, CCGetRequest<T>> {

    public CCGetRequest(String url, CCNetApiService apiService) {
        super(url, apiService);
    }

    @Override
    protected Flowable<CCBaseResponse<T>> getRequestFlowable() {

        return Flowable.create(new FlowableOnSubscribe<Call<ResponseBody>>() {
            @Override
            public void subscribe(FlowableEmitter<Call<ResponseBody>> e) throws Exception {
                Call<ResponseBody> call = getCCNetApiService().executeGet(CCNetUtil.regexApiUrlWithPathParam(getApiUrl(), getPathMap()), getHeaderMap(), getParamMap());

                e.onNext(call);
                e.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
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
                }).retryWhen(new FlowableRetryWithDelay(getRetryCount(), getRetryDelayTimeMillis())).onBackpressureLatest();
    }

    @Override
    protected int getHttpMethod() {
        return CCHttpMethod.GET;
    }
}
