package com.anwesh.uiprojects.halfarcfillstepview

/**
 * Created by anweshmishra on 06/04/19.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.*

val nodes : Int = 5
val arcs : Int = 2
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#9C27B0")
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.mirror(a : Float, b : Float) : Float = (1 - this) * a + this * b
fun Float.mirrorValue(a : Int, b : Int) : Float {
    val k : Float = scaleFactor()
    return k.mirror(a.inverse(), b.inverse())
}
fun Float.updateValue(dir : Int, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap

fun Canvas.drawXY(x : Float, y : Float, cb : (Canvas) -> Unit) {
    save()
    translate(x, y)
    cb(this)
    restore()
}

fun Canvas.drawRotateDeg(deg : Float, cb : (Canvas) -> Unit) {
    save()
    rotate(deg)
    cb(this)
    restore()
}

fun Canvas.drawClip(path : Path, cb : (Canvas) -> Unit) {
    save()
    clipPath(path)
    cb(this)
    restore()
}

fun Canvas.drawHalfArc(x : Float, sc1 : Float, sc2 : Float, r : Float, paint : Paint) {
    drawXY(x, 0f) {
        it.drawRotateDeg(180f * sc1) {
            val arcRect : RectF = RectF(-r, -r, r, r)
            val arcDeg : Float = 180f
            val path : Path = Path()
            path.addArc(arcRect, arcDeg, arcDeg)
            it.drawClip(path) {
                paint.style = Paint.Style.FILL
                drawRect(RectF(-r, -r, r, -r + r * sc2), paint)
            }
            paint.style = Paint.Style.STROKE
            drawArc(RectF(arcRect), arcDeg, arcDeg, false, paint)
        }
    }
}

fun Canvas.drawHAFNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    val xGap : Float = size / (arcs)
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    drawXY(w / 2, gap * (i + 1)) {
        for (j in 0..(arcs)) {
            val scj1 : Float = sc1.divideScale(j, arcs)
            val scj2 : Float = sc2.divideScale(j, arcs)
            it.drawHalfArc(-size + xGap * j, scj1, scj2, xGap / 2, paint)
        }
    }
}

class HalfArcFillStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}