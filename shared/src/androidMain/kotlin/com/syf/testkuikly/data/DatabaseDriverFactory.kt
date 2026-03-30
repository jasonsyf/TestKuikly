package com.syf.testkuikly.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual fun createDatabaseDriver(): SqlDriver {
    val context = AppContextProvider.context
        ?: error("AppContextProvider.context not initialized. Call AppContextProvider.init(context) in Application.onCreate()")
    return AndroidSqliteDriver(WanDb.Schema, context, "wanandroid.db")
}

object AppContextProvider {
    var context: Context? = null
}
