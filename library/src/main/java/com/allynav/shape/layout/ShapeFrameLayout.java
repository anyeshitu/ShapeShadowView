package com.allynav.shape.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.allynav.shape.R;
import com.allynav.shape.builder.ShapeDrawableBuilder;
import com.allynav.shape.config.IGetShapeDrawableBuilder;
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
    }

    @Override
    public ShapeDrawableBuilder getShapeDrawableBuilder() {
        return mShapeDrawableBuilder;
    }
}
