// Android Library 模块：提供 ShapeView 风格控件，并通过 Maven Publish 供 JitPack 发布。
plugins {
    id("com.android.library")
    id("maven-publish")
}

android {
    // namespace 也是资源 R 类和公开控件包的基础命名空间。
    namespace = "com.allynav.shape"
    compileSdk = 35

    defaultConfig {
        // minSdk 21 覆盖 RippleDrawable 和当前 AppCompat 依赖所需的最低平台。
        minSdk = 21
        // 为库模块的 Android instrumented regression tests 指定标准 AndroidX 执行器。
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    publishing {
        // 发布源码包，方便使用方在 IDE 中直接查看控件实现。
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "com.github.anyeshitu"
                artifactId = "ShapeShadowView"
                version = "1.1.11"
                from(components["release"])
            }
        }
    }
}

dependencies {
    // Shape 控件公开继承 AppCompat 控件，因此必须暴露该依赖；1.6.1 兼容更多旧项目，
    // 避免库发布后无意把宿主工程的 AppCompat 解析到 1.7.1。
    api("androidx.appcompat:appcompat:1.6.1")
    api("androidx.constraintlayout:constraintlayout:2.2.1")
    api("androidx.recyclerview:recyclerview:1.4.0")

    // 回归用例需要真实 Android TextView/Editable 生命周期，放在 androidTest 中运行。
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:core:1.6.1")
    // AndroidJUnitRunner 不由所有 AndroidX Test 扩展包直接传递，显式声明避免执行器缺失。
    androidTestImplementation("androidx.test:runner:1.6.2")
}
