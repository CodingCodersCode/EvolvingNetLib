package com.codingcoderscode.lib.net.request;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.codingcoderscode.lib.net.request.api.CCNetApiService;
import com.codingcoderscode.lib.net.request.base.CCSimpleDownloadRequest;
import com.codingcoderscode.lib.net.request.listener.CCDownloadFileWriteListener;
import com.codingcoderscode.lib.net.request.listener.CCSingleDownloadProgressListener;
import com.codingcoderscode.lib.net.request.entity.CCDownloadTask;
import com.codingcoderscode.lib.net.request.exception.CCUnExpectedException;
import com.codingcoderscode.lib.net.request.exception.CCNoEnoughSpaceException;
import com.codingcoderscode.lib.net.request.exception.CCNoResponseBodyDataException;
import com.codingcoderscode.lib.net.request.retry.CCFlowableRetryWithDelay;
import com.codingcoderscode.lib.net.response.CCBaseResponse;
import com.codingcoderscode.lib.net.util.CCFileUtils;
import com.codingcoderscode.lib.net.util.CCLogUtil;
import com.codingcoderscode.lib.net.util.CCNetUtil;
import com.codingcoderscode.lib.net.util.CCSDCardUtil;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Function;
import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by CodingCodersCode on 2017/10/31.
 * <p>
 * 下载文件请求类
 */

public class CCDownloadRequest<T> extends CCSimpleDownloadRequest<T> {

    //进度回调
    private CCSingleDownloadProgressListener mSingleDownloadProgressListener;
    //下载文件的本地保存路径
    private String fileSavePath;
    //下载文件的本地名称，含后缀
    private String fileSaveName;

    private Context context;

    private Handler uiCallbackHandler;
    //是否删除本地fileSavePath路径下已存在的与fileSaveName同名的文件
    private boolean deleteExistFile;
    //Http Range=rangeStart-rangeEnd
    private long rangeStart = -1;
    private long rangeEnd = -1;
    //是否自动获取和设置Http Range信息
    private boolean autoRange;
    //是否支持断点下载
    private boolean supportRage;

    private InputStream inputStream = null;
    private OutputStream outputStream = null;

    //文件大小
    private long fileSize = 0;
    //已下载大小
    private long downloadedSize = 0;
    //已下载进度
    private int downloadedProgress = 0;
    //上次计算获得的进度
    private int lastDownloadedProgress = downloadedProgress;
    //上次进度计算时间点
    private long lastUpdateTime = 0L;
    //网络下载速度，实时速度 单位：b/s
    private long downloadNetworkSpeed = 0L;

    private CCDownloadFileWriteListener mDownloadFileWriteListener;

    private final int DEFAULT_BUFFER_SIZE = 8 * 1024;

    private CCDownloadTask mDownloadTask;

    public CCDownloadRequest(String url, CCNetApiService apiService) {
        super(url, apiService);
        this.supportRage = false;
    }

