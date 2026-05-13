package com.kutirakone.app

import android.app.Application

class KutiraKoneApp : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}