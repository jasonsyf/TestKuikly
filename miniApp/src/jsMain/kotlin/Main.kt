import com.tencent.kuikly.core.render.web.collection.FastMutableMap
import com.tencent.kuikly.core.render.web.ktx.SizeI
import com.tencent.kuikly.core.render.web.runtime.miniapp.MiniDocument
import com.tencent.kuikly.core.render.web.runtime.miniapp.core.App
import com.tencent.kuikly.core.render.web.runtime.miniapp.core.NativeApi

const val TAG = "Main"

fun main() {
    App.onShow {
        console.log(TAG, "app show")
    }

    App.onLaunch {
        console.log(TAG, "app launch")
    }

    App.onHide {
        console.log(TAG, "app hide")
    }
}

/**
 * 小程序页面入口
 * 使用renderView委托方法来初始化和创建渲染视图
 */
@JsName(name = "renderView")
@JsExport
@ExperimentalJsExport
fun renderView(json: dynamic) {
    // Write to global render function
    val renderParams = FastMutableMap<String, dynamic>(json)
    // View size
    var size: SizeI? = null
    if (json.width != null && json.height != null) {
        size = SizeI(json.width.unsafeCast<Int>(), json.height.unsafeCast<Int>())
    }

    MiniDocument.initPage(renderParams) { pageId: Int, pageName: String, paramsMap: FastMutableMap<String, Any> ->
        val systemInfo = NativeApi.plat.getSystemInfoSync()
        val isAndroid = systemInfo.platform == "android"
        val params = paramsMap["param"].unsafeCast<FastMutableMap<String, Any>>()
        params["is_wx_mp"] = "true"

        paramsMap["platform"] = if (isAndroid) "android" else "iOS"
        paramsMap["isIOS"] = !isAndroid
        paramsMap["isIphoneX"] = !isAndroid && systemInfo.safeArea.top > 30

        KuiklyWebRenderViewDelegator().delegate.onAttach(
            pageId,
            pageName,
            paramsMap,
            size,
        )
    }
}

/**
 * 注册小程序App对象上的回调方法
 * 需要在小程序的app.js中调用
 */
@JsName(name = "initApp")
@JsExport
@ExperimentalJsExport
fun initApp(options: dynamic = js("{}")) {
    App.initApp(options)
}