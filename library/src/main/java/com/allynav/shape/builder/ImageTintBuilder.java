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
        if (array.hasValue(R.styleable.ShapeImageTint_shape_enableTint)) {
            // 新名称与 shape_tint 含义相同；同时配置时明确使用 shape_enableTint。
            mHasTint = true;
            mTintColor = array.getColor(
                    R.styleable.ShapeImageTint_shape_enableTint, mTintColor);
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
        if (array.hasValue(R.styleable.ShapeImageTint_shape_disableTint)) {
            // shape_disableTint 是 disabledTint 的简写别名，同时配置时由新名称覆盖。
            mDisabledTint = array.getColor(
                    R.styleable.ShapeImageTint_shape_disableTint, Color.TRANSPARENT);
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

    /** 返回 XML 或 Java 是否至少设置了一项自定义 tint，用于避免无配置时接管原生行为。 */
    public boolean hasCustomTint() {
        return mHasTint || mPressedTint != null || mCheckedTint != null ||
                mDisabledTint != null || mFocusedTint != null || mSelectedTint != null;
    }

    /** 设置普通启用状态的 tint；配置后会覆盖图片自身颜色，调用 intoTint 后生效。 */
    public ImageTintBuilder setTintColor(int color) {
        mHasTint = true;
        mTintColor = color;
        return this;
    }

    /** 设置默认图片 tint，等同于 setTintColor。 */
    public ImageTintBuilder setTint(int color) {
        return setTintColor(color);
    }

    /** 设置启用状态普通 tint，等同于 setTintColor。 */
    public ImageTintBuilder setEnableTintColor(int color) {
        return setTintColor(color);
    }

    /** 与 {@link #setEnableTintColor(int)} 含义相同。 */
    public ImageTintBuilder setEnableTint(int color) {
        return setEnableTintColor(color);
    }

    /** 设置按下状态 tint；传入 null 可删除该状态颜色并回退到普通状态。 */
    public ImageTintBuilder setPressedTintColor(@Nullable Integer color) {
        mPressedTint = color;
        return this;
    }

    /** 设置按下状态 tint，等同于 setPressedTintColor。 */
    public ImageTintBuilder setPressedTint(@Nullable Integer color) {
        return setPressedTintColor(color);
    }

    /** 设置 checked 状态 tint，供追加了 checked DrawableState 的扩展图片控件使用。 */
    public ImageTintBuilder setCheckedTintColor(@Nullable Integer color) {
        mCheckedTint = color;
        return this;
    }

    public ImageTintBuilder setCheckedTint(@Nullable Integer color) {
        return setCheckedTintColor(color);
    }

    /** 设置 enabled=false 时的 tint；禁用状态在所有自定义状态中优先级最高。 */
    public ImageTintBuilder setDisabledTintColor(@Nullable Integer color) {
        mDisabledTint = color;
        return this;
    }

    public ImageTintBuilder setDisabledTint(@Nullable Integer color) {
        return setDisabledTintColor(color);
    }

    /** shape_disableTint 对应的 Java API，内部继续复用 disabled 状态字段。 */
    public ImageTintBuilder setDisableTintColor(@Nullable Integer color) {
        return setDisabledTintColor(color);
    }

    public ImageTintBuilder setDisableTint(@Nullable Integer color) {
        return setDisableTintColor(color);
    }

    /** 设置控件获得焦点时的 tint；传入 null 可删除该状态配置。 */
    public ImageTintBuilder setFocusedTintColor(@Nullable Integer color) {
        mFocusedTint = color;
        return this;
    }

    public ImageTintBuilder setFocusedTint(@Nullable Integer color) {
        return setFocusedTintColor(color);
    }

    /** 设置 selected=true 时的 tint；普通状态不受该配置影响。 */
    public ImageTintBuilder setSelectedTintColor(@Nullable Integer color) {
        mSelectedTint = color;
        return this;
    }

    public ImageTintBuilder setSelectedTint(@Nullable Integer color) {
        return setSelectedTintColor(color);
    }

    /** 开始接管图片 tint，并立即按照控件当前 DrawableState 刷新颜色。 */
    public void intoTint() {
        // 立即按当前状态解析颜色，保证动态设置后马上生效。
        mTintConfigured = true;
        onDrawableStateChanged(mImageView.getDrawableState());
    }

    /** 清空全部自定义状态 tint，并恢复构建器创建时记录的原始 ColorStateList。 */
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

    /** 由 ShapeImageView 在 DrawableState 变化后调用，解析并应用当前状态颜色。 */
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