    @Override
    protected Flowable<CCBaseResponse<T>> getRequestFlowable() {

        uiCallbackHandler = new Handler(Looper.getMainLooper());

        return Flowable.create(new FlowableOnSubscribe<Call<ResponseBody>>() {
            @Override
            public void subscribe(FlowableEmitter<Call<ResponseBody>> e) throws Exception {
                Call<ResponseBody> call;

                call = getRequestCall();

                e.onNext(call);
                e.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                //.subscribeOn(Schedulers.io())
                //.unsubscribeOn(Schedulers.io())
                //.observeOn(Schedulers.io())
                .flatMap(new Function<Call<ResponseBody>, Publisher<CCBaseResponse<T>>>() {
                    @Override
                    public Publisher<CCBaseResponse<T>> apply(Call<ResponseBody> responseBodyCall) throws Exception {

                        Response<ResponseBody> retrofitResponse;
                        Headers headers = null;
                        try {
                            retrofitResponse = responseBodyCall.clone().execute();

                            headers = retrofitResponse.headers();
                        } catch (Exception exception) {
                            throw new CCUnExpectedException(exception);
                        }

                        onTryToWriteFileToSDCard(retrofitResponse, headers);

                        return Flowable.just(new CCBaseResponse<T>(null, headers, false, false, true, null));
                    }
                }).retryWhen(new CCFlowableRetryWithDelay(getRetryCount(), getRetryDelayTimeMillis())).onBackpressureLatest();
    }

    @Override
    protected Call<ResponseBody> getRequestCall() {

        this.mDownloadTask = new CCDownloadTask(CCNetUtil.regexApiUrlWithPathParam(getApiUrl(), getPathMap()), getFileSavePath(), getFileSaveName(), 1, null, 0);

        if (isSupportRage()) {

            onFileSaveCheck(isSupportRage());

            File fileToSave = new File(getFileSavePath(), getFileSaveName());

            long fileNowSize = fileToSave.length();

            long requestRangeStart = (fileNowSize - 2 >= 0) ? fileNowSize - 2 : 0;

            StringBuilder rangeBulider = new StringBuilder("bytes=");

            rangeBulider.append(requestRangeStart).append("-");
            downloadedSize = requestRangeStart;

            getHeaderMap().put("Range", rangeBulider.toString());
        }

        Call<ResponseBody> call;
        call = getCCNetApiService().executeDownload(CCNetUtil.regexApiUrlWithPathParam(getApiUrl(), getPathMap()), getHeaderMap(), getRequestParam());

        return call;
    }

    @Override
    protected void onSubscribeLocal(Subscription s) {
        super.onSubscribeLocal(s);
        try {
            if (getDownloadProgressListener() != null && isRequestRunning() && !isForceCanceled()) {
                getDownloadProgressListener().<T>onStart(getReqTag(), this.mDownloadTask, getNetCCCanceler());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNextLocal(CCBaseResponse<T> tccBaseResponse) {
        super.onNextLocal(tccBaseResponse);
        try {
            if (getDownloadProgressListener() != null && isRequestRunning() && !isForceCanceled()) {
                getDownloadProgressListener().<T>onSuccess(getReqTag(), this.mDownloadTask);
            }
            this.setRequestRunning(false);
            this.setForceCanceled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onErrorLocal(Throwable t) {
        super.onErrorLocal(t);
        try {
            if (getDownloadProgressListener() != null && isRequestRunning() && !isForceCanceled()) {
                getDownloadProgressListener().<T>onError(getReqTag(), this.mDownloadTask, t);
            }
            this.setRequestRunning(false);
            this.setForceCanceled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCompleteLocal() {
        super.onCompleteLocal();
        try {
            if (getDownloadProgressListener() != null && isRequestRunning() && !isForceCanceled()) {
                getDownloadProgressListener().<T>onComplete(getReqTag(), this.mDownloadTask);
            }
            this.setRequestRunning(false);
            this.setForceCanceled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long convertStringToLong(String source) {
        long result = 0;
        try {
            result = Long.parseLong(source);
        } catch (Exception e) {

        }
        return result;
    }

    /**
     * 将文件内容写入磁盘
     *
     * @param retrofitResponse
     * @param headers
     * @throws Exception
     */
    protected void onTryToWriteFileToSDCard(Response<ResponseBody> retrofitResponse, Headers headers) throws Exception {
        try {
            onFileSaveCheck(CCNetUtil.isHttpSupportRange(headers) && isSupportRage());

            String contentLengthStr = CCNetUtil.getHeader("Content-Length", headers);

            if (TextUtils.isEmpty(contentLengthStr)) {
                throw new CCNoResponseBodyDataException("no file data");
            } else {
                long contentLengthLong = convertStringToLong(contentLengthStr);

                if (contentLengthLong > CCSDCardUtil.getSDCardAvailableSize() * 1024 * 1024) {
                    throw new CCNoEnoughSpaceException("write failed: ENOSPC (No space left on device)");
                }

                if (contentLengthLong == 0) {
                    throw new CCNoResponseBodyDataException("no file data");
                }
            }

            if (getCCDownloadFileWriteListener() != null) {
                getCCDownloadFileWriteListener().onWriteToDisk(getReqTag(), this.mDownloadTask, headers, retrofitResponse.body(), getDownloadProgressListener());
            } else {
                onWriteToDisk(retrofitResponse.body());
            }

        } catch (CCNoResponseBodyDataException nrbde) {
            throw nrbde;
        } catch (Exception exception) {
            throw new CCUnExpectedException(exception);
        }
    }

    /**
     * 将下载内容写到磁盘指定位置
     *
     * @param responseBody
     */
    private void onWriteToDisk(ResponseBody responseBody) throws Exception {
        File fileToSave;
        byte[] fileReaderBuffer;
        int readSize;
        RandomAccessFile rafFile = null;
        try {
            if (responseBody == null) {
                throw new CCNoResponseBodyDataException("okhttp3.ResponseBody == null, there is no data!");
            }

            fileToSave = new File(getFileSavePath(), getFileSaveName());

            rafFile = new RandomAccessFile(fileToSave, "rw");
            if (isSupportRage()) {
                rafFile.seek(downloadedSize);
            } else {
                rafFile.setLength(0);
                downloadedSize = 0;
            }

            fileReaderBuffer = new byte[DEFAULT_BUFFER_SIZE];

            fileSize = responseBody.contentLength() + downloadedSize;

            inputStream = responseBody.byteStream();

            lastUpdateTime = System.currentTimeMillis();

            while (true) {
                readSize = inputStream.read(fileReaderBuffer);

                if (readSize == -1) {
                    break;
                }

                rafFile.write(fileReaderBuffer, 0, readSize);
                downloadedSize += readSize;

                if (fileSize == -1 || fileSize == 0) {
                    downloadedProgress = 100;
                } else {
                    downloadedProgress = (int) (downloadedSize * 100 / fileSize);
                }

                if (downloadedProgress > lastDownloadedProgress || lastDownloadedProgress == 0.0f) {

                    lastDownloadedProgress = downloadedProgress;

                    long nowTime = System.currentTimeMillis();

                    downloadNetworkSpeed = (readSize * 1000) / ((nowTime - lastUpdateTime == 0) ? 1 : nowTime - lastUpdateTime);

                    lastUpdateTime = nowTime;

                    if (getDownloadProgressListener() != null) {

                        getDownloadProgressListener().onProgressSave(getReqTag(), this.mDownloadTask, downloadedProgress, downloadNetworkSpeed, downloadedSize, fileSize);

                        requireNonNullUICallbackHandler();

                        uiCallbackHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                getDownloadProgressListener().onProgress(getReqTag(), mDownloadTask, downloadedProgress, downloadNetworkSpeed, downloadedSize, fileSize);
                            }
                        });
                    }
                }
            }
        } catch (Exception exception) {
            if ((exception instanceof java.io.InterruptedIOException) && isForceCanceled()) {
                CCLogUtil.printLog("e", "CCDownloadRequest", "当前异常类型是java.io.InterruptedIOException,且是强制中断,为用户主动取消网络传输");
            } else {
                throw new CCUnExpectedException(exception);
            }
        } finally {
            onCloseInputStream(inputStream);
            onCloseOutputStream(outputStream);
            onCloseRandomAccessFile(rafFile);
        }
    }

    private void onCloseInputStream(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
            CCLogUtil.printLog("e", getClass().getCanonicalName(), "发生异常", e);
        }
    }

    private void onCloseOutputStream(OutputStream outputStream) {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Exception e) {
            CCLogUtil.printLog("e", getClass().getCanonicalName(), "发生异常", e);
        }
    }

    private void onCloseRandomAccessFile(RandomAccessFile rafFile) {
        try {
            if (rafFile != null) {
                rafFile.close();
            }
        } catch (Exception e) {
            CCLogUtil.printLog("e", getClass().getCanonicalName(), "发生异常", e);
        }
    }

    private void requireNonNullUICallbackHandler() {
        if (uiCallbackHandler == null) {
            uiCallbackHandler = new Handler(Looper.getMainLooper());
        }
    }

    private void onFileSaveCheck(boolean supportRage) {
        File file;
        try {
            if (TextUtils.isEmpty(getFileSaveName())) {
                setFileSaveName("file-" + System.currentTimeMillis() + ".tmp");
            }

            if (!getFileSaveName().contains(".")) {
                setFileSaveName(getFileSaveName() + ".tmp");
            }

            if (TextUtils.isEmpty(getFileSavePath())) {
                //setFileSavePath(context.getExternalFilesDir(null) + File.separator + "DownLoads" + File.separator);
                setFileSavePath(Environment.getExternalStorageDirectory() + File.separator + "DownLoads" + File.separator);
            }

            file = new File(getFileSavePath(), getFileSaveName());

            if (file.exists() && file.isFile() && !supportRage) {
                CCFileUtils.deleteFile(file);
            }

            String parentPath = file.getParent();
            File parentFile = new File(parentPath);
            if (parentFile.exists() && parentFile.isDirectory()) {

            } else {
                parentFile.mkdirs();
            }
            file.createNewFile();

        } catch (Exception exception) {
            CCLogUtil.printLog("e", getClass().getCanonicalName(), "发生异常", exception);
        }
    }

    @Override
    public void cancel() {
        super.cancel();

    }

    public String getFileSavePath() {
        return fileSavePath;
    }

    public CCDownloadRequest<T> setFileSavePath(String fileSavePath) {
        this.fileSavePath = fileSavePath;
        return this;
    }

    public String getFileSaveName() {
        return fileSaveName;
    }

    public CCDownloadRequest<T> setFileSaveName(String fileSaveName) {
        this.fileSaveName = fileSaveName;
        return this;
    }

    public boolean isSupportRage() {
        return supportRage;
    }

    public CCDownloadRequest<T> setSupportRage(boolean supportRage) {
        this.supportRage = supportRage;
        return this;
    }

    public CCDownloadFileWriteListener getCCDownloadFileWriteListener() {
        return mDownloadFileWriteListener;
    }

    /**
     * 设置自定义的文件本地写入回调
     *
     * @param downloadFileWriteListener
     * @return
     */
    public CCDownloadRequest<T> setCCDownloadFileWriteListener(CCDownloadFileWriteListener downloadFileWriteListener) {
        this.mDownloadFileWriteListener = downloadFileWriteListener;
        return this;
    }

    public CCSingleDownloadProgressListener getDownloadProgressListener() {
        return mSingleDownloadProgressListener;
    }

    public CCDownloadRequest<T> setDownloadProgressListener(CCSingleDownloadProgressListener downloadProgressListener) {
        this.mSingleDownloadProgressListener = downloadProgressListener;
        return this;
    }
}
