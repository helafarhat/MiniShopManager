package com.example.minishopmanager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class PrizeWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    val prizes = listOf(5, 10, 15, 20, 5, 10)

    private val colors = listOf(
        0xFFF2D9D9.toInt(),
        0xFFD9B7A8.toInt(),
        0xFFE9C8C3.toInt(),
        0xFFB77C79.toInt(),
        0xFFF4E4E5.toInt(),
        0xFFCF9F91.toInt()
    )

    private val sectorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF593F38.toInt()
        textSize = 15f * resources.displayMetrics.scaledDensity
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create(
            "serif",
            android.graphics.Typeface.BOLD
        )
    }

    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF9C6662.toInt()
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 3f * resources.displayMetrics.density
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) - 12f * resources.displayMetrics.density

        val rect = RectF(
            cx - radius,
            cy - radius,
            cx + radius,
            cy + radius
        )

        val sweep = 360f / prizes.size

        prizes.forEachIndexed { index, discount ->

            val startAngle = -90f + index * sweep

            sectorPaint.color = colors[index]

            canvas.drawArc(
                rect,
                startAngle,
                sweep,
                true,
                sectorPaint
            )

            canvas.drawArc(
                rect,
                startAngle,
                sweep,
                true,
                borderPaint
            )

            val middleAngle =
                Math.toRadians((startAngle + sweep / 2).toDouble())

            val textRadius = radius * 0.62f

            val x = cx + cos(middleAngle).toFloat() * textRadius
            val y = cy + sin(middleAngle).toFloat() * textRadius

            canvas.save()
            canvas.rotate(
                startAngle + sweep / 2 + 90f,
                x,
                y
            )

            canvas.drawText(
                "$discount% OFF",
                x,
                y,
                textPaint
            )

            canvas.restore()
        }

        canvas.drawCircle(
            cx,
            cy,
            radius * 0.19f,
            centerPaint
        )

        val centerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 17f * resources.displayMetrics.scaledDensity
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        canvas.drawText(
            "SPIN",
            cx,
            cy + centerTextPaint.textSize / 3f,
            centerTextPaint
        )
    }
}
