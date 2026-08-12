package com.allynav.shape.styleable;

import com.allynav.shape.R;
import com.allynav.shape.config.ICompoundButtonStyleable;
import com.allynav.shape.config.IShapeDrawableStyleable;
import com.allynav.shape.config.ITextColorStyleable;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ShapeView
 *    time   : 2021/08/28
 *    desc   : RadioButton 的 Shape、文字和按钮图标属性索引映射
 *
 * <p>同时实现背景、文字和 CompoundButton 图标协议，使 ShapeRadioButton 支持
 * checked 等状态的一致背景、文字色和按钮图片。该类只负责索引映射。</p>
 */
public final class ShapeRadioButtonStyleable implements IShapeDrawableStyleable,
        ITextColorStyleable, ICompoundButtonStyleable {

    /** 以下方法映射 Shape 背景属性，返回 R.styleable 中的实际索引。 */

    @Override
    public int getShapeTypeStyleable() {
        return R.styleable.ShapeRadioButton_shape_type;
    }

    @Override
    public int getShapeWidthStyleable() {
        return R.styleable.ShapeRadioButton_shape_width;
    }

    @Override
    public int getShapeHeightStyleable() {
        return R.styleable.ShapeRadioButton_shape_height;
    }

    @Override
    public int getRadiusStyleable() {
        return R.styleable.ShapeRadioButton_shape_radius;
    }

    @Override
    public int getRadiusInTopLeftStyleable() {
        return R.styleable.ShapeRadioButton_shape_radiusInTopLeft;
    }

    @Override
    public int getRadiusInTopStartStyleable() {
        return R.styleable.ShapeRadioButton_shape_radiusInTopStart;
    }

    @Override
    public int getRadiusInTopRightStyleable() {
        return R.styleable.ShapeRadioButton_shape_radiusInTopRight;
    }

    @Override
    public int getRadiusInTopEndStyleable() {
        return R.styleable.ShapeRadioButton_shape_radiusInTopEnd;
    }

    @Override
    public int getRadiusInBottomLeftStyleable() {
        return R.styleable.ShapeRadioButton_shape_radiusInBottomLeft;
    }

    @Override
    public int getRadiusInBottomStartStyleable() {
        return R.styleable.ShapeRadioButton_shape_radiusInBottomStart;
    }

    @Override
    public int getRadiusInBottomRightStyleable() {
        return R.styleable.ShapeRadioButton_shape_radiusInBottomRight;
    }

    @Override
    public int getRadiusInBottomEndStyleable() {
        return R.styleable.ShapeRadioButton_shape_radiusInBottomEnd;
    }

    @Override
    public int getSolidColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_solidColor;
    }

    @Override
    public int getSolidPressedColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_solidPressedColor;
    }

    @Override
    public int getSolidCheckedColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_solidCheckedColor;
    }

    @Override
    public int getSolidDisabledColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_solidDisabledColor;
    }

    @Override
    public int getSolidFocusedColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_solidFocusedColor;
    }

    @Override
    public int getSolidSelectedColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_solidSelectedColor;
    }

    @Override
    public int getSolidGradientStartColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_solidGradientStartColor;
    }

    @Override
    public int getSolidGradientCenterColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_solidGradientCenterColor;
    }

    @Override
    public int getSolidGradientEndColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_solidGradientEndColor;
    }

    @Override
    public int getSolidGradientOrientationStyleable() {
        return R.styleable.ShapeRadioButton_shape_solidGradientOrientation;
    }

    @Override
    public int getSolidGradientTypeStyleable() {
        return R.styleable.ShapeRadioButton_shape_solidGradientType;
    }

    @Override
    public int getSolidGradientCenterXStyleable() {
        return R.styleable.ShapeRadioButton_shape_solidGradientCenterX;
    }

    @Override
    public int getSolidGradientCenterYStyleable() {
        return R.styleable.ShapeRadioButton_shape_solidGradientCenterY;
    }

    @Override
    public int getSolidGradientRadiusStyleable() {
        return R.styleable.ShapeRadioButton_shape_solidGradientRadius;
    }

    @Override
    public int getStrokeColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_strokeColor;
    }

    @Override
    public int getStrokePressedColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_strokePressedColor;
    }

    @Override
    public int getStrokeCheckedColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_strokeCheckedColor;
    }

    @Override
    public int getStrokeDisabledColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_strokeDisabledColor;
    }

    @Override
    public int getStrokeFocusedColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_strokeFocusedColor;
    }

    @Override
    public int getStrokeSelectedColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_strokeSelectedColor;
    }

    @Override
    public int getStrokeGradientStartColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_strokeGradientStartColor;
    }

    @Override
    public int getStrokeGradientCenterColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_strokeGradientCenterColor;
    }

    @Override
    public int getStrokeGradientEndColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_strokeGradientEndColor;
    }

    @Override
    public int getStrokeGradientOrientationStyleable() {
        return R.styleable.ShapeRadioButton_shape_strokeGradientOrientation;
    }

    @Override
    public int getStrokeSizeStyleable() {
        return R.styleable.ShapeRadioButton_shape_strokeSize;
    }

    @Override
    public int getStrokeDashSizeStyleable() {
        return R.styleable.ShapeRadioButton_shape_strokeDashSize;
    }

    @Override
    public int getStrokeDashGapStyleable() {
        return R.styleable.ShapeRadioButton_shape_strokeDashGap;
    }

    @Override
    public int getShadowSizeStyleable() {
        return R.styleable.ShapeRadioButton_shape_shadowSize;
    }

    @Override
    public int getShadowColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_shadowColor;
    }

    @Override
    public int getShadowOffsetXStyleable() {
        return R.styleable.ShapeRadioButton_shape_shadowOffsetX;
    }

    @Override
    public int getShadowOffsetYStyleable() {
        return R.styleable.ShapeRadioButton_shape_shadowOffsetY;
    }

    @Override
    public int getRingInnerRadiusSizeStyleable() {
        return R.styleable.ShapeRadioButton_shape_ringInnerRadiusSize;
    }

    @Override
    public int getRingInnerRadiusRatioStyleable() {
        return R.styleable.ShapeRadioButton_shape_ringInnerRadiusRatio;
    }

    @Override
    public int getRingThicknessSizeStyleable() {
        return R.styleable.ShapeRadioButton_shape_ringThicknessSize;
    }

    @Override
    public int getRingThicknessRatioStyleable() {
        return R.styleable.ShapeRadioButton_shape_ringThicknessRatio;
    }

    @Override
    public int getLineGravityStyleable() {
        return R.styleable.ShapeRadioButton_shape_lineGravity;
    }

    /**
     * {@link ITextColorStyleable}
     */

    @Override
    public int getTextColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_textColor;
    }

    @Override
    public int getTextPressedColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_textPressedColor;
    }

    @Override
    public int getTextCheckedColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_textCheckedColor;
    }

    @Override
    public int getTextDisabledColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_textDisabledColor;
    }

    @Override
    public int getTextFocusedColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_textFocusedColor;
    }

    @Override
    public int getTextSelectedColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_textSelectedColor;
    }

    @Override
    public int getTextStartColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_textStartColor;
    }

    @Override
    public int getTextCenterColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_textCenterColor;
    }

    @Override
    public int getTextEndColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_textEndColor;
    }

    @Override
    public int getTextGradientOrientationStyleable() {
        return R.styleable.ShapeRadioButton_shape_textGradientOrientation;
    }
    /**
     * {@link ICompoundButtonStyleable}
     */

    @Override
    public int getButtonDrawableStyleable() {
        return R.styleable.ShapeRadioButton_shape_buttonDrawable;
    }

    @Override
    public int getButtonPressedDrawableStyleable() {
        return R.styleable.ShapeRadioButton_shape_buttonPressedDrawable;
    }

    @Override
    public int getButtonCheckedDrawableStyleable() {
        return R.styleable.ShapeRadioButton_shape_buttonCheckedDrawable;
    }

    @Override
    public int getButtonDisabledDrawableStyleable() {
        return R.styleable.ShapeRadioButton_shape_buttonDisabledDrawable;
    }

    @Override
    public int getButtonFocusedDrawableStyleable() {
        return R.styleable.ShapeRadioButton_shape_buttonFocusedDrawable;
    }

    @Override
    public int getButtonSelectedDrawableStyleable() {
        return R.styleable.ShapeRadioButton_shape_buttonSelectedDrawable;
    }

    @Override
    public int getTextStrokeColorStyleable() {
        return R.styleable.ShapeRadioButton_shape_textStrokeColor;
    }

    @Override
    public int getTextStrokeSizeStyleable() {
        return R.styleable.ShapeRadioButton_shape_textStrokeSize;
    }
}
