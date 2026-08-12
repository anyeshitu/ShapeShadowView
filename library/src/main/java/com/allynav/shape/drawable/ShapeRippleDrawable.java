package com.allynav.shape.drawable;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

/**
 * 保留内容和固定蒙版引用的 RippleDrawable 包装器。
 *
 * <p>系统 RippleDrawable 不公开原始构造参数。Builder 动态重建背景时需要取回内容，
 * ShadowDrawable 生成稳定阴影时需要取回不会随触摸动画变化的 Shape 蒙版，因此本类
 * 显式保存两者。</p>
 */
public final class ShapeRippleDrawable extends RippleDrawable {

    /** 正常显示内容与限定 Ripple/阴影轮廓的不透明蒙版。 */
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
