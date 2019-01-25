package com.codingcoderscode.evolving.net.request;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.codingcoderscode.evolving.net.CCRxNetManager;
import com.codingcoderscode.evolving.net.cache.mode.CCCMode;
import com.codingcoderscode.evolving.net.request.api.CCNetApiService;
import com.codingcoderscode.evolving.net.request.base.CCRequest;
import com.codingcoderscode.evolving.net.request.callback.CCDownloadFileWritterCallback;
import com.codingcoderscode.evolving.net.request.exception.CCUnExpectedException;
import com.codingcoderscode.evolving.net.request.exception.NoEnoughSpaceException;
import com.codingcoderscode.evolving.net.request.exception.NoResponseBodyDataException;
import com.codingcoderscode.evolving.net.request.method.CCHttpMethod;
import com.codingcoderscode.evolving.net.request.retry.FlowableRetryWithDelay;
import com.codingcoderscode.evolving.net.response.CCBaseResponse;
import com.codingcoderscode.evolving.net.util.CCFileUtils;
import com.codingcoderscode.evolving.net.util.CCNetUtil;
import com.codingcoderscode.evolving.net.util.NetLogUtil;
import com.codingcoderscode.evolving.net.util.SDCardUtil;

import org.reactivestreams.Publisher;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by CodingCodersCode on 2017/10/31.
 * <p>
 * 下载文件请求类
 */

public class CCDownloadRequest<T> extends CCRequest<T, CCDownloadRequest<T>> {

    //进度回调
    //private CCDownloadProgressCallback ccDownloadProgressCallback;
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

    private CCDownloadFileWritterCallback ccDownloadFileWritterCallback;

    private final int DEFAULT_BUFFER_SIZE = 8 * 1024;

    public CCDownloadRequest(String url, CCNetApiService apiService) {
        super(url, apiService);
    }

    @Override
    protected Flowable<CCBaseResponse<T>> getRequestFlowable() {

        uiCallbackHandler = new Handler(Looper.getMainLooper());

        return Flowable.create(new FlowableOnSubscribe<Call<ResponseBody>>() {
            @Override
            public void subscribe(FlowableEmitter<Call<ResponseBody>> e) throws Exception {
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

                Call<ResponseBody> call = getCCNetApiService().executeDownload(CCNetUtil.regexApiUrlWithPathParam(getApiUrl(), getPathMap()), getHeaderMap(), getParamMap());

                e.onNext(call);
                e.onComplete();
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(new Function<Call<ResponseBody>, Publisher<CCBaseResponse<T>>>() {
                    @Override
                    public Publisher<CCBaseResponse<T>> apply(Call<ResponseBody> responseBodyCall) throws Exception {

                        Response<ResponseBody> retrofitResponse;
                        Headers headers = null;
                        try {

                            retrofitResponse = responseBodyCall.clone().execute();

                            headers = retrofitResponse.headers();

                            onFileSaveCheck(CCNetUtil.isHttpSupportRange(headers) && isSupportRage());

                            String contentLengthStr = CCNetUtil.getHeader("Content-Length", headers);

                            if (TextUtils.isEmpty(contentLengthStr)) {
                                throw new NoResponseBodyDataException("no file data");
                            } else {
                                long contentLengthLong = convertStringToLong(contentLengthStr);

                                if (contentLengthLong > SDCardUtil.getSDCardAvailableSize() * 1024 * 1024) {
                                    throw new NoEnoughSpaceException("write failed: ENOSPC (No space left on device)");
                                }

                                if (contentLengthLong == 0) {
                                    throw new NoResponseBodyDataException("no file data");
                                }
                            }

                            if (getCcDownloadFileWritterCallback() != null) {
                                getCcDownloadFileWritterCallback().onWriteToDisk(retrofitResponse.body(), headers, getCcNetCallback());
                            } else {
                                onWriteToDisk(retrofitResponse.body());
                            }

                        } catch (NoResponseBodyDataException nrbde) {
                            throw nrbde;
                        } catch (Exception exception) {
                            throw new CCUnExpectedException(exception);
                        }

                        return Flowable.just(new CCBaseResponse<T>(null, headers, false, false, true, null));
                    }
                }).retryWhen(new FlowableRetryWithDelay(getRetryCount(), getRetryDelayTimeMillis())).onBackpressureLatest();
    }

    private long convertStringToLong(String source) {
        long result = 0;
        try {
            result = Long.parseLong(source);
        } catch (Exception e) {

        }
        return result;
    }

    @Override
    protected int getHttpMethod() {
        return CCHttpMethod.GET;
    }

    @Override
    public int getCacheQueryMode() {
        return CCCMode.QueryMode.MODE_NET;
    }

    @Override
    public int getCacheSaveMode() {
        return CCCMode.SaveMode.MODE_NONE;
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
                throw new NoResponseBodyDataException("okhttp3.ResponseBody == null, there is no data!");
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

                    if (getCcNetCallback() != null) {

                        getCcNetCallback().onProgressSave(getReqTag(), downloadedProgress, downloadNetworkSpeed, downloadedSize, fileSize);

                        requireNonNullUICallbackHandler();

                        uiCallbackHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                getCcNetCallback().onProgress(getReqTag(), downloadedProgress, downloadNetworkSpeed, downloadedSize, fileSize);
                            }
                        });
                    }
                }
            }
        } catch (Exception exception) {
            if ((exception instanceof java.io.InterruptedIOException) && isForceCanceled()) {
                NetLogUtil.printLog("e", "CCDownloadRequest", "当前异常类型是java.io.InterruptedIOException,且是强制中断,为用户主动取消网络传输");
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
            NetLogUtil.printLog("e", getClass().getCanonicalName(), "发生异常", e);
        }
    }

    private void onCloseOutputStream(OutputStream outputStream) {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Exception e) {
            NetLogUtil.printLog("e", getClass().getCanonicalName(), "发生异常", e);
        }
    }

    private void onCloseRandomAccessFile(RandomAccessFile rafFile) {
        try {
            if (rafFile != null) {
                rafFile.close();
            }
        } catch (Exception e) {
            NetLogUtil.printLog("e", getClass().getCanonicalName(), "发生异常", e);
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
            NetLogUtil.printLog("e", getClass().getCanonicalName(), "发生异常", exception);
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

    public CCDownloadFileWritterCallback getCcDownloadFileWritterCallback() {
        return ccDownloadFileWritterCallback;
    }

    /**
     * 设置自定义的文件本地写入回调
     *
     * @param ccDownloadFileWritterCallback
     * @return
     */
    public CCDownloadRequest<T> setCcDownloadFileWritterCallback(CCDownloadFileWritterCallback ccDownloadFileWritterCallback) {
        this.ccDownloadFileWritterCallback = ccDownloadFileWritterCallback;
        return this;
    }
}
