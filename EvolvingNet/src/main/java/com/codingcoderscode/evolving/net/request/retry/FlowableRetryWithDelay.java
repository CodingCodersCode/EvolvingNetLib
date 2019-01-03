package com.codingcoderscode.evolving.net.request.retry;

import com.codingcoderscode.evolving.net.request.exception.CCUnExpectedException;
import com.codingcoderscode.evolving.net.util.NetLogUtil;

import org.reactivestreams.Publisher;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by CodingCodersCode on 2017/11/1.
 * <p>
 * 网络请求失败重试逻辑处理类
 */

public class FlowableRetryWithDelay implements Function<Flowable<? extends Throwable>, Flowable<?>> {

    private final String LOG_TAG = getClass().getCanonicalName();

    private final int maxRetries;
    private final int retryDelayMillis;
    private int retryCount;

    public FlowableRetryWithDelay(int maxRetries, int retryDelayMillis) {
        this.maxRetries = maxRetries;
        this.retryDelayMillis = retryDelayMillis;
        this.retryCount = 0;
    }

    @Override
    public Flowable<?> apply(Flowable<? extends Throwable> flowable) throws Exception {
        return flowable.flatMap(new Function<Throwable, Publisher<?>>() {
            @Override
            public Publisher<?> apply(Throwable throwable) throws Exception {
                try {
                    if (throwable.getMessage().equals("write failed: ENOSPC (No space left on device)")) {
                        //NetLogUtil.printLog("e", LOG_TAG, "磁盘空间不足，不发起重试请求", throwable);
                        return Flowable.error(throwable);
                    } else if (throwable instanceof IOException) {
                        retryCount += 1;
                        if (retryCount <= maxRetries) {
                            NetLogUtil.printLog("e", LOG_TAG, "第" + retryCount + "次失败重试,在" + retryDelayMillis + "毫秒后开始", throwable);
                            return Flowable.timer(retryDelayMillis, TimeUnit.MILLISECONDS, Schedulers.trampoline());
                        } else {
                            NetLogUtil.printLog("e", LOG_TAG, "重试次数已用尽", throwable);
                            return Flowable.error(throwable);
                        }
                    } else {
                        NetLogUtil.printLog("e", LOG_TAG, "不是IOException，即不是网络异常，不发起重试", throwable);
                        return Flowable.error(throwable);
                    }
                } catch (Exception e) {
                    throw e;
                }
            }
        });
    }
}
