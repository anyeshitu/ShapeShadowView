package com.allynav.shape.config;

/**
 * 文字 Span 所需的最小 TextView 布局信息接口。
 *
 * <p>Span 不直接依赖完整 TextView，只读取布局方向、gravity 和左右 padding，降低
 * 绘制逻辑与具体控件的耦合，并保证 RTL 环境中的文字对齐计算正确。</p>
 */
public interface ITextViewAttribute {

    /**
     * 获取当前布局方向
     */
    int getLayoutDirection();

    /**
     * 获取当前文本重心
     */
    int getTextGravity();

    /**
     * 获取 TextView 左内间距
     */
    int getPaddingLeft();

    /**
     * 获取 TextView 右内间距
     */
    int getPaddingRight();
}
