package com.allynav.shape.other;

import android.content.res.TypedArray;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.allynav.shape.R;

/**
 * 固定高度不足时，通过压缩行间距或减少最大行数保证文本区域完整显示。
 * 功能行为参考：https://github.com/AndrewSuan/AdaptiveTextView
 */
public final class AdaptiveTextDelegate {

    public static final int MODE_REDUCE_LINES = 1;
    public static final int MODE_REDUCE_LINE_SPACING = 2;
    public static final int MODE_REDUCE_LINE_SPACING_THEN_LINES = 3;

    private final TextView mTextView;

    private boolean mEnabled;
    private int mMode;
    private int mMinLines;
    private float mMinLineSpacingExtra;

    private int mConfiguredMaxLines;
    private float mConfiguredLineSpacingExtra;
    private float mConfiguredLineSpacingMultiplier;

    private boolean mApplying;
    private boolean mResetPending;
    private int mLastAvailableWidth;
    private int mLastAvailableHeight;

    public AdaptiveTextDelegate(TextView textView, TypedArray typedArray) {
        mTextView = textView;
        mConfiguredMaxLines = textView.getMaxLines();
        mConfiguredLineSpacingExtra = textView.getLineSpacingExtra();
        mConfiguredLineSpacingMultiplier = textView.getLineSpacingMultiplier();

        mEnabled = typedArray.getBoolean(
                R.styleable.ShapeTextView_shape_adaptiveTextEnable, false);
        mMode = typedArray.getInt(
                R.styleable.ShapeTextView_shape_adaptiveTextMode, MODE_REDUCE_LINES);
        mMinLines = Math.max(1, typedArray.getInt(
                R.styleable.ShapeTextView_shape_adaptiveMinLines, 1));
        mMinLineSpacingExtra = typedArray.hasValue(
                R.styleable.ShapeTextView_shape_adaptiveMinLineSpacingExtra)
                ? typedArray.getDimension(
                        R.styleable.ShapeTextView_shape_adaptiveMinLineSpacingExtra, 0f)
                : Float.NaN;
    }

    public void beforeMeasure() {
        if (!mEnabled || !mResetPending) {
            return;
        }
        restoreConfiguredValues();
        mResetPending = false;
    }

    public void afterLayout() {
        if (!mEnabled || mApplying || mTextView.getVisibility() != View.VISIBLE) {
            return;
        }

        ViewGroup.LayoutParams layoutParams = mTextView.getLayoutParams();
        if (layoutParams == null || layoutParams.height <= 0
                || mTextView.getMaxLines() == Integer.MAX_VALUE) {
            return;
        }

        int availableWidth = mTextView.getWidth()
                - mTextView.getCompoundPaddingLeft()
                - mTextView.getCompoundPaddingRight();
        int availableHeight = mTextView.getHeight()
                - mTextView.getCompoundPaddingTop()
                - mTextView.getCompoundPaddingBottom();
        if (mLastAvailableWidth > 0
                && (mLastAvailableWidth != availableWidth
                || mLastAvailableHeight != availableHeight)) {
            mLastAvailableWidth = availableWidth;
            mLastAvailableHeight = availableHeight;
            restoreConfiguredValues();
            mTextView.requestLayout();
            return;
        }
        mLastAvailableWidth = availableWidth;
        mLastAvailableHeight = availableHeight;

        Layout layout = mTextView.getLayout();
        if (layout == null) {
            return;
        }

        int lineCount = layout.getLineCount();
        if (lineCount <= 0 || availableHeight <= 0) {
            return;
        }

        int contentHeight = layout.getLineBottom(lineCount - 1) - layout.getLineTop(0);
        int overflow = contentHeight - availableHeight;
        if (overflow <= 0) {
            return;
        }

        if (mMode != MODE_REDUCE_LINES
                && reduceLineSpacing(layout, lineCount, overflow)) {
            return;
        }

        if (mMode != MODE_REDUCE_LINE_SPACING) {
            reduceMaxLines();
        }
    }

    public void onContentMetricsChanged() {
        if (mEnabled && !mApplying) {
            mResetPending = true;
            mTextView.requestLayout();
        }
    }

    public void onMaxLinesChanged(int maxLines) {
        if (mApplying) {
            return;
        }
        mConfiguredMaxLines = maxLines;
        onContentMetricsChanged();
    }

