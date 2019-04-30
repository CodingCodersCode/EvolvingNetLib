package com.codingcoderscode.evolving.net.request.base;

import com.codingcoderscode.evolving.net.request.api.CCNetApiService;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Date：2019/4/30 13:50
 * <p>
 * author: ghc
 */
public class CCSimpleRequest<T> extends CCRequest<T, CCSimpleRequest<T>> {
    public CCSimpleRequest(String url, CCNetApiService apiService) {
        super(url, apiService);
    }

    @Override
    protected int getHttpMethod() {
        return 0;
    }

    @Override
    protected Call<ResponseBody> getRequestCall() {
        return null;
    }
}
