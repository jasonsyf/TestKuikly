package com.syf.testkuikly.base

/**
 * WeChat Mini Program: open URL via wx.navigateTo to webview page
 */
actual fun openWebDetail(url: String, title: String) {
    js("wx.navigateTo({url: '/packageA/pages/webview/webview?url=' + encodeURIComponent(url)})")
}
