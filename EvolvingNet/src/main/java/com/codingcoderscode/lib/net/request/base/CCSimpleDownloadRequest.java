package com.codingcoderscode.lib.net.request.base;

import com.codingcoderscode.lib.net.cache.mode.CCMode;
import com.codingcoderscode.lib.net.request.api.CCNetApiService;
import com.codingcoderscode.lib.net.request.method.CCHttpMethod;
import com.codingcoderscode.lib.net.response.CCBaseResponse;

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
        return CCMode.QueryMode.MODE_NET;
    }

    @Override
    public int getCacheSaveMode() {
        return CCMode.SaveMode.MODE_NONE;
    }
}
