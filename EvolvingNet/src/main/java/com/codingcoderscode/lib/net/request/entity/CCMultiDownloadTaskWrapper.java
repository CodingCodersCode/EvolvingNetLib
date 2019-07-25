package com.codingcoderscode.lib.net.request.entity;

/**
 * Date：2019/5/5 09:36
 * <p>
 * author: CodingCodersCode
 */
public class CCMultiDownloadTaskWrapper {
    private String defaultStringReqTag;
    private CCDownloadTask downloadTask;

    public static CCMultiDownloadTaskWrapper newInstance(String defaultStringReqTag, CCDownloadTask downloadTask) {
        return new CCMultiDownloadTaskWrapper(defaultStringReqTag, downloadTask);
    }

    public CCMultiDownloadTaskWrapper(String defaultStringReqTag, CCDownloadTask downloadTask) {
        this.defaultStringReqTag = defaultStringReqTag;
        this.downloadTask = downloadTask;
    }

    public String getDefaultStringReqTag() {
        return defaultStringReqTag;
    }

    public void setDefaultStringReqTag(String defaultStringReqTag) {
        this.defaultStringReqTag = defaultStringReqTag;
    }

    public CCDownloadTask getDownloadTask() {
        return downloadTask;
    }

    public void setDownloadTask(CCDownloadTask downloadTask) {
        this.downloadTask = downloadTask;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof CCMultiDownloadTaskWrapper)) return false;

        CCMultiDownloadTaskWrapper that = (CCMultiDownloadTaskWrapper) object;

        if (getDefaultStringReqTag() != null ? !getDefaultStringReqTag().equals(that.getDefaultStringReqTag()) : that.getDefaultStringReqTag() != null)
            return false;
        return getDownloadTask() != null ? getDownloadTask().equals(that.getDownloadTask()) : that.getDownloadTask() == null;
    }

    @Override
    public int hashCode() {
        int result = getDefaultStringReqTag() != null ? getDefaultStringReqTag().hashCode() : 0;
        result = 31 * result + (getDownloadTask() != null ? getDownloadTask().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CCMultiDownloadTaskWrapper{" +
                "defaultStringReqTag='" + defaultStringReqTag + '\'' +
                ", downloadTask=" + downloadTask +
                '}';
    }
}
