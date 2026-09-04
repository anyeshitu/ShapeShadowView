package com.allynav.shape.builder;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.allynav.shape.R;
import com.allynav.shape.config.IShapeDrawableStyleable;
import com.allynav.shape.drawable.ShapeDrawable;
import com.allynav.shape.drawable.ShapeClipDrawable;
import com.allynav.shape.drawable.ShapeGradientOrientation;
import com.allynav.shape.drawable.ShapeGradientType;
import com.allynav.shape.drawable.ShapeGradientTypeLimit;
import com.allynav.shape.drawable.ShapeRippleDrawable;
import com.allynav.shape.drawable.ShadowDrawable;
import com.allynav.shape.drawable.ShapeType;
import com.allynav.shape.drawable.ShapeTypeLimit;
import com.allynav.shape.other.ExtendStateListDrawable;

/**
 * Shape 背景的统一配置、构建和应用入口。
 *
 * <p>该类把 XML 属性和 Java 链式 API 转换为 {@link ShapeDrawable}、状态选择器、
 * {@link ShapeRippleDrawable} 与 {@link ShadowDrawable}。支持矩形、椭圆、线、环形，
 * 以及圆角、渐变、描边、虚线、Ripple、状态背景和位图缓存阴影。</p>
 *
 * <p>背景装饰顺序固定为：构建默认/状态内容、按 Shape 轮廓裁剪自定义 Drawable、
 * 包装 Ripple、最后包装阴影。Java 修改属性后必须调用 {@link #intoBackground()}
 * 才会重新构建并应用背景。</p>
 */
public final class ShapeDrawableBuilder {

    /** 透明色同时作为“未配置可见颜色”的默认值。 */
    private static final int NO_COLOR = Color.TRANSPARENT;

    /** 目标 View 和初始业务 padding；阴影占用的 inset 会在初始 padding 上叠加。 */
    private final View mView;
    private final int mOriginalPaddingStart;
    private final int mOriginalPaddingTop;
    private final int mOriginalPaddingEnd;
    private final int mOriginalPaddingBottom;

    /** Shape 基础几何参数，所有尺寸单位均为 px。 */
    @ShapeTypeLimit
    private int mType;
    private int mWidth;
    private int mHeight;

    /** 默认及各 DrawableState 对应的填充色。 */
    private int mSolidColor;
    private Integer mSolidPressedColor;
    private Integer mSolidCheckedColor;
    private Integer mSolidDisabledColor;
    private Integer mSolidFocusedColor;
    private Integer mSolidSelectedColor;

    /** 四个物理方向圆角；start/end XML 属性读取时会转换成 left/right。 */
    private float mTopLeftRadius;
    private float mTopRightRadius;
    private float mBottomLeftRadius;
    private float mBottomRightRadius;

    /** 填充渐变参数。 */
    private int[] mSolidGradientColors;
    private ShapeGradientOrientation mSolidGradientOrientation;
    /** 任意角度填充渐变；NaN 表示使用 mSolidGradientOrientation。 */
    private float mSolidGradientAngle = Float.NaN;
    @ShapeGradientTypeLimit
    private int mSolidGradientType;
    private float mSolidGradientCenterX;
    private float mSolidGradientCenterY;
    private int mSolidGradientRadius;

    /** 默认及各 DrawableState 对应的描边参数。 */
    private int mStrokeColor;
    private Integer mStrokePressedColor;
    private Integer mStrokeCheckedColor;
    private Integer mStrokeDisabledColor;
    private Integer mStrokeFocusedColor;
    private Integer mStrokeSelectedColor;

    private int[] mStrokeGradientColors;
    private ShapeGradientOrientation mStrokeGradientOrientation;
    /** 任意角度描边渐变；NaN 表示使用 mStrokeGradientOrientation。 */
    private float mStrokeGradientAngle = Float.NaN;

    private int mStrokeSize;
    private int mStrokeDashSize;
    private int mStrokeDashGap;

    /** 阴影参数由最外层 ShadowDrawable 使用，不交给内容 ShapeDrawable 重复绘制。 */
    private int mShadowSize;
    private int mShadowColor;
    private int mShadowOffsetX;
    private int mShadowOffsetY;
    private boolean mShadowHidden;
    private float mShadowSpread;
    private boolean mShadowSymmetry;
    private boolean mShadowHiddenLeft;
    private boolean mShadowHiddenTop;
    private boolean mShadowHiddenRight;
    private boolean mShadowHiddenBottom;
    private float mShadowBitmapScale = 0.5f;

    private int mRingInnerRadiusSize;
    private float mRingInnerRadiusRatio;
    private int mRingThicknessSize;
    private float mRingThicknessRatio;

    private int mLineGravity;

    /** Ripple 默认关闭，默认颜色为低透明度黑色。 */
    private boolean mRippleEnable;
    private int mRippleColor = 0x24000000;

    /** 各状态可直接指定任意 Drawable，构建时按当前 Shape 轮廓裁剪。 */
    private Drawable mBackgroundDrawable;
    private Drawable mPressedBackgroundDrawable;
    private Drawable mCheckedBackgroundDrawable;
    private Drawable mDisabledBackgroundDrawable;
    private Drawable mFocusedBackgroundDrawable;
    private Drawable mSelectedBackgroundDrawable;
    /** shape_clickable=false 时显示的独立背景；不参与 android:enabled 状态。 */
    private Drawable mNonClickableBackgroundDrawable;
    /** 只有显式配置 shape_clickable 或通过 Builder 设置后，才接管 View 的 clickable。 */
    private boolean mShapeClickableConfigured;
    private boolean mShapeClickable = true;
    private boolean mClipExistingBackground;

    public ShapeDrawableBuilder(View view, TypedArray typedArray, IShapeDrawableStyleable styleable) {
        this(view, null, typedArray, styleable);
    }

