package com.syf.testkuikly

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.tencent.kuikly.core.render.android.IKuiklyRenderExport
import com.tencent.kuikly.core.render.android.adapter.KuiklyRenderAdapterManager
import com.tencent.kuikly.core.render.android.css.ktx.toMap
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewBaseDelegatorDelegate
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewBaseDelegator
import com.syf.testkuikly.adapter.KRColorParserAdapter
import com.syf.testkuikly.adapter.KRFontAdapter
import com.syf.testkuikly.adapter.KRImageAdapter
import com.syf.testkuikly.adapter.KRLogAdapter
import com.syf.testkuikly.adapter.KRRouterAdapter
import com.syf.testkuikly.adapter.KRThreadAdapter
import com.syf.testkuikly.adapter.KRUncaughtExceptionHandlerAdapter
import com.syf.testkuikly.module.KRBridgeModule
import com.syf.testkuikly.module.KRShareModule
import org.json.JSONObject

/**
 * Kuikly UI 渲染Activity
 * 实现KuiklyRenderViewBaseDelegatorDelegate接口
 * 负责管理页面生命周期、渲染器初始化和模块注册
 */
class KuiklyRenderActivity : AppCompatActivity(), KuiklyRenderViewBaseDelegatorDelegate {

    /** 容器视图，用于承载Kuikly渲染内容 */
    private lateinit var hrContainerView: ViewGroup
    /** 加载视图 */
    private lateinit var loadingView: View
    /** 错误视图 */
    private lateinit var errorView: View

    /** Kuikly渲染视图委托器 */
    private val kuiklyRenderViewDelegator = KuiklyRenderViewBaseDelegator(this)

    /** 页面名称，从Intent中获取 */
    private val pageName: String
        get() {
            val pn = intent.getStringExtra(KEY_PAGE_NAME) ?: ""
            return if (pn.isNotEmpty()) {
                return pn
            } else {
                "main"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_hr)
        setupImmersiveMode()
        hrContainerView = findViewById(R.id.hr_container)
        loadingView = findViewById(R.id.hr_loading)
        errorView = findViewById(R.id.hr_error)
        kuiklyRenderViewDelegator.onAttach(hrContainerView, "", pageName, createPageData())
    }

    override fun onDestroy() {
        super.onDestroy()
        kuiklyRenderViewDelegator.onDetach()
    }

    override fun onPause() {
        super.onPause()
        kuiklyRenderViewDelegator.onPause()
    }

    override fun onResume() {
        super.onResume()
        kuiklyRenderViewDelegator.onResume()
    }

    /**
     * 注册外部模块
     * 在这里注册Kuikly核心功能模块
     */
    override fun registerExternalModule(kuiklyRenderExport: IKuiklyRenderExport) {
        super.registerExternalModule(kuiklyRenderExport)
        with(kuiklyRenderExport) {
            // 注册桥接模块，处理JavaScript Native通信
            moduleExport(KRBridgeModule.MODULE_NAME) {
                KRBridgeModule()
            }
            // 注册分享模块，提供分享功能
            moduleExport(KRShareModule.MODULE_NAME) {
                KRShareModule()
            }
        }
    }

    /**
     * 注册外部渲染视图
     * 目前为空，可扩展自定义渲染视图
     */
    override fun registerExternalRenderView(kuiklyRenderExport: IKuiklyRenderExport) {
        super.registerExternalRenderView(kuiklyRenderExport)
        with(kuiklyRenderExport) {

        }
    }

    /**
     * 创建页面数据
     * 在这里添加页面需要的初始化参数
     */
    private fun createPageData(): Map<String, Any> {
        val param = argsToMap()
        param["appId"] = 1
        return param
    }

    /**
     * 将Intent中的参数转换为Map
     */
    private fun argsToMap(): MutableMap<String, Any> {
        val jsonStr = intent.getStringExtra(KEY_PAGE_DATA) ?: return mutableMapOf()
        return JSONObject(jsonStr).toMap()
    }

    /**
     * 设置沉浸式模式
     * 隐藏状态栏、设置透明背景等
     */
    private fun setupImmersiveMode() {
        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window?.statusBarColor = Color.TRANSPARENT
            window?.decorView?.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

    }

    companion object {

        private const val KEY_PAGE_NAME = "pageName"
        private const val KEY_PAGE_DATA = "pageData"

        /**
         * 初始化Kuikly适配器
         * 在类加载时初始化所有必需的适配器
         */
        init {
            initKuiklyAdapter()
        }

        /**
         * 启动Kuikly渲染Activity
         * @param context 上下文
         * @param pageName 页面名称
         * @param pageData 页面数据
         */
        fun start(context: Context, pageName: String, pageData: JSONObject) {
            val starter = Intent(context, KuiklyRenderActivity::class.java)
            starter.putExtra(KEY_PAGE_NAME, pageName)
            starter.putExtra(KEY_PAGE_DATA, pageData.toString())
            context.startActivity(starter)
        }

        /**
         * 初始化Kuikly适配器
         * 设置图片、日志、字体等适配器
         */
        private fun initKuiklyAdapter() {
            with(KuiklyRenderAdapterManager) {
                // 图片加载适配器
                krImageAdapter = KRImageAdapter(KRApplication.application)
                // 日志适配器
                krLogAdapter = KRLogAdapter
                // 异常处理适配器
                krUncaughtExceptionHandlerAdapter = KRUncaughtExceptionHandlerAdapter
                // 字体适配器
                krFontAdapter = KRFontAdapter
                // 颜色解析适配器
                krColorParseAdapter = KRColorParserAdapter(KRApplication.application)
                // 路由适配器
                krRouterAdapter = KRRouterAdapter
                // 线程适配器
                krThreadAdapter = KRThreadAdapter()
            }
        }
    }
}