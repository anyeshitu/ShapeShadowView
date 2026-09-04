package com.allynav.shape.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.appcompat.widget.AppCompatImageView;
import com.allynav.shape.R;
import com.allynav.shape.builder.ImageSourceBuilder;
import com.allynav.shape.builder.ShapeDrawableBuilder;
import com.allynav.shape.builder.ImageTintBuilder;
import com.allynav.shape.builder.ImageBlurShadowBuilder;
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
    private final ImageBlurShadowBuilder mImageBlurShadowBuilder;

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
        mImageSourceBuilder = new ImageSourceBuilder(this, attrs);
        mImageBlurShadowBuilder = new ImageBlurShadowBuilder(this, attrs);
        if (mImageTintBuilder.hasCustomTint()) {
            mImageTintBuilder.intoTint();
        }
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

    /** 返回图片内容驱动的彩色模糊投影配置器，默认关闭。 */
    public ImageBlurShadowBuilder getImageBlurShadowBuilder() {
        return mImageBlurShadowBuilder;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        // Glide/Picasso 以及状态 src 最终都会经过该入口，保证新图片生成自己的彩色投影。
        if (mImageBlurShadowBuilder != null) {
            mImageBlurShadowBuilder.onContentChanged();
        }
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        super.setImageBitmap(bitmap);
        if (mImageBlurShadowBuilder != null) {
            mImageBlurShadowBuilder.onContentChanged();
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (mImageBlurShadowBuilder != null) {
            mImageBlurShadowBuilder.onContentChanged();
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        if (mImageBlurShadowBuilder != null) {
            mImageBlurShadowBuilder.onContentChanged();
        }
    }

    @Override
    public void setImageTintList(ColorStateList tint) {
        super.setImageTintList(tint);
        // 直接调用 ImageView API 改 tint 时也要让图片颜色投影重新取样。
        if (mImageBlurShadowBuilder != null) {
            mImageBlurShadowBuilder.onContentChanged();
        }
    }

    @Override
    public void invalidateDrawable(Drawable drawable) {
        super.invalidateDrawable(drawable);
        // AnimatedDrawable 或自定义 Drawable 可在不调用 setImageDrawable 的情况下换帧。
        if (mImageBlurShadowBuilder != null && drawable == getDrawable()) {
            mImageBlurShadowBuilder.onContentChanged();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 用自定义开关屏蔽自身点击，同时保持 enabled=true，便于继续显示启用态 tint/src。
        if (mShapeDrawableBuilder.shouldBlockTouch()) {
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        // 代码主动 performClick 也属于自身点击，需要与触摸入口保持一致。
        if (mShapeDrawableBuilder.shouldBlockTouch()) {
            return false;
        }
        return super.performClick();
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
        // selected/pressed/disabled 状态可能改变当前 tint，投影必须重新取着色后的图片。
        if (mImageBlurShadowBuilder != null) {
            mImageBlurShadowBuilder.onContentChanged();
        }
    }

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
        // 投影先于清晰原图绘制；ShapeImageView 原有背景由 View.draw 在此之前完成。
        mImageBlurShadowBuilder.onDraw(canvas);
        if (!mImageBlurShadowBuilder.isEnabled() ||
                mImageBlurShadowBuilder.getImageScale() >= 1f) {
            super.onDraw(canvas);
            return;
        }

        // 围绕控件中心缩小清晰原图，在自身边界内给彩色投影留出空间；该变换只影响
        // ImageView 的内容绘制，不会缩放已经由 View.draw 绘制完成的 Shape 背景。
        int saveCount = canvas.save();
        float imageScale = mImageBlurShadowBuilder.getImageScale();
        canvas.scale(imageScale, imageScale, getWidth() / 2f, getHeight() / 2f);
        super.onDraw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mImageBlurShadowBuilder.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        mImageBlurShadowBuilder.onDetachedFromWindow();
        super.onDetachedFromWindow();
    }
}