    public void onLineSpacingChanged(float add, float multiplier) {
        if (mApplying) {
            return;
        }
        mConfiguredLineSpacingExtra = add;
        mConfiguredLineSpacingMultiplier = multiplier;
        onContentMetricsChanged();
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public boolean isApplying() {
        return mApplying;
    }

    public void setEnabled(boolean enabled) {
        if (mEnabled == enabled) {
            return;
        }
        mEnabled = enabled;
        if (enabled) {
            mResetPending = true;
        } else {
            restoreConfiguredValues();
            mResetPending = false;
        }
        mTextView.requestLayout();
        mTextView.invalidate();
    }

    public int getMode() {
        return mMode;
    }

    public void setMode(int mode) {
        if (mode < MODE_REDUCE_LINES || mode > MODE_REDUCE_LINE_SPACING_THEN_LINES) {
            throw new IllegalArgumentException("不支持的自适应文本模式：" + mode);
        }
        if (mMode == mode) {
            return;
        }
        mMode = mode;
        onContentMetricsChanged();
    }

    public int getMinLines() {
        return mMinLines;
    }

    public void setMinLines(int minLines) {
        int safeMinLines = Math.max(1, minLines);
        if (mMinLines == safeMinLines) {
            return;
        }
        mMinLines = safeMinLines;
        onContentMetricsChanged();
    }

    public boolean hasMinLineSpacingExtra() {
        return !Float.isNaN(mMinLineSpacingExtra);
    }

    public float getMinLineSpacingExtra() {
        return mMinLineSpacingExtra;
    }

    public void setMinLineSpacingExtra(float minLineSpacingExtra) {
        if (Float.compare(mMinLineSpacingExtra, minLineSpacingExtra) == 0) {
            return;
        }
        mMinLineSpacingExtra = minLineSpacingExtra;
        onContentMetricsChanged();
    }

    public void clearMinLineSpacingExtra() {
        if (Float.isNaN(mMinLineSpacingExtra)) {
            return;
        }
        mMinLineSpacingExtra = Float.NaN;
        onContentMetricsChanged();
    }

    private boolean reduceLineSpacing(Layout layout, int lineCount, int overflow) {
        if (lineCount <= 1) {
            return false;
        }

        float currentExtra = mTextView.getLineSpacingExtra();
        float automaticMinExtra = -findMinDescent(layout, lineCount);
        float minExtra = hasMinLineSpacingExtra()
                ? mMinLineSpacingExtra
                : automaticMinExtra;
        float targetExtra = Math.max(
                currentExtra - ((float) overflow / (lineCount - 1)), minExtra);
        if (targetExtra >= currentExtra - 0.01f) {
            return false;
        }

        applyLineSpacing(targetExtra, mConfiguredLineSpacingMultiplier);
        return true;
    }

    private int findMinDescent(Layout layout, int lineCount) {
        int minDescent = Integer.MAX_VALUE;
        for (int index = 0; index < lineCount; index++) {
            minDescent = Math.min(minDescent, Math.max(0, layout.getLineDescent(index)));
        }
        return minDescent == Integer.MAX_VALUE ? 0 : minDescent;
    }

    private void reduceMaxLines() {
        int currentMaxLines = mTextView.getMaxLines();
        if (currentMaxLines == Integer.MAX_VALUE || currentMaxLines <= mMinLines) {
            return;
        }
        applyMaxLines(Math.max(mMinLines, currentMaxLines - 1));
    }

    private void restoreConfiguredValues() {
        if (mTextView.getMaxLines() != mConfiguredMaxLines) {
            applyMaxLines(mConfiguredMaxLines);
        }
        if (Float.compare(mTextView.getLineSpacingExtra(), mConfiguredLineSpacingExtra) != 0
                || Float.compare(mTextView.getLineSpacingMultiplier(),
                        mConfiguredLineSpacingMultiplier) != 0) {
            applyLineSpacing(mConfiguredLineSpacingExtra, mConfiguredLineSpacingMultiplier);
        }
    }

    private void applyMaxLines(int maxLines) {
        mApplying = true;
        try {
            mTextView.setMaxLines(maxLines);
        } finally {
            mApplying = false;
        }
    }

    private void applyLineSpacing(float add, float multiplier) {
        mApplying = true;
        try {
            mTextView.setLineSpacing(add, multiplier);
        } finally {
            mApplying = false;
        }
    }
}
