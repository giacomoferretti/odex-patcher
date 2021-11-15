package me.hexile.odexpatcher.art

import com.topjohnwu.superuser.Shell
import me.hexile.odexpatcher.core.App
import me.hexile.odexpatcher.core.Const
import me.hexile.odexpatcher.ktx.getFileInCacheDir
import me.hexile.odexpatcher.utils.logd

object Dex2Oat {
    fun run(
        dexFile: String,
        dexLocation: String,
        oatFile: String = App.getContext().getFileInCacheDir(Const.BASE_ODEX_FILE_NAME).absolutePath
    ): Boolean {
        // Run dex2oat
        val dex2OatCommand = "dex2oat --dex-file=$dexFile --dex-location=$dexLocation --oat-file=$oatFile --instruction-set=${Art.ISA} --instruction-set-variant=${Art.ISA_VARIANT} --instruction-set-features=${Art.ISA_FEATURES}"
        logd("dex2oat", dex2OatCommand)
        return Shell.sh(dex2OatCommand).exec().isSuccess
    }
}