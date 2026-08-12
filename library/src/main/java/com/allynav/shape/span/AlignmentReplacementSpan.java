package com.allynav.shape.span;

import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.style.AlignmentSpan;
import android.text.style.ReplacementSpan;
import android.view.Gravity;
import android.view.View;
import androidx.annotation.NonNull;
import com.allynav.shape.config.ITextViewAttribute;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ShapeView
 *    time   : 2024/09/15
 *    desc   : 支持 TextView 重心和 RTL 语义的 ReplacementSpan 基类
 *
 * <p>ReplacementSpan 不会自动继承 TextView 的 gravity。本类通过只读属性接口把
 * LEFT/RIGHT、START/END 和 CENTER 转换为 Layout.Alignment，供渐变、描边和组合
 * Span 共享一致的水平对齐行为。垂直基线仍由系统字体度量决定。</p>
 */
public abstract class AlignmentReplacementSpan extends ReplacementSpan implements AlignmentSpan {

    @NonNull
    /** 实时读取目标 TextView 布局方向、gravity 和 padding 的适配器。 */
    private final ITextViewAttribute mTextAttribute;

    public AlignmentReplacementSpan(@NonNull ITextViewAttribute textViewAttribute) {
        mTextAttribute = textViewAttribute;
    }

    @NonNull
    public ITextViewAttribute getTextAttribute() {
        return mTextAttribute;
    }

    @Override
    public Alignment getAlignment() {
        // 获取 TextView 的文本重心
        int gravity = mTextAttribute.getTextGravity();

        // 根据 gravity 设置 AlignmentSpan
        Layout.Alignment alignment;

        // 获取当前布局方向（LTR 或 RTL）
        boolean isRtl = (mTextAttribute.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);

        // 判断的顺序必须为：left 和 right，start 和 end，center 和 center_horizontal
        if (hasFlag(gravity, Gravity.LEFT)) {
            // Gravity.LEFT 始终左对齐，需要根据布局方向判断
            alignment = isRtl ? Layout.Alignment.ALIGN_OPPOSITE : Layout.Alignment.ALIGN_NORMAL;
        } else if (hasFlag(gravity, Gravity.RIGHT)) {
            // Gravity.RIGHT 始终右对齐，需要根据布局方向判断
            alignment = isRtl ? Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_OPPOSITE;
        } else if (hasFlag(gravity, Gravity.START)) {
            // Gravity.START 等于 ALIGN_NORMAL，自动根据布局方向调整
            alignment = Layout.Alignment.ALIGN_NORMAL;
        } else if (hasFlag(gravity, Gravity.END)) {
            // Gravity.END 等于 ALIGN_OPPOSITE，自动根据布局方向调整
            alignment = Layout.Alignment.ALIGN_OPPOSITE;
        } else if (hasFlag(gravity, Gravity.CENTER) ||
            hasFlag(gravity, Gravity.CENTER_HORIZONTAL)) {
            // 居中对齐，这里的对齐只能水平居中，而不支持垂直居中
            alignment = Layout.Alignment.ALIGN_CENTER;
        } else {
            // 默认左对齐
            alignment = Layout.Alignment.ALIGN_NORMAL;
        }

        // 坏消息：这种方式只支持水平方向文本重心设置，不支持垂直方向文本重心设置
        // 好消息：在设置自定义 Span 的情况下，TextView 设置文本重心，目前只发现了水平重心设置失效，垂直重心设置仍然有效
        return alignment;
    }

    /**
     * 检查整数是否包含某个标志
     */
    protected boolean hasFlag(int i, int flag) {
        return (i & flag) == flag;
    }
}
