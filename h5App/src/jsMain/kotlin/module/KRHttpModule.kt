package module

import com.tencent.kuikly.core.render.web.export.KuiklyRenderBaseModule
import com.tencent.kuikly.core.render.web.ktx.KuiklyRenderCallback
import com.tencent.kuikly.core.render.web.nvi.serialization.json.JSONObject
import kotlinx.browser.window
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.js.json
import kotlin.js.then

/**
 * H5 端 HTTP 请求模块，通过 JS fetch API 发起请求
 */
class KRHttpModule : KuiklyRenderBaseModule() {
    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? {
        return when (method) {
            "httpRequest" -> {
                handleHttpRequest(params, callback)
            }
            else -> null
        }
    }

    private fun handleHttpRequest(params: String?, callback: KuiklyRenderCallback?) {
        if (params == null) {
            callback?.invoke("""{"errorCode":-1,"errorMsg":"params is null"}""")
            return
        }
        try {
            val paramJSON = JSONObject(params)
            val urlStr = paramJSON.optString("url")
            val method = paramJSON.optString("method", "GET")

            val bodyObj = paramJSON.optJSONObject("body")
            val bodyStr = if (bodyObj != null) {
                val pairs = mutableListOf<String>()
                val keys = bodyObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = bodyObj.optString(key)
                    pairs.add("${encodeURIComponent(key)}=${encodeURIComponent(value)}")
                }
                pairs.joinToString("&")
            } else null

            val requestInit = RequestInit(
                method = method,
                body = bodyStr,
                headers = json(
                    "Content-Type" to "application/x-www-form-urlencoded"
                )
            )

            window.fetch(urlStr, requestInit).then { response: Response ->
                if (response.ok) {
                    response.text().then { text ->
                        callback?.invoke(text)
                    }
                } else {
                    callback?.invoke(
                        """{"errorCode":${response.status},"errorMsg":"HTTP error ${response.status}"}"""
                    )
                }
            }.catch { error ->
                callback?.invoke(
                    """{"errorCode":-1,"errorMsg":"${error.toString()}"}"""
                )
            }
        } catch (e: Exception) {
            callback?.invoke("""{"errorCode":-1,"errorMsg":"${e.message ?: "Parse error"}"}""")
        }
    }

    private fun encodeURIComponent(str: String): String {
        return js("encodeURIComponent(str)")
    }

    companion object {
        const val MODULE_NAME = "HttpModule"
    }
}
