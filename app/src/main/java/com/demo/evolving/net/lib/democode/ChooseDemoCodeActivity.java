package com.demo.evolving.net.lib.democode;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.codingcoderscode.evolving.base.CCBaseRxAppCompactActivity;
import com.demo.evolving.net.lib.R;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by CodingCodersCode on 2017/11/30.
 */

public class ChooseDemoCodeActivity extends CCBaseRxAppCompactActivity implements View.OnClickListener {

    private final String LOG_TAG = getClass().getCanonicalName();

    private TextView tv_btn_1;
    private TextView tv_btn_2;
    private TextView tv_btn_3;
    private TextView tv_btn_4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_demo_code);

        initView();
    }

    private void initView(){
        this.tv_btn_1 = (TextView)findViewById(R.id.tv_btn_1);
        this.tv_btn_1.setOnClickListener(this);

        this.tv_btn_2 = (TextView)findViewById(R.id.tv_btn_2);
        this.tv_btn_2.setOnClickListener(this);

        this.tv_btn_3 = (TextView)findViewById(R.id.tv_btn_3);
        this.tv_btn_3.setOnClickListener(this);

        this.tv_btn_4 = (TextView)findViewById(R.id.tv_btn_4);
        this.tv_btn_4.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_btn_1:
                openOrdinaryRequestActivity();
                break;
            case R.id.tv_btn_2:
                openSingleDownloadRequestActivity();
                break;
            case R.id.tv_btn_3:
                openMultiDownloadRequestActivity();
                break;
            case R.id.tv_btn_4:
                openUploadRequestActivity();
                break;
        }
    }

    /**
     * 普通类型请求demo展示
     */
    private void openOrdinaryRequestActivity(){
        Intent intent = new Intent(this, OrdinaryRequestActivity.class);
        this.startActivity(intent);
    }

    /**
     * 单文件下载请求demo展示
     */
    private void openSingleDownloadRequestActivity(){
        Intent intent = new Intent(this, SingleDownloadRequestActivity.class);
        this.startActivity(intent);
    }

    /**
     * 多文件下载请求demo展示
     */
    private void openMultiDownloadRequestActivity(){
        Intent intent = new Intent(this, MultiDownloadRequestActivity.class);
        this.startActivity(intent);
    }

    /**
     * 文件上传请求demo展示
     */
    private void openUploadRequestActivity(){
        Intent intent = new Intent(this, UploadRequestActivity.class);
        this.startActivity(intent);
    }
}
