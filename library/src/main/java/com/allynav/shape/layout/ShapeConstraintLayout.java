package com.allynav.shape.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.allynav.shape.R;
import com.allynav.shape.builder.ShapeDrawableBuilder;
import com.allynav.shape.config.IGetShapeDrawableBuilder;
import com.allynav.shape.styleable.ShapeConstraintLayoutStyleable;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ShapeView
 *    time   : 2021/07/17
 *    desc   : 支持直接定义 Shape 背景的 ConstraintLayout
 *
 * <p>保留 ConstraintLayout 原有约束布局行为，只增加 Shape 背景解析和动态 Builder
 * 入口。构造时读取并回收 TypedArray，随后立即应用 XML 背景配置。</p>
 */
public class ShapeConstraintLayout extends ConstraintLayout implements IGetShapeDrawableBuilder {

    private static final ShapeConstraintLayoutStyleable STYLEABLE = new ShapeConstraintLayoutStyleable();

    private final ShapeDrawableBuilder mShapeDrawableBuilder;

    public ShapeConstraintLayout(Context context) {
        this(context, null);
    }

    public ShapeConstraintLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // TypedArray 只在构造期间使用，Builder 会复制所需值，不持有资源数组。
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeConstraintLayout);
        mShapeDrawableBuilder = new ShapeDrawableBuilder(this, attrs, typedArray, STYLEABLE);
        typedArray.recycle();

        mShapeDrawableBuilder.intoBackground();
    }

    @Override
    public ShapeDrawableBuilder getShapeDrawableBuilder() {
        return mShapeDrawableBuilder;
    }
}
