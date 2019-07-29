package com.codingcoderscode.lib.net.request.entity;


import com.codingcoderscode.lib.net.util.CCFileUtils;

import okhttp3.RequestBody;

/**
 * Created by CodingCodersCode on 2017/10/17.
 */

public class CCFile {

    private String url;
    private String mimeType;
    /**
     * 详见{@link okhttp3.MultipartBody.Part#createFormData(String, String, RequestBody)}方法第二个参数
     */
    private String fileName;

    public CCFile(String url) {
        this.url = url;
        this.mimeType = CCFileUtils.getMimeType(url);
        this.fileName = null;
    }

    public CCFile(String url, String mimeType) {
        this.url = url;
        this.mimeType = mimeType;
        this.fileName = null;
    }

    public CCFile(String url, String mimeType, String fileName) {
        this.url = url;
        this.mimeType = mimeType;
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
