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

/** Clips an arbitrary color, image, vector or XML drawable to ShapeView geometry. */
public final class ShapeClipDrawable extends Drawable implements Drawable.Callback {

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
