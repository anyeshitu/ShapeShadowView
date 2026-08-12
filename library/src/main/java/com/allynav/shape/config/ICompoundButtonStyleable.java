package com.allynav.shape.config;

/**
 * CompoundButton 状态图标属性索引协议。
 *
 * <p>CheckBox 和 RadioButton 的 {@code R.styleable} 数组不同，具体适配类通过本接口
 * 把默认、按下、选中、禁用、聚焦和 selected 属性映射成统一语义，供
 * {@code ButtonDrawableBuilder} 复用。</p>
 */
public interface ICompoundButtonStyleable {

    int getButtonDrawableStyleable();

    int getButtonPressedDrawableStyleable();

    int getButtonCheckedDrawableStyleable();

    int getButtonDisabledDrawableStyleable();

    int getButtonFocusedDrawableStyleable();

    int getButtonSelectedDrawableStyleable();
}
