package com.allynav.shape.builder;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.allynav.shape.R;

/**
 * ImageView 图片内容驱动的彩色模糊投影构建器。
 *
 * <p>普通 {@code shape_shadow*} 是单色 Shape 轮廓阴影，本类则从 ImageView 当前显示的
 * Drawable 生成低分辨率彩色缓存，再将缓存扩大、偏移并绘制到原图下面，适合封面、头像
 * 和设备图片等需要“图片自身颜色投影”的场景。算法不依赖 RenderScript，API 21 以上
 * 均可使用；低分辨率缓存和简单盒式模糊用于控制内存及绘制成本。</p>
 *
 * <p>默认关闭。开启后缓存只在图片、DrawableState、尺寸或投影参数变化时重建，不会在
 * 每一帧创建 Bitmap。该类只负责投影，ShapeImageView 原有的 src、tint、圆角和单色
 * 阴影仍由各自 Builder 独立处理。</p>
 */
public final class ImageBlurShadowBuilder {

    /** 低分辨率投影的最大边长，避免原图很大时创建高内存缓存。 */
    private static final int MAX_BITMAP_DIMENSION = 96;
    /** 允许的缓存缩放范围；过小会产生明显色块，过大则失去低成本意义。 */
    private static final float MIN_BITMAP_SCALE = 0.05f;
    private static final float MAX_BITMAP_SCALE = 1f;

    private final ImageView mImageView;
    /** 每帧绘制复用 Paint，避免 onDraw 中产生短生命周期对象。 */
    private final Paint mBitmapPaint = new Paint(
            Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private boolean mEnable;
    private float mRadius;
    private float mOffsetX;
    private float mOffsetY;
    private float mAlpha = 0.45f;
    private float mBitmapScale = 0.18f;
    /** 开启投影后清晰原图的缩放比例，给投影留出 View 内可见空间。 */
    private float mImageScale = 0.86f;

    /** 缓存的模糊图片及其对应的 Drawable bounds/尺寸版本。 */
    private Bitmap mBlurBitmap;
    private final Rect mSourceBounds = new Rect();
    private final RectF mDrawRect = new RectF();
    private int mCachedViewWidth = -1;
    private int mCachedViewHeight = -1;
    private int mContentGeneration;
    private int mCachedContentGeneration = -1;
    /** 缓存生成时的图片矩阵，用于识别 ScaleType/矩阵变化。 */
    private final float[] mCachedImageMatrix = new float[9];
    /** 比较矩阵时复用，避免 onDraw 的缓存命中路径也创建临时数组。 */
    private final float[] mCurrentImageMatrix = new float[9];
    private boolean mCacheDirty = true;

    public ImageBlurShadowBuilder(@NonNull ImageView imageView, @Nullable AttributeSet attrs) {
        mImageView = imageView;
        mRadius = dpToPx(imageView.getContext(), 18f);

        if (attrs == null) {
            return;
        }
        TypedArray array = imageView.getContext().obtainStyledAttributes(
                attrs, R.styleable.ShapeImageBlurShadow);
        mEnable = array.getBoolean(
                R.styleable.ShapeImageBlurShadow_shape_imageBlurShadowEnable, false);
        mRadius = array.getDimension(
                R.styleable.ShapeImageBlurShadow_shape_imageBlurShadowRadius, mRadius);
        mOffsetX = array.getDimension(
                R.styleable.ShapeImageBlurShadow_shape_imageBlurShadowOffsetX, 0f);
        mOffsetY = array.getDimension(
                R.styleable.ShapeImageBlurShadow_shape_imageBlurShadowOffsetY, 0f);
        mAlpha = clamp(array.getFloat(
                R.styleable.ShapeImageBlurShadow_shape_imageBlurShadowAlpha, mAlpha), 0f, 1f);
        mBitmapScale = clamp(array.getFloat(
                R.styleable.ShapeImageBlurShadow_shape_imageBlurShadowBitmapScale, mBitmapScale),
                MIN_BITMAP_SCALE, MAX_BITMAP_SCALE);
        mImageScale = clamp(array.getFloat(
                R.styleable.ShapeImageBlurShadow_shape_imageBlurShadowImageScale, mImageScale),
                0.1f, 1f);
        array.recycle();
    }

    /** 返回是否启用图片彩色模糊投影。 */
    public boolean isEnabled() {
        return mEnable;
    }

    /** 设置是否启用图片彩色模糊投影；默认关闭。 */
    public ImageBlurShadowBuilder setEnabled(boolean enabled) {
        if (mEnable != enabled) {
            mEnable = enabled;
            if (enabled) {
                invalidate();
            } else {
                // 关闭后缓存不再使用，立即释放可降低图片列表离屏前的内存峰值。
                recycleBlurBitmap();
                mImageView.invalidate();
            }
        }
        return this;
    }

    /** 设置投影模糊半径，单位为 px；半径越大，投影扩散范围越大。 */
    public ImageBlurShadowBuilder setRadius(float radius) {
        float safeRadius = Math.max(0f, radius);
        if (mRadius != safeRadius) {
            mRadius = safeRadius;
            invalidate();
        }
        return this;
    }

    public float getRadius() {
        return mRadius;
    }

    /** 设置投影水平偏移，正值向右。 */
    public ImageBlurShadowBuilder setOffsetX(float offsetX) {
        mOffsetX = offsetX;
        mImageView.invalidate();
        return this;
    }

    public float getOffsetX() {
        return mOffsetX;
    }

    /** 设置投影垂直偏移，正值向下。 */
    public ImageBlurShadowBuilder setOffsetY(float offsetY) {
        mOffsetY = offsetY;
        mImageView.invalidate();
        return this;
    }

    public float getOffsetY() {
        return mOffsetY;
    }

    /** 设置投影透明度，取值范围 0..1。 */
    public ImageBlurShadowBuilder setAlpha(float alpha) {
        mAlpha = clamp(alpha, 0f, 1f);
        mImageView.invalidate();
        return this;
    }

    public float getAlpha() {
        return mAlpha;
    }

    /**
     * 设置低分辨率缓存比例。
     *
     * <p>比例越低，缓存越省内存且投影越柔和；比例越高，颜色细节越多但计算量也越大。</p>
     */
    public ImageBlurShadowBuilder setBitmapScale(float bitmapScale) {
        float safeScale = clamp(bitmapScale, MIN_BITMAP_SCALE, MAX_BITMAP_SCALE);
        if (mBitmapScale != safeScale) {
            mBitmapScale = safeScale;
            invalidate();
        }
        return this;
    }

    public float getBitmapScale() {
        return mBitmapScale;
    }

    /** 设置开启投影时清晰原图的居中缩放比例，取值范围 0.1..1。 */
    public ImageBlurShadowBuilder setImageScale(float imageScale) {
        mImageScale = clamp(imageScale, 0.1f, 1f);
        mImageView.invalidate();
        return this;
    }

    public float getImageScale() {
        return mImageScale;
    }

    /** 图片或 tint 状态变化后调用，使投影重新取当前 Drawable 的像素。 */
    public void onContentChanged() {
        // 关闭时没有缓存也没有额外绘制，不维护无意义的投影版本号。
        if (!mEnable) {
            return;
        }
        mContentGeneration++;
        mCacheDirty = true;
        mImageView.invalidate();
    }

    /**
     * 在 ShapeImageView 的 onDraw 中、super.onDraw 之前绘制投影。
     *
     * <p>此时 View 的背景已经由 View.draw 绘制完成，投影会覆盖在背景之上，并在随后
     * 由 ImageView.super.onDraw 绘制的清晰原图之下。</p>
     */
    public void onDraw(@NonNull Canvas canvas) {
        if (!mEnable || mAlpha <= 0f || mImageView.getDrawable() == null ||
                mImageView.getWidth() <= 0 || mImageView.getHeight() <= 0) {
            return;
        }

        Drawable drawable = mImageView.getDrawable();
        Rect bounds = drawable.getBounds();
        if (bounds.isEmpty()) {
            return;
        }

        ensureBlurBitmap(drawable, bounds);
        if (mBlurBitmap == null) {
            return;
        }

        // 缓存记录的是 ImageView 当前真正可见的整块内容（包含 ScaleType 裁剪后的结果），
        // 因此目标区域也使用完整 View，再和清晰原图采用相同的居中缩放比例。
        mDrawRect.set(0f, 0f, mImageView.getWidth(), mImageView.getHeight());
        scaleRectFromCenter(mDrawRect, mImageScale);
        float expansion = Math.max(1f, mRadius);
        mDrawRect.set(
                mDrawRect.left - expansion + mOffsetX,
                mDrawRect.top - expansion + mOffsetY,
                mDrawRect.right + expansion + mOffsetX,
                mDrawRect.bottom + expansion + mOffsetY);

        mBitmapPaint.setAlpha(Math.round(mAlpha * 255f));
        canvas.drawBitmap(mBlurBitmap, null, mDrawRect, mBitmapPaint);
    }

    /** View 离开窗口时主动释放缓存，避免大图页面被暂时移除后继续占用内存。 */
    public void onDetachedFromWindow() {
        recycleBlurBitmap();
    }

    /** 重新进入窗口后按需懒加载投影。 */
    public void onAttachedToWindow() {
        mCacheDirty = true;
    }

    private void ensureBlurBitmap(@NonNull Drawable drawable, @NonNull Rect bounds) {
        if (!mCacheDirty && mCachedViewWidth == mImageView.getWidth() &&
                mCachedViewHeight == mImageView.getHeight() &&
                mCachedContentGeneration == mContentGeneration &&
                mSourceBounds.equals(bounds) && isSameImageMatrix()) {
            return;
        }

        int bitmapWidth = Math.max(1, Math.min(MAX_BITMAP_DIMENSION,
                Math.round(mImageView.getWidth() * mBitmapScale)));
        int bitmapHeight = Math.max(1, Math.min(MAX_BITMAP_DIMENSION,
                Math.round(mImageView.getHeight() * mBitmapScale)));
        Bitmap sourceBitmap = null;
        try {
            sourceBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas sourceCanvas = new Canvas(sourceBitmap);
            float scaleX = bitmapWidth / (float) Math.max(1, mImageView.getWidth());
            float scaleY = bitmapHeight / (float) Math.max(1, mImageView.getHeight());
            sourceCanvas.scale(scaleX, scaleY);
            // ImageView 的实际绘制还会应用 imageMatrix；同步该矩阵才能对齐 centerCrop、
            // fitCenter 和 matrix 等 ScaleType，而不是按 Drawable 原始尺寸猜测位置。
            sourceCanvas.translate(mImageView.getPaddingLeft(), mImageView.getPaddingTop());
            sourceCanvas.concat(mImageView.getImageMatrix());
            // 直接绘制当前 Drawable 可保留 src、tint 和 drawable alpha，且不会触发
            // setAlpha 带来的 Drawable.Callback 递归失效。
            drawable.draw(sourceCanvas);

            int blurPixels = Math.min(12, Math.max(1,
                    Math.round(mRadius * Math.min(scaleX, scaleY) / 2f)));
            Bitmap newBlurBitmap = blurBitmap(sourceBitmap, blurPixels);
            recycleBlurBitmap();
            mBlurBitmap = newBlurBitmap;
            mCachedViewWidth = mImageView.getWidth();
            mCachedViewHeight = mImageView.getHeight();
            mCachedContentGeneration = mContentGeneration;
            mSourceBounds.set(bounds);
            mImageView.getImageMatrix().getValues(mCachedImageMatrix);
            mCacheDirty = false;
        } finally {
            if (sourceBitmap != null && sourceBitmap != mBlurBitmap && !sourceBitmap.isRecycled()) {
                sourceBitmap.recycle();
            }
        }
    }

    /**
     * 在小图上执行两次一维盒式模糊，保留图片颜色并把每帧成本固定在小尺寸内。
     * 这是对原 BlurShadowImageView“缩小后放大”思路的增强：原图不会在每帧重复分配，
     * 同时半透明像素边缘会真正扩散，而不是只做一次生硬缩放。
     */
    @NonNull
    private static Bitmap blurBitmap(@NonNull Bitmap source, int radius) {
        int width = source.getWidth();
        int height = source.getHeight();
        int count = width * height;
        int[] sourcePixels = new int[count];
        int[] horizontalPixels = new int[count];
        int[] outputPixels = new int[count];
        source.getPixels(sourcePixels, 0, width, 0, 0, width, height);

        for (int y = 0; y < height; y++) {
            int row = y * width;
            for (int x = 0; x < width; x++) {
                int left = Math.max(0, x - radius);
                int right = Math.min(width - 1, x + radius);
                int red = 0;
                int green = 0;
                int blue = 0;
                int alpha = 0;
                for (int sampleX = left; sampleX <= right; sampleX++) {
                    int color = sourcePixels[row + sampleX];
                    alpha += Color.alpha(color);
                    red += Color.red(color);
                    green += Color.green(color);
                    blue += Color.blue(color);
                }
                int samples = right - left + 1;
                horizontalPixels[row + x] = Color.argb(
                        alpha / samples, red / samples, green / samples, blue / samples);
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int top = Math.max(0, y - radius);
                int bottom = Math.min(height - 1, y + radius);
                int red = 0;
                int green = 0;
                int blue = 0;
                int alpha = 0;
                for (int sampleY = top; sampleY <= bottom; sampleY++) {
                    int color = horizontalPixels[sampleY * width + x];
                    alpha += Color.alpha(color);
                    red += Color.red(color);
                    green += Color.green(color);
                    blue += Color.blue(color);
                }
                int samples = bottom - top + 1;
                outputPixels[y * width + x] = Color.argb(
                        alpha / samples, red / samples, green / samples, blue / samples);
            }
        }

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.setPixels(outputPixels, 0, width, 0, 0, width, height);
        return result;
    }

    private void invalidate() {
        mCacheDirty = true;
        mImageView.invalidate();
    }

    private void recycleBlurBitmap() {
        if (mBlurBitmap != null && !mBlurBitmap.isRecycled()) {
            mBlurBitmap.recycle();
        }
        mBlurBitmap = null;
        mCacheDirty = true;
        mCachedContentGeneration = -1;
    }

    /** 判断 ScaleType 或业务 imageMatrix 是否在缓存生成后发生变化。 */
    private boolean isSameImageMatrix() {
        mImageView.getImageMatrix().getValues(mCurrentImageMatrix);
        for (int i = 0; i < mCurrentImageMatrix.length; i++) {
            if (Float.compare(mCurrentImageMatrix[i], mCachedImageMatrix[i]) != 0) {
                return false;
            }
        }
        return true;
    }

    private static float dpToPx(@NonNull Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /** 围绕矩形中心缩放，保持图片与彩色投影中心一致。 */
    private static void scaleRectFromCenter(@NonNull RectF rect, float scale) {
        float halfWidth = rect.width() * scale / 2f;
        float halfHeight = rect.height() * scale / 2f;
        float centerX = rect.centerX();
        float centerY = rect.centerY();
        rect.set(centerX - halfWidth, centerY - halfHeight,
                centerX + halfWidth, centerY + halfHeight);
    }
}
