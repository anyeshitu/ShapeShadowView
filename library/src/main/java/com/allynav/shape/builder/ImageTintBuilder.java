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
 * ImageView 状态着色构建器。
 *
 * <p>支持默认、按下、checked、禁用、聚焦和 selected tint。没有匹配到自定义状态时
 * 恢复 ImageView 原始 {@code android:tint}，避免局部状态配置改变普通状态显示。</p>
 *
 * <p>颜色使用 {@link ColorStateList} 应用，既保留 AppCompat 的兼容行为，也允许在
 * DrawableState 变化时只替换当前 tint 而不更换 src。</p>
 */
public final class ImageTintBuilder {

    /** 状态匹配顺序由高优先级到低优先级。 */
    private static final int[] STATE_PRESSED = {android.R.attr.state_pressed};
    private static final int[] STATE_CHECKED = {android.R.attr.state_checked};
    private static final int[] STATE_DISABLED = {-android.R.attr.state_enabled};
    private static final int[] STATE_FOCUSED = {android.R.attr.state_focused};
    private static final int[] STATE_SELECTED = {android.R.attr.state_selected};

    /** 目标 ImageView 及构建器创建时保存的原始 tint。 */
    private final ImageView mImageView;
    private final ColorStateList mOriginalTint;

    /** 是否已调用 intoTint；setter 只修改配置，不直接接管控件 tint。 */
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
        // 立即按当前状态解析颜色，保证动态设置后马上生效。
        mTintConfigured = true;
        onDrawableStateChanged(mImageView.getDrawableState());
    }

    public void clearTint() {
        // 恢复控件创建构建器时的原始 ColorStateList。
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
        // 禁用状态优先，随后处理按下、checked、聚焦和 selected 状态。
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
