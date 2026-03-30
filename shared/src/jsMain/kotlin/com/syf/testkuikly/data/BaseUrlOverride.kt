package com.syf.testkuikly.data

/**
 * JS/Web platform override: use webpack dev server CORS proxy path
 * For WeChat Mini Program, we need to use the full URL directly
 */
actual fun getBaseUrl(): String = "https://www.wanandroid.com/"
