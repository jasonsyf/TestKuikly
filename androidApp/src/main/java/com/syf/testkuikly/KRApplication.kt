package com.syf.testkuikly

import android.app.Application
import com.syf.testkuikly.data.AppContextProvider

class KRApplication : Application() {

    init {
        application = this
        AppContextProvider.context = this
    }

    companion object {
        lateinit var application: Application
    }
}