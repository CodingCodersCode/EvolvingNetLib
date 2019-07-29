package com.codingcoderscode.lib.net.request.entity;


import android.text.TextUtils;

import java.net.FileNameMap;
import java.net.URLConnection;

import okhttp3.MediaType;
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

    public CCFile(String url){
        this.url = url;

        if (TextUtils.isEmpty(url)){
            this.mimeType = "multipart/form-data;";
        }else {
            FileNameMap fileNameMap = URLConnection.getFileNameMap();
            //url = url.replace("#", "");   //解决文件名中含有#号异常的问题
            String contentType = fileNameMap.getContentTypeFor(url.replace("#", ""));
            if (contentType == null) {
                this.mimeType =  MediaType.parse("application/octet-stream").toString();
            }
            this.mimeType =   MediaType.parse(contentType).toString();
        }

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
