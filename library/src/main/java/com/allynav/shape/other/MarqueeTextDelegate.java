package com.allynav.shape.other;

import android.content.res.TypedArray;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import com.allynav.shape.R;

/** 根据控件的屏幕可见状态启停系统跑马灯。 */
public final class MarqueeTextDelegate {

    private final TextView mTextView;
    private final Rect mVisibleRect = new Rect();
    private final Runnable mRefreshRunnable = this::refreshSelectedState;
    private final ViewTreeObserver.OnScrollChangedListener mScrollChangedListener =
            this::scheduleRefresh;
    private final ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener =
            this::scheduleRefresh;

    private boolean mEnabled;
    private int mRepeatLimit;
    private boolean mRequireFullyVisible;
    private boolean mOriginalSingleLine;
    private int mOriginalMaxLines;
    private TextUtils.TruncateAt mOriginalEllipsize;
    private boolean mOriginalSelected;
    private boolean mConfigurationApplied;
    private boolean mObserving;
    private boolean mApplyingConfiguration;
    private ViewTreeObserver mObserver;

    public MarqueeTextDelegate(TextView textView, TypedArray typedArray) {
        mTextView = textView;
        mEnabled = typedArray.getBoolean(
                R.styleable.ShapeTextView_shape_marqueeEnable, false);
        mRepeatLimit = typedArray.getInt(
                R.styleable.ShapeTextView_shape_marqueeRepeatLimit, -1);
        mRequireFullyVisible = typedArray.getBoolean(
                R.styleable.ShapeTextView_shape_marqueeRequireFullyVisible, true);
    }

    public void initialize() {
        applyConfiguration();
    }

    public boolean isApplyingConfiguration() {
        return mApplyingConfiguration;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        if (mEnabled == enabled) {
            return;
        }
        mEnabled = enabled;
        applyConfiguration();
    }

    public int getRepeatLimit() {
        return mRepeatLimit;
    }

    public void setRepeatLimit(int repeatLimit) {
        mRepeatLimit = repeatLimit;
        if (mEnabled) {
            mTextView.setMarqueeRepeatLimit(repeatLimit);
        }
    }

    public boolean isRequireFullyVisible() {
        return mRequireFullyVisible;
    }

    public void setRequireFullyVisible(boolean requireFullyVisible) {
        mRequireFullyVisible = requireFullyVisible;
        scheduleRefresh();
    }

    public void onVisibilityChanged(boolean isVisible) {
        if (!mEnabled) {
            return;
        }
        if (!isVisible) {
            mTextView.removeCallbacks(mRefreshRunnable);
            mTextView.setSelected(false);
            return;
        }
        scheduleRefresh();
    }

    public void onAttachedToWindow() {
        startObserving();
        scheduleRefresh();
    }

    public void onDetachedFromWindow() {
        mTextView.removeCallbacks(mRefreshRunnable);
        stopObserving();
        if (mEnabled) {
            mTextView.setSelected(false);
        }
    }

    private void applyConfiguration() {
        mTextView.removeCallbacks(mRefreshRunnable);
        mApplyingConfiguration = true;
        try {
            if (!mEnabled) {
                stopObserving();
                if (!mConfigurationApplied) {
                    return;
                }
                if (mOriginalSingleLine) {
                    mTextView.setSingleLine(true);
                } else {
                    mTextView.setSingleLine(false);
                    mTextView.setMaxLines(mOriginalMaxLines);
                }
                mTextView.setEllipsize(mOriginalEllipsize);
                mTextView.setSelected(mOriginalSelected);
                mConfigurationApplied = false;
                return;
            }
            if (!mConfigurationApplied) {
                mOriginalSingleLine = mTextView.getMaxLines() == 1;
                mOriginalMaxLines = mTextView.getMaxLines();
                mOriginalEllipsize = mTextView.getEllipsize();
                mOriginalSelected = mTextView.isSelected();
                mConfigurationApplied = true;
            }
            mTextView.setSingleLine(true);
            mTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            mTextView.setMarqueeRepeatLimit(mRepeatLimit);
            startObserving();
            scheduleRefresh();
        } finally {
            mApplyingConfiguration = false;
        }
    }

    private void scheduleRefresh() {
        if (!mEnabled) {
            return;
        }
        mTextView.removeCallbacks(mRefreshRunnable);
        mTextView.postDelayed(mRefreshRunnable, 300L);
    }

    private void refreshSelectedState() {
        if (!mEnabled || !mTextView.isAttachedToWindow()
                || mTextView.getVisibility() != View.VISIBLE) {
            mTextView.setSelected(false);
            return;
        }

        boolean visible = mTextView.getGlobalVisibleRect(mVisibleRect);
        if (visible && mRequireFullyVisible) {
            visible = mVisibleRect.width() >= mTextView.getWidth() - 1
                    && mVisibleRect.height() >= mTextView.getHeight() - 1;
        }
        mTextView.setSelected(visible);
    }

    private void startObserving() {
        if (!mEnabled || mObserving || !mTextView.isAttachedToWindow()) {
            return;
        }
        mObserver = mTextView.getViewTreeObserver();
        mObserver.addOnScrollChangedListener(mScrollChangedListener);
        mObserver.addOnGlobalLayoutListener(mGlobalLayoutListener);
        mObserving = true;
    }

    private void stopObserving() {
        if (!mObserving) {
            return;
        }
        if (mObserver != null && mObserver.isAlive()) {
            mObserver.removeOnScrollChangedListener(mScrollChangedListener);
            mObserver.removeOnGlobalLayoutListener(mGlobalLayoutListener);
        }
        mObserver = null;
        mObserving = false;
    }
}
