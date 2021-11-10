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

import java.security.SecureRandom
import java.util.*

class RandomString @JvmOverloads constructor(
    length: Int = 21,
    random: Random = SecureRandom(),
    symbols: String = alphanum
) {
    /**
     * Generate a random string.
     */
    fun nextString(): String {
        for (idx in buf.indices) buf[idx] = symbols[random.nextInt(symbols.size)]
        return String(buf)
    }

    private val random: Random
    private val symbols: CharArray
    private val buf: CharArray

    companion object {
        const val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lower = upper.lowercase(Locale.ROOT)
        const val digits = "0123456789"
        val alphanum = upper + lower + digits
    }
    /**
     * Create an alphanumeric string generator.
     */
    /**
     * Create an alphanumeric strings from a secure generator.
     */
    /**
     * Create session identifiers.
     */
    init {
        require(length >= 1)
        require(symbols.length >= 2)
        this.random = Objects.requireNonNull(random)
        this.symbols = symbols.toCharArray()
        buf = CharArray(length)
    }
}