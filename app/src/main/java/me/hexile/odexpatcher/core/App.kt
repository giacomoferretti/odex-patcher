package me.hexile.odexpatcher.core

import android.app.Application
import android.content.Context
import com.topjohnwu.superuser.Shell
import me.hexile.odexpatcher.BuildConfig

class App : Application() {
    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()

        context = applicationContext
    }

    init {
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        Shell.setDefaultBuilder(Shell.Builder.create()
            .setFlags(Shell.FLAG_REDIRECT_STDERR)
            .setTimeout(10));
    }
}