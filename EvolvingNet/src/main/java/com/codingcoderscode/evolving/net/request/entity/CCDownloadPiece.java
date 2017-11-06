package com.codingcoderscode.evolving.net.request.entity;

/**
 * Created by ghc on 2017/11/5.
 */

public class CCDownloadPiece {
    private long rawRangeStart;
    private long rawRangeEnd;

    private long realRangeStart;
    private long realRangeEnd;

    public CCDownloadPiece(long rawRangeStart, long rawRangeEnd) {
        this.rawRangeStart = rawRangeStart;
        this.rawRangeEnd = rawRangeEnd;
    }

    public long getRawRangeStart() {
        return rawRangeStart;
    }

    public void setRawRangeStart(long rawRangeStart) {
        this.rawRangeStart = rawRangeStart;
    }

    public long getRawRangeEnd() {
        return rawRangeEnd;
    }

    public void setRawRangeEnd(long rawRangeEnd) {
        this.rawRangeEnd = rawRangeEnd;
    }

    public long getRealRangeStart() {
        return realRangeStart;
    }

    public void setRealRangeStart(long realRangeStart) {
        this.realRangeStart = realRangeStart;
    }

    public long getRealRangeEnd() {
        return realRangeEnd;
    }

    public void setRealRangeEnd(long realRangeEnd) {
        this.realRangeEnd = realRangeEnd;
    }
}
