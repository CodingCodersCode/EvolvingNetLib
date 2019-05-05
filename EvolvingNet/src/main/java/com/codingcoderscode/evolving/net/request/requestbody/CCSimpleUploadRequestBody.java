package com.codingcoderscode.evolving.net.request.requestbody;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.codingcoderscode.evolving.net.request.listener.CCNetResultListener;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ByteString;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by CodingCodersCode on 2017/10/18.
 * <p>
 * 上传文件时文件信息包装类
 */

public class CCSimpleUploadRequestBody extends RequestBody {

    private String requestBodyTag;
    private RequestBody requestBody;
    private long previousTime;
    protected CCNetResultListener callback;
    protected CountingSink countingSink;
    private Handler uiHandler;

    public CCSimpleUploadRequestBody(@Nullable String requestBodyTag, @Nullable RequestBody requestBody, CCNetResultListener callback) {
        this.requestBodyTag = requestBodyTag;
        this.requestBody = requestBody;
        this.callback = callback;
        this.uiHandler = new Handler(Looper.getMainLooper());
    }

    public CCSimpleUploadRequestBody(@Nullable String requestBodyTag, @Nullable MediaType contentType, String content, CCNetResultListener callback) {
        this.requestBodyTag = requestBodyTag;
        this.requestBody = RequestBody.create(contentType, content);
        this.callback = callback;
        this.uiHandler = new Handler(Looper.getMainLooper());

    }

    public CCSimpleUploadRequestBody(@Nullable String requestBodyTag, final @Nullable MediaType contentType, final ByteString content, CCNetResultListener callback) {
        this.requestBodyTag = requestBodyTag;
        requestBody = RequestBody.create(contentType, content);
        this.callback = callback;
        this.uiHandler = new Handler(Looper.getMainLooper());

    }


    public CCSimpleUploadRequestBody(@Nullable String requestBodyTag, final @Nullable MediaType contentType, final byte[] content, CCNetResultListener callback) {
        this.requestBodyTag = requestBodyTag;
        requestBody = RequestBody.create(contentType, content);
        this.callback = callback;
        this.uiHandler = new Handler(Looper.getMainLooper());
    }

    public CCSimpleUploadRequestBody(@Nullable String requestBodyTag, final @Nullable MediaType contentType, final byte[] content,
                                     final int offset, final int byteCount, CCNetResultListener callback) {
        this.requestBodyTag = requestBodyTag;
        requestBody = RequestBody.create(contentType, content, offset, byteCount);
        this.callback = callback;
        this.uiHandler = new Handler(Looper.getMainLooper());
    }

    public CCSimpleUploadRequestBody(@Nullable String requestBodyTag, final @Nullable MediaType contentType, final File file, CCNetResultListener callback) {
        this.requestBodyTag = requestBodyTag;
        requestBody = RequestBody.create(contentType, file);
        this.callback = callback;
        this.uiHandler = new Handler(Looper.getMainLooper());
    }


    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() {
        try {
            return requestBody.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        previousTime = System.currentTimeMillis();
        countingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(countingSink);
        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    protected final class CountingSink extends ForwardingSink {

        private long bytesWritten = 0;
        private long contentLength = 0L;
        private long nowTime = 0L;
        private long networkSpeed = 0L;
        private int progress = 0;
        private long totalTime = 0L;

        public CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(@NonNull Buffer source, long byteCount) throws IOException {

            try {

                super.write(source, byteCount);

                if (contentLength == 0) {
                    contentLength = contentLength();
                }
                bytesWritten += byteCount;
                if (callback != null) {

                    nowTime = System.currentTimeMillis();

                    totalTime = (nowTime - previousTime) / 1000;
                    if (totalTime == 0) {
                        totalTime += 1;
                    }
                    networkSpeed = bytesWritten / totalTime;
                    progress = (int) (bytesWritten * 100 / contentLength);

                    if (callback != null){

                        if (uiHandler == null){
                            uiHandler = new Handler(Looper.getMainLooper());
                        }

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                //进度回调
                                callback.onProgress(requestBodyTag == null ? "" : requestBodyTag, progress, networkSpeed, bytesWritten, contentLength);
                            }
                        });
                    }
                }

            } catch (Exception exception) {
                throw exception;
            }
        }
    }
}
