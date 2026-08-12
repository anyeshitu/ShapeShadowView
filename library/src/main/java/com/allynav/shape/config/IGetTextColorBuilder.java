package com.allynav.shape.config;

import com.allynav.shape.builder.TextColorBuilder;

/**
 * 对外暴露 {@link TextColorBuilder} 的能力接口。
 *
 * <p>文本类 Shape 控件通过本接口提供统一的状态色、渐变和文字描边动态配置入口。</p>
 */
public interface IGetTextColorBuilder {

    TextColorBuilder getTextColorBuilder();
}
