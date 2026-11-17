package com.rd184.calculator

import kotlin.math.*

/** Safe recursive-descent expression evaluator. No eval(), scripting engine, or reflection. */
class CalculatorEngine(var degrees: Boolean = true) {
    fun evaluate(source: String): Double {
        val parser = Parser(source.replace('×', '*').replace('÷', '/').replace('−', '-'), degrees)
        val value = parser.parse()
        if (!value.isFinite()) throw ArithmeticException("Result is outside the real-number range")
        return if (abs(value) < 1e-14) 0.0 else value
    }

    private class Parser(private val s: String, private val degrees: Boolean) {
        private var p = 0
        private fun skip() { while (p < s.length && s[p].isWhitespace()) p++ }
        private fun eat(c: Char): Boolean { skip(); if (p < s.length && s[p] == c) { p++; return true }; return false }
        private fun requireFinite(x: Double): Double { if (!x.isFinite()) throw ArithmeticException("Undefined mathematical operation"); return x }
        fun parse(): Double { val v = expression(); skip(); if (p != s.length) error("Unexpected input: '${s[p]}'"); return requireFinite(v) }
        private fun expression(): Double { var v = term(); while (true) { v = when { eat('+') -> v + term(); eat('-') -> v - term(); else -> return v } } }
        private fun term(): Double { var v = unary(); while (true) { v = when { eat('*') -> v * unary(); eat('/') -> { val d=unary(); if (d==0.0) throw ArithmeticException("Cannot divide by zero"); v/d }; else -> return v } } }
        private fun unary(): Double { return when { eat('+') -> unary(); eat('-') -> -unary(); else -> power() } }
        private fun power(): Double { var v = postfix(); if (eat('^')) v = v.pow(unary()); return v }
        private fun postfix(): Double { var v = atom(); while (true) { if (eat('!')) { if (v < 0 || v > 170 || v % 1.0 != 0.0) throw ArithmeticException("Factorial requires an integer from 0 to 170"); var r=1.0; for (i in 2..v.toInt()) r*=i; v=r } else if (eat('%')) v/=100.0 else return v } }
        private fun atom(): Double {
            skip(); if (eat('(')) { val v=expression(); if (!eat(')')) error("Missing closing parenthesis"); return v }
            if (p >= s.length) error("Expression is incomplete")
            if (s[p].isDigit() || s[p]=='.') return number()
            if (s[p].isLetter() || s[p]=='π') {
                val name=identifier()
                if (name=="pi" || name=="π") return PI
                if (name=="e") return E
                if (name=="ans") return 0.0
                if (!eat('(')) error("Function $name needs parentheses")
                val x=expression(); if (!eat(')')) error("Missing closing parenthesis")
                val a=if(degrees) Math.toRadians(x) else x
                return requireFinite(when(name.lowercase()) {
                    "sin" -> sin(a); "cos" -> cos(a); "tan" -> tan(a)
                    "asin", "sin⁻¹" -> { val z=asin(x); if(degrees) Math.toDegrees(z) else z }
                    "acos", "cos⁻¹" -> { val z=acos(x); if(degrees) Math.toDegrees(z) else z }
                    "atan", "tan⁻¹" -> { val z=atan(x); if(degrees) Math.toDegrees(z) else z }
                    "sinh" -> sinh(x); "cosh" -> cosh(x); "tanh" -> tanh(x)
                    "log" -> log10(x); "ln" -> ln(x); "sqrt" -> sqrt(x); "cbrt" -> cbrt(x)
                    "abs" -> abs(x); "exp" -> exp(x); "ten" -> 10.0.pow(x)
                    else -> error("Unknown function: $name")
                })
            }
            error("Unexpected character '${s[p]}'")
        }
        private fun identifier(): String { val start=p; while(p<s.length && (s[p].isLetter() || s[p]=='π' || s[p]=='⁻' || s[p]=='¹')) p++; return s.substring(start,p) }
        private fun number(): Double { val start=p; while(p<s.length && (s[p].isDigit() || s[p]=='.')) p++; if(p<s.length && (s[p]=='e'||s[p]=='E')) { p++; if(p<s.length && (s[p]=='+'||s[p]=='-')) p++; while(p<s.length&&s[p].isDigit()) p++ }; return s.substring(start,p).toDoubleOrNull() ?: error("Invalid number") }
    }
}

object ModeMath {
    fun baseConvert(value: String, from: Int, to: Int): String { require(from in listOf(2,8,10,16) && to in listOf(2,8,10,16)); val n=value.trim().toLongOrNull(from) ?: throw IllegalArgumentException("Invalid base-$from integer"); return java.lang.Long.toString(n,to).uppercase() }
    fun bitwise(a: Long, op: String, b: Long = 0): Long = when(op.uppercase()) { "AND" -> a and b; "OR" -> a or b; "XOR" -> a xor b; "NOT" -> a.inv(); "SHL" -> a shl b.toInt(); "SHR" -> a shr b.toInt(); else -> throw IllegalArgumentException("Unknown bitwise operator") }
    data class Complex(val re: Double, val im: Double) {
        operator fun plus(o: Complex)=Complex(re+o.re,im+o.im); operator fun minus(o: Complex)=Complex(re-o.re,im-o.im)
        operator fun times(o: Complex)=Complex(re*o.re-im*o.im,re*o.im+im*o.re)
        operator fun div(o: Complex): Complex { val d=o.re*o.re+o.im*o.im; require(d!=0.0){"Cannot divide by zero"}; return Complex((re*o.re+im*o.im)/d,(im*o.re-re*o.im)/d) }
        fun conjugate()=Complex(re,-im); fun modulus()=hypot(re,im); fun argument(deg:Boolean=true):Double { val a=atan2(im,re); return if(deg) Math.toDegrees(a) else a }
        override fun toString()="${fmt(re)} ${if(im<0) "-" else "+"} ${fmt(abs(im))}i"
    }
    fun determinant(a: Array<DoubleArray>): Double { require(a.isNotEmpty() && a.all{it.size==a.size}){"Matrix must be square"}; val m=Array(a.size){a[it].clone()}; var sign=1.0; var det=1.0; for(i in m.indices){ var pivot=i; for(r in i+1 until m.size) if(abs(m[r][i])>abs(m[pivot][i])) pivot=r; if(abs(m[pivot][i])<1e-12)return 0.0; if(pivot!=i){val t=m[i];m[i]=m[pivot];m[pivot]=t;sign=-sign}; val v=m[i][i]; det*=v; for(r in i+1 until m.size){val f=m[r][i]/v; for(c in i+1 until m.size)m[r][c]-=f*m[i][c]} }; return det*sign }
    fun multiply(a:Array<DoubleArray>,b:Array<DoubleArray>):Array<DoubleArray>{require(a.isNotEmpty()&&b.isNotEmpty()&&a[0].size==b.size){"Incompatible matrix dimensions"};return Array(a.size){r->DoubleArray(b[0].size){c->(b.indices).sumOf{k->a[r][k]*b[k][c]}}}}
    fun transpose(a:Array<DoubleArray>)=Array(a[0].size){c->DoubleArray(a.size){r->a[r][c]}}
    fun solveQuadratic(a:Double,b:Double,c:Double):List<Complex>{require(a!=0.0){"Leading coefficient cannot be zero"};val d=b*b-4*a*c;return if(d>=0)listOf(Complex((-b+sqrt(d))/(2*a),0.0),Complex((-b-sqrt(d))/(2*a),0.0)) else listOf(Complex(-b/(2*a),sqrt(-d)/(2*a)),Complex(-b/(2*a),-sqrt(-d)/(2*a)))}
    data class Stats(val count:Int,val sum:Double,val mean:Double,val median:Double,val min:Double,val max:Double,val populationVariance:Double,val sampleVariance:Double)
    fun statistics(values:List<Double>):Stats{require(values.isNotEmpty()){"Enter at least one value"};val s=values.sorted();val mean=s.average();val ss=s.sumOf{(it-mean).pow(2)};return Stats(s.size,s.sum(),mean,if(s.size%2==1)s[s.size/2] else (s[s.size/2-1]+s[s.size/2])/2,s.first(),s.last(),ss/s.size,if(s.size>1)ss/(s.size-1) else Double.NaN)}
    fun vectorDot(a:List<Double>,b:List<Double>) : Double { require(a.size==b.size){"Vector dimensions must match"};return a.indices.sumOf{a[it]*b[it]} }
    fun vectorCross(a:List<Double>,b:List<Double>):List<Double>{require(a.size==3&&b.size==3){"Cross product requires 3D vectors"};return listOf(a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0])}
    fun fraction(x:Double,maxDenominator:Int=100000):String{require(x.isFinite());if(x==0.0)return "0";var bestN=0L;var bestD=1;var err=Double.POSITIVE_INFINITY;for(d in 1..maxDenominator){val n=round(x*d).toLong();val e=abs(x-n.toDouble()/d);if(e<err){err=e;bestN=n;bestD=d};if(err<1e-12)break};val g=gcd(kotlin.math.abs(bestN),bestD.toLong());return "${bestN/g}/${bestD/g}"}
    private fun gcd(a:Long,b:Long):Long=if(b==0L)max(1L,a) else gcd(b,a%b)
    fun fmt(v:Double):String { if(v.isNaN())return "Undefined";if(v.isInfinite())return "Overflow";return if(abs(v)<1e12 && abs(v-round(v))<1e-10) round(v).toLong().toString() else String.format(java.util.Locale.US,"%.10g",v) }
}
