package com.allynav.shape.drawable;

import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;

/**
 * ShapeDrawable 的可复制 ConstantState 参数快照。
 *
 * <p>这里保存颜色、渐变、圆角、描边、环形、尺寸和旧版阴影等资源状态；Paint、Path
 * 和位图缓存属于具体 Drawable 实例，不放入 ConstantState。复制构造函数会复制数组，
 * 避免 mutate 后不同 Drawable 共享可变配置。</p>
 */
public class ShapeState extends Drawable.ConstantState {

    /** Android 资源配置变化标记。 */
    public int changingConfigurations;
    /** Shape 几何和渐变类型。 */
    @ShapeTypeLimit
    public int shapeType = ShapeType.RECTANGLE;
    @ShapeGradientTypeLimit
    public int solidGradientType = ShapeGradientType.LINEAR_GRADIENT;
    public ShapeGradientOrientation solidGradientOrientation = ShapeGradientOrientation.TOP_TO_BOTTOM;
    /**
     * 任意角度线性渐变，单位为度；NaN 表示继续使用 solidGradientOrientation。
     * 采用哨兵值而不是默认 0 度，是为了保证旧版 XML 的方向枚举行为不发生变化。
     */
    public float solidGradientAngle = Float.NaN;
    /** 填充/描边渐变颜色数组及对应位置。 */
    public int[] solidColors;
    public int[] strokeColors;
    public int[] tempSolidColors; // no need to copy
    public float[] tempSolidPositions; // no need to copy
    public float[] positions;
    public boolean hasSolidColor;
    public boolean hasStrokeColor;
    public int solidColor;
    public int strokeSize = -1;   // if >= 0 use stroking.
    public ShapeGradientOrientation strokeGradientOrientation = ShapeGradientOrientation.TOP_TO_BOTTOM;
    /** 任意角度描边渐变；NaN 时继续使用 strokeGradientOrientation。 */
    public float strokeGradientAngle = Float.NaN;
    public int strokeColor;
    public float strokeDashSize;
    public float strokeDashGap;
    /** 统一圆角值；radiusArray 不为空时使用四角独立值。 */
    public float radius;
    public float[] radiusArray;
    public Rect padding;
    public int width = -1;
    public int height = -1;
    public float ringInnerRadiusRatio;
    public float ringThicknessRatio;
    public int ringInnerRadiusSize = -1;
    public int ringThicknessSize = -1;
    public float solidCenterX = 0.5f;
    public float solidCenterY = 0.5f;
    public float gradientRadius = 0.5f;
    public boolean useLevel;
    public boolean useLevelForShape;
    public boolean opaque;

    /** 兼容 ShapeDrawable 直接阴影 API 的参数；ShapeView 默认由 ShadowDrawable 承载。 */
    public int shadowSize;
    public int shadowColor;
    public int shadowOffsetX;
    public int shadowOffsetY;

    public int lineGravity = Gravity.CENTER;

    public ShapeState() {}

    public ShapeState(ShapeState state) {
        changingConfigurations = state.changingConfigurations;
        shapeType = state.shapeType;
        solidGradientType = state.solidGradientType;
        solidGradientOrientation = state.solidGradientOrientation;
        solidGradientAngle = state.solidGradientAngle;
        if (state.solidColors != null) {
            solidColors = state.solidColors.clone();
        }
        if (state.strokeColors != null) {
            strokeColors = state.strokeColors.clone();
        }
        if (state.positions != null) {
            positions = state.positions.clone();
        }
        hasSolidColor = state.hasSolidColor;
        hasStrokeColor = state.hasStrokeColor;
        solidColor = state.solidColor;
        strokeSize = state.strokeSize;
        strokeGradientOrientation = state.strokeGradientOrientation;
        strokeGradientAngle = state.strokeGradientAngle;
        strokeColor = state.strokeColor;
        strokeDashSize = state.strokeDashSize;
        strokeDashGap = state.strokeDashGap;
        radius = state.radius;
        if (state.radiusArray != null) {
            radiusArray = state.radiusArray.clone();
        }
        if (state.padding != null) {
            padding = new Rect(state.padding);
        }
        width = state.width;
        height = state.height;
        ringInnerRadiusRatio = state.ringInnerRadiusRatio;
        ringThicknessRatio = state.ringThicknessRatio;
        ringInnerRadiusSize = state.ringInnerRadiusSize;
        ringThicknessSize = state.ringThicknessSize;
        solidCenterX = state.solidCenterX;
        solidCenterY = state.solidCenterY;
        gradientRadius = state.gradientRadius;
        useLevel = state.useLevel;
        useLevelForShape = state.useLevelForShape;
        opaque = state.opaque;

        shadowSize = state.shadowSize;
        shadowColor = state.shadowColor;
        shadowOffsetX = state.shadowOffsetX;
        shadowOffsetY = state.shadowOffsetY;

        lineGravity = state.lineGravity;
    }

    @Override
    public Drawable newDrawable() {
        return new ShapeDrawable(this);
    }

    @Override
    public Drawable newDrawable(Resources res) {
        return new ShapeDrawable(this);
    }

    @Override
    public int getChangingConfigurations() {
        return changingConfigurations;
    }

    public void setType(int shape) {
        shapeType = shape;
        computeOpacity();
    }

    public void setSolidGradientType(int gradientType) {
        this.solidGradientType = gradientType;
    }

    public void setSolidColor(int... colors) {
        if (colors == null) {
            solidColor = 0;
            hasSolidColor = true;
            computeOpacity();
            return;
        }

        if (colors.length == 1) {
            hasSolidColor = true;
            solidColor = colors[0];
            solidColors = null;
        } else {
            hasSolidColor = false;
            solidColor = 0;
            solidColors = colors;
        }
        computeOpacity();
    }

    public void setSolidColor(int argb) {
        hasSolidColor = true;
        solidColor = argb;
        solidColors = null;
        computeOpacity();
    }

    private void computeOpacity() {
        if (shapeType != ShapeType.RECTANGLE) {
            opaque = false;
            return;
        }

        if (radius > 0 || radiusArray != null) {
            opaque = false;
            return;
        }

        if (shadowSize > 0) {
            opaque = false;
            return;
        }

        if (strokeSize > 0 && !isOpaque(strokeColor)) {
            opaque = false;
            return;
        }

        if (hasSolidColor) {
            opaque = isOpaque(solidColor);
            return;
        }

        if (solidColors != null) {
            for (int color : solidColors) {
                if (!isOpaque(color)) {
                    opaque = false;
                    return;
                }
            }
        }

        if (hasStrokeColor) {
            opaque = isOpaque(strokeColor);
            return;
        }

        if (strokeColors != null) {
            for (int color : strokeColors) {
                if (!isOpaque(color)) {
                    opaque = false;
                    return;
                }
            }
        }

        opaque = true;
    }

    private static boolean isOpaque(int color) {
        return ((color >> 24) & 0xff) == 0xff;
    }

    public void setStrokeSize(int size) {
        strokeSize = size;
        computeOpacity();
    }

    public void setStrokeColor(int... colors) {
        if (colors == null) {
            strokeColor = 0;
            hasStrokeColor = true;
            computeOpacity();
            return;
        }

        if (colors.length == 1) {
            hasStrokeColor = true;
            strokeColor = colors[0];
            strokeColors = null;
        } else {
            hasStrokeColor = false;
            strokeColor = 0;
            strokeColors = colors;
        }
        computeOpacity();
    }

    public void setCornerRadius(float radius) {
        if (radius < 0) {
            radius = 0;
        }
        this.radius = radius;
        radiusArray = null;
    }

    public void setCornerRadii(float[] radii) {
        radiusArray = radii;
        if (radii == null) {
            radius = 0;
        }
    }
}
