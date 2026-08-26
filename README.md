# ShapeShadowView

`ShapeShadowView` 是一个 Android UI 控件库。它以
[ShapeView](https://github.com/getActivity/ShapeView) 的控件体系、`shape_*` XML 属性和
Builder 调用方式为基础，吸收了
[ShadowLayout](https://github.com/lihangleo2/ShadowLayout) 的阴影空间计算与位图缓存思路，
让所有 Shape 控件都能直接使用真实、可配置的模糊阴影。

使用方只需要这个库，不需要同时依赖原版 ShapeView、ShapeDrawable 或 ShadowLayout，
也不需要使用 `hl_*` 属性。

## 功能概览

| 能力 | 支持内容 |
| --- | --- |
| 形状 | 矩形、椭圆、线、圆环、自定义宽高 |
| 圆角 | 统一圆角、四角独立圆角、Start/End 圆角、顶部组合圆角、底部组合圆角 |
| 填充 | 纯色、按下/选中/禁用/聚焦/选择状态色、线性/径向/扫描渐变 |
| 边框 | 普通边框、状态边框色、渐变边框、虚线边框、虚线线条 |
| 阴影 | 真实模糊阴影、扩散、偏移、对称空间、整体隐藏、按边隐藏、位图缓存缩放 |
| 交互 | Android `RippleDrawable` 水波纹，自动匹配圆角和形状 |
| 背景 | Color、Bitmap、Vector、XML Drawable，并支持各状态背景和形状裁剪 |
| 文本 | 状态颜色、渐变、描边、各状态文本内容、复合图片状态 tint、固定高度自适应、自动字号、跑马灯 |
| 输入框 | ShapeEditText 可选完成收键盘、聚焦全选和失焦隐藏光标 |
| 复选控件 | CheckBox、RadioButton 的各状态按钮图标 |
| 图片状态 | ShapeImageView 默认、按下、选中、禁用、聚焦和选择状态 tint 与 src |

## 环境要求

- Android `minSdk 21`
- 建议 `compileSdk 35` 或更高
- Java 8
- AndroidX

应用模块可使用以下配置：

```groovy
android {
    compileSdk 35

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
```

## JitPack 依赖

在根工程的 `settings.gradle.kts` 中加入 JitPack 仓库：

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}
```

Groovy DSL：

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

然后在应用模块中添加依赖：

```kotlin
dependencies {
    implementation("com.github.anyeshitu:ShapeShadowView:1.1.11")
}
```

Groovy DSL：

```groovy
dependencies {
    implementation 'com.github.anyeshitu:ShapeShadowView:1.1.11'
}
```

### 依赖版本兼容

库中的 Shape 控件继承 AndroidX AppCompat 控件，发布包会声明兼容基线
`androidx.appcompat:appcompat:1.6.1`。Gradle 会优先采用应用工程或其他依赖显式声明的版本；
如果宿主工程必须固定 AppCompat 版本，也可以在依赖声明中排除库的传递依赖：

```groovy
implementation('com.github.anyeshitu:ShapeShadowView:1.1.11') {
    // 由宿主工程统一提供 AppCompat，避免三方库传递版本改变现有主题行为。
    exclude group: 'androidx.appcompat', module: 'appcompat'
}
```

ShapeShadowView 不会替宿主工程自动切换主题。布局中存在普通 `Button`、
`MaterialButton` 或使用 AppCompat/Material 组件的弹窗时，承载它们的 Activity、Fragment
和 Dialog Context 应使用 `Theme.MaterialComponents` 或其子主题；仅使用平台
`Theme.Material` 的启动窗口不应直接承载业务布局。

## 本地模块接入

把本项目的 `library` 目录复制到应用工程根目录，并重命名为 `shape-shadow-view`：

```text
your-android-project/
├── app/
├── shape-shadow-view/
│   └── build.gradle.kts
└── settings.gradle.kts
```

Kotlin DSL，在根工程的 `settings.gradle.kts` 中加入：

```kotlin
include(":shape-shadow-view")
```

Groovy DSL，在根工程的 `settings.gradle` 中加入：

```groovy
include ':shape-shadow-view'
```

然后在应用模块的 `build.gradle.kts` 中加入：

```kotlin
dependencies {
    implementation(project(":shape-shadow-view"))
}
```

Groovy DSL：

```groovy
dependencies {
    implementation project(':shape-shadow-view')
}
```

如果不移动当前目录，也可以把模块映射到嵌套的 `library` 目录：

```kotlin
include(":shape-shadow-view")
project(":shape-shadow-view").projectDir = file("shape-shadow-view/library")
```

本库已经包含兼容的 ShapeDrawable 实现。请移除原来的依赖，避免同名资源和类冲突：

```groovy
// 删除这两项
implementation 'com.github.getActivity:ShapeView:11.0'
implementation 'com.github.getActivity:ShapeDrawable:5.0'
```

同时不需要添加 ShadowLayout 依赖。

## 支持的控件

### View 子类

| 控件 | XML 类名 |
| --- | --- |
| ShapeView | `com.allynav.shape.view.ShapeView` |
| ShapeTextView | `com.allynav.shape.view.ShapeTextView` |
| ShapeButton | `com.allynav.shape.view.ShapeButton` |
| ShapeImageView | `com.allynav.shape.view.ShapeImageView` |
| ShapeRadioButton | `com.allynav.shape.view.ShapeRadioButton` |
| ShapeCheckBox | `com.allynav.shape.view.ShapeCheckBox` |
| ShapeEditText | `com.allynav.shape.view.ShapeEditText` |

### ViewGroup 子类

| 控件 | XML 类名 |
| --- | --- |
| ShapeLinearLayout | `com.allynav.shape.layout.ShapeLinearLayout` |
| ShapeFrameLayout | `com.allynav.shape.layout.ShapeFrameLayout` |
| ShapeRelativeLayout | `com.allynav.shape.layout.ShapeRelativeLayout` |
| ShapeConstraintLayout | `com.allynav.shape.layout.ShapeConstraintLayout` |
| ShapeRecyclerView | `com.allynav.shape.layout.ShapeRecyclerView` |
| ShapeRadioGroup | `com.allynav.shape.layout.ShapeRadioGroup` |

所有控件都支持形状、背景状态、边框、圆角、阴影和 Ripple。文本相关属性只对文本控件有效，
按钮图标属性只对 `ShapeCheckBox` 和 `ShapeRadioButton` 有效。

## 基础用法

先在布局根节点声明 `app` 命名空间：

```xml
xmlns:app="http://schemas.android.com/apk/res-auto"
```

一个同时使用组合圆角、Ripple 和真实阴影的按钮：

```xml
<com.allynav.shape.view.ShapeButton
    android:id="@+id/btn_main_test"
    android:layout_width="180dp"
    android:layout_height="72dp"
    android:gravity="center"
    android:text="确认"
    android:textSize="16sp"
    app:shape_solidColor="#FFFFFF"
    app:shape_solidPressedColor="#F2F5F8"
    app:shape_radiusInTop="18dp"
    app:shape_radiusInBottom="10dp"
    app:shape_rippleEnable="true"
    app:shape_rippleColor="#1F000000"
    app:shape_shadowColor="#52000000"
    app:shape_shadowSize="14dp"
    app:shape_shadowOffsetY="5dp"
    app:shape_shadowSpread="1dp"
    app:shape_textColor="#202124"
    app:shape_textPressedColor="#000000" />
```

## 布局属性大全

除特别注明外，下列属性适用于全部 Shape 控件。

### 形状和尺寸

| 属性 | 格式/可选值 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `shape_type` | `rectangle`、`oval`、`line`、`ring` | `rectangle` | Shape 类型 |
| `shape_width` | dimension | 未指定 | Drawable 的内部宽度，不等同于 `layout_width` |
| `shape_height` | dimension | 未指定 | Drawable 的内部高度，不等同于 `layout_height` |

### 圆角

| 属性 | 格式 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `shape_radius` | dimension | `0dp` | 四个角统一圆角 |
| `shape_radiusInTop` | dimension | 继承 `shape_radius` | 同时设置左上和右上圆角，本库扩展 |
| `shape_radiusInBottom` | dimension | 继承 `shape_radius` | 同时设置左下和右下圆角，本库扩展 |
| `shape_radiusInTopLeft` | dimension | 继承上级圆角 | 左上圆角 |
| `shape_radiusInTopStart` | dimension | 继承上级圆角 | 顶部 Start 圆角，自动适配 RTL |
| `shape_radiusInTopRight` | dimension | 继承上级圆角 | 右上圆角 |
| `shape_radiusInTopEnd` | dimension | 继承上级圆角 | 顶部 End 圆角，自动适配 RTL |
| `shape_radiusInBottomLeft` | dimension | 继承上级圆角 | 左下圆角 |
| `shape_radiusInBottomStart` | dimension | 继承上级圆角 | 底部 Start 圆角，自动适配 RTL |
| `shape_radiusInBottomRight` | dimension | 继承上级圆角 | 右下圆角 |
| `shape_radiusInBottomEnd` | dimension | 继承上级圆角 | 底部 End 圆角，自动适配 RTL |

圆角优先级为：物理方向单角 `Left/Right` > 相对方向单角 `Start/End` >
组合圆角 `Top/Bottom` > `shape_radius`。

### 填充颜色和状态

| 属性 | 格式 | 说明 |
| --- | --- | --- |
| `shape_solidColor` | color | 默认状态填充色 |
| `shape_solidPressedColor` | color | 按下状态填充色 |
| `shape_solidCheckedColor` | color | 选中状态填充色，主要用于 CheckBox、RadioButton |
| `shape_solidDisabledColor` | color | `android:enabled="false"` 时的填充色 |
| `shape_solidFocusedColor` | color | 获取焦点时的填充色 |
| `shape_solidSelectedColor` | color | `android:selected="true"` 时的填充色 |

### 填充渐变

只有同时设置开始色和结束色才会启用填充渐变。

| 属性 | 格式/可选值 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `shape_solidGradientStartColor` | color | 无 | 渐变开始色 |
| `shape_solidGradientCenterColor` | color | 无 | 可选的渐变中间色 |
| `shape_solidGradientEndColor` | color | 无 | 渐变结束色 |
| `shape_solidGradientType` | `linear`、`radial`、`sweep` | `linear` | 线性、径向或扫描渐变 |
| `shape_solidGradientOrientation` | 见下表 | `startToEnd` | 线性渐变方向 |
| `shape_solidGradientCenterX` | float/fraction | `0.5` | 径向/扫描渐变中心 X |
| `shape_solidGradientCenterY` | float/fraction | `0.5` | 径向/扫描渐变中心 Y |
| `shape_solidGradientRadius` | float/fraction/dimension | 当前圆角值 | 径向渐变半径 |

填充和边框渐变方向都支持：

| 方向值 | 含义 |
| --- | --- |
| `leftToRight` / `startToEnd` | 从左到右 / 从 Start 到 End |
| `rightToLeft` / `endToStart` | 从右到左 / 从 End 到 Start |
| `bottomToTop` | 从下到上 |
| `topToBottom` | 从上到下 |
| `topLeftToBottomRight` / `topStartToBottomEnd` | 左上到右下 / Start 上到 End 下 |
| `bottomLeftToTopRight` / `bottomStartToTopEnd` | 左下到右上 / Start 下到 End 上 |
| `topRightToBottomLeft` / `topEndToBottomStart` | 右上到左下 / End 上到 Start 下 |
| `bottomRightToTopLeft` / `bottomEndToTopStart` | 右下到左上 / End 下到 Start 上 |

### 边框和虚线

| 属性 | 格式 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `shape_strokeColor` | color | transparent | 默认状态边框色 |
| `shape_strokePressedColor` | color | 无 | 按下状态边框色 |
| `shape_strokeCheckedColor` | color | 无 | 选中状态边框色，主要用于 CheckBox、RadioButton |
| `shape_strokeDisabledColor` | color | 无 | 禁用状态边框色 |
| `shape_strokeFocusedColor` | color | 无 | 聚焦状态边框色 |
| `shape_strokeSelectedColor` | color | 无 | 选择状态边框色 |
| `shape_strokeSize` | dimension | `0dp` | 边框宽度 |
| `shape_strokeDashSize` | dimension | `0dp` | 每段虚线长度；大于 0 时启用虚线 |
| `shape_strokeDashGap` | dimension | `0dp` | 虚线间隔 |
| `shape_strokeGradientStartColor` | color | 无 | 渐变边框开始色 |
| `shape_strokeGradientCenterColor` | color | 无 | 可选的渐变边框中间色 |
| `shape_strokeGradientEndColor` | color | 无 | 渐变边框结束色 |
| `shape_strokeGradientOrientation` | 渐变方向枚举 | `startToEnd` | 渐变边框方向 |

虚线边框示例：

```xml
app:shape_strokeColor="#607D8B"
app:shape_strokeSize="1dp"
app:shape_strokeDashSize="6dp"
app:shape_strokeDashGap="4dp"
```

虚线线条示例：

```xml
<com.allynav.shape.view.ShapeView
    android:layout_width="match_parent"
    android:layout_height="1dp"
    app:shape_type="line"
    app:shape_lineGravity="center"
    app:shape_strokeColor="#607D8B"
    app:shape_strokeSize="1dp"
    app:shape_strokeDashSize="6dp"
    app:shape_strokeDashGap="4dp" />
```

因此，虚线边框和 ShadowLayout 风格的虚线分隔线都支持。

### 真实阴影

| 属性 | 格式 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `shape_shadowSize` | dimension | `0dp` | 模糊半径；大于 0 才启用阴影 |
| `shape_shadowColor` | color | `#40000000` | 阴影颜色和透明度 |
| `shape_shadowOffsetX` | dimension | `0dp` | 水平偏移，正值向右 |
| `shape_shadowOffsetY` | dimension | `0dp` | 垂直偏移，正值向下 |
| `shape_shadowSpread` | dimension | `0dp` | 模糊前向外扩张的距离 |
| `shape_shadowSymmetry` | boolean | `false` | 两侧预留对称阴影空间 |
| `shape_shadowHidden` | boolean | `false` | 临时隐藏整个阴影，保留其他配置 |
| `shape_shadowHiddenLeft` | boolean | `false` | 隐藏左侧阴影并取消左侧阴影占位 |
| `shape_shadowHiddenTop` | boolean | `false` | 隐藏顶部阴影并取消顶部阴影占位 |
| `shape_shadowHiddenRight` | boolean | `false` | 隐藏右侧阴影并取消右侧阴影占位 |
| `shape_shadowHiddenBottom` | boolean | `false` | 隐藏底部阴影并取消底部阴影占位 |
| `shape_shadowBitmapScale` | float | `0.5` | 阴影缓存精度，运行时限制为 `0.25..1.0` |

完整阴影示例：

```xml
<com.allynav.shape.view.ShapeTextView
    android:layout_width="200dp"
    android:layout_height="88dp"
    android:gravity="center"
    android:text="真实阴影"
    app:shape_solidColor="#FFFFFF"
    app:shape_radius="14dp"
    app:shape_shadowColor="#66000000"
    app:shape_shadowSize="16dp"
    app:shape_shadowOffsetX="0dp"
    app:shape_shadowOffsetY="6dp"
    app:shape_shadowSpread="1dp"
    app:shape_shadowSymmetry="false"
    app:shape_shadowBitmapScale="0.5" />
```

阴影由缓存位图和模糊遮罩绘制，不依赖系统 `elevation`，因此颜色、偏移、扩散和圆角可控。
阴影绘制在 View 自己的边界内，库会把阴影占位叠加到原始 padding 上。固定宽高包含形状和阴影空间，
`ShapeLayout` 的可用子内容区域也会相应减少。

### Ripple 水波纹

| 属性 | 格式 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `shape_rippleEnable` | boolean | `false` | 启用 Android 原生 Ripple |
| `shape_rippleColor` | color | `#24000000` | 水波纹颜色 |

```xml
app:shape_rippleEnable="true"
app:shape_rippleColor="#26007AFF"
```

Ripple 会包裹最终的 Shape/状态背景，并使用相同形状作为 Mask，所以圆角处不会出现矩形水波纹。

### 自定义 Drawable 和状态背景

这些属性可以引用 Color、Bitmap、Vector 或 XML Drawable。背景会按当前形状和圆角裁剪。

| 属性 | 格式 | 说明 |
| --- | --- | --- |
| `shape_background` | reference/color | 默认背景 |
| `shape_pressedBackground` | reference/color | 按下状态背景 |
| `shape_checkedBackground` | reference/color | 选中状态背景 |
| `shape_disabledBackground` | reference/color | 禁用状态背景 |
| `shape_focusedBackground` | reference/color | 聚焦状态背景 |
| `shape_selectedBackground` | reference/color | 选择状态背景 |

```xml
app:shape_background="@drawable/button_normal"
app:shape_pressedBackground="@drawable/button_pressed"
app:shape_disabledBackground="#DADCE0"
app:shape_radius="12dp"
```

当 Drawable 状态和填充/边框状态同时配置时，该状态的 Drawable 优先。

### 线和圆环

| 属性 | 格式/可选值 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `shape_lineGravity` | `top`、`bottom`、`left`、`right`、`start`、`end`、`center`，可组合 | `center` | 仅 `shape_type="line"` 生效 |
| `shape_ringInnerRadiusSize` | dimension | 自动 | 圆环内半径，优先于比例 |
| `shape_ringInnerRadiusRatio` | float | `3.0` | 圆环尺寸 / 比例 = 内半径 |
| `shape_ringThicknessSize` | dimension | 自动 | 圆环厚度，优先于比例 |
| `shape_ringThicknessRatio` | float | `9.0` | 圆环尺寸 / 比例 = 厚度 |

### 文本颜色、渐变和描边

适用于 `ShapeTextView`、`ShapeButton`、`ShapeEditText`、`ShapeCheckBox` 和
`ShapeRadioButton`。

| 属性 | 格式/可选值 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `shape_textColor` | color | 当前 `android:textColor` | 默认文本颜色 |
| `shape_textPressedColor` | color | 无 | 按下状态文本颜色 |
| `shape_textCheckedColor` | color | 无 | 选中状态文本颜色，主要用于 CheckBox、RadioButton |
| `shape_textDisabledColor` | color | 无 | 禁用状态文本颜色 |
| `shape_textFocusedColor` | color | 无 | 聚焦状态文本颜色 |
| `shape_textSelectedColor` | color | 无 | 选择状态文本颜色 |
| `shape_textStartColor` | color | 无 | 文本渐变开始色 |
| `shape_textCenterColor` | color | 无 | 可选的文本渐变中间色 |
| `shape_textEndColor` | color | 无 | 文本渐变结束色 |
| `shape_textGradientOrientation` | `horizontal`、`vertical` | `horizontal` | 文本渐变方向 |
| `shape_textStrokeColor` | color | transparent | 文本描边颜色 |
| `shape_textStrokeSize` | dimension | `0dp` | 文本描边宽度 |

文本渐变同样要求同时设置开始色和结束色。

### 状态文本内容

适用于所有文本控件。没有匹配状态文本时，显示 `android:text` 或最后一次 `setText` 的内容。

| 属性 | 格式 | 说明 |
| --- | --- | --- |
| `shape_textPressed` | string | 按下时显示的文本 |
| `shape_textChecked` | string | 选中时显示的文本 |
| `shape_textDisabled` | string | 禁用时显示的文本 |
| `shape_textFocused` | string | 聚焦时显示的文本 |
| `shape_textSelected` | string | 选择时显示的文本 |

```xml
android:text="提交"
app:shape_textPressed="松开提交"
app:shape_textDisabled="暂不可用"
```

禁用状态优先匹配。请使用 `android:enabled="false"` 进入禁用状态，而不是用
`android:clickable="false"` 模拟禁用。

### ShapeEditText 输入框增强

`ShapeEditText` 默认保持 `AppCompatEditText` 的原生输入行为。配置
`shape_closeKeyboardEnable="true"` 后，输入法点击完成/前往按钮或硬件回车会自动收起键盘，
输入框获得焦点时会全选文本，失去焦点时隐藏光标并清除文本选择。

```xml
<com.allynav.shape.view.ShapeEditText
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:hint="请输入名称"
    app:shape_closeKeyboardEnable="true"
    app:shape_radius="4dp"
    app:shape_strokeColor="#B0BEC5"
    app:shape_strokeSize="1dp" />
```

该功能不依赖 `setOnEditorActionListener` 或 `setOnFocusChangeListener`，业务代码设置这些
监听器后，组件自己的行为仍然有效。Java 中也可以动态控制：

```java
shapeEditText.setCloseKeyboardEnabled(true);
// 需要时主动收起键盘并转移焦点。
shapeEditText.closeKeyboard();
```

也可以通过 `getCloseKeyboardEditTextDelegate()` 获取委托。关闭功能后会恢复输入框创建时的
IME 选项和光标可见性。

### ShapeTextView 固定高度自适应

该能力的行为参考 [AdaptiveTextView](https://github.com/AndrewSuan/AdaptiveTextView)，仅适用于
`ShapeTextView`。它不会缩小字号，而是在固定可用高度不足时压缩行间距、减少 `maxLines`，
或者先压缩行间距再减少行数。默认关闭，不会影响现有布局。

| 属性 | 格式/可选值 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `shape_adaptiveTextEnable` | boolean | `false` | 是否启用固定高度文本自适应 |
| `shape_adaptiveTextMode` | `reduceLines`、`reduceLineSpacing`、`reduceLineSpacingThenLines` | `reduceLines` | 高度不足时采用的策略 |
| `shape_adaptiveMinLines` | integer | `1` | 减少行数时允许保留的最小行数 |
| `shape_adaptiveMinLineSpacingExtra` | dimension | 自动 | `setLineSpacing` 的 add 参数下限；未配置时根据字体 descent 自动计算 |

```xml
<com.allynav.shape.view.ShapeTextView
    android:layout_width="match_parent"
    android:layout_height="80dp"
    android:ellipsize="end"
    android:maxLines="3"
    android:text="需要在固定高度内完整排布的多行文本"
    android:textSize="23sp"
    app:shape_adaptiveTextEnable="true"
    app:shape_adaptiveTextMode="reduceLineSpacingThenLines"
    app:shape_adaptiveMinLines="1" />
```

文本、字号、内边距、控件尺寸、`maxLines` 或行间距发生变化后，控件会先恢复 XML/Java
配置的基准值再重新计算，不会持续累减 `maxLines`。该功能要求 `layout_height` 为固定尺寸且
配置有限的 `android:maxLines`；`wrap_content`、`match_parent` 或未限制行数时不会执行调整。

### ShapeTextView 自动字号

该能力参考 XUI 的 `AutoFitTextView`，通过二分查找在有限 `android:maxLines` 内找到合适的字号，
不会改动 Shape 背景、阴影或文字颜色。默认关闭；需要时可通过 XML 开启：

| 属性 | 格式 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `shape_autoFitTextEnable` | boolean | `false` | 是否开启自动字号 |
| `shape_autoFitMinTextSize` | dimension | `8sp` | 允许使用的最小字号 |
| `shape_autoFitMaxTextSize` | dimension | `android:textSize` | 允许使用的最大字号 |
| `shape_autoFitPrecision` | float | `0.5` | 二分查找精度，越小越精确但计算更多 |
| `shape_textBaselineEnabled` | boolean | 自动 | 是否向父布局提供文字基线；AutoFit 开启时默认关闭，普通文本默认开启 |

```xml
<com.allynav.shape.view.ShapeTextView
    android:layout_width="180dp"
    android:layout_height="56dp"
    android:maxLines="2"
    android:text="需要在有限宽度和行数内自动缩小的文字"
    android:textSize="20sp"
    app:shape_autoFitTextEnable="true"
    app:shape_autoFitMinTextSize="12sp"
    app:shape_autoFitPrecision="0.5" />
```

该能力要求有限的 `android:maxLines`；未限制行数时不会调整。它可以和固定高度自适应同时开启，
执行顺序是先自动缩小字号，仍然放不下时再按 `shape_adaptiveTextMode` 处理行间距或行数。

开启 AutoFit 后，`ShapeTextView` 默认不会向横向 `LinearLayout` 提供文字基线，因此动态
`GONE/VISIBLE` 或字号变化不会带动固定高度按钮上下移动，父容器无需重复配置
`android:baselineAligned="false"`。表单等确实需要文字基线对齐的场景，可在对应控件上设置
`app:shape_textBaselineEnabled="true"`，或调用 `setTextBaselineEnabled(true)`。

### ShapeTextView 跑马灯

该能力使用 Android 原生 `MARQUEE`，由组件根据窗口、父容器和自身可见性自动维护
`selected`。控件从 `GONE` 恢复为 `VISIBLE` 后会在最终尺寸稳定时自动重启，不需要业务代码
手动调用 `setSelected(true)`。默认只要求控件与窗口存在可见交集；需要完整进入屏幕后才滚动时，
可显式开启 `shape_marqueeRequireFullyVisible`。

DataBinding、LiveData 或业务代码动态替换文本时，组件会先停止旧文本的 Marquee，并在新文本
完成测量和布局、进入绘制前重新启动。因此文本可以在短名称和长名称之间反复切换，调用方
不需要额外切换 `selected`，长文本也不会停留在静态省略号状态。

跑马灯和 `shape_autoFitTextEnable` 可以同时开启。自动字号会在测量阶段完成，控件首帧和后续帧
使用同一字号、基线与高度，避免动态显示后文字或控件发生位置跳动；缩到最小字号仍然超宽时，
原生跑马灯继续展示完整文本。
默认关闭，开启后控件会自动设置为单行：

| 属性 | 格式 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `shape_marqueeEnable` | boolean | `false` | 是否开启系统跑马灯 |
| `shape_marqueeRepeatLimit` | integer | `-1` | 重复次数，`-1` 表示无限循环 |
| `shape_marqueeRequireFullyVisible` | boolean | `false` | 是否要求控件完整位于屏幕内才滚动；普通按钮建议保持 `false` |

```xml
<com.allynav.shape.view.ShapeTextView
    android:layout_width="180dp"
    android:layout_height="48dp"
    android:gravity="center_vertical"
    android:text="超过控件宽度后自动滚动显示的文字"
    android:textSize="16sp"
    app:shape_marqueeEnable="true"
    app:shape_marqueeRepeatLimit="-1" />
```

### CheckBox 和 RadioButton 图标

| 属性 | 格式 | 说明 |
| --- | --- | --- |
| `shape_buttonDrawable` | reference | 默认按钮图标 |
| `shape_buttonPressedDrawable` | reference | 按下状态图标 |
| `shape_buttonCheckedDrawable` | reference | 选中状态图标 |
| `shape_buttonDisabledDrawable` | reference | 禁用状态图标 |
| `shape_buttonFocusedDrawable` | reference | 聚焦状态图标 |
| `shape_buttonSelectedDrawable` | reference | 选择状态图标 |

## Java 动态设置

### 修改 ShapeButton 颜色

```java
ShapeButton shapeButton = findViewById(R.id.btn_main_test);
shapeButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

        shapeButton.getShapeDrawableBuilder()
                .setSolidColor(0xFF000000)
                .setStrokeColor(0xFF5A8DDF)
                // 注意：最后需要调用 intoBackground 方法才能生效
                .intoBackground();

        shapeButton.getTextColorBuilder()
                .setTextColor(0xFFFFFFFF)
                // 注意：最后需要调用 intoTextColor 方法才能生效
                .intoTextColor();

        shapeButton.setText("颜色已经改变啦");
    }
});
```

导入本地控件：

```java
import com.allynav.shape.view.ShapeButton;
```

### 动态设置真实阴影、圆角和 Ripple

Builder 中所有尺寸参数都是像素，不是 dp。可以先提供一个转换方法：

```java
private int dp(float value) {
    return Math.round(value * getResources().getDisplayMetrics().density);
}
```

```java
shapeButton.getShapeDrawableBuilder()
        .setSolidColor(0xFFFFFFFF)
        .setTopRadius(dp(18))
        .setBottomRadius(dp(10))
        .setShadowSize(dp(14))
        .setShadowColor(0x52000000)
        .setShadowOffsetY(dp(5))
        .setShadowSpread(dp(1))
        .setShadowSymmetry(false)
        .setShadowHiddenLeft(false)
        .setShadowHiddenTop(false)
        .setShadowHiddenRight(false)
        .setShadowHiddenBottom(false)
        .setShadowBitmapScale(0.5f)
        .setRippleEnable(true)
        .setRippleColor(0x1F000000)
        .intoBackground();
```

### 动态设置状态颜色

```java
shapeButton.getShapeDrawableBuilder()
        .setSolidColor(0xFF2563EB)
        .setSolidPressedColor(0xFF1D4ED8)
        .setSolidDisabledColor(0xFF94A3B8)
        .setStrokeColor(0xFF1E40AF)
        .setStrokePressedColor(0xFF1E3A8A)
        .setStrokeSize(dp(1))
        .intoBackground();

shapeButton.getTextColorBuilder()
        .setTextColor(0xFFFFFFFF)
        .setTextPressedColor(0xFFE2E8F0)
        .setTextDisabledColor(0xFFF1F5F9)
        .intoTextColor();
```

状态色 setter 接受 `Integer` 的方法可以传 `null`，用于移除对应状态配置。

### 动态设置渐变和虚线

```java
import com.allynav.shape.drawable.ShapeGradientOrientation;

shapeButton.getShapeDrawableBuilder()
        .setSolidGradientColors(0xFF0EA5E9, 0xFF2563EB)
        .setSolidGradientOrientation(ShapeGradientOrientation.LEFT_TO_RIGHT)
        .setStrokeColor(0xFFFFFFFF)
        .setStrokeSize(dp(1))
        .setStrokeDashSize(dp(6))
        .setStrokeDashGap(dp(4))
        .intoBackground();
```

### 动态设置 Drawable 状态背景

```java
import androidx.appcompat.content.res.AppCompatResources;

shapeButton.getShapeDrawableBuilder()
        .setBackgroundDrawable(
                AppCompatResources.getDrawable(this, R.drawable.button_normal))
        .setPressedBackgroundDrawable(
                AppCompatResources.getDrawable(this, R.drawable.button_pressed))
        .setDisabledBackgroundDrawable(
                AppCompatResources.getDrawable(this, R.drawable.button_disabled))
        .setRadius(dp(12))
        .intoBackground();
```

### 动态设置状态文本

`TextStateDelegate` 的 setter 会立即刷新，不需要额外调用 `into...`：

```java
shapeButton.getTextStateDelegate()
        .setPressedText("松开提交")
        .setDisabledText("暂不可用")
        .setFocusedText("继续提交");
```

### 动态设置 ShapeTextView 自适应

```java
shapeTextView.setAdaptiveTextMode(
        ShapeTextView.ADAPTIVE_MODE_REDUCE_LINE_SPACING_THEN_LINES);
shapeTextView.setAdaptiveMinLines(1);
shapeTextView.setAdaptiveTextEnabled(true);
```

`setAdaptiveMinLineSpacingExtra(float)` 的单位是 px。调用
`clearAdaptiveMinLineSpacingExtra()` 可以恢复为按字体 descent 自动计算下限。

### 动态设置自动字号和跑马灯

```java
shapeTextView.setAutoFitMinTextSize(12f); // 单位：sp
shapeTextView.setAutoFitPrecision(0.5f);
shapeTextView.setAutoFitTextEnabled(true);

shapeTextView.setShapeMarqueeRepeatLimit(-1);
shapeTextView.setMarqueeRequireFullyVisible(true);
shapeTextView.setMarqueeEnabled(true);
```

关闭 `setMarqueeEnabled(false)` 会恢复控件创建时的单行、行数和 `ellipsize` 配置；关闭
`setAutoFitTextEnabled(false)` 会恢复调用前的字号。

为方便从 XUI `AutoFitTextView` 迁移，也保留了 `setEnableFit`、`enableFit`、`isEnableFit`、
`setMinTextSize`、`setMaxTextSize` 和 `setPrecision` 这些兼容方法。

### 动态设置 CheckBox 或 RadioButton 图标

```java
shapeCheckBox.getButtonDrawableBuilder()
        .setButtonDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_unchecked))
        .setButtonCheckedDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_checked))
        .setButtonDisabledDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_disabled))
        .intoButtonDrawable();
```

## Builder 方法速查

### ShapeDrawableBuilder

通过所有控件的 `getShapeDrawableBuilder()` 获取。

| 分类 | 方法 |
| --- | --- |
| 形状 | `setType`、`setWidth`、`setHeight`、`setLineGravity` |
| 圆角 | `setRadius`、`setRadiusRelative`、`setTopLeftRadius`、`setTopRightRadius`、`setBottomLeftRadius`、`setBottomRightRadius`、`setTopRadius`、`setBottomRadius` |
| 填充状态 | `setSolidColor`、`setSolidPressedColor`、`setSolidCheckedColor`、`setSolidDisabledColor`、`setSolidFocusedColor`、`setSolidSelectedColor` |
| 填充渐变 | `setSolidGradientColors`、`setSolidGradientOrientation`、`setSolidGradientType`、`setSolidGradientCenterX`、`setSolidGradientCenterY`、`setSolidGradientRadius`、`clearSolidGradientColors` |
| 边框 | `setStrokeColor`、`setStrokePressedColor`、`setStrokeCheckedColor`、`setStrokeDisabledColor`、`setStrokeFocusedColor`、`setStrokeSelectedColor`、`setStrokeSize`、`setStrokeDashSize`、`setStrokeDashGap` |
| 边框渐变 | `setStrokeGradientColors`、`setStrokeGradientOrientation`、`clearStrokeGradientColors` |
| 圆环 | `setRingInnerRadiusSize`、`setRingInnerRadiusRatio`、`setRingThicknessSize`、`setRingThicknessRatio` |
| 阴影 | `setShadowHidden`、`setShadowSize`、`setShadowColor`、`setShadowOffsetX`、`setShadowOffsetY`、`setShadowSpread`、`setShadowSymmetry`、`setShadowHiddenLeft`、`setShadowHiddenTop`、`setShadowHiddenRight`、`setShadowHiddenBottom`、`setShadowBitmapScale` |
| Ripple | `setRippleEnable`、`setRippleColor` |
| Drawable 状态 | `setBackgroundDrawable`、`setPressedBackgroundDrawable`、`setCheckedBackgroundDrawable`、`setDisabledBackgroundDrawable`、`setFocusedBackgroundDrawable`、`setSelectedBackgroundDrawable` |
| 应用/清除 | `intoBackground`、`clearBackground` |

形状常量位于 `com.allynav.shape.drawable.ShapeType`：`RECTANGLE`、`OVAL`、`LINE`、`RING`。
渐变类型常量位于 `ShapeGradientType`：`LINEAR_GRADIENT`、`RADIAL_GRADIENT`、
`SWEEP_GRADIENT`。

### TextColorBuilder

通过文本控件的 `getTextColorBuilder()` 获取。

| 分类 | 方法 |
| --- | --- |
| 状态色 | `setTextColor`、`setTextPressedColor`、`setTextCheckedColor`、`setTextDisabledColor`、`setTextFocusedColor`、`setTextSelectedColor` |
| 渐变 | `setTextGradientColors`、`setTextGradientOrientation`、`clearTextGradientColor` |
| 描边 | `setTextStrokeColor`、`setTextStrokeSize`、`clearTextStrokeColor` |
| 应用 | `intoTextColor` |

### ButtonDrawableBuilder

仅 `ShapeCheckBox` 和 `ShapeRadioButton` 提供 `getButtonDrawableBuilder()`。

| 分类 | 方法 |
| --- | --- |
| 图标状态 | `setButtonDrawable`、`setButtonPressedDrawable`、`setButtonCheckedDrawable`、`setButtonDisabledDrawable`、`setButtonFocusedDrawable`、`setButtonSelectedDrawable` |
| 应用 | `intoButtonDrawable` |

### ShapeTextView 复合图片 tint

`ShapeTextView` 支持对 `android:drawableStart`、`android:drawableTop`、`android:drawableEnd`
和 `android:drawableBottom` 统一设置状态 tint。Android 的 TextView 只提供一份复合图片 tint，
因此四个方向会使用同一组状态颜色。

| 属性 | 格式 | 说明 |
| --- | --- | --- |
| `shape_enableTint` | color | 普通启用状态 tint；不配置时保留图片自身颜色 |
| `shape_pressedTint` | color | 按下状态 tint |
| `shape_checkedTint` | color | checked 状态 tint，供扩展控件使用 |
| `shape_disableTint` | color | `enabled=false` 时的 tint |
| `shape_focusedTint` | color | 聚焦状态 tint |
| `shape_selectedTint` | color | `selected=true` 时的 tint |

`shape_tint` 是 `shape_enableTint` 的兼容名称，`shape_disabledTint` 是
`shape_disableTint` 的兼容名称；新旧名称同时配置时使用新名称。

下面只配置按下和禁用状态。普通状态没有 `shape_enableTint`，所以
`@mipmap/obstacles_points` 会显示图片自身颜色：

```xml
<com.allynav.shape.view.ShapeTextView
    android:layout_width="120dp"
    android:layout_height="38dp"
    android:clickable="true"
    android:drawableStart="@mipmap/obstacles_points"
    android:text="障碍点"
    app:shape_pressedTint="#00C853"
    app:shape_disableTint="#808080" />
```

需要普通状态也统一着色时，再添加 `app:shape_enableTint="#FFFFFF"`。Java 动态设置：

```java
shapeTextView.getCompoundDrawableTintBuilder()
        .setPressedTintColor(0xFF00C853)
        .setDisableTintColor(0xFF808080)
        .intoTint();
```

按下状态要求控件可点击。`shape_marqueeEnable="true"` 的跑马灯会通过 `selected=true`
维持滚动，因此跑马灯控件配置 `shape_selectedTint` 后，该颜色通常会持续生效。

### ShapeImageView 图片 tint

`ShapeImageView` 支持图片 tint 的状态切换，不会改变 Shape 背景、阴影或圆角。未匹配自定义状态时，
会回退到 `android:tint`；如果没有设置 `android:tint`，则保持 `android:src` 图片自身颜色。

| 属性 | 格式 | 说明 |
| --- | --- | --- |
| `shape_enableTint` | color | 普通启用状态 tint；不配置时保留图片自身颜色 |
| `shape_pressedTint` | color | 按下状态 tint |
| `shape_checkedTint` | color | 选中状态 tint |
| `shape_disableTint` | color | `enabled=false` 时的 tint |
| `shape_focusedTint` | color | 聚焦状态 tint |
| `shape_selectedTint` | color | 选择状态 tint |

`shape_tint` 和 `shape_disabledTint` 继续作为兼容名称使用。

```xml
<com.allynav.shape.view.ShapeImageView
    android:layout_width="48dp"
    android:layout_height="48dp"
    android:src="@drawable/ic_location"
    app:shape_enableTint="#607D8B"
    app:shape_pressedTint="#1565C0"
    app:shape_selectedTint="#2E7D32"
    app:shape_disableTint="#BDBDBD" />
```

Java 动态设置：

```java
shapeImageView.getImageTintBuilder()
        .setEnableTintColor(0xFF607D8B)
        .setPressedTintColor(0xFF1565C0)
        .setSelectedTintColor(0xFF2E7D32)
        .setDisableTintColor(0xFFBDBDBD)
        .intoTint();
```

也可以使用简写方法 `setTint`、`setPressedTint`、`setSelectedTint` 等。

### ShapeImageView 状态 src

`ShapeImageView` 也支持按状态切换图片。状态图片没有配置时回退到默认图片；默认图片没有配置时
回退到 `android:src`。

按下状态要求控件设置了点击监听，或配置 `android:clickable="true"`。普通 `ShapeImageView`
的选中状态使用 `shape_selectedSrc` / `shape_selectedTint`，并通过
`shapeImageView.setSelected(true)` 切换；`checked` 属性仅供实现了 Checkable 状态的扩展控件使用。

| 属性 | 格式 | 说明 |
| --- | --- | --- |
| `shape_src` | reference | 默认图片，缺省时使用 `android:src` |
| `shape_pressedSrc` | reference | 按下状态图片 |
| `shape_checkedSrc` | reference | 选中状态图片 |
| `shape_disabledSrc` | reference | 禁用状态图片 |
| `shape_focusedSrc` | reference | 聚焦状态图片 |
| `shape_selectedSrc` | reference | 选择状态图片 |

```xml
<com.allynav.shape.view.ShapeImageView
    android:layout_width="48dp"
    android:layout_height="48dp"
    android:src="@drawable/ic_normal"
    app:shape_pressedSrc="@drawable/ic_pressed"
    app:shape_selectedSrc="@drawable/ic_selected"
    app:shape_pressedTint="#1565C0"
    app:shape_selectedTint="#2E7D32" />
```

Java 动态设置：

```java
shapeImageView.getImageSourceBuilder()
        .setPressedSourceDrawable(AppCompatResources.getDrawable(
                this, R.drawable.ic_pressed))
        .setSelectedSourceDrawable(AppCompatResources.getDrawable(
                this, R.drawable.ic_selected))
        .intoSource();
```

`ImageTintBuilder` 修改后必须调用 `intoTint()`；`ImageSourceBuilder` 修改后必须调用
`intoSource()`。

### TextStateDelegate

文本控件通过 `getTextStateDelegate()` 获取，支持 `setPressedText`、`setCheckedText`、
`setDisabledText`、`setFocusedText`、`setSelectedText`。

## 状态匹配顺序

背景和状态文本优先处理禁用状态，其余状态依次为按下、选中、聚焦、选择，最后回退到默认状态。
CheckBox/RadioButton 的 `checked` 状态只有在控件确实进入选中状态时才会显示。

## 注意事项

1. `ShapeDrawableBuilder` 修改后必须调用 `intoBackground()`。
2. `TextColorBuilder` 修改后必须调用 `intoTextColor()`。
3. `ButtonDrawableBuilder` 修改后必须调用 `intoButtonDrawable()`。
4. Java Builder 的尺寸参数单位是 px；XML dimension 建议使用 dp。
5. 阴影空间位于 View 边界内。固定尺寸过小时，主体内容会变小，应给控件预留足够宽高。
6. 单角属性会覆盖 `shape_radiusInTop`、`shape_radiusInBottom` 和 `shape_radius`。
7. 使用 `shape_solidGradient*` 后，渐变填充优先于普通 `shape_solidColor`。
8. 不要同时引入原版 ShapeView/ShapeDrawable，资源名称兼容会导致冲突。

## 许可和来源

ShapeView 与 ShapeDrawable 使用 Apache-2.0 License；阴影空间和缓存设计参考了 MIT License
的 ShadowLayout。完整说明见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。
