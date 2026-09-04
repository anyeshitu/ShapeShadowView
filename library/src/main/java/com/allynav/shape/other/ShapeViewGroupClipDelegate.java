package com.allynav.shape.other;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.allynav.shape.builder.ShapeDrawableBuilder;
import com.allynav.shape.drawable.ShapeType;

/**
 * 为 ShapeView 风格的 ViewGroup 裁剪子 View 绘制区域。
 *
 * <p>ShapeClipDrawable 只能裁剪 Drawable 本身，无法限制 ImageView、视频控件或自定义
 * Canvas View 的绘制范围。本委托在容器 dispatchDraw 前对 Canvas 应用与 Shape 背景一致的
 * 圆角/椭圆 Path，从而补齐 ShadowLayout 对任意子 View 的裁剪能力。</p>
 *
 * <p>委托不缓存固定尺寸和圆角结果。Builder 的圆角、阴影和隐藏边可以通过 Java 动态修改，
 * 每次 dispatchDraw 都会读取最新值，避免动态重建背景后子 View 仍使用旧裁剪区域。</p>
 */
public final class ShapeViewGroupClipDelegate {

    private final ViewGroup mViewGroup;
    private final ShapeDrawableBuilder mShapeDrawableBuilder;
    private final Path mClipPath = new Path();
    private final Rect mShadowInsets = new Rect();
    private final RectF mClipBounds = new RectF();

    public ShapeViewGroupClipDelegate(@NonNull ViewGroup viewGroup,
                                      @NonNull ShapeDrawableBuilder shapeDrawableBuilder) {
        mViewGroup = viewGroup;
        mShapeDrawableBuilder = shapeDrawableBuilder;
    }

    /**
     * 在父类 dispatchDraw 前保存并裁剪 Canvas。
     *
     * @return 需要传给 restore 的保存编号；无需裁剪时返回 -1。
     */
    public int save(@NonNull Canvas canvas) {
        if (!buildClipPath()) {
            return -1;
        }
        int saveCount = canvas.save();
        canvas.clipPath(mClipPath);
        return saveCount;
    }

    /** 恢复 save() 创建的 Canvas 状态，保证裁剪不会泄漏到父级绘制。 */
    public void restore(@NonNull Canvas canvas, int saveCount) {
        if (saveCount != -1) {
            canvas.restoreToCount(saveCount);
        }
    }

    /**
     * 根据当前容器尺寸和 Builder 参数重建裁剪路径。
     * 阴影位于 View 边界内，因此子 View 只应绘制在扣除阴影占位后的内容区域内。
     */
    private boolean buildClipPath() {
        int width = mViewGroup.getWidth();
        int height = mViewGroup.getHeight();
        if (width <= 0 || height <= 0 || mViewGroup.getChildCount() == 0) {
            return false;
        }

        int shapeType = mShapeDrawableBuilder.getType();
        if (shapeType != ShapeType.RECTANGLE && shapeType != ShapeType.OVAL) {
            // line/ring 是独立绘制形状，不把它们误当成可承载子 View 的圆角容器。
            return false;
        }

        mShapeDrawableBuilder.getShadowInsets(mShadowInsets);
        float left = mShadowInsets.left;
        float top = mShadowInsets.top;
        float right = width - mShadowInsets.right;
        float bottom = height - mShadowInsets.bottom;
        if (right <= left || bottom <= top) {
            return false;
        }

        mClipBounds.set(left, top, right, bottom);
        mClipPath.reset();
        if (shapeType == ShapeType.OVAL) {
            mClipPath.addOval(mClipBounds, Path.Direction.CW);
        } else if (mShapeDrawableBuilder.getTopLeftRadius() > 0f ||
                mShapeDrawableBuilder.getTopRightRadius() > 0f ||
                mShapeDrawableBuilder.getBottomLeftRadius() > 0f ||
                mShapeDrawableBuilder.getBottomRightRadius() > 0f) {
            float topLeft = mShapeDrawableBuilder.getTopLeftRadius();
            float topRight = mShapeDrawableBuilder.getTopRightRadius();
            float bottomRight = mShapeDrawableBuilder.getBottomRightRadius();
            float bottomLeft = mShapeDrawableBuilder.getBottomLeftRadius();
            if (topLeft == topRight && topLeft == bottomRight && topLeft == bottomLeft) {
                // ShapeDrawable 对统一圆角会先按短边的一半限制半径，这里保持同样的规则。
                float radius = Math.min(topLeft,
                        Math.min(mClipBounds.width(), mClipBounds.height()) / 2f);
                mClipPath.addRoundRect(mClipBounds, radius, radius, Path.Direction.CW);
            } else {
                // 四角独立圆角必须使用与 ShapeDrawable 相同的原始 radii 数组，
                // 交给 Android Path/Skia 按边长比例统一归一化。
                mClipPath.addRoundRect(mClipBounds, new float[] {
                        topLeft, topLeft,
                        topRight, topRight,
                        bottomRight, bottomRight,
                        bottomLeft, bottomLeft
                }, Path.Direction.CW);
            }
        } else {
            // 没有圆角时无需修改 Canvas，避免无意义的 clipPath 开销。
            return false;
        }
        return true;
    }

}
