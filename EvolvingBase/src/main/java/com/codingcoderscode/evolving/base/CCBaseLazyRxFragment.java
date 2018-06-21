package com.codingcoderscode.evolving.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.codingcoderscode.evolving.base.interfaces.OnFragmentVisibilityChangedListener;

/**
 * Date：2018/6/18 10:40
 * <p>
 * author: CodingCodersCode
 * <p>
 * 懒加载Fragment基类
 */
public abstract class CCBaseLazyRxFragment extends CCBaseRxFragment implements View.OnAttachStateChangeListener, OnFragmentVisibilityChangedListener {

    private String LOG_TAG = getClass().getCanonicalName();

// ***************************************************************************************************
// **                                                                                               **
// **                        Fragment懒加载，及显示 or 隐藏状态判断相关变量代码块                        **
// **                                                                                               **
// ***************************************************************************************************

    private boolean isFirstVisible = true;
    private boolean isFirstInvisible = true;

    /**
     * V2版本(当前版本)判断逻辑
     */
    private Bundle mSavedInstanceState;

    /**
     * 标识客户端是否真正初始化了视图，通过调用{@link #getFragmentView}
     */
    private boolean mIsRealViewSetup;

    private View mRootView;

    /**
     * 是否已经调用了初始化View方法
     */
    private boolean mIsCalledOnCreateViewMethod = false;

    private boolean originVisibleOfUserHint = false;

    /**
     * 记录当前Fragment可见状态
     */
    protected boolean mCurrentFragmentVisibility = false;

    /**
     * V1版本判断逻辑
     */
    //Parent Activity是否可见
    private boolean mParentActivityVisible = false;
    //是否可见(Activity处于前台、Tab被选中、Fragment被添加、Fragment没有隐藏、Fragment.View已经Attach)
    protected boolean mVisible = false;

    private CCBaseLazyRxFragment mParentFragment;

    private OnFragmentVisibilityChangedListener mListener;

// ***************************************************************************************************

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
        return requireFragmentView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @CallSuper
    @Override
    public void onStart() {
        super.onStart();

        this.onStartMethodCalled();

        onActivityVisibilityChanged(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @CallSuper
    @Override
    public void onStop() {
        super.onStop();

        this.onStopMethodCalled();

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

        this.onSetUserVisibleHintMethodCalled(isVisibleToUser);

        checkVisibility(isVisibleToUser);
    }

    /**
     * V1版本判断逻辑
     *
     * @param v
     */
    @Override
    public void onViewAttachedToWindow(View v) {
        checkVisibility(true);
    }

    /**
     * V1版本判断逻辑
     *
     * @param v
     */
    @Override
    public void onViewDetachedFromWindow(View v) {
        v.removeOnAttachStateChangeListener(this);
        checkVisibility(false);
    }

    /**
     * ParentFragment可见性改变
     */
    @Override
    public void onParentFragmentVisibilityChanged(boolean visible) {
        checkVisibility(visible);
    }

// ***************************************************************************************************
// **                                                                                               **
// **                        Fragment懒加载，及显示 or 隐藏状态判断相关功能代码块                        **
// **                                                                                               **
// ***************************************************************************************************

    /**
     * ParentActivity可见性改变
     * <p>
     * V1版本判断逻辑
     */
    protected void onActivityVisibilityChanged(boolean visible) {
        mParentActivityVisible = visible;
        checkVisibility(visible);
    }

    /**
     * 检查可见性是否变化
     * <p>
     * V1版本判断逻辑
     *
     * @param expected 可见性期望的值。只有当前值和expected不同，才需要做判断
     */
    private void checkVisibility(boolean expected) {
        if (expected == this.mVisible) {
            return;
        }
        final boolean parentVisible = mParentFragment == null ? mParentActivityVisible : mParentFragment.isFragmentVisible();
        final boolean superVisible = super.isVisible();
        final boolean hintVisible = getUserVisibleHint();
        final boolean visible = parentVisible && superVisible && hintVisible;

        if (visible != mVisible) {
            mVisible = visible;
            onFragmentVisibilityChanged(mVisible);
        }
    }

    /**
     * 获取Fragment视图
     * <p>
     * V2版本判断逻辑
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    protected View requireFragmentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (!this.isViewLazyLoadEnable() || originVisibleOfUserHint) {//非懒加载
            this.mRootView = getFragmentView(inflater, container, savedInstanceState);
            this.mIsRealViewSetup = true;
        } else {
            this.mRootView = new FrameLayout(getContext());
        }

        this.mSavedInstanceState = savedInstanceState;
        this.mIsCalledOnCreateViewMethod = true;

        return this.mRootView;
    }

    /**
     * 由方法{@link #onStart()}调用
     * <p>
     * V2版本判断逻辑
     */
    protected void onStartMethodCalled() {
        if (this.mIsRealViewSetup && this.originVisibleOfUserHint) {
            this.onFragmentVisibilityChanged(true, true);
        }
    }

    /**
     * 由方法{@link #onStop()}调用
     * <p>
     * V2版本判断逻辑
     */
    protected void onStopMethodCalled() {
        if (this.mIsRealViewSetup && this.originVisibleOfUserHint) {
            this.onFragmentVisibilityChanged(false, true);
        }
    }

    /**
     * 由方法{@link #setUserVisibleHint(boolean)}调用
     * <p>
     * V2版本判断逻辑
     *
     * @param isVisibleToUser
     */
    protected void onSetUserVisibleHintMethodCalled(boolean isVisibleToUser) {
        ViewGroup rootView;
        View contentView;
        try {
            this.originVisibleOfUserHint = getUserVisibleHint();

            if (this.mRootView == null) {
                return;
            }

            if (this.isViewLazyLoadEnable() && isVisibleToUser && this.mIsCalledOnCreateViewMethod && !this.mIsRealViewSetup) {
                rootView = (ViewGroup) this.mRootView;
                rootView.removeAllViews();

                contentView = getFragmentView(LayoutInflater.from(getContext()), rootView, this.mSavedInstanceState);
                rootView.addView(contentView);

                this.mIsRealViewSetup = true;
            }

            if (this.mIsRealViewSetup) {
                this.onFragmentVisibilityChanged(isVisibleToUser, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fragment可见性变化时回调
     * <p>
     * V2版本判断逻辑
     *
     * @param isVisibleToUser 当前Fragment是否前台可见
     * @param isLifeCycle     当前Fragment可见性是否由Fragment生命周期变化引起 false：由调用{@link #setUserVisibleHint(boolean)}引起
     */
    @CallSuper
    private void onFragmentVisibilityChanged(boolean isVisibleToUser, boolean isLifeCycle) {
        Fragment parentFragment;
        boolean fragmentVisible = false;
        ViewPager viewPager;
        try {


            parentFragment = getParentFragment();

            if (parentFragment instanceof CCBaseLazyRxFragment) {
                //处理View非懒加载时有二级Fragment ViewPager情况，第一次初始化时的问题，这种情况下，一级Fragment不可见，但二级ViewPager中Fragment初始化后
                //会自动设置二级Fragment的可见性。
                fragmentVisible = ((CCBaseLazyRxFragment) parentFragment).isCurrentFragmentVisibility();
                if (!fragmentVisible && isLifeCycle) {
                    return;
                }
            }

            if (this.mCurrentFragmentVisibility != isVisibleToUser) {
                this.mCurrentFragmentVisibility = isVisibleToUser;

                //处理当前Fragment可见性变化
                this.onFragmentVisibilityChanged(isVisibleToUser);

                //处理嵌套ViewPager情况下的Fragment可见性变化
                viewPager = this.setNestedViewPagerWithNestedFragment();
                if (viewPager != null) {
                    this.handleNestedFragmentVisibilityWhenFragmentVisibilityChanged(viewPager, isVisibleToUser, isLifeCycle);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理在内外层ViewPager里的Fragment初始化后引起Fragment可见性不一致问题（尤其开启了View懒加载后，子ViewPager并未处理外层Fragment可见性）
     * <p>
     * 这里的处理是：
     * 1.外层Fragment不可见时，它内部的所有Fragment都应该不可见
     * 2.内部Fragment可见时，他所关联的父Fragment也应该可见
     * <p>
     * V2版本判断逻辑
     *
     * @param viewPager
     * @param isVisible
     * @param isLifeCycle
     */
    private void handleNestedFragmentVisibilityWhenFragmentVisibilityChanged(final ViewPager viewPager, boolean isVisible, boolean isLifeCycle) {
        FragmentPagerAdapter adapter;
        int size;
        Fragment fragment;
        try {
            if (viewPager == null || isLifeCycle) {
                return;
            }

            adapter = ((FragmentPagerAdapter) viewPager.getAdapter());
            if (adapter != null) {
                if (!isVisible) {
                    //不可见的情况下，子ViewPager里的所有Fragment都不应该可见
                    size = adapter.getCount();
                    for (int i = 0; i < size; i++) {
                        fragment = adapter.getItem(i);
                        if (fragment == null) {
                            return;
                        }

                        fragment.setUserVisibleHint(isVisible);
                    }
                } else {
                    fragment = adapter.getItem(viewPager.getCurrentItem());
                    if (fragment != null) {
                        fragment.setUserVisibleHint(isVisible);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean isInRealVisibleToUserStatus = false;

    /**
     * Fragment可见性变化时回调
     * <p>
     * Fragment可见时该方法会回调一次，不可见时保证至少调用一次
     * <p>
     * V2版本判断逻辑
     *
     * @param isVisible
     */
    protected void onFragmentVisibilityChanged(boolean isVisible) {
        //if (isVisible != this.isCurrentFragmentVisibility()) {
        if (isVisible) {
            if (this.isFirstVisible) {
                this.isFirstVisible = false;
                this.onFirstUserVisible();
            }

            if (!isInRealVisibleToUserStatus) {
                isInRealVisibleToUserStatus = isVisible;
                this.onUserVisible();
            }
        } else {
            if (this.isFirstInvisible) {
                this.isFirstInvisible = false;
                this.onFirstUserInvisible();
            }

            if (isInRealVisibleToUserStatus) {
                isInRealVisibleToUserStatus = isVisible;
                this.onUserInvisible();
            }
        }
        // }

        /*if (this.mListener != null){
            this.mListener.onParentFragmentVisibilityChanged(isVisible);
        }*/
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
     * 适用于外层是ViewPager，ViewPager中某个Fragment中也用了ViewPager时，用来设置嵌套的ViewPager
     * <p>
     * V2版本判断逻辑
     *
     * @return
     */
    protected ViewPager setNestedViewPagerWithNestedFragment() {
        return null;
    }

    /**
     * 是否开启View的懒加载模式，非数据的懒加载
     * <p>
     * V2版本判断逻辑
     *
     * @return
     */
    protected boolean isViewLazyLoadEnable() {
        return false;
    }


    /**
     * 在Fragment的onViewCreated方法中被调用，用于在子类中初始化相关控件和设置
     *
     * @param view               view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState savedInstanceState If non-null, this fragment is being re-constructed
     */
    protected void onInitAfterFragmentOnViewCreated(View view, @Nullable Bundle savedInstanceState) {

    }


    /**
     * 当前Fragment是否可见
     * <p>
     * V2版本判断逻辑
     *
     * @return
     */
    public boolean isCurrentFragmentVisibility() {
        return mCurrentFragmentVisibility;
    }

    /**
     * V1版本判断逻辑
     * <p>
     * 是否可见（Activity处于前台、Tab被选中、Fragment被添加、Fragment没有隐藏、Fragment.View已经Attach）
     */
    public boolean isFragmentVisible() {
        return mVisible;
    }

    /**
     * V1版本判断逻辑
     *
     * @param listener
     */
    public void setOnVisibilityChangedListener(OnFragmentVisibilityChangedListener listener) {
        mListener = listener;
    }

}
