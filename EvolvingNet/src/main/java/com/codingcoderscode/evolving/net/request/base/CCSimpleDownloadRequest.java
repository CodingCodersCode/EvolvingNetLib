package com.codingcoderscode.evolving.net.request.base;

import com.codingcoderscode.evolving.net.cache.mode.CCCMode;
import com.codingcoderscode.evolving.net.request.api.CCNetApiService;
import com.codingcoderscode.evolving.net.request.method.CCHttpMethod;
import com.codingcoderscode.evolving.net.response.CCBaseResponse;

import io.reactivex.Flowable;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Date：2019/4/30 17:03
 * <p>
 * author: CodingCodersCode
 */
public abstract class CCSimpleDownloadRequest<T> extends CCRequest<T, CCSimpleDownloadRequest<T>> {

    public CCSimpleDownloadRequest(String url, CCNetApiService apiService) {
        super(url, apiService);
    }

    @Override
    protected int getHttpMethod() {
        return CCHttpMethod.GET;
    }

    @Override
    protected Call<ResponseBody> getRequestCall() {
        return null;
    }

    @Override
    protected void onSaveToCache(CCBaseResponse<T> tccBaseResponse) {

    }

    @Override
    protected Flowable<CCBaseResponse<T>> getDiskQueryFlowable() {
        return null;
    }

    @Override
    public int getCacheQueryMode() {
        return CCCMode.QueryMode.MODE_NET;
    }

    @Override
    public int getCacheSaveMode() {
        return CCCMode.SaveMode.MODE_NONE;
    }
}
