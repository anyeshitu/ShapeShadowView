package com.allynav.shape.other;

import android.content.res.TypedArray;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.allynav.shape.R;

/**
 * 固定高度文本自适应委托。
 *
 * <p>当 TextView 的确定高度不足以容纳当前 Layout 时，按配置选择压缩行间距、减少
 * maxLines，或先压缩行间距再减少行数。功能行为参考 AdaptiveTextView，但实现为委托，
 * 以便与 ShapeTextView 的背景、文字效果和跑马灯组合。</p>
 *
 * <p>每次内容、尺寸或排版参数变化时，先恢复调用方配置的基准 maxLines/行间距，再重新
 * 计算，避免多次布局持续累减。wrap_content、match_parent 或无限 maxLines 不执行调整。</p>
 */
public final class AdaptiveTextDelegate {

    /** 仅减少最大行数。 */
    public static final int MODE_REDUCE_LINES = 1;
    /** 仅压缩额外行间距。 */
    public static final int MODE_REDUCE_LINE_SPACING = 2;
    /** 优先压缩行间距，仍溢出时再减少最大行数。 */
    public static final int MODE_REDUCE_LINE_SPACING_THEN_LINES = 3;

    private final TextView mTextView;

    /** 当前功能配置。minLineSpacingExtra 为 NaN 时按字体 descent 自动计算下限。 */
    private boolean mEnabled;
    private int mMode;
    private int mMinLines;
    private float mMinLineSpacingExtra;

    /** 调用方配置的基准排版值，重新适配前必须恢复。 */
    private int mConfiguredMaxLines;
    private float mConfiguredLineSpacingExtra;
    private float mConfiguredLineSpacingMultiplier;

    /** 内部应用标记用于阻止 ShapeTextView 重写 setter 时把临时值记录为新基准。 */
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
        // 参数变化后先恢复基准值，让本轮系统测量从调用方真实配置开始。
        if (!mEnabled || !mResetPending) {
            return;
        }
        restoreConfiguredValues();
        mResetPending = false;
    }

    public void afterLayout() {
        // Layout 已生成后才能取得真实行高和溢出量，据此执行压缩策略。
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
        // 将溢出高度均摊到行间距，并限制在调用方下限或字体 descent 安全线之上。
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
        // maxLines 每次最多降到配置的最小行数，修改后请求下一轮布局生成新 Layout。
        int currentMaxLines = mTextView.getMaxLines();
        if (currentMaxLines == Integer.MAX_VALUE || currentMaxLines <= mMinLines) {
            return;
        }
        applyMaxLines(Math.max(mMinLines, currentMaxLines - 1));
    }

    private void restoreConfiguredValues() {
        // 恢复动作由 mApplying 包裹，防止回调再次改写配置快照。
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
