package com.codingcoderscode.evolving.net.request;

import com.codingcoderscode.evolving.net.CCRxNetManager;
import com.codingcoderscode.evolving.net.cache.mode.CCCacheMode;
import com.codingcoderscode.evolving.net.request.base.CCRequest;
import com.codingcoderscode.evolving.net.request.callback.CCNetCallback;
import com.codingcoderscode.evolving.net.request.canceler.CCCanceler;
import com.codingcoderscode.evolving.net.request.comparator.CCDownloadTaskComparator;
import com.codingcoderscode.evolving.net.request.entity.CCDownloadTask;
import com.codingcoderscode.evolving.net.request.exception.NoEnoughSpaceException;
import com.codingcoderscode.evolving.net.request.method.CCHttpMethod;
import com.codingcoderscode.evolving.net.request.wrapper.CCDownloadRequestWrapper;
import com.codingcoderscode.evolving.net.response.CCBaseResponse;
import com.codingcoderscode.evolving.net.util.NetLogUtil;
import com.codingcoderscode.evolving.net.util.SDCardUtil;

import org.reactivestreams.Publisher;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by CodingCodersCode on 2017/11/13.
 * <p>
 * 多线程下载(单个文件只对应一个线程)
 */

public class CCMultiDownladRequest<T> extends CCRequest<T, CCMultiDownladRequest<T>> {

    private final String LOG_TAG = getClass().getCanonicalName();

    //处于等待下载(WAIT)状态的任务集合
    private final ConcurrentSkipListMap<CCDownloadTask, CCDownloadRequestWrapper> taskWaiting;
    //处于正在下载(DOWNLOADING)状态的任务集合
    private final ConcurrentSkipListMap<CCDownloadTask, CCDownloadRequestWrapper> taskDownloading;
    //处于暂停下载(PAUSE)状态的任务集合
    private final ConcurrentSkipListMap<CCDownloadTask, CCDownloadRequestWrapper> taskPaused;

    //同时存在的最大任务数量
    private int maxTaskCount;
    //已经存在的任务数量
    private AtomicInteger existTaskCount;
    //下载状态回调
    private CCNetCallback ccNetCallback;
    //最大重试次数
    private final int DEFAULT_RETRY_COUNT = 3;

    private boolean requestInPriority = true;

    /**
     * 请求构造器
     *
     * @param url
     */
    public CCMultiDownladRequest(String url) {
        this.apiUrl = url;

        this.taskWaiting = new ConcurrentSkipListMap<CCDownloadTask, CCDownloadRequestWrapper>(CCDownloadTaskComparator.getInstance().innerComparator);

        this.taskDownloading = new ConcurrentSkipListMap<CCDownloadTask, CCDownloadRequestWrapper>(CCDownloadTaskComparator.getInstance().innerComparator);

        this.taskPaused = new ConcurrentSkipListMap<CCDownloadTask, CCDownloadRequestWrapper>(CCDownloadTaskComparator.getInstance().innerComparator);

        this.existTaskCount = new AtomicInteger(0);
    }

