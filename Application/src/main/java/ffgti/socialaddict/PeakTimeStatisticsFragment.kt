package ffgti.socialaddict

import android.support.v4.app.Fragment
import android.app.usage.UsageStatsManager
import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import ffgti.socialaddict.R
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint

/**
 * Created by Finn on 21.11.17.
 */
class PeakTimeStatisticsFragment() : Fragment() {

    internal var mGraphView: GraphView? = null
    internal var mUsageStatsManager: UsageStatsManager? = null
    internal var mCurrentTimeView: TextView? = null
    internal var mUsageStatsInterval : Int = 0
    internal var mLegendCurrentView : TextView? = null
    internal var mLegendBeforeView : TextView? = null
    internal var mWakeRowTwo: RelativeLayout? = null
    internal var mWakeRowThree: RelativeLayout? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUsageStatsManager = activity
                .getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater!!.inflate(R.layout.fragment_peak_time_statistics, container, false)

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)
        mGraphView = rootView.findViewById(R.id.peak_graph)
        mCurrentTimeView = rootView.findViewById(R.id.current_time)
        mWakeRowTwo = rootView.findViewById(R.id.wake_row_two)
        mWakeRowThree = rootView.findViewById(R.id.wake_row_three)
        mLegendCurrentView = rootView.findViewById(R.id.legend_today)
        mLegendBeforeView = rootView.findViewById(R.id.legend_yesterday)
        when(mUsageStatsInterval){
            UsageStatsManager.INTERVAL_DAILY -> {
                mWakeRowTwo?.visibility = View.GONE
                mWakeRowThree?.visibility = View.GONE
                makeBarChartDaily(emptyArray())
                mCurrentTimeView?.text = "Daily"
                mLegendCurrentView?.text = "Today"
                mLegendBeforeView?.text = "Yesterday"
            }
            UsageStatsManager.INTERVAL_WEEKLY -> {
                mWakeRowTwo?.visibility = View.VISIBLE
                mWakeRowThree?.visibility = View.VISIBLE
                makeBarChartWeekly(emptyArray())
                mCurrentTimeView?.text = "Weekly"
                mLegendCurrentView?.text = "This Week"
                mLegendBeforeView?.text = "Last Week"
            }
            UsageStatsManager.INTERVAL_MONTHLY -> {
                mWakeRowTwo?.visibility = View.VISIBLE
                mWakeRowThree?.visibility = View.VISIBLE
                makeBarChartMonthly(emptyArray())
                mCurrentTimeView?.text = "Monthly"
                mLegendCurrentView?.text = "This Month"
                mLegendBeforeView?.text = "Last Month"
            }
            UsageStatsManager.INTERVAL_YEARLY -> {
                mWakeRowTwo?.visibility = View.VISIBLE
                mWakeRowThree?.visibility = View.VISIBLE
                makeBarChartYearly(emptyArray())
                mCurrentTimeView?.text = "Yearly"
                mLegendCurrentView?.text = "This Year"
                mLegendBeforeView?.text = "Last Year"
            }
        }
    }

    private fun makeBarChartDaily(usages: Array<Int>){
        mGraphView?.removeAllSeries()
        val data = Array<DataPoint>(24,init = {
            i -> DataPoint(i.toDouble(), (Math.random() * 100000) / 1000 / 60 / 60 + 10)
        })
        val series = BarGraphSeries(data)
        val customPaint = Paint()
        customPaint.strokeWidth = 10f
        customPaint.style = Paint.Style.FILL_AND_STROKE
        val startColor = Color.parseColor("#0c486c")
        val endColor   = Color.parseColor("#cff09f")
        customPaint.shader = LinearGradient(0f,0f,0f,mGraphView?.height!!.toFloat(), startColor,
                endColor, Shader.TileMode.MIRROR)
        series.customPaint = customPaint
        mGraphView?.addSeries(series)
        val data_yesterday = Array<DataPoint>(24,init = {
            i -> DataPoint(i.toDouble(), (Math.random() * 100000) / 1000 / 60 / 60 + 10)
        })
        val series_data_yesterday = BarGraphSeries(data_yesterday)
        val customPaint2 = Paint()
        customPaint2.strokeWidth = 10f
        customPaint2.style = Paint.Style.FILL_AND_STROKE
        customPaint2.color = Color.parseColor("#d9d9d9")
        series_data_yesterday.customPaint = customPaint2
        mGraphView?.addSeries(series_data_yesterday)
        mGraphView?.gridLabelRenderer?.gridStyle = GridLabelRenderer.GridStyle.HORIZONTAL
        val staticLabelsFormatter = StaticLabelsFormatter(mGraphView)
        staticLabelsFormatter.setHorizontalLabels(arrayOf("00:00","12:00","24:00"))
        mGraphView?.gridLabelRenderer?.labelFormatter = staticLabelsFormatter

    }

    private fun makeBarChartWeekly(usages: Array<Int>){
        mGraphView?.removeAllSeries()
        val data = Array<DataPoint>(7,init = {
            i -> DataPoint(i.toDouble(), (Math.random() * 10000000) / 1000 / 60 / 60 + 10)
        })
        val series = BarGraphSeries(data)
        val customPaint = Paint()
        customPaint.strokeWidth = 10f
        customPaint.style = Paint.Style.FILL_AND_STROKE
        val startColor = Color.parseColor("#0c486c")
        val endColor   = Color.parseColor("#cff09f")
        customPaint.shader = LinearGradient(0f,0f,0f,mGraphView?.height!!.toFloat(), startColor,
                endColor, Shader.TileMode.MIRROR)
        series.customPaint = customPaint
        mGraphView?.addSeries(series)
        val data_weekly = Array<DataPoint>( 7,init = {
            i -> DataPoint(i.toDouble(), (Math.random() * 10000000) / 1000 / 60 / 60 + 10)
        })
        val series_data_yesterday = BarGraphSeries(data_weekly)
        val customPaint2 = Paint()
        customPaint2.strokeWidth = 10f
        customPaint2.style = Paint.Style.FILL_AND_STROKE
        customPaint2.color = Color.parseColor("#d9d9d9")
        series_data_yesterday.customPaint = customPaint2
        mGraphView?.addSeries(series_data_yesterday)
        mGraphView?.gridLabelRenderer?.gridStyle = GridLabelRenderer.GridStyle.HORIZONTAL
        val staticLabelsFormatter = StaticLabelsFormatter(mGraphView)
        staticLabelsFormatter.setHorizontalLabels(arrayOf("Mon","Thu","Sun"))
        mGraphView?.gridLabelRenderer?.labelFormatter = staticLabelsFormatter

    }
    private fun makeBarChartMonthly(usages: Array<Int>){
        mGraphView?.removeAllSeries()
        val data = Array<DataPoint>(4,init = {
            i -> DataPoint(i.toDouble(), (Math.random() * 10000000) / 1000 / 60 / 60 + 10)
        })
        val series = BarGraphSeries(data)
        val customPaint = Paint()
        customPaint.strokeWidth = 10f
        customPaint.style = Paint.Style.FILL_AND_STROKE
        val startColor = Color.parseColor("#0c486c")
        val endColor   = Color.parseColor("#cff09f")
        customPaint.shader = LinearGradient(0f,0f,0f,mGraphView?.height!!.toFloat(), startColor,
                endColor, Shader.TileMode.MIRROR)
        series.customPaint = customPaint
        mGraphView?.addSeries(series)
        val data_monthly = Array<DataPoint>(4,init = {
            i -> DataPoint(i.toDouble(), (Math.random() * 10000000) / 1000 / 60 / 60 + 10)
        })
        val series_data_yesterday = BarGraphSeries(data_monthly)
        val customPaint2 = Paint()
        customPaint2.strokeWidth = 10f
        customPaint2.style = Paint.Style.FILL_AND_STROKE
        customPaint2.color = Color.parseColor("#d9d9d9")
        series_data_yesterday.customPaint = customPaint2
        mGraphView?.addSeries(series_data_yesterday)
        mGraphView?.gridLabelRenderer?.gridStyle = GridLabelRenderer.GridStyle.HORIZONTAL
        val staticLabelsFormatter = StaticLabelsFormatter(mGraphView)
        staticLabelsFormatter.setHorizontalLabels(arrayOf("Week1","Week4"))
        mGraphView?.gridLabelRenderer?.labelFormatter = staticLabelsFormatter

    }
    private fun makeBarChartYearly(usages: Array<Int>){
        mGraphView?.removeAllSeries()
        val data = Array<DataPoint>(12,init = {
            i -> DataPoint(i.toDouble(), (Math.random() * 100000000) / 1000 / 60 / 60 + 10)
        })
        val series = BarGraphSeries(data)
        val customPaint = Paint()
        customPaint.strokeWidth = 10f
        customPaint.style = Paint.Style.FILL_AND_STROKE
        val startColor = Color.parseColor("#0c486c")
        val endColor   = Color.parseColor("#cff09f")
        customPaint.shader = LinearGradient(0f,0f,0f,mGraphView?.height!!.toFloat(), startColor,
                endColor, Shader.TileMode.MIRROR)
        series.customPaint = customPaint
        mGraphView?.addSeries(series)
        val data_yesterday = Array<DataPoint>(12,init = {
            i -> DataPoint(i.toDouble(), (Math.random() * 100000000) / 1000 / 60 / 60 + 10)
        })
        val series_data_yesterday = BarGraphSeries(data_yesterday)
        val customPaint2 = Paint()
        customPaint2.strokeWidth = 10f
        customPaint2.style = Paint.Style.FILL_AND_STROKE
        customPaint2.color = Color.parseColor("#d9d9d9")
        series_data_yesterday.customPaint = customPaint2
        mGraphView?.addSeries(series_data_yesterday)
        mGraphView?.gridLabelRenderer?.gridStyle = GridLabelRenderer.GridStyle.HORIZONTAL
        val staticLabelsFormatter = StaticLabelsFormatter(mGraphView)
        staticLabelsFormatter.setHorizontalLabels(arrayOf("Jan","Jun","Dec"))
        mGraphView?.gridLabelRenderer?.labelFormatter = staticLabelsFormatter

    }

    override fun toString(): String = "PeakTimeStatistics"

    companion object {
        private val TAG = PeakTimeStatisticsFragment::class.java.simpleName

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment [AppUsageStatisticsFragment].
         */
        fun newInstance(mUsageStatsUsageInterval: Int): PeakTimeStatisticsFragment {
            val toReturn = PeakTimeStatisticsFragment()
            toReturn.mUsageStatsInterval = mUsageStatsUsageInterval
            return toReturn
        }
    }
}