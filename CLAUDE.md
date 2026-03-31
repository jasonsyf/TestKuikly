# CLAUDE.md

本文档面向 AI 助手和开发者，说明 TestKuikly 项目的组织结构、开发约定、编码规范和开发流程。

## 项目概述

TestKuikly 是基于 **Kuikly UI 框架**和 **Kotlin Multiplatform (KMP)** 的跨端应用项目，实现"一套代码，五端运行"（Android、iOS、HarmonyOS、H5、微信小程序）。项目完整实现了 WanAndroid 客户端功能。

**技术栈：**
- **UI 框架**: Kuikly UI 2.7.0
- **语言**: Kotlin 2.1.21
- **架构**: MVI (Model-View-Intent)
- **网络**: Ktor 3.1.2 + Ktorfit 2.5.1
- **数据库**: SQLDelight
- **构建工具**: Gradle (Kotlin DSL)

## 常用命令

### 构建命令

```bash
# Android
./gradlew :androidApp:assembleDebug

# iOS (仅 macOS) - 构建 XCFramework
./gradlew :shared:podDebugXCFramework

# H5/Web
./gradlew :h5App:jsBrowserDevelopmentRun   # 开发环境
./gradlew :h5App:jsBrowserProductionWebpack  # 生产环境

# 微信小程序 - 组合构建（推荐）
./gradlew :miniApp:jsMiniAppProductionWebpack    # 生产环境
./gradlew :miniApp:jsMiniAppDevelopmentWebpack    # 开发环境

# HarmonyOS
./gradlew :ohosApp:assembleHap
```

### 小程序构建详情

小程序需要同时构建共享业务模块和渲染引擎。使用组合命令：

```bash
./gradlew :miniApp:jsMiniAppProductionWebpack
```

此命令会：
1. 构建 `:shared:jsBrowserProductionWebpack` → `nativevue2.js`（业务代码）
2. 构建 `:miniApp:jsBrowserProductionWebpack` → `miniApp.js`（渲染引擎）
3. 自动向 `nativevue2.js` 注入 polyfill 代码
4. 将两者复制到 `miniApp/dist/` 目录

### 查看生成代码

```bash
# SQLDelight 生成的数据库接口
./gradlew :shared:generateCommonMainWanDbInterface

# KSP 生成的 Kuikly 核心入口
cat shared/build/generated/ksp/js/jsMain/kotlin/KuiklyCoreEntry.kt
```

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
        }
    }
}
```

**关键特性：**
- 使用 `launch()` 封装协程（由 BaseViewModel 提供）
- 使用 `reduce { copy(...) }` 更新状态
- 使用 `sendEffect()` 发送一次性事件（Toast、导航）

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

### 项目结构

```
TestKuikly/
├── shared/                    # ★ 核心共享模块 (KMP)
│   └── src/
│       ├── commonMain/        # 所有平台共享代码
│       ├── androidMain/       # Android 平台特定实现
│       ├── iosMain/           # iOS 平台特定实现
│       ├── jsMain/            # JS/Web/小程序 平台特定实现
│       ├── ohosCommonMain/    # HarmonyOS 通用实现
│       └── ohosArm64Main/    # HarmonyOS ARM64 特定实现
├── androidApp/                # Android 原生壳工程
├── iosApp/                    # iOS 原生壳工程
├── h5App/                     # H5/Web 平台
├── miniApp/                   # 微信小程序
│   └── dist/                  # ★ 小程序输出目录（手动维护）
├── ohosApp/                   # HarmonyOS 原生壳工程
├── buildSrc/                  # 构建脚本和版本管理
└── gradle/                    # Gradle wrapper
```

### Shared 模块布局

```
shared/src/commonMain/kotlin/com/syf/testkuikly/
├── base/                      # 基础组件
│   ├── BasePager.kt           # 所有页面的基类 (@Page → ComposeContainer)
│   ├── OpenWebDetail.kt       # expect fun: 平台页面跳转
│   └── BridgeModule.kt        # WebView 与原生代码交互桥接
├── data/                      # 数据层
│   ├── WanApi.kt              # API 接口定义 + 响应数据类
│   ├── WanApiServiceImpl.kt    # Ktorfit API 实现
│   ├── KtorfitInstance.kt     # HTTP 客户端单例
│   ├── BaseUrl.kt             # expect fun: 平台 Base URL
│   ├── WanRepository.kt       # 数据仓库（组合 API + 缓存）
│   ├── CacheRepository.kt     # 缓存接口
│   ├── SqlDelightCacheRepository.kt
│   └── MemoryCacheRepository.kt
├── home/                      # 首页模块（ViewModel + Page）
├── project/                   # 项目模块
├── square/                    # 广场模块
├── navigation/                # 导航模块
├── mine/                      # 我的模块
└── MainPage.kt                # 主入口页面 (@Page("main"))
```

## 跨平台实现

### expect/actual 模式

使用 Kotlin 的 expect/actual 机制实现平台特定代码：

```kotlin
// commonMain - 声明
expect fun openWebDetail(url: String, title: String)
expect fun getBaseUrl(): String
expect object DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

// jsMain - 小程序实现
actual fun openWebDetail(url: String, title: String) {
    js("wx.navigateTo({url: '/packageA/pages/webview/webview?url=' + encodeURIComponent(url)})")
}
actual fun getBaseUrl(): String = "https://www.wanandroid.com/"
```

**需要处理的关键平台差异：**
| 功能 | Android/iOS/Web | 小程序 |
|------|-----------------|--------------|
| 页面跳转 | `window.open()` 或原生 API | `wx.navigateTo()` |
| Base URL | 运行时配置 | jsMain 中静态字符串 |
| 网络请求 | Ktor HttpClient | Fetch polyfill (wx.request) |
| TextEncoder | 内置支持 | 需要 polyfill |

## 小程序开发要点

### 关键：初始化顺序

小程序需要**严格的初始化顺序**以避免错误：

```
app.js (主包，最先执行)
  ├─ 全局 polyfill (TextEncoder/TextDecoder)
  └─ 预加载 require('./lib/miniApp.js')
     └─ 缓存到 global.__kuiklyRender

main.js (分包，用户进入分包时执行)
  ├─ require('../../business/nativevue2.js')
  ├─ render = global.__kuiklyRender (使用缓存，避免跨包 require 异常)
  ├─ global.com = business.com (挂载业务入口)
  ├─ global.callKotlinMethod = business.callKotlinMethod
  ├─ render.initApp() (必须在业务代码加载之后)
  └─ render.renderView({ pageName: "main" })
```

### 为什么顺序很重要

1. **app.js 在主包预加载 miniApp.js**：小程序分包的 CommonJS 上下文与主包不同。UMD 模块（webpack 输出）需要完整的 `module.exports` 支持，跨包 require 时可能丢失。在主包预加载确保 UMD 正确初始化。

2. **initApp() 在 nativevue2.js 加载后调用**：渲染引擎需要 `global.com`（业务入口），该入口仅在 `nativevue2.js` 加载后才存在。

3. **使用缓存的 render**：从分包直接 require 可能失败，使用主包缓存的 `global.__kuiklyRender`。

### Polyfill 注入机制

`shared/build.gradle.kts` 在 webpack 构建后自动向 `nativevue2.js` 注入 polyfill：

- TextEncoder/TextDecoder（Ktor 需要）
- window.open → wx.navigateTo
- document mock（用于 webpack runtime）
- AbortController polyfill
- fetch polyfill（基于 wx.request）
- __decodeUTF8() 用于手动 UTF-8 解码

**重要说明**：polyfill 使用 `var` 声明（而非 `global.xxx`），使其可通过 webpack IIFE 闭包作用域链访问。

### DevTools 与真机环境差异

| API | DevTools | 真机 | 影响范围 |
|-----|----------|------|----------|
| TextEncoder/TextDecoder | ✅ | ❌ | Ktor HTTP、编码 |
| window | ✅ | ❌ | 页面跳转 |
| document | ✅ | ❌ | webpack runtime |
| fetch | ✅ | ❌ | HTTP 客户端 |
| AbortController | ✅ | ❌ | 请求取消 |
| global | ✅ | ✅ | 全局对象（小程序提供） |

**重要原则**：真机调试是必须环节，DevTools 通过 ≠ 真机通过。

### 已知真机问题

1. **网络响应乱码**：`wx.request` 使用 `responseType: 'text'` 在真机对 UTF-8 解码有 bug。必须使用 `'arraybuffer'` + 手动 UTF-8 解码。

2. **UMD 跨包 require**：从分包 require 主包模块可能返回空对象。在 `app.js`（主包）中预先缓存渲染引擎到 `global.__kuiklyRender`。

3. **模块路径解析**：页面文件中的路径相对于页面目录。对于 `packageA/pages/main/main.js`，使用 `../../../lib/miniApp.js`（而非 `../../`）。

4. **重复 Page 注册**：不要手动调用 `Page({...})`。`render.renderView()` 会处理注册。

5. **TextEncoder 作用域**：必须使用 `var TextEncoder`（文件作用域），而非 `global.TextEncoder`，以便 webpack IIFE 闭包访问。

### Webpack 配置

小程序需要特定的 webpack 配置，位于 `miniApp/build.gradle.kts`：

```kotlin
webpackTask {
    outputFileName = "miniApp.js"
    webpackConfigApplier {
        // target='node' 是微信小程序的必需配置
        // Kotlin/JS IR 使用 'global' 作为全局对象
        // 小程序提供 'global' 但不提供 'window'
        // 不得改为 'web'，否则 webpack 会将 global 映射到不存在的 window
        config.target = 'node';
    }
}
```

### 页面注册

1. 使用 `@Page("pageName", supportInLocal = true)` 注解注册页面
2. 所有页面继承 `BasePager`（它继承 Kuikly 的 `ComposeContainer`）
3. 在 `willInit()` 中调用 `setContent { ... }` 设置 Compose UI
4. 无需手动创建 wxml/json/js 文件 — KSP 编译器自动生成

```kotlin
@Page("main", supportInLocal = true)
internal class MainPage : BasePager() {
    override fun willInit() {
        super.willInit()
        setContent { MainContent() }
    }
}
```

## 编码规范

### 包结构约定

```
com.syf.testkuikly/
├── home/              # 首页功能模块
│   ├── HomeViewModel.kt
│   └── HomePage.kt
├── project/           # 项目模块
├── square/            # 广场模块
├── navigation/        # 导航模块
├── mine/             # 我的模块
├── data/             # 数据层
│   ├── WanRepository.kt
│   ├── WanApi.kt
│   ├── ApiModels.kt
│   └── CacheRepository.kt
└── base/             # 基础组件
    ├── mvi/
    ├── Utils.kt
    └── BasePager.kt
