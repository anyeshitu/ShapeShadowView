package com.allynav.shape.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatEditText;
import com.allynav.shape.R;
import com.allynav.shape.builder.ShapeDrawableBuilder;
import com.allynav.shape.builder.TextColorBuilder;
import com.allynav.shape.config.IGetShapeDrawableBuilder;
import com.allynav.shape.config.IGetTextColorBuilder;
import com.allynav.shape.config.IGetTextStateDelegate;
import com.allynav.shape.other.TextStateDelegate;
import com.allynav.shape.styleable.ShapeEditTextStyleable;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ShapeView
 *    time   : 2021/07/17
 *    desc   : 支持 Shape 背景、状态文字色、渐变和描边的 EditText
 *
 * <p>输入、选择、光标和 IME 行为继续由 AppCompatEditText 负责。本类只同步背景、
 * 文字绘制和可选状态文本，不改变 Editable 的生命周期。</p>
 */
public class ShapeEditText extends AppCompatEditText implements
        IGetShapeDrawableBuilder, IGetTextColorBuilder, IGetTextStateDelegate {

    private static final ShapeEditTextStyleable STYLEABLE = new ShapeEditTextStyleable();

    private final ShapeDrawableBuilder mShapeDrawableBuilder;
    private final TextColorBuilder mTextColorBuilder;
    private final TextStateDelegate mTextStateDelegate;

    public ShapeEditText(Context context) {
        this(context, null);
    }

    public ShapeEditText(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.editTextStyle);
    }

    public ShapeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 所有 Builder 完成属性复制后立即回收 TypedArray，再应用初始外观。
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeEditText);
        mShapeDrawableBuilder = new ShapeDrawableBuilder(this, attrs, typedArray, STYLEABLE);
        mTextColorBuilder = new TextColorBuilder(this, typedArray, STYLEABLE);
        mTextStateDelegate = new TextStateDelegate(this, attrs);
        typedArray.recycle();

        mShapeDrawableBuilder.intoBackground();
        mTextColorBuilder.intoTextColor();
        mTextStateDelegate.refresh();
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        if (mTextColorBuilder == null) {
            return;
        }
        mTextColorBuilder.setTextColor(color);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (mTextColorBuilder != null && mTextColorBuilder.isTextStrokeColorEnable()) {
            super.setText(mTextColorBuilder.buildStrokeFontSpannable(text), BufferType.SPANNABLE);
        } else {
            super.setText(text, type);
        }
        if (mTextStateDelegate != null) {
            mTextStateDelegate.onTextSet(text);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mTextStateDelegate != null) {
            mTextStateDelegate.refresh();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 先更新渐变 Shader，再交给 EditText 绘制文字、选择区和光标。
        mTextColorBuilder.onDraw(this, canvas, getPaint());
        super.onDraw(canvas);
    }

    @Override
    public ShapeDrawableBuilder getShapeDrawableBuilder() {
        return mShapeDrawableBuilder;
    }

    @Override
    public TextColorBuilder getTextColorBuilder() {
        return mTextColorBuilder;
    }

    @Override
    public TextStateDelegate getTextStateDelegate() {
        return mTextStateDelegate;
    }
}
