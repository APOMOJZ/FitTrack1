package com.example.fittrack.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.fittrack.R

class BarChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val data = listOf(0.7f, 0.5f, 0.8f, 0.4f, 0.9f, 0.6f, 0.75f)
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val padding = 20f
        val barWidth = (width - (data.size + 1) * padding) / data.size

        data.forEachIndexed { index, value ->
            val left = padding + index * (barWidth + padding)
            val top = height - (value * height)
            val right = left + barWidth
            val bottom = height
            canvas.drawRect(left, top, right, bottom, barPaint)
        }
    }
}