package com.syf.testkuikly

import android.app.Application
import com.syf.testkuikly.data.AppContextProvider

/**
 * Kuikly UI 应用的Application类
 * 负责初始化全局应用实例和上下文
 */
class KRApplication : Application() {

    /**
     * 初始化Application
     * 设置全局application实例和AppContextProvider上下文
     */
    init {
        application = this
        AppContextProvider.context = this
    }

    /**
     * 伴生对象，用于保存全局application实例
     */
    companion object {
        lateinit var application: Application
    }
}