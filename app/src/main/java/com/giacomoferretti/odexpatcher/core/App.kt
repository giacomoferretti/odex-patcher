package com.giacomoferretti.odexpatcher.core

import android.app.Application

class App : Application() {
    val appContainer: AppContainer by lazy { AppContainer(this) }
}