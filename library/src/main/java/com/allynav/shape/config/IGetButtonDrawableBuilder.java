package com.allynav.shape.config;

import com.allynav.shape.builder.ButtonDrawableBuilder;

/**
 * 对外暴露 {@link ButtonDrawableBuilder} 的能力接口。
 *
 * <p>业务代码通过统一入口取得状态按钮图标构建器，链式设置完成后调用
 * {@code intoButtonDrawable()} 将新配置应用到控件。</p>
 */
public interface IGetButtonDrawableBuilder {

    ButtonDrawableBuilder getButtonDrawableBuilder();
}