    /**
     * 创建请求Flowable对象
     *
     * @return
     */
    @Override
    protected Flowable<CCBaseResponse<T>> getRequestFlowable() {

        return Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {

                while (true) {

                    if (!isRequestRunning()) {
                        break;
                    }

                    if (existTaskCount.intValue() < maxTaskCount) {
                        if (taskWaiting.size() > 0) {
                            e.onNext(existTaskCount.getAndIncrement());
                            NetLogUtil.printLog("e", LOG_TAG, "启动一个下载任务，当前existTaskCount=" + existTaskCount.intValue());
                        } else {
                            NetLogUtil.printLog("e", LOG_TAG, "没有可下载任务，当前existTaskCount=" + existTaskCount.intValue() + "，休眠3秒");
                            Thread.sleep(3000);
                        }
                    } else {
                        NetLogUtil.printLog("e", LOG_TAG, "当前existTaskCount=" + existTaskCount.intValue() + "，休眠3秒");
                        Thread.sleep(3000);
                    }
                }

                e.onComplete();

            }
        }, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.computation())
                .unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(new Function<Integer, Publisher<CCBaseResponse<T>>>() {
                    @Override
                    public Publisher<CCBaseResponse<T>> apply(Integer integer) throws Exception {

                        onCreateAndStartTask();

                        return Flowable.just(new CCBaseResponse<T>(null, null, false, false, false));
                    }
                });
    }

    /**
     * 创建实际下载请求
     *
     * @throws Exception
     */
    private void onCreateAndStartTask() throws Exception {
        Map.Entry<CCDownloadTask, CCDownloadRequestWrapper> toDownloadTaskEntry = null;
        CCDownloadTask toDownloadTask = null;
        CCDownloadRequestWrapper toDownloadTaskWrapper = null;
        CCDownloadRequest downloadRequest = null;
        try {
            if ((toDownloadTaskEntry = taskWaiting.pollFirstEntry()) != null) {

                toDownloadTask = toDownloadTaskEntry.getKey();
                toDownloadTaskWrapper = toDownloadTaskEntry.getValue();

                if (toDownloadTask == null) {
                    existTaskCount.getAndDecrement();
                    return;
                }

                if (toDownloadTask.getFileSize() > SDCardUtil.getSDCardAvailableSize() * 1024 * 1024) {

                    pause(toDownloadTask, toDownloadTaskWrapper);

                    existTaskCount.getAndDecrement();

                    NoEnoughSpaceException noEnoughSpaceException = new NoEnoughSpaceException("write failed: ENOSPC (No space left on device)");

                    if (ccNetCallback != null) {
                        ccNetCallback.onError(toDownloadTask, noEnoughSpaceException);
                    }

                    return;
                }

                if (toDownloadTaskWrapper != null) {

                    if (toDownloadTaskWrapper.getRequest() != null) {
                        toDownloadTaskWrapper.getRequest().executeAsync();
                    } else {
                        downloadRequest = CCRxNetManager.<T>download(toDownloadTask.getSourceUrl())
                                .setFileSavePath(toDownloadTask.getSavePath())
                                .setFileSaveName(toDownloadTask.getSaveName())
                                .setRetryCount(DEFAULT_RETRY_COUNT)
                                .setCacheQueryMode(CCCacheMode.QueryMode.MODE_ONLY_NET)
                                .setCacheSaveMode(CCCacheMode.SaveMode.MODE_NO_CACHE)
                                .setReqTag(toDownloadTask)
                                .setSupportRage(true)
                                .setCCNetCallback(new MultiDownloadNetCallback<T>(this))
                                .setNetLifecycleComposer(getNetLifecycleComposer())
                                .setResponseBeanType(Void.class);

                        downloadRequest.executeAsync();
                        toDownloadTaskWrapper.setRequest(downloadRequest);
                    }

                } else {
                    downloadRequest = CCRxNetManager.<T>download(toDownloadTask.getSourceUrl())
                            .setFileSavePath(toDownloadTask.getSavePath())
                            .setFileSaveName(toDownloadTask.getSaveName())
                            .setRetryCount(DEFAULT_RETRY_COUNT)
                            .setCacheQueryMode(CCCacheMode.QueryMode.MODE_ONLY_NET)
                            .setCacheSaveMode(CCCacheMode.SaveMode.MODE_NO_CACHE)
                            .setReqTag(toDownloadTask)
                            .setSupportRage(true)
                            .setCCNetCallback(new MultiDownloadNetCallback<T>(this))
                            .setNetLifecycleComposer(getNetLifecycleComposer())
                            .setResponseBeanType(Void.class);

                    downloadRequest.executeAsync();

                    toDownloadTaskWrapper = new CCDownloadRequestWrapper(downloadRequest);
                }

                taskDownloading.put(toDownloadTask, toDownloadTaskWrapper);

            } else {
                existTaskCount.getAndDecrement();
            }
        } catch (Exception e) {

            pause(toDownloadTask, toDownloadTaskWrapper);

            this.existTaskCount.getAndDecrement();

            if (ccNetCallback != null) {
                ccNetCallback.onError(toDownloadTask, e);
            }

        }
    }

    @Override
    public void cancel() {
        pauseAll();
        super.cancel();

    }

    /**
     * 重置所有的暂停[PAUSED]状态任务为待下载[WAIT]
     */
    private void resetPausedToWait() {
        Map.Entry<CCDownloadTask, CCDownloadRequestWrapper> pausedTask;
        while ((pausedTask = taskPaused.pollFirstEntry()) != null) {
            taskWaiting.put(pausedTask.getKey(), pausedTask.getValue());
        }
    }


    /////////////////////////////////////
    //  开始下载
    /////////////////////////////////////

    /**
     * 开始所有下载任务
     */
    public synchronized void startAll() {
        resetPausedToWait();
        if (!isRequestRunning()) {
            executeAsync();
        }
    }

    /**
     * 开始列表指定的所有下载任务
     *
     * @param taskList 指定的任务列表
     */
    public synchronized void startAll(List<CCDownloadTask> taskList) {

        if (taskList != null && taskList.size() > 0) {

            Collections.sort(taskList, CCDownloadTaskComparator.getInstance().innerComparator);

            for (int i = 0; i < taskList.size(); i++) {
                start(taskList.get(i));
            }
        }
    }

    /**
     * 开始指定的下载任务
     *
     * @param task 指定的下载任务
     */
    public synchronized void start(CCDownloadTask task) {
        if (task != null) {
            if (taskPaused.containsKey(task)) {
                start(task, taskPaused.remove(task));
            } else if (taskWaiting.containsKey(task)) {

            } else {
                start(task, new CCDownloadRequestWrapper(null));
            }
        }
    }

    /**
     * 开始指定的下载任务
     *
     * @param task           指定的下载任务
     * @param requestWrapper 下载任务所对应的请求
     */
    private synchronized void start(CCDownloadTask task, CCDownloadRequestWrapper requestWrapper) {
        if (task == null) {
            return;
        }

        if (taskWaiting.containsKey(task)) {
            return;
        }

        if (taskDownloading.containsKey(task)) {
            return;
        }

        if (requestWrapper == null) {
            requestWrapper = new CCDownloadRequestWrapper(null);
        }

        this.taskWaiting.put(task, requestWrapper);

        if (!isRequestRunning()) {
            executeAsync();
        }
    }

    /////////////////////////////////////////////
    //          暂停
    /////////////////////////////////////////////

    /**
     * 暂停所有下载任务
     */
    public synchronized void pauseAll() {

        Map.Entry<CCDownloadTask, CCDownloadRequestWrapper> toPauseTask;

        if (taskWaiting.size() > 0) {
            synchronized (taskWaiting) {
                while ((toPauseTask = taskWaiting.pollFirstEntry()) != null) {
                    pause(toPauseTask);
                }
            }
        }

        if (taskDownloading.size() > 0) {
            synchronized (taskDownloading) {
                while ((toPauseTask = taskDownloading.pollFirstEntry()) != null) {
                    existTaskCount.getAndDecrement();
                    pause(toPauseTask);
                }
            }
        }
    }

    /**
     * 暂停列表指定的所有下载任务
     *
     * @param taskList 指定的暂停任务列表
     */
    public synchronized void pauseAll(List<CCDownloadTask> taskList) {
        if (taskList != null && taskList.size() > 0) {
            for (int i = 0; i < taskList.size(); i++) {
                pause(taskList.get(i));
            }
        }
    }

    /**
     * 暂停指定的下载任务
     *
     * @param task 指定的下载任务
     */
    public synchronized void pause(CCDownloadTask task) {

        if (taskWaiting.containsKey(task)) {
            pause(task, taskWaiting.remove(task));
            return;
        }

        if (taskDownloading.containsKey(task)) {
            existTaskCount.getAndDecrement();
            pause(task, taskDownloading.remove(task));
        }
    }

    /**
     * 暂停指定的下载任务
     *
     * @param taskEntry 指定的下载任务
     */
    private synchronized void pause(Map.Entry<CCDownloadTask, CCDownloadRequestWrapper> taskEntry) {
        if (taskEntry != null) {
            pause(taskEntry.getKey(), taskEntry.getValue());
        }
    }

    /**
     * 暂停指定的下载任务
     *
     * @param task           指定的下载任务
     * @param requestWrapper 指定下载任务的请求
     */
    private synchronized void pause(CCDownloadTask task, CCDownloadRequestWrapper requestWrapper) {
        if (task == null) {
            return;
        }

        if (requestWrapper == null) {
            requestWrapper = new CCDownloadRequestWrapper(null);
        }

        if (requestWrapper.getRequest() != null) {
            requestWrapper.getRequest().cancel();
        }

        if (taskPaused.containsKey(task)) {
            return;
        }

        this.taskPaused.put(task, requestWrapper);
    }

    ///////////////////////////
    //  继续下载
    //////////////////////////

    /**
     * 继续下载所有任务
     */
    public synchronized void resumeAll() {
        resetPausedToWait();

        if (!isRequestRunning()) {
            executeAsync();
        }
    }

    /**
     * 继续下载列表所指定的所有任务
     *
     * @param taskList 指定的继续下载的任务列表
     */
    public synchronized void resumeAll(List<CCDownloadTask> taskList) {

        if (taskList != null && taskList.size() > 0) {

            int taskSize = taskList.size();

            for (int i = 0; i < taskSize; i++) {
                resume(taskList.get(i));
            }
        }
    }

    /**
     * 继续下载指定的任务
     *
     * @param task 指定的任务
     */
    public synchronized void resume(CCDownloadTask task) {

        CCDownloadRequestWrapper requestWrapper;

        if (task != null) {
            if (taskPaused.containsKey(task)) {
                requestWrapper = taskPaused.remove(task);
                taskWaiting.put(task, requestWrapper);
            }

            if (!isRequestRunning()) {
                executeAsync();
            }
        }
    }

    @Override
    protected int getHttpMethod() {
        return CCHttpMethod.GET;
    }

    @Override
    public int getCacheQueryMode() {
        return CCCacheMode.QueryMode.MODE_ONLY_NET;
    }

    @Override
    public int getCacheSaveMode() {
        return CCCacheMode.SaveMode.MODE_NO_CACHE;
    }

    private static class MultiDownloadNetCallback<M> extends CCNetCallback {

        private CCMultiDownladRequest<M> multiDownladRequest;

        public MultiDownloadNetCallback(CCMultiDownladRequest<M> multiDownladRequest) {
            this.multiDownladRequest = multiDownladRequest;
        }

        @Override
        public <T> void onStartRequest(Object reqTag, CCCanceler canceler) {
            multiDownladRequest.ccNetCallback.onStartRequest(reqTag, canceler);
        }

        @Override
        public <T> void onSuccess(Object reqTag, T response) {
            if (reqTag != null && reqTag instanceof CCDownloadTask) {
                CCDownloadTask task = (CCDownloadTask) reqTag;
                multiDownladRequest.taskDownloading.remove(task);
                multiDownladRequest.existTaskCount.getAndDecrement();
            }

            if (multiDownladRequest.ccNetCallback != null) {
                multiDownladRequest.ccNetCallback.onSuccess(reqTag, response);
            }
        }

        @Override
        public <T> void onError(Object reqTag, Throwable t) {
            if (reqTag != null && reqTag instanceof CCDownloadTask) {
                CCDownloadTask task = (CCDownloadTask) reqTag;
                CCDownloadRequestWrapper requestWrapper = multiDownladRequest.taskDownloading.remove(task);

                if (requestWrapper.getRequest() != null) {
                    requestWrapper.getRequest().cancel();
                }

                multiDownladRequest.taskPaused.put(task, requestWrapper);
                multiDownladRequest.existTaskCount.getAndDecrement();
            }

            if (multiDownladRequest.ccNetCallback != null) {
                multiDownladRequest.ccNetCallback.onError(reqTag, t);
            }
        }

        @Override
        public <T> void onComplete(Object reqTag) {
            if (multiDownladRequest.ccNetCallback != null) {
                multiDownladRequest.ccNetCallback.onComplete(reqTag);
            }
        }

        @Override
        public <T> void onProgress(Object reqTag, int progress, long netSpeed, long completedSize, long fileSize) {
            if (multiDownladRequest.ccNetCallback != null) {
                multiDownladRequest.ccNetCallback.onProgress(reqTag, progress, netSpeed, completedSize, fileSize);
            }
        }

        @Override
        public <T> void onProgressSave(Object reqTag, int progress, long netSpeed, long completedSize, long fileSize) {
            if (multiDownladRequest.ccNetCallback != null) {
                multiDownladRequest.ccNetCallback.onProgressSave(reqTag, progress, netSpeed, completedSize, fileSize);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public CCMultiDownladRequest<T> setCCNetCallback(CCNetCallback ccNetCallback) {
        this.ccNetCallback = ccNetCallback;
        return this;
    }

    public CCNetCallback getCcNetCallback() {
        return ccNetCallback;
    }

    public int getMaxTaskCount() {
        return maxTaskCount;
    }

    public CCMultiDownladRequest<T> setMaxTaskCount(int maxTaskCount) {
        this.maxTaskCount = maxTaskCount;
        return this;
    }

}
