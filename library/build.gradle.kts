plugins {
    id("com.android.library")
    id("maven-publish")
}

android {
    namespace = "com.allynav.shape"
    compileSdk = 35

    defaultConfig {
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
                version = "1.1.0"
                from(components["release"])
            }
        }
    }
}

dependencies {
    api("androidx.appcompat:appcompat:1.7.1")
    api("androidx.constraintlayout:constraintlayout:2.2.1")
    api("androidx.recyclerview:recyclerview:1.4.0")
}
