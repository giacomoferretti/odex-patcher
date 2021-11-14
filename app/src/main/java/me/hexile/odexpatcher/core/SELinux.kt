/*
 * Copyright 2020-2021 Giacomo Ferretti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.hexile.odexpatcher.core

import android.annotation.SuppressLint

@SuppressLint("PrivateApi")
object SELinux {
    // https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/os/SELinux.java

    private fun invoke(methodName: String): Any? {
        val c = Class.forName("android.os.SELinux")
        val method = c.getMethod(methodName)
        return method.invoke(c)
    }

    fun isEnabled(): Boolean {
        return invoke("isSELinuxEnabled") as Boolean
    }

    fun isEnforced(): Boolean {
        return invoke("isSELinuxEnforced") as Boolean
    }

    fun getContext(): String {
        val c = Class.forName("android.os.SELinux")
        val method = c.getMethod("getContext")
        return method.invoke(c) as String
    }

    fun getFileContext(path: String): String {
        val c = Class.forName("android.os.SELinux")
        val method = c.getMethod("getFileContext", String::class.java)
        return method.invoke(c, path) as String
    }

    // https://cs.android.com/android/platform/superproject/+/master:external/selinux/libselinux/src/android/android_platform.c;l=763
}