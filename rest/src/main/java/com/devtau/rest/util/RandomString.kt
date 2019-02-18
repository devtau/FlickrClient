package com.devtau.rest.util

import android.os.Build
import android.support.annotation.RequiresApi
import java.security.SecureRandom
import java.util.Objects
import java.util.Random
/**
 * В принципе для nonce достаточно использовать System.currentTimeMillis()
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
class RandomString constructor(
    length: Int = 32,
    random: Random = SecureRandom(),
    symbols: String = alphaNum
) {

    private val random: Random
    private val symbols: CharArray
    private val buf: CharArray


    init {
        if (length < 1) throw IllegalArgumentException()
        if (symbols.length < 2) throw IllegalArgumentException()
        this.random = Objects.requireNonNull(random)
        this.symbols = symbols.toCharArray()
        this.buf = CharArray(length)
    }

    fun nextString(): String {
        for (idx in buf.indices)
            buf[idx] = symbols[random.nextInt(symbols.size)]
        return String(buf)
    }


    companion object {
        private const val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val lower = "abcdefghijklmnopqrstuvwxyz"
        private const val digits = "0123456789"
        private const val alphaNum = upper + lower + digits
    }
}