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
 *    desc   : 支持直接定义 Shape 背景的 ImageView
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
        if (mImageSourceBuilder != null) {
            mImageSourceBuilder.onDrawableStateChanged(getDrawableState());
        }
        if (mImageTintBuilder != null) {
            mImageTintBuilder.onDrawableStateChanged(getDrawableState());
        }
    }
}