    public ShapeDrawableBuilder(View view, @Nullable AttributeSet attrs,
                                TypedArray typedArray, IShapeDrawableStyleable styleable) {
        mView = view;
        // 保存业务原始 padding，动态重建阴影时从同一基准计算，避免反复累加 inset。
        mOriginalPaddingStart = view.getPaddingStart();
        mOriginalPaddingTop = view.getPaddingTop();
        mOriginalPaddingEnd = view.getPaddingEnd();
        mOriginalPaddingBottom = view.getPaddingBottom();
        // 先读取基础 Shape 属性，再读取状态、Ripple、自定义背景和阴影扩展属性。
        mType = typedArray.getInt(styleable.getShapeTypeStyleable(), ShapeType.RECTANGLE);
        mWidth = typedArray.getDimensionPixelSize(styleable.getShapeWidthStyleable(), -1);
        mHeight = typedArray.getDimensionPixelSize(styleable.getShapeHeightStyleable(), -1);
        mClipExistingBackground = typedArray.hasValue(styleable.getShapeTypeStyleable()) ||
                typedArray.hasValue(styleable.getRadiusStyleable()) ||
                typedArray.hasValue(styleable.getRadiusInTopLeftStyleable()) ||
                typedArray.hasValue(styleable.getRadiusInTopStartStyleable()) ||
                typedArray.hasValue(styleable.getRadiusInTopRightStyleable()) ||
                typedArray.hasValue(styleable.getRadiusInTopEndStyleable()) ||
                typedArray.hasValue(styleable.getRadiusInBottomLeftStyleable()) ||
                typedArray.hasValue(styleable.getRadiusInBottomStartStyleable()) ||
                typedArray.hasValue(styleable.getRadiusInBottomRightStyleable()) ||
                typedArray.hasValue(styleable.getRadiusInBottomEndStyleable());

        mSolidColor = typedArray.getColor(styleable.getSolidColorStyleable(), NO_COLOR);
        if (typedArray.hasValue(styleable.getSolidPressedColorStyleable())) {
            mSolidPressedColor = typedArray.getColor(styleable.getSolidPressedColorStyleable(), NO_COLOR);
        }
        if (styleable.getSolidCheckedColorStyleable() > 0 && typedArray.hasValue(styleable.getSolidCheckedColorStyleable())) {
            mSolidCheckedColor = typedArray.getColor(styleable.getSolidCheckedColorStyleable(), NO_COLOR);
        }
        if (typedArray.hasValue(styleable.getSolidDisabledColorStyleable())) {
            mSolidDisabledColor = typedArray.getColor(styleable.getSolidDisabledColorStyleable(), NO_COLOR);
        }
        if (typedArray.hasValue(styleable.getSolidFocusedColorStyleable())) {
            mSolidFocusedColor = typedArray.getColor(styleable.getSolidFocusedColorStyleable(), NO_COLOR);
        }
        if (typedArray.hasValue(styleable.getSolidSelectedColorStyleable())) {
            mSolidSelectedColor = typedArray.getColor(styleable.getSolidSelectedColorStyleable(), NO_COLOR);
        }

        int layoutDirection = getLayoutDirection(view);

        int radius = typedArray.getDimensionPixelSize(styleable.getRadiusStyleable(), 0);
        mTopLeftRadius = mTopRightRadius = mBottomLeftRadius = mBottomRightRadius = radius;

        if (attrs != null) {
            TypedArray extrasArray = view.getContext().obtainStyledAttributes(attrs, R.styleable.ShapeExtras);
            if (extrasArray.hasValue(R.styleable.ShapeExtras_shape_radiusInTop)) {
                mClipExistingBackground = true;
                float topRadius = extrasArray.getDimension(
                        R.styleable.ShapeExtras_shape_radiusInTop, radius);
                mTopLeftRadius = topRadius;
                mTopRightRadius = topRadius;
            }
            if (extrasArray.hasValue(R.styleable.ShapeExtras_shape_radiusInBottom)) {
                mClipExistingBackground = true;
                float bottomRadius = extrasArray.getDimension(
                        R.styleable.ShapeExtras_shape_radiusInBottom, radius);
                mBottomLeftRadius = bottomRadius;
                mBottomRightRadius = bottomRadius;
            }
            mRippleEnable = extrasArray.getBoolean(
                    R.styleable.ShapeExtras_shape_rippleEnable, false);
            mRippleColor = extrasArray.getColor(
                    R.styleable.ShapeExtras_shape_rippleColor, 0x24000000);
            // 多色数组是对原有 start/center/end 三色入口的扩展，配置后优先使用数组。
            mSolidGradientColors = readColorArray(extrasArray,
                    R.styleable.ShapeExtras_shape_solidGradientColors);
            mStrokeGradientColors = readColorArray(extrasArray,
                    R.styleable.ShapeExtras_shape_strokeGradientColors);
            if (extrasArray.hasValue(R.styleable.ShapeExtras_shape_solidGradientAngle)) {
                mSolidGradientAngle = extrasArray.getFloat(
                        R.styleable.ShapeExtras_shape_solidGradientAngle, Float.NaN);
            }
            if (extrasArray.hasValue(R.styleable.ShapeExtras_shape_strokeGradientAngle)) {
                mStrokeGradientAngle = extrasArray.getFloat(
                        R.styleable.ShapeExtras_shape_strokeGradientAngle, Float.NaN);
            }
            // 这两个属性与 android:enabled 分离，必须使用 ShapeExtras 中的值读取，
            // 避免额外创建重复的 styleable，也保证所有 Shape 控件共用同一套属性索引。
            mShapeClickableConfigured = extrasArray.hasValue(
                    R.styleable.ShapeExtras_shape_clickable);
            mShapeClickable = extrasArray.getBoolean(
                    R.styleable.ShapeExtras_shape_clickable, true);
            mNonClickableBackgroundDrawable = extrasArray.getDrawable(
                    R.styleable.ShapeExtras_shape_nonClickableBackground);
            extrasArray.recycle();

            TypedArray backgroundArray = view.getContext().obtainStyledAttributes(
                    attrs, R.styleable.ShapeBackgroundState);
            mBackgroundDrawable = backgroundArray.getDrawable(
                    R.styleable.ShapeBackgroundState_shape_background);
            mPressedBackgroundDrawable = backgroundArray.getDrawable(
                    R.styleable.ShapeBackgroundState_shape_pressedBackground);
            mCheckedBackgroundDrawable = backgroundArray.getDrawable(
                    R.styleable.ShapeBackgroundState_shape_checkedBackground);
            mDisabledBackgroundDrawable = backgroundArray.getDrawable(
                    R.styleable.ShapeBackgroundState_shape_disabledBackground);
            mFocusedBackgroundDrawable = backgroundArray.getDrawable(
                    R.styleable.ShapeBackgroundState_shape_focusedBackground);
            mSelectedBackgroundDrawable = backgroundArray.getDrawable(
                    R.styleable.ShapeBackgroundState_shape_selectedBackground);
            backgroundArray.recycle();
        }

        if (typedArray.hasValue(styleable.getRadiusInTopStartStyleable())) {
            switch (layoutDirection) {
                case View.LAYOUT_DIRECTION_RTL:
                    mTopRightRadius = typedArray.getDimensionPixelSize(styleable.getRadiusInTopStartStyleable(), radius);
                    break;
                case View.LAYOUT_DIRECTION_LTR:
                default:
                    mTopLeftRadius = typedArray.getDimensionPixelSize(styleable.getRadiusInTopStartStyleable(), radius);
                    break;
            }
        }
        if (typedArray.hasValue(styleable.getRadiusInTopEndStyleable())) {
            switch (layoutDirection) {
                case View.LAYOUT_DIRECTION_RTL:
                    mTopLeftRadius = typedArray.getDimensionPixelSize(styleable.getRadiusInTopEndStyleable(), radius);
                    break;
                case View.LAYOUT_DIRECTION_LTR:
                default:
                    mTopRightRadius = typedArray.getDimensionPixelSize(styleable.getRadiusInTopEndStyleable(), radius);
                    break;
            }
        }
        if (typedArray.hasValue(styleable.getRadiusInBottomStartStyleable())) {
            switch (layoutDirection) {
                case View.LAYOUT_DIRECTION_RTL:
                    mBottomRightRadius = typedArray.getDimensionPixelSize(styleable.getRadiusInBottomStartStyleable(), radius);
                    break;
                case View.LAYOUT_DIRECTION_LTR:
                default:
                    mBottomLeftRadius = typedArray.getDimensionPixelSize(styleable.getRadiusInBottomStartStyleable(), radius);
                    break;
            }
        }
        if (typedArray.hasValue(styleable.getRadiusInBottomEndStyleable())) {
            switch (layoutDirection) {
                case View.LAYOUT_DIRECTION_RTL:
                    mBottomLeftRadius = typedArray.getDimensionPixelSize(styleable.getRadiusInBottomEndStyleable(), radius);
                    break;
                case View.LAYOUT_DIRECTION_LTR:
                default:
                    mBottomRightRadius = typedArray.getDimensionPixelSize(styleable.getRadiusInBottomEndStyleable(), radius);
                    break;
            }
        }

        if (typedArray.hasValue(styleable.getRadiusInTopLeftStyleable())) {
            mTopLeftRadius = typedArray.getDimensionPixelSize(styleable.getRadiusInTopLeftStyleable(), radius);
        }
        if (typedArray.hasValue(styleable.getRadiusInTopRightStyleable())) {
            mTopRightRadius = typedArray.getDimensionPixelSize(styleable.getRadiusInTopRightStyleable(), radius);
        }
        if (typedArray.hasValue(styleable.getRadiusInBottomLeftStyleable())) {
            mBottomLeftRadius = typedArray.getDimensionPixelSize(styleable.getRadiusInBottomLeftStyleable(), radius);
        }
        if (typedArray.hasValue(styleable.getRadiusInBottomRightStyleable())) {
            mBottomRightRadius = typedArray.getDimensionPixelSize(styleable.getRadiusInBottomRightStyleable(), radius);
        }

        if (mSolidGradientColors == null &&
                typedArray.hasValue(styleable.getSolidGradientStartColorStyleable()) &&
                typedArray.hasValue(styleable.getSolidGradientEndColorStyleable())) {
            if (typedArray.hasValue(styleable.getSolidGradientCenterColorStyleable())) {
                mSolidGradientColors = new int[] {typedArray.getColor(styleable.getSolidGradientStartColorStyleable(), NO_COLOR),
                        typedArray.getColor(styleable.getSolidGradientCenterColorStyleable(), NO_COLOR),
                        typedArray.getColor(styleable.getSolidGradientEndColorStyleable(), NO_COLOR)};
            } else {
                mSolidGradientColors = new int[] {typedArray.getColor(styleable.getSolidGradientStartColorStyleable(), NO_COLOR),
                        typedArray.getColor(styleable.getSolidGradientEndColorStyleable(), NO_COLOR)};
            }
        }

        mSolidGradientOrientation = transformGradientOrientation(typedArray.getInt(styleable.getSolidGradientOrientationStyleable(),
                                                                    getDefaultGradientOrientation()));
        mSolidGradientType = typedArray.getInt(styleable.getSolidGradientTypeStyleable(), ShapeGradientType.LINEAR_GRADIENT);
        mSolidGradientCenterX = typedArray.getFloat(styleable.getSolidGradientCenterXStyleable(), 0.5f);
        mSolidGradientCenterY = typedArray.getFloat(styleable.getSolidGradientCenterYStyleable(), 0.5f);
        mSolidGradientRadius = typedArray.getDimensionPixelSize(styleable.getSolidGradientRadiusStyleable(), radius);

        mStrokeColor = typedArray.getColor(styleable.getStrokeColorStyleable(), NO_COLOR);
        if (typedArray.hasValue(styleable.getStrokePressedColorStyleable())) {
            mStrokePressedColor = typedArray.getColor(styleable.getStrokePressedColorStyleable(), NO_COLOR);
        }
        if (styleable.getStrokeCheckedColorStyleable() > 0 && typedArray.hasValue(styleable.getStrokeCheckedColorStyleable())) {
            mStrokeCheckedColor = typedArray.getColor(styleable.getStrokeCheckedColorStyleable(), NO_COLOR);
        }
        if (typedArray.hasValue(styleable.getStrokeDisabledColorStyleable())) {
            mStrokeDisabledColor = typedArray.getColor(styleable.getStrokeDisabledColorStyleable(), NO_COLOR);
        }
        if (typedArray.hasValue(styleable.getStrokeFocusedColorStyleable())) {
            mStrokeFocusedColor = typedArray.getColor(styleable.getStrokeFocusedColorStyleable(), NO_COLOR);
        }
        if (typedArray.hasValue(styleable.getStrokeSelectedColorStyleable())) {
            mStrokeSelectedColor = typedArray.getColor(styleable.getStrokeSelectedColorStyleable(), NO_COLOR);
        }

        if (mStrokeGradientColors == null &&
                typedArray.hasValue(styleable.getStrokeGradientStartColorStyleable()) &&
                typedArray.hasValue(styleable.getStrokeGradientEndColorStyleable())) {
            if (typedArray.hasValue(styleable.getStrokeGradientCenterColorStyleable())) {
                mStrokeGradientColors = new int[] {typedArray.getColor(styleable.getStrokeGradientStartColorStyleable(), NO_COLOR),
                        typedArray.getColor(styleable.getStrokeGradientCenterColorStyleable(), NO_COLOR),
                        typedArray.getColor(styleable.getStrokeGradientEndColorStyleable(), NO_COLOR)};
            } else {
                mStrokeGradientColors = new int[] {typedArray.getColor(styleable.getStrokeGradientStartColorStyleable(), NO_COLOR),
                        typedArray.getColor(styleable.getStrokeGradientEndColorStyleable(), NO_COLOR)};
            }
        }

        mStrokeGradientOrientation = transformGradientOrientation(typedArray.getInt(styleable.getStrokeGradientOrientationStyleable(),
                                                                    getDefaultGradientOrientation()));

        mStrokeSize = typedArray.getDimensionPixelSize(styleable.getStrokeSizeStyleable(), 0);
        mStrokeDashSize = typedArray.getDimensionPixelSize(styleable.getStrokeDashSizeStyleable(), 0);
        mStrokeDashGap = typedArray.getDimensionPixelSize(styleable.getStrokeDashGapStyleable(), 0);

        mShadowSize = typedArray.getDimensionPixelSize(styleable.getShadowSizeStyleable(), 0);
        mShadowColor = typedArray.getColor(styleable.getShadowColorStyleable(), 0x40000000);
        mShadowOffsetX = typedArray.getDimensionPixelOffset(styleable.getShadowOffsetXStyleable(), 0);
        mShadowOffsetY = typedArray.getDimensionPixelOffset(styleable.getShadowOffsetYStyleable(), 0);
        if (attrs != null) {
            TypedArray shadowArray = view.getContext().obtainStyledAttributes(attrs, R.styleable.ShapeShadow);
            mShadowHidden = shadowArray.getBoolean(
                    R.styleable.ShapeShadow_shape_shadowHidden, false);
            mShadowSpread = shadowArray.getDimension(R.styleable.ShapeShadow_shape_shadowSpread, 0f);
            mShadowSymmetry = shadowArray.getBoolean(R.styleable.ShapeShadow_shape_shadowSymmetry, false);
            mShadowHiddenLeft = shadowArray.getBoolean(R.styleable.ShapeShadow_shape_shadowHiddenLeft, false);
            mShadowHiddenTop = shadowArray.getBoolean(R.styleable.ShapeShadow_shape_shadowHiddenTop, false);
            mShadowHiddenRight = shadowArray.getBoolean(R.styleable.ShapeShadow_shape_shadowHiddenRight, false);
            mShadowHiddenBottom = shadowArray.getBoolean(R.styleable.ShapeShadow_shape_shadowHiddenBottom, false);
            mShadowBitmapScale = shadowArray.getFloat(
                    R.styleable.ShapeShadow_shape_shadowBitmapScale, 0.5f);
            shadowArray.recycle();
        }

        mRingInnerRadiusSize = typedArray.getDimensionPixelOffset(styleable.getRingInnerRadiusSizeStyleable(), -1);
        mRingInnerRadiusRatio = typedArray.getFloat(styleable.getRingInnerRadiusRatioStyleable(), 3.0f);
        mRingThicknessSize = typedArray.getDimensionPixelOffset(styleable.getRingThicknessSizeStyleable(), -1);
        mRingThicknessRatio = typedArray.getFloat(styleable.getRingThicknessRatioStyleable(), 9.0f);

        mLineGravity = typedArray.getInt(styleable.getLineGravityStyleable(), Gravity.CENTER);
    }

