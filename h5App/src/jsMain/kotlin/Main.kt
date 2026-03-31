import com.tencent.kuikly.core.render.web.ktx.SizeI
import kotlinx.browser.document
import kotlinx.browser.window
import utils.URL

/**
 * H5 WebApp 入口
 * 使用renderView委托方法来初始化和创建渲染视图
 */
/**
 * H5应用主入口函数
 * 初始化Kuikly渲染器并处理页面生命周期
 */
fun main() {
    console.log("##### Kuikly Web Render")
    // 根容器id，需要与实际index.html文件中的容器id匹配
    val containerId = "root"
    val webSign = "is_web"
    // 处理URL参数
    val urlParams = URL.parseParams(window.location.href)
    // 页面名称，默认为main
    val pageName = urlParams["page_name"] ?: "main"
    // 容器尺寸
    val containerWidth = window.innerWidth
    val containerHeight = window.innerHeight
    // 业务参数
    val params: MutableMap<String, String> = mutableMapOf()
    // 添加业务参数
    if (urlParams.isNotEmpty()) {
        // 将所有URL参数添加到业务参数中
        urlParams.forEach { (key, value) ->
            params[key] = value
        }
    }
    // 添加web特定参数
    params[webSign] = "1"
    // 页面参数Map
    val paramMap = mapOf(
        "statusBarHeight" to 0f,
        "activityWidth" to containerWidth,
        "activityHeight" to containerHeight,
        "param" to params,
    )

    // 初始化委托器
    val delegator = KuiklyWebRenderViewDelegator()
    // 创建渲染视图
    delegator.init(
        containerId, pageName, paramMap, SizeI(
            containerWidth,
            containerHeight,
        )
    )
    // 触发恢复
    delegator.resume()

    // 注册可见性事件
    document.addEventListener("visibilitychange", {
        val hidden = document.asDynamic().hidden as Boolean
        if (hidden) {
            // 页面隐藏
            delegator.pause()
        } else {
            // 页面恢复
            delegator.resume()
        }
    })
}