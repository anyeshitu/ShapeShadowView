# ShapeShadowView

`ShapeShadowView` 是一个面向 Android View 的 Shape UI library。

它以 [ShapeView](https://github.com/getActivity/ShapeView) 的控件体系、`shape_*` XML
属性和 Builder API 为基础，并吸收 [ShadowLayout](https://github.com/lihangleo2/ShadowLayout)
的阴影空间计算与 bitmap cache 思路。库内已经包含 ShapeDrawable 实现，通常只需要引入
`ShapeShadowView`，不需要再同时依赖原版 ShapeView、ShapeDrawable 或 ShadowLayout。

## Features

- Shape：`rectangle`、`oval`、`line`、`ring`
- Corner radius：统一圆角、四角独立圆角、Start/End 圆角、顶部/底部组合圆角
- Background：solid color、state color、custom `Drawable`、`VectorDrawable`、Ripple
- Border：普通边框、state border、dashed border、dashed line
- Gradient：linear、radial、sweep、任意角度、XML 多色数组
- Shadow：模糊半径、offset、spread、对称空间、按边隐藏、bitmap cache scale
- Text：state color、gradient、stroke、state text、Adaptive text、AutoFit、Marquee
- Image：state `tint`、state `src`、图片自身颜色的 blur shadow
- Container：Shape 圆角裁剪子 View，不改变子 View 的测量、布局和事件
- Controls：`ShapeSwitchButton`、`ShapeScrollIndicator`
- Java API：沿用 ShapeView 的 `Builder` 调用方式，修改后显式调用 `into...()` 生效

## Table of Contents

- [Requirements](#requirements)
- [Installation](#installation)
- [Local Module](#local-module)
- [Components](#components)
- [Quick Start](#quick-start)
- [Shape and Background](#shape-and-background)
- [Gradient](#gradient)
- [Shadow](#shadow)
- [Ripple and Click State](#ripple-and-click-state)
- [Text Components](#text-components)
- [Image Components](#image-components)
- [ShapeSwitchButton](#shapeswitchbutton)
- [ShapeScrollIndicator](#shapescrollindicator)
- [ViewGroup Child Clipping](#viewgroup-child-clipping)
- [Java Builder API](#java-builder-api)
- [State Priority](#state-priority)
- [Compatibility Notes](#compatibility-notes)
- [License](#license)

## Requirements

- Android `minSdk 21`
- `compileSdk 35` or higher is recommended
- AndroidX
- Java 8

应用模块可以使用以下 Java 8 配置：

```groovy
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
```

## Installation

### JitPack

在根工程的 `settings.gradle` 或 `settings.gradle.kts` 中加入 JitPack repository。

Kotlin DSL：

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

在应用 module 的 `build.gradle` 或 `build.gradle.kts` 中添加当前版本：

Kotlin DSL：

```kotlin
dependencies {
    implementation("com.github.anyeshitu:ShapeShadowView:1.1.14")
}
```

Groovy DSL：

```groovy
dependencies {
    implementation 'com.github.anyeshitu:ShapeShadowView:1.1.14'
}
```

### Dependency compatibility

库中的 Shape 控件继承 AndroidX AppCompat 控件，发布包声明了以下公开依赖：

- `androidx.appcompat:appcompat:1.6.1`
- `androidx.constraintlayout:constraintlayout:2.2.1`
- `androidx.recyclerview:recyclerview:1.4.0`

Gradle 通常会按宿主工程的 dependency resolution 规则选择最终版本。如果项目需要固定
AppCompat 版本，可以排除库传递的 AppCompat，再由宿主工程统一提供：

```groovy
implementation('com.github.anyeshitu:ShapeShadowView:1.1.14') {
    // 由宿主工程统一管理 AppCompat 版本，避免三方依赖改变主题行为。
    exclude group: 'androidx.appcompat', module: 'appcompat'
}
```

该配置只处理 dependency version conflict，不会替宿主工程自动修复 theme。使用
`MaterialButton`、Material inflater 或 AppCompat dialog 时，Activity、Fragment 和
Dialog Context 应使用匹配的 AndroidX/Material theme。

### JitPack troubleshooting

如果出现 `401 Unauthorized` 或 `Failed to resolve`：

1. 确认 GitHub repository 为 public。
2. 确认 JitPack repository 已加入 `settings.gradle(.kts)`，而不是只加入 module 文件。
3. 确认依赖版本对应已推送的 Git tag，例如 `1.1.14`。
4. 在 JitPack 页面检查该 tag 的 build log；JitPack 需要能够访问公开仓库和 tag。

## Local Module

把仓库中的 `library` 目录复制到应用工程中，例如：

```text
your-project/
├── app/
├── shape-shadow-view/
│   └── build.gradle.kts
└── settings.gradle.kts
```

在根工程 `settings.gradle.kts` 中加入：

```kotlin
include(":shape-shadow-view")
```

在应用 module 中加入：

```kotlin
dependencies {
    implementation(project(":shape-shadow-view"))
}
```

如果保留 `library` 目录的嵌套结构，也可以映射 module path：

```kotlin
include(":shape-shadow-view")
project(":shape-shadow-view").projectDir = file("shape-shadow-view/library")
```

Groovy DSL：

```groovy
include ':shape-shadow-view'

dependencies {
    implementation project(':shape-shadow-view')
}
```

本地接入后，请移除原版的 ShapeView 和 ShapeDrawable 依赖，避免同名 class、resource
和 attribute 冲突：

```groovy
// 删除以下依赖
implementation 'com.github.getActivity:ShapeView:11.0'
implementation 'com.github.getActivity:ShapeDrawable:5.0'
```

不需要额外添加 ShadowLayout。

## Components

### View

| Component | Class |
| --- | --- |
| ShapeView | `com.allynav.shape.view.ShapeView` |
| ShapeTextView | `com.allynav.shape.view.ShapeTextView` |
| ShapeButton | `com.allynav.shape.view.ShapeButton` |
| ShapeImageView | `com.allynav.shape.view.ShapeImageView` |
| ShapeRadioButton | `com.allynav.shape.view.ShapeRadioButton` |
| ShapeCheckBox | `com.allynav.shape.view.ShapeCheckBox` |
| ShapeEditText | `com.allynav.shape.view.ShapeEditText` |
| ShapeSwitchButton | `com.allynav.shape.view.ShapeSwitchButton` |
| ShapeScrollIndicator | `com.allynav.shape.view.ShapeScrollIndicator` |

### ViewGroup

| Component | Class |
| --- | --- |
| ShapeLinearLayout | `com.allynav.shape.layout.ShapeLinearLayout` |
| ShapeFrameLayout | `com.allynav.shape.layout.ShapeFrameLayout` |
| ShapeRelativeLayout | `com.allynav.shape.layout.ShapeRelativeLayout` |
| ShapeConstraintLayout | `com.allynav.shape.layout.ShapeConstraintLayout` |
| ShapeRecyclerView | `com.allynav.shape.layout.ShapeRecyclerView` |
| ShapeRadioGroup | `com.allynav.shape.layout.ShapeRadioGroup` |

Shape 控件统一提供 `getShapeDrawableBuilder()`。文本控件额外提供 text Builder 和
`TextStateDelegate`；`ShapeImageView` 额外提供 image tint/source/blur shadow Builder。
`ShapeSwitchButton` 和 `ShapeScrollIndicator` 是独立组件，详见后文。

## Quick Start

先在布局根节点声明 `app` namespace：

```xml
xmlns:app="http://schemas.android.com/apk/res-auto"
```

下面的按钮同时使用圆角、状态色、Ripple、dashed border 和真实 shadow：

```xml
<com.allynav.shape.view.ShapeButton
    android:id="@+id/btn_main_test"
    android:layout_width="180dp"
    android:layout_height="56dp"
    android:gravity="center"
    android:text="确认"
    android:textSize="16sp"
    app:shape_solidColor="#FFFFFFFF"
    app:shape_solidPressedColor="#FFF2F5F8"
    app:shape_radiusInTop="18dp"
    app:shape_radiusInBottom="10dp"
    app:shape_strokeColor="#FF5A8DDF"
    app:shape_strokeSize="1dp"
    app:shape_strokeDashSize="6dp"
    app:shape_strokeDashGap="4dp"
    app:shape_rippleEnable="true"
    app:shape_rippleColor="#1F000000"
    app:shape_shadowColor="#52000000"
    app:shape_shadowSize="14dp"
    app:shape_shadowOffsetY="5dp"
    app:shape_shadowSpread="1dp"
    app:shape_textColor="#FF202124"
    app:shape_textPressedColor="#FF000000" />
```

对应的 Java 动态修改方式：

```java
ShapeButton shapeButton = findViewById(R.id.btn_main_test);
shapeButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        shapeButton.getShapeDrawableBuilder()
                .setSolidColor(0xFF000000)
                .setStrokeColor(0xFF5A8DDF)
                // ShapeView 风格 Builder 必须调用 intoBackground() 才会应用。
                .intoBackground();

        shapeButton.getTextColorBuilder()
                .setTextColor(0xFFFFFFFF)
                // TextColorBuilder 必须调用 intoTextColor() 才会应用。
                .intoTextColor();

        shapeButton.setText("颜色已经改变啦");
    }
});
```

## Shape and Background

### Common shape attributes

除特别说明外，以下属性适用于 Shape View 和 Shape ViewGroup。`dimension` 在 XML 中
建议使用 `dp`；Java Builder 的尺寸参数使用 `px`。

| Attribute | Format / values | Default | Description |
| --- | --- | --- | --- |
| `shape_type` | `rectangle`, `oval`, `line`, `ring` | `rectangle` | Shape 类型 |
| `shape_width` | dimension | unset | Drawable 内部宽度，不等同于 `layout_width` |
| `shape_height` | dimension | unset | Drawable 内部高度，不等同于 `layout_height` |
| `shape_solidColor` | color | transparent | 默认 fill color |
| `shape_solidPressedColor` | color | fallback | pressed 状态 fill color |
| `shape_solidCheckedColor` | color | fallback | checked 状态 fill color；适用于具有 checked state 的控件 |
| `shape_solidDisabledColor` | color | fallback | disabled 状态 fill color |
| `shape_solidFocusedColor` | color | fallback | focused 状态 fill color |
| `shape_solidSelectedColor` | color | fallback | selected 状态 fill color |
| `shape_strokeColor` | color | transparent | 默认 border color |
| `shape_strokePressedColor` | color | fallback | pressed 状态 border color |
| `shape_strokeCheckedColor` | color | fallback | checked 状态 border color；适用于具有 checked state 的控件 |
| `shape_strokeDisabledColor` | color | fallback | disabled 状态 border color |
| `shape_strokeFocusedColor` | color | fallback | focused 状态 border color |
| `shape_strokeSelectedColor` | color | fallback | selected 状态 border color |
| `shape_strokeSize` | dimension | `0dp` | border width |
| `shape_strokeDashSize` | dimension | `0dp` | dashed segment length；大于 0 时启用虚线 |
| `shape_strokeDashGap` | dimension | `0dp` | dashed segment gap |

状态属性是否生效取决于控件是否能够产生对应的 Android `DrawableState`。例如普通
`ShapeTextView` 没有 `checked` state；`ShapeCheckBox`、`ShapeRadioButton` 和
`ShapeSwitchButton` 才会使用 checked fill/border。没有产生某个状态时，控件会回退到
默认值。

### Corner radius

```xml
<com.allynav.shape.view.ShapeView
    android:layout_width="200dp"
    android:layout_height="80dp"
    app:shape_radius="12dp"
    app:shape_radiusInTop="24dp"
    app:shape_radiusInBottom="6dp" />
```

| Attribute | Description |
| --- | --- |
| `shape_radius` | 四个角统一圆角 |
| `shape_radiusInTop` | 同时设置左上和右上圆角，本库扩展属性 |
| `shape_radiusInBottom` | 同时设置左下和右下圆角，本库扩展属性 |
| `shape_radiusInTopLeft` | 左上物理方向圆角 |
| `shape_radiusInTopStart` | 顶部 Start 圆角，自动适配 RTL |
| `shape_radiusInTopRight` | 右上物理方向圆角 |
| `shape_radiusInTopEnd` | 顶部 End 圆角，自动适配 RTL |
| `shape_radiusInBottomLeft` | 左下物理方向圆角 |
| `shape_radiusInBottomStart` | 底部 Start 圆角，自动适配 RTL |
| `shape_radiusInBottomRight` | 右下物理方向圆角 |
| `shape_radiusInBottomEnd` | 底部 End 圆角，自动适配 RTL |

优先级从高到低为：物理方向单角 `Left/Right` > 相对方向单角 `Start/End` > 组合圆角
`Top/Bottom` > `shape_radius`。例如同时设置 `shape_radiusInTop="20dp"` 和
`shape_radiusInTopLeft="4dp"` 时，左上角使用 `4dp`，右上角使用 `20dp`。

### State colors and custom Drawable

Shape 会根据当前 Android `DrawableState` 选择 fill 和 border。除 color 外，也可以为各
状态指定自定义背景：

| Attribute | Description |
| --- | --- |
| `shape_background` | 默认背景，支持 color、bitmap、Vector 和 XML `Drawable` |
| `shape_pressedBackground` | pressed 状态背景 |
| `shape_checkedBackground` | checked 状态背景 |
| `shape_disabledBackground` | disabled 状态背景 |
| `shape_focusedBackground` | focused 状态背景 |
| `shape_selectedBackground` | selected 状态背景 |

```xml
<com.allynav.shape.view.ShapeButton
    android:layout_width="160dp"
    android:layout_height="48dp"
    android:text="提交"
    app:shape_radius="12dp"
    app:shape_background="@drawable/button_normal"
    app:shape_pressedBackground="@drawable/button_pressed"
    app:shape_disabledBackground="#FFDADCE0" />
```

指定状态的 custom `Drawable` 优先于同一状态的 fill/border color；没有配置的状态继续
回退到默认背景或默认 Shape。

### Line and ring

`shape_type="line"` 时使用 `shape_lineGravity` 控制线条位置。可选 flag 包括
`top`、`bottom`、`left`、`right`、`start`、`end`、`center`，也可以组合使用。

```xml
<com.allynav.shape.view.ShapeView
    android:layout_width="match_parent"
    android:layout_height="1dp"
    app:shape_type="line"
    app:shape_lineGravity="center"
    app:shape_strokeColor="#FF607D8B"
    app:shape_strokeSize="1dp"
    app:shape_strokeDashSize="6dp"
    app:shape_strokeDashGap="4dp" />
```

`shape_type="ring"` 时可以使用以下参数：

| Attribute | Description |
| --- | --- |
| `shape_ringInnerRadiusSize` | inner radius，优先于 ratio |
| `shape_ringInnerRadiusRatio` | Shape 尺寸除以该 ratio 得到 inner radius |
| `shape_ringThicknessSize` | ring thickness，优先于 ratio |
| `shape_ringThicknessRatio` | Shape 尺寸除以该 ratio 得到 thickness |

## Gradient

### XML attributes

Fill 和 border 都支持 linear、radial、sweep 三种 gradient。线性渐变支持预设方向和
任意角度；radial/sweep 使用 center 与 radius 参数。

| Attribute | Description |
| --- | --- |
| `shape_solidGradientStartColor` | fill gradient start color |
| `shape_solidGradientCenterColor` | 可选 fill gradient center color |
| `shape_solidGradientEndColor` | fill gradient end color |
| `shape_solidGradientColors` | `@array` 多色 fill gradient；优先于 start/center/end |
| `shape_solidGradientType` | `linear`、`radial`、`sweep` |
| `shape_solidGradientOrientation` | 线性 gradient 方向 |
| `shape_solidGradientAngle` | 任意角度线性 gradient；优先于 orientation |
| `shape_solidGradientCenterX` | radial/sweep center X，默认 `0.5` |
| `shape_solidGradientCenterY` | radial/sweep center Y，默认 `0.5` |
| `shape_solidGradientRadius` | radial radius |
| `shape_strokeGradientStartColor` | border gradient start color |
| `shape_strokeGradientCenterColor` | 可选 border gradient center color |
| `shape_strokeGradientEndColor` | border gradient end color |
| `shape_strokeGradientColors` | `@array` 多色 border gradient |
| `shape_strokeGradientOrientation` | border gradient 方向 |
| `shape_strokeGradientAngle` | 任意角度 border gradient |

线性 gradient 的 orientation：

| Value | Direction |
| --- | --- |
| `leftToRight` / `startToEnd` | 左到右 / Start 到 End |
| `rightToLeft` / `endToStart` | 右到左 / End 到 Start |
| `bottomToTop` | 下到上 |
| `topToBottom` | 上到下 |
| `topLeftToBottomRight` / `topStartToBottomEnd` | 左上到右下 |
| `bottomLeftToTopRight` / `bottomStartToTopEnd` | 左下到右上 |
| `topRightToBottomLeft` / `topEndToBottomStart` | 右上到左下 |
| `bottomRightToTopLeft` / `bottomEndToTopStart` | 右下到左上 |

angle 使用 Android gradient 的坐标习惯：`0` 度为左到右，`90` 度为下到上，支持正负
浮点数。配置 angle 后，该层的 orientation 不再生效。

### XML multi-color gradient

数组至少需要两个 color。未指定 positions 时，颜色按等距位置分布：

```xml
<!-- res/values/colors.xml 或 res/values/arrays.xml -->
<array name="shape_card_gradient">
    <item>#FF3B82F6</item>
    <item>#FF22C55E</item>
    <item>#FFF59E0B</item>
    <item>#FFEF4444</item>
</array>
```

```xml
<com.allynav.shape.view.ShapeTextView
    android:layout_width="200dp"
    android:layout_height="52dp"
    android:gravity="center"
    android:text="多色渐变"
    app:shape_radius="8dp"
    app:shape_solidGradientColors="@array/shape_card_gradient"
    app:shape_solidGradientAngle="35"
    app:shape_strokeGradientColors="@array/shape_card_gradient"
    app:shape_strokeGradientAngle="125"
    app:shape_strokeSize="2dp" />
```

## Shadow

阴影使用 bitmap cache 和 blur mask 绘制，不依赖系统 `elevation`。因此 shadow 的 color、
blur size、offset、spread 和占位空间都可以独立控制。

| Attribute | Format | Default | Description |
| --- | --- | --- | --- |
| `shape_shadowSize` | dimension | `0dp` | blur radius；大于 0 才启用 |
| `shape_shadowColor` | color | `#40000000` | shadow color and alpha |
| `shape_shadowOffsetX` | dimension | `0dp` | 水平偏移，正值向右 |
| `shape_shadowOffsetY` | dimension | `0dp` | 垂直偏移，正值向下 |
| `shape_shadowSpread` | dimension | `0dp` | blur 前向外扩张的距离 |
| `shape_shadowSymmetry` | boolean | `false` | 左右、上下分别使用对称 inset |
| `shape_shadowHidden` | boolean | `false` | 临时隐藏整个 shadow |
| `shape_shadowHiddenLeft` | boolean | `false` | 隐藏左侧 shadow 并取消该侧占位 |
| `shape_shadowHiddenTop` | boolean | `false` | 隐藏顶部 shadow 并取消该侧占位 |
| `shape_shadowHiddenRight` | boolean | `false` | 隐藏右侧 shadow 并取消该侧占位 |
| `shape_shadowHiddenBottom` | boolean | `false` | 隐藏底部 shadow 并取消该侧占位 |
| `shape_shadowBitmapScale` | float | `0.5` | shadow cache scale，运行时限制为 `0.25..1.0` |

```xml
<com.allynav.shape.view.ShapeTextView
    android:layout_width="220dp"
    android:layout_height="96dp"
    android:gravity="center"
    android:text="真实阴影"
    app:shape_solidColor="#FFFFFFFF"
    app:shape_radius="14dp"
    app:shape_shadowColor="#66000000"
    app:shape_shadowSize="16dp"
    app:shape_shadowOffsetY="6dp"
    app:shape_shadowSpread="1dp"
    app:shape_shadowSymmetry="false"
    app:shape_shadowBitmapScale="0.5" />
```

阴影空间位于 View 自身边界内，固定宽高会同时包含 Shape 和 shadow inset。尺寸较小时，
主体内容可能被压缩；需要为 shadow 预留足够的宽高。`ShapeLayout` 的可用子内容区域也
会随 shadow inset 相应减少。

## Ripple and Click State

### Ripple

`shape_rippleEnable` 默认关闭，开启后使用 Android `RippleDrawable`，并根据当前 Shape
轮廓生成 mask，圆角位置不会出现矩形 ripple：

```xml
app:shape_rippleEnable="true"
app:shape_rippleColor="#26007AFF"
```

| Attribute | Format | Default | Description |
| --- | --- | --- | --- |
| `shape_rippleEnable` | boolean | `false` | 是否启用 Android native Ripple |
| `shape_rippleColor` | color | `#24000000` | Ripple color |

### Non-clickable but enabled

`shape_clickable` 是库独立的交互开关，与 `android:enabled` 分离：

- `shape_clickable="false"` 时，控件仍保持 `enabled=true`，所以 enabled 状态的文字色、
  图片 tint/src 和其他 state 逻辑仍然有效。
- 控件自身不响应 click；对于 Shape ViewGroup，子 View 仍可继续接收自己的触摸事件。
- `shape_nonClickableBackground` 可指定该状态使用的 color 或 custom `Drawable`。
- `android:enabled="false"` 仍然优先进入 disabled state，不会被误认为只是不可点击。

```xml
<com.allynav.shape.view.ShapeButton
    android:layout_width="160dp"
    android:layout_height="48dp"
    android:enabled="true"
    android:text="暂不可点击"
    app:shape_clickable="false"
    app:shape_nonClickableBackground="#FFBDBDBD"
    app:shape_solidColor="#FF1976D2" />
```

## Text Components

普通 text color、gradient 和 stroke 适用于 `ShapeTextView`、`ShapeButton`、
`ShapeEditText`、`ShapeCheckBox` 和 `ShapeRadioButton`。`checked` text color 只有在控件
具有 checked state 时才会生效。

### Text color, gradient and stroke

| Attribute | Description |
| --- | --- |
| `shape_textColor` | 默认 text color；未配置时使用 `android:textColor` |
| `shape_textPressedColor` | pressed text color |
| `shape_textCheckedColor` | checked text color；适用于 `CheckBox`、`RadioButton` 等 checked 控件 |
| `shape_textDisabledColor` | disabled text color |
| `shape_textFocusedColor` | focused text color |
| `shape_textSelectedColor` | selected text color |
| `shape_textStartColor` | text gradient start color |
| `shape_textCenterColor` | 可选 text gradient center color |
| `shape_textEndColor` | text gradient end color |
| `shape_textGradientOrientation` | `horizontal` 或 `vertical` |
| `shape_textStrokeColor` | text stroke color |
| `shape_textStrokeSize` | text stroke width |

同时配置 text gradient start 和 end color 后启用 text gradient。Java 中通过
`getTextColorBuilder()` 配置，最后调用 `intoTextColor()`。

### Stateful text content

文本控件通过 `TextStateDelegate` 支持 state text：

| Attribute | Description |
| --- | --- |
| `shape_textPressed` | pressed 时显示的文本 |
| `shape_textChecked` | checked 时显示的文本 |
| `shape_textDisabled` | disabled 时显示的文本 |
| `shape_textFocused` | focused 时显示的文本 |
| `shape_textSelected` | selected 时显示的文本 |

```xml
<com.allynav.shape.view.ShapeTextView
    android:layout_width="160dp"
    android:layout_height="48dp"
    android:text="提交"
    app:shape_textPressed="松开提交"
    app:shape_textDisabled="暂不可用" />
```

`ShapeEditText` 会同步用户通过键盘输入、删除、粘贴产生的 `Editable` 内容；状态文本
内部的 `setText()` 不会反过来污染默认文本。因此输入框失焦后不会恢复到 XML 初始化旧值。

### Adaptive text and AutoFit

`ShapeTextView` 提供两套可独立开启的文字适配能力，默认都关闭：

#### Fixed-height Adaptive text

用于固定高度控件，在高度不足时压缩 line spacing、减少行数，或按指定组合策略处理。

| Attribute | Default | Description |
| --- | --- | --- |
| `shape_adaptiveTextEnable` | `false` | 是否开启 fixed-height Adaptive text |
| `shape_adaptiveTextMode` | `reduceLines` | `reduceLines`、`reduceLineSpacing`、`reduceLineSpacingThenLines` |
| `shape_adaptiveMinLines` | `1` | 允许保留的最小行数 |
| `shape_adaptiveMinLineSpacingExtra` | auto | `setLineSpacing` 的 add 参数下限 |

#### AutoFit text size

用于在可用宽度内自动缩小字号：

| Attribute | Default | Description |
| --- | --- | --- |
| `shape_autoFitTextEnable` | `false` | 是否开启 AutoFit |
| `shape_autoFitMinTextSize` | `8sp` | 最小字号 |
| `shape_autoFitMaxTextSize` | `android:textSize` | 最大字号 |
| `shape_autoFitPrecision` | `0.5` | 二分查找精度 |
| `shape_textBaselineEnabled` | auto | 是否向父 `LinearLayout` 提供 baseline |

AutoFit 开启时，若没有显式配置 `shape_textBaselineEnabled`，控件默认不向父
`LinearLayout` 输出 baseline，避免 GONE -> VISIBLE 或字号变化时发生纵向跳动。需要表单
基线对齐时可以显式设置：

```xml
app:shape_textBaselineEnabled="true"
```

### Marquee

开启 `shape_marqueeEnable` 后，控件会自动设置为 single line，并在最终测量、布局和屏幕
可见后启动 system Marquee：

| Attribute | Default | Description |
| --- | --- | --- |
| `shape_marqueeEnable` | `false` | 是否开启 system Marquee |
| `shape_marqueeRepeatLimit` | `-1` | 重复次数；`-1` 表示无限循环 |
| `shape_marqueeRequireFullyVisible` | `false` | 是否要求控件完整位于屏幕内才滚动 |

```xml
<com.allynav.shape.view.ShapeTextView
    android:layout_width="180dp"
    android:layout_height="48dp"
    android:gravity="center_vertical"
    android:ellipsize="marquee"
    android:singleLine="true"
    android:text="超过控件宽度后自动滚动显示的长文本"
    app:shape_marqueeEnable="true"
    app:shape_marqueeRepeatLimit="-1" />
```

以下行为已经由控件内部处理：

- 文本从短内容切换为长内容时，自动重建 layout 并重新触发 Marquee。
- 控件由 `GONE`/`INVISIBLE` 变为可见或父布局重新排版后，会在尺寸稳定时重新判断。
- 默认只要求控件与窗口存在可见交集；需要完整可见时设置
  `shape_marqueeRequireFullyVisible="true"`。
- Marquee 内部为启动滚动而使用的 `selected=true` 与业务 selected 状态隔离。

因此，Marquee 不会触发 `shape_textSelectedColor`、`shape_solidSelectedColor`、
`shape_selectedTint` 或 selected state text。业务代码调用 `setSelected(true)` 时，
selected 状态仍然正常生效。

需要业务选中状态与父容器同步时，可以使用：

```xml
android:duplicateParentState="true"
```

### ShapeEditText

输入框增强默认关闭，通过以下属性开启：

```xml
<com.allynav.shape.view.ShapeEditText
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:inputType="numberDecimal"
    android:text="2.0"
    app:shape_closeKeyboardEnable="true" />
```

`shape_closeKeyboardEnable="true"` 提供：

- IME Done/Go/Enter action 收起 keyboard
- hardware Enter 收起 keyboard
- 获得 focus 时全选文本
- 失去 focus 时隐藏 cursor 并清除 selection
- 用户编辑内容同步为 `TextStateDelegate` 的 default text

动态开关：

```java
shapeEditText.setCloseKeyboardEnabled(true);
shapeEditText.closeKeyboard();
```

### CheckBox and RadioButton icons

`ShapeCheckBox` 和 `ShapeRadioButton` 支持各状态 button Drawable：

| Attribute | Description |
| --- | --- |
| `shape_buttonDrawable` | 默认按钮图标 |
| `shape_buttonPressedDrawable` | pressed 状态图标 |
| `shape_buttonCheckedDrawable` | checked 状态图标 |
| `shape_buttonDisabledDrawable` | disabled 状态图标 |
| `shape_buttonFocusedDrawable` | focused 状态图标 |
| `shape_buttonSelectedDrawable` | selected 状态图标 |

## Image Components

### ShapeTextView compound Drawable tint

`ShapeTextView` 支持对 `android:drawableStart`、`drawableTop`、`drawableEnd` 和
`drawableBottom` 统一设置 state tint。Android `TextView` 只提供一份 compound drawable
tint，因此四个方向共用同一组状态颜色。

| Attribute | Description |
| --- | --- |
| `shape_enableTint` | enabled 普通 tint；未配置时保留图片自身颜色 |
| `shape_pressedTint` | pressed tint |
| `shape_checkedTint` | checked tint |
| `shape_disableTint` | disabled tint |
| `shape_focusedTint` | focused tint |
| `shape_selectedTint` | selected tint |
| `shape_tint` | `shape_enableTint` 的兼容名称 |
| `shape_disabledTint` | `shape_disableTint` 的兼容名称 |

`shape_checkedTint` 只有在宿主控件能够产生 `checked` state 时才会匹配；普通
`ShapeTextView` 本身没有 checked state。

```xml
<com.allynav.shape.view.ShapeTextView
    android:layout_width="120dp"
    android:layout_height="38dp"
    android:clickable="true"
    android:drawableStart="@mipmap/obstacles_points"
    android:text="障碍点"
    app:shape_pressedTint="#FF00C853"
    app:shape_disableTint="#FF808080" />
```

没有配置 `shape_enableTint` 时，普通状态会显示 `@mipmap/obstacles_points` 的原始颜色。
需要普通状态也统一为白色时，添加 `app:shape_enableTint="#FFFFFFFF"`。新名称与兼容
名称同时配置时，新名称优先。

```java
shapeTextView.getCompoundDrawableTintBuilder()
        .setPressedTintColor(0xFF00C853)
        .setDisableTintColor(0xFF808080)
        .intoTint();
```

### ShapeImageView state tint

`ShapeImageView` 的 image tint 不会改变 Shape background、shadow 或 corner radius：

| Attribute | Description |
| --- | --- |
| `shape_enableTint` | enabled 普通 tint；未配置时保留图片自身颜色 |
| `shape_pressedTint` | pressed tint |
| `shape_checkedTint` | checked tint |
| `shape_disableTint` | disabled tint |
| `shape_focusedTint` | focused tint |
| `shape_selectedTint` | selected tint |
| `shape_tint` | `shape_enableTint` 的兼容名称 |
| `shape_disabledTint` | `shape_disableTint` 的兼容名称 |

普通 `ShapeImageView` 没有 checked state，因此 `shape_checkedTint` 需要由实现
`Checkable` 的扩展控件使用；`pressed`、`disabled`、`focused` 和 `selected` 可以直接
通过对应的 Android state 触发。

```xml
<com.allynav.shape.view.ShapeImageView
    android:layout_width="48dp"
    android:layout_height="48dp"
    android:src="@drawable/ic_location"
    app:shape_enableTint="#FF607D8B"
    app:shape_pressedTint="#FF1565C0"
    app:shape_selectedTint="#FF2E7D32"
    app:shape_disableTint="#FFBDBDBD" />
```

`ShapeImageView` 也会回退到 `android:tint`。如果没有任何 tint 配置，则保持
`android:src` 图片自身颜色。

### ShapeImageView state src

| Attribute | Description |
| --- | --- |
| `shape_src` | 默认图片；缺省时使用 `android:src` |
| `shape_pressedSrc` | pressed 状态图片 |
| `shape_checkedSrc` | checked 状态图片 |
| `shape_disabledSrc` | disabled 状态图片 |
| `shape_focusedSrc` | focused 状态图片 |
| `shape_selectedSrc` | selected 状态图片 |

```xml
<com.allynav.shape.view.ShapeImageView
    android:layout_width="48dp"
    android:layout_height="48dp"
    android:src="@drawable/ic_normal"
    app:shape_pressedSrc="@drawable/ic_pressed"
    app:shape_selectedSrc="@drawable/ic_selected"
    app:shape_pressedTint="#FF1565C0"
    app:shape_selectedTint="#FF2E7D32" />
```

普通 `ShapeImageView` 的 selected 状态通过 `setSelected(true)` 切换；`checked` src 只
在控件确实进入 checked DrawableState 时生效。按下状态需要控件可点击或配置
`android:clickable="true"`。

### Image blur shadow

`ShapeImageView` 可以参考 `BlurShadowImageView` 的使用场景，根据当前 src 和 tint 后的
Drawable 像素生成带图片主色的模糊投影。该功能默认关闭，与普通 `shape_shadowColor`
轮廓阴影相互独立。

| Attribute | Format | Default | Description |
| --- | --- | --- | --- |
| `shape_imageBlurShadowEnable` | boolean | `false` | 是否开启 image blur shadow |
| `shape_imageBlurShadowRadius` | dimension | `18dp` | blur radius |
| `shape_imageBlurShadowOffsetX` | dimension | `0dp` | 水平偏移 |
| `shape_imageBlurShadowOffsetY` | dimension | `0dp` | 垂直偏移 |
| `shape_imageBlurShadowAlpha` | float | `0.45` | alpha，范围 `0..1` |
| `shape_imageBlurShadowBitmapScale` | float | `0.18` | 低分辨率 cache scale，范围 `0.05..1` |
| `shape_imageBlurShadowImageScale` | float | `0.86` | 清晰图片居中缩放，范围 `0.1..1` |

```xml
<com.allynav.shape.view.ShapeImageView
    android:layout_width="180dp"
    android:layout_height="180dp"
    android:scaleType="centerCrop"
    android:src="@drawable/cover"
    app:shape_radius="16dp"
    app:shape_imageBlurShadowEnable="true"
    app:shape_imageBlurShadowRadius="20dp"
    app:shape_imageBlurShadowOffsetY="8dp"
    app:shape_imageBlurShadowAlpha="0.55"
    app:shape_imageBlurShadowImageScale="0.84" />
```

Java 动态配置使用独立 Builder。该 Builder 的尺寸参数为 px：

```java
shapeImageView.getImageBlurShadowBuilder()
        .setEnabled(true)
        .setRadius(dpToPx(20))
        .setOffsetY(dpToPx(8))
        .setAlpha(0.55f)
        .setImageScale(0.84f);
```

投影会在图片、tint、尺寸或 image matrix 变化时重建；控件离开 window 后会释放 bitmap。
投影仍受 View 自身边界裁剪，建议给控件留出足够空间，不要在大量 RecyclerView item 中
无节制开启。

## ShapeSwitchButton

`ShapeSwitchButton` 基于 AndroidX `SwitchCompat`，不是 `ShapeButton` 的子类。它保留
checked、拖动切换、animation、RTL、accessibility 和 state save 行为；轨道使用统一的
Shape background，thumb 由控件绘制。

```xml
<com.allynav.shape.view.ShapeSwitchButton
    android:id="@+id/switch_enabled"
    android:layout_width="58dp"
    android:layout_height="36dp"
    android:checked="true"
    app:shape_radius="18dp"
    app:shape_solidColor="#FFDDDDDD"
    app:shape_solidCheckedColor="#FF40B5FF"
    app:shape_switchThumbColor="#FFFFFFFF"
    app:shape_switchThumbCheckedColor="#FFFFFFFF"
    app:shape_switchAnimationEnable="true" />
```

轨道使用 `shape_solidColor` 表示关闭状态，`shape_solidCheckedColor` 表示开启状态。

| Attribute | Default | Description |
| --- | --- | --- |
| `shape_switchThumbColor` | `#FFFFFFFF` | 关闭状态 thumb color |
| `shape_switchThumbCheckedColor` | fallback | checked thumb color |
| `shape_switchThumbPressedColor` | fallback | pressed thumb color |
| `shape_switchThumbDisabledColor` | `#FFBDBDBD` | disabled thumb color |
| `shape_switchThumbInset` | `2dp` | thumb 相对 Drawable 边界的 inset |
| `shape_switchAnimationEnable` | `true` | 是否保留 checked position animation |

Java：

```java
ShapeSwitchButton switchButton = findViewById(R.id.switch_enabled);
switchButton.setOnCheckedChangeListener((button, checked) -> {
    // checked 表示开关的最终状态。
});
```

## ShapeScrollIndicator

`ShapeScrollIndicator` 是独立绘制的滚动指示器，支持 `ScrollView` 和
`HorizontalScrollView`。它通过 `ViewTreeObserver` 观察目标滚动，不会覆盖业务已有的
`setOnScrollChangeListener`。

```xml
<com.allynav.shape.view.ShapeScrollIndicator
    android:id="@+id/scroll_indicator"
    android:layout_width="4dp"
    android:layout_height="match_parent"
    app:shape_indicatorTrackColor="#331B1C21"
    app:shape_indicatorColor="#4DECF7FF"
    app:shape_indicatorLength="0.2"
    app:shape_indicatorAlwaysShow="false" />
```

```java
ShapeScrollIndicator indicator = findViewById(R.id.scroll_indicator);
indicator.bindScrollView(scrollView);

// 横向滚动：
// indicator.bindHorizontalScrollView(horizontalScrollView);

// 页面销毁或更换目标时解除监听并恢复目标 View 原来的 scrollbar 配置：
indicator.unbind();
```

如果业务已经自行注册滚动监听，可以在原 listener 中转发：

```java
indicator.bindScrollViewFromScrollListener(
        scrollView, scrollX, scrollY, oldScrollX, oldScrollY);
```

| Attribute | Format | Default | Description |
| --- | --- | --- | --- |
| `shape_indicatorOrientation` | `vertical` / `horizontal` | `vertical` | indicator direction |
| `shape_indicatorTrackColor` | color | `#331B1C21` | track color |
| `shape_indicatorColor` | color | `#4DECF7FF` | thumb color |
| `shape_indicatorLength` | float `0..1` | `0.2` | thumb 占 track 的比例 |
| `shape_indicatorTrackRound` | boolean | `true` | 是否使用 round track/thumb |
| `shape_indicatorAnimationDuration` | integer | `500` | fade duration，单位 ms |
| `shape_indicatorHideDelay` | integer | `1500` | 滚动停止后延迟隐藏时间，单位 ms |
| `shape_indicatorAlwaysShow` | boolean | `false` | 是否始终显示 |

Java API 还支持 `setProgressPercent(0..1)` 手动设置进度；`setProcessPrecent()` 是为
兼容 ShadowLayout 历史拼写保留的方法，其历史语义是设置 indicator length。

## ViewGroup Child Clipping

以下 Shape ViewGroup 会在绘制子 View 时使用与自身 Shape background 一致的圆角或椭圆
path 进行 clipping：

- `ShapeLinearLayout`
- `ShapeFrameLayout`
- `ShapeRelativeLayout`
- `ShapeConstraintLayout`
- `ShapeRecyclerView`
- `ShapeRadioGroup`

因此图片、视频、自定义绘制 View 和 RecyclerView item 不会从容器的 rounded corner 区域
溢出。阴影开启时，clip path 会扣除 shadow 占用的 inset。

该能力只限制子 View 的绘制区域，不改变子 View 的 measure、layout、click、touch 或
scroll event。`line` 和 `ring` 是线条/环形绘制模型，不作为承载子 View 的 rounded
corner container。

## Java Builder API

### ShapeDrawableBuilder

所有 Shape 控件都可以通过 `getShapeDrawableBuilder()` 获取：

| Category | Methods |
| --- | --- |
| Shape | `setType`、`setWidth`、`setHeight`、`setLineGravity` |
| Radius | `setRadius`、`setRadiusRelative`、`setTopRadius`、`setBottomRadius`、`setTopLeftRadius`、`setTopRightRadius`、`setBottomLeftRadius`、`setBottomRightRadius` |
| Fill state | `setSolidColor`、`setSolidPressedColor`、`setSolidCheckedColor`、`setSolidDisabledColor`、`setSolidFocusedColor`、`setSolidSelectedColor` |
| Fill gradient | `setSolidGradientColors`、`setSolidGradientOrientation`、`setSolidGradientAngle`、`setSolidGradientType`、`setSolidGradientCenterX`、`setSolidGradientCenterY`、`setSolidGradientRadius`、`clearSolidGradientColors` |
| Border | `setStrokeColor`、`setStrokePressedColor`、`setStrokeCheckedColor`、`setStrokeDisabledColor`、`setStrokeFocusedColor`、`setStrokeSelectedColor`、`setStrokeSize`、`setStrokeDashSize`、`setStrokeDashGap` |
| Border gradient | `setStrokeGradientColors`、`setStrokeGradientOrientation`、`setStrokeGradientAngle`、`clearStrokeGradientColors` |
| Ring | `setRingInnerRadiusSize`、`setRingInnerRadiusRatio`、`setRingThicknessSize`、`setRingThicknessRatio` |
| Shadow | `setShadowHidden`、`setShadowSize`、`setShadowColor`、`setShadowOffsetX`、`setShadowOffsetY`、`setShadowSpread`、`setShadowSymmetry`、`setShadowHiddenLeft`、`setShadowHiddenTop`、`setShadowHiddenRight`、`setShadowHiddenBottom`、`setShadowBitmapScale` |
| Ripple | `setRippleEnable`、`setRippleColor` |
| Click state | `setShapeClickable`、`setNonClickableBackgroundDrawable`、`setNonClickableBackgroundColor` |
| Custom background | `setBackgroundDrawable`、`setPressedBackgroundDrawable`、`setCheckedBackgroundDrawable`、`setDisabledBackgroundDrawable`、`setFocusedBackgroundDrawable`、`setSelectedBackgroundDrawable` |
| Apply | `intoBackground`、`clearBackground` |

典型的动态 shadow 配置：

```java
private int dpToPx(float value) {
    return Math.round(value * getResources().getDisplayMetrics().density);
}

shapeButton.getShapeDrawableBuilder()
        .setSolidColor(0xFFFFFFFF)
        .setTopRadius(dpToPx(18))
        .setBottomRadius(dpToPx(10))
        .setShadowSize(dpToPx(14))
        .setShadowColor(0x52000000)
        .setShadowOffsetY(dpToPx(5))
        .setShadowSpread(dpToPx(1))
        .setShadowSymmetry(false)
        .setShadowBitmapScale(0.5f)
        .setRippleEnable(true)
        .setRippleColor(0x1F000000)
        .intoBackground();
```

### TextColorBuilder and TextStateDelegate

```java
shapeTextView.getTextColorBuilder()
        .setTextColor(0xFFFFFFFF)
        .setTextPressedColor(0xFFE2E8F0)
        .setTextDisabledColor(0xFFF1F5F9)
        .setTextStrokeColor(0xFF000000)
        .setTextStrokeSize(dpToPx(1))
        .intoTextColor();

shapeTextView.getTextStateDelegate()
        .setPressedText("松开提交")
        .setDisabledText("暂不可用")
        .setFocusedText("继续提交");
```

`TextColorBuilder` 修改后必须调用 `intoTextColor()`；`TextStateDelegate` 的 state text
setter 会立即刷新，不需要额外调用 `into...()`。

### Image builders

```java
shapeImageView.getImageTintBuilder()
        .setEnableTintColor(0xFF607D8B)
        .setPressedTintColor(0xFF1565C0)
        .setSelectedTintColor(0xFF2E7D32)
        .setDisableTintColor(0xFFBDBDBD)
        .intoTint();

shapeImageView.getImageSourceBuilder()
        .setPressedSourceDrawable(AppCompatResources.getDrawable(
                this, R.drawable.ic_pressed))
        .setSelectedSourceDrawable(AppCompatResources.getDrawable(
                this, R.drawable.ic_selected))
        .intoSource();
```

`ImageTintBuilder` 修改后调用 `intoTint()`；`ImageSourceBuilder` 修改后调用
`intoSource()`。将某个 state setter 传入 `null`，可以移除该 state 配置并回退到默认行为。

### ButtonDrawableBuilder

只有 `ShapeCheckBox` 和 `ShapeRadioButton` 提供 `getButtonDrawableBuilder()`：

```java
shapeCheckBox.getButtonDrawableBuilder()
        .setButtonDrawable(AppCompatResources.getDrawable(
                this, R.drawable.ic_unchecked))
        .setButtonCheckedDrawable(AppCompatResources.getDrawable(
                this, R.drawable.ic_checked))
        .setButtonDisabledDrawable(AppCompatResources.getDrawable(
                this, R.drawable.ic_disabled))
        .intoButtonDrawable();
```

## State Priority

背景、文本颜色、文本内容、image tint、image src 和 button Drawable 使用统一的 state
匹配顺序：

1. disabled
2. pressed
3. checked
4. focused
5. selected
6. enabled/default fallback

未配置某个 state 时会回退到默认值；默认文本最终回退到 `android:text` 或最后一次
业务 `setText()` 的内容。ShapeTextView 的 Marquee internal selected 不会参与上述业务
selected 匹配。

## Compatibility Notes

### ShapeView compatibility

- XML 使用 `shape_*` 命名，Java 使用 ShapeView 风格的 `Builder`。
- 原有常用 ShapeView 控件类名和包名保持为 `com.allynav.shape...`。
- Builder 的尺寸参数是 px；XML `dimension` 推荐使用 dp。
- 每个 Builder 修改后都要调用对应的 `into...()`，否则只修改了内存配置，不会重建
  Drawable。

### ShadowLayout compatibility

阴影的核心效果、空间占位、offset、spread、按边隐藏和 bitmap cache 已集成，但不是对
ShadowLayout 的 class-level drop-in replacement。特别是：

- `shape_shadow*` 命名与 ShadowLayout 原有 `hl_*` 命名不同。
- 两者的 shadow 参数和边界计算模型不完全同义，不能按数值一一换算出完全相同的像素。
- 本库的 `ShapeScrollIndicator` 保留了 `setProcessPrecent()` 兼容入口，但推荐使用拼写
  正确的 `setProgressPercent()`。
- ShadowLayout 特有的 `clickable=false` 语义在本库通过独立的 `shape_clickable` 和
  `shape_nonClickableBackground` 表达，并且不会把控件错误地标记为 disabled。

### Theme and Material

本库不负责替换宿主工程的 theme，也不阻止 AppCompat/Material inflater 工作。普通
`Button` 被 Material inflater 替换为 `MaterialButton` 属于 theme/inflater 行为；如果
页面同时使用 Material 控件，请保证宿主 Activity/Dialog 使用对应的 Material theme，并
统一 AppCompat/Material 版本。

## License

本项目使用 Apache-2.0 License。项目中参考或适配的第三方内容及许可证见
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。

主要参考项目：

- [ShapeView](https://github.com/getActivity/ShapeView)
- [ShapeDrawable](https://github.com/getActivity/ShapeDrawable)
- [ShadowLayout](https://github.com/lihangleo2/ShadowLayout)
- [AdaptiveTextView](https://github.com/AndrewSuan/AdaptiveTextView)
- [XUI AutoFitTextView](https://github.com/xuexiangjys/XUI)
