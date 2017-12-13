package ffgti.socialaddict

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View


/**
 * Created by Finn on 15.11.17.
 */
class CircularCompletionView : View {

    private var completionPercent = 0f
    private val paint = Paint()
    private var radius = 100.0f
    private var strokeSize = 20.0f
    private var textSize = 10.0f
    private var diameter = radius * 2

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        // TODO Auto-generated constructor stub
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        // TODO Auto-generated constructor stub
    }

    constructor(context: Context) : super(context) {
        // TODO Auto-generated constructor stub
    }

    fun setCompletionPercentage(completion: Float) {
        completionPercent = completion//size should change for different Resolution screens
        invalidate()
    }

    fun setTextSize(size: Float) {
        textSize = size//size should change for different Resolution screens
        invalidate()
    }

    fun setStrokeSize(size: Float) {
        strokeSize = size//size should change for different Resolution screens
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        if (width > height) {
            width = height
        } else {
            height = width
        }

        diameter = width.toFloat()
        radius = diameter / 2

        val newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)

        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        paint.color = Color.parseColor("#dedede")  // circle stroke color- grey
        paint.strokeWidth = strokeSize
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE

        canvas.drawCircle(radius, radius, radius - 10, paint)

        paint.color = Color.parseColor("#04B404")  // circle stroke color(indicating completion Percentage) - green
        paint.strokeWidth = strokeSize
        paint.style = Paint.Style.FILL

        val oval = RectF()
        paint.style = Paint.Style.STROKE
        oval.set(10f, 10f, (diameter - 10), (diameter - 10))

        canvas.drawArc(oval, 270f, completionPercent * 360 / 100, false, paint)

        paint.textAlign = Align.CENTER
        paint.color = Color.parseColor("#282828")  // text color - dark grey

        paint.style = Paint.Style.FILL
        paint.textSize = textSize

        canvas.drawText(completionPercent.toString() + "%", radius, radius + paint.textSize / 2, paint)

    }

}