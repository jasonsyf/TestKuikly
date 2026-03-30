package com.syf.testkuikly.base

/**
 * Android platform: open URL through native bridge
 */
actual fun openWebDetail(url: String, title: String) {
    Utils.openWebDetail(url, title)
}
