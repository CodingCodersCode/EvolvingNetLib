package com.codingcoderscode.evolving.net.request;

import com.codingcoderscode.evolving.net.cache.mode.CCCMode;
import com.codingcoderscode.evolving.net.request.api.CCNetApiService;
import com.codingcoderscode.evolving.net.request.base.CCRequest;
import com.codingcoderscode.evolving.net.request.callback.CCNetResultListener;
import com.codingcoderscode.evolving.net.request.canceler.CCCanceler;
import com.codingcoderscode.evolving.net.request.comparator.CCDownloadTaskComparator;
import com.codingcoderscode.evolving.net.request.entity.CCDownloadTask;
import com.codingcoderscode.evolving.net.request.method.CCHttpMethod;
import com.codingcoderscode.evolving.net.request.wrapper.CCDownloadRequestWrapper;
import com.codingcoderscode.evolving.net.response.CCBaseResponse;

import org.reactivestreams.Publisher;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Flowable;
import io.reactivex.functions.BooleanSupplier;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Date：2019/4/28 15:59
 * <p>
 * author: CodingCodersCode
 * <p>
 * 多文件下载
 */
public class CCMultiDownloadRequest<T> extends CCRequest<T, CCMultiDownloadRequest<T>> implements CCNetResultListener {
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
    private CCNetResultListener mNetResultListener;
    //最大重试次数
    private final int DEFAULT_RETRY_COUNT = 3;

    private boolean requestInPriority = true;

    public CCMultiDownloadRequest(String url, CCNetApiService apiService) {
        super(url, apiService);

        this.taskWaiting = new ConcurrentSkipListMap<CCDownloadTask, CCDownloadRequestWrapper>(CCDownloadTaskComparator.getInstance().innerComparator);

        this.taskDownloading = new ConcurrentSkipListMap<CCDownloadTask, CCDownloadRequestWrapper>(CCDownloadTaskComparator.getInstance().innerComparator);

        this.taskPaused = new ConcurrentSkipListMap<CCDownloadTask, CCDownloadRequestWrapper>(CCDownloadTaskComparator.getInstance().innerComparator);

        this.existTaskCount = new AtomicInteger(0);
    }

    @Override
    protected int getHttpMethod() {
        return CCHttpMethod.GET;
    }

    @Override
    protected Call<ResponseBody> getRequestCall() {
        return null;
    }

    @Override
    protected Flowable<CCBaseResponse<T>> getRequestFlowable() {
        return Flowable.intervalRange(0, 1, 0, 5 * 60 * 1000, TimeUnit.MILLISECONDS, Schedulers.computation())
                .repeatUntil(new BooleanSupplier() {
                    @Override
                    public boolean getAsBoolean() throws Exception {
                        return false;
                    }
                })
                //.subscribeOn(Schedulers.computation())
                //.unsubscribeOn(Schedulers.io())
                //.observeOn(Schedulers.io())
                .flatMap(new Function<Long, Publisher<CCBaseResponse<T>>>() {
                    @Override
                    public Publisher<CCBaseResponse<T>> apply(Long aLong) throws Exception {
                        if (onCheckRequest()) {
                            return Flowable.never();
                        } else {
                            return Flowable.empty();
                        }
                    }
                })
                .subscribeOn(Schedulers.computation());
    }

