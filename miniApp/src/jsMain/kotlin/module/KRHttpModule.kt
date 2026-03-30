package module

import com.tencent.kuikly.core.render.web.export.KuiklyRenderBaseModule
import com.tencent.kuikly.core.render.web.ktx.KuiklyRenderCallback
import com.tencent.kuikly.core.render.web.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.render.web.runtime.miniapp.core.NativeApi
import com.tencent.kuikly.core.render.web.utils.Log
import kotlin.js.json

/**
 * 微信小程序端HTTP请求模块，通过 wx.request 发起请求
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
            val httpMethod = paramJSON.optString("method", "GET")

            val reqParams = json(
                "url" to urlStr,
                "method" to httpMethod,
                "header" to json(
                    "content-type" to "application/x-www-form-urlencoded"
                ),
                "success" to { res: dynamic ->
                    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
                    val data = res.data as? String ?: JSON.stringify(res.data)
                    callback?.invoke(data)
                },
                "fail" to { err: dynamic ->
                    callback?.invoke(
                        """{"errorCode":-1,"errorMsg":"${err.errMsg ?: "Request failed"}"}"""
                    )
                }
            )

            val bodyObj = paramJSON.optJSONObject("body")
            if (bodyObj != null) {
                val bodyStr = StringBuilder()
                val keys = bodyObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = bodyObj.optString(key)
                    if (bodyStr.isNotEmpty()) bodyStr.append("&")
                    bodyStr.append("${encodeURIComponent(key)}=${encodeURIComponent(value)}")
                }
                reqParams["data"] = bodyStr.toString()
            }

            NativeApi.plat.request(reqParams)
        } catch (e: Exception) {
            Log.error("httpRequest error: ${e.message}")
            callback?.invoke("""{"errorCode":-1,"errorMsg":"${e.message ?: "Unknown error"}"}""")
        }
    }

    private fun encodeURIComponent(str: String): String {
        return js("encodeURIComponent(str)")
    }

    private fun JSON.stringify(obj: dynamic): String {
        return js("JSON.stringify(obj)")
    }

    companion object {
        const val MODULE_NAME = "HttpModule"
    }
}
