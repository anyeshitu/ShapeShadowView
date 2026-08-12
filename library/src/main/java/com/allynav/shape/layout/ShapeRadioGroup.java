package com.allynav.shape.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RadioGroup;

import com.allynav.shape.R;
import com.allynav.shape.builder.ShapeDrawableBuilder;
import com.allynav.shape.config.IGetShapeDrawableBuilder;
import com.allynav.shape.styleable.ShapeRadioGroupStyleable;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ShapeView
 *    time   : 2021/09/07
 *    desc   : 支持直接定义 Shape 背景的 RadioGroup
 *
 * <p>RadioButton 的互斥选择仍由 Android RadioGroup 管理，本类只增强容器背景，
 * 不拦截 checkedId、监听器或子 View 状态。</p>
 */
public class ShapeRadioGroup extends RadioGroup implements IGetShapeDrawableBuilder {

    private static final ShapeRadioGroupStyleable STYLEABLE = new ShapeRadioGroupStyleable();

    private final ShapeDrawableBuilder mShapeDrawableBuilder;

    public ShapeRadioGroup(Context context) {
        this(context, null);
    }

    public ShapeRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Shape 参数复制后立即回收 TypedArray，再应用容器背景。
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeRadioGroup);
        mShapeDrawableBuilder = new ShapeDrawableBuilder(this, attrs, typedArray, STYLEABLE);
        typedArray.recycle();

        mShapeDrawableBuilder.intoBackground();
    }

    @Override
    public ShapeDrawableBuilder getShapeDrawableBuilder() {
        return mShapeDrawableBuilder;
    }
}
