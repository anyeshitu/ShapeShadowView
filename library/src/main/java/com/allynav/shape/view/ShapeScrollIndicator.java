package com.allynav.shape.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;

import com.allynav.shape.R;

/**
 * 可绑定普通 ScrollView 或 HorizontalScrollView 的滚动指示器。
 *
 * <p>组件只绘制轨道和滑块，滚动位置通过目标 View 的 ViewTreeObserver 观察，
 * 不会覆盖业务已经设置的 {@code setOnScrollChangeListener}。内容没有超出可视区域
 * 时自动隐藏；默认在滚动后显示一段时间再渐隐，也可以使用 alwaysShow 保持显示。</p>
 *
 * <p>纵向指示器通常使用窄宽高布局，横向指示器通常使用宽窄布局。方向既可以由
 * XML 的 {@code shape_indicatorOrientation} 指定，也会在 bindHorizontalScrollView
 * 时自动切换为横向。</p>
 */
public class ShapeScrollIndicator extends View {

    public static final int ORIENTATION_VERTICAL = 0;
    public static final int ORIENTATION_HORIZONTAL = 1;

    private static final int DEFAULT_TRACK_COLOR = 0x331B1C21;
    private static final int DEFAULT_INDICATOR_COLOR = 0x4DECF7FF;
    private static final float DEFAULT_INDICATOR_LENGTH = 0.2f;
    private static final long DEFAULT_ANIMATION_DURATION = 500L;
    private static final long DEFAULT_HIDE_DELAY = 1500L;

    private final Paint mTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mDrawRect = new RectF();

    private int mOrientation;
    private float mIndicatorLength;
    private float mProgress;
    private boolean mTrackRound;
    private long mAnimationDuration;
    private long mHideDelay;
    private boolean mAlwaysShow;

    private ScrollView mScrollView;
    private HorizontalScrollView mHorizontalScrollView;
    private ViewTreeObserver mBoundObserver;
    private boolean mOldVerticalScrollBarEnabled;
    private boolean mOldHorizontalScrollBarEnabled;
    private int mOldOverScrollMode;
    private boolean mScrollViewOptionsSaved;

    private final ViewTreeObserver.OnScrollChangedListener mScrollChangedListener =
            this::updateFromBoundScrollView;
    private final ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener =
            this::updateFromBoundScrollView;
    private final Runnable mHideRunnable = () -> {
        if (!mAlwaysShow && hasScrollableContent()) {
            animate().alpha(0f).setDuration(mAnimationDuration).start();
        }
    };

    public ShapeScrollIndicator(Context context) {
        this(context, null);
    }

    public ShapeScrollIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeScrollIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.ShapeScrollIndicator);
        mOrientation = typedArray.getInt(
                R.styleable.ShapeScrollIndicator_shape_indicatorOrientation,
                ORIENTATION_VERTICAL);
        mTrackPaint.setColor(typedArray.getColor(
                R.styleable.ShapeScrollIndicator_shape_indicatorTrackColor,
                DEFAULT_TRACK_COLOR));
        mIndicatorPaint.setColor(typedArray.getColor(
                R.styleable.ShapeScrollIndicator_shape_indicatorColor,
                DEFAULT_INDICATOR_COLOR));
        mIndicatorLength = clampLength(typedArray.getFloat(
                R.styleable.ShapeScrollIndicator_shape_indicatorLength,
                DEFAULT_INDICATOR_LENGTH));
        mTrackRound = typedArray.getBoolean(
                R.styleable.ShapeScrollIndicator_shape_indicatorTrackRound, true);
        mAnimationDuration = Math.max(0L, typedArray.getInt(
                R.styleable.ShapeScrollIndicator_shape_indicatorAnimationDuration,
                (int) DEFAULT_ANIMATION_DURATION));
        mHideDelay = Math.max(0L, typedArray.getInt(
                R.styleable.ShapeScrollIndicator_shape_indicatorHideDelay,
                (int) DEFAULT_HIDE_DELAY));
        mAlwaysShow = typedArray.getBoolean(
                R.styleable.ShapeScrollIndicator_shape_indicatorAlwaysShow, false);
        typedArray.recycle();

        setVisibility(GONE);
        setAlpha(1f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int defaultWidth = dpToPx(mOrientation == ORIENTATION_VERTICAL ? 4 : 100);
        int defaultHeight = dpToPx(mOrientation == ORIENTATION_VERTICAL ? 100 : 4);
        int width = resolveDefaultSize(getMeasuredWidth(), widthMeasureSpec, defaultWidth);
        int height = resolveDefaultSize(getMeasuredHeight(), heightMeasureSpec, defaultHeight);
        setMeasuredDimension(width, height);
    }

    private int resolveDefaultSize(int measuredSize, int measureSpec, int defaultSize) {
        int mode = MeasureSpec.getMode(measureSpec);
        if (mode == MeasureSpec.UNSPECIFIED) {
            return defaultSize;
        }
        if (mode == MeasureSpec.AT_MOST) {
            return Math.min(measuredSize, MeasureSpec.getSize(measureSpec));
        }
        return measuredSize;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        if (mOrientation == ORIENTATION_VERTICAL) {
            drawVertical(canvas);
        } else {
            drawHorizontal(canvas);
        }
    }

    private void drawVertical(Canvas canvas) {
        float radius = mTrackRound ? getWidth() / 2f : 0f;
        mDrawRect.set(0f, 0f, getWidth(), getHeight());
        canvas.drawRoundRect(mDrawRect, radius, radius, mTrackPaint);

        float indicatorHeight = Math.max(getWidth(), getHeight() * mIndicatorLength);
        float top = (getHeight() - indicatorHeight) * mProgress;
        mDrawRect.set(0f, top, getWidth(), top + indicatorHeight);
        canvas.drawRoundRect(mDrawRect, radius, radius, mIndicatorPaint);
    }

    private void drawHorizontal(Canvas canvas) {
        float radius = mTrackRound ? getHeight() / 2f : 0f;
        mDrawRect.set(0f, 0f, getWidth(), getHeight());
        canvas.drawRoundRect(mDrawRect, radius, radius, mTrackPaint);

        float indicatorWidth = Math.max(getHeight(), getWidth() * mIndicatorLength);
        float left = (getWidth() - indicatorWidth) * mProgress;
        mDrawRect.set(left, 0f, left + indicatorWidth, getHeight());
        canvas.drawRoundRect(mDrawRect, radius, radius, mIndicatorPaint);
    }

    /** 绑定垂直 ScrollView，并隐藏该 ScrollView 自带的滚动条。 */
    public void bindScrollView(@NonNull ScrollView scrollView) {
        bindTarget(scrollView, ORIENTATION_VERTICAL);
    }

    /** 绑定横向 HorizontalScrollView，并隐藏该 View 自带的滚动条。 */
    public void bindHorizontalScrollView(@NonNull HorizontalScrollView scrollView) {
        bindTarget(scrollView, ORIENTATION_HORIZONTAL);
    }

    /**
     * 解除当前绑定并恢复目标 ScrollView 原来的滚动条和 overscroll 配置。
     * 解除后指示器不会再根据目标滚动变化，也不会继续持有目标 View 的监听器。
     */
    public void unbind() {
        unbindTarget();
        setVisibility(GONE);
        setAlpha(1f);
    }

    private void bindTarget(@NonNull View target, int orientation) {
        unbindTarget();
        mOrientation = orientation;
        if (target instanceof ScrollView) {
            mScrollView = (ScrollView) target;
        } else if (target instanceof HorizontalScrollView) {
            mHorizontalScrollView = (HorizontalScrollView) target;
        }

        saveAndDisableScrollBars(target);
        registerTargetObserver(target);
        requestLayout();
        post(this::updateFromBoundScrollView);
    }

    private void saveAndDisableScrollBars(@NonNull View target) {
        if (target instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) target;
            mOldVerticalScrollBarEnabled = scrollView.isVerticalScrollBarEnabled();
            mOldHorizontalScrollBarEnabled = scrollView.isHorizontalScrollBarEnabled();
            mOldOverScrollMode = scrollView.getOverScrollMode();
            mScrollViewOptionsSaved = true;
            scrollView.setVerticalScrollBarEnabled(false);
            scrollView.setHorizontalScrollBarEnabled(false);
        } else if (target instanceof HorizontalScrollView) {
            HorizontalScrollView scrollView = (HorizontalScrollView) target;
            mOldVerticalScrollBarEnabled = scrollView.isVerticalScrollBarEnabled();
            mOldHorizontalScrollBarEnabled = scrollView.isHorizontalScrollBarEnabled();
            mOldOverScrollMode = scrollView.getOverScrollMode();
            mScrollViewOptionsSaved = true;
            scrollView.setVerticalScrollBarEnabled(false);
            scrollView.setHorizontalScrollBarEnabled(false);
        }
    }

    private void registerTargetObserver(@NonNull View target) {
        mBoundObserver = target.getViewTreeObserver();
        mBoundObserver.addOnScrollChangedListener(mScrollChangedListener);
        mBoundObserver.addOnGlobalLayoutListener(mGlobalLayoutListener);
    }

    /**
     * 当业务已经自行监听滚动时，可在业务监听器中转发此方法，避免任何监听器互相覆盖。
     * 该方法同时保留 ShadowLayout 原版的调用方式。
     */
    public void bindScrollViewFromScrollListener(
            @NonNull View view, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (view instanceof HorizontalScrollView) {
            mHorizontalScrollView = (HorizontalScrollView) view;
            mScrollView = null;
            mOrientation = ORIENTATION_HORIZONTAL;
        } else if (view instanceof ScrollView) {
            mScrollView = (ScrollView) view;
            mHorizontalScrollView = null;
            mOrientation = ORIENTATION_VERTICAL;
        } else {
            return;
        }
        updateFromBoundScrollView();
    }

    /** 正确拼写的进度设置方法，取值范围为 0 到 1。 */
    public void setProgressPercent(@FloatRange(from = 0.0, to = 1.0) float percent) {
        mProgress = clampProgress(percent);
        invalidate();
    }

    /**
     * ShadowLayout 原版的兼容方法；方法名中的 Precent 拼写错误及“设置滑块长度”的
     * 历史语义都保留，方便迁移旧代码。当前位置请使用 setProgressPercent。
     */
    @SuppressWarnings("unused")
    public void setProcessPrecent(@FloatRange(from = 0.0, to = 1.0) float percent) {
        setIndicatorLength(percent);
    }

    @ColorInt
    public int getIndicatorBackgroundColor() {
        return mTrackPaint.getColor();
    }

    public void setIndicatorBackgroundColor(@ColorInt int color) {
        mTrackPaint.setColor(color);
        invalidate();
    }

    @ColorInt
    public int getIndicatorColor() {
        return mIndicatorPaint.getColor();
    }

    public void setIndicatorColor(@ColorInt int color) {
        mIndicatorPaint.setColor(color);
        invalidate();
    }

    public float getIndicatorLength() {
        return mIndicatorLength;
    }

    public void setIndicatorLength(@FloatRange(from = 0.0, to = 1.0) float length) {
        mIndicatorLength = clampLength(length);
        invalidate();
    }

    public boolean isAlwaysShow() {
        return mAlwaysShow;
    }

    public void setAlwaysShow(boolean alwaysShow) {
        mAlwaysShow = alwaysShow;
        updateIndicatorVisibility();
    }

    public long getAnimationDuration() {
        return mAnimationDuration;
    }

    public void setAnimationDuration(long duration) {
        mAnimationDuration = Math.max(0L, duration);
    }

    public long getHideDelay() {
        return mHideDelay;
    }

    public void setHideDelay(long delay) {
        mHideDelay = Math.max(0L, delay);
    }

    public int getIndicatorOrientation() {
        return mOrientation;
    }

    public void setIndicatorOrientation(int orientation) {
        if (orientation != ORIENTATION_VERTICAL && orientation != ORIENTATION_HORIZONTAL) {
            throw new IllegalArgumentException("orientation must be vertical or horizontal");
        }
        if (mOrientation != orientation) {
            mOrientation = orientation;
            requestLayout();
            invalidate();
        }
    }

    private void updateFromBoundScrollView() {
        if (mScrollView != null) {
            View child = mScrollView.getChildAt(0);
            int viewport = mScrollView.getHeight() - mScrollView.getPaddingTop()
                    - mScrollView.getPaddingBottom();
            int content = child == null ? 0 : child.getHeight();
            int maxScroll = Math.max(0, content - Math.max(0, viewport));
            setProgressWithoutVisibility(maxScroll == 0
                    ? 0f : mScrollView.getScrollY() / (float) maxScroll);
            updateIndicatorVisibility(maxScroll > 0);
            return;
        }
        if (mHorizontalScrollView != null) {
            View child = mHorizontalScrollView.getChildAt(0);
            int viewport = mHorizontalScrollView.getWidth() - mHorizontalScrollView.getPaddingLeft()
                    - mHorizontalScrollView.getPaddingRight();
            int content = child == null ? 0 : child.getWidth();
            int maxScroll = Math.max(0, content - Math.max(0, viewport));
            setProgressWithoutVisibility(maxScroll == 0
                    ? 0f : mHorizontalScrollView.getScrollX() / (float) maxScroll);
            updateIndicatorVisibility(maxScroll > 0);
        }
    }

    private void setProgressWithoutVisibility(float progress) {
        mProgress = clampProgress(progress);
        invalidate();
    }

    private boolean hasScrollableContent() {
        if (mScrollView != null) {
            View child = mScrollView.getChildAt(0);
            return child != null && child.getHeight() > mScrollView.getHeight()
                    - mScrollView.getPaddingTop() - mScrollView.getPaddingBottom();
        }
        if (mHorizontalScrollView != null) {
            View child = mHorizontalScrollView.getChildAt(0);
            return child != null && child.getWidth() > mHorizontalScrollView.getWidth()
                    - mHorizontalScrollView.getPaddingLeft() - mHorizontalScrollView.getPaddingRight();
        }
        return false;
    }

    private void updateIndicatorVisibility() {
        updateIndicatorVisibility(hasScrollableContent());
    }

    private void updateIndicatorVisibility(boolean hasScrollableContent) {
        removeCallbacks(mHideRunnable);
        if (!hasScrollableContent) {
            animate().cancel();
            setAlpha(1f);
            setVisibility(GONE);
            return;
        }

        if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
        }
        animate().cancel();
        setAlpha(1f);
        if (!mAlwaysShow) {
            postDelayed(mHideRunnable, mHideDelay);
        }
    }

    private void unbindTarget() {
        removeCallbacks(mHideRunnable);
        if (mBoundObserver != null && mBoundObserver.isAlive()) {
            mBoundObserver.removeOnScrollChangedListener(mScrollChangedListener);
            mBoundObserver.removeOnGlobalLayoutListener(mGlobalLayoutListener);
        }
        mBoundObserver = null;

        if (mScrollViewOptionsSaved) {
            View target = mScrollView != null ? mScrollView : mHorizontalScrollView;
            if (target instanceof ScrollView) {
                restoreScrollBars((ScrollView) target);
            } else if (target instanceof HorizontalScrollView) {
                restoreScrollBars((HorizontalScrollView) target);
            }
        }
        mScrollViewOptionsSaved = false;
        mScrollView = null;
        mHorizontalScrollView = null;
    }

    private void restoreScrollBars(@NonNull ScrollView scrollView) {
        scrollView.setVerticalScrollBarEnabled(mOldVerticalScrollBarEnabled);
        scrollView.setHorizontalScrollBarEnabled(mOldHorizontalScrollBarEnabled);
        scrollView.setOverScrollMode(mOldOverScrollMode);
    }

    private float clampProgress(float progress) {
        return Math.max(0f, Math.min(1f, progress));
    }

    private float clampLength(float length) {
        return Math.max(0.01f, Math.min(1f, length));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        View target = mScrollView != null ? mScrollView : mHorizontalScrollView;
        if (target != null && mBoundObserver == null) {
            registerTargetObserver(target);
            post(this::updateFromBoundScrollView);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        unbindObserverOnly();
        removeCallbacks(mHideRunnable);
        super.onDetachedFromWindow();
    }

    private void unbindObserverOnly() {
        if (mBoundObserver != null && mBoundObserver.isAlive()) {
            mBoundObserver.removeOnScrollChangedListener(mScrollChangedListener);
            mBoundObserver.removeOnGlobalLayoutListener(mGlobalLayoutListener);
        }
        mBoundObserver = null;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
