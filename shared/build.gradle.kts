import com.tencent.kuikly.gradle.config.KuiklyConfig
import app.cash.sqldelight.gradle.SqlDelightExtension

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("maven-publish")
    id("com.tencent.kuikly-open.kuikly")
    id("org.jetbrains.compose")
    kotlin("plugin.compose")
    kotlin("plugin.serialization")
    alias(libs.plugins.sqldelight)
}

val ktorVersion = "3.1.2"
val ktorfitVersion = "2.5.1"

val KEY_PAGE_NAME = "pageName"

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }

    js(IR) {
        browser {
            webpackTask {
                mainOutputFileName.set("nativevue2.js")
                
                // 明确设置 publicPath，避免 webpack runtime 自动检测（解决小程序中 document 不存在的问题）
                webpackConfigApplier {
                    val tempConfigFile = File(project.buildDir, "../webpack.config.d/shared-config.js")
                    tempConfigFile.parentFile.mkdirs()
                    tempConfigFile.writeText("""
                        config.output.publicPath = '';
                    """.trimIndent())
                    file(tempConfigFile.absolutePath)
                }
            }

            commonWebpackConfig {
                output?.library = null // 不导出全局对象，只导出必要的入口函数
                devtool = "source-map" // 不使用默认的 eval 执行方式构建出 source-map，而是构建单独的 sourceMap 文件
            }
        }
        binaries.executable() //将kotlin.js与kotlin代码打包成一份可直接运行的js文件
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            freeCompilerArgs = freeCompilerArgs + getCommonCompilerArgs()
            isStatic = true
            license = "MIT"
        }
        extraSpecAttributes["resources"] = "['src/commonMain/assets/**']"
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.tencent.kuikly-open:core:${Version.getKuiklyVersion()}")
                implementation("com.tencent.kuikly-open:core-annotations:${Version.getKuiklyVersion()}")
                implementation("com.tencent.kuikly-open:compose:${Version.getKuiklyVersion()}")

                implementation("de.jensklingenberg.ktorfit:ktorfit-lib:$ktorfitVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

                implementation("app.cash.sqldelight:runtime:${libs.versions.sqldelight.get()}")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                api("com.tencent.kuikly-open:core-render-android:${Version.getKuiklyVersion()}")
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
                implementation("app.cash.sqldelight:android-driver:${libs.versions.sqldelight.get()}")
            }
        }
        val jsMain by getting

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
                implementation("app.cash.sqldelight:native-driver:${libs.versions.sqldelight.get()}")
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

group = "com.syf.testkuikly"
version = System.getenv("kuiklyBizVersion") ?: "1.0.0"

publishing {
    repositories {
        maven {
            credentials {
                username = System.getenv("mavenUserName") ?: ""
                password = System.getenv("mavenPassword") ?: ""
            }
            rootProject.properties["mavenUr?"]?.toString()?.let { url = uri(it) }
        }
    }
}

ksp {
    arg(KEY_PAGE_NAME, getPageName())
}

dependencies {
    compileOnly("com.tencent.kuikly-open:core-ksp:${Version.getKuiklyVersion()}") {
        add("kspAndroid", this)
        add("kspIosArm64", this)
        add("kspIosX64", this)
        add("kspIosSimulatorArm64", this)
        add("kspJs", this)
    }
}

android {
    namespace = "com.syf.testkuikly.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
    sourceSets {
        named("main") {
            assets.srcDirs("src/commonMain/assets")
        }
    }
}

fun getPageName(): String {
    return (project.properties[KEY_PAGE_NAME] as? String) ?: ""
}

fun getCommonCompilerArgs(): List<String> {
    return listOf(
        "-Xallocator=std"
    )
}

fun getLinkerArgs(): List<String> {
    return listOf()
}

// SQLDelight 配置
sqldelight {
    databases {
        create("WanDb") {
            packageName.set("com.syf.testkuikly.data")
        }
    }
}

