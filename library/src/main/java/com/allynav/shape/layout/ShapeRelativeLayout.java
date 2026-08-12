package com.allynav.shape.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.allynav.shape.R;
import com.allynav.shape.builder.ShapeDrawableBuilder;
import com.allynav.shape.config.IGetShapeDrawableBuilder;
import com.allynav.shape.styleable.ShapeRelativeLayoutStyleable;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ShapeView
 *    time   : 2021/07/17
 *    desc   : 支持直接定义 Shape 背景的 RelativeLayout
 *
 * <p>保留 RelativeLayout 的规则解析和子 View 定位，仅在构造阶段创建并应用统一
 * Shape 背景 Builder。</p>
 */
public class ShapeRelativeLayout extends RelativeLayout implements IGetShapeDrawableBuilder {

    private static final ShapeRelativeLayoutStyleable STYLEABLE = new ShapeRelativeLayoutStyleable();

    private final ShapeDrawableBuilder mShapeDrawableBuilder;

    public ShapeRelativeLayout(Context context) {
        this(context, null);
    }

    public ShapeRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // TypedArray 由当前构造函数拥有，Builder 初始化完成后立即回收。
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeRelativeLayout);
        mShapeDrawableBuilder = new ShapeDrawableBuilder(this, attrs, typedArray, STYLEABLE);
        typedArray.recycle();

        mShapeDrawableBuilder.intoBackground();
    }

    @Override
    public ShapeDrawableBuilder getShapeDrawableBuilder() {
        return mShapeDrawableBuilder;
    }
}