    public ShapeDrawableBuilder setType(@ShapeTypeLimit int type) {
        mType = type;
        return this;
    }

    @ShapeTypeLimit
    public int getType() {
        return mType;
    }

    public ShapeDrawableBuilder setWidth(int width) {
        mWidth = width;
        return this;
    }

    public int getWidth() {
        return mWidth;
    }

    public ShapeDrawableBuilder setHeight(int height) {
        mHeight = height;
        return this;
    }

    public int getHeight() {
        return mHeight;
    }

    public ShapeDrawableBuilder setRadius(float radius) {
        // 统一圆角覆盖四个角，调用方仍可在之后单独修改某个角。
        return setRadius(radius, radius, radius, radius);
    }

    public ShapeDrawableBuilder setRadius(float topLeftRadius, float topRightRadius,
                                          float bottomLeftRadius, float bottomRightRadius) {
        mTopLeftRadius = topLeftRadius;
        mTopRightRadius = topRightRadius;
        mBottomLeftRadius = bottomLeftRadius;
        mBottomRightRadius = bottomRightRadius;
        return this;
    }

    public ShapeDrawableBuilder setRadiusRelative(float topStartRadius, float topEndRadius,
                                                    float bottomStartRadius, float bottomEndRadius) {
        int layoutDirection = mView.getLayoutDirection();
        switch (layoutDirection) {
            case View.LAYOUT_DIRECTION_RTL:
                mTopLeftRadius = topEndRadius;
                mTopRightRadius = topStartRadius;
                mBottomLeftRadius = bottomEndRadius;
                mBottomRightRadius = bottomStartRadius;
                break;
            case View.LAYOUT_DIRECTION_LTR:
            default:
                mTopLeftRadius = topStartRadius;
                mTopRightRadius = topEndRadius;
                mBottomLeftRadius = bottomStartRadius;
                mBottomRightRadius = bottomEndRadius;
                break;
        }
        return this;
    }

    public ShapeDrawableBuilder setTopLeftRadius(float radius) {
        mTopLeftRadius = radius;
        return this;
    }

    public float getTopLeftRadius() {
        return mTopLeftRadius;
    }

    public ShapeDrawableBuilder setTopRightRadius(float radius) {
        mTopRightRadius = radius;
        return this;
    }

    public float getTopRightRadius() {
        return mTopRightRadius;
    }

    public ShapeDrawableBuilder setBottomLeftRadius(float radius) {
        mBottomLeftRadius = radius;
        return this;
    }

    public float getBottomLeftRadius() {
        return mBottomLeftRadius;
    }

    public ShapeDrawableBuilder setBottomRightRadius(float radius) {
        mBottomRightRadius = radius;
        return this;
    }

    public float getBottomRightRadius() {
        return mBottomRightRadius;
    }

    public ShapeDrawableBuilder setSolidColor(int color) {
        mSolidColor = color;
        clearSolidGradientColors();
        clearSolidGradientAngle();
        return this;
    }

    public int getSolidColor() {
        return mSolidColor;
    }

    public ShapeDrawableBuilder setSolidPressedColor(Integer color) {
        mSolidPressedColor = color;
        return this;
    }

    @Nullable
    public Integer getSolidPressedColor() {
        return mSolidPressedColor;
    }

    public ShapeDrawableBuilder setSolidCheckedColor(Integer color) {
        mSolidCheckedColor = color;
        return this;
    }

    @Nullable
    public Integer getSolidCheckedColor() {
        return mSolidCheckedColor;
    }

    public ShapeDrawableBuilder setSolidDisabledColor(Integer color) {
        mSolidDisabledColor = color;
        return this;
    }

    @Nullable
    public Integer getSolidDisabledColor() {
        return mSolidDisabledColor;
    }

    public ShapeDrawableBuilder setSolidFocusedColor(Integer color) {
        mSolidFocusedColor = color;
        return this;
    }

    @Nullable
    public Integer getSolidFocusedColor() {
        return mSolidFocusedColor;
    }

    public ShapeDrawableBuilder setSolidSelectedColor(Integer color) {
        mSolidSelectedColor = color;
        return this;
    }

    @Nullable
    public Integer getSolidSelectedColor() {
        return mSolidSelectedColor;
    }

    public ShapeDrawableBuilder setSolidGradientColors(int startColor, int endColor) {
        return setSolidGradientColors(new int[]{startColor, endColor});
    }

    public ShapeDrawableBuilder setSolidGradientColors(int startColor, int centerColor, int endColor) {
        return setSolidGradientColors(new int[]{startColor, centerColor, endColor});
    }

    public ShapeDrawableBuilder setSolidGradientColors(int[] colors) {
        // 复制调用方数组，避免业务后续修改数组内容时悄悄改变控件外观。
        mSolidGradientColors = colors == null ? null : colors.clone();
        return this;
    }

    @Nullable
    public int[] getSolidGradientColors() {
        return mSolidGradientColors;
    }

    public boolean isSolidGradientColorsEnable() {
        return mSolidGradientColors != null &&
                mSolidGradientColors.length > 1;
    }

    public void clearSolidGradientColors() {
        mSolidGradientColors = null;
    }

    public ShapeDrawableBuilder setSolidGradientOrientation(ShapeGradientOrientation orientation) {
        mSolidGradientOrientation = orientation;
        // 方向枚举与任意角度是互斥入口，最后一次设置的方式拥有优先权。
        mSolidGradientAngle = Float.NaN;
        return this;
    }

    /** 设置填充线性渐变角度；0 度为左到右，90 度为下到上，支持任意正负角度。 */
    public ShapeDrawableBuilder setSolidGradientAngle(float angle) {
        mSolidGradientAngle = angle;
        return this;
    }

    /** 清除填充渐变角度，恢复使用方向枚举。 */
    public ShapeDrawableBuilder clearSolidGradientAngle() {
        mSolidGradientAngle = Float.NaN;
        return this;
    }

    /** 返回填充渐变角度；NaN 表示当前未配置角度。 */
    public float getSolidGradientAngle() {
        return mSolidGradientAngle;
    }

    public ShapeGradientOrientation getSolidGradientOrientation() {
        return mSolidGradientOrientation;
    }

    public ShapeDrawableBuilder setSolidGradientType(@ShapeGradientTypeLimit int type) {
        mSolidGradientType = type;
        return this;
    }

    @ShapeGradientTypeLimit
    public int getSolidGradientType() {
        return mSolidGradientType;
    }

    public ShapeDrawableBuilder setSolidGradientCenterX(float centerX) {
        mSolidGradientCenterX = centerX;
        return this;
    }

    public float getSolidGradientCenterX() {
        return mSolidGradientCenterX;
    }

    public ShapeDrawableBuilder setSolidGradientCenterY(float centerY) {
        mSolidGradientCenterY = centerY;
        return this;
    }

    public float getSolidGradientCenterY() {
        return mSolidGradientCenterY;
    }

    public ShapeDrawableBuilder setSolidGradientRadius(int radius) {
        mSolidGradientRadius = radius;
        return this;
    }

    public int getSolidGradientRadius() {
        return mSolidGradientRadius;
    }

    public ShapeDrawableBuilder setStrokeColor(int color) {
        mStrokeColor = color;
        clearStrokeGradientColors();
        clearStrokeGradientAngle();
        return this;
    }

    public int getStrokeColor() {
        return mStrokeColor;
    }

