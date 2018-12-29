package com.codingcoderscode.evolving.net.request;

import android.text.TextUtils;

import com.codingcoderscode.evolving.net.CCRxNetManager;
import com.codingcoderscode.evolving.net.cache.mode.CCCMode;
import com.codingcoderscode.evolving.net.request.base.CCRequest;
import com.codingcoderscode.evolving.net.request.entity.CCFile;
import com.codingcoderscode.evolving.net.request.exception.CCSampleHttpException;
import com.codingcoderscode.evolving.net.request.exception.CCUnExpectedException;
import com.codingcoderscode.evolving.net.request.method.CCHttpMethod;
import com.codingcoderscode.evolving.net.request.requestbody.CCSimpleUploadRequestBody;
import com.codingcoderscode.evolving.net.request.retry.FlowableRetryWithDelay;
import com.codingcoderscode.evolving.net.response.CCBaseResponse;
import com.codingcoderscode.evolving.net.util.CCNetUtil;

import org.reactivestreams.Publisher;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by CodingCodersCode on 2017/10/31.
 * <p>
 * 上传文件类型请求类
 */

public class CCUploadRequest<T> extends CCRequest<T, CCUploadRequest<T>> {

    //private CCUploadProgressCallback ccUploadProgressCallback;

    private Map<String, Object> txtParamMap;

    private Map<String, CCFile> fileParamMap;

    public CCUploadRequest(String url) {
        this.apiUrl = url;
    }

    @Override
    protected Flowable<CCBaseResponse<T>> getRequestFlowable() {

        return Flowable.create(new FlowableOnSubscribe<Call<ResponseBody>>() {
            @Override
            public void subscribe(FlowableEmitter<Call<ResponseBody>> e) throws Exception {
                RequestBody requestBody;
                ArrayList<MultipartBody.Part> paramPartList = new ArrayList<>();
                CCFile fileValue;
                MultipartBody.Part partBody;
                File uploadFile;
                try {
                    if (txtParamMap != null) {
                        for (Map.Entry<String, ?> entry : txtParamMap.entrySet()) {

                            partBody = MultipartBody.Part.createFormData(entry.getKey(), entry.getValue().toString());

                            paramPartList.add(partBody);

                        }
                    }

                    if (fileParamMap != null) {
                        for (Map.Entry<String, CCFile> entry : fileParamMap.entrySet()) {

                            fileValue = entry.getValue();

                            if (fileValue == null || TextUtils.isEmpty(fileValue.getUrl())) {
                                continue;
                            }

                            uploadFile = new File(fileValue.getUrl());

                            requestBody = new CCSimpleUploadRequestBody(entry.getKey(), MediaType.parse(fileValue.getMimeType()), uploadFile, getCcNetCallback());

                            partBody = MultipartBody.Part.createFormData(entry.getKey(), uploadFile.getName(), requestBody);

                            paramPartList.add(partBody);

                        }
                    }
                } catch (Exception exception) {

                }

                Call<ResponseBody> call = CCRxNetManager.getCcNetApiService().executeUpload(CCNetUtil.regexApiUrlWithPathParam(getApiUrl(), getPathMap()), getHeaderMap(), paramPartList);

                e.onNext(call);
                e.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .retry(retryCount)
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

                                //realResponse = CCDefaultResponseBodyConvert.<T>convertResponse(retrofitResponse.body(), responseBeanType);
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
        return CCHttpMethod.POST;
    }

    @Override
    public int getCacheQueryMode() {
        return CCCMode.QueryMode.MODE_NET;
    }

    @Override
    public int getCacheSaveMode() {
        return CCCMode.SaveMode.MODE_NONE;
    }

    @Deprecated
    @Override
    public Map<String, Object> getParamMap() {
        return txtParamMap;
    }

    @Deprecated
    @Override
    public CCUploadRequest<T> setParamMap(Map<String, Object> paramMap) {
        this.txtParamMap = paramMap;
        return super.setParamMap(paramMap);
    }

    /*public CCUploadProgressCallback getCcUploadProgressCallback() {
        return ccUploadProgressCallback;
    }

    public CCUploadRequest<T> setCcUploadProgressCallback(CCUploadProgressCallback ccUploadProgressCallback) {
        this.ccUploadProgressCallback = ccUploadProgressCallback;
        return this;
    }*/

    public Map<String, Object> getTxtParamMap() {
        return txtParamMap;
    }

    public CCUploadRequest<T> setTxtParamMap(Map<String, Object> txtParamMap) {
        this.txtParamMap = txtParamMap;
        return this;
    }

    public Map<String, CCFile> getFileParamMap() {
        return fileParamMap;
    }

    public CCUploadRequest<T> setFileParamMap(Map<String, CCFile> fileParamMap) {
        this.fileParamMap = fileParamMap;
        return this;
    }
}
