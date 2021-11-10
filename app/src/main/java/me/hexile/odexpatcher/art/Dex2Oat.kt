package me.hexile.odexpatcher.art

object Dex2Oat {
    /*fun run(context: Context) {
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> {
                android44_51(context)
            }
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
                android6_9(context)
            }
            else -> {
                android10(context)
            }
        }
    }*/

    /*private fun android44_51(context: Context) {
        // Android 4.4 - 5.1 workflow
        //  - copy from /data/app/packagename.apk to /data/data/me.hexile.odexpatcher/files/backup.apk
        //  - copy from /data/data/me.hexile.odexpatcher/files/base.apk to /data/app/packagename.apk
        //  - dex2oat /data/app/packagename.apk
        //  - copy from /data/data/me.hexile.odexpatcher/files/backup.apk to /data/app/packagename.apk
        val backupApk = context.getFileInFilesDir("backup.apk").absolutePath

        //viewModel.addLog("[I] Backing up target apk…")
        var shellResult = Shell.su("cp $targetApk $backupApk").exec()
        if (!shellResult.isSuccess) {
            //viewModel.addLog("[E] ERROR: cp exit code was ${shellResult.code}.")
            //viewModel.state.postValue(true)
            eventChannel.send(MainViewModel.Event.SnackBarString("ERROR: cp exit code was ${shellResult.code}."))
            return@launch
        }
        //viewModel.addLog(" Done!", false)

        //viewModel.addLog("[I] Copying over input file…")
        shellResult = Shell.su("cp ${baseApk.absolutePath} $targetApk").exec()
        if (!shellResult.isSuccess) {
            //viewModel.addLog("[E] ERROR: cp exit code was ${shellResult.code}.")
            //viewModel.state.postValue(true)
            eventChannel.send(MainViewModel.Event.SnackBarString("ERROR: cp exit code was ${shellResult.code}."))
            return@launch
        }
        //viewModel.addLog(" Done!", false)

        viewModel.addLog("[I] Running dex2oat…")
        shellResult = Shell.sh(
            "dex2oat --dex-file=$targetApk --oat-file=${
                App.context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME).absolutePath
            }"
        ).exec()
        if (!shellResult.isSuccess) {
            viewModel.addLog("[E] ERROR: dex2oat exit code was ${shellResult.code}.")
            viewModel.state.postValue(true)
            return@launch
        }
        viewModel.addLog(" Done!", false)

        viewModel.addLog("[I] Restoring backup…")
        shellResult = Shell.su("cp $backupApk $targetApk").exec()
        if (!shellResult.isSuccess) {
            viewModel.addLog("[E] ERROR: cp exit code was ${shellResult.code}.")
            viewModel.state.postValue(true)
            return@launch
        }
        viewModel.addLog(" Done!", false)
    }

    private fun android6_9(context: Context) {
        // Android 6.0 - 9.0 workflow
        //  - cd /data/data/me.hexile.odexpatcher/files/
        //  - dex2oat base.apk
        viewModel.addLog("[I] Running dex2oat…")
        val shellResult = Shell.sh(
            "cd ${App.context.getFileInFilesDir("").absolutePath} && dex2oat --dex-file=${Const.BASE_APK_FILE_NAME} --oat-file=${
                App.context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME).absolutePath
            }"
        ).exec()
        if (!shellResult.isSuccess) {
            viewModel.addLog("[E] ERROR: dex2oat exit code was ${shellResult.code}.")
            viewModel.state.postValue(true)
            return@launch
        }
        viewModel.addLog(" Done!", false)
    }

    private fun android10(context: Context) {
        // Android 10+ workflow
        //  - cd /data/data/me.hexile.odexpatcher/files/
        //  - su dex2oat base.apk
        viewModel.addLog("[I] Running dex2oat…")
        val shellResult = Shell.su(
            "cd ${App.context.getFileInFilesDir("").absolutePath} && dex2oat64 --dex-file=${Const.BASE_APK_FILE_NAME} --oat-file=${
                App.context.getFileInFilesDir(Const.BASE_ODEX_FILE_NAME).absolutePath
            }"
        ).exec()
        if (!shellResult.isSuccess) {
            viewModel.addLog("[E] ERROR: dex2oat exit code was ${shellResult.code}.")
            viewModel.state.postValue(true)
            return@launch
        }
        viewModel.addLog(" Done!", false)
    }*/
}