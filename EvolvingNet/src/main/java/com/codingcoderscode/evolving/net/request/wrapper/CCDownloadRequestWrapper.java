package com.codingcoderscode.evolving.net.request.wrapper;

import com.codingcoderscode.evolving.net.request.base.CCSimpleDownloadRequest;

/**
 * Created by CodingCodersCode on 2017/11/13.
 */

public class CCDownloadRequestWrapper {

    private CCSimpleDownloadRequest request;

    public CCDownloadRequestWrapper(CCSimpleDownloadRequest request) {
        this.request = request;
    }

    public CCSimpleDownloadRequest getRequest() {
        return request;
    }

    public void setRequest(CCSimpleDownloadRequest request) {
        this.request = request;
    }

}
