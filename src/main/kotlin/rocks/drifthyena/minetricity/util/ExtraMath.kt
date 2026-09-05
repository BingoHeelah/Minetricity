package rocks.drifthyena.minetricity.util

import kotlin.math.PI
import kotlin.math.abs

const val SQRT_2 = 1.41421356237
const val SQRT_3 = 1.73205080757
const val FULL_RADIAN_ROTATION = 2.0 * PI

fun radiansToDegrees(angle: Double): Double {
    return (angle * PI) / 180.0
}

fun degreesToRadians(angle: Double): Double {
    return (angle * 180.0) / PI
}

fun toPeakVoltage(voltage: Double): Double {
    return voltage * SQRT_2
}

fun requireFinite(n: Double) {
    require(n.isFinite()) {
        "Value is not finite"
    }
}

infix fun Double.approxEquals(other: Double): Boolean {
    return abs(this - other) < 0.0001
}

fun Double.isOverUnder(other: Double, ou: Double = 1.0): Boolean {
    return abs(this - other) < ou
}
