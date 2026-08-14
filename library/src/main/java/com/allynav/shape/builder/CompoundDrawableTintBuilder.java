package com.allynav.shape.builder;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.StateSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;

import com.allynav.shape.R;

/**
 * ShapeTextView compound drawable 状态着色构建器。
 *
 * <p>该构建器统一管理 {@code drawableStart}、{@code drawableTop}、{@code drawableEnd}
 * 和 {@code drawableBottom} 的 tint。Android TextView 只提供一份 compound drawable tint，
 * 因此四个方向会使用同一状态颜色；构建器不会替换任何 Drawable。</p>
 *
 * <p>只配置按下或禁用等局部状态时，普通状态会恢复控件初始化时的
 * {@code android:drawableTint}/{@code app:drawableTint}。原始 tint 为空时等同于清除着色，
 * 因而 Vector、PNG 或 mipmap 会显示自身原始颜色，而不是被默认染成白色。</p>
 */
public final class CompoundDrawableTintBuilder {

    /** 状态匹配顺序从高优先级到低优先级。 */
    private static final int[] STATE_PRESSED = {android.R.attr.state_pressed};
    private static final int[] STATE_CHECKED = {android.R.attr.state_checked};
    private static final int[] STATE_DISABLED = {-android.R.attr.state_enabled};
    private static final int[] STATE_FOCUSED = {android.R.attr.state_focused};
    private static final int[] STATE_SELECTED = {android.R.attr.state_selected};

    /** 目标 TextView 以及构建器创建时保存的原始 compound drawable tint。 */
    private final TextView mTextView;
    private final ColorStateList mOriginalTint;

    /** intoTint 调用后才接管状态变化，避免未配置时改变 TextView 原生行为。 */
    private boolean mTintConfigured;
    private Integer mEnabledTint;
    private Integer mPressedTint;
    private Integer mCheckedTint;
    private Integer mDisabledTint;
    private Integer mFocusedTint;
    private Integer mSelectedTint;

    public CompoundDrawableTintBuilder(@NonNull TextView textView,
            @Nullable AttributeSet attrs) {
        mTextView = textView;
        mOriginalTint = TextViewCompat.getCompoundDrawableTintList(textView);
        if (attrs == null) {
            return;
        }

        Context context = textView.getContext();
        TypedArray array = context.obtainStyledAttributes(
                attrs, R.styleable.ShapeCompoundDrawableTint);
        // shape_enableTint/shape_disableTint 是新命名；同时配置兼容旧名称时由新名称覆盖。
        if (array.hasValue(R.styleable.ShapeCompoundDrawableTint_shape_tint)) {
            mEnabledTint = array.getColor(
                    R.styleable.ShapeCompoundDrawableTint_shape_tint, Color.TRANSPARENT);
        }
        if (array.hasValue(R.styleable.ShapeCompoundDrawableTint_shape_enableTint)) {
            mEnabledTint = array.getColor(
                    R.styleable.ShapeCompoundDrawableTint_shape_enableTint, Color.TRANSPARENT);
        }
        if (array.hasValue(R.styleable.ShapeCompoundDrawableTint_shape_pressedTint)) {
            mPressedTint = array.getColor(
                    R.styleable.ShapeCompoundDrawableTint_shape_pressedTint, Color.TRANSPARENT);
        }
        if (array.hasValue(R.styleable.ShapeCompoundDrawableTint_shape_checkedTint)) {
            mCheckedTint = array.getColor(
                    R.styleable.ShapeCompoundDrawableTint_shape_checkedTint, Color.TRANSPARENT);
        }
        if (array.hasValue(R.styleable.ShapeCompoundDrawableTint_shape_disabledTint)) {
            mDisabledTint = array.getColor(
                    R.styleable.ShapeCompoundDrawableTint_shape_disabledTint, Color.TRANSPARENT);
        }
        if (array.hasValue(R.styleable.ShapeCompoundDrawableTint_shape_disableTint)) {
            mDisabledTint = array.getColor(
                    R.styleable.ShapeCompoundDrawableTint_shape_disableTint, Color.TRANSPARENT);
        }
        if (array.hasValue(R.styleable.ShapeCompoundDrawableTint_shape_focusedTint)) {
            mFocusedTint = array.getColor(
                    R.styleable.ShapeCompoundDrawableTint_shape_focusedTint, Color.TRANSPARENT);
        }
        if (array.hasValue(R.styleable.ShapeCompoundDrawableTint_shape_selectedTint)) {
            mSelectedTint = array.getColor(
                    R.styleable.ShapeCompoundDrawableTint_shape_selectedTint, Color.TRANSPARENT);
        }
        array.recycle();
    }

    /** 返回是否至少配置了一个普通或状态 tint。 */
    public boolean hasCustomTint() {
        return mEnabledTint != null || mPressedTint != null || mCheckedTint != null
                || mDisabledTint != null || mFocusedTint != null || mSelectedTint != null;
    }

    /** 设置启用状态的普通 tint；传入 null 表示普通状态恢复 Drawable 自身颜色。 */
    public CompoundDrawableTintBuilder setEnableTintColor(@Nullable Integer color) {
        mEnabledTint = color;
        return this;
    }

    /** 与 setEnableTintColor 含义相同。 */
    public CompoundDrawableTintBuilder setEnableTint(@Nullable Integer color) {
        return setEnableTintColor(color);
    }

    /** 与 {@link #setEnableTintColor(Integer)} 含义相同，兼容简写调用方式。 */
    public CompoundDrawableTintBuilder setTint(@Nullable Integer color) {
        return setEnableTintColor(color);
    }

    /** 与 {@link #setEnableTintColor(Integer)} 含义相同，保持两个 TintBuilder 的 API 一致。 */
    public CompoundDrawableTintBuilder setTintColor(@Nullable Integer color) {
        return setEnableTintColor(color);
    }

    /** 设置按下状态 tint；传入 null 可删除该状态颜色并回退到普通状态。 */
    public CompoundDrawableTintBuilder setPressedTintColor(@Nullable Integer color) {
        mPressedTint = color;
        return this;
    }

    /** 与 {@link #setPressedTintColor(Integer)} 含义相同。 */
    public CompoundDrawableTintBuilder setPressedTint(@Nullable Integer color) {
        return setPressedTintColor(color);
    }

    /** 设置 checked 状态 tint，供追加了 checked DrawableState 的扩展文本控件使用。 */
    public CompoundDrawableTintBuilder setCheckedTintColor(@Nullable Integer color) {
        mCheckedTint = color;
        return this;
    }

    /** 与 {@link #setCheckedTintColor(Integer)} 含义相同。 */
    public CompoundDrawableTintBuilder setCheckedTint(@Nullable Integer color) {
        return setCheckedTintColor(color);
    }

    /** 设置 enabled=false 时的 tint；禁用状态在所有自定义状态中优先级最高。 */
    public CompoundDrawableTintBuilder setDisableTintColor(@Nullable Integer color) {
        mDisabledTint = color;
        return this;
    }

    /** 与 {@link #setDisableTintColor(Integer)} 含义相同。 */
    public CompoundDrawableTintBuilder setDisableTint(@Nullable Integer color) {
        return setDisableTintColor(color);
    }

    /** 兼容已有 disabled 命名。 */
    public CompoundDrawableTintBuilder setDisabledTint(@Nullable Integer color) {
        return setDisableTintColor(color);
    }

    /** 与 {@link #setDisableTintColor(Integer)} 含义相同，兼容完整的 disabled 命名。 */
    public CompoundDrawableTintBuilder setDisabledTintColor(@Nullable Integer color) {
        return setDisableTintColor(color);
    }

    /** 设置控件获得焦点时的 tint；传入 null 可删除该状态配置。 */
    public CompoundDrawableTintBuilder setFocusedTintColor(@Nullable Integer color) {
        mFocusedTint = color;
        return this;
    }

    /** 与 {@link #setFocusedTintColor(Integer)} 含义相同。 */
    public CompoundDrawableTintBuilder setFocusedTint(@Nullable Integer color) {
        return setFocusedTintColor(color);
    }

    /** 设置 selected=true 时的 tint；跑马灯会占用 selected 状态，使用时需注意。 */
    public CompoundDrawableTintBuilder setSelectedTintColor(@Nullable Integer color) {
        mSelectedTint = color;
        return this;
    }

    /** 与 {@link #setSelectedTintColor(Integer)} 含义相同。 */
    public CompoundDrawableTintBuilder setSelectedTint(@Nullable Integer color) {
        return setSelectedTintColor(color);
    }

    /** 应用当前配置，并立即按照控件现有 DrawableState 刷新 tint。 */
    public void intoTint() {
        mTintConfigured = true;
        onDrawableStateChanged(mTextView.getDrawableState());
    }

    /** 清空自定义状态并恢复控件初始化时的 compound drawable tint。 */
    public void clearTint() {
        mTintConfigured = false;
        mEnabledTint = null;
        mPressedTint = null;
        mCheckedTint = null;
        mDisabledTint = null;
        mFocusedTint = null;
        mSelectedTint = null;
        TextViewCompat.setCompoundDrawableTintList(mTextView, mOriginalTint);
    }

    /** 在 TextView DrawableState 变化后解析并应用当前状态颜色。 */
    public void onDrawableStateChanged(@NonNull int[] stateSet) {
        if (!mTintConfigured) {
            return;
        }
        Integer stateTint = resolveStateTint(stateSet);
        if (stateTint != null) {
            TextViewCompat.setCompoundDrawableTintList(
                    mTextView, ColorStateList.valueOf(stateTint));
        } else {
            // 未配置 enable tint 时必须恢复原始 ColorStateList；原始值为 null 即保留图片原色。
            TextViewCompat.setCompoundDrawableTintList(mTextView, mOriginalTint);
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
        return mEnabledTint;
    }
}