    public ShapeDrawableBuilder setStrokePressedColor(Integer color) {
        mStrokePressedColor = color;
        return this;
    }

    @Nullable
    public Integer getStrokePressedColor() {
        return mStrokePressedColor;
    }

    public ShapeDrawableBuilder setStrokeCheckedColor(Integer color) {
        mStrokeCheckedColor = color;
        return this;
    }

    @Nullable
    public Integer getStrokeCheckedColor() {
        return mStrokeCheckedColor;
    }

    public ShapeDrawableBuilder setStrokeDisabledColor(Integer color) {
        mStrokeDisabledColor = color;
        return this;
    }

    @Nullable
    public Integer getStrokeDisabledColor() {
        return mStrokeDisabledColor;
    }

    public ShapeDrawableBuilder setStrokeFocusedColor(Integer color) {
        mStrokeFocusedColor = color;
        return this;
    }

    @Nullable
    public Integer getStrokeFocusedColor() {
        return mStrokeFocusedColor;
    }

    public ShapeDrawableBuilder setStrokeSelectedColor(Integer color) {
        mStrokeSelectedColor = color;
        return this;
    }

    @Nullable
    public Integer getStrokeSelectedColor() {
        return mStrokeSelectedColor;
    }

    public ShapeDrawableBuilder setStrokeGradientColors(int startColor, int endColor) {
        return setStrokeGradientColors(new int[]{startColor, endColor});
    }

    public ShapeDrawableBuilder setStrokeGradientColors(int startColor, int centerColor, int endColor) {
        return setStrokeGradientColors(new int[]{startColor, centerColor, endColor});
    }

    public ShapeDrawableBuilder setStrokeGradientColors(int[] colors) {
        // 与填充渐变保持同样的数组隔离规则，防止外部可变数组污染 Drawable 状态。
        mStrokeGradientColors = colors == null ? null : colors.clone();
        return this;
    }

    @Nullable
    public int[] getStrokeGradientColors() {
        return mStrokeGradientColors;
    }

    public boolean isStrokeGradientColorsEnable() {
        return mStrokeGradientColors != null &&
                mStrokeGradientColors.length > 1;
    }

    public void clearStrokeGradientColors() {
        mStrokeGradientColors = null;
    }

    public ShapeDrawableBuilder setStrokeGradientOrientation(ShapeGradientOrientation orientation) {
        mStrokeGradientOrientation = orientation;
        mStrokeGradientAngle = Float.NaN;
        return this;
    }

    /** 设置描边线性渐变角度，角度定义与 setSolidGradientAngle 相同。 */
    public ShapeDrawableBuilder setStrokeGradientAngle(float angle) {
        mStrokeGradientAngle = angle;
        return this;
    }

    /** 清除描边渐变角度，恢复使用方向枚举。 */
    public ShapeDrawableBuilder clearStrokeGradientAngle() {
        mStrokeGradientAngle = Float.NaN;
        return this;
    }

    /** 返回描边渐变角度；NaN 表示当前未配置角度。 */
    public float getStrokeGradientAngle() {
        return mStrokeGradientAngle;
    }

    public ShapeGradientOrientation getStrokeGradientOrientation() {
        return mStrokeGradientOrientation;
    }

    public ShapeDrawableBuilder setStrokeSize(int size) {
        mStrokeSize = size;
        return this;
    }

    public int getStrokeSize() {
        return mStrokeSize;
    }

    public ShapeDrawableBuilder setStrokeDashSize(int size) {
        mStrokeDashSize = size;
        return this;
    }

    public int getStrokeDashSize() {
        return mStrokeDashSize;
    }

    public ShapeDrawableBuilder setStrokeDashGap(int gap) {
        mStrokeDashGap = gap;
        return this;
    }

    public int getStrokeDashGap() {
        return mStrokeDashGap;
    }

    public boolean isStrokeDashLineEnable() {
        return mStrokeDashGap > 0;
    }

    public ShapeDrawableBuilder setRingInnerRadiusSize(int size) {
        mRingInnerRadiusSize = size;
        return this;
    }

    public int getRingInnerRadiusSize() {
        return mRingInnerRadiusSize;
    }

    public ShapeDrawableBuilder setRingInnerRadiusRatio(float ratio) {
        mRingInnerRadiusRatio = ratio;
        return this;
    }

    public float getRingInnerRadiusRatio() {
        return mRingInnerRadiusRatio;
    }

    public ShapeDrawableBuilder setRingThicknessSize(int size) {
        mRingThicknessSize = size;
        return this;
    }

    public int getRingThicknessSize() {
        return mRingThicknessSize;
    }

    public ShapeDrawableBuilder setRingThicknessRatio(float ratio) {
        mRingThicknessRatio = ratio;
        return this;
    }

    public float getRingThicknessRatio() {
        return mRingThicknessRatio;
    }

    public boolean isShadowEnable() {
        // 阴影半径大于 0 且未主动隐藏时才创建 ShadowDrawable。
        return mShadowSize > 0 && !mShadowHidden;
    }

    /**
     * 计算当前阴影占用的四边空间。
     *
     * <p>容器的子 View 裁剪需要使用与背景 Drawable 相同的内容边界，因此这里复用
     * ShadowDrawable 的空间规则，而不是直接读取 View 当前 padding。View 的原始 padding
     * 还包含业务内容间距，不能作为圆角裁剪路径的外边界。</p>
     */
    public void getShadowInsets(@NonNull Rect outInsets) {
        if (!isShadowEnable()) {
            outInsets.setEmpty();
            return;
        }

        float extent = mShadowSize + mShadowSpread;
        if (mShadowSymmetry) {
            int horizontal = (int) Math.ceil(extent + Math.abs(mShadowOffsetX));
            int vertical = (int) Math.ceil(extent + Math.abs(mShadowOffsetY));
            outInsets.set(
                    mShadowHiddenLeft ? 0 : horizontal,
                    mShadowHiddenTop ? 0 : vertical,
                    mShadowHiddenRight ? 0 : horizontal,
                    mShadowHiddenBottom ? 0 : vertical);
            return;
        }

        outInsets.set(
                mShadowHiddenLeft ? 0 : (int) Math.ceil(extent + Math.max(0, -mShadowOffsetX)),
                mShadowHiddenTop ? 0 : (int) Math.ceil(extent + Math.max(0, -mShadowOffsetY)),
                mShadowHiddenRight ? 0 : (int) Math.ceil(extent + Math.max(0, mShadowOffsetX)),
                mShadowHiddenBottom ? 0 : (int) Math.ceil(extent + Math.max(0, mShadowOffsetY)));
    }

    /** 返回是否显式配置了独立的 shape_clickable 行为。 */
    public boolean isShapeClickableConfigured() {
        return mShapeClickableConfigured;
    }

    /** 返回当前独立点击开关；未配置时默认为 true。 */
    public boolean isShapeClickable() {
        return mShapeClickable;
    }

    /**
     * 返回是否应该拦截触摸事件。
     *
     * <p>单独判断配置标志，避免库为了默认状态而把 ImageView、普通 View 等控件强制变成
     * clickable；只有调用方明确使用 shape_clickable 时，才启用独立点击语义。</p>
     */
    public boolean shouldBlockTouch() {
        return mShapeClickableConfigured && !mShapeClickable;
    }

    /** 设置独立点击开关；应用到背景和 View 事件行为仍需调用 intoBackground。 */
    public ShapeDrawableBuilder setShapeClickable(boolean clickable) {
        mShapeClickableConfigured = true;
        mShapeClickable = clickable;
        return this;
    }

