package com.allynav.shape.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.allynav.shape.R;
import com.allynav.shape.builder.ShapeDrawableBuilder;
import com.allynav.shape.config.IGetShapeDrawableBuilder;
import com.allynav.shape.other.ShapeViewGroupClipDelegate;
import com.allynav.shape.styleable.ShapeFrameLayoutStyleable;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ShapeView
 *    time   : 2021/07/17
 *    desc   : 支持直接定义 Shape 背景的 FrameLayout
 *
 * <p>不改变 FrameLayout 的子 View 叠放和测量规则，仅接入统一 Shape 背景、Ripple、
 * 状态色和阴影能力。动态修改通过 getShapeDrawableBuilder 完成。</p>
 */
public class ShapeFrameLayout extends FrameLayout implements IGetShapeDrawableBuilder {

    private static final ShapeFrameLayoutStyleable STYLEABLE = new ShapeFrameLayoutStyleable();

    private final ShapeDrawableBuilder mShapeDrawableBuilder;
    private final ShapeViewGroupClipDelegate mShapeViewGroupClipDelegate;

    public ShapeFrameLayout(Context context) {
        this(context, null);
    }

    public ShapeFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Builder 复制 XML 参数后立即回收 TypedArray，再一次性应用完整背景装饰链。
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeFrameLayout);
        mShapeDrawableBuilder = new ShapeDrawableBuilder(this, attrs, typedArray, STYLEABLE);
        typedArray.recycle();

        mShapeDrawableBuilder.intoBackground();
        mShapeViewGroupClipDelegate = new ShapeViewGroupClipDelegate(
                this, mShapeDrawableBuilder);
    }

    @Override
    public ShapeDrawableBuilder getShapeDrawableBuilder() {
        return mShapeDrawableBuilder;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int saveCount = mShapeViewGroupClipDelegate.save(canvas);
        try {
            super.dispatchDraw(canvas);
        } finally {
            mShapeViewGroupClipDelegate.restore(canvas, saveCount);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 返回 false 让父布局或子 View 继续处理，避免把“不可点击”变成“不可用”。
        if (mShapeDrawableBuilder.shouldBlockTouch()) {
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        // 保护容器的主动点击入口，子 View 的 performClick 不受影响。
        if (mShapeDrawableBuilder.shouldBlockTouch()) {
            return false;
        }
        return super.performClick();
    }
}
