package com.sifre.kasasi

import java.security.SecureRandom

object PasswordGenerator {
    private const val LOWER = "abcdefghijkmnopqrstuvwxyz"
    private const val UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    private const val DIGITS = "23456789"
    private const val SYMBOLS = "!@#$%&*?-_"

    fun generate(length: Int = 16): String {
        val rnd = SecureRandom()
        val all = LOWER + UPPER + DIGITS + SYMBOLS
        val chars = mutableListOf(
            LOWER[rnd.nextInt(LOWER.length)],
            UPPER[rnd.nextInt(UPPER.length)],
            DIGITS[rnd.nextInt(DIGITS.length)],
            SYMBOLS[rnd.nextInt(SYMBOLS.length)]
        )
        while (chars.size < length) {
            chars.add(all[rnd.nextInt(all.length)])
        }
        chars.shuffle(rnd)
        return chars.joinToString("")
    }
}
