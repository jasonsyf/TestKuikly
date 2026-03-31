# TestKuikly 项目开发指南

本文档面向 AI 助手和开发者，说明 TestKuikly 项目的组织结构、开发约定、编码规范和开发流程。

## 项目概述

TestKuikly 是基于 **Kuikly UI 框架**和 **Kotlin Multiplatform (KMP)** 的跨端应用项目，实现"一套代码，五端运行"（Android、iOS、HarmonyOS、H5、小程序）。项目完整实现了 WanAndroid 客户端功能。

**技术栈：**
- **UI 框架**: Kuikly UI 2.7.0
- **语言**: Kotlin 2.1.21
- **架构**: MVI (Model-View-Intent)
- **网络**: Ktor 3.1.2 + Ktorfit 2.5.1
- **数据库**: SQLDelight
- **构建工具**: Gradle (Kotlin DSL)

## 项目结构

```
TestKuikly/
├── androidApp/          # Android 平台实现
│   └── src/main/
│       └── AndroidManifest.kt
├── iosApp/              # iOS 平台实现
│   ├── iosApp/
│   ├── Podfile
│   └── *.swift
├── ohosApp/             # HarmonyOS 平台实现
│   ├── AppScope/
│   ├── entry/
│   └── hvigorfile.ts
├── h5App/               # H5/Web 平台实现
│   ├── src/
│   └── webpack.config.d/
├── miniApp/             # 微信小程序平台实现
│   ├── src/
│   └── project.config.json
├── shared/              # 共享代码模块（核心）
│   ├── src/
│   │   ├── commonMain/          # 所有平台共享代码
│   │   │   └── kotlin/com/syf/testkuikly/
│   │   │       ├── home/        # 首页模块
│   │   │       ├── project/     # 项目模块
│   │   │       ├── square/      # 广场模块
│   │   │       ├── mine/        # 我的模块
│   │   │       ├── navigation/  # 导航模块
│   │   │       ├── data/        # 数据层
│   │   │       └── base/        # 基础框架
│   │   ├── androidMain/         # Android 特定实现
│   │   ├── iosMain/             # iOS 特定实现
│   │   ├── jsMain/              # JS/H5 特定实现
│   │   ├── ohosCommonMain/      # HarmonyOS 通用实现
│   │   └── ohosArm64Main/       # HarmonyOS ARM64 特定实现
│   └── build.gradle.kts
├── buildSrc/            # 构建脚本和版本管理
│   └── src/main/java/KotlinBuildVar.kt
└── gradle/              # Gradle wrapper
```

### 模块职责

| 模块 | 职责 |
|------|------|
| `shared/commonMain` | 跨平台业务逻辑、UI 组件、数据模型 |
| `shared/androidMain` | Android 平台特定实现（如 SQLite Driver） |
| `shared/iosMain` | iOS 平台特定实现（如 Darwin Driver） |
| `shared/jsMain` | JS 平台特定实现（LocalStorage 缓存等） |
| `shared/ohosCommonMain` | HarmonyOS 通用实现 |
| `shared/ohosArm64Main` | HarmonyOS ARM64 特定实现 |

## 架构设计

### MVI 架构模式

项目采用 **MVI (Model-View-Intent)** 架构模式，通过 `BaseViewModel` 实现：

```kotlin
// 定义 Intent（用户意图）
sealed class HomeIntent : MviIntent {
    data object LoadInitial : HomeIntent()
    data object Refresh : HomeIntent()
    data object LoadMore : HomeIntent()
}

// 定义 State（UI 状态）
data class HomeState(
    val banners: List<BannerItem> = emptyList(),
    val articles: List<Article> = emptyList(),
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true
) : MviState

// 定义 Effect（一次性事件）
sealed class HomeEffect : MviEffect {
    data object ShowError : HomeEffect()
}

// ViewModel 实现
class HomeViewModel : BaseViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {
    override fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadInitial -> loadInitial()
            // ...
        }
    }
}
```

**关键特性：**
- `StateFlow` 用于 UI 状态管理
- `SharedFlow` 用于一次性事件（如 Toast、导航）
- `launch` 封装协程作用域

### 数据层架构

**Repository 模式 + 缓存优先策略：**

```kotlin
class WanRepository {
    private val api = KtorfitInstance.api
    private val cache: CacheRepository by lazy { createCacheRepository() }

    // 缓存优先：先返回缓存，后台刷新
    fun getBanners(): Flow<List<BannerItem>> = cacheFirst(
        cached = { cache.getCachedBanners() },
        network = { api.getBanners().data ?: emptyList() },
        save = { cache.saveBanners(it) },
        isEmpty = { it.isEmpty() }
    )
}
```

**缓存策略：**
- `cacheFirst()`: 立即返回缓存，后台请求并更新
- `cacheFirstNullable()`: 仅在有缓存时返回，否则仅网络请求
- `networkOnly()`: 仅网络请求（不使用缓存）

## 跨平台实现模式

### expect/actual 模式

使用 Kotlin 的 expect/actual 机制实现平台特定代码：

```kotlin
// commonMain - 声明
expect object DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

// androidMain - Android 实现
actual object DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver = AndroidSqliteDriver(WanDb.Schema, context, "wan.db")
}

// iosMain - iOS 实现
actual object DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver = NativeSqliteDriver(WanDb.Schema, "wan.db")
}
```

### 平台桥接

**Bridge Module**: 用于 WebView 与原生代码的交互

```kotlin
class BridgeModule {
    fun openWebDetail(url: String) {
        // 平台特定实现
    }
}
```

## 编码规范

### 1. 包结构约定

```
com.syf.testkuikly/
├── home/              # 首页功能模块
│   ├── HomeViewModel.kt
│   └── HomePage.kt
├── project/           # 项目模块
│   ├── ProjectViewModel.kt
│   └── ProjectPage.kt
├── data/              # 数据层
│   ├── WanRepository.kt
│   ├── WanApi.kt
│   ├── ApiModels.kt
│   └── CacheRepository.kt
└── base/              # 基础组件
    ├── mvi/
    ├── Utils.kt
    └── BasePager.kt
```

### 2. 命名规范

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| 类/接口 | 大驼峰 (PascalCase) | `HomeViewModel` |
| 函数/属性 | 小驼峰 (camelCase) | `loadInitial()` |
| 常量 | 全大写下划线分隔 | `DEFAULT_PAGE_SIZE` |
| 密封类成员 | 小驼峰 + object | `data object LoadInitial` |

### 3. 协程使用规范

```kotlin
// 使用 launch() 封装协程
private fun loadData() {
    launch {
        // 在 Dispatchers.Default 中执行
        val result = repository.getData()
        reduce { copy(data = result) }
    }
}

// 避免直接使用 CoroutineScope
```

### 4. 状态更新规范

```kotlin
// 使用 reduce() 更新状态
reduce { copy(isLoading = true) }

// 组合更新
reduce {
    copy(
        articles = articles + newItems,
        currentPage = page + 1,
        isLoadingMore = false
    )
}
```

### 5. 错误处理

```kotlin
// 静默处理非关键错误
try {
    val data = api.getData()
    reduce { copy(data = data) }
} catch (_: Exception) {
    // 不处理，依赖缓存或保持当前状态
}

// 关键错误发送 Effect
try {
    // ...
} catch (e: Exception) {
    sendEffect(HomeEffect.ShowError)
}
```

## 构建配置

### 版本管理

版本号集中在 `buildSrc/src/main/java/KotlinBuildVar.kt`：

```kotlin
object Version {
    private const val KUIKLY_VERSION = "2.7.0"
    private const val KOTLIN_VERSION = "2.1.21"

    fun getKuiklyVersion(): String = "$KUIKLY_VERSION-$KOTLIN_VERSION"
}
```

### 依赖管理

```kotlin
// shared/build.gradle.kts
val ktorVersion = "3.1.2"
val ktorfitVersion = "2.5.1"

dependencies {
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("de.jensklingenberg.ktorfit:ktorfit-lib:$ktorfitVersion")
}
```

### 平台构建

```bash
# Android
./gradlew :androidApp:assembleDebug

# iOS (需要 macOS)
./gradlew :shared:podDebugXCFramework

# H5
./gradlew :h5App:jsBrowserDevelopmentRun

# 小程序
./gradlew :miniApp:jsBrowserDevelopmentRun

# HarmonyOS
./gradlew :ohosApp:assembleHap
```

## 开发流程

### 1. 功能开发流程

1. **在 `commonMain` 中定义数据模型** (`ApiModels.kt`)
2. **创建 API 接口** (`WanApi.kt`)
3. **实现 Repository** (`WanRepository.kt`)
4. **定义 ViewModel** (继承 `BaseViewModel`)
5. **创建 UI Compose 组件** (`HomePage.kt`)
6. **平台特定实现** (如需要，在 `androidMain`/`iosMain` 等)

### 2. Git 提交规范

基于项目历史，提交信息使用中文：

```bash
# 格式: [类型]: 简短描述
git commit -m "feat: 添加文章收藏功能"
git commit -m "fix: 修复加载失败问题"
git commit -m "docs: 更新 README 文档"
git commit -m "perf: 优化列表滚动性能"
```

### 3. 分支策略

- `master`: 主分支，保持稳定
- 功能分支: 从 master 分出，完成后合并回 master

## 特殊约定

### 1. 微信小程序兼容性

项目在构建时自动注入 polyfill 代码以支持微信小程序环境：

- `window.open` → `wx.navigateTo()`
- `TextEncoder/TextDecoder` polyfill
- `document` mock
- `AbortController` polyfill
- `fetch` 基于 `wx.request`

### 2. HarmonyOS 特定

- 使用 `ohosCommonMain` 和 `ohosArm64Main` 进行分层实现
- 网络层使用 HarmonyOS 原生实现

### 3. H5/Web

- Webpack 配置位于 `h5App/webpack.config.d/`
- 输出文件名: `nativevue2.js`

## 调试技巧

### 1. 查看生成的代码

```bash
# SQLDelight 生成的数据库代码
./gradlew :shared:generateCommonMainWanDbInterface

# KSP 生成的 Kuikly 核心入口
cat shared/build/generated/ksp/js/jsMain/kotlin/KuiklyCoreEntry.kt
```

### 2. 平台特定调试

- **Android**: 使用 Android Studio Logcat
- **iOS**: 使用 Xcode 控制台
- **H5/小程序**: 使用浏览器开发者工具
- **HarmonyOS**: 使用 DevEco Studio

## 注意事项

1. **缓存策略**: 优先使用缓存提升体验，仅必要时强制刷新
2. **错误处理**: 非关键错误静默处理，关键错误通过 Effect 通知
3. **内存管理**: 避免在 Flow 中持有大对象，使用合理的作用域
4. **平台差异**: 充分利用 expect/actual 机制处理平台特定逻辑
5. **版本同步**: 修改 `KotlinBuildVar.kt` 时注意 Kuikly 版本号规则

## 相关文档

- [README.md](./README.md) - 项目介绍和快速开始
- [Kuikly UI 文档](https://github.com/Tencent/Kuikly) - Kuikly 框架文档
- [WanAndroid API](https://www.wanandroid.com/) - API 接口文档
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) - KMP 官方文档

---

本文档随项目演进持续更新。
