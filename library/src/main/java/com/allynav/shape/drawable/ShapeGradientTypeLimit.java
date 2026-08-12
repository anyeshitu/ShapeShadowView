package com.allynav.shape.drawable;

import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ShapeDrawable
 *    time   : 2023/07/16
 *    desc   : Shape 渐变类型的编译期赋值限制
 *
 * <p>只允许 ShapeGradientType 中声明的三种渐变值，避免未知整数进入不可预期的绘制
 * 分支。Retention 为 SOURCE，不会增加运行时注解或产物大小。</p>
 */
@IntDef({
    ShapeGradientType.LINEAR_GRADIENT,
    ShapeGradientType.RADIAL_GRADIENT,
    ShapeGradientType.SWEEP_GRADIENT
})
@Retention(RetentionPolicy.SOURCE)
public @interface ShapeGradientTypeLimit {}
