package com.smartdialer.app.util

object T9 {

    fun toDigits(input: String): String {
        val builder = StringBuilder()
        for (ch in input.lowercase()) {
            when (ch) {
                in 'a'..'c' -> builder.append('2')
                in 'd'..'f' -> builder.append('3')
                in 'g'..'i' -> builder.append('4')
                in 'j'..'l' -> builder.append('5')
                in 'm'..'o' -> builder.append('6')
                in 'p'..'s' -> builder.append('7')
                in 't'..'v' -> builder.append('8')
                in 'w'..'z' -> builder.append('9')
                in '0'..'9' -> builder.append(ch)
            }
        }
        return builder.toString()
    }
}
