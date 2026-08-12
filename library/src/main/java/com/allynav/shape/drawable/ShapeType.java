package com.allynav.shape.drawable;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/ShapeDrawable
 *    time   : 2021/08/15
 *    desc   : Shape 形状类型常量
 *
 * <p>数值与 attrs.xml 中 shape_type 的 enum 保持一致，修改时必须同步资源和
 * ShapeTypeLimit，否则 XML 与 Java 配置会产生不一致。</p>
 */
public final class ShapeType {

    /** 矩形 */
    public static final int RECTANGLE = 0;

    /** 椭圆形 */
    public static final int OVAL = 1;

    /** 线条 */
    public static final int LINE = 2;

    /** 圆环 */
    public static final int RING = 3;
}
