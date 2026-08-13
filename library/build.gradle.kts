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
                version = "1.1.4"
                from(components["release"])
            }
        }
    }
}

dependencies {
    // 使用 api 暴露父控件类型，消费方 XML 和 Java/Kotlin 可直接引用这些 AndroidX 类。
    api("androidx.appcompat:appcompat:1.7.1")
    api("androidx.constraintlayout:constraintlayout:2.2.1")
    api("androidx.recyclerview:recyclerview:1.4.0")
}
