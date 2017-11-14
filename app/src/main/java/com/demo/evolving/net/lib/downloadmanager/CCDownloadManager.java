package com.demo.evolving.net.lib.downloadmanager;


import com.codingcoderscode.evolving.net.CCRxNetManager;
import com.codingcoderscode.evolving.net.cache.mode.CCCacheMode;
import com.codingcoderscode.evolving.net.request.CCDownloadRequest;
import com.codingcoderscode.evolving.net.request.callback.CCNetCallback;
import com.codingcoderscode.evolving.net.request.canceler.CCCanceler;
import com.codingcoderscode.evolving.net.request.exception.NoEnoughSpaceException;
import com.codingcoderscode.evolving.net.util.NetLogUtil;
import com.codingcoderscode.evolving.net.util.SDCardUtil;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ghc on 2017/11/7.
 * <p>
 * 下载管理类，封装多线程下载，任务自动管理和调度等功能
 */

public class CCDownloadManager extends CCNetCallback {

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

    private CCNetCallback ccNetCallback;

    private final int DEFAULT_RETRY_COUNT = 3;

    private boolean requestInPriority = true;

    private boolean quitDownload = false;

    private boolean managerRunning = false;

    private CCDownloadManager() {
        this.taskWaiting = new ConcurrentSkipListMap<CCDownloadTask, CCDownloadRequestWrapper>(new DownloadTaskComparator());

        this.taskDownloading = new ConcurrentSkipListMap<CCDownloadTask, CCDownloadRequestWrapper>(new DownloadTaskComparator());

        this.taskPaused = new ConcurrentSkipListMap<CCDownloadTask, CCDownloadRequestWrapper>(new DownloadTaskComparator());

        this.existTaskCount = new AtomicInteger(0);

    }

    private static class CCDownloadManagerHolder{
        private static final CCDownloadManager INSTANCE = new CCDownloadManager();
    }

    public static final CCDownloadManager getInstance(){
        return CCDownloadManagerHolder.INSTANCE;
    }

    private void resetPausedToWait() {
        Map.Entry<CCDownloadTask, CCDownloadRequestWrapper> pausedTask;
        while ((pausedTask = taskPaused.pollFirstEntry()) != null) {
            pausedTask.getKey().setDownloadStatus(CCDownloadStatus.WAIT);
            taskWaiting.put(pausedTask.getKey(), pausedTask.getValue());
        }
    }


    /////////////////////////////////////
    //  开始下载
    /////////////////////////////////////

    public synchronized void startAll() {

        resetPausedToWait();

        if (!managerRunning) {
            onStartDownloadManager();
        }
    }

    public synchronized void startAll(List<CCDownloadTask> taskList) {

        if (taskList != null && taskList.size() > 0) {

            Collections.sort(taskList, new DownloadTaskComparator());

            for (int i = 0; i < taskList.size(); i++) {
                start(taskList.get(i));
            }
        }
    }

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

    private synchronized void start(CCDownloadTask task, CCDownloadRequestWrapper requestWrapper){
        if (task == null){
            return;
        }

        if (taskWaiting.containsKey(task)){
            return;
        }

        if (taskDownloading.containsKey(task)){
            return;
        }

        if (requestWrapper == null){
            requestWrapper = new CCDownloadRequestWrapper(null);
        }

        task.setDownloadStatus(CCDownloadStatus.WAIT);

        this.taskWaiting.put(task, requestWrapper);

        if (!managerRunning) {
            onStartDownloadManager();
        }
    }

    /////////////////////////////////////////////
    //          暂停
    /////////////////////////////////////////////

    public synchronized void pauseAll() {

        Map.Entry<CCDownloadTask, CCDownloadRequestWrapper> toPauseTask;

        quitDownload = true;

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

    public synchronized void pauseAll(List<CCDownloadTask> taskList) {
        if (taskList != null && taskList.size() > 0) {
            for (int i = 0; i < taskList.size(); i++) {
                pause(taskList.get(i));
            }
        }
    }

    public synchronized void pause(CCDownloadTask task) {

        task.setDownloadStatus(CCDownloadStatus.WAIT);
        if (taskWaiting.containsKey(task)) {
            pause(task, taskWaiting.remove(task));
            return;
        }

        task.setDownloadStatus(CCDownloadStatus.DOWNLOADING);
        if (taskDownloading.containsKey(task)) {
            existTaskCount.getAndDecrement();
            pause(task, taskDownloading.remove(task));
            return;
        }
    }

    private synchronized void pause(Map.Entry<CCDownloadTask, CCDownloadRequestWrapper> taskEntry) {
        if (taskEntry != null) {
            pause(taskEntry.getKey(), taskEntry.getValue());
        }
    }

    private synchronized void pause(CCDownloadTask task, CCDownloadRequestWrapper requestWrapper){
        if (task == null){
            return;
        }

        if (requestWrapper == null){
            requestWrapper = new CCDownloadRequestWrapper(null);
        }

        if (requestWrapper.getRequest() != null){
            requestWrapper.getRequest().cancel();
        }

        task.setDownloadStatus(CCDownloadStatus.PAUSED);
        if (taskPaused.containsKey(task)){
            return;
        }

        this.taskPaused.put(task, requestWrapper);
    }

    ///////////////////////////
    //  继续下载
    //////////////////////////

    public synchronized void resumeAll() {
        resetPausedToWait();

        if (!managerRunning) {
            onStartDownloadManager();
        }
    }

    public synchronized void resumeAll(List<CCDownloadTask> taskList) {

        if (taskList != null && taskList.size() > 0) {

            int taskSize = taskList.size();

            for (int i = 0; i < taskSize; i++) {
                resume(taskList.get(i));
            }
        }
    }

    public synchronized void resume(CCDownloadTask task) {

        CCDownloadRequestWrapper requestWrapper;

        if (task != null) {

            if (taskPaused.containsKey(task)) {
                requestWrapper = taskPaused.remove(task);

                task.setDownloadStatus(CCDownloadStatus.WAIT);
                taskWaiting.put(task, requestWrapper);
            }

            if (!managerRunning) {
                onStartDownloadManager();
            }
        }
    }

    private boolean quitOrNot(){
        if (taskWaiting.size() > 0 || taskDownloading.size() > 0){
            return false;
        }else if (!managerRunning){
            return true;
        }
        else {
            return true;
        }
    }

    private synchronized void onStartDownloadManager() {

        if (managerRunning) {
            return;
        }

        managerRunning = true;

        Flowable<Integer> flowable = Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {

                while (true) {

                    if (!managerRunning) {
                        break;
                    }

                    if (existTaskCount.intValue() < maxTaskCount) {
                        if (taskWaiting.size() > 0) {
                            e.onNext(existTaskCount.getAndIncrement());
                            NetLogUtil.printLog("e", LOG_TAG, "启动一个下载任务，当前existTaskCount=" + existTaskCount.intValue());
                        }else {
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
                .unsubscribeOn(Schedulers.io());

        flowable.observeOn(Schedulers.io())
                .flatMap(new Function<Integer, Publisher<String>>() {
                    @Override
                    public Publisher<String> apply(Integer integer) throws Exception {

                        onCreateAndStartTask();

                        return Flowable.just("atomicInteger的值为:" + integer);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                        managerRunning = true;
                        NetLogUtil.printLog("e", LOG_TAG, "调用了onSubscribe(Subscription s)方法");
                    }

                    @Override
                    public void onNext(String s) {
                        NetLogUtil.printLog("e", LOG_TAG, "调用了onNext(String s)方法，s=" + s);
                    }

                    @Override
                    public void onError(Throwable t) {
                        managerRunning = false;
                        pauseAll();
                        NetLogUtil.printLog("e", LOG_TAG, "调用了onError(Throwable t)方法", t);
                    }

                    @Override
                    public void onComplete() {
                        managerRunning = false;
                        NetLogUtil.printLog("e", LOG_TAG, "调用了onComplete()方法");
                    }
                });


    }

    private void onCreateAndStartTask() throws Exception {
        Map.Entry<CCDownloadTask, CCDownloadRequestWrapper> toDownloadTaskEntry = null;
        CCDownloadTask toDownloadTask = null;
        CCDownloadRequestWrapper toDownloadTaskWrapper = null;
        CCDownloadRequest downloadRequest = null;
        try {
            if ((toDownloadTaskEntry = taskWaiting.pollFirstEntry()) != null) {

                toDownloadTask = toDownloadTaskEntry.getKey();
                toDownloadTaskWrapper = toDownloadTaskEntry.getValue();

                if (toDownloadTask == null){
                    existTaskCount.getAndDecrement();
                    return;
                }

                if (toDownloadTask.getFileSize() > SDCardUtil.getSDCardAvailableSize() * 1024 * 1024){

                    pause(toDownloadTask, toDownloadTaskWrapper);

                    existTaskCount.getAndDecrement();

                    NoEnoughSpaceException noEnoughSpaceException = new NoEnoughSpaceException("write failed: ENOSPC (No space left on device)");

                    if (ccNetCallback != null){
                        ccNetCallback.onError(toDownloadTask, noEnoughSpaceException);
                    }

                    return;
                }

                toDownloadTask.setDownloadStatus(CCDownloadStatus.DOWNLOADING);

                if (toDownloadTaskWrapper != null){

                    if (toDownloadTaskWrapper.getRequest() != null){
                        toDownloadTaskWrapper.getRequest().executeAsync();
                    }else {
                        downloadRequest = CCRxNetManager.<Void>download(toDownloadTask.getSourceUrl())
                                .setFileSavePath(toDownloadTask.getSavePath())
                                .setFileSaveName(toDownloadTask.getSaveName())
                                .setRetryCount(DEFAULT_RETRY_COUNT)
                                .setCacheQueryMode(CCCacheMode.QueryMode.MODE_ONLY_NET)
                                .setCacheSaveMode(CCCacheMode.SaveMode.MODE_NO_CACHE)
                                .setReqTag(toDownloadTask)
                                .setSupportRage(true)
                                .setCCNetCallback(this)
                                .setNetLifecycleComposer(toDownloadTask.getNetLifecycleComposer())
                                .setResponseBeanType(Void.class);

                        downloadRequest.executeAsync();
                        toDownloadTaskWrapper.setRequest(downloadRequest);
                    }

                }else {
                    downloadRequest = CCRxNetManager.<Void>download(toDownloadTask.getSourceUrl())
                            .setFileSavePath(toDownloadTask.getSavePath())
                            .setFileSaveName(toDownloadTask.getSaveName())
                            .setRetryCount(DEFAULT_RETRY_COUNT)
                            .setCacheQueryMode(CCCacheMode.QueryMode.MODE_ONLY_NET)
                            .setCacheSaveMode(CCCacheMode.SaveMode.MODE_NO_CACHE)
                            .setReqTag(toDownloadTask)
                            .setSupportRage(true)
                            .setCCNetCallback(this)
                            .setNetLifecycleComposer(toDownloadTask.getNetLifecycleComposer())
                            .setResponseBeanType(Void.class);

                    downloadRequest.executeAsync();

                    toDownloadTaskWrapper = new CCDownloadRequestWrapper(downloadRequest);
                }

                taskDownloading.put(toDownloadTask, toDownloadTaskWrapper);

            }else {
                existTaskCount.getAndDecrement();
            }
        }
        catch (Exception e) {

            pause(toDownloadTask, toDownloadTaskWrapper);

            this.existTaskCount.getAndDecrement();

            if (ccNetCallback != null){
                ccNetCallback.onError(toDownloadTask, e);
            }

        }
    }


    @Override
    public <T> void onStartRequest(Object reqTag, CCCanceler canceler) {

        if (reqTag != null && reqTag instanceof CCDownloadTask){
            CCDownloadTask task = (CCDownloadTask)reqTag;

            task.setDownloadStatus(CCDownloadStatus.DOWNLOADING);
        }

        if (ccNetCallback != null) {
            ccNetCallback.onStartRequest(reqTag, canceler);
        }
    }

    @Override
    public <T> void onSuccess(Object reqTag, T response) {

        if (reqTag != null && reqTag instanceof CCDownloadTask){
            CCDownloadTask task = (CCDownloadTask)reqTag;
            this.taskDownloading.remove(task);

            task.setDownloadStatus(CCDownloadStatus.COMPLETED);

            existTaskCount.getAndDecrement();
        }

        if (ccNetCallback != null) {
            ccNetCallback.onSuccess(reqTag, response);
        }
    }

    @Override
    public <T> void onError(Object reqTag, Throwable t) {

        if (reqTag != null && reqTag instanceof CCDownloadTask){
            CCDownloadTask task = (CCDownloadTask)reqTag;
            CCDownloadRequestWrapper requestWrapper = this.taskDownloading.remove(task);

            task.setDownloadStatus(CCDownloadStatus.PAUSED);
            if (requestWrapper.getRequest() != null) {
                requestWrapper.getRequest().cancel();
            }

            this.taskPaused.put(task, requestWrapper);

            existTaskCount.getAndDecrement();
        }

        if (ccNetCallback != null) {
            ccNetCallback.onError(reqTag, t);
        }
    }

    @Override
    public <T> void onComplete(Object reqTag) {
        if (ccNetCallback != null) {
            ccNetCallback.onComplete(reqTag);
        }
    }

    @Override
    public <T> void onProgress(Object reqTag, int progress, long netSpeed, long completedSize, long fileSize) {
        if (ccNetCallback != null) {

            if (reqTag instanceof CCDownloadTask){
                CCDownloadTask task = (CCDownloadTask)reqTag;

                task.setFileSize(fileSize);
                task.setDownloadedSize(completedSize);
                task.setProgress(progress);

            }

            ccNetCallback.onProgress(reqTag, progress, netSpeed, completedSize, fileSize);
        }
    }

    @Override
    public <T> void onProgressSave(Object reqTag, int progress, long netSpeed, long completedSize, long fileSize) {
        if (ccNetCallback != null) {
            ccNetCallback.onProgressSave(reqTag, progress, netSpeed, completedSize, fileSize);
        }
    }

    private static class DownloadTaskComparator implements Comparator<CCDownloadTask> {

        @Override
        public int compare(CCDownloadTask o1, CCDownloadTask o2) {
            int result = CCTaskComparator.getInstance().compare(o1, o2);
            return result;
        }
    }

    public int getMaxTaskCount() {
        return maxTaskCount;
    }

    public void setMaxTaskCount(int maxTaskCount) {
        this.maxTaskCount = maxTaskCount;
    }

    public CCNetCallback getCcNetCallback() {
        return ccNetCallback;
    }

    public void setCcNetCallback(CCNetCallback ccNetCallback) {
        this.ccNetCallback = ccNetCallback;
    }
}
