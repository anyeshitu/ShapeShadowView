package com.allynav.shape.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.recyclerview.widget.RecyclerView;
import com.allynav.shape.R;
import com.allynav.shape.builder.ShapeDrawableBuilder;
import com.allynav.shape.config.IGetShapeDrawableBuilder;
import com.allynav.shape.styleable.ShapeRecyclerViewStyleable;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ShapeView
 *    time   : 2021/07/17
 *    desc   : 支持直接定义 Shape 背景的 RecyclerView
 *
 * <p>Adapter、LayoutManager、回收池和滚动逻辑全部沿用 RecyclerView，只增强列表容器
 * 背景。阴影会通过 padding inset 为列表内容预留绘制空间。</p>
 */
public class ShapeRecyclerView extends RecyclerView implements IGetShapeDrawableBuilder {

    private static final ShapeRecyclerViewStyleable STYLEABLE = new ShapeRecyclerViewStyleable();

    private final ShapeDrawableBuilder mShapeDrawableBuilder;

    public ShapeRecyclerView(Context context) {
        this(context, null);
    }

    public ShapeRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Builder 复制属性后立即回收 TypedArray，避免跨生命周期持有资源对象。
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeRecyclerView);
        mShapeDrawableBuilder = new ShapeDrawableBuilder(this, attrs, typedArray, STYLEABLE);
        typedArray.recycle();

        mShapeDrawableBuilder.intoBackground();
    }

    @Override
    public ShapeDrawableBuilder getShapeDrawableBuilder() {
        return mShapeDrawableBuilder;
    }
}
