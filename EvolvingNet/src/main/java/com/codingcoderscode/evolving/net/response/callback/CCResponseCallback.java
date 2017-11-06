package com.codingcoderscode.evolving.net.response.callback;


import com.codingcoderscode.evolving.net.request.canceler.CCCanceler;

/**
 * Created by ghc on 2017/10/27.
 */
@Deprecated
public abstract class CCResponseCallback {

    /**
     * you can call the method canceler.cancel() to cancel the request when you need.
     * @param canceler
     */
    public <T> void onStartRequest(Object reqTag, CCCanceler canceler){}

    public <T> void onMemoryCacheQuerySuccess(Object reqTag, T response){}

    public <T> void onDiskCacheQuerySuccess(Object reqTag, T response){}

    public <T> void onCacheQuerySuccess(Object reqTag, T response){}

    public <T> void onNetSuccess(Object reqTag, T response){}

    public <T> void onSuccess(Object reqTag, T response){}

    public <T> void onError(Object reqTag, Throwable t){}

    public <T> void onComplete(Object reqTag){}
}
