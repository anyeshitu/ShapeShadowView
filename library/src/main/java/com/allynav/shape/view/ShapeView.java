package com.allynav.shape.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import com.allynav.shape.R;
import com.allynav.shape.builder.ShapeDrawableBuilder;
import com.allynav.shape.config.IGetShapeDrawableBuilder;
import com.allynav.shape.styleable.ShapeViewStyleable;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ShapeView
 *    time   : 2021/07/17
 *    desc   : 只增强 Shape 背景能力的基础 View
 *
 * <p>适用于分割线、色块和无需文本/图片内容的轻量元素。控件只持有一个
 * ShapeDrawableBuilder，不改变 View 的测量、点击和可见性行为。</p>
 */
public class ShapeView extends View implements IGetShapeDrawableBuilder {

    private static final ShapeViewStyleable STYLEABLE = new ShapeViewStyleable();

    private final ShapeDrawableBuilder mShapeDrawableBuilder;

    public ShapeView(Context context) {
        this(context, null);
    }

    public ShapeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Builder 复制属性后回收 TypedArray，并立即应用 XML 中声明的背景。
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeView);
        mShapeDrawableBuilder = new ShapeDrawableBuilder(this, attrs, typedArray, STYLEABLE);
        typedArray.recycle();

        mShapeDrawableBuilder.intoBackground();
    }

    @Override
    public ShapeDrawableBuilder getShapeDrawableBuilder() {
        return mShapeDrawableBuilder;
    }
}
