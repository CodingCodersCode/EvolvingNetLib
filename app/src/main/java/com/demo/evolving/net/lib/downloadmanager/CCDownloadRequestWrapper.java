package com.demo.evolving.net.lib.downloadmanager;

import com.codingcoderscode.lib.net.request.CCDownloadRequest;

/**
 * Created by CodingCodersCode on 2017/11/8.
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
