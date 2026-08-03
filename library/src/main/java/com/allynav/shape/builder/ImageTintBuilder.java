package com.allynav.shape.builder;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.StateSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.allynav.shape.R;

/**
 * ImageView tint builder with standard Android drawable-state support.
 *
 * <p>当没有匹配到自定义状态时，会恢复 ImageView 原本的 android:tint，避免状态 tint
 * 配置影响普通状态下的图片显示。</p>
 */
public final class ImageTintBuilder {

    private static final int[] STATE_PRESSED = {android.R.attr.state_pressed};
    private static final int[] STATE_CHECKED = {android.R.attr.state_checked};
    private static final int[] STATE_DISABLED = {-android.R.attr.state_enabled};
    private static final int[] STATE_FOCUSED = {android.R.attr.state_focused};
    private static final int[] STATE_SELECTED = {android.R.attr.state_selected};

    private final ImageView mImageView;
    private final ColorStateList mOriginalTint;

    private boolean mTintConfigured;
    private boolean mHasTint;
    private int mTintColor = Color.WHITE;
    private Integer mPressedTint;
    private Integer mCheckedTint;
    private Integer mDisabledTint;
    private Integer mFocusedTint;
    private Integer mSelectedTint;

    public ImageTintBuilder(@NonNull ImageView imageView, @Nullable AttributeSet attrs) {
        mImageView = imageView;
        mOriginalTint = imageView.getImageTintList();
        if (attrs == null) {
            return;
        }

        Context context = imageView.getContext();
        android.content.res.TypedArray array = context.obtainStyledAttributes(
                attrs, R.styleable.ShapeImageTint);
        if (array.hasValue(R.styleable.ShapeImageTint_shape_tint)) {
            mHasTint = true;
            mTintColor = array.getColor(
                    R.styleable.ShapeImageTint_shape_tint, mTintColor);
        }
        if (array.hasValue(R.styleable.ShapeImageTint_shape_pressedTint)) {
            mPressedTint = array.getColor(
                    R.styleable.ShapeImageTint_shape_pressedTint, Color.TRANSPARENT);
        }
        if (array.hasValue(R.styleable.ShapeImageTint_shape_checkedTint)) {
            mCheckedTint = array.getColor(
                    R.styleable.ShapeImageTint_shape_checkedTint, Color.TRANSPARENT);
        }
        if (array.hasValue(R.styleable.ShapeImageTint_shape_disabledTint)) {
            mDisabledTint = array.getColor(
                    R.styleable.ShapeImageTint_shape_disabledTint, Color.TRANSPARENT);
        }
        if (array.hasValue(R.styleable.ShapeImageTint_shape_focusedTint)) {
            mFocusedTint = array.getColor(
                    R.styleable.ShapeImageTint_shape_focusedTint, Color.TRANSPARENT);
        }
        if (array.hasValue(R.styleable.ShapeImageTint_shape_selectedTint)) {
            mSelectedTint = array.getColor(
                    R.styleable.ShapeImageTint_shape_selectedTint, Color.TRANSPARENT);
        }
        array.recycle();
    }

    public boolean hasCustomTint() {
        return mHasTint || mPressedTint != null || mCheckedTint != null ||
                mDisabledTint != null || mFocusedTint != null || mSelectedTint != null;
    }

    public ImageTintBuilder setTintColor(int color) {
        mHasTint = true;
        mTintColor = color;
        return this;
    }

    /** 设置默认图片 tint，等同于 setTintColor。 */
    public ImageTintBuilder setTint(int color) {
        return setTintColor(color);
    }

    public ImageTintBuilder setPressedTintColor(@Nullable Integer color) {
        mPressedTint = color;
        return this;
    }

    /** 设置按下状态 tint，等同于 setPressedTintColor。 */
    public ImageTintBuilder setPressedTint(@Nullable Integer color) {
        return setPressedTintColor(color);
    }

    public ImageTintBuilder setCheckedTintColor(@Nullable Integer color) {
        mCheckedTint = color;
        return this;
    }

    public ImageTintBuilder setCheckedTint(@Nullable Integer color) {
        return setCheckedTintColor(color);
    }

    public ImageTintBuilder setDisabledTintColor(@Nullable Integer color) {
        mDisabledTint = color;
        return this;
    }

    public ImageTintBuilder setDisabledTint(@Nullable Integer color) {
        return setDisabledTintColor(color);
    }

    public ImageTintBuilder setFocusedTintColor(@Nullable Integer color) {
        mFocusedTint = color;
        return this;
    }

    public ImageTintBuilder setFocusedTint(@Nullable Integer color) {
        return setFocusedTintColor(color);
    }

    public ImageTintBuilder setSelectedTintColor(@Nullable Integer color) {
        mSelectedTint = color;
        return this;
    }

    public ImageTintBuilder setSelectedTint(@Nullable Integer color) {
        return setSelectedTintColor(color);
    }

    public void intoTint() {
        mTintConfigured = true;
        onDrawableStateChanged(mImageView.getDrawableState());
    }

    public void clearTint() {
        mTintConfigured = false;
        mHasTint = false;
        mPressedTint = null;
        mCheckedTint = null;
        mDisabledTint = null;
        mFocusedTint = null;
        mSelectedTint = null;
        mImageView.setImageTintList(mOriginalTint);
    }

    public void onDrawableStateChanged(@NonNull int[] stateSet) {
        if (!mTintConfigured) {
            return;
        }

        Integer stateTint = resolveStateTint(stateSet);
        if (stateTint != null) {
            mImageView.setImageTintList(ColorStateList.valueOf(stateTint));
        } else if (mHasTint) {
            mImageView.setImageTintList(ColorStateList.valueOf(mTintColor));
        } else {
            mImageView.setImageTintList(mOriginalTint);
        }
    }

    @Nullable
    private Integer resolveStateTint(@NonNull int[] stateSet) {
        if (mDisabledTint != null && StateSet.stateSetMatches(STATE_DISABLED, stateSet)) {
            return mDisabledTint;
        }
        if (mPressedTint != null && StateSet.stateSetMatches(STATE_PRESSED, stateSet)) {
            return mPressedTint;
        }
        if (mCheckedTint != null && StateSet.stateSetMatches(STATE_CHECKED, stateSet)) {
            return mCheckedTint;
        }
        if (mFocusedTint != null && StateSet.stateSetMatches(STATE_FOCUSED, stateSet)) {
            return mFocusedTint;
        }
        if (mSelectedTint != null && StateSet.stateSetMatches(STATE_SELECTED, stateSet)) {
            return mSelectedTint;
        }
        return null;
    }
}
