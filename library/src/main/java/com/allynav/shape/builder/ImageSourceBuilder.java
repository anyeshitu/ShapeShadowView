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
 * ImageView source builder with standard Android drawable-state support.
 *
 * <p>状态图片没有配置时会回退到默认图片；默认图片没有配置时会回退到 android:src，
 * 因此只配置按下或选中图片也能正常使用。</p>
 */
public final class ImageSourceBuilder {

    private static final int[] STATE_PRESSED = {android.R.attr.state_pressed};
    private static final int[] STATE_CHECKED = {android.R.attr.state_checked};
    private static final int[] STATE_DISABLED = {-android.R.attr.state_enabled};
    private static final int[] STATE_FOCUSED = {android.R.attr.state_focused};
    private static final int[] STATE_SELECTED = {android.R.attr.state_selected};

    private final ImageView mImageView;
    private final Drawable mOriginalDrawable;

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
        onDrawableStateChanged(mImageView.getDrawableState());
    }

    public void clearSource() {
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
