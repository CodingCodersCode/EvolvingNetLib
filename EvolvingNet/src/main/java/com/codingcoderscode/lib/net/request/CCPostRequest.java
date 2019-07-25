package com.codingcoderscode.lib.net.request;

import com.codingcoderscode.lib.net.request.api.CCNetApiService;
import com.codingcoderscode.lib.net.request.base.CCSimpleRequest;
import com.codingcoderscode.lib.net.request.method.CCHttpMethod;
import com.codingcoderscode.lib.net.util.CCNetUtil;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by CodingCodersCode on 2017/10/26.
 * <p>
 * POST类型请求类
 */

public class CCPostRequest<T> extends CCSimpleRequest<T> {

    public CCPostRequest(String url, CCNetApiService apiService) {
        super(url, apiService);
    }

    @Override
    protected int getHttpMethod() {
        return CCHttpMethod.POST;
    }

    @Override
    protected Call<ResponseBody> getRequestCall() {
        Call<ResponseBody> call;

        if (isUseBodyParamStyle()) {
            call = getCCNetApiService().executeBodyPost(CCNetUtil.regexApiUrlWithPathParam(getApiUrl(), getPathMap()), getHeaderMap(), getRequestParam());
        } else {
            call = getCCNetApiService().executePost(CCNetUtil.regexApiUrlWithPathParam(getApiUrl(), getPathMap()), getHeaderMap(), getRequestParam());
        }

        return call;
    }
}