    /** 设置 shape_clickable=false 时显示的背景 Drawable。 */
    public ShapeDrawableBuilder setNonClickableBackgroundDrawable(@Nullable Drawable drawable) {
        mNonClickableBackgroundDrawable = drawable;
        return this;
    }

    /** 设置 shape_clickable=false 时显示的纯色背景。 */
    public ShapeDrawableBuilder setNonClickableBackgroundColor(int color) {
        mNonClickableBackgroundDrawable = new ColorDrawable(color);
        return this;
    }

    /** 返回 shape_clickable=false 时配置的背景 Drawable。 */
    @Nullable
    public Drawable getNonClickableBackgroundDrawable() {
        return mNonClickableBackgroundDrawable;
    }

    public ShapeDrawableBuilder setShadowHidden(boolean hidden) {
        mShadowHidden = hidden;
        return this;
    }

    public ShapeDrawableBuilder setShadowSize(int size) {
        mShadowSize = size;
        return this;
    }

    public int getShadowSize() {
        return mShadowSize;
    }

    public ShapeDrawableBuilder setShadowColor(int color) {
        mShadowColor = color;
        return this;
    }

    public int getShadowColor() {
        return mShadowColor;
    }

    public ShapeDrawableBuilder setShadowOffsetX(int offsetX) {
        mShadowOffsetX = offsetX;
        return this;
    }

    public int getShadowOffsetX() {
        return mShadowOffsetX;
    }

    public ShapeDrawableBuilder setShadowOffsetY(int offsetY) {
        mShadowOffsetY = offsetY;
        return this;
    }

    public int getShadowOffsetY() {
        return mShadowOffsetY;
    }

    public ShapeDrawableBuilder setShadowSpread(float spread) {
        mShadowSpread = Math.max(0f, spread);
        return this;
    }

    public float getShadowSpread() {
        return mShadowSpread;
    }

    public ShapeDrawableBuilder setShadowSymmetry(boolean symmetry) {
        mShadowSymmetry = symmetry;
        return this;
    }

    public boolean isShadowSymmetry() {
        return mShadowSymmetry;
    }

    public ShapeDrawableBuilder setShadowHiddenLeft(boolean hidden) {
        mShadowHiddenLeft = hidden;
        return this;
    }

    public ShapeDrawableBuilder setShadowHiddenTop(boolean hidden) {
        mShadowHiddenTop = hidden;
        return this;
    }

    public ShapeDrawableBuilder setShadowHiddenRight(boolean hidden) {
        mShadowHiddenRight = hidden;
        return this;
    }

    public ShapeDrawableBuilder setShadowHiddenBottom(boolean hidden) {
        mShadowHiddenBottom = hidden;
        return this;
    }

    public ShapeDrawableBuilder setShadowBitmapScale(float bitmapScale) {
        mShadowBitmapScale = bitmapScale;
        return this;
    }

    public int getLineGravity() {
        return mLineGravity;
    }

    public ShapeDrawableBuilder setLineGravity(int gravity) {
        mLineGravity = gravity;
        return this;
    }

    public ShapeDrawableBuilder setTopRadius(float radius) {
        mTopLeftRadius = radius;
        mTopRightRadius = radius;
        return this;
    }

    public ShapeDrawableBuilder setBottomRadius(float radius) {
        mBottomLeftRadius = radius;
        mBottomRightRadius = radius;
        return this;
    }

    public ShapeDrawableBuilder setRippleEnable(boolean enable) {
        mRippleEnable = enable;
        return this;
    }

    public boolean isRippleEnable() {
        return mRippleEnable;
    }

    public ShapeDrawableBuilder setRippleColor(int color) {
        mRippleColor = color;
        return this;
    }

    public int getRippleColor() {
        return mRippleColor;
    }

    public ShapeDrawableBuilder setBackgroundDrawable(@Nullable Drawable drawable) {
        mBackgroundDrawable = drawable;
        return this;
    }

    public ShapeDrawableBuilder setPressedBackgroundDrawable(@Nullable Drawable drawable) {
        mPressedBackgroundDrawable = drawable;
        return this;
    }

    public ShapeDrawableBuilder setCheckedBackgroundDrawable(@Nullable Drawable drawable) {
        mCheckedBackgroundDrawable = drawable;
        return this;
    }

    public ShapeDrawableBuilder setDisabledBackgroundDrawable(@Nullable Drawable drawable) {
        mDisabledBackgroundDrawable = drawable;
        return this;
    }

    public ShapeDrawableBuilder setFocusedBackgroundDrawable(@Nullable Drawable drawable) {
        mFocusedBackgroundDrawable = drawable;
        return this;
    }

    public ShapeDrawableBuilder setSelectedBackgroundDrawable(@Nullable Drawable drawable) {
        mSelectedBackgroundDrawable = drawable;
        return this;
    }

    @Nullable
    public Drawable buildBackgroundDrawable() {
        // 每次构建都创建新的状态容器，避免旧 Drawable.Callback 指向错误的 View。
        boolean hasSolidColorState = mSolidPressedColor != null || mSolidCheckedColor != null ||
                mSolidDisabledColor != null || mSolidFocusedColor != null || mSolidSelectedColor != null;

        boolean hasStrokeColorState = mStrokePressedColor != null || mStrokeCheckedColor != null ||
                mStrokeDisabledColor != null || mStrokeFocusedColor != null || mStrokeSelectedColor != null;

        boolean hasDrawableState = mPressedBackgroundDrawable != null ||
                mCheckedBackgroundDrawable != null || mDisabledBackgroundDrawable != null ||
                mFocusedBackgroundDrawable != null || mSelectedBackgroundDrawable != null;
        boolean hasNonClickableBackground = !mShapeClickable &&
                mNonClickableBackgroundDrawable != null;

        boolean hasGeneratedShape = isSolidGradientColorsEnable() || isStrokeGradientColorsEnable() ||
                mSolidColor != NO_COLOR || hasSolidColorState ||
                mStrokeColor != NO_COLOR || hasStrokeColorState;

        Drawable currentBackground = unwrapDecoratedDrawable(mView.getBackground());

        if (!hasGeneratedShape && mBackgroundDrawable == null && !hasDrawableState &&
                !hasNonClickableBackground) {
            return decorateDrawable(mClipExistingBackground ?
                    prepareCustomDrawable(currentBackground) : currentBackground);
        }

        Drawable defaultDrawable;
        if (hasNonClickableBackground) {
            // 独立不可点击背景优先于普通背景，但不会改变 View 的 enabled 状态。
            defaultDrawable = prepareCustomDrawable(mNonClickableBackgroundDrawable);
        } else if (mBackgroundDrawable != null) {
            defaultDrawable = prepareCustomDrawable(mBackgroundDrawable);
        } else if (hasGeneratedShape) {
            Drawable defaultSource = currentBackground;
            if (defaultSource instanceof ExtendStateListDrawable) {
                defaultSource = ((ExtendStateListDrawable) defaultSource).getDefaultDrawable();
            }
            ShapeDrawable generatedDefault = convertShapeDrawable(defaultSource);
            refreshShapeDrawable(generatedDefault, null, null);
            defaultDrawable = generatedDefault;
        } else {
            defaultDrawable = currentBackground != null ?
                    (mClipExistingBackground ? prepareCustomDrawable(currentBackground) : currentBackground) :
                    new ColorDrawable(Color.TRANSPARENT);
        }

        if (!hasSolidColorState && !hasStrokeColorState && !hasDrawableState) {
            return decorateDrawable(defaultDrawable);
        }

        ExtendStateListDrawable stateListDrawable = new ExtendStateListDrawable();

        // 禁用状态先于 checked/selected 注册，多个状态重叠时由 disabled 胜出。
        if (mDisabledBackgroundDrawable != null || mSolidDisabledColor != null || mStrokeDisabledColor != null) {
            stateListDrawable.setDisabledDrawable(buildStateDrawable(
                    mDisabledBackgroundDrawable, mSolidDisabledColor, mStrokeDisabledColor));
        }
        if (mShapeClickable && (mPressedBackgroundDrawable != null ||
                mSolidPressedColor != null || mStrokePressedColor != null)) {
            stateListDrawable.setPressedDrawable(buildStateDrawable(
                    mPressedBackgroundDrawable, mSolidPressedColor, mStrokePressedColor));
        }
        // checked、focused、selected 与 enabled 相互独立，即使控件不可点击，
        // 业务通过 setChecked/setSelected 或键盘获得焦点时仍应正常显示对应状态。
        if (mCheckedBackgroundDrawable != null || mSolidCheckedColor != null ||
                mStrokeCheckedColor != null) {
            stateListDrawable.setCheckDrawable(buildStateDrawable(
                    mCheckedBackgroundDrawable, mSolidCheckedColor, mStrokeCheckedColor));
        }
        if (mFocusedBackgroundDrawable != null || mSolidFocusedColor != null ||
                mStrokeFocusedColor != null) {
            stateListDrawable.setFocusedDrawable(buildStateDrawable(
                    mFocusedBackgroundDrawable, mSolidFocusedColor, mStrokeFocusedColor));
        }
        if (mSelectedBackgroundDrawable != null || mSolidSelectedColor != null ||
                mStrokeSelectedColor != null) {
            stateListDrawable.setSelectDrawable(buildStateDrawable(
                    mSelectedBackgroundDrawable, mSolidSelectedColor, mStrokeSelectedColor));
        }

        stateListDrawable.setDefaultDrawable(defaultDrawable);
        return decorateDrawable(stateListDrawable);
    }