// Kuikly 插件配置
configure<KuiklyConfig> {
    // JS 产物配置
    js {
        // 构建产物名，与 KMM 插件 webpackTask#outputFileName 一致
        outputName("nativevue2")
        // 可选：分包构建时的页面列表，如果为空则构建全部页面
        // addSplitPage("route","home")
    }
}

// 注入 document mock 以支持微信小程序环境
fun injectDocumentMock(jsFile: File) {
    val mockCode = """
// window.open polyfill for WeChat Mini Program (navigate to webview page)
if (typeof window === 'undefined') {
    globalThis.window = {
        open: function(url) {
            wx.navigateTo({ url: '/packageA/pages/webview/webview?url=' + encodeURIComponent(url) });
        }
    };
}

// TextEncoder / TextDecoder polyfill for WeChat Mini Program (required by Ktor)
if (typeof TextEncoder === 'undefined') {
    globalThis.TextEncoder = function TextEncoder() { this.encoding = 'utf-8'; };
    globalThis.TextEncoder.prototype.encode = function(str) {
        var utf8 = [];
        for (var i = 0; i < str.length; i++) {
            var code = str.charCodeAt(i);
            if (code < 0x80) {
                utf8.push(code);
            } else if (code < 0x800) {
                utf8.push(0xC0 | (code >> 6), 0x80 | (code & 0x3F));
            } else if (code >= 0xD800 && code <= 0xDBFF) {
                var hi = code, lo = str.charCodeAt(++i);
                if (lo >= 0xDC00 && lo <= 0xDFFF) {
                    code = (hi - 0xD800) * 0x400 + (lo - 0xDC00) + 0x10000;
                    utf8.push(0xF0 | (code >> 18), 0x80 | ((code >> 12) & 0x3F), 0x80 | ((code >> 6) & 0x3F), 0x80 | (code & 0x3F));
                } else { utf8.push(0xEF, 0xBF, 0xBD); }
            } else {
                utf8.push(0xE0 | (code >> 12), 0x80 | ((code >> 6) & 0x3F), 0x80 | (code & 0x3F));
            }
        }
        return new Uint8Array(utf8);
    };
    globalThis.TextEncoder.prototype.encodeInto = function(str, dest) {
        var encoded = this.encode(str);
        dest.set(encoded.subarray(0, dest.length));
        return { read: str.length, written: encoded.length };
    };
}
if (typeof TextDecoder === 'undefined') {
    globalThis.TextDecoder = function TextDecoder(encoding) { this.encoding = encoding || 'utf-8'; };
    globalThis.TextDecoder.prototype.decode = function(input) {
        if (!input) return '';
        var bytes = input instanceof Uint8Array ? input : new Uint8Array(input);
        var result = '', i = 0;
        while (i < bytes.length) {
            var b1 = bytes[i++];
            if (b1 < 0x80) { result += String.fromCharCode(b1); }
            else if (b1 < 0xE0) { var b2 = bytes[i++]; result += String.fromCharCode(((b1 & 0x1F) << 6) | (b2 & 0x3F)); }
            else if (b1 < 0xF0) { var b2 = bytes[i++], b3 = bytes[i++]; result += String.fromCharCode(((b1 & 0x0F) << 12) | ((b2 & 0x3F) << 6) | (b3 & 0x3F)); }
            else { var b2 = bytes[i++], b3 = bytes[i++], b4 = bytes[i++]; var cp = ((b1 & 0x07) << 18) | ((b2 & 0x3F) << 12) | ((b3 & 0x3F) << 6) | (b4 & 0x3F); result += String.fromCharCode(((cp - 0x10000) >> 10) + 0xD800, ((cp - 0x10000) & 0x3FF) + 0xDC00); }
        }
        return result;
    };
}

// Mock document for webpack runtime in WeChat Mini Program environment
if (typeof globalThis.document === 'undefined') {
    globalThis.document = {
        getElementsByTagName: function() { return [{ src: "" }]; },
        createElement: function() { return { setAttribute: function(){}, src: '', onerror: null, onload: null, parentNode: null }; },
        head: { appendChild: function() {} },
        currentScript: { src: "", tagName: "SCRIPT" }
    };
}

// AbortController polyfill for WeChat Mini Program
if (typeof globalThis.AbortController === 'undefined') {
    globalThis.AbortController = function AbortController() {
        var signal = new globalThis.AbortSignal();
        this.signal = signal;
        this.abort = function() {
            signal.abort();
        };
    };
    globalThis.AbortSignal = function AbortSignal() {
        this.aborted = false;
        this.onabort = null;
        this._eventListeners = [];
        this.addEventListener = function(type, listener) {
            if (type === 'abort') {
                this._eventListeners.push(listener);
            }
        };
        this.removeEventListener = function(type, listener) {
            if (type === 'abort') {
                var index = this._eventListeners.indexOf(listener);
                if (index >= 0) this._eventListeners.splice(index, 1);
            }
        };
        this.abort = function() {
            this.aborted = true;
            if (this.onabort) this.onabort();
            for (var i = 0; i < this._eventListeners.length; i++) {
                try { this._eventListeners[i](); } catch (e) {}
            }
        };
    };
}

// fetch polyfill for WeChat Mini Program based on wx.request
// IMPORTANT: Must be defined at module top-level (not inside IIFE) so that
// bare name 'fetch' is accessible inside webpack closures via scope chain.
if (typeof globalThis.fetch === 'undefined') {
    function __FetchHeaders(init) {
        this._headers = {};
        if (init) {
            if (typeof init.forEach === 'function') {
                var self = this;
                init.forEach(function(value, key) { self._headers[key] = value; });
            } else if (typeof init === 'object') {
                for (var key in init) { this._headers[key] = init[key]; }
            }
        }
    }
    __FetchHeaders.prototype.get = function(name) { return this._headers[name] || null; };
    __FetchHeaders.prototype.forEach = function(callback) {
        for (var key in this._headers) { callback(this._headers[key], key); }
    };

    function __FetchResponse(body, init) {
        this.status = init ? init.status : 200;
        this.statusText = init ? init.statusText : '';
        this.headers = init ? init.headers : new __FetchHeaders();
        this.body = body ? new __ReadableStream(body) : null;
    }

    function __ReadableStream(data) {
        var self = this;
        this._data = data;
        this._reader = null;
        this.getReader = function() { return new __ReadableStreamDefaultReader(self._data); };
    }

    function __ReadableStreamDefaultReader(data) {
        this._data = data;
        this._done = false;
    }
    __ReadableStreamDefaultReader.prototype.read = function() {
        var self = this;
        if (self._done) { return Promise.resolve({ done: true, value: undefined }); }
        self._done = true;
        var encoder = (typeof TextEncoder !== 'undefined') ? new TextEncoder() : null;
        var uint8;
        if (encoder) {
            uint8 = encoder.encode(self._data);
        } else {
            var str = self._data;
            uint8 = new Uint8Array(str.length);
            for (var i = 0; i < str.length; i++) { uint8[i] = str.charCodeAt(i); }
        }
        return Promise.resolve({ done: false, value: uint8 });
    };
    __ReadableStreamDefaultReader.prototype.releaseLock = function() {};

    function __decodeUTF8(uint8) {
        var r = '', i = 0;
        while (i < uint8.length) {
            var b = uint8[i++];
            if (b < 0x80) { r += String.fromCharCode(b); }
            else if ((b & 0xE0) === 0xC0) { var b2 = uint8[i++]; r += String.fromCharCode(((b & 0x1F) << 6) | (b2 & 0x3F)); }
            else if ((b & 0xF0) === 0xE0) { var b2 = uint8[i++], b3 = uint8[i++]; r += String.fromCharCode(((b & 0x0F) << 12) | ((b2 & 0x3F) << 6) | (b3 & 0x3F)); }
            else { var b2 = uint8[i++], b3 = uint8[i++], b4 = uint8[i++]; var cp = ((b & 0x07) << 18) | ((b2 & 0x3F) << 12) | ((b3 & 0x3F) << 6) | (b4 & 0x3F); r += String.fromCharCode(((cp - 0x10000) >> 10) + 0xD800, ((cp - 0x10000) & 0x3FF) + 0xDC00); }
        }
        return r;
    }

    globalThis.fetch = function(url, options) {
        options = options || {};
        var method = (options.method || 'GET').toUpperCase();
        var headers = {};
        if (options.headers) {
            if (typeof options.headers.forEach === 'function') {
                options.headers.forEach(function(v, k) { headers[k] = v; });
            } else {
                for (var k in options.headers) { headers[k] = options.headers[k]; }
            }
        }
        var reqTask = null;
        var aborted = false;
        if (options.signal && options.signal.aborted) {
            return Promise.reject(new DOMException('The operation was aborted.', 'AbortError'));
        }
        return new Promise(function(resolve, reject) {
            if (options.signal) {
                if (options.signal.aborted) {
                    reject(new DOMException('The operation was aborted.', 'AbortError'));
                    return;
                }
                options.signal.addEventListener('abort', function() {
                    aborted = true;
                    if (reqTask) reqTask.abort();
                    reject(new DOMException('The operation was aborted.', 'AbortError'));
                });
            }
            var req = { url: url, method: method, header: headers, responseType: 'arraybuffer' };
            if (options.body && method !== 'GET' && method !== 'HEAD') {
                req.data = options.body;
                if (!headers['Content-Type']) { headers['Content-Type'] = 'application/octet-stream'; }
                req.header = headers;
            }
            reqTask = wx.request(Object.assign({}, req, {
                success: function(res) {
                    if (aborted) return;
                    var respHeaders = new __FetchHeaders(res.header || {});
                    var bodyStr;
                    if (res.data && res.data.byteLength !== undefined) {
                        bodyStr = __decodeUTF8(new Uint8Array(res.data));
                    } else {
                        bodyStr = typeof res.data === 'string' ? res.data : JSON.stringify(res.data);
                    }
                    resolve(new __FetchResponse(bodyStr, {
                        status: res.statusCode,
                        statusText: '',
                        headers: respHeaders
                    }));
                },
                fail: function(err) {
                    if (aborted) return;
                    reject(new TypeError('Network request failed: ' + (err.errMsg || 'unknown error')));
                }
            }));
        });
    };
}
// Ensure bare name 'fetch' resolves in WeChat Mini Program module scope
var fetch = globalThis.fetch;

// Prevent webpack runtime auto publicPath detection
var __webpack_public_path__ = "";

""".trimIndent()
    
    var content = jsFile.readText()
    if (!content.contains("// window.open polyfill for WeChat Mini Program")) {
        // 移除 source map 引用（因为注入代码后行号会变化）
        content = content.replace(Regex("\\n?//# sourceMappingURL=.*"), "")
        jsFile.writeText(mockCode + content)
        println("Injected document mock into ${jsFile.name}")
    }
    
    // 删除 source map 文件
    val mapFile = File(jsFile.parentFile, jsFile.name + ".map")
    if (mapFile.exists()) {
        mapFile.delete()
        println("Deleted source map: ${mapFile.name}")
    }
}

// 在 webpack 构建完成后注入 document mock
tasks.matching { it.name.startsWith("jsBrowser") && it.name.endsWith("Webpack") }.configureEach {
    doLast {
        val buildDir = project.layout.buildDirectory.get().asFile
        val buildDirs = listOf(
            "$buildDir/kotlin-webpack/js/developmentExecutable",
            "$buildDir/kotlin-webpack/js/productionExecutable",
            "$buildDir/kotlin-webpack/js/distributions",
            "$buildDir/dist/js/productionExecutable"
        )
        buildDirs.forEach { dir ->
            val jsFile = File(dir, "nativevue2.js")
            if (jsFile.exists()) {
                injectDocumentMock(jsFile)
            }
        }
    }
}