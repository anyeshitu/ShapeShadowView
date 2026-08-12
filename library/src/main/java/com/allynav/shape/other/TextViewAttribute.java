package com.allynav.shape.other;

import android.widget.TextView;
import com.allynav.shape.config.ITextViewAttribute;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ShapeView
 *    time   : 2024/09/15
 *    desc   : TextView 布局属性只读适配器
 *
 * <p>向文字 Span 暴露布局方向、gravity 和左右 padding，避免 Span 直接依赖具体
 * TextView 子类。适配器不缓存属性值，始终返回控件当前状态。</p>
 */
public class TextViewAttribute implements ITextViewAttribute {

    /** 被读取的目标 TextView。 */
    private final TextView mTextView;

    public TextViewAttribute(TextView textView) {
        mTextView = textView;
    }

    @Override
    public int getLayoutDirection() {
        return mTextView.getLayoutDirection();
    }

    @Override
    public int getTextGravity() {
        return mTextView.getGravity();
    }

    @Override
    public int getPaddingLeft() {
        return mTextView.getPaddingLeft();
    }

    @Override
    public int getPaddingRight() {
        return mTextView.getPaddingRight();
    }
}