    /**
     * 检查下载任务
     *
     * @return true：存在下载中和等待下载的任务；false：无下载中和等待下载的任务
     */
    public synchronized boolean onCheckRequest() {
        //是否有正在下载的任务
        boolean hasDownloading;
        //是否有等待下载的任务
        boolean hasWaiting;
        //下载中任务是否达到最大数量限制
        boolean hasDownloadingQueueFull;
        try {
            while (true) {
                hasDownloading = taskDownloading.size() > 0;
                hasWaiting = taskWaiting.size() > 0;
                hasDownloadingQueueFull = hasDownloadingQueueFull();

                if (!hasDownloadingQueueFull) {
                    if (hasWaiting) {
                        onSelectOneTaskToDownload();
                        //return true;
                    } else if (hasDownloading) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 下载中队列是否满
     *
     * @return
     */
    public synchronized boolean hasDownloadingQueueFull() {
        if (existTaskCount.intValue() < this.maxTaskCount) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 选取一个待下载任务进行下载
     * <p>
     * you should call {@link #onCheckRequest} instead of calling this method directly
     */
    private void onSelectOneTaskToDownload() {
        Map.Entry<CCDownloadTask, CCDownloadRequestWrapper> toDownloadTaskEntry = null;
        CCDownloadTask toDownloadTask = null;
        CCDownloadRequestWrapper toDownloadTaskWrapper = null;
        CCDownloadRequest downloadRequest = null;
        try {
            existTaskCount.incrementAndGet();

            toDownloadTaskEntry = taskWaiting.pollFirstEntry();

            if (toDownloadTaskEntry != null) {

                toDownloadTask = toDownloadTaskEntry.getKey();
                toDownloadTaskWrapper = toDownloadTaskEntry.getValue();

                if (toDownloadTaskWrapper != null) {

                    if (toDownloadTaskWrapper.getRequest() != null) {
                        toDownloadTaskWrapper.getRequest().executeAsync();
                    } else {
                        downloadRequest = new CCDownloadRequest<T>(toDownloadTask.getSourceUrl(), getCCNetApiService())
                                .setFileSavePath(toDownloadTask.getSavePath())
                                .setFileSaveName(toDownloadTask.getSaveName())
                                .setRetryCount(DEFAULT_RETRY_COUNT)
                                .setCacheQueryMode(CCCMode.QueryMode.MODE_NET)
                                .setCacheSaveMode(CCCMode.SaveMode.MODE_NONE)
                                .setReqTag(toDownloadTask)
                                .setSupportRage(true)
                                .setCCNetCallback(this)
                                .setNetLifecycleComposer(getNetLifecycleComposer())
                                .setResponseBeanType(Void.class);

                        downloadRequest.executeAsync();
                        toDownloadTaskWrapper.setRequest(downloadRequest);
                    }

                } else {
                    downloadRequest = new CCDownloadRequest<T>(toDownloadTask.getSourceUrl(), getCCNetApiService())
                            .setFileSavePath(toDownloadTask.getSavePath())
                            .setFileSaveName(toDownloadTask.getSaveName())
                            .setRetryCount(DEFAULT_RETRY_COUNT)
                            .setCacheQueryMode(CCCMode.QueryMode.MODE_NET)
                            .setCacheSaveMode(CCCMode.SaveMode.MODE_NONE)
                            .setReqTag(toDownloadTask)
                            .setSupportRage(true)
                            .setCCNetCallback(this)
                            .setNetLifecycleComposer(getNetLifecycleComposer())
                            .setResponseBeanType(Void.class);

                    downloadRequest.executeAsync();

                    toDownloadTaskWrapper = new CCDownloadRequestWrapper(downloadRequest);
                }

                taskDownloading.put(toDownloadTask, toDownloadTaskWrapper);

            } else {
                existTaskCount.decrementAndGet();
            }
        } catch (Exception e) {
            this.existTaskCount.decrementAndGet();

            pause(toDownloadTask, toDownloadTaskWrapper);

            if (mNetResultListener != null) {
                mNetResultListener.onRequestFail(toDownloadTask, e);
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

    /**
     * 开始所有下载任务
     */
    public synchronized void startAll() {
        resetPausedToWait();
        if (!isRequestRunning()) {
            executeAsync();
        } else {
            this.onCheckRequest();
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

            } else if (taskDownloading.containsKey(task)) {

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

        if (taskPaused.containsKey(task)) {
            requestWrapper = taskPaused.remove(task);
        }

        if (requestWrapper == null) {
            requestWrapper = new CCDownloadRequestWrapper(null);
        }

        this.taskWaiting.put(task, requestWrapper);

        if (!isRequestRunning()) {
            executeAsync();
        } else {
            this.onCheckRequest();
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
                    pause(toPauseTask);
                    existTaskCount.decrementAndGet();
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
        this.onCheckRequest();
    }

    /**
     * 暂停指定的下载任务
     *
     * @param task 指定的下载任务
     */
    public synchronized void pause(CCDownloadTask task) {

        if (taskWaiting.containsKey(task)) {
            pause(task, taskWaiting.remove(task));
        } else if (taskDownloading.containsKey(task)) {
            existTaskCount.getAndDecrement();
            pause(task, taskDownloading.remove(task));
        }

        this.onCheckRequest();
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

        if (taskPaused.containsKey(task)) {
            return;
        }

        if (requestWrapper == null) {
            if (taskWaiting.containsKey(task)) {
                requestWrapper = taskWaiting.remove(task);
            } else if (taskDownloading.containsKey(task)) {
                requestWrapper = taskDownloading.remove(task);
            }
        }

        if ((requestWrapper != null) && (requestWrapper.getRequest() != null)) {
            requestWrapper.getRequest().cancel();
        }

        if (requestWrapper == null) {
            requestWrapper = new CCDownloadRequestWrapper(null);
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
        } else {
            this.onCheckRequest();
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
                resume(taskList.get(i), i == taskSize - 1);
            }
        }
    }

    /**
     * 继续下载指定的任务
     *
     * @param task 指定的任务
     */
    public synchronized void resume(CCDownloadTask task) {
        this.resume(task, true);
    }

    private synchronized void resume(CCDownloadTask task, boolean startImmediately) {
        CCDownloadRequestWrapper requestWrapper;

        if (task != null) {
            if (taskPaused.containsKey(task)) {
                requestWrapper = taskPaused.remove(task);
                taskWaiting.put(task, requestWrapper);
            }

            if (startImmediately) {
                if (!isRequestRunning()) {
                    executeAsync();
                } else {
                    this.onCheckRequest();
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    public CCMultiDownloadRequest<T> setCCNetCallback(com.codingcoderscode.evolving.net.request.callback.CCNetResultListener netResultListener) {
        this.mNetResultListener = netResultListener;
        return this;
    }

    public CCNetResultListener getCCNetResultListener() {
        return mNetResultListener;
    }

    public int getMaxTaskCount() {
        return maxTaskCount;
    }

    public CCMultiDownloadRequest<T> setMaxTaskCount(int maxTaskCount) {
        this.maxTaskCount = maxTaskCount;
        return this;
    }

    @Override
    public <T> void onStartRequest(Object reqTag, CCCanceler canceler) {
        if (this.mNetResultListener != null) {
            this.mNetResultListener.onStartRequest(reqTag, canceler);
        }
    }

    @Override
    public <T> void onDiskCacheQuerySuccess(Object reqTag, T response) {

    }

    @Override
    public <T> void onDiskCacheQueryFail(Object reqTag, Throwable t) {

    }

    @Override
    public <T> void onNetSuccess(Object reqTag, T response) {

    }

    @Override
    public <T> void onNetFail(Object reqTag, Throwable t) {

    }

    @Override
    public <T> void onRequestSuccess(Object reqTag, T response, int dataSourceMode) {
        try {
            if (reqTag != null && reqTag instanceof CCDownloadTask) {
                CCDownloadTask task = (CCDownloadTask) reqTag;
                this.taskDownloading.remove(task);
                this.existTaskCount.getAndDecrement();
            }

            if (this.mNetResultListener != null) {
                this.mNetResultListener.onRequestSuccess(reqTag, response, dataSourceMode);
            }

            this.onCheckRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T> void onRequestFail(Object reqTag, Throwable t) {
        try {
            if (reqTag != null && reqTag instanceof CCDownloadTask) {
                CCDownloadTask task = (CCDownloadTask) reqTag;
                CCDownloadRequestWrapper requestWrapper = this.taskDownloading.remove(task);

                if (requestWrapper.getRequest() != null) {
                    requestWrapper.getRequest().cancel();
                }

                this.taskPaused.put(task, requestWrapper);
                this.existTaskCount.getAndDecrement();
            }

            if (this.mNetResultListener != null) {
                this.mNetResultListener.onRequestFail(reqTag, t);
            }

            this.onCheckRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T> void onRequestComplete(Object reqTag) {
        if (this.mNetResultListener != null) {
            this.mNetResultListener.onRequestComplete(reqTag);
        }
    }

    @Override
    public <T> void onProgress(Object reqTag, int progress, long netSpeed, long completedSize, long fileSize) {
        if (this.mNetResultListener != null) {
            this.mNetResultListener.onProgress(reqTag, progress, netSpeed, completedSize, fileSize);
        }
    }

    @Override
    public <T> void onProgressSave(Object reqTag, int progress, long netSpeed, long completedSize, long fileSize) {
        if (this.mNetResultListener != null) {
            this.mNetResultListener.onProgressSave(reqTag, progress, netSpeed, completedSize, fileSize);
        }
    }

    @Override
    public void onIntervalCallback() {

    }
}





























