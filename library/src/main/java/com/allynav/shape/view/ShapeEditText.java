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
import com.allynav.shape.other.CloseKeyboardEditTextDelegate;
import com.allynav.shape.other.TextStateDelegate;
import com.allynav.shape.styleable.ShapeEditTextStyleable;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ShapeView
 *    time   : 2021/07/17
 *    desc   : 支持 Shape 背景、状态文字色、渐变和描边的 EditText
 *
 * <p>输入和 Editable 生命周期继续由 AppCompatEditText 负责。本类只同步背景、文字绘制、
 * 可选状态文本，以及一个默认关闭的输入框增强委托。</p>
 */
public class ShapeEditText extends AppCompatEditText implements
        IGetShapeDrawableBuilder, IGetTextColorBuilder, IGetTextStateDelegate {

    private static final ShapeEditTextStyleable STYLEABLE = new ShapeEditTextStyleable();

    private final ShapeDrawableBuilder mShapeDrawableBuilder;
    private final TextColorBuilder mTextColorBuilder;
    private final TextStateDelegate mTextStateDelegate;
    private final CloseKeyboardEditTextDelegate mCloseKeyboardEditTextDelegate;

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
        mCloseKeyboardEditTextDelegate = new CloseKeyboardEditTextDelegate(
                this, typedArray.getBoolean(
                        R.styleable.ShapeEditText_shape_closeKeyboardEnable, false));
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
    public void onEditorAction(int actionCode) {
        // 先执行父类默认处理和外部监听器，再由委托保证完成动作一定收起键盘。
        super.onEditorAction(actionCode);
        mCloseKeyboardEditTextDelegate.onEditorAction(actionCode);
    }

    @Override
    public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        // 父类先处理输入内容；委托随后仅拦截已确认的 Enter 抬起事件。
        boolean handled = super.onKeyUp(keyCode, event);
        return mCloseKeyboardEditTextDelegate.onKeyUp(keyCode, event) || handled;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
            android.graphics.Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        mCloseKeyboardEditTextDelegate.onFocusChanged(
                focused, direction, previouslyFocusedRect);
    }

    @Override
    protected void onDetachedFromWindow() {
        // 控件移除时取消委托中等待执行的全选任务，避免页面销毁后仍回调旧控件。
        mCloseKeyboardEditTextDelegate.onDetachedFromWindow();
        super.onDetachedFromWindow();
    }

    @Override
    public TextStateDelegate getTextStateDelegate() {
        return mTextStateDelegate;
    }

    /** 返回输入框增强委托，可在 Java 中动态开关或主动收起键盘。 */
    public CloseKeyboardEditTextDelegate getCloseKeyboardEditTextDelegate() {
        return mCloseKeyboardEditTextDelegate;
    }

    /** 返回当前是否开启完成收键盘、聚焦全选和失焦隐藏光标能力。 */
    public boolean isCloseKeyboardEnabled() {
        return mCloseKeyboardEditTextDelegate.isEnabled();
    }

    /** 动态开启或关闭输入框增强能力。 */
    public void setCloseKeyboardEnabled(boolean enabled) {
        mCloseKeyboardEditTextDelegate.setEnabled(enabled);
    }

    /** 主动转移焦点并收起软键盘。 */
    public void closeKeyboard() {
        mCloseKeyboardEditTextDelegate.closeKeyboard();
    }
}
