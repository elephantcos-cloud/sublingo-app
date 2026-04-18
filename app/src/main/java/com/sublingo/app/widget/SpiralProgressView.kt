package com.sublingo.app.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class SpiralProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class Phase { UPLOAD, TRANSLATE, SAVE }

    private var _progress: Float = 0f          // 0..1 overall
    private var _phase: Phase = Phase.UPLOAD
    private var _currentLine: String = ""
    private var _phaseName: String = "Parsing"

    // Colors
    private val colorBg      = Color.parseColor("#12122A")
    private val colorUpload  = Color.parseColor("#00D4FF")
    private val colorTranslate = Color.parseColor("#7857FF")
    private val colorSave    = Color.parseColor("#00E676")

    private fun activeColor() = when (_phase) {
        Phase.UPLOAD    -> colorUpload
        Phase.TRANSLATE -> colorTranslate
        Phase.SAVE      -> colorSave
    }

    // Paints
    private val bgSpiralPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#1E1E3A")
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        maskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL)
    }

    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val percentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        letterSpacing = -0.05f
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        letterSpacing = 0.1f
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8888AA")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
    }

    // Spiral parameters
    private val TURNS      = 3.5f
    private val STEPS      = 600
    private val STROKE_BG  = 4f
    private val STROKE_ACT = 8f
    private val STROKE_GLOW = 18f

    fun setProgress(progress: Float, phase: Phase, phaseName: String, currentLine: String = "") {
        _progress = progress.coerceIn(0f, 1f)
        _phase    = phase
        _phaseName = phaseName
        _currentLine = currentLine
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height * 0.45f
        val maxR = min(width, height) / 2f * 0.82f
        val dp = resources.displayMetrics.density

        bgSpiralPaint.strokeWidth = STROKE_BG * dp
        glowPaint.strokeWidth = STROKE_GLOW * dp
        activePaint.strokeWidth = STROKE_ACT * dp
        percentPaint.textSize = 44f * dp
        labelPaint.textSize = 11f * dp
        linePaint.textSize = 10f * dp

        // Draw full background spiral
        drawSpiralPath(canvas, cx, cy, maxR, 1f, bgSpiralPaint)

        if (_progress > 0f) {
            // Glow layer
            val c = activeColor()
            glowPaint.color = Color.argb(50, Color.red(c), Color.green(c), Color.blue(c))
            drawSpiralPath(canvas, cx, cy, maxR, _progress, glowPaint)

            // Active spiral with gradient
            val shader = SweepGradient(cx, cy,
                intArrayOf(Color.argb(80, Color.red(c), Color.green(c), Color.blue(c)), c),
                floatArrayOf(0f, 1f))
            activePaint.shader = shader
            activePaint.color = c
            drawSpiralPath(canvas, cx, cy, maxR, _progress, activePaint)

            // Bright dot at spiral tip
            val tipAngle = -_progress * TURNS * 2f * Math.PI.toFloat() - Math.PI.toFloat() / 2f
            val tipR = maxR * (1f - _progress)
            val tipX = cx + tipR * cos(tipAngle)
            val tipY = cy + tipR * sin(tipAngle)
            dotPaint.color = c
            dotPaint.maskFilter = BlurMaskFilter(12f * dp, BlurMaskFilter.Blur.NORMAL)
            canvas.drawCircle(tipX, tipY, 8f * dp, dotPaint)
            dotPaint.maskFilter = null
            dotPaint.color = Color.WHITE
            canvas.drawCircle(tipX, tipY, 4f * dp, dotPaint)
        }

        // Draw percentage text
        val pct = (_progress * 100f).toInt()
        canvas.drawText("$pct%", cx, cy + percentPaint.textSize * 0.35f, percentPaint)

        // Draw phase label
        val c = activeColor()
        labelPaint.color = c
        val phaseUp = _phaseName.uppercase()
        canvas.drawText(phaseUp, cx, cy + percentPaint.textSize * 0.35f + labelPaint.textSize * 1.6f, labelPaint)

        // Draw phase dots at bottom of spiral
        val dotY = cy + maxR + 20f * dp
        val dotSpacing = 60f * dp
        val phases = listOf(Phase.UPLOAD, Phase.TRANSLATE, Phase.SAVE)
        val labels = listOf("Parse", "Translate", "Save")
        val colors = listOf(colorUpload, colorTranslate, colorSave)

        phases.forEachIndexed { i, ph ->
            val dotX = cx + (i - 1) * dotSpacing
            dotPaint.maskFilter = null
            if (ph == _phase) {
                dotPaint.color = colors[i]
                dotPaint.maskFilter = BlurMaskFilter(8f * dp, BlurMaskFilter.Blur.NORMAL)
                canvas.drawCircle(dotX, dotY, 7f * dp, dotPaint)
                dotPaint.maskFilter = null
                dotPaint.color = Color.WHITE
                canvas.drawCircle(dotX, dotY, 4f * dp, dotPaint)
            } else {
                dotPaint.color = Color.parseColor("#333355")
                canvas.drawCircle(dotX, dotY, 5f * dp, dotPaint)
            }
            // Connector line
            if (i < phases.size - 1) {
                val lineP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#1E1E3A")
                    strokeWidth = 1.5f * dp
                    style = Paint.Style.STROKE
                }
                canvas.drawLine(dotX + 10f * dp, dotY, dotX + dotSpacing - 10f * dp, dotY, lineP)
            }
        }

        // Label below dots
        val lblY = dotY + 16f * dp
        labels.forEachIndexed { i, lbl ->
            val lx = cx + (i - 1) * dotSpacing
            val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (phases[i] == _phase) colors[i] else Color.parseColor("#44445A")
                textSize = 9f * dp
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                letterSpacing = 0.05f
            }
            canvas.drawText(lbl.uppercase(), lx, lblY, smallPaint)
        }

        // Current line text
        if (_currentLine.isNotEmpty()) {
            val lineY = lblY + 22f * dp
            val maxWidth = width * 0.8f
            val ellipsized = if (_currentLine.length > 40) _currentLine.take(37) + "..." else _currentLine
            canvas.drawText(ellipsized, cx, lineY, linePaint)
        }
    }

    private fun drawSpiralPath(canvas: Canvas, cx: Float, cy: Float, maxR: Float, progress: Float, paint: Paint) {
        if (progress <= 0f) return
        val path = Path()
        val totalAngle = TURNS * 2f * Math.PI.toFloat()
        val drawnAngle = progress * totalAngle
        val steps = (STEPS * progress).toInt().coerceAtLeast(20)

        for (i in 0..steps) {
            val t = i.toFloat() / steps * drawnAngle
            val r = maxR * (1f - t / totalAngle)
            // Clockwise from top: x=sin(t), y=-cos(t)
            val x = cx + r * sin(t.toDouble()).toFloat()
            val y = cy - r * cos(t.toDouble()).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, paint)
    }
}
