package com.demo.evolving.net.lib;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.codingcoderscode.evolving.base.CCBaseRxAppCompactActivity;
import com.codingcoderscode.evolving.net.CCRxNetManager;
import com.codingcoderscode.evolving.net.request.callback.CCNetCallback;
import com.codingcoderscode.evolving.net.request.canceler.CCCanceler;
import com.codingcoderscode.evolving.net.util.NetLogUtil;
import com.demo.evolving.net.lib.downloadmanager.CCDownloadManager;
import com.demo.evolving.net.lib.downloadmanager.CCDownloadTask;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by CodingCodersCode on 2017/11/8.
 */

public class RepeatTestActivity extends CCBaseRxAppCompactActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {


    private static final String LOG_TAG = "RepeatTestActivity";

    private TextView tvTest;

    private TextView tvReduce;

    private TextView tvQuit;

    private TextView tvRequest;

    private TextView tvUpdateTxt;

    private TextView tvPause;

    private TextView tvResume;

    private PriorityBlockingQueue<CCDownloadTask> taskToDownload;

    private int maxThreadNum = 5;

    private AtomicInteger existTaskCount;

    private boolean quit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repeat_test);

        this.tvTest = (TextView) findViewById(R.id.tv_test);
        this.tvTest.setOnClickListener(this);

        this.tvReduce = (TextView)findViewById(R.id.tv_reduce);
        this.tvReduce.setOnClickListener(this);

        this.tvQuit = (TextView)findViewById(R.id.tv_quit);
        this.tvQuit.setOnClickListener(this);

        this.tvRequest = (TextView)findViewById(R.id.tv_request);
        this.tvRequest.setOnClickListener(this);

        this.tvUpdateTxt = (TextView)findViewById(R.id.tv_update_txt);
        this.tvUpdateTxt.setOnClickListener(this);

        this.tvPause = (TextView)findViewById(R.id.tv_pause);
        this.tvPause.setOnClickListener(this);

        this.tvResume = (TextView)findViewById(R.id.tv_resume);
        this.tvResume.setOnClickListener(this);

        existTaskCount = new AtomicInteger(0);

        NetLogUtil.setDebugAble(true);


        Map<String, String> commonHeaderMap = new HashMap<>();
        commonHeaderMap.put("common_header_param1", "common_header_value1");
        commonHeaderMap.put("common_header_param2", "common_header_value2");
        commonHeaderMap.put("common_header_param3", "common_header_value3");

        Map<String, String> specifyHeaderMap = new HashMap<>();
        specifyHeaderMap.put("specify_header_param1", "specify_header_value1");
        specifyHeaderMap.put("specify_header_param2", "specify_header_value2");
        specifyHeaderMap.put("specify_header_param3", "specify_header_value3");


        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("logic_txt_param1", "logic_txt_value1");
        paramMap.put("logic_txt_param2", "logic_txt_value2");
        paramMap.put("logic_txt_param3", "logic_txt_value3");

        Map<String, String> pathMap = new HashMap<String, String>();
        pathMap.put("{path1}", "path1_value1");
        pathMap.put("{path2}", "path1_value2");
        pathMap.put("{path3}", "path1_value3");
        pathMap.put("{path4}", "path1_value4");
        pathMap.put("{path5}", "path1_value5");


        //CCRxNetManager测试代码
        CCRxNetManager ccRxNetManager = new CCRxNetManager.Builder()
                .baseUrl("http://sw.bos.baidu.com/")
                .callAdapterFactory(RxJava2CallAdapterFactory.create())
                .converterFactory(GsonConverterFactory.create())
                .commonHeaders(commonHeaderMap)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .enableLogInterceptor(true)
                .build();



        CCDownloadManager.getInstance().setMaxTaskCount(2);
        CCDownloadManager.getInstance().setCcNetCallback(new RxNetDownloadCalback());
    }

    private int numToShow = 0;

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.tv_test:
                quit = false;
                onTestRepeat();
                break;
            case R.id.tv_reduce:
                if (existTaskCount.intValue() > 1) {
                    existTaskCount.getAndDecrement();
                    NetLogUtil.printLog("e", LOG_TAG, "点击了tv_reduce按钮，existTaskCount的值为:" + existTaskCount.intValue());
                }
                break;
            case R.id.tv_quit:
                quit = true;
                break;
            case R.id.tv_request:
                onTestSendRealRequest();
                break;
            case R.id.tv_update_txt:
                this.tvUpdateTxt.setText(String.valueOf(numToShow++));
                break;
            case R.id.tv_pause:
                CCDownloadManager.getInstance().pause(taskList.get(1));
                break;
            case R.id.tv_resume:
                CCDownloadManager.getInstance().resume(taskList.get(1));
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        //Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    private int taskKeyCount = 0;
    List<CCDownloadTask> taskList = new ArrayList<>();

    @AfterPermissionGranted(2002)
    private void onTestSendRealRequest(){

        String[] perms = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {

            taskList.clear();

            int tmpTaskCount = taskKeyCount;

            taskKeyCount += 10;

            CCDownloadTask task;

            List<CCDownloadTask> taskList2 = new ArrayList<>();

            for (; tmpTaskCount < taskKeyCount; tmpTaskCount++){

                task = new CCDownloadTask(this, "taskKey-" + tmpTaskCount, "sw-search-sp/software/16d5a98d3e034/QQ_8.9.5.22062_setup.exe", null, "QQ_download_test_file_" + tmpTaskCount + ".apk", 1, 0, 0);

                taskList.add(task);
                taskList2.add(task);
            }


            taskList.addAll(taskList2);



            CCDownloadManager.getInstance().startAll(taskList);

        }else {
            EasyPermissions.requestPermissions(this, "This operation needs access to write and read external storage.", 2002, perms);
        }









    }


    /**
     * 下载进度回调
     */
    private class RxNetDownloadCalback extends CCNetCallback {
        @Override
        public <T> void onStartRequest(Object reqTag, CCCanceler canceler) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了RxNetDownloadCalback.onStart方法，调用者reqTag=" + reqTag);
        }

        @Override
        public <T> void onRequestSuccess(Object reqTag, T response, int dataSourceMode) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了RxNetDownloadCalback.onSuccess方法，调用者reqTag=" + reqTag);
        }

        @Override
        public <T> void onRequestFail(Object reqTag, Throwable t) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了RxNetDownloadCalback.onError方法，调用者reqTag=" + reqTag, t);
        }

        @Override
        public <T> void onRequestComplete(Object reqTag) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了RxNetDownloadCalback.onComplete方法，调用者reqTag=" + reqTag);
        }

        @Override
        public <T> void onProgress(Object tag, int progress, long netSpeed, long completedSize, long fileSize) {
            NetLogUtil.printLog("e", LOG_TAG, "调用了RxNetDownloadCalback.onProgress方法，调用者tag=" + tag + ",progress=" + progress + "，netSpeed=" + netSpeed + "，completedSize=" + completedSize + "，fileSize=" + fileSize);
        }
    }














































    private int getExistTaskCount(){
        return existTaskCount.intValue();
    }

    private void onTestRepeat(){

        Flowable<Integer> flowable = Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {

                NetLogUtil.printLog("e", LOG_TAG, "调用了subscribe(FlowableEmitter<AtomicInteger> e)方法");

                while (true){

                    if (quit){
                        break;
                    }

                    if (existTaskCount.intValue() < maxThreadNum){
                        int nowValue = existTaskCount.getAndIncrement();
                        e.onNext(nowValue);

                        NetLogUtil.printLog("e", LOG_TAG, "当前existTaskCount值为" + nowValue + ",发射一个请求");
                    }else {
                        NetLogUtil.printLog("e", LOG_TAG, "当前existTaskCount值为" + existTaskCount.intValue() + ",休眠3秒");
                        Thread.sleep(3000);
                    }

                }

                e.onComplete();

            }
        }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.computation()).unsubscribeOn(Schedulers.io());

        flowable.observeOn(Schedulers.io()).flatMap(new Function<Integer, Publisher<String>>() {
            @Override
            public Publisher<String> apply(Integer integer) throws Exception {

                NetLogUtil.printLog("e", LOG_TAG, "调用了apply方法,atomicInteger.intValue()=" + integer);

                return Flowable.just("atomicInteger的值为:" + integer);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<String>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);

                NetLogUtil.printLog("e", LOG_TAG, "调用了onSubscribe方法");
            }

            @Override
            public void onNext(String s) {
                NetLogUtil.printLog("e", LOG_TAG, "调用了onNext方法,s=" + s);
            }

            @Override
            public void onError(Throwable t) {
                NetLogUtil.printLog("e", LOG_TAG, "调用了oonError方法", t);
            }

            @Override
            public void onComplete() {
                NetLogUtil.printLog("e", LOG_TAG, "调用了onComplete方法");
            }
        });



    }
}
