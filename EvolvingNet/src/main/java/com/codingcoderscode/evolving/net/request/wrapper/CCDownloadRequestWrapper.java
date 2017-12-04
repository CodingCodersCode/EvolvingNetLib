package com.codingcoderscode.evolving.net.request.wrapper;

import com.codingcoderscode.evolving.net.request.CCDownloadRequest;

/**
 * Created by CodingCodersCode on 2017/11/13.
 */

public class CCDownloadRequestWrapper {

    private CCDownloadRequest request;

    public CCDownloadRequestWrapper(CCDownloadRequest request) {
        this.request = request;
    }

    public CCDownloadRequest getRequest() {
        return request;
    }

    public void setRequest(CCDownloadRequest request) {
        this.request = request;
    }

}
