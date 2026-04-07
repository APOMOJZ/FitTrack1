package com.example.fittrack.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.fittrack.R

class CircularProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress = 0.7f
    private val strokeWidthPx = 12f * resources.displayMetrics.density

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.divider)
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary)
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
        strokeCap = Paint.Cap.ROUND
    }

    private val rect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val size = if (w < h) w else h
        val centerSide = size / 2f
        
        val left = (w - size) / 2f + strokeWidthPx / 2f
        val top = (h - size) / 2f + strokeWidthPx / 2f
        val right = (w + size) / 2f - strokeWidthPx / 2f
        val bottom = (h + size) / 2f - strokeWidthPx / 2f
        
        rect.set(left, top, right, bottom)

        canvas.drawCircle(w / 2f, h / 2f, (size - strokeWidthPx) / 2f, backgroundPaint)
        canvas.drawArc(rect, -90f, progress * 360, false, progressPaint)
    }

    fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }
}