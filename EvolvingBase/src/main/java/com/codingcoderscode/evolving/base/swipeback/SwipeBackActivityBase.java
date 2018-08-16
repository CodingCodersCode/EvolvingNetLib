package com.codingcoderscode.evolving.base.swipeback;

/**
 * Date：2018/8/14 15:28
 * <p>
 * author: CodingCodersCode
 */
@Deprecated
public interface SwipeBackActivityBase {
    /**
     * @return the SwipeBackLayout associated with this activity.
     */
    public abstract SwipeBackLayout getSwipeBackLayout();

    public abstract void setSwipeBackEnable(boolean enable);

    /**
     * Scroll out contentView and finish the activity
     */
    public abstract void scrollToFinishActivity();
}
