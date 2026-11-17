package com.rd184.calculator

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import kotlin.math.*

class MainActivity : Activity() {
    private val bg=0xFF000024.toInt(); private val navy=0xFF08163D.toInt(); private val slate=0xFF39466D.toInt(); private val pale=0xFFF0F6FF.toInt(); private val yellow=0xFFFFE77A.toInt(); private val purple=0xFFAAA0FF.toInt(); private val peach=0xFFEBA58F.toInt()
    private lateinit var history:HistoryStore; private lateinit var display:TextView; private lateinit var expression:TextView; private lateinit var keys:LinearLayout
    private var input=""; private val modeInputs=mutableMapOf<String,String>(); private var answer=0.0; private var degrees=true; private var shift=false; private var hyp=false; private var mode="NORMAL"; private var base=10
    private val engine get()=CalculatorEngine(degrees)
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);history=HistoryStore(this);input=savedInstanceState?.getString("input")?:"";mode=savedInstanceState?.getString("mode")?:"NORMAL";base=savedInstanceState?.getInt("base",10)?:10;degrees=savedInstanceState?.getBoolean("degrees",true)?:true;answer=savedInstanceState?.getDouble("answer",0.0)?:0.0;buildUi()}
    override fun onSaveInstanceState(outState:Bundle){outState.putString("input",input);outState.putString("mode",mode);outState.putInt("base",base);outState.putBoolean("degrees",degrees);outState.putDouble("answer",answer);super.onSaveInstanceState(outState)}
    private fun buildUi(){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(bg);setPadding(dp(8),dp(8),dp(8),dp(8))};setContentView(root)
        val top=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL;orientation=LinearLayout.HORIZONTAL}
        top.addView(label("RD184",yellow,16f,true),LinearLayout.LayoutParams(0,dp(34),1f));top.addView(label("${mode}  ${if(degrees)"DEG" else "RAD"}",pale,14f,true));val m=button("MODE",slate,pale){showModes()};top.addView(m);root.addView(top)
        val screen=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.BOTTOM;setPadding(dp(14),dp(12),dp(14),dp(12));background=rounded(pale,18)}
        expression=TextView(this).apply{setTextColor(0xFF52627F.toInt());textSize=18f;gravity=Gravity.END;maxLines=2};display=TextView(this).apply{setTextColor(0xFF101A39.toInt());textSize=34f;gravity=Gravity.END;maxLines=2;typeface=Typeface.MONOSPACE}
        screen.addView(expression,LinearLayout.LayoutParams(-1,0,1f));screen.addView(display);root.addView(screen,LinearLayout.LayoutParams(-1,0,0.27f))
        val tools=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        tools.addView(button("HISTORY",slate,pale){showHistory()},LinearLayout.LayoutParams(0,dp(42),1f));tools.addView(button(if(degrees)"DEG" else "RAD",slate,yellow){degrees=!degrees;refresh() },LinearLayout.LayoutParams(0,dp(42),1f));tools.addView(button("SHIFT",0xFFFFE38A.toInt(),0xFF11162F.toInt()){shift=!shift;refresh()},LinearLayout.LayoutParams(0,dp(42),1f));root.addView(tools)
        val sc=ScrollView(this);keys=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};sc.addView(keys);root.addView(sc,LinearLayout.LayoutParams(-1,0,0.73f));refresh()
    }
    private fun refresh(){display.text=if(input.isBlank())"0" else input;expression.text="${mode}  •  ${if(degrees)"DEG" else "RAD"}";rebuildKeys()}
    private fun rebuildKeys(){keys.removeAllViews(); when(mode){"NORMAL"->normalKeys();"BASE-N"->baseKeys();else->modeKeys()}}
    private fun normalKeys(){
        val rows=listOf(
            listOf("CALC","∫dx","x⁻¹","logᵧx","MODE"),
            listOf("x/y","√x","x²","xʸ","log","ln"),
            listOf("(-)","%","hyp","sin","cos","tan"),
            listOf("RCL","ENG","(",")","S⇔D","AC"),
            listOf("7","8","9","⌫","AC"),listOf("4","5","6","×","÷"),listOf("1","2","3","+","−"),listOf("0",".","Exp","Ans","=")
        )
        rows.forEachIndexed{idx,row->addRow(row,idx>=4)}
        addRow(listOf("sin⁻¹","cos⁻¹","tan⁻¹","π","e","!"),false)
    }
    private fun baseKeys(){
        addRow(listOf("BIN","OCT","DEC","HEX"),false)
        val digits=when(base){2->listOf("0","1");8->(0..7).map{it.toString()};10->(0..9).map{it.toString()};else->(0..9).map{it.toString()}+listOf("A","B","C","D","E","F")}
        digits.chunked(4).forEach{addRow(it,false)};addRow(listOf("AND","OR","XOR","NOT","SHL","SHR"),false);addRow(listOf("+","−","×","÷","⌫","AC","="),true)
    }
    private fun modeKeys(){
        val actions=when(mode){
            "COMPLEX"->listOf("Add","Subtract","Multiply","Divide","Conjugate","Modulus","Argument","Polar form")
            "MATRIX"->listOf("Matrix add","Matrix subtract","Matrix multiply","Transpose","Determinant","Inverse (2×2)")
            "EQUATION"->listOf("Quadratic solver","Linear solver","Quadratic value")
            "STATISTICS"->listOf("Mean / Median","Variance / StdDev","Min / Max","Sum / Sum squares")
            "TABLE"->listOf("Generate table")
            "VECTOR"->listOf("Add vectors","Subtract vectors","Dot product","Cross product","Magnitude","Unit vector","Angle")
            "FRACTION"->listOf("Decimal → fraction","Simplify fraction","Fraction → decimal","Mixed number")
            else->emptyList()
        }
        actions.chunked(2).forEach{addRow(it,false)};addRow(listOf("Back to NORMAL","HISTORY"),false)
    }
    private fun addRow(items:List<String>,numeric:Boolean){val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};items.forEach{key->val color=when(key){"AC","⌫"->peach;in listOf("+","−","×","÷","=")->navy;in listOf("SHIFT","BIN","OCT","DEC","HEX")->slate;else->if(numeric)navy else slate};val fg=if(color==peach)0xFF11162F.toInt() else pale;row.addView(button(key,color,fg){press(key)},LinearLayout.LayoutParams(0,dp(if(numeric)64 else 54),1f).apply{setMargins(dp(3),dp(3),dp(3),dp(3))})};keys.addView(row)}
    private fun press(k:String) {
        try {
            when (k) {
                "MODE" -> showModes()
                "HISTORY" -> showHistory()
                "Back to NORMAL" -> { modeInputs[mode]=input; mode="NORMAL"; input=modeInputs[mode]?:"" }
                "BIN" -> changeBase(2)
                "OCT" -> changeBase(8)
                "DEC" -> changeBase(10)
                "HEX" -> changeBase(16)
                "AC" -> input=""
                "⌫" -> input=input.dropLast(1)
                "=" -> calculate()
                "Ans" -> input+=ModeMath.fmt(answer)
                "π" -> input+="pi"
                "e" -> input+="e"
                "Exp" -> input+="E"
                "(-)" -> input+="-"
                "S⇔D" -> if(input.isNotBlank()) input=ModeMath.fraction(engine.evaluate(input))
                "sin⁻¹","cos⁻¹","tan⁻¹" -> input+=when(k){"sin⁻¹"->"asin(";"cos⁻¹"->"acos(";else->"atan("}
                "hyp" -> { hyp=!hyp; showMessage("Hyperbolic mode",if(hyp)"Hyperbolic functions enabled" else "Hyperbolic functions disabled") }
                "sin","cos","tan" -> { input+=(if(hyp)"${k}h(" else if(shift)"a$k(" else "$k("); shift=false }
                "log","ln","√x" -> input+=when(k){"√x"->"sqrt(";else->"$k("}
                "x²" -> input="($input)^2"
                "xʸ" -> input+="^"
                "x⁻¹" -> input="1/($input)"
                "x/y" -> input+="/"
                "!" -> input+="!"
                "CALC" -> prompt("Evaluate expression","Enter expression, e.g. 2*sin(30)"){s->ModeMath.fmt(engine.evaluate(s))}
                "∫dx" -> integralDialog()
                "RCL" -> showHistory()
                "ENG" -> { if(input.isNotBlank()) { val v=engine.evaluate(input); val exponent=if(v==0.0)0 else floor(log10(abs(v))/3.0).toInt()*3; input=ModeMath.fmt(v/10.0.pow(exponent))+"E"+exponent } }
                "AND","OR","XOR","NOT","SHL","SHR" -> bitwiseDialog(k)
                "Add","Subtract","Multiply","Divide","Conjugate","Modulus","Argument","Polar form" -> complexDialog(k)
                "Matrix add","Matrix subtract","Matrix multiply","Transpose","Determinant","Inverse (2×2)" -> matrixDialog(k)
                "Quadratic solver","Linear solver","Quadratic value" -> equationDialog(k)
                "Mean / Median","Variance / StdDev","Min / Max","Sum / Sum squares" -> statsDialog(k)
                "Generate table" -> tableDialog()
                "Add vectors","Subtract vectors","Dot product","Cross product","Magnitude","Unit vector","Angle" -> vectorDialog(k)
                "Decimal → fraction","Simplify fraction","Fraction → decimal","Mixed number" -> fractionDialog(k)
                else -> {
                    val validDigit = if(mode=="BASE-N") k in listOf("+","−","×","÷","(",")","-","⌫","AC","=") || k.toIntOrNull(16)?.let{it<base}==true || (base==16 && k in listOf("A","B","C","D","E","F")) else true
                    if(validDigit && k.length==1 && (k[0].isDigit() || k in listOf(".","+","−","×","÷","(",")","%","A","B","C","D","E","F")))
                        input+=if(k=="×")"*" else if(k=="÷")"/" else if(k=="−")"-" else k
                }
            }
            refresh()
        } catch(e:Exception) { showMessage("Input error",e.message?:"Invalid input") }
    }
    private fun calculate(){if(input.isBlank())return;val old=input;val result=if(mode=="BASE-N")evalBaseExpression(old,base).toString(base).uppercase() else ModeMath.fmt(engine.evaluate(old));answer=result.toDoubleOrNull()?:answer;history.add(mode,old,result);expression.text=old;input=result}

    private fun changeBase(next:Int){
        if(input.isNotBlank()){
            val value=if(mode=="BASE-N") input.toLongOrNull(base) ?: throw IllegalArgumentException("Current value is not valid in base $base") else engine.evaluate(input).toLong()
            input=java.lang.Long.toString(value,next).uppercase()
        }
        base=next;mode="BASE-N";refresh()
    }
    private fun evalBaseExpression(src:String, radix:Int):Long {
        val normalized=src.replace("×","*").replace("÷","/").replace("−","-")
        val tokens=Regex("[0-9A-Fa-f]+|[+*/()\\-]").findAll(normalized).map{it.value}.toList()
        require(tokens.isNotEmpty() && tokens.joinToString("")==normalized){"Invalid base-$radix expression"}
        var i=0
        lateinit var expr:()->Long
        lateinit var product:()->Long
        fun atom():Long {
            if(i>=tokens.size) throw IllegalArgumentException("Incomplete expression")
            val t=tokens[i++]
            if(t=="("){val v=expr();require(i<tokens.size&&tokens[i++]==")");return v}
            if(t=="-") return -atom()
            return t.toLongOrNull(radix) ?: throw IllegalArgumentException("Invalid base-$radix digit: $t")
        }
        product={var v=atom();while(i<tokens.size&&tokens[i] in listOf("*","/")){val op=tokens[i++];val b=atom();v=if(op=="*")Math.multiplyExact(v,b) else {require(b!=0L){"Cannot divide by zero"};v/b}};v}
        expr={var v=product();while(i<tokens.size&&tokens[i] in listOf("+","-")){val op=tokens[i++];val b=product();v=if(op=="+")Math.addExact(v,b) else Math.subtractExact(v,b)};v}
        val result=expr();require(i==tokens.size){"Unexpected token"};return result
    }
    private fun showModes(){val modes=arrayOf("NORMAL","BASE-N","COMPLEX","MATRIX","EQUATION","STATISTICS","TABLE","VECTOR","FRACTION");AlertDialog.Builder(this).setTitle("Select calculator mode").setSingleChoiceItems(modes,modes.indexOf(mode)){d,w->modeInputs[mode]=input;mode=modes[w];input=modeInputs[mode]?:"";d.dismiss();refresh()}.setNegativeButton("Cancel",null).show()}
    private fun showHistory(){val rows=history.all();if(rows.isEmpty()){showMessage("History","No saved calculations yet.");return};val labels=rows.map{"${it.first}: ${it.second} = ${it.third}"}.toTypedArray();AlertDialog.Builder(this).setTitle("Calculation history").setItems(labels){_,i->mode=rows[i].first;input=rows[i].second;refresh()}.setNeutralButton("Clear history"){_,_->AlertDialog.Builder(this).setTitle("Delete all history?").setMessage("This permanently deletes saved calculations from this device.").setNegativeButton("Cancel",null).setPositiveButton("Delete"){_,_->history.clear();showMessage("History","History permanently cleared.")}.show()}.setNegativeButton("Close",null).show()}

    private fun prompt(title: String, hint: String, run: (String) -> String) {
        val field = EditText(this).apply {
            setTextColor(pale)
            setHintTextColor(0xFFB7C2D8.toInt())
            this.hint = hint
            setSingleLine(false)
            minLines = 1
        }

        val box = LinearLayout(this).apply {
            setPadding(dp(18), dp(8), dp(18), dp(4))
            addView(field)
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(box)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Calculate") { _, _ ->
                try {
                    val res = run(field.text.toString())
                    showMessage("Result", res)
                    history.add(
                        mode,
                        title + ": " + field.text.toString(),
                        res
                    )
                } catch (e: Exception) {
                    showMessage(
                        "Calculation error",
                        e.message ?: "Invalid input"
                    )
                }
            }
            .show()
    }

    private fun complexDialog(op:String) {
        prompt("Complex $op","Enter a,b for a+bi; for binary operations enter A as a,b; B as c,d") { s ->
            val q=s.split(';')
            fun parse(t:String):ModeMath.Complex { val z=t.split(','); require(z.size==2); return ModeMath.Complex(z[0].trim().toDouble(),z[1].trim().toDouble()) }
            val a=parse(q[0]); val b=if(q.size>1)parse(q[1]) else ModeMath.Complex(0.0,0.0)
            when(op) {
                "Add" -> { require(q.size==2); (a+b).toString() }
                "Subtract" -> { require(q.size==2); (a-b).toString() }
                "Multiply" -> { require(q.size==2); (a*b).toString() }
                "Divide" -> { require(q.size==2); (a/b).toString() }
                "Conjugate" -> a.conjugate().toString()
                "Modulus" -> ModeMath.fmt(a.modulus())
                "Argument" -> ModeMath.fmt(a.argument(degrees))
                else -> "r=${ModeMath.fmt(a.modulus())}, θ=${ModeMath.fmt(a.argument(degrees))}${if(degrees)"°" else " rad"}"
            }
        }
    }
    private fun matrixDialog(op:String) {
        prompt("Matrix $op","Rows separated by ;, values by comma. For two matrices use A|B. Example: 1,2;3,4") { s ->
            fun mat(t:String):Array<DoubleArray> = t.split(';').map { row -> row.split(',').map { it.trim().toDouble() }.toDoubleArray() }.toTypedArray().also { m -> require(m.isNotEmpty() && m.all { it.size==m[0].size }) { "Matrix rows must have equal lengths" } }
            val parts=s.split("|",limit=2); val a=mat(parts[0])
            when(op) {
                "Transpose" -> ModeMath.transpose(a).joinToString("\n") { row -> row.joinToString(", ") { ModeMath.fmt(it) } }
                "Determinant" -> ModeMath.fmt(ModeMath.determinant(a))
                "Inverse (2×2)" -> { require(a.size==2&&a.all{it.size==2}); val d=ModeMath.determinant(a); require(abs(d)>1e-12){"Matrix is singular"}; "${a[1][1]/d}, ${-a[0][1]/d}\n${-a[1][0]/d}, ${a[0][0]/d}" }
                else -> {
                    require(parts.size==2){"Enter A|B for two matrices"}; val b=mat(parts[1])
                    val out=when(op) {
                        "Matrix multiply" -> ModeMath.multiply(a,b)
                        else -> { require(a.size==b.size&&a.indices.all{a[it].size==b[it].size}); Array(a.size){r->DoubleArray(a[r].size){c->if(op=="Matrix add")a[r][c]+b[r][c] else a[r][c]-b[r][c]}} }
                    }
                    out.joinToString("\n") { row -> row.joinToString(", ") { ModeMath.fmt(it) } }
                }
            }
        }
    }
    private fun equationDialog(op:String){prompt(op,if(op=="Quadratic solver")"Enter a,b,c for ax²+bx+c=0" else "Enter coefficients separated by commas"){s->val v=s.split(',').map{it.trim().toDouble()};when(op){"Quadratic solver"->{require(v.size==3);ModeMath.solveQuadratic(v[0],v[1],v[2]).joinToString("; ")};"Linear solver"->{require(v.size==2&&v[0]!=0.0);"x = ${ModeMath.fmt(-v[1]/v[0])}"};else->{require(v.size==2);"P(${ModeMath.fmt(v[1])}) = ${ModeMath.fmt(v[0]*v[1]*v[1])}"}}}}
    private fun statsDialog(op:String){prompt(op,"Enter comma-separated data values"){s->val st=ModeMath.statistics(s.split(',').map{it.trim().toDouble()});when(op){"Mean / Median"->"Mean=${ModeMath.fmt(st.mean)}\nMedian=${ModeMath.fmt(st.median)}";"Variance / StdDev"->"Population variance=${ModeMath.fmt(st.populationVariance)}\nPopulation SD=${ModeMath.fmt(sqrt(st.populationVariance))}\nSample variance=${ModeMath.fmt(st.sampleVariance)}";"Min / Max"->"Min=${st.min}, Max=${st.max}";else->"Sum=${st.sum}\nSum of squares=${ModeMath.fmt(s.split(',').sumOf{it.trim().toDouble().pow(2)})}"}}}
    private fun integralDialog(){
        prompt("Numerical integration","Enter f(x),a,b. Example: x^2,0,3"){s->
            val p=s.split(',');require(p.size==3);val f=p[0];val a=p[1].toDouble();val b=p[2].toDouble();val n=1000;val h=(b-a)/n
            var sum=engine.evaluate(f.replace("x","($a)"))+engine.evaluate(f.replace("x","($b)"))
            for(i in 1 until n){val x=a+i*h;sum+=(if(i%2==0)2 else 4)*engine.evaluate(f.replace("x","($x)"))}
            ModeMath.fmt(sum*h/3.0)
        }
    }
    private fun tableDialog(){prompt("Function table","Enter function of x, start,end,step. Example: sin(x),0,90,15"){s->val p=s.split(',');require(p.size==4);val f=p[0];val start=p[1].toDouble();val end=p[2].toDouble();val step=p[3].toDouble();require(step>0&&end>=start&&((end-start)/step)<200);buildString{var x=start;while(x<=end+1e-10){val y=engine.evaluate(f.replace("x","($x)"));append("x=${ModeMath.fmt(x)}  y=${ModeMath.fmt(y)}\n");x+=step}}}}
    private fun vectorDialog(op:String) {
        prompt("Vector $op","Enter vector A; vector B (comma-separated). Example: 1,2,3;4,5,6") { s ->
            val ps=s.split(';')
            fun vec(t:String):List<Double> = t.split(',').map{it.trim().toDouble()}
            val a=vec(ps[0]); val b=if(ps.size>1)vec(ps[1]) else emptyList()
            when(op) {
                "Magnitude" -> ModeMath.fmt(sqrt(a.sumOf{it*it}))
                "Unit vector" -> { val n=sqrt(a.sumOf{it*it}); require(n!=0.0); a.map{it/n}.joinToString(", "){ModeMath.fmt(it)} }
                "Dot product" -> ModeMath.fmt(ModeMath.vectorDot(a,b))
                "Cross product" -> ModeMath.vectorCross(a,b).joinToString(", "){ModeMath.fmt(it)}
                "Angle" -> { val d=ModeMath.vectorDot(a,b); val n=sqrt(a.sumOf{it*it}*b.sumOf{it*it}); require(n!=0.0); ModeMath.fmt(if(degrees)Math.toDegrees(acos((d/n).coerceIn(-1.0,1.0))) else acos((d/n).coerceIn(-1.0,1.0))) }
                else -> { require(a.size==b.size); a.indices.map{if(op=="Add vectors")a[it]+b[it] else a[it]-b[it]}.joinToString(", "){ModeMath.fmt(it)} }
            }
        }
    }
    private fun fractionDialog(op:String){prompt(op,"Enter a decimal or numerator/denominator"){s->when(op){"Decimal → fraction"->ModeMath.fraction(s.toDouble());"Fraction → decimal"->{val p=s.split('/');require(p.size==2);ModeMath.fmt(p[0].toDouble()/p[1].toDouble())};"Simplify fraction"->{val p=s.split('/');require(p.size==2);ModeMath.fraction(p[0].toDouble()/p[1].toDouble())};else->{val x=s.toDouble();val whole=x.toLong();val frac=ModeMath.fraction(abs(x-whole));"$whole + $frac"}}}}
    private fun bitwiseDialog(op:String){prompt("Bitwise $op","Enter integer A,B (B is ignored for NOT)"){s->val p=s.split(',');val a=p[0].trim().toLong();val b=if(p.size>1)p[1].trim().toLong() else 0L;ModeMath.bitwise(a,op,b).toString()}}
    private fun showMessage(title:String,message:String){AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK",null).show()}
    private fun label(t:String,c:Int,size:Float,bold:Boolean=false)=TextView(this).apply{text=t;setTextColor(c);textSize=size;if(bold)typeface=Typeface.DEFAULT_BOLD;gravity=Gravity.CENTER_VERTICAL}
    private fun button(t:String,color:Int,fg:Int,action:()->Unit)=TextView(this).apply{text=t;setTextColor(fg);textSize=if(t.length>7)13f else 18f;gravity=Gravity.CENTER;typeface=Typeface.create("sans-serif",Typeface.NORMAL);background=rounded(color,16);isClickable=true;isFocusable=true;setPadding(dp(3),dp(3),dp(3),dp(3));setOnClickListener{action()}}
    private fun rounded(color:Int,r: Int):GradientDrawable=GradientDrawable().apply{setColor(color);cornerRadius=dp(r).toFloat();setStroke(dp(1),0xFF1D2B57.toInt())}
    private fun dp(v:Int)= (v*resources.displayMetrics.density).toInt()
}
