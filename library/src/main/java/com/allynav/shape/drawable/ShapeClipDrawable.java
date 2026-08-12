package com.allynav.shape.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 将任意颜色、图片、Vector 或 XML Drawable 裁剪为 ShapeView 轮廓。
 *
 * <p>矩形使用四角独立圆角 Path，椭圆使用 Oval Path。包装器完整转发 state、level、
 * visible、hotspot 和 Drawable.Callback，确保被裁剪内容仍保留选择器与 Ripple 行为。</p>
 */
public final class ShapeClipDrawable extends Drawable implements Drawable.Callback {

    /** 被裁剪内容、目标形状和复用的裁剪路径。 */
    private final Drawable mContentDrawable;
    private final int mShapeType;
    private final float[] mRadii;
    private final Path mClipPath = new Path();
    private final RectF mRect = new RectF();

    public ShapeClipDrawable(@NonNull Drawable contentDrawable, int shapeType,
                             float topLeftRadius, float topRightRadius,
                             float bottomLeftRadius, float bottomRightRadius) {
        mContentDrawable = contentDrawable.mutate();
        mContentDrawable.setCallback(this);
        mShapeType = shapeType;
        mRadii = new float[] {
                topLeftRadius, topLeftRadius,
                topRightRadius, topRightRadius,
                bottomRightRadius, bottomRightRadius,
                bottomLeftRadius, bottomLeftRadius
        };
    }

    @NonNull
    public Drawable getContentDrawable() {
        return mContentDrawable;
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        // 边界变化时同步内容 bounds，并只重建一次裁剪 Path。
        mContentDrawable.setBounds(bounds);
        mRect.set(bounds);
        mClipPath.reset();
        if (mShapeType == ShapeType.OVAL) {
            mClipPath.addOval(mRect, Path.Direction.CW);
        } else {
            mClipPath.addRoundRect(mRect, mRadii, Path.Direction.CW);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        // save/restore 将 clipPath 的影响限制在当前 Drawable 绘制范围内。
        int saveCount = canvas.save();
        canvas.clipPath(mClipPath);
        mContentDrawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public void setAlpha(int alpha) {
        mContentDrawable.setAlpha(alpha);
    }

    @Override
    public int getAlpha() {
        return mContentDrawable.getAlpha();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mContentDrawable.setColorFilter(colorFilter);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public boolean isStateful() {
        return mContentDrawable.isStateful();
    }

    @Override
    protected boolean onStateChange(int[] state) {
        return mContentDrawable.setState(state);
    }

    @Override
    protected boolean onLevelChange(int level) {
        return mContentDrawable.setLevel(level);
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        boolean contentChanged = mContentDrawable.setVisible(visible, restart);
        boolean wrapperChanged = super.setVisible(visible, restart);
        return contentChanged || wrapperChanged;
    }

    @Override
    public int getIntrinsicWidth() {
        return mContentDrawable.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mContentDrawable.getIntrinsicHeight();
    }

    @Override
    public int getMinimumWidth() {
        return mContentDrawable.getMinimumWidth();
    }

    @Override
    public int getMinimumHeight() {
        return mContentDrawable.getMinimumHeight();
    }

    @Override
    public void jumpToCurrentState() {
        mContentDrawable.jumpToCurrentState();
    }

    @Override
    public void setHotspot(float x, float y) {
        mContentDrawable.setHotspot(x, y);
    }

    @Override
    public void setHotspotBounds(int left, int top, int right, int bottom) {
        mContentDrawable.setHotspotBounds(left, top, right, bottom);
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
        // 子 Drawable 失效时通知外层 View 重绘。
        invalidateSelf();
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        scheduleSelf(what, when);
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        unscheduleSelf(what);
    }
}