    @NonNull
    private Drawable buildStateDrawable(@Nullable Drawable customDrawable,
                                        @Nullable Integer solidColor,
                                        @Nullable Integer strokeColor) {
        // 自定义状态背景优先；未提供时复制 Builder 参数生成对应 ShapeDrawable。
        if (customDrawable != null) {
            return prepareCustomDrawable(customDrawable);
        }
        ShapeDrawable drawable = new ShapeDrawable();
        refreshShapeDrawable(drawable, solidColor, strokeColor);
        return drawable;
    }

    public void refreshShapeDrawable(ShapeDrawable drawable,
                                     @Nullable Integer solidStateColor,
                                     @Nullable Integer strokeStateColor) {
        // 写入 Builder 的完整参数快照，保证动态重建与 XML 初始化行为一致。
        drawable.setType(mType)
                .setWidth(mWidth)
                .setHeight(mHeight)
                .setRadius(mTopLeftRadius, mTopRightRadius,
                        mBottomLeftRadius, mBottomRightRadius);

        drawable.setSolidGradientType(mSolidGradientType)
                .setSolidGradientOrientation(mSolidGradientOrientation)
                .setSolidGradientRadius(mSolidGradientRadius)
                .setSolidGradientCenterX(mSolidGradientCenterX)
                .setSolidGradientCenterY(mSolidGradientCenterY);
        if (Float.isNaN(mSolidGradientAngle)) {
            drawable.clearSolidGradientAngle();
        } else {
            drawable.setSolidGradientAngle(mSolidGradientAngle);
        }

        drawable.setStrokeGradientOrientation(mStrokeGradientOrientation)
                .setStrokeSize(mStrokeSize)
                .setStrokeDashSize(mStrokeDashSize)
                .setStrokeDashGap(mStrokeDashGap);
        if (Float.isNaN(mStrokeGradientAngle)) {
            drawable.clearStrokeGradientAngle();
        } else {
            drawable.setStrokeGradientAngle(mStrokeGradientAngle);
        }

        // 阴影由外层 ShadowDrawable 绘制并预留边界，内容 Drawable 关闭旧阴影。
        drawable.setShadowSize(0)
                .setShadowOffsetX(0)
                .setShadowOffsetY(0);

        if (mRingInnerRadiusRatio > 0) {
            drawable.setRingInnerRadiusRatio(mRingInnerRadiusRatio);
        } else if (mRingInnerRadiusSize > -1) {
            drawable.setRingInnerRadiusSize(mRingInnerRadiusSize);
        }

        if (mRingThicknessRatio > 0) {
            drawable.setRingThicknessRatio(mRingThicknessRatio);
        } else if (mRingThicknessSize > -1) {
            drawable.setRingThicknessSize(mRingThicknessSize);
        }

        drawable.setLineGravity(mLineGravity);

        // 填充色设置
        if (solidStateColor != null) {
            drawable.setSolidColor(solidStateColor);
        } else if (isSolidGradientColorsEnable()){
            drawable.setSolidColor(mSolidGradientColors);
        } else {
            drawable.setSolidColor(mSolidColor);
        }

        // 边框色设置
        if (strokeStateColor != null) {
            drawable.setStrokeColor(strokeStateColor);
        } else if (isStrokeGradientColorsEnable()) {
            drawable.setStrokeColor(mStrokeGradientColors);
        } else {
            drawable.setStrokeColor(mStrokeColor);
        }
    }

    @NonNull
    public ShapeDrawable convertShapeDrawable(Drawable drawable) {
        if (drawable instanceof ShapeDrawable) {
            return (ShapeDrawable) drawable;
        }
        return new ShapeDrawable();
    }

