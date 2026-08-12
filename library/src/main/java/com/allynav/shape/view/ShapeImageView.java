package com.allynav.shape.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;
import com.allynav.shape.R;
import com.allynav.shape.builder.ImageSourceBuilder;
import com.allynav.shape.builder.ShapeDrawableBuilder;
import com.allynav.shape.builder.ImageTintBuilder;
import com.allynav.shape.config.IGetShapeDrawableBuilder;
import com.allynav.shape.styleable.ShapeImageViewStyleable;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ShapeView
 *    time   : 2021/07/17
 *    desc   : 支持 Shape 背景以及状态 src、tint 的 ImageView
 *
 * <p>背景由 ShapeDrawableBuilder 管理，图片内容由 ImageSourceBuilder 管理，着色由
 * ImageTintBuilder 管理。三者职责独立，可以只开启其中任意一项。</p>
 */
public class ShapeImageView extends AppCompatImageView implements IGetShapeDrawableBuilder {

    private static final ShapeImageViewStyleable STYLEABLE = new ShapeImageViewStyleable();

    private final ShapeDrawableBuilder mShapeDrawableBuilder;
    private final ImageTintBuilder mImageTintBuilder;
    private final ImageSourceBuilder mImageSourceBuilder;

    public ShapeImageView(Context context) {
        this(context, null);
    }

    public ShapeImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 背景属性使用主 TypedArray；src/tint 使用独立属性数组并由各 Builder 自行回收。
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeImageView);
        mShapeDrawableBuilder = new ShapeDrawableBuilder(this, attrs, typedArray, STYLEABLE);
        typedArray.recycle();

        mShapeDrawableBuilder.intoBackground();
        mImageTintBuilder = new ImageTintBuilder(this, attrs);
        if (mImageTintBuilder.hasCustomTint()) {
            mImageTintBuilder.intoTint();
        }
        mImageSourceBuilder = new ImageSourceBuilder(this, attrs);
        if (mImageSourceBuilder.hasCustomSource()) {
            mImageSourceBuilder.intoSource();
        }
    }

    @Override
    public ShapeDrawableBuilder getShapeDrawableBuilder() {
        return mShapeDrawableBuilder;
    }

    public ImageTintBuilder getImageTintBuilder() {
        return mImageTintBuilder;
    }

    public ImageSourceBuilder getImageSourceBuilder() {
        return mImageSourceBuilder;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        // 每次 Android DrawableState 变化时，先换 src，再按同一状态更新 tint。
        if (mImageSourceBuilder != null) {
            mImageSourceBuilder.onDrawableStateChanged(getDrawableState());
        }
        if (mImageTintBuilder != null) {
            mImageTintBuilder.onDrawableStateChanged(getDrawableState());
        }
    }
}
