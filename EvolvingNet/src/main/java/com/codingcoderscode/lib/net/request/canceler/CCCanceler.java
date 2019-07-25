package com.codingcoderscode.lib.net.request.canceler;

import com.codingcoderscode.lib.net.request.base.CCRequest;

/**
 * Created by CodingCodersCode on 2017/10/27.
 */

public class CCCanceler {

    /**
     * {@link org.reactivestreams.Subscription}
     */
    private CCRequest ccRequest;

    public CCCanceler(CCRequest ccRequest) {
        this.ccRequest = ccRequest;
    }

    public void cancel(){
        if (ccRequest != null){
            ccRequest.cancel();
            ccRequest = null;
        }
    }
}
