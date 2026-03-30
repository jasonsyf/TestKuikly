import java.nio.file.Paths

plugins {
    // Import KMM plugin
    kotlin("multiplatform")
}

kotlin {
    // Build JS output for miniApp
    js(IR) {
        // Build output supports browser
        browser {
            webpackTask {
                // Final output executable JS filename
                outputFileName = "miniApp.js"
                webpackConfigApplier {
                    val tempConfigFile = File(project.buildDir, "../webpack.config.d/config.js")
                    tempConfigFile.parentFile.mkdirs()
                    // target='node' 是 Kuikly 框架在 WeChat miniapp 环境下的必要配置：
                    // Kotlin/JS IR 编译产物使用 global 作为全局对象。
                    // 小程序真机提供了 global，但没有 window；
                    // target='node' 让 webpack 不对 global 做 polyfill，直接使用小程序的 global。
                    // 不得将此改为 'web'，否则 webpack 会把 global 映射为不存在的 window。
                    tempConfigFile.writeText(
                        """
                        config.target = 'node';
                        """.trimIndent()
                    )
                    file(tempConfigFile.absolutePath)
                }
            }

            commonWebpackConfig {
                // Do not export global objects, only export necessary entry methods
                output?.library = null
                // use next line to enable source map
                // devtool = org.jetbrains.kotlin.gradle.targets.js.webpack.WebpackDevtool.INLINE_CHEAP_SOURCE_MAP
                devtool = null
            }
        }
        // Package render code and miniApp code together and execute directly
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                // Import web render
                implementation("com.tencent.kuikly-open.core-render-web:base:${Version.getKuiklyVersion()}")
                implementation("com.tencent.kuikly-open.core-render-web:miniapp:${Version.getKuiklyVersion()}")
            }
        }
    }
}

// Business project path name
val businessPathName = "shared"

/**
 * Copy locally built unified JS result to miniApp's subpackage business directory
 */
fun copyLocalJSBundle(buildSubPath: String) {
    // Output target path: subpackage directory to keep main package < 2MB
    val destDir = Paths.get(
        project.buildDir.absolutePath, "../",
        "dist", "packageA", "business"
    ).toFile()
    if (!destDir.exists()) {
        destDir.mkdirs()
    } else {
        destDir.deleteRecursively()
    }

    // Kotlin/JS IR: development → kotlin-webpack/js/developmentExecutable
    //                   production → kotlin-webpack/js/productionExecutable
    val sourceDir = Paths.get(
        project.rootDir.absolutePath,
        businessPathName,
        "build/kotlin-webpack/js", buildSubPath
    ).toFile()

    // Copy files
    project.copy {
        // Copy js files from business build result (main bundle + chunks)
        from(sourceDir) {
            include("nativevue2*.js")
        }
        into(destDir)
    }
}

project.afterEvaluate {
    // kotlin 1.9: production output path is kotlin-webpack/js/productionExecutable
    tasks.register<Copy>("syncRenderProductionToDist") {
        from("$buildDir/kotlin-webpack/js/productionExecutable")
        into("$projectDir/dist/lib")
        include("**/*.js", "**/*.d.ts")
    }

    // kotlin 1.9 from 改为 $buildDir/dist/js/developmentExecutable
    tasks.register<Copy>("syncRenderDevelopmentToDist") {
        from("$buildDir/kotlin-webpack/js/developmentExecutable")
        into("$projectDir/dist/lib")
        include("**/*.js", "**/*.d.ts")
    }

    tasks.register<Copy>("copyAssets") {
        val assetsDir = Paths.get(
            project.rootDir.absolutePath,
            businessPathName,
            "src/commonMain/assets"
        ).toFile()
        from(assetsDir)
        into("$projectDir/dist/assets")
        include("**/**")
    }

    tasks.named("jsBrowserProductionWebpack") {
        finalizedBy("syncRenderProductionToDist")
    }

    tasks.named("jsBrowserDevelopmentWebpack") {
        finalizedBy("syncRenderDevelopmentToDist")
    }

    tasks.register("jsMiniAppProductionWebpack") {
        group = "kuikly"
        // First execute shared and miniApp production webpack build tasks
        dependsOn(":shared:jsBrowserProductionWebpack", "jsBrowserProductionWebpack")
        // Then copy nativevue2.js from shared production build result to miniApp's dist/business
        doLast {
            copyLocalJSBundle("productionExecutable")
        }
    }

    tasks.register("jsMiniAppDevelopmentWebpack") {
        group = "kuikly"
        // First execute shared and miniApp development webpack build tasks
        dependsOn(":shared:jsBrowserDevelopmentWebpack", "jsBrowserDevelopmentWebpack")
        // Then copy nativevue2.js from shared development build result to miniApp's dist/business
        doLast {
            copyLocalJSBundle("developmentExecutable")
        }
    }
}