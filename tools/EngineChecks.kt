import com.rd184.calculator.CalculatorEngine
import com.rd184.calculator.ModeMath
import kotlin.math.abs

private fun near(actual: Double, expected: Double) {
    check(abs(actual - expected) < 1e-8) { "Expected $expected, received $actual" }
}

fun main() {
    val deg = CalculatorEngine(true)
    near(deg.evaluate("2+3*4"), 14.0)
    near(deg.evaluate("(2+3)*4"), 20.0)
    near(deg.evaluate("sin(30)"), 0.5)
    near(deg.evaluate("cos(60)"), 0.5)
    near(deg.evaluate("tan(45)"), 1.0)
    near(deg.evaluate("asin(0.5)"), 30.0)
    near(deg.evaluate("sqrt(81)+log(100)+ln(e)"), 12.0)
    near(deg.evaluate("5!"), 120.0)
    near(deg.evaluate("50%"), 0.5)
    near(deg.evaluate("2^3^2"), 512.0)
    val rad = CalculatorEngine(false)
    near(rad.evaluate("sin(pi/2)"), 1.0)
    check(runCatching { deg.evaluate("1/0") }.isFailure)
    check(runCatching { deg.evaluate("sqrt(-1)") }.isFailure)
    check(runCatching { deg.evaluate("log(-2)") }.isFailure)
    check(ModeMath.baseConvert("FF", 16, 2) == "11111111")
    check(ModeMath.baseConvert("1010", 2, 10) == "10")
    check(ModeMath.bitwise(6, "XOR", 3) == 5L)
    val z = ModeMath.Complex(2.0, 3.0) * ModeMath.Complex(1.0, -1.0)
    near(z.re, 5.0); near(z.im, 1.0)
    near(ModeMath.determinant(arrayOf(doubleArrayOf(1.0,2.0),doubleArrayOf(3.0,4.0))), -2.0)
    near(ModeMath.multiply(arrayOf(doubleArrayOf(1.0,2.0)),arrayOf(doubleArrayOf(3.0),doubleArrayOf(4.0)))[0][0], 11.0)
    val stats = ModeMath.statistics(listOf(1.0,2.0,3.0,4.0))
    near(stats.mean,2.5); near(stats.median,2.5); near(stats.populationVariance,1.25)
    check(ModeMath.vectorCross(listOf(1.0,0.0,0.0),listOf(0.0,1.0,0.0)) == listOf(0.0,0.0,1.0))
    check(ModeMath.fraction(0.75) == "3/4")
    println("PASS: 21 mathematical engine checks")
}
