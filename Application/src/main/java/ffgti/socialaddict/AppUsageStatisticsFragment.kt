/*
* Copyright 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package ffgti.socialaddict

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import ffgti.socialaddict.R
import ffgti.socialaddict.UsageListAdapter
import java.util.*
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.BarGraphSeries
import ffgti.socialaddict.FragmentChangeListener


/**
 * Fragment that demonstrates how to use App Usage Statistics API.
 */
class AppUsageStatisticsFragment : Fragment() {

    //VisibleForTesting for variables below
    internal var mUsageStatsManager: UsageStatsManager? = null
    internal var mTotalTimeView : TextView? = null
    internal var mUsageListAdapter: UsageListAdapter? = null
    internal var mRecyclerView: RecyclerView? = null
    internal var mLayoutManager: RecyclerView.LayoutManager? = null
    internal var mOpenUsageSettingButton: Button? = null
    internal var mSpinner: Spinner? = null
    internal var mCircularCompletionView : CircularCompletionView? = null
    internal var mGraphView : GraphView? = null
    internal var mPeakView : TextView? = null

    class PhonePickedUp {
        var phonePickedUP = 0
        var lastDate = Calendar.getInstance().time

        fun update(currentDate : Date){
            if(lastDate.day == currentDate.day){
                phonePickedUP++
            }
            else{
                phonePickedUP = 0
                lastDate = currentDate
            }
        }

    }

    internal val phonePickUp = PhonePickedUp()
    private var mPickUpView : TextView? = null
    private var mPickUpHeader : TextView? = null

    private var mPowerKeyReceiver: BroadcastReceiver? = null

