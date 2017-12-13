package ffgti.socialaddict

import android.graphics.*
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPointInterface
import com.jjoe64.graphview.series.LineGraphSeries



/**
 * Created by Finn on 20.11.17.
 */
class LineGraphCurve<T : DataPointInterface>(data : Array<T>) : LineGraphSeries<T>(data) {
    private inner class Styles {
        /**
         * the thickness of the line.
         * This option will be ignored if you are
         * using a custom paint via [.setCustomPaint]
         */
        val thickness = 8

        /**
         * flag whether the area under the line to the bottom
         * of the viewport will be filled with a
         * specific background color.
         *
         * @see .backgroundColor
         */
        val drawBackground = false

        /**
         * the background color for the filling under
         * the line.
         *
         * @see .drawBackground
         */
        val backgroundColor = Color.argb(100, 172, 218, 255)
    }

    /**
     * wrapped styles
     */
    private var mStyles: Styles = Styles()

    /**
     * internal paint object
     */
    private var mPaint: Paint = Paint()

    /**
     * flag whether the line should be drawn as a path
     * or with single drawLine commands (more performance)
     * By default we use drawLine because it has much more peformance.
     * For some styling reasons it can make sense to draw as path.
     */
    private var mDrawAsPath = true

    /**
     * paint for the background
     */
    private var mPaintBackground: Paint = Paint()

    /**
     * path for the background filling
     */
    private var mPathBackground: Path = Path()

    /**
     * path to the line
     */
    private var mPath: Path = Path()

    override fun draw(graphView : GraphView, canvas : Canvas, isSecondScale : Boolean){
        resetDataPoints()

        // get data
        val maxX = graphView.viewport.getMaxX(false)
        val minX = graphView.viewport.getMinX(false)

        val maxY: Double = if (isSecondScale) {
            graphView.secondScale.getMaxY(false)
        } else {
            graphView.viewport.getMaxY(false)
        }

        val values = getValues(minX, maxX)

        // draw data
        mPaint.strokeWidth = mStyles.thickness.toFloat()
        mPaint.color = color
        mPaintBackground.color = mStyles.backgroundColor

        val paint: Paint
        val customPaint = Paint()
        customPaint.style = Paint.Style.STROKE
        val endColor = Color.parseColor("#0c486c")
        val startColor   = Color.parseColor("#cff09f")
        customPaint.shader = LinearGradient(0f,0f,0f,
                graphView.graphContentHeight.toFloat(), startColor,
                endColor, Shader.TileMode.MIRROR)
        customPaint.strokeWidth = 10f
        paint = customPaint

        mPath.reset()

        if (mStyles.drawBackground) {
            mPathBackground.reset()
        }


        val graphHeight = graphView.graphContentHeight.toFloat()
        val graphWidth = graphView.graphContentWidth.toFloat()


        val first = values.next()
        val second = values.next()
        val third = values.next()
        val fourth = values.next()
        val fifth = values.next()
        val sixth = values.next()
        val seventh = values.next()

        val knotsArr = arrayOf(
                BezierSplineUtil.Point(mapValue(first.x,maxX,graphWidth) + 30, mapValue(first.y,maxY,graphHeight)),
                BezierSplineUtil.Point(mapValue(second.x,maxX,graphWidth) + 30, mapValue(second.y,maxY,graphHeight)),
                BezierSplineUtil.Point(mapValue(third.x,maxX,graphWidth) + 30, mapValue(third.y,maxY,graphHeight)),
                BezierSplineUtil.Point(mapValue(fourth.x,maxX,graphWidth) + 30, mapValue(fourth.y,maxY,graphHeight)),
                BezierSplineUtil.Point(mapValue(fifth.x,maxX,graphWidth) + 30, mapValue(fifth.y,maxY,graphHeight)),
                BezierSplineUtil.Point(mapValue(sixth.x,maxX,graphWidth) + 30, mapValue(sixth.y,maxY,graphHeight)),
                BezierSplineUtil.Point(mapValue(seventh.x,maxX,graphWidth) + 30, mapValue(seventh.y,maxY,graphHeight)))
        val controlPoints = BezierSplineUtil.getCurveControlPoints(knotsArr)
        val firstCP = controlPoints[0]
        val secondCP = controlPoints[1]

        mPath.moveTo(knotsArr[0].x, knotsArr[0].y)

        for (i in firstCP.indices) {
            mPath.cubicTo(firstCP[i].x, firstCP[i].y,
                    secondCP[i].x, secondCP[i].y,
                    knotsArr[i + 1].x, knotsArr[i + 1].y)
        }


        if (mDrawAsPath) {
            // draw at the end
            paint.style = Paint.Style.STROKE
            canvas.drawPath(mPath, paint)
        }
    }


    private fun mapValue(value : Double, max : Double, scale: Float) : Float
            = (value / max * scale).toFloat()
}