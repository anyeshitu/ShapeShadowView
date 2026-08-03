package com.allynav.shape.drawable;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Draws a cached bitmap shadow behind any ShapeView background drawable.
 *
 * <p>The inset calculation and low-resolution bitmap cache are inspired by ShadowLayout. The
 * actual mask is produced from the wrapped drawable, so selectors, gradients, ovals and custom
 * corner radii all share the same shadow implementation.</p>
 */
public final class ShadowDrawable extends Drawable implements Drawable.Callback {

    private static final float MIN_BITMAP_SCALE = 0.25f;
    private static final float MAX_BITMAP_SCALE = 1f;

    private final Drawable mContentDrawable;
    private final Paint mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Rect mContentBounds = new Rect();
    private final Rect mShadowInsets = new Rect();

    private final float mShadowRadius;
    private final float mShadowOffsetX;
    private final float mShadowOffsetY;
    private final float mShadowSpread;
    private final int mShadowColor;
    private final boolean mShadowSymmetry;
    private final boolean mShadowHiddenLeft;
    private final boolean mShadowHiddenTop;
    private final boolean mShadowHiddenRight;
    private final boolean mShadowHiddenBottom;
    private final float mBitmapScale;

    private Bitmap mShadowBitmap;
    private boolean mShadowDirty = true;
    private boolean mRenderingShadow;
    private int mAlpha = 255;

    public ShadowDrawable(
            @NonNull Drawable contentDrawable,
            float shadowRadius,
            int shadowColor,
            float shadowOffsetX,
            float shadowOffsetY,
            float shadowSpread,
            boolean shadowSymmetry,
            boolean shadowHiddenLeft,
            boolean shadowHiddenTop,
            boolean shadowHiddenRight,
            boolean shadowHiddenBottom,
            float bitmapScale) {
        mContentDrawable = contentDrawable.mutate();
        mContentDrawable.setCallback(this);
        mShadowRadius = Math.max(0f, shadowRadius);
        mShadowColor = shadowColor;
        mShadowOffsetX = shadowOffsetX;
        mShadowOffsetY = shadowOffsetY;
        mShadowSpread = Math.max(0f, shadowSpread);
        mShadowSymmetry = shadowSymmetry;
        mShadowHiddenLeft = shadowHiddenLeft;
        mShadowHiddenTop = shadowHiddenTop;
        mShadowHiddenRight = shadowHiddenRight;
        mShadowHiddenBottom = shadowHiddenBottom;
        mBitmapScale = Math.max(MIN_BITMAP_SCALE, Math.min(MAX_BITMAP_SCALE, bitmapScale));
        updateShadowInsets();
    }

    @NonNull
    public Drawable getContentDrawable() {
        return mContentDrawable;
    }

    public void getShadowInsets(@NonNull Rect outInsets) {
        outInsets.set(mShadowInsets);
    }

    private void updateShadowInsets() {
        float extent = mShadowRadius + mShadowSpread;
        if (mShadowSymmetry) {
            int horizontal = (int) Math.ceil(extent + Math.abs(mShadowOffsetX));
            int vertical = (int) Math.ceil(extent + Math.abs(mShadowOffsetY));
            mShadowInsets.set(
                    mShadowHiddenLeft ? 0 : horizontal,
                    mShadowHiddenTop ? 0 : vertical,
                    mShadowHiddenRight ? 0 : horizontal,
                    mShadowHiddenBottom ? 0 : vertical);
            return;
        }

        mShadowInsets.set(
                mShadowHiddenLeft ? 0 : (int) Math.ceil(extent + Math.max(0f, -mShadowOffsetX)),
                mShadowHiddenTop ? 0 : (int) Math.ceil(extent + Math.max(0f, -mShadowOffsetY)),
                mShadowHiddenRight ? 0 : (int) Math.ceil(extent + Math.max(0f, mShadowOffsetX)),
                mShadowHiddenBottom ? 0 : (int) Math.ceil(extent + Math.max(0f, mShadowOffsetY)));
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        int left = bounds.left + mShadowInsets.left;
        int top = bounds.top + mShadowInsets.top;
        int right = bounds.right - mShadowInsets.right;
        int bottom = bounds.bottom - mShadowInsets.bottom;

        if (right < left) {
            int center = bounds.centerX();
            left = center;
            right = center;
        }
        if (bottom < top) {
            int center = bounds.centerY();
            top = center;
            bottom = center;
        }

        mContentBounds.set(left, top, right, bottom);
        mContentDrawable.setBounds(mContentBounds);
        markShadowDirty();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        ensureShadowBitmap();
        Rect bounds = getBounds();
        if (mShadowBitmap != null && !bounds.isEmpty()) {
            int saveCount = canvas.save();
            canvas.clipRect(
                    mShadowHiddenLeft ? mContentBounds.left : bounds.left,
                    mShadowHiddenTop ? mContentBounds.top : bounds.top,
                    mShadowHiddenRight ? mContentBounds.right : bounds.right,
                    mShadowHiddenBottom ? mContentBounds.bottom : bounds.bottom);
            mBitmapPaint.setAlpha(mAlpha);
            canvas.drawBitmap(mShadowBitmap, null, bounds, mBitmapPaint);
            canvas.restoreToCount(saveCount);
        }
        mContentDrawable.draw(canvas);
    }

    private void ensureShadowBitmap() {
        Rect bounds = getBounds();
        if (!mShadowDirty || bounds.isEmpty() || Color.alpha(mShadowColor) == 0) {
            return;
        }

        recycleShadowBitmap();
        int bitmapWidth = Math.max(1, Math.round(bounds.width() * mBitmapScale));
        int bitmapHeight = Math.max(1, Math.round(bounds.height() * mBitmapScale));

        Bitmap maskBitmap = null;
        Bitmap blurredMask = null;
        try {
            maskBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas maskCanvas = new Canvas(maskBitmap);
            maskCanvas.scale(mBitmapScale, mBitmapScale);
            maskCanvas.translate(-bounds.left, -bounds.top);

            Drawable shadowSource = mContentDrawable instanceof ShapeRippleDrawable ?
                    ((ShapeRippleDrawable) mContentDrawable).getShadowMaskDrawable() :
                    mContentDrawable;
            Rect originalBounds = new Rect(shadowSource.getBounds());
            Rect casterBounds = new Rect(mContentBounds);
            int spread = (int) Math.ceil(mShadowSpread);
            casterBounds.inset(-spread, -spread);

            mRenderingShadow = true;
            int originalAlpha = shadowSource.getAlpha();
            try {
                shadowSource.setAlpha(255);
                shadowSource.setBounds(casterBounds);
                shadowSource.draw(maskCanvas);
            } finally {
                shadowSource.setBounds(originalBounds);
                shadowSource.setAlpha(originalAlpha);
                mRenderingShadow = false;
            }

            Paint blurPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            blurPaint.setMaskFilter(new BlurMaskFilter(
                    Math.max(0.5f, mShadowRadius * mBitmapScale),
                    BlurMaskFilter.Blur.NORMAL));
            int[] blurOffset = new int[2];
            blurredMask = maskBitmap.extractAlpha(blurPaint, blurOffset);

            mShadowBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas shadowCanvas = new Canvas(mShadowBitmap);
            Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            shadowPaint.setColor(mShadowColor);
            shadowCanvas.drawBitmap(
                    blurredMask,
                    blurOffset[0] + mShadowOffsetX * mBitmapScale,
                    blurOffset[1] + mShadowOffsetY * mBitmapScale,
                    shadowPaint);
            mShadowDirty = false;
        } finally {
            mRenderingShadow = false;
            if (maskBitmap != null && !maskBitmap.isRecycled()) {
                maskBitmap.recycle();
            }
            if (blurredMask != null && !blurredMask.isRecycled()) {
                blurredMask.recycle();
            }
        }
    }

    private void markShadowDirty() {
        mShadowDirty = true;
        recycleShadowBitmap();
    }

    private void recycleShadowBitmap() {
        if (mShadowBitmap != null && !mShadowBitmap.isRecycled()) {
            mShadowBitmap.recycle();
        }
        mShadowBitmap = null;
    }

    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
        mContentDrawable.setAlpha(alpha);
        markShadowDirty();
        invalidateSelf();
    }

    @Override
    public int getAlpha() {
        return mAlpha;
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mContentDrawable.setColorFilter(colorFilter);
        markShadowDirty();
        invalidateSelf();
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
        boolean changed = mContentDrawable.setState(state);
        if (changed && !isRippleContent()) {
            markShadowDirty();
        }
        return changed;
    }

    @Override
    protected boolean onLevelChange(int level) {
        boolean changed = mContentDrawable.setLevel(level);
        if (changed && !isRippleContent()) {
            markShadowDirty();
        }
        return changed;
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        boolean contentChanged = mContentDrawable.setVisible(visible, restart);
        boolean wrapperChanged = super.setVisible(visible, restart);
        return contentChanged || wrapperChanged;
    }

    @Override
    public int getIntrinsicWidth() {
        int width = mContentDrawable.getIntrinsicWidth();
        return width < 0 ? width : width + mShadowInsets.left + mShadowInsets.right;
    }

    @Override
    public int getIntrinsicHeight() {
        int height = mContentDrawable.getIntrinsicHeight();
        return height < 0 ? height : height + mShadowInsets.top + mShadowInsets.bottom;
    }

    @Override
    public int getMinimumWidth() {
        return mContentDrawable.getMinimumWidth() + mShadowInsets.left + mShadowInsets.right;
    }

    @Override
    public int getMinimumHeight() {
        return mContentDrawable.getMinimumHeight() + mShadowInsets.top + mShadowInsets.bottom;
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
        if (!mRenderingShadow) {
            if (!isRippleContent()) {
                markShadowDirty();
            }
            invalidateSelf();
        }
    }

    private boolean isRippleContent() {
        return mContentDrawable instanceof ShapeRippleDrawable;
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
