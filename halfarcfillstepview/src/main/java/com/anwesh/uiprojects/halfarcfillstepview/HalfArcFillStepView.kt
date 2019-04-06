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
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap

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
                val rh : Float = r + paint.strokeWidth
                paint.style = Paint.Style.FILL
                drawRect(RectF(-r, -rh , r, -rh + rh * sc2), paint)
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
    val xGap : Float = (2 * size) / (arcs)
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    paint.color = foreColor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    drawXY(w / 2, gap * (i + 1)) {
        drawLine(-size, 0f, size, 0f, paint)
        for (j in 0..(arcs - 1)) {
            val scj1 : Float = sc1.divideScale(j, arcs)
            val scj2 : Float = sc2.divideScale(j, arcs)
            it.drawHalfArc(-size + xGap * j + xGap / 2, scj1, scj2, xGap / 2, paint)
        }
    }
}

class HalfArcFillStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateValue(dir, arcs, arcs)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class HAFNode(var i : Int, val state : State = State()) {

        private var next : HAFNode? = null
        private var prev : HAFNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = HAFNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawHAFNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : HAFNode {
            var curr : HAFNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class HalfArcFillStep(var i : Int) {

        private val root : HAFNode = HAFNode(0)
        private var curr : HAFNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : HalfArcFillStepView) {

        private val animator : Animator = Animator(view)
        private val hafs : HalfArcFillStep = HalfArcFillStep(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            hafs.draw(canvas, paint)
            animator.animate {
                hafs.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            hafs.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : HalfArcFillStepView {
            val view : HalfArcFillStepView = HalfArcFillStepView(activity)
            activity.setContentView(view)
            return view
        }
    }
}