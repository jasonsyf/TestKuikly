package com.syf.testkuikly.data

import app.cash.sqldelight.db.SqlDriver

actual fun createDatabaseDriver(): SqlDriver {
    throw UnsupportedOperationException("SQLDelight has no JS driver in 2.0.x. JS uses memory cache.")
}
