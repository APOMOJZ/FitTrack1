package com.example.fittrack.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.fittrack.R

class LineChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var dataPoints: List<Float> = emptyList()
    private var labels: List<String> = emptyList()
    
    fun setData(newData: List<Float>, newLabels: List<String> = emptyList()) {
        dataPoints = newData
        labels = newLabels
        invalidate()
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary)
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary)
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dataPoints.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()
        val paddingLeft = 100f
        val paddingRight = 50f
        val paddingTop = 50f
        val paddingBottom = 80f
        
        val chartWidth = w - paddingLeft - paddingRight
        val chartHeight = h - paddingTop - paddingBottom

        val maxVal = (dataPoints.maxOrNull() ?: 0f).coerceAtLeast(2000f) * 1.2f
        val xStep = if (dataPoints.size > 1) chartWidth / (dataPoints.size - 1) else chartWidth
        
        // Draw Grid Lines
        for (i in 0..4) {
            val y = paddingTop + chartHeight - (chartHeight / 4 * i)
            canvas.drawLine(paddingLeft, y, paddingLeft + chartWidth, y, gridPaint)
            val label = (maxVal / 4 * i).toInt().toString()
            canvas.drawText(label, paddingLeft - 40f, y + 10f, textPaint.apply { textAlign = Paint.Align.RIGHT })
        }

        val path = Path()
        val fillPath = Path()

        dataPoints.forEachIndexed { index, value ->
            val x = paddingLeft + index * xStep
            val y = paddingTop + chartHeight - (value / maxVal * chartHeight)

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, paddingTop + chartHeight)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            
            if (index == dataPoints.size - 1) {
                fillPath.lineTo(x, paddingTop + chartHeight)
                fillPath.close()
            }
            
            // Draw Dots
            canvas.drawCircle(x, y, 10f, dotPaint)
            
            // Draw Labels
            if (labels.size > index) {
                canvas.drawText(labels[index], x, h - 20f, textPaint.apply { textAlign = Paint.Align.CENTER })
            }
        }

        val gradient = LinearGradient(0f, paddingTop, 0f, paddingTop + chartHeight,
            ContextCompat.getColor(context, R.color.primary) and 0x40FFFFFF,
            Color.TRANSPARENT, Shader.TileMode.CLAMP)
        fillPaint.shader = gradient
        
        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(path, linePaint)
    }
}