    public void intoBackground() {
        // 重建完整装饰链并一次性替换背景，构建结果在无背景配置时可能为空。
        Drawable drawable = buildBackgroundDrawable();
        if (isStrokeDashLineEnable()) {
            // 需要关闭硬件加速，否则虚线或者阴影在某些手机上面无法生效
            // https://developer.android.com/guide/topics/graphics/hardware-accel?hl=zh-cn
            mView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        if (drawable != null) {
            mView.setBackground(drawable);
            applyContentPadding(drawable);
        }
        if (mShapeClickableConfigured) {
            // 仅在显式使用 shape_clickable 时修改 clickable，避免改变普通控件默认交互。
            mView.setClickable(mShapeClickable);
        }
    }

    @Nullable
    private Drawable unwrapDecoratedDrawable(@Nullable Drawable drawable) {
        // 重建前剥离本库包装层，避免 Ripple、阴影和裁剪层重复嵌套。
        if (drawable instanceof ShadowDrawable) {
            drawable = ((ShadowDrawable) drawable).getContentDrawable();
        }
        if (drawable instanceof ShapeRippleDrawable) {
            drawable = ((ShapeRippleDrawable) drawable).getContentDrawable();
        }
        if (drawable instanceof ShapeClipDrawable) {
            drawable = ((ShapeClipDrawable) drawable).getContentDrawable();
        }
        return drawable;
    }

    @Nullable
    private Drawable prepareCustomDrawable(@Nullable Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        // mutate 隔离共享 ConstantState；矩形和椭圆使用 Path 做可靠轮廓裁剪。
        if (mType != ShapeType.RECTANGLE && mType != ShapeType.OVAL) {
            return drawable.mutate();
        }
        return new ShapeClipDrawable(
                drawable,
                mType,
                mTopLeftRadius,
                mTopRightRadius,
                mBottomLeftRadius,
                mBottomRightRadius);
    }

    @Nullable
    private Drawable decorateDrawable(@Nullable Drawable drawable) {
        // Ripple 包裹内容后再套阴影，阴影可以使用最终内容轮廓生成蒙版。
        return wrapShadowDrawable(wrapRippleDrawable(drawable));
    }

    @Nullable
    private Drawable wrapRippleDrawable(@Nullable Drawable drawable) {
        if (drawable == null || !mRippleEnable) {
            return drawable;
        }
        return new ShapeRippleDrawable(mRippleColor, drawable, buildRippleMaskDrawable());
    }

    @NonNull
    private Drawable buildRippleMaskDrawable() {
        // Ripple 蒙版只关心透明度和轮廓，使用不透明白色 Shape 即可。
        ShapeDrawable maskDrawable = new ShapeDrawable()
                .setType(mType)
                .setRadius(mTopLeftRadius, mTopRightRadius,
                        mBottomLeftRadius, mBottomRightRadius)
                .setSolidColor(Color.WHITE)
                .setStrokeColor(Color.WHITE)
                .setStrokeSize(Math.max(1, mStrokeSize))
                .setStrokeDashSize(mStrokeDashSize)
                .setStrokeDashGap(mStrokeDashGap)
                .setLineGravity(mLineGravity);

        if (mRingInnerRadiusRatio > 0) {
            maskDrawable.setRingInnerRadiusRatio(mRingInnerRadiusRatio);
        } else if (mRingInnerRadiusSize > -1) {
            maskDrawable.setRingInnerRadiusSize(mRingInnerRadiusSize);
        }
        if (mRingThicknessRatio > 0) {
            maskDrawable.setRingThicknessRatio(mRingThicknessRatio);
        } else if (mRingThicknessSize > -1) {
            maskDrawable.setRingThicknessSize(mRingThicknessSize);
        }
        return maskDrawable;
    }

    @Nullable
    private Drawable wrapShadowDrawable(@Nullable Drawable drawable) {
        // 未开启阴影时直接返回内容，避免增加无意义的绘制层级。
        if (drawable == null || !isShadowEnable()) {
            return drawable;
        }
        return new ShadowDrawable(
                drawable,
                mShadowSize,
                mShadowColor,
                mShadowOffsetX,
                mShadowOffsetY,
                mShadowSpread,
                mShadowSymmetry,
                mShadowHiddenLeft,
                mShadowHiddenTop,
                mShadowHiddenRight,
                mShadowHiddenBottom,
                mShadowBitmapScale);
    }

    private void applyContentPadding(@NonNull Drawable drawable) {
        // 阴影外部 inset 叠加到原 padding，避免业务内容压在阴影区域。
        Rect insets = new Rect();
        if (drawable instanceof ShadowDrawable) {
            ((ShadowDrawable) drawable).getShadowInsets(insets);
        }
        boolean rtl = mView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        int startInset = rtl ? insets.right : insets.left;
        int endInset = rtl ? insets.left : insets.right;
        mView.setPaddingRelative(
                mOriginalPaddingStart + startInset,
                mOriginalPaddingTop + insets.top,
                mOriginalPaddingEnd + endInset,
                mOriginalPaddingBottom + insets.bottom);
    }

    public void clearBackground() {
        mSolidColor = NO_COLOR;
        mSolidGradientColors = null;
        mSolidGradientAngle = Float.NaN;
        mSolidPressedColor = null;
        mSolidCheckedColor = null;
        mSolidDisabledColor = null;
        mSolidFocusedColor = null;
        mSolidSelectedColor = null;

        mStrokeColor = NO_COLOR;
        mStrokeGradientColors = null;
        mStrokeGradientAngle = Float.NaN;
        mStrokePressedColor = null;
        mStrokeCheckedColor = null;
        mStrokeDisabledColor = null;
        mStrokeFocusedColor = null;
        mStrokeSelectedColor = null;

        mView.setBackground(null);
    }

    /**
     * 从上下文中获取当前布局方向
     */
    private static int getLayoutDirection(View view) {
        int layoutDirection;
        Context context = view.getContext();
        Resources resources = null;
        Configuration configuration = null;
        if (context != null) {
            resources = context.getResources();
        }
        if (resources != null) {
            configuration = resources.getConfiguration();
        }
        if (configuration != null) {
            layoutDirection = configuration.getLayoutDirection();
        } else {
            layoutDirection = View.LAYOUT_DIRECTION_LTR;
        }
        return layoutDirection;
    }

    /**
     * 从 XML 的 color-array 资源读取颜色。
     *
     * <p>TypedArray 只能取得数组资源 ID，真正的数组需要再次通过 Resources.obtainTypedArray
     * 打开；这里统一负责回收临时 TypedArray，并在资源类型不合法时安全回退为未配置。</p>
     */
    @Nullable
    private int[] readColorArray(@NonNull TypedArray sourceArray, int attributeIndex) {
        if (attributeIndex < 0 || !sourceArray.hasValue(attributeIndex)) {
            return null;
        }
        int resourceId = sourceArray.getResourceId(attributeIndex, 0);
        if (resourceId == 0) {
            return null;
        }

        TypedArray colorArray = null;
        try {
            colorArray = mView.getResources().obtainTypedArray(resourceId);
            int[] colors = new int[colorArray.length()];
            for (int i = 0; i < colorArray.length(); i++) {
                colors[i] = colorArray.getColor(i, NO_COLOR);
            }
            // LinearGradient 至少需要两个颜色；非法的一项数组回退到旧三色入口。
            return colors.length < 2 ? null : colors;
        } catch (RuntimeException ignored) {
            // 资源被移除、不是数组或数组项不是颜色时，保留旧三色渐变配置。
            return null;
        } finally {
            if (colorArray != null) {
                colorArray.recycle();
            }
        }
    }

    /**
     * 获取默认的渐变色方向
     */
    private int getDefaultGradientOrientation() {
        // Github issue 地址：https://github.com/getActivity/ShapeView/issues/109
        return 10;
    }

    /**
     * 将 ShapeView 框架中渐变色的 xml 属性值转换成 ShapeDrawable 中的枚举值
     */
    private ShapeGradientOrientation transformGradientOrientation(int value) {
        switch (value) {
            case 0:
                return ShapeGradientOrientation.LEFT_TO_RIGHT;
            case 180:
                return ShapeGradientOrientation.RIGHT_TO_LEFT;
            case 1800:
                return ShapeGradientOrientation.END_TO_START;
            case 90:
                return ShapeGradientOrientation.BOTTOM_TO_TOP;
            case 270:
                return ShapeGradientOrientation.TOP_TO_BOTTOM;
            case 315:
                return ShapeGradientOrientation.TOP_LEFT_TO_BOTTOM_RIGHT;
            case 3150:
                return ShapeGradientOrientation.TOP_START_TO_BOTTOM_END;
            case 45:
                return ShapeGradientOrientation.BOTTOM_LEFT_TO_TOP_RIGHT;
            case 450:
                return ShapeGradientOrientation.BOTTOM_START_TO_TOP_END;
            case 225:
                return ShapeGradientOrientation.TOP_RIGHT_TO_BOTTOM_LEFT;
            case 2250:
                return ShapeGradientOrientation.TOP_END_TO_BOTTOM_START;
            case 135:
                return ShapeGradientOrientation.BOTTOM_RIGHT_TO_TOP_LEFT;
            case 1350:
                return ShapeGradientOrientation.BOTTOM_END_TO_TOP_START;
            case 10:
            default:
                return ShapeGradientOrientation.START_TO_END;
        }
    }
}
