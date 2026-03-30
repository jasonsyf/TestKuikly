package com.syf.testkuikly.data

actual fun createCacheRepository(): CacheRepository = SqlDelightCacheRepository()
