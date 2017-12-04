package com.demo.evolving.net.lib.downloadmanager;

import com.codingcoderscode.evolving.net.response.CCBaseResponse;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.trello.rxlifecycle2.components.support.RxFragment;

import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.FlowableTransformer;

/**
 * Created by CodingCodersCode on 2017/11/7.
 *
 *
 */

public abstract class CCBaseDownloadTask {
    private FlowableTransformer<CCBaseResponse<Void>, CCBaseResponse<Void>> netLifecycleComposer;

    private static AtomicInteger downloadTaskStampAtomicInteger;

    private int taskStamp;

    private String renamedSaveName;

    static {
        downloadTaskStampAtomicInteger = new AtomicInteger(0);
    }

    public CCBaseDownloadTask() {
        this.netLifecycleComposer = null;
        initTaskStamp();
    }

    public CCBaseDownloadTask(RxAppCompatActivity activity) {
        this.netLifecycleComposer = activity.bindUntilEvent(ActivityEvent.DESTROY);
        initTaskStamp();
    }

    public CCBaseDownloadTask(RxFragment fragment) {
        this.netLifecycleComposer = fragment.bindUntilEvent(FragmentEvent.DESTROY);
        initTaskStamp();
    }

    private void initTaskStamp(){
        this.taskStamp = downloadTaskStampAtomicInteger.getAndIncrement();
    }

    public FlowableTransformer<CCBaseResponse<Void>, CCBaseResponse<Void>> getNetLifecycleComposer() {
        return netLifecycleComposer;
    }

    public void setNetLifecycleComposer(FlowableTransformer<CCBaseResponse<Void>, CCBaseResponse<Void>> netLifecycleComposer) {
        this.netLifecycleComposer = netLifecycleComposer;
    }

    public int getTaskStamp() {
        return taskStamp;
    }

    public String getRenamedSaveName() {
        return renamedSaveName;
    }

    public void setRenamedSaveName(String renamedSaveName) {
        this.renamedSaveName = renamedSaveName;
    }
}
