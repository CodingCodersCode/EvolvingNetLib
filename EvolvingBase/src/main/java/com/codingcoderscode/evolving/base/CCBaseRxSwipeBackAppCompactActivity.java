package com.codingcoderscode.evolving.base;

import android.os.Bundle;
import android.view.View;

import com.codingcoderscode.evolving.base.swipeback.SwipeBackActivityBase;
import com.codingcoderscode.evolving.base.swipeback.SwipeBackActivityHelper;
import com.codingcoderscode.evolving.base.swipeback.SwipeBackLayout;
import com.codingcoderscode.evolving.base.swipeback.Utils;

/**
 * Date：2018/8/14 15:27
 * <p>
 * author: ghc
 * <p>
 * 包含功能：滑动关闭
 * <p>
 * 存在的问题：
 * 根据网文[https://blog.csdn.net/Imshuyuan/article/details/78365968]所描述，会出现onRestart方法不执行的问题。
 * <p>
 * 说明：
 * 此项目中的滑动关闭功能代码均直接源于[https://github.com/CodingCodersCode/SwipeBackLayout],间接来源于[https://github.com/ikew0ng/SwipeBackLayout]，一切使用方式和方法，均与
 * 项目[https://github.com/ikew0ng/SwipeBackLayout]相同
 * <p>
 * 因项目结构，不能直接以依赖的方式集成SwipeBackLayout，因此才采取拷贝源码方式，特此说明。
 */
public class CCBaseRxSwipeBackAppCompactActivity extends CCBaseRxAppCompactActivity implements SwipeBackActivityBase {
    private SwipeBackActivityHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate();
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v == null && mHelper != null)
            return mHelper.findViewById(id);
        return v;
    }

    @Override
    public SwipeBackLayout getSwipeBackLayout() {
        return mHelper.getSwipeBackLayout();
    }

    @Override
    public void setSwipeBackEnable(boolean enable) {
        getSwipeBackLayout().setEnableGesture(enable);
    }

    @Override
    public void scrollToFinishActivity() {
        Utils.convertActivityToTranslucent(this);
        getSwipeBackLayout().scrollToFinishActivity();
    }
}
