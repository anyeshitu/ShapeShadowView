package com.allynav.shape.builder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.StateSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.allynav.shape.R;

/**
 * ImageView 状态图片构建器。
 *
 * <p>支持默认、按下、checked、禁用、聚焦和 selected 状态。状态图片未配置时回退到
 * 默认图片，默认图片未配置时继续使用控件原始 {@code android:src}，因此只配置按下
 * 或选中图片也能正常工作。</p>
 *
 * <p>该类根据 {@code drawableStateChanged} 直接解析当前图片，不额外包装
 * StateListDrawable，可兼容普通图片、VectorDrawable 和 XML Drawable。</p>
 */
public final class ImageSourceBuilder {

    /** 状态匹配顺序由高优先级到低优先级，默认图片在解析方法末尾兜底。 */
    private static final int[] STATE_PRESSED = {android.R.attr.state_pressed};
    private static final int[] STATE_CHECKED = {android.R.attr.state_checked};
    private static final int[] STATE_DISABLED = {-android.R.attr.state_enabled};
    private static final int[] STATE_FOCUSED = {android.R.attr.state_focused};
    private static final int[] STATE_SELECTED = {android.R.attr.state_selected};

    /** 目标 ImageView 及构建器创建时保存的原始 src。 */
    private final ImageView mImageView;
    private final Drawable mOriginalDrawable;

    /** 是否至少配置过一个自定义状态图片。 */
    private boolean mSourceConfigured;
    private Drawable mDefaultSource;
    private Drawable mPressedSource;
    private Drawable mCheckedSource;
    private Drawable mDisabledSource;
    private Drawable mFocusedSource;
    private Drawable mSelectedSource;

    public ImageSourceBuilder(@NonNull ImageView imageView, @Nullable AttributeSet attrs) {
        mImageView = imageView;
        mOriginalDrawable = imageView.getDrawable();
        if (attrs == null) {
            return;
        }

        Context context = imageView.getContext();
        android.content.res.TypedArray array = context.obtainStyledAttributes(
                attrs, R.styleable.ShapeImageSource);
        if (array.hasValue(R.styleable.ShapeImageSource_shape_src)) {
            mSourceConfigured = true;
            mDefaultSource = array.getDrawable(R.styleable.ShapeImageSource_shape_src);
        }
        if (array.hasValue(R.styleable.ShapeImageSource_shape_pressedSrc)) {
            mSourceConfigured = true;
            mPressedSource = array.getDrawable(R.styleable.ShapeImageSource_shape_pressedSrc);
        }
        if (array.hasValue(R.styleable.ShapeImageSource_shape_checkedSrc)) {
            mSourceConfigured = true;
            mCheckedSource = array.getDrawable(R.styleable.ShapeImageSource_shape_checkedSrc);
        }
        if (array.hasValue(R.styleable.ShapeImageSource_shape_disabledSrc)) {
            mSourceConfigured = true;
            mDisabledSource = array.getDrawable(R.styleable.ShapeImageSource_shape_disabledSrc);
        }
        if (array.hasValue(R.styleable.ShapeImageSource_shape_focusedSrc)) {
            mSourceConfigured = true;
            mFocusedSource = array.getDrawable(R.styleable.ShapeImageSource_shape_focusedSrc);
        }
        if (array.hasValue(R.styleable.ShapeImageSource_shape_selectedSrc)) {
            mSourceConfigured = true;
            mSelectedSource = array.getDrawable(R.styleable.ShapeImageSource_shape_selectedSrc);
        }
        array.recycle();
    }

    public boolean hasCustomSource() {
        return mSourceConfigured;
    }

    public ImageSourceBuilder setSourceDrawable(@Nullable Drawable drawable) {
        mSourceConfigured = true;
        mDefaultSource = drawable;
        return this;
    }

    /** 设置默认图片，等同于 setSourceDrawable。 */
    public ImageSourceBuilder setSource(@Nullable Drawable drawable) {
        return setSourceDrawable(drawable);
    }

    public ImageSourceBuilder setPressedSourceDrawable(@Nullable Drawable drawable) {
        mSourceConfigured = true;
        mPressedSource = drawable;
        return this;
    }

    /** 设置按下状态图片，等同于 setPressedSourceDrawable。 */
    public ImageSourceBuilder setPressedSource(@Nullable Drawable drawable) {
        return setPressedSourceDrawable(drawable);
    }

    public ImageSourceBuilder setCheckedSourceDrawable(@Nullable Drawable drawable) {
        mSourceConfigured = true;
        mCheckedSource = drawable;
        return this;
    }

    public ImageSourceBuilder setCheckedSource(@Nullable Drawable drawable) {
        return setCheckedSourceDrawable(drawable);
    }

    public ImageSourceBuilder setDisabledSourceDrawable(@Nullable Drawable drawable) {
        mSourceConfigured = true;
        mDisabledSource = drawable;
        return this;
    }

    public ImageSourceBuilder setDisabledSource(@Nullable Drawable drawable) {
        return setDisabledSourceDrawable(drawable);
    }

    public ImageSourceBuilder setFocusedSourceDrawable(@Nullable Drawable drawable) {
        mSourceConfigured = true;
        mFocusedSource = drawable;
        return this;
    }

    public ImageSourceBuilder setFocusedSource(@Nullable Drawable drawable) {
        return setFocusedSourceDrawable(drawable);
    }

    public ImageSourceBuilder setSelectedSourceDrawable(@Nullable Drawable drawable) {
        mSourceConfigured = true;
        mSelectedSource = drawable;
        return this;
    }

    public ImageSourceBuilder setSelectedSource(@Nullable Drawable drawable) {
        return setSelectedSourceDrawable(drawable);
    }

    public void intoSource() {
        // 应用时立即按当前 DrawableState 选图，不必等待下一次状态变化。
        onDrawableStateChanged(mImageView.getDrawableState());
    }

    public void clearSource() {
        // 清空自定义状态后恢复构建器创建时保存的 android:src。
        mSourceConfigured = false;
        mDefaultSource = null;
        mPressedSource = null;
        mCheckedSource = null;
        mDisabledSource = null;
        mFocusedSource = null;
        mSelectedSource = null;
        mImageView.setImageDrawable(mOriginalDrawable);
    }

    public void onDrawableStateChanged(@NonNull int[] stateSet) {
        if (!mSourceConfigured) {
            return;
        }

        Drawable source = resolveStateSource(stateSet);
        Drawable target = source != null ? source : mOriginalDrawable;
        if (mImageView.getDrawable() != target) {
            mImageView.setImageDrawable(target);
        }
    }

    @Nullable
    private Drawable resolveStateSource(@NonNull int[] stateSet) {
        // 禁用状态优先于交互状态，避免 disabled 控件仍显示 pressed/checked 图片。
        if (mDisabledSource != null && StateSet.stateSetMatches(STATE_DISABLED, stateSet)) {
            return mDisabledSource;
        }
        if (mPressedSource != null && StateSet.stateSetMatches(STATE_PRESSED, stateSet)) {
            return mPressedSource;
        }
        if (mCheckedSource != null && StateSet.stateSetMatches(STATE_CHECKED, stateSet)) {
            return mCheckedSource;
        }
        if (mFocusedSource != null && StateSet.stateSetMatches(STATE_FOCUSED, stateSet)) {
            return mFocusedSource;
        }
        if (mSelectedSource != null && StateSet.stateSetMatches(STATE_SELECTED, stateSet)) {
            return mSelectedSource;
        }
        return mDefaultSource;
    }
}