    private fun registerBroadcastReceiver() {
        val theFilter = IntentFilter()
        /** System Defined Broadcast  */
        theFilter.addAction(Intent.ACTION_SCREEN_ON)
        theFilter.addAction(Intent.ACTION_SCREEN_OFF)

        mPowerKeyReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val strAction = intent.action

                if (strAction == Intent.ACTION_SCREEN_ON) {
                    phonePickUp.update(Calendar.getInstance().time)
                }
            }
        }

        context.applicationContext.registerReceiver(mPowerKeyReceiver, theFilter)
    }

    private fun unregisterReceiver() {
        val apiLevel = Build.VERSION.SDK_INT

        if (apiLevel >= 7) {
            try {
                context.applicationContext.unregisterReceiver(mPowerKeyReceiver)
            } catch (e: IllegalArgumentException) {
                mPowerKeyReceiver = null
            }

        } else {
            context.applicationContext.unregisterReceiver(mPowerKeyReceiver)
            mPowerKeyReceiver = null
        }
    }

    override fun onResume() {
        super.onResume()
        mPickUpView?.text = phonePickUp.phonePickedUP.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mUsageStatsManager = activity
                .getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager //Context.USAGE_STATS_SERVICE
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater!!.inflate(R.layout.fragment_app_usage_statistics, container, false)

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)
        registerBroadcastReceiver()
        mGraphView = rootView.findViewById(R.id.graph)
        mPeakView = rootView.findViewById(R.id.Peak)
        mPickUpHeader = rootView.findViewById(R.id.Pick_Header)
        mUsageListAdapter = UsageListAdapter()
        mTotalTimeView = rootView.findViewById(R.id.total_time)
        mPickUpView = rootView.findViewById(R.id.Pick)
        mUsageListAdapter?.mPackageManager = context.packageManager
        mRecyclerView = rootView.findViewById<View>(R.id.recyclerview_app_usage) as RecyclerView
        mLayoutManager = mRecyclerView?.layoutManager
        mCircularCompletionView = rootView.findViewById<View>(R.id.UsageCircle) as CircularCompletionView
        mRecyclerView?.scrollToPosition(0)
        mRecyclerView?.adapter = mUsageListAdapter
        mOpenUsageSettingButton = rootView.findViewById<View>(R.id.button_open_usage_setting) as Button
        mSpinner = rootView.findViewById<View>(R.id.spinner_time_span) as Spinner
        val spinnerAdapter = ArrayAdapter(context,R.layout.spinner_text,resources.getStringArray(R.array.action_list))
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown)
        mPeakView?.setOnClickListener {
            val fr = PeakTimeStatisticsFragment.newInstance(if (mSpinner != null) {
                mSpinner?.selectedItemPosition!!
            }else{
                UsageStatsManager.INTERVAL_DAILY
            })
            val fc = activity as FragmentChangeListener
            fc.replaceFragment(fr)
        }
        mSpinner?.adapter = spinnerAdapter
        mSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            internal var strings = resources.getStringArray(R.array.action_list)

            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val statsUsageInterval = StatsUsageInterval
                        .getValue(strings[position])
                if (statsUsageInterval != null) {
                    val usageStatsList = getUsageStatistics(statsUsageInterval.mInterval)
                    Collections.sort(usageStatsList, MostTimeUsedComparatorDesc())
                    val totalTimeAllApps = usageStatsList.sumBy {
                        stat -> (stat.totalTimeInForeground / 60.0 / 1000.0).toInt()
                    }.toFloat()
                    val totalTimeAllSocialApps = usageStatsList.filter {
                        service -> isSocialMedia(service.packageName)
                    }.sumBy {
                        stat -> (stat.totalTimeInForeground / 60.0 / 1000.0).toInt()
                    }
                    mUsageListAdapter?.totalTimeAllApps = totalTimeAllSocialApps.toDouble()
                    if(totalTimeAllSocialApps > 60){
                        val totalHoursUsed = (totalTimeAllSocialApps / 60)
                        val minutesLeft = totalTimeAllSocialApps - (totalHoursUsed * 60)
                        mTotalTimeView?.text = "${totalHoursUsed}h${minutesLeft}m"
                    }else{
                        mTotalTimeView?.text = "${totalTimeAllSocialApps}m"
                    }
                    mCircularCompletionView?.setCompletionPercentage(totalTimeAllSocialApps /
                            totalTimeAllApps * 100)
                    mCircularCompletionView?.setStrokeSize(2f)
                    mCircularCompletionView?.setTextSize(0f)
                    updateAppsList(usageStatsList)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * Returns the [.mRecyclerView] including the time span specified by the
     * intervalType argument.
     *
     * @param intervalType The time interval by which the stats are aggregated.
     * Corresponding to the value of [UsageStatsManager].
     * E.g. [UsageStatsManager.INTERVAL_DAILY], or [UsageStatsManager.INTERVAL_WEEKLY],
     *
     * @return A list of [android.app.usage.UsageStats].
     */
    fun getUsageStatistics(intervalType: Int): List<UsageStats> {
        // Get the app statistics since one year ago from the current time.
        val cal = Calendar.getInstance()

        when(intervalType){
            UsageStatsManager.INTERVAL_DAILY -> {
                cal.add(Calendar.DATE, -1)
                mCircularCompletionView?.visibility = View.VISIBLE
                mGraphView?.visibility = View.INVISIBLE
                mTotalTimeView?.visibility = View.VISIBLE
            }
            UsageStatsManager.INTERVAL_WEEKLY -> {
                val lastDay = Calendar.getInstance()
                    val usages = Array<Int>(7,{_ -> 0})
                    for (i in 1..7){
                        cal.add(Calendar.DATE,-1)
                        usages[i-1] = mUsageStatsManager!!.queryUsageStats(UsageStatsManager.INTERVAL_DAILY
                                , cal.timeInMillis, lastDay.timeInMillis).filter {
                            stats -> isSocialMedia(stats.packageName)
                        }.sumBy {
                            stats -> stats.totalTimeInForeground.toInt()
                        }
                        Log.e("Data Adding amount",usages[i-1].toString())
                        lastDay.add(Calendar.DATE,-1)
                    }
                makeCurve(usages)
                mGraphView?.visibility = View.VISIBLE
                mCircularCompletionView?.visibility = View.INVISIBLE
                mTotalTimeView?.visibility = View.INVISIBLE

            }
            UsageStatsManager.INTERVAL_MONTHLY -> {
                val lastWeek = Calendar.getInstance()
                val usages = Array<Int>(4,{_ -> 0})
                for (i in 1..4){
                    cal.add(Calendar.DATE,-7)
                    usages[i-1] = mUsageStatsManager!!.queryUsageStats(UsageStatsManager.INTERVAL_WEEKLY
                            , cal.timeInMillis, lastWeek.timeInMillis).filter {
                        stats -> isSocialMedia(stats.packageName)
                    }.sumBy {
                        stats -> stats.totalTimeInForeground.toInt()
                    }
                    Log.e("Data Adding amount",usages[i-1].toString())
                    lastWeek.add(Calendar.DATE,-7)
                }
                makeGraph(usages)
                mGraphView?.visibility = View.VISIBLE
                mCircularCompletionView?.visibility = View.INVISIBLE
                mTotalTimeView?.visibility = View.INVISIBLE
            }
            UsageStatsManager.INTERVAL_YEARLY -> {
                val lastMonth = Calendar.getInstance()
                val usages = Array<Int>(12,{_ -> 0})
                for (i in 1..12){
                    cal.add(Calendar.MONTH,-1)
                    usages[i-1] = mUsageStatsManager!!.queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY
                            , cal.timeInMillis, lastMonth.timeInMillis).filter {
                        stats -> isSocialMedia(stats.packageName)
                    }.sumBy {
                        stats -> stats.totalTimeInForeground.toInt()
                    }
                    Log.e("Data Adding amount",usages[i-1].toString())
                    lastMonth.add(Calendar.MONTH,-1)
                }
                makeBarChart(usages)
                mGraphView?.visibility = View.VISIBLE
                mCircularCompletionView?.visibility = View.INVISIBLE
                mTotalTimeView?.visibility = View.INVISIBLE
            }
        }

        val queryUsageStats = mUsageStatsManager!!
                .queryUsageStats(intervalType, cal.timeInMillis,
                        System.currentTimeMillis())

        if(intervalType == UsageStatsManager.INTERVAL_WEEKLY ||
                intervalType == UsageStatsManager.INTERVAL_MONTHLY ||
                intervalType == UsageStatsManager.INTERVAL_YEARLY){
            mPickUpHeader?.text = getString(R.string.pick_header_total)
            mPickUpView?.text = convertToHourMinutes(queryUsageStats.filter {
                stats -> isSocialMedia(stats.packageName)
            }.sumBy {
                stats -> stats.totalTimeInForeground.toInt()
            })
        }else{
            mPickUpHeader?.text = getString(R.string.pick_header)
            mPickUpView?.text = phonePickUp.phonePickedUP.toString()
        }

        if (queryUsageStats.size == 0) {
            Log.i(TAG, "The user may not allow the access to apps usage. ")
            Toast.makeText(activity,
                    getString(R.string.explanation_access_to_appusage_is_not_enabled),
                    Toast.LENGTH_LONG).show()
            mOpenUsageSettingButton!!.visibility = View.VISIBLE
            mOpenUsageSettingButton!!.setOnClickListener { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
        }

        return queryUsageStats
    }

    private fun convertToHourMinutes(sum: Int): CharSequence? {
        var hour_string = ""
        val minutes = sum / 1000 / 60
        val hours = minutes / 60
        if(hours >= 1){
            hour_string += "${hours}h"
        }
        hour_string += "${minutes - hours * 60}m"
        return hour_string
    }

    private fun makeBarChart(usages: Array<Int>){
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
        mGraphView?.gridLabelRenderer?.gridStyle = GridLabelRenderer.GridStyle.HORIZONTAL
        val staticLabelsFormatter = StaticLabelsFormatter(mGraphView)
        staticLabelsFormatter.setHorizontalLabels(arrayOf("Jan","Jun","Dec"))
        mGraphView?.gridLabelRenderer?.labelFormatter = staticLabelsFormatter

    }

    private fun makeCurve(usages: Array<Int>) {
        mGraphView?.removeAllSeries()
        val data = Array<DataPoint>(7,init = {
            i -> DataPoint(i.toDouble(), Math.random() * 1000000)
        })
        val series = LineGraphCurve(data)
        mGraphView?.addSeries(series)
        mGraphView?.gridLabelRenderer?.gridStyle = GridLabelRenderer.GridStyle.NONE
        val staticLabelsFormatter = StaticLabelsFormatter(mGraphView)
        staticLabelsFormatter.setHorizontalLabels(arrayOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun"))
        staticLabelsFormatter.setVerticalLabels(arrayOf("",""))
        mGraphView?.gridLabelRenderer?.labelFormatter = staticLabelsFormatter
    }

    private fun makeGraph(usages: Array<Int>) {
        mGraphView?.removeAllSeries()
        val data = Array<DataPoint>(7,init = {
            i -> DataPoint(i.toDouble(), Math.random() * 1000000)
        })
        val series = LineGraphSeries(data)
        val customPaint = Paint()
        customPaint.strokeWidth = 10f
        customPaint.style = Paint.Style.STROKE
        val startColor = Color.parseColor("#0c486c")
        val endColor   = Color.parseColor("#cff09f")
        customPaint.shader = LinearGradient(0f,0f,0f,mGraphView?.height!!.toFloat(), startColor,
                endColor, Shader.TileMode.MIRROR)
        series.setCustomPaint(customPaint)
        mGraphView?.addSeries(series)
        mGraphView?.gridLabelRenderer?.gridStyle = GridLabelRenderer.GridStyle.NONE
        val staticLabelsFormatter = StaticLabelsFormatter(mGraphView)
        staticLabelsFormatter.setHorizontalLabels(arrayOf("Week1","Week 4"))
        staticLabelsFormatter.setVerticalLabels(arrayOf("",""))
        mGraphView?.gridLabelRenderer?.labelFormatter = staticLabelsFormatter
    }

    val SOCIAL_MEDIA_LIST = listOf<String>("FACEBOOK","REDDIT","WHATSAPP",
            "YOUTUBE","WECHAT","LINKEDIN","INSTAGRAM","SNAPCHAT")

    private fun isSocialMedia(packageName : String) : Boolean{
        SOCIAL_MEDIA_LIST.forEach {
            service ->
            if (packageName.toUpperCase().contains(service)){
            return true
        }
        }

        return false
    }

    /**
     * Updates the [.mRecyclerView] with the list of [UsageStats] passed as an argument.
     *
     * @param usageStatsList A list of [UsageStats] from which update the
     * [.mRecyclerView].
     */
    //VisibleForTesting
    internal fun updateAppsList(usageStatsList: List<UsageStats>) {
        val customUsageStatsList = ArrayList<CustomUsageStats>()
        for (i in usageStatsList) {
            val customUsageStats = CustomUsageStats()
            if (isSocialMedia(i.packageName)){

                customUsageStats.usageStats = i
                try {
                    val appIcon = activity.packageManager
                            .getApplicationIcon(customUsageStats.usageStats!!.packageName)
                    customUsageStats.appIcon = appIcon
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.w(TAG, "App Icon is not found for ${customUsageStats.usageStats!!.packageName}")
                    customUsageStats.appIcon = activity
                            .getDrawable(R.drawable.ic_default_app_launcher)
                }

                customUsageStatsList.add(customUsageStats)
            }
        }
        mUsageListAdapter!!.setCustomUsageStatsList(customUsageStatsList)
        mUsageListAdapter!!.notifyDataSetChanged()
        mRecyclerView!!.scrollToPosition(0)
    }

    /**
     * The [Comparator] to sort a collection of [UsageStats] sorted by the timestamp
     * last time the app was used in the descendant order.
     */
    private class MostTimeUsedComparatorDesc : Comparator<UsageStats> {

        override fun compare(left: UsageStats, right: UsageStats): Int =
                java.lang.Long.compare(right.totalTimeInForeground, left.totalTimeInForeground)
    }

    /**
     * Enum represents the intervals for [android.app.usage.UsageStatsManager] so that
     * values for intervals can be found by a String representation.
     *
     */
    //VisibleForTesting
    internal enum class StatsUsageInterval  constructor(private val mStringRepresentation: String,
                                                        val mInterval: Int) {
        DAILY("Today", UsageStatsManager.INTERVAL_DAILY),
        WEEKLY("Weekly", UsageStatsManager.INTERVAL_WEEKLY),
        MONTHLY("Monthly", UsageStatsManager.INTERVAL_MONTHLY),
        YEARLY("Yearly", UsageStatsManager.INTERVAL_YEARLY);


        companion object {

            fun getValue(stringRepresentation: String): StatsUsageInterval? =
                    values().firstOrNull { it.mStringRepresentation == stringRepresentation }
        }
    }

    companion object {

        private val TAG = AppUsageStatisticsFragment::class.java.simpleName

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment [AppUsageStatisticsFragment].
         */
        fun newInstance(): AppUsageStatisticsFragment = AppUsageStatisticsFragment()
    }

}// Required empty public constructor
