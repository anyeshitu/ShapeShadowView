package com.allynav.shape.drawable;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

/** Ripple wrapper that keeps the content drawable available to ShapeDrawableBuilder. */
public final class ShapeRippleDrawable extends RippleDrawable {

    private final Drawable mContentDrawable;
    private final Drawable mMaskDrawable;

    public ShapeRippleDrawable(@ColorInt int rippleColor, @NonNull Drawable contentDrawable,
                               @NonNull Drawable maskDrawable) {
        super(ColorStateList.valueOf(rippleColor), contentDrawable, maskDrawable);
        mContentDrawable = contentDrawable;
        mMaskDrawable = maskDrawable;
    }

    @NonNull
    public Drawable getContentDrawable() {
        return mContentDrawable;
    }

    @NonNull
    public Drawable getShadowMaskDrawable() {
        return mMaskDrawable;
    }
}
