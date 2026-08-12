package com.allynav.shape.config;

/**
 * Shape 背景属性索引协议。
 *
 * <p>每种控件的 declare-styleable 索引不同，具体 Styleable 适配类把它们映射到本接口。
 * {@code ShapeDrawableBuilder} 只依赖统一协议，即可复用形状、圆角、渐变、描边、虚线、
 * 阴影和环形等解析逻辑。</p>
 *
 * <p>扩展属性的 default 方法返回 0，表示当前控件没有声明该能力；构建器读取前会先
 * 检查索引是否有效。</p>
 */
public interface IShapeDrawableStyleable {

    int getShapeTypeStyleable();

    int getShapeWidthStyleable();

    int getShapeHeightStyleable();

    int getRadiusStyleable();

    int getRadiusInTopLeftStyleable();

    int getRadiusInTopStartStyleable();

    int getRadiusInTopRightStyleable();

    int getRadiusInTopEndStyleable();

    int getRadiusInBottomLeftStyleable();

    int getRadiusInBottomStartStyleable();

    int getRadiusInBottomRightStyleable();

    int getRadiusInBottomEndStyleable();

    int getSolidColorStyleable();

    int getSolidPressedColorStyleable();

    default int getSolidCheckedColorStyleable() {
        return 0;
    }

    int getSolidDisabledColorStyleable();

    int getSolidFocusedColorStyleable();

    int getSolidSelectedColorStyleable();

    int getSolidGradientStartColorStyleable();

    int getSolidGradientCenterColorStyleable();

    int getSolidGradientEndColorStyleable();

    int getSolidGradientOrientationStyleable();

    int getSolidGradientTypeStyleable();

    int getSolidGradientCenterXStyleable();

    int getSolidGradientCenterYStyleable();

    int getSolidGradientRadiusStyleable();

    int getStrokeColorStyleable();

    int getStrokePressedColorStyleable();

    default int getStrokeCheckedColorStyleable() {
        return 0;
    }

    int getStrokeDisabledColorStyleable();

    int getStrokeFocusedColorStyleable();

    int getStrokeSelectedColorStyleable();

    int getStrokeGradientStartColorStyleable();

    int getStrokeGradientCenterColorStyleable();

    int getStrokeGradientEndColorStyleable();

    int getStrokeGradientOrientationStyleable();

    int getStrokeSizeStyleable();

    int getStrokeDashSizeStyleable();

    int getStrokeDashGapStyleable();

    int getShadowSizeStyleable();

    int getShadowColorStyleable();

    int getShadowOffsetXStyleable();

    int getShadowOffsetYStyleable();

    int getRingInnerRadiusSizeStyleable();

    int getRingInnerRadiusRatioStyleable();

    int getRingThicknessSizeStyleable();

    int getRingThicknessRatioStyleable();

    int getLineGravityStyleable();
}
