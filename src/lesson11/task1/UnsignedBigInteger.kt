package lesson11.task1

import java.lang.Character.getNumericValue
import java.lang.StringBuilder
import kotlin.math.pow


/**
 * Класс "беззнаковое большое целое число".
 *
 * Общая сложность задания -- очень сложная, общая ценность в баллах -- 32.
 * Объект класса содержит целое число без знака произвольного размера
 * и поддерживает основные операции над такими числами, а именно:
 * сложение, вычитание (при вычитании большего числа из меньшего бросается исключение),
 * умножение, деление, остаток от деления,
 * преобразование в строку/из строки, преобразование в целое/из целого,
 * сравнение на равенство и неравенство
 */

class UnsignedBigInteger : Comparable<UnsignedBigInteger> {

    /**
     * Конструктор из строки
     */
    var number = mutableListOf<Int>()

    constructor(s: String) {
        if (s.matches(Regex("""^\d+$"""))) {
            for (index in s.length - 1 downTo 0) number.add(getNumericValue(s[index]))
        } else throw ArithmeticException("Not a number")
    }


    /**
     * Конструктор из целого
     */
    constructor(i: Int) {
        var integer = i
        when {
            i < 0 -> throw ArithmeticException("Val cannot be negative")
            i == 0 -> number.add(0)
            else -> while (integer != 0) {
                number.add(integer % 10)
                integer /= 10
            }
        }
    }

    constructor(list: MutableList<Int>) {
        number = list
    }


    /**
     * Сложение
     */

    operator fun plus(other: UnsignedBigInteger): UnsignedBigInteger {
        var inMem = 0
        val resultNumber = mutableListOf<Int>()
        val longerNumber: MutableList<Int>
        val shorterNumber: MutableList<Int>
        if (number.size >= other.number.size) {
            longerNumber = number
            shorterNumber = other.number
        } else {
            longerNumber = other.number
            shorterNumber = number
        }
        fun summing(oldSum: Int) {
            var sum = oldSum
            if (sum >= 10) {
                inMem = 1
                sum %= 10
            } else inMem = 0
            resultNumber.add(sum)
        }

        for (index in shorterNumber.indices) {
            summing(longerNumber[index] + shorterNumber[index] + inMem)

        }
        for (index in shorterNumber.size until longerNumber.size) {
            summing(longerNumber[index] + inMem)
        }
        if (inMem != 0) resultNumber.add(inMem)

        return UnsignedBigInteger(resultNumber)
    }


    /**
     * Вычитание (бросить ArithmeticException, если this < other)
     */
    operator fun minus(other: UnsignedBigInteger): UnsignedBigInteger {
        val resultNumber = mutableListOf<Int>()
        var inMem = 0
        if (this < other) throw ArithmeticException("Cannot subtract from a smaller number")
        val longerNumber = number
        val shorterNumber = other.number
        fun difference(oldDifference: Int) {
            var difference = oldDifference
            if (difference < 0) {
                inMem = 1
                difference += 10
            } else inMem = 0
            resultNumber.add(difference)
        }

        for (index in shorterNumber.indices) {
            difference(longerNumber[index] - shorterNumber[index] - inMem)
        }
        for (index in shorterNumber.size until longerNumber.size) {
            difference(longerNumber[index] - inMem)
        }
        while (resultNumber.last() == 0 && resultNumber.size > 1) resultNumber.removeLast()

        return UnsignedBigInteger(resultNumber)
    }


    /**
     * Умножение
     */
    operator fun times(other: UnsignedBigInteger): UnsignedBigInteger {
        var currentSum = UnsignedBigInteger(0)
        val sumAdd = mutableListOf<Int>()
        var inMem = 0

        for ((position, digit0) in other.number.withIndex()) {
            for (index in 0 until position) sumAdd.add(0)
            for (digit1 in number) {
                val multiplication = digit0 * digit1 + inMem
                if (multiplication > 10) {
                    inMem = multiplication / 10
                    sumAdd.add(multiplication % 10)
                } else {
                    inMem = 0
                    sumAdd.add(multiplication)
                }
            }
            if (inMem != 0) sumAdd.add(inMem)
            inMem = 0
            currentSum += UnsignedBigInteger(sumAdd)
            sumAdd.clear()
        }
        return currentSum
    }


    /**
     * Деление
     */
    private fun divisionProcess(
        numerator: MutableList<Int>,
        denominator: UnsignedBigInteger,
        onlyRemains: Boolean
    ): UnsignedBigInteger {
        var tmpNum = UnsignedBigInteger(mutableListOf())
        var index = numerator.size - 1
        val resultNumber = mutableListOf<Int>()
        if (denominator == UnsignedBigInteger(0)) throw ArithmeticException("Cannot divide by 0")
        while (index >= 0) {
            var counter = 0
            while (tmpNum <= denominator && index != -1) {
                tmpNum.number.add(0, numerator[index])
                index--
            }
            while (tmpNum >= denominator) {
                tmpNum -= denominator
                counter++
            }
            resultNumber.add(counter)

        }
        resultNumber.reverse()
        return if (onlyRemains) tmpNum
        else UnsignedBigInteger(resultNumber)
    }


    operator fun div(other: UnsignedBigInteger): UnsignedBigInteger = divisionProcess(number, other, false)

    /**
     * Взятие остатка
     */
    operator fun rem(other: UnsignedBigInteger): UnsignedBigInteger = divisionProcess(number, other, true)


    /**
     * Сравнение на равенство (по контракту Any.equals)
     */
    override fun equals(other: Any?): Boolean = other is UnsignedBigInteger && this.compareTo(other) == 0


    /**
     * Сравнение на больше/меньше (по контракту Comparable.compareTo)
     */
    override fun compareTo(other: UnsignedBigInteger): Int {
        when {
            number.size > other.number.size -> return 1
            number.size < other.number.size -> return -1
            else -> {
                for (index in number.size - 1 downTo 0) {
                    if (number[index] > other.number[index]) return 1
                    else if (number[index] < other.number[index]) return -1
                }
            }
        }
        return 0
    }


    /**
     * Преобразование в строку
     */
    override fun toString(): String {
        val result = StringBuilder()
        for (index in number.size - 1 downTo 0) result.append(number[index])
        return result.toString()
    }


    /**
     * Преобразование в целое
     * Если число не влезает в диапазон Int, бросить ArithmeticException
     */
    fun toInt(): Int {
        if (this > UnsignedBigInteger(Int.MAX_VALUE)) throw ArithmeticException("Value too big")
        else {
            var result = 0
            for ((place, digit) in number.withIndex()) {
                result += digit * 10.0.pow(place).toInt()
            }
            return result
        }

    }

    override fun hashCode(): Int = number.hashCode()
}


