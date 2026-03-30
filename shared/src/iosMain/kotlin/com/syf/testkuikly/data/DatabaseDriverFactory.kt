package com.syf.testkuikly.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual fun createDatabaseDriver(): SqlDriver {
    return NativeSqliteDriver(WanDb.Schema, "wanandroid.db")
}
