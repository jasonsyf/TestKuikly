package com.syf.testkuikly.base

/**
 * OHOS platform: open URL through native bridge (WebView)
 */
actual fun openWebDetail(url: String, title: String) {
    Utils.openWebDetail(url, title)
}
