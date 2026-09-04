package com.allynav.shape.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.allynav.shape.R;
import com.allynav.shape.builder.ShapeDrawableBuilder;
import com.allynav.shape.config.IGetShapeDrawableBuilder;
import com.allynav.shape.other.ShapeViewGroupClipDelegate;
import com.allynav.shape.styleable.ShapeLinearLayoutStyleable;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ShapeView
 *    time   : 2021/07/17
 *    desc   : 支持直接定义 Shape 背景的 LinearLayout
 *
 * <p>保留 orientation、weight 和 baseline 等 LinearLayout 行为，只为容器背景增加
 * ShapeView 风格 XML 属性及 Java Builder 配置。</p>
 */
public class ShapeLinearLayout extends LinearLayout implements IGetShapeDrawableBuilder {

    private static final ShapeLinearLayoutStyleable STYLEABLE = new ShapeLinearLayoutStyleable();

    private final ShapeDrawableBuilder mShapeDrawableBuilder;
    private final ShapeViewGroupClipDelegate mShapeViewGroupClipDelegate;

    public ShapeLinearLayout(Context context) {
        this(context, null);
    }

    public ShapeLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 构造期间完成属性复制和 TypedArray 回收，Builder 在控件生命周期内复用。
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeLinearLayout);
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
        // shape_clickable=false 只阻止容器自身点击，不改变 enabled，也不阻止子 View 接收事件。
        if (mShapeDrawableBuilder.shouldBlockTouch()) {
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        // 键盘、无障碍或业务主动调用 performClick 时也必须遵守独立不可点击状态。
        if (mShapeDrawableBuilder.shouldBlockTouch()) {
            return false;
        }
        return super.performClick();
    }
}
