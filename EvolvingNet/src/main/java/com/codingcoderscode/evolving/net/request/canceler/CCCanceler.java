package com.codingcoderscode.evolving.net.request.canceler;

import com.codingcoderscode.evolving.net.request.base.CCRequest;

import org.reactivestreams.Subscription;

/**
 * Created by ghc on 2017/10/27.
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
