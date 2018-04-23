package com.codingcoderscode.evolving.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codingcoderscode.evolving.base.interfaces.OnFragmentVisibilityChangedListener;


/**
 * Created by CodingCodersCode on 2017/10/16.
 * <p>
 * 懒加载Fragment基类
 */

public abstract class CCBaseLazyRxFragment extends CCBaseRxFragment implements View.OnAttachStateChangeListener, OnFragmentVisibilityChangedListener {

    private boolean isFirstVisible = true;
    private boolean isFirstInvisible = true;

// ***************************************************************************************************
// **                                                                                               **
// **                        Fragment懒加载，及显示 or 隐藏状态判断相关功能代码块                        **
// **                                                                                               **
// ***************************************************************************************************

    //Parent Activity是否可见
    private boolean mParentActivityVisible = false;
    //是否可见(Activity处于前台、Tab被选中、Fragment被添加、Fragment没有隐藏、Fragment.View已经Attach)
    protected boolean mVisible = false;

    private CCBaseLazyRxFragment mParentFragment;

    private OnFragmentVisibilityChangedListener mListener;

    public void setOnVisibilityChangedListener(OnFragmentVisibilityChangedListener listener) {
        mListener = listener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        final Fragment parentFragment = getParentFragment();
        if (parentFragment != null && parentFragment instanceof CCBaseLazyRxFragment) {
            mParentFragment = ((CCBaseLazyRxFragment) parentFragment);
            mParentFragment.setOnVisibilityChangedListener(this);
        }
        checkVisibility(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getFragmentView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        onActivityVisibilityChanged(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        onActivityVisibilityChanged(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onDetach() {
        if (mParentFragment != null) {
            mParentFragment.setOnVisibilityChangedListener(null);
        }
        super.onDetach();
        checkVisibility(false);
        mParentFragment = null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.addOnAttachStateChangeListener(this);
        onInitAfterFragmentOnViewCreated(view, savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        checkVisibility(!hidden);
    }

    /**
     * Tab切换时会回调此方法。对于没有Tab的页面，{@link Fragment#getUserVisibleHint()}默认为true。
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        checkVisibility(isVisibleToUser);
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        checkVisibility(true);
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        v.removeOnAttachStateChangeListener(this);
        checkVisibility(false);
    }

    /**
     * ParentActivity可见性改变
     */
    protected void onActivityVisibilityChanged(boolean visible) {
        mParentActivityVisible = visible;
        checkVisibility(visible);
    }

    /**
     * ParentFragment可见性改变
     */
    @Override
    public void onFragmentVisibilityChanged(boolean visible) {
        checkVisibility(visible);
    }

    /**
     * 检查可见性是否变化
     *
     * @param expected 可见性期望的值。只有当前值和expected不同，才需要做判断
     */
    private void checkVisibility(boolean expected) {
        if (expected == mVisible) return;
        final boolean parentVisible = mParentFragment == null ? mParentActivityVisible : mParentFragment.isFragmentVisible();
        final boolean superVisible = super.isVisible();
        final boolean hintVisible = getUserVisibleHint();
        final boolean visible = parentVisible && superVisible && hintVisible;

        if (visible != mVisible) {
            mVisible = visible;
            onVisibilityChanged(mVisible);
        }
    }

    /**
     * 可见性改变
     */
    protected void onVisibilityChanged(boolean visible) {
        if (mListener == null) {
            mListener = new OnFragVisibilityChangeListener(this);
        }

        mListener.onFragmentVisibilityChanged(visible);
    }

    /**
     * 是否可见（Activity处于前台、Tab被选中、Fragment被添加、Fragment没有隐藏、Fragment.View已经Attach）
     */
    public boolean isFragmentVisible() {
        return mVisible;
    }

    // *******************************************************************************************
    // *******************************************************************************************
    //
    // 重要说明：
    //
    //         （1） onFirstUserVisible()：表示第一次对用户可见
    //         （2） onUserVisible()：表示对用户可见
    //          (3) onFirstUserInvisible()：表示第一次对用户不可见
    //          (4) onUserInvisible()：表示对用户不可见
    //
    // 调用情况说明：
    //              (1)、第一次可见时，调用顺序：(1)->(2)
    //              (2)、非第一次可见时，调用顺序：(2)
    //              (3)、第一次不可见时，调用顺序：(3)->(4)
    //              (4)、非第一次不可见时，调用顺序：(4)
    // 说明：
    //              以上状态方法，及调用情况，仅指Fragment显示阶段的状态，不包含准备初期的状态
    //              Fragment中多层嵌套ViewPager或Fragment的情况暂未测试，不保证正确性
    //
    // 待后续逐步完善
    //
    //*******************************************************************************************
    //*******************************************************************************************

    /**
     * 第一次对用户可见
     */
    protected void onFirstUserVisible() {

    }

    /**
     * 对用户可见
     */
    protected void onUserVisible() {

    }


    /**
     * 第一次对用户不可见
     */
    private void onFirstUserInvisible() {

    }

    /**
     * 对用户不可见
     */
    protected void onUserInvisible() {

    }

    // *******************************************************************************************
    // *******************************************************************************************

    /**
     * 获取Fragment展示的视图
     *
     * @param inflater           {@link LayoutInflater}
     * @param container          {@link ViewGroup}
     * @param savedInstanceState {@link Bundle}
     * @return Fragment要展示的View类型视图对象
     */
    public abstract View getFragmentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    /**
     * 在Fragment的onViewCreated方法中被调用，用于在子类中初始化相关控件和设置
     *
     * @param view  view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState    savedInstanceState If non-null, this fragment is being re-constructed
     */
    protected void onInitAfterFragmentOnViewCreated(View view, @Nullable Bundle savedInstanceState) {

    }

    /**
     * Fragment显示or隐藏状态改变监听器
     */
    private static class OnFragVisibilityChangeListener implements OnFragmentVisibilityChangedListener {

        private CCBaseLazyRxFragment lazyFragment;

        private OnFragVisibilityChangeListener(CCBaseLazyRxFragment lazyFragment) {
            this.lazyFragment = lazyFragment;
        }

        @Override
        public void onFragmentVisibilityChanged(boolean visibility) {
            try {

                if (visibility) {

                    if (lazyFragment.isFirstVisible) {
                        lazyFragment.isFirstVisible = false;
                        lazyFragment.onFirstUserVisible();
                    }
                    lazyFragment.onUserVisible();

                } else {

                    if (lazyFragment.isFirstInvisible) {
                        lazyFragment.isFirstInvisible = false;
                        lazyFragment.onFirstUserInvisible();
                    }
                    lazyFragment.onUserInvisible();
                }

            } catch (Exception e) {

            }
        }
    }


}
