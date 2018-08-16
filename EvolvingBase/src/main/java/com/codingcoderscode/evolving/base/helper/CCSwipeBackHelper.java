package com.codingcoderscode.evolving.base.helper;

import android.app.Application;
import android.view.View;

import java.util.List;

import cn.bingoogolapple.swipebacklayout.BGASwipeBackHelper;

/**
 * Date：2018/8/16 14:09
 * <p>
 * author: CodingCodersCode
 * <p>
 * 描述:「必须在 Application 的 onCreate 方法中执行 BGASwipeBackHelper.init 来初始化滑动返回」，使用方法同BGASwipeBackHelper
 */

public class CCSwipeBackHelper {
    public static void init(Application application, List<Class<? extends View>> problemViewClassList) {
        /**
         * 必须在 Application 的 onCreate 方法中执行 BGASwipeBackHelper.init 来初始化滑动返回
         * 第一个参数：应用程序上下文
         * 第二个参数：如果发现滑动返回后立即触摸界面时应用崩溃，请把该界面里比较特殊的 View 的 class 添加到该集合中，目前在库中已经添加了 WebView 和 SurfaceView
         */
        BGASwipeBackHelper.init(application, problemViewClassList);
    }
}
