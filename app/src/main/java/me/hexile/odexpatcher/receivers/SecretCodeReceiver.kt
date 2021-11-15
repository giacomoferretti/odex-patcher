package me.hexile.odexpatcher.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.hexile.odexpatcher.ui.activities.SplashActivity


class SecretCodeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SECRET_CODE") {
            context.startActivity(Intent(Intent.ACTION_MAIN).apply {
                setClass(context, SplashActivity::class.java)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }
}