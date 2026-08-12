/*
 * Copyright (C) 2019 xuexiangjys(xuexiangjys@163.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.allynav.shape.other;

import android.content.res.TypedArray;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.method.TransformationMethod;
import android.util.TypedValue;
import android.widget.TextView;
import com.allynav.shape.R;

/**
 * 自动字号委托。
 *
 * <p>在调用方配置的最小/最大字号区间内通过二分查找，找到能够放入当前可用宽度和
 * 有限 maxLines 的最大字号。核心算法适配自 XUI AutoFitTextView，并保留其常用 API
 * 命名以降低迁移成本。</p>
 *
 * <p>算法只在布局取得有效宽度后运行，不修改 View 尺寸。未限制 maxLines 时不会缩放；
 * 关闭功能会恢复调用方配置字号。与固定高度自适应同时开启时，本委托先执行。</p>
 */
public final class AutoFitTextDelegate {

    /** 默认最小字号使用 sp；二分精度保存为 px。 */
    private static final float DEFAULT_MIN_TEXT_SIZE_SP = 8f;
    private static final float DEFAULT_PRECISION = 0.5f;

    /** 复用测量 Paint，避免每次布局创建临时对象。 */
    private final TextView mTextView;
    private final TextPaint mMeasurePaint = new TextPaint();

    private boolean mEnabled;
    /** 调用方字号、搜索区间和精度，内部统一换算为 px。 */
    private float mConfiguredTextSize;
    private float mMinTextSize;
    private float mMaxTextSize;
    private float mPrecision;
    private boolean mHasExplicitMaxTextSize;
    /** mApplying 阻止内部 setTextSize 被当成新的调用方配置。 */
    private boolean mApplying;
    private boolean mFitPending;
    /**
     * 记录上一次参与字号计算的正文可用宽度。
     *
     * <p>父容器约束变化不一定伴随文本、padding 等 setter 调用，因此不能只依赖
     * {@link #onContentMetricsChanged()} 标记重新计算。每次测量时比较该值，可以覆盖
     * 横竖屏切换、父容器显示隐藏以及 ConstraintLayout 重新分配宽度等场景。</p>
     */
    private int mLastAvailableWidth = -1;

    public AutoFitTextDelegate(TextView textView, TypedArray typedArray) {
        mTextView = textView;
        mConfiguredTextSize = textView.getTextSize();
        mHasExplicitMaxTextSize = typedArray.hasValue(
                R.styleable.ShapeTextView_shape_autoFitMaxTextSize);
        mMaxTextSize = mHasExplicitMaxTextSize
                ? typedArray.getDimension(
                        R.styleable.ShapeTextView_shape_autoFitMaxTextSize, mConfiguredTextSize)
                : mConfiguredTextSize;
        mMinTextSize = typedArray.hasValue(R.styleable.ShapeTextView_shape_autoFitMinTextSize)
                ? typedArray.getDimension(
                        R.styleable.ShapeTextView_shape_autoFitMinTextSize, defaultMinTextSize())
                : defaultMinTextSize();
        mPrecision = Math.max(0.01f, typedArray.getFloat(
                R.styleable.ShapeTextView_shape_autoFitPrecision, DEFAULT_PRECISION));
        mEnabled = typedArray.getBoolean(
                R.styleable.ShapeTextView_shape_autoFitTextEnable, false);
        mFitPending = mEnabled;
    }

    public void onTextSizeChanged(int unit, float size) {
        if (mApplying) {
            return;
        }
        mConfiguredTextSize = TypedValue.applyDimension(
                unit, size, mTextView.getResources().getDisplayMetrics());
        if (!mHasExplicitMaxTextSize) {
            mMaxTextSize = mConfiguredTextSize;
        }
        requestFit();
    }

    public void onContentMetricsChanged() {
        requestFit();
    }

