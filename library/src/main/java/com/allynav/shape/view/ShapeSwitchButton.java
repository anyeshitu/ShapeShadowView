package com.allynav.shape.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import com.allynav.shape.R;
import com.allynav.shape.builder.ShapeDrawableBuilder;
import com.allynav.shape.config.IGetShapeDrawableBuilder;
import com.allynav.shape.styleable.ShapeSwitchButtonStyleable;

/**
 * 具有 Shape 背景、阴影、Ripple 和滑块动画的开关控件。
 *
 * <p>控件基于 {@link SwitchCompat}，保留 AndroidX 对 checked、触摸拖动、无障碍、
 * RTL 和状态保存的处理；轨道由现有 {@link ShapeDrawableBuilder} 负责绘制，滑块
 * 由本控件在内容区域内绘制。这样不会把开关状态混入普通 {@link ShapeButton}。</p>
 *
 * <p>建议直接给控件设置固定宽高。未配置宽高时，控件按 58dp x 36dp 测量，尺寸与
 * ShadowLayout 原版 SwitchButton 的默认尺寸保持接近。</p>
 */
public class ShapeSwitchButton extends SwitchCompat implements IGetShapeDrawableBuilder {

    private static final ShapeSwitchButtonStyleable STYLEABLE = new ShapeSwitchButtonStyleable();
    private static final int DEFAULT_WIDTH_DP = 58;
    private static final int DEFAULT_HEIGHT_DP = 36;
    private static final int DEFAULT_THUMB_COLOR = Color.WHITE;
    private static final int DEFAULT_DISABLED_THUMB_COLOR = 0xFFBDBDBD;

    private final ShapeDrawableBuilder mShapeDrawableBuilder;
    private SwitchThumbDrawable mThumbDrawable;

    private int mThumbColor;
    private int mThumbCheckedColor;
    private int mThumbPressedColor;
    private int mThumbDisabledColor;
    private int mThumbInset;
    private boolean mAnimationEnable;
    public ShapeSwitchButton(Context context) {
        this(context, null);
    }

