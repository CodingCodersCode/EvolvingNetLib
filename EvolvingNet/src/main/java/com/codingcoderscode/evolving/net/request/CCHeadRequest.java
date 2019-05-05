package com.codingcoderscode.evolving.net.request;


import com.codingcoderscode.evolving.net.request.api.CCNetApiService;
import com.codingcoderscode.evolving.net.request.base.CCRequest;
import com.codingcoderscode.evolving.net.request.base.CCSimpleRequest;
import com.codingcoderscode.evolving.net.request.method.CCHttpMethod;
import com.codingcoderscode.evolving.net.util.CCNetUtil;

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
