package com.codingcoderscode.evolving.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.codingcoderscode.evolving.base.util.NetLogUtil;

import cn.bingoogolapple.swipebacklayout.BGASwipeBackHelper;

/**
 * Date：2018/8/16 11:33
 * <p>
 * author: ghc
 */
public class CCBaseRxSwipeBackAppCompactActivity extends CCBaseRxAppCompactActivity implements BGASwipeBackHelper.Delegate {

    protected BGASwipeBackHelper mSwipeBackHelper;
    protected Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 「必须在 Application 的 onCreate 方法中执行 BGASwipeBackHelper.init 来初始化滑动返回」
        // 在 super.onCreate(savedInstanceState) 之前调用该方法
        initSwipeBackFinish();
        super.onCreate(savedInstanceState);
    }

    /**
     * 是否支持滑动返回。这里在父类中默认返回 true 来支持滑动返回，如果某个界面不想支持滑动返回则重写该方法返回 false 即可
     *
     * @return
     */
    @Override
    public boolean isSupportSwipeBack() {
        return true;
    }

    /**
     * 正在滑动返回
     *
     * @param slideOffset 从 0 到 1
     */
    @Override
    public void onSwipeBackLayoutSlide(float slideOffset) {

    }

    /**
     * 没达到滑动返回的阈值，取消滑动返回动作，回到默认状态
     */
    @Override
    public void onSwipeBackLayoutCancel() {

    }

    /**
     * 滑动返回执行完毕，销毁当前 Activity
     */
    @Override
    public void onSwipeBackLayoutExecuted() {
        try {
            this.mSwipeBackHelper.swipeBackward();
        } catch (Exception e) {
            NetLogUtil.printLog("e", getClass().getCanonicalName(), "调用onSwipeBackLayoutExecuted方法发生异常", e);
        }
    }

    @Override
    public void onBackPressed() {
        try {
            // 正在滑动返回的时候取消返回按钮事件
            if (this.mSwipeBackHelper.isSliding()) {
                return;
            }
            this.mSwipeBackHelper.backward();
        } catch (Exception e) {
            NetLogUtil.printLog("e", getClass().getCanonicalName(), "调用onBackPressed方法发生异常", e);
        }
    }

    /**
     * 初始化滑动返回。在 super.onCreate(savedInstanceState) 之前调用该方法
     */
    protected void initSwipeBackFinish() {
        try {
            this.mSwipeBackHelper = new BGASwipeBackHelper(this, this);

            // 「必须在 Application 的 onCreate 方法中执行 BGASwipeBackHelper.init 来初始化滑动返回」
            // 下面几项可以不配置，这里只是为了讲述接口用法。

            // 设置滑动返回是否可用。默认值为 true
            this.mSwipeBackHelper.setSwipeBackEnable(isSupportSwipeBack());
            // 设置是否仅仅跟踪左侧边缘的滑动返回。默认值为 true
            this.mSwipeBackHelper.setIsOnlyTrackingLeftEdge(isOnlyTrackingLeftEdge());
            // 设置是否是微信滑动返回样式。默认值为 true
            this.mSwipeBackHelper.setIsWeChatStyle(isWeChatStyle());
            // 设置阴影资源 id。默认值为 R.drawable.bga_sbl_shadow
            this.mSwipeBackHelper.setShadowResId(getShadowResId());
            // 设置是否显示滑动返回的阴影效果。默认值为 true
            this.mSwipeBackHelper.setIsNeedShowShadow(isNeedShowShadow());
            // 设置阴影区域的透明度是否根据滑动的距离渐变。默认值为 true
            this.mSwipeBackHelper.setIsShadowAlphaGradient(isShadowAlphaGradient());
            // 设置触发释放后自动滑动返回的阈值，默认值为 0.3f
            this.mSwipeBackHelper.setSwipeBackThreshold(getSwipeBackThreshold());
            // 设置底部导航条是否悬浮在内容上，默认值为 false
            this.mSwipeBackHelper.setIsNavigationBarOverlap(isNavigationBarOverlap());
        } catch (Exception e) {
            NetLogUtil.printLog("e", getClass().getCanonicalName(), "调用initSwipeBackFinish方法发生异常", e);
        }
    }

    /**
     * 是否仅仅跟踪左侧边缘的滑动返回
     *
     * @return
     */
    protected boolean isOnlyTrackingLeftEdge() {
        return true;
    }

    /**
     * 是否是微信滑动返回样式
     *
     * @return
     */
    protected boolean isWeChatStyle() {
        return true;
    }

    /**
     * 获取阴影资源
     *
     * @return
     */
    protected int getShadowResId() {
        return R.drawable.bga_sbl_shadow;
    }

    /**
     * 是否显示滑动返回的阴影效果
     *
     * @return
     */
    protected boolean isNeedShowShadow() {
        return true;
    }

    /**
     * 阴影区域的透明度是否根据滑动的距离渐变
     *
     * @return
     */
    protected boolean isShadowAlphaGradient() {
        return true;
    }

    /**
     * 触发释放后自动滑动返回的阈值
     *
     * @return
     */
    protected float getSwipeBackThreshold() {
        return 0.3f;
    }

    /**
     * 底部导航条是否悬浮在内容上
     *
     * @return
     */
    protected boolean isNavigationBarOverlap() {
        return false;
    }
}
