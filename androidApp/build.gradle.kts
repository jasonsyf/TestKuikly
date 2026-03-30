plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.syf.testkuikly"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.syf.testkuikly"
        minSdk = 23
        targetSdk = 30
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":shared")) {
        // Ktor 3.x 传递依赖 slf4j-api 2.x，AGP 7.4.2 的 D8 无法 dex
        exclude(group = "org.slf4j", module = "slf4j-api")
    }

    // 提供 slf4j-api 1.7.x（AGP 7.4.2 兼容）
    implementation("org.slf4j:slf4j-api:1.7.36")

    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.appcompat:appcompat:1.3.1")

    implementation("com.squareup.picasso:picasso:2.71828")

    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
}