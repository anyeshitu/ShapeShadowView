package com.allynav.shape.config;

/**
 * 文本颜色相关属性索引协议。
 *
 * <p>统一映射默认/状态文字色、文字渐变和文字描边属性，使 TextView、Button、
 * EditText 与 CompoundButton 可以共用 {@code TextColorBuilder}。</p>
 */
public interface ITextColorStyleable {

    int getTextColorStyleable();

    int getTextPressedColorStyleable();

    default int getTextCheckedColorStyleable() {
        return 0;
    }

    int getTextDisabledColorStyleable();

    int getTextFocusedColorStyleable();

    int getTextSelectedColorStyleable();

    int getTextStartColorStyleable();

    int getTextCenterColorStyleable();

    int getTextEndColorStyleable();

    int getTextGradientOrientationStyleable();

    int getTextStrokeColorStyleable();

    int getTextStrokeSizeStyleable();
}
