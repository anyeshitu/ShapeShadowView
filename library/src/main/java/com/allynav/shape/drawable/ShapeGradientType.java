package com.allynav.shape.drawable;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ShapeDrawable
 *    time   : 2021/08/15
 *    desc   : Shape 渐变类型常量
 *
 * <p>数值与 attrs.xml 的 shape_solidGradientType 保持一致，供填充和描边渐变选择
 * 线性、径向或扫描算法。</p>
 */
public final class ShapeGradientType {

    /** 线性渐变 */
    public static final int LINEAR_GRADIENT = 0;

    /** 径向渐变 */
    public static final int RADIAL_GRADIENT = 1;

    /** 扫描渐变 */
    public static final int SWEEP_GRADIENT  = 2;
}
