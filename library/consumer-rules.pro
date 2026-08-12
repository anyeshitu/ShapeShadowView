# 公开 View 通常由 XML 布局引用，Android 资源收缩流程会保留对应构造方法。
# 当前实现不使用反射、JNI 或序列化，因此无需额外 consumer keep 规则。
