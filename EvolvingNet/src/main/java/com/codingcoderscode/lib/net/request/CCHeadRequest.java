package com.codingcoderscode.lib.net.request;


import com.codingcoderscode.lib.net.request.api.CCNetApiService;
import com.codingcoderscode.lib.net.request.base.CCSimpleRequest;
import com.codingcoderscode.lib.net.request.method.CCHttpMethod;
import com.codingcoderscode.lib.net.util.CCNetUtil;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by CodingCodersCode on 2017/10/30.
 * <p>
 * HEAD类型请求类
 */

public class CCHeadRequest<T> extends CCSimpleRequest<T> {

    public CCHeadRequest(String url, CCNetApiService apiService) {
        super(url, apiService);
    }

    @Override
    protected int getHttpMethod() {
        return CCHttpMethod.HEAD;
    }

    @Override
    protected Call<ResponseBody> getRequestCall() {
        Call<ResponseBody> call;

        call = getCCNetApiService().executeHead(CCNetUtil.regexApiUrlWithPathParam(getApiUrl(), getPathMap()), getHeaderMap(), getRequestParam());

        return call;
    }
}
