package com.codingcoderscode.evolving.net.request.entity;

/**
 * Created by ghc on 2017/10/17.
 */

public class CCFile {

    private String url;
    private String mimeType;

    public CCFile(String url){
        this.url = url;
        this.mimeType = "multipart/form-data;";
    }

    public CCFile(String url, String mimeType) {
        this.url = url;
        this.mimeType = mimeType;
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
}
