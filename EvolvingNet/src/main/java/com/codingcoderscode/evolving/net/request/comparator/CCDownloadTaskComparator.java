package com.codingcoderscode.evolving.net.request.comparator;

import com.codingcoderscode.evolving.net.request.entity.CCDownloadTask;

import java.util.Comparator;

/**
 * Created by ghc on 2017/11/13.
 */

public class CCDownloadTaskComparator {

    public InnerComparator innerComparator;

    private CCDownloadTaskComparator() {
        innerComparator = new InnerComparator();
    }

    private static class CCTaskComparatorHolder {
        private static final CCDownloadTaskComparator INSTANCE = new CCDownloadTaskComparator();
    }

    public static final CCDownloadTaskComparator getInstance() {
        return CCDownloadTaskComparator.CCTaskComparatorHolder.INSTANCE;
    }

    public int compare(CCDownloadTask o1, CCDownloadTask o2) {
        return innerComparator.compare(o1, o2);
    }


    private static class InnerComparator implements Comparator<CCDownloadTask> {

        @Override
        public int compare(CCDownloadTask o1, CCDownloadTask o2) {
            int result = 0;
            if (o1 == o2) {
                result = 0;
            } else if (o1.equals(o2)) {
                result = 0;
            } /*else if (o1.getDownloadStatus() != o2.getDownloadStatus()) {
                result = o1.getDownloadStatus() - o2.getDownloadStatus();
            } */else if (o1.getPriority() != o2.getPriority()) {
                result = o1.getPriority() - o2.getPriority();
            } else {
                result = o1.getTaskStamp() - o2.getTaskStamp();
            }

            return result;
        }
    }

}
