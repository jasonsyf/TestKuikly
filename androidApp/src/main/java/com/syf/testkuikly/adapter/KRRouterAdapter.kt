package com.syf.testkuikly.adapter

import android.app.Activity
import android.content.Context
import com.tencent.kuikly.core.render.android.adapter.IKRRouterAdapter
import com.syf.testkuikly.KuiklyRenderActivity
import org.json.JSONObject

/**
 * Kuikly UI 路由适配器
 * 实现IKRRouterAdapter接口，提供页面跳转功能
 */
object KRRouterAdapter : IKRRouterAdapter {

    /**
     * 打开新页面
     * @param context 上下文
     * @param pageName 页面名称
     * @param pageData 页面数据
     */
    override fun openPage(
        context: Context,
        pageName: String,
        pageData: JSONObject,
    ) {
        KuiklyRenderActivity.start(context, pageName, pageData)
    }

    /**
     * 关闭当前页面
     * @param context 上下文
     */
    override fun closePage(context: Context) {
        (context as? Activity)?.finish()
    }
}