    public ShapeSwitchButton(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.switchStyle);
    }

    public ShapeSwitchButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 使用固定尺寸的透明轨道保留 SwitchCompat 的测量和拖动范围，轨道可见部分
        // 由 ShapeDrawableBuilder 绘制；自定义滑块 Drawable 则由 SwitchCompat 负责定位。
        setShowText(false);

        TypedArray typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.ShapeSwitchButton, 0, R.style.ShapeSwitchButtonStyle);
        mShapeDrawableBuilder = new ShapeDrawableBuilder(this, attrs, typedArray, STYLEABLE);
        mThumbColor = typedArray.getColor(
                R.styleable.ShapeSwitchButton_shape_switchThumbColor, DEFAULT_THUMB_COLOR);
        mThumbCheckedColor = typedArray.getColor(
                R.styleable.ShapeSwitchButton_shape_switchThumbCheckedColor, mThumbColor);
        mThumbPressedColor = typedArray.getColor(
                R.styleable.ShapeSwitchButton_shape_switchThumbPressedColor, mThumbColor);
        mThumbDisabledColor = typedArray.getColor(
                R.styleable.ShapeSwitchButton_shape_switchThumbDisabledColor,
                DEFAULT_DISABLED_THUMB_COLOR);
        mThumbInset = typedArray.getDimensionPixelSize(
                R.styleable.ShapeSwitchButton_shape_switchThumbInset, dpToPx(2));
        mAnimationEnable = typedArray.getBoolean(
                R.styleable.ShapeSwitchButton_shape_switchAnimationEnable, true);
        typedArray.recycle();

        super.setTrackDrawable(new TransparentTrackDrawable(
                dpToPx(DEFAULT_WIDTH_DP), dpToPx(DEFAULT_HEIGHT_DP)));
        mThumbDrawable = new SwitchThumbDrawable(dpToPx(32));
        super.setThumbDrawable(mThumbDrawable);
        mShapeDrawableBuilder.intoBackground();
        refreshDrawableState();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 关闭 SwitchCompat 的文字宽度测量，避免默认 compound drawable 的最小尺寸影响轨道。
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int defaultWidth = dpToPx(DEFAULT_WIDTH_DP) + getPaddingLeft() + getPaddingRight();
        int defaultHeight = dpToPx(DEFAULT_HEIGHT_DP) + getPaddingTop() + getPaddingBottom();
        int measuredWidth = resolveDefaultSize(getMeasuredWidth(), widthMeasureSpec, defaultWidth);
        int measuredHeight = resolveDefaultSize(getMeasuredHeight(), heightMeasureSpec, defaultHeight);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    private int resolveDefaultSize(int measuredSize, int measureSpec, int defaultSize) {
        if (MeasureSpec.getMode(measureSpec) == MeasureSpec.UNSPECIFIED) {
            return defaultSize;
        }
        return measuredSize;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        // 轨道和滑块都交给 SwitchCompat 绘制；轨道 Drawable 透明，所以最终轨道仍是
        // View 背景中的 ShapeDrawable，滑块位置则会在点击和拖动时实时更新。
        super.onDraw(canvas);
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        if (!mAnimationEnable) {
            // SwitchCompat 内部使用 Animator 做位置过渡；关闭动画时直接跳到最终位置。
            jumpDrawablesToCurrentState();
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        // ShapeDrawableBuilder 的状态背景依赖 View 状态；滑块颜色也需要同步刷新。
        if (mThumbDrawable != null) {
            mThumbDrawable.setState(getDrawableState());
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 复用 ShapeView 的独立不可点击开关，同时保留 SwitchCompat 的拖动处理。
        if (mShapeDrawableBuilder.shouldBlockTouch()) {
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        if (mShapeDrawableBuilder.shouldBlockTouch()) {
            return false;
        }
        return super.performClick();
    }

    @Override
    public ShapeDrawableBuilder getShapeDrawableBuilder() {
        return mShapeDrawableBuilder;
    }

    public int getThumbColor() {
        return mThumbColor;
    }

    public void setThumbColor(@ColorInt int color) {
        mThumbColor = color;
        invalidateThumb();
    }

    public int getThumbCheckedColor() {
        return mThumbCheckedColor;
    }

    public void setThumbCheckedColor(@ColorInt int color) {
        mThumbCheckedColor = color;
        invalidateThumb();
    }

    public int getThumbPressedColor() {
        return mThumbPressedColor;
    }

    public void setThumbPressedColor(@ColorInt int color) {
        mThumbPressedColor = color;
        invalidateThumb();
    }

    public int getThumbDisabledColor() {
        return mThumbDisabledColor;
    }

    public void setThumbDisabledColor(@ColorInt int color) {
        mThumbDisabledColor = color;
        invalidateThumb();
    }

    public int getThumbInset() {
        return mThumbInset;
    }

    public void setThumbInset(int inset) {
        mThumbInset = Math.max(0, inset);
        invalidateThumb();
    }

    public boolean isSwitchAnimationEnabled() {
        return mAnimationEnable;
    }

    public void setSwitchAnimationEnabled(boolean enabled) {
        mAnimationEnable = enabled;
        if (!enabled) {
            // 动态关闭动画时，同时结束当前正在运行的 checked 位置过渡。
            jumpDrawablesToCurrentState();
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void invalidateThumb() {
        if (mThumbDrawable != null) {
            mThumbDrawable.invalidateSelf();
        }
        invalidate();
    }

    /** 不参与实际绘制，只为 SwitchCompat 提供稳定的轨道尺寸和拖动范围。 */
    private static final class TransparentTrackDrawable extends Drawable {

        private final int mWidth;
        private final int mHeight;

        private TransparentTrackDrawable(int width, int height) {
            mWidth = width;
            mHeight = height;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            // ShapeSwitchButton 的真实轨道是 View background，这里故意保持透明。
        }

        @Override
        public int getIntrinsicWidth() {
            return mWidth;
        }

        @Override
        public int getIntrinsicHeight() {
            return mHeight;
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSPARENT;
        }
    }

    /**
     * 由 SwitchCompat 设置 bounds 并绘制的滑块 Drawable。
     * SwitchCompat 的 bounds 会随着点击、拖动和 checked 动画实时变化，因此不需要
     * 在 ShapeSwitchButton 中重复实现一套手势状态机。
     */
    private final class SwitchThumbDrawable extends Drawable {

        private final int mSize;
        private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF mRect = new RectF();
        private int mAlpha = 0xFF;
        private ColorFilter mColorFilter;

        private SwitchThumbDrawable(int size) {
            mSize = size;
            mPaint.setStyle(Paint.Style.FILL);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            Rect bounds = getBounds();
            float inset = Math.min(mThumbInset, Math.min(bounds.width(), bounds.height()) / 2f);
            mRect.set(bounds.left + inset, bounds.top + inset,
                    bounds.right - inset, bounds.bottom - inset);
            mPaint.setColor(resolveStateColor());
            mPaint.setAlpha(mAlpha);
            mPaint.setColorFilter(mColorFilter);
            canvas.drawCircle(mRect.centerX(), mRect.centerY(),
                    Math.min(mRect.width(), mRect.height()) / 2f, mPaint);
        }

        @ColorInt
        private int resolveStateColor() {
            int[] state = getState();
            if (containsState(state, -android.R.attr.state_enabled)) {
                return mThumbDisabledColor;
            }
            if (containsState(state, android.R.attr.state_pressed)) {
                return mThumbPressedColor;
            }
            if (containsState(state, android.R.attr.state_checked)) {
                return mThumbCheckedColor;
            }
            return mThumbColor;
        }

        private boolean containsState(int[] stateSet, int state) {
            for (int current : stateSet) {
                if (current == state) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean isStateful() {
            return true;
        }

        @Override
        protected boolean onStateChange(int[] state) {
            invalidateSelf();
            return true;
        }

        @Override
        public int getIntrinsicWidth() {
            return mSize;
        }

        @Override
        public int getIntrinsicHeight() {
            return mSize;
        }

        @Override
        public void setAlpha(int alpha) {
            mAlpha = alpha;
            invalidateSelf();
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            mColorFilter = colorFilter;
            invalidateSelf();
        }

        @Override
        public int getOpacity() {
            return mAlpha == 0xFF ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT;
        }
    }
}
