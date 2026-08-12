package com.allynav.shape.config;

import com.allynav.shape.builder.ShapeDrawableBuilder;

/**
 * 对外暴露 {@link ShapeDrawableBuilder} 的能力接口。
 *
 * <p>所有 Shape View 和 Shape Layout 都实现本接口，使业务层无需区分具体控件类型
 * 即可动态修改背景，并在链式设置后调用 {@code intoBackground()} 应用。</p>
 */
public interface IGetShapeDrawableBuilder {

    ShapeDrawableBuilder getShapeDrawableBuilder();
}
