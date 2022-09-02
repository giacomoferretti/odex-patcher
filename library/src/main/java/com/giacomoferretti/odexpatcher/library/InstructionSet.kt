package com.giacomoferretti.odexpatcher.library

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

/*
https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-13.0.0_r1/services/core/java/com/android/server/pm/InstructionSets.java#60

arm64 device:
[ro.dalvik.vm.native.bridge]: [0]

x86_64 emulator:
[ro.dalvik.vm.isa.arm]: [x86]
[ro.dalvik.vm.isa.arm64]: [x86_64]
[ro.dalvik.vm.native.bridge]: [libndk_translation.so]
 */

enum class InstructionSet(val value: String) {
    // Reference: https://android.googlesource.com/platform/art/+/refs/tags/android-13.0.0_r1/libartbase/arch/instruction_set.cc#40
    NONE("none"),
    ARM("arm"),
    ARM_64("arm64"),
    X86("x86"),
    X86_64("x86_64");

    companion object {
        @JvmStatic
        fun getDefault() = fromAbi(if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Build.CPU_ABI
        } else {
            Build.SUPPORTED_ABIS[0]
        })

        @JvmStatic
        fun fromAbi(abi: String) = when (abi) {
            // Reference:
            //  - https://developer.android.com/ndk/guides/abis
            //  - https://android.googlesource.com/platform/libcore/+/refs/tags/android-13.0.0_r1/libart/src/main/java/dalvik/system/VMRuntime.java#56
            "armeabi-v7a", "armeabi" -> ARM
            "arm64-v8a" -> ARM_64
            "x86" -> X86
            "x86_64" -> X86_64
            else -> NONE
        }

        @JvmStatic
        @SuppressLint("DiscouragedPrivateApi")
        fun fromPackageName(packageManager: PackageManager, packageName: String): InstructionSet {
            val primaryCpuAbi = ApplicationInfo::class.java.getDeclaredField("primaryCpuAbi").get(packageManager.getPackageInfo(packageName, 0).applicationInfo)

            return if (primaryCpuAbi != null) fromAbi(primaryCpuAbi as String) else getDefault()
        }
    }
}