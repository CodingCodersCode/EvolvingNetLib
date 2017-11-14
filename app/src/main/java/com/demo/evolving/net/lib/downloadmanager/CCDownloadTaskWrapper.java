package com.demo.evolving.net.lib.downloadmanager;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.codingcoderscode.evolving.net.request.CCDownloadRequest;

/**
 * Created by ghc on 2017/11/8.
 */

public class CCDownloadTaskWrapper implements Comparable<CCDownloadTaskWrapper> {

    private CCDownloadTask task;
    private CCDownloadRequest request;

    public CCDownloadTaskWrapper(CCDownloadTask task, CCDownloadRequest request) {
        this.task = task;
        this.request = request;
    }

    public CCDownloadTask getTask() {
        return task;
    }

    public void setTask(CCDownloadTask task) {
        this.task = task;
    }

    public CCDownloadRequest getRequest() {
        return request;
    }

    public void setRequest(CCDownloadRequest request) {
        this.request = request;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }else {

            if (obj instanceof CCDownloadTaskWrapper){

                CCDownloadTaskWrapper objTaskWrapper = (CCDownloadTaskWrapper)obj;

                if (this.task == objTaskWrapper.getTask()){
                    return true;
                }else {
                    return false;
                }
            }else {
                return false;
            }
        }
    }

    @Override
    public int compareTo(@NonNull CCDownloadTaskWrapper o) {
        if (this == o){
            return 0;
        }else {

            CCDownloadTask oTask = o.getTask();

            if (this == o) {
                return 0;
            } else {
                if (this.task.getDownloadStatus() == CCDownloadStatus.WAIT) {

                    switch (oTask.getDownloadStatus()) {
                        case CCDownloadStatus.WAIT:
                            return this.task.getPriority() - oTask.getPriority();
                        case CCDownloadStatus.PAUSED:
                            return -1;
                        case CCDownloadStatus.DOWNLOADING:
                            return 1;
                        case CCDownloadStatus.COMPLETED:
                            return -1;
                    }

                } else if (this.task.getDownloadStatus() == CCDownloadStatus.PAUSED) {

                    switch (oTask.getDownloadStatus()) {
                        case CCDownloadStatus.WAIT:
                            return 1;
                        case CCDownloadStatus.PAUSED:
                            return this.task.getPriority() - oTask.getPriority();
                        case CCDownloadStatus.DOWNLOADING:
                            return 1;
                        case CCDownloadStatus.COMPLETED:
                            return -1;
                    }

                } else if (this.task.getDownloadStatus() == CCDownloadStatus.DOWNLOADING) {

                    switch (oTask.getDownloadStatus()) {
                        case CCDownloadStatus.WAIT:
                            return -1;
                        case CCDownloadStatus.PAUSED:
                            return -1;
                        case CCDownloadStatus.DOWNLOADING:
                            return this.task.getPriority() - oTask.getPriority();
                        case CCDownloadStatus.COMPLETED:
                            return -1;
                    }

                } else if (this.task.getDownloadStatus() == CCDownloadStatus.COMPLETED) {

                    switch (oTask.getDownloadStatus()) {
                        case CCDownloadStatus.WAIT:
                            return 1;
                        case CCDownloadStatus.PAUSED:
                            return 1;
                        case CCDownloadStatus.DOWNLOADING:
                            return 1;
                        case CCDownloadStatus.COMPLETED:
                            return this.task.getPriority() - oTask.getPriority();
                    }
                }
                return this.task.getPriority() - oTask.getPriority();
            }


        }
    }
}