    /**
     * 在 TextView 完成第一次测量后，按本轮测得的宽度计算并应用最终字号。
     *
     * <p>旧实现把字号计算放在 onLayout 之后。控件由 GONE 恢复为 VISIBLE 时，系统会先
     * 用原字号绘制/定位一轮，再因 AutoFit 修改字号重新布局：跑马灯可能短暂启动后停止，
     * 水平 LinearLayout 还会依据变化后的文字基线把子控件整体下移。现在字号在父容器
     * 真正布局子控件之前确定，ShapeTextView 会在同一次 onMeasure 中使用最终字号复测，
     * 因而首帧位置和跑马灯所使用的 Layout 都保持一致。</p>
     *
     * @param measuredWidth TextView 第一次系统测量得到的宽度，包含左右 compound padding
     * @return true 表示字号发生变化，调用方需要立即使用相同 MeasureSpec 再测量一次
     */
    public boolean fitAfterMeasure(int measuredWidth) {
        if (!mEnabled) {
            return false;
        }

        int maxLines = mTextView.getMaxLines();
        int availableWidth = measuredWidth
                - mTextView.getCompoundPaddingLeft()
                - mTextView.getCompoundPaddingRight();
        if (availableWidth != mLastAvailableWidth) {
            // 宽度变化会直接改变单行文字是否溢出，必须重新搜索，而不是沿用旧字号。
            mLastAvailableWidth = availableWidth;
            mFitPending = true;
        }
        if (!mFitPending && mTextView.getTextSize() <= mMaxTextSize) {
            return false;
        }
        if (maxLines <= 0 || maxLines == Integer.MAX_VALUE) {
            mFitPending = false;
            return false;
        }
        if (availableWidth <= 0) {
            return false;
        }

        CharSequence text = mTextView.getText();
        TransformationMethod transformationMethod = mTextView.getTransformationMethod();
        if (transformationMethod != null) {
            text = transformationMethod.getTransformation(text, mTextView);
        }
        if (text == null) {
            text = "";
        }

        float high = Math.max(0f, mMaxTextSize);
        float low = Math.min(Math.max(0f, mMinTextSize), high);
        // 最大字号已能容纳时直接恢复最大值，否则二分查找最大可用字号。
        float targetSize = fits(text, high, availableWidth, maxLines)
                ? high
                : findLargestTextSize(text, low, high, availableWidth, maxLines);
        boolean changed = applyTextSize(targetSize);
        mFitPending = false;
        return changed;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        if (mEnabled == enabled) {
            return;
        }
        mEnabled = enabled;
        if (enabled) {
            mFitPending = true;
        } else {
            applyTextSize(mConfiguredTextSize);
            mFitPending = false;
        }
        mTextView.requestLayout();
        mTextView.invalidate();
    }

    public float getMinTextSize() {
        return mMinTextSize;
    }

    public void setMinTextSize(int unit, float size) {
        mMinTextSize = TypedValue.applyDimension(
                unit, size, mTextView.getResources().getDisplayMetrics());
        requestFit();
    }

    public float getMaxTextSize() {
        return mMaxTextSize;
    }

    public void setMaxTextSize(int unit, float size) {
        mHasExplicitMaxTextSize = true;
        mMaxTextSize = TypedValue.applyDimension(
                unit, size, mTextView.getResources().getDisplayMetrics());
        requestFit();
    }

    public float getPrecision() {
        return mPrecision;
    }

    public void setPrecision(float precision) {
        if (precision <= 0f) {
            throw new IllegalArgumentException("自动字号精度必须大于 0");
        }
        mPrecision = precision;
        requestFit();
    }

    private void requestFit() {
        if (!mEnabled || mApplying) {
            return;
        }
        mFitPending = true;
        mTextView.requestLayout();
    }

    private float findLargestTextSize(CharSequence text, float low, float high,
            int availableWidth, int maxLines) {
        // 每轮保留能够容纳文本的一侧，直到搜索区间小于配置精度。
        float best = low;
        while (high - low > mPrecision) {
            float middle = (low + high) / 2f;
            if (fits(text, middle, availableWidth, maxLines)) {
                best = middle;
                low = middle;
            } else {
                high = middle;
            }
        }
        return best;
    }

    private boolean fits(CharSequence text, float textSize, int availableWidth, int maxLines) {
        // 单行直接测量宽度；多行使用 StaticLayout 同步系统换行和行距参数。
        mMeasurePaint.set(mTextView.getPaint());
        mMeasurePaint.setTextSize(textSize);
        if (maxLines == 1) {
            return mMeasurePaint.measureText(text.toString()) <= availableWidth;
        }

        StaticLayout layout = new StaticLayout(text, mMeasurePaint, availableWidth,
                Layout.Alignment.ALIGN_NORMAL, mTextView.getLineSpacingMultiplier(),
                mTextView.getLineSpacingExtra(), mTextView.getIncludeFontPadding());
        return layout.getLineCount() <= maxLines;
    }

    private boolean applyTextSize(float textSize) {
        // 使用 px 应用搜索结果，避免重复进行 sp/dp 单位换算。
        if (Math.abs(mTextView.getTextSize() - textSize) < 0.01f) {
            return false;
        }
        mApplying = true;
        try {
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        } finally {
            mApplying = false;
        }
        return true;
    }

    private float defaultMinTextSize() {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                DEFAULT_MIN_TEXT_SIZE_SP, mTextView.getResources().getDisplayMetrics());
    }

}
