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
 * 在有限行数内通过二分查找缩小字号，核心算法来自 XUI AutoFitTextView。
 */
public final class AutoFitTextDelegate {

    private static final float DEFAULT_MIN_TEXT_SIZE_SP = 8f;
    private static final float DEFAULT_PRECISION = 0.5f;

    private final TextView mTextView;
    private final TextPaint mMeasurePaint = new TextPaint();

    private boolean mEnabled;
    private float mConfiguredTextSize;
    private float mMinTextSize;
    private float mMaxTextSize;
    private float mPrecision;
    private boolean mHasExplicitMaxTextSize;
    private boolean mApplying;
    private boolean mFitPending;

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

    public boolean fitAfterLayout() {
        if (!mEnabled || (!mFitPending && mTextView.getTextSize() <= mMaxTextSize)) {
            return false;
        }

        int maxLines = mTextView.getMaxLines();
        int availableWidth = mTextView.getWidth()
                - mTextView.getCompoundPaddingLeft()
                - mTextView.getCompoundPaddingRight();
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
