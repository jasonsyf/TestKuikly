package com.syf.testkuikly.data

actual object KtorfitInstance {

    actual val api: WanApiService by lazy {
        OhosWanApiService(getBaseUrl())
    }
}
