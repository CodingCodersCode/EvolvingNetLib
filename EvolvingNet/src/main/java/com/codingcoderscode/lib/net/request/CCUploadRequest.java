package com.codingcoderscode.lib.net.request;

import android.text.TextUtils;

import com.codingcoderscode.lib.net.cache.mode.CCMode;
import com.codingcoderscode.lib.net.request.api.CCNetApiService;
import com.codingcoderscode.lib.net.request.base.CCSimpleRequest;
import com.codingcoderscode.lib.net.request.entity.CCFile;
import com.codingcoderscode.lib.net.request.method.CCHttpMethod;
import com.codingcoderscode.lib.net.request.requestbody.CCSimpleUploadRequestBody;
import com.codingcoderscode.lib.net.util.CCNetUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by CodingCodersCode on 2017/10/31.
 * <p>
 * 上传文件类型请求类
 */

public class CCUploadRequest<T> extends CCSimpleRequest<T> {

    private Map<String, Object> mTxtRequestParam;

    private Map<String, CCFile> mFileRequestParam;

    public CCUploadRequest(String url, CCNetApiService apiService) {
        super(url, apiService);
    }

    @Override
    protected int getHttpMethod() {
        return CCHttpMethod.POST;
    }

    @Override
    protected Call<ResponseBody> getRequestCall() {
        RequestBody requestBody;
        ArrayList<MultipartBody.Part> paramPartList = new ArrayList<>();
        CCFile fileValue;
        MultipartBody.Part partBody;
        File uploadFile;
        String fileNameInMultiBodyPart;
        try {
            if (mTxtRequestParam != null) {
                for (Map.Entry<String, ?> entry : mTxtRequestParam.entrySet()) {

                    partBody = MultipartBody.Part.createFormData(entry.getKey(), entry.getValue().toString());

                    paramPartList.add(partBody);

                }
            }

            if (mFileRequestParam != null) {
                for (Map.Entry<String, CCFile> entry : mFileRequestParam.entrySet()) {

                    fileValue = entry.getValue();

                    if (fileValue == null || TextUtils.isEmpty(fileValue.getUrl())) {
                        continue;
                    }

                    uploadFile = new File(fileValue.getUrl());

                    fileNameInMultiBodyPart = fileValue.getFileName();
                    if (fileNameInMultiBodyPart == null || fileNameInMultiBodyPart.equals("")){
                        fileNameInMultiBodyPart = uploadFile.getName();
                    }

                    requestBody = new CCSimpleUploadRequestBody(entry.getKey(), MediaType.parse(fileValue.getMimeType()), uploadFile, getCCNetResultListener());

                    partBody = MultipartBody.Part.createFormData(entry.getKey(), fileNameInMultiBodyPart, requestBody);

                    paramPartList.add(partBody);

                }
            }
        } catch (Exception exception) {

        }

        Call<ResponseBody> call;
        call = getCCNetApiService().executeUpload(CCNetUtil.regexApiUrlWithPathParam(getApiUrl(), getPathMap()), getHeaderMap(), paramPartList);

        return call;
    }

    @Override
    public int getCacheQueryMode() {
        return CCMode.QueryMode.MODE_NET;
    }

    @Override
    public int getCacheSaveMode() {
        return CCMode.SaveMode.MODE_NONE;
    }

    @Deprecated
    @Override
    public Map<String, Object> getRequestParam() {
        return mTxtRequestParam;
    }

    @Deprecated
    @Override
    public CCUploadRequest<T> setRequestParam(Map<String, Object> requestParam) {
        super.setRequestParam(requestParam);
        this.mTxtRequestParam = requestParam;
        return this;
    }

    public Map<String, Object> getTxtRequestParam() {
        return mTxtRequestParam;
    }

    public CCUploadRequest<T> setTxtRequestParam(Map<String, Object> txtRequestParam) {
        this.mTxtRequestParam = txtRequestParam;
        return this;
    }

    public Map<String, CCFile> getFileRequestParam() {
        return mFileRequestParam;
    }

    public CCUploadRequest<T> setFileRequestParam(Map<String, CCFile> fileRequestParam) {
        this.mFileRequestParam = fileRequestParam;
        return this;
    }
}