```

### 命名规范

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| 类/接口 | 大驼峰 (PascalCase) | `HomeViewModel` |
| 函数/属性 | 小驼峰 (camelCase) | `loadInitial()` |
| 常量 | 全大写下划线分隔 | `DEFAULT_PAGE_SIZE` |
| 密封类成员 | 小驼峰 + object | `data object LoadInitial` |

### 协程使用规范

```kotlin
// 使用 launch() 封装协程（由 BaseViewModel 提供）
private fun loadData() {
    launch {
        // 在 Dispatchers.Default 中执行
        val result = repository.getData()
        reduce { copy(data = result) }
    }
}

// 避免直接使用 CoroutineScope
```

### 状态更新规范

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

### 错误处理

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

## 版本管理

版本号集中在 `buildSrc/src/main/java/KotlinBuildVar.kt`：

```kotlin
object Version {
    private const val KUIKLY_VERSION = "2.7.0"
    private const val KOTLIN_VERSION = "2.1.21"

    fun getKuiklyVersion(): String = "$KUIKLY_VERSION-$KOTLIN_VERSION"  // "2.7.0-2.1.21"
}
```

## Git 工作流

基于项目历史，提交信息使用中文：

```bash
git commit -m "feat: 添加文章收藏功能"
git commit -m "fix: 修复加载失败问题"
git commit -m "docs: 更新 README 文档"
git commit -m "perf: 优化列表滚动性能"
```

- `master`: 主分支，保持稳定
- 功能分支: 从 master 分出，完成后合并回 master

## 开发流程

### 新增功能

1. **在 `commonMain` 中定义数据模型** (`ApiModels.kt`)
2. **创建 API 接口** (`WanApi.kt`)
3. **实现 Repository** (`WanRepository.kt`)
4. **定义 ViewModel** (继承 `BaseViewModel`)
5. **创建 UI Compose 组件** (`HomePage.kt`)
6. **平台特定实现** (如需要，在 `androidMain`/`iosMain`/`jsMain` 等)

### 新增页面

1. **Kotlin 层**: 在 `shared/src/commonMain/` 对应模块目录创建页面类
2. **平台适配** (如需要): 在对应平台源码集写 `actual` 实现
3. **小程序端**: 无需手动创建 wxml/json/js 文件 — KSP 自动生成
4. **构建**: 运行 `./gradlew :miniApp:jsMiniAppProductionWebpack`

## 注意事项

1. **缓存策略**: 优先使用缓存提升体验，仅必要时强制刷新
2. **错误处理**: 非关键错误静默处理，关键错误通过 Effect 通知
3. **内存管理**: 避免在 Flow 中持有大对象，使用合理的作用域
4. **平台差异**: 充分利用 expect/actual 机制处理平台特定逻辑
5. **版本同步**: 修改 `KotlinBuildVar.kt` 时注意 Kuikly 版本号规则
6. **小程序测试**: 务必在真机上测试 — DevTools 行为与真机差异显著

## 相关文档

- [README.md](./README.md) - 项目介绍和快速开始
- [Kuikly UI 文档](https://github.com/Tencent/Kuikly) - Kuikly 框架文档
- [WanAndroid API](https://www.wanandroid.com/) - API 接口文档
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) - KMP 官方文档
- [docs/项目开发手册.md](./docs/项目开发手册.md) - 详细的中文开发手册，包含真机调试问题全记录
