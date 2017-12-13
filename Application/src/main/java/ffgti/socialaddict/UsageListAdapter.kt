/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package ffgti.socialaddict

import android.content.pm.PackageManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.ArrayList

/**
 * Provide views to RecyclerView with the directory entries.
 */
class UsageListAdapter : RecyclerView.Adapter<UsageListAdapter.ViewHolder>() {

    private var mCustomUsageStatsList: List<CustomUsageStats> = ArrayList()
    private val mDateFormat = SimpleDateFormat()
    var mPackageManager : PackageManager? = null
    var totalTimeAllApps = 0.0

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val packageName: TextView = v.findViewById<View>(R.id.textview_package_name) as TextView
        val timeinSpan: TextView = v.findViewById<View>(R.id.textview_last_time_used) as TextView
        val appIcon: ImageView = v.findViewById<View>(R.id.app_icon) as ImageView
        val percentUsed : TextView = v.findViewById<View>(R.id.percentText) as TextView

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.usage_row, viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (mPackageManager != null)
        {
            val packageName =  mCustomUsageStatsList[position].usageStats!!.packageName
            val appName = mPackageManager!!.getApplicationLabel(
                    mPackageManager!!.getApplicationInfo(packageName, PackageManager.GET_META_DATA))
            viewHolder.packageName.text = appName
        }else{
            viewHolder.packageName.text = mCustomUsageStatsList[position].usageStats!!.packageName
        }

        val lastTimeUsed = mCustomUsageStatsList[position].usageStats!!.lastTimeUsed
        val totalTimeUsed = (mCustomUsageStatsList[position].usageStats!!.totalTimeInForeground /
                1000.0 / 60.0).toInt()
        if(totalTimeUsed > 60){
            val totalHoursUsed = (totalTimeUsed / 60)
            val minutesLeft = totalTimeUsed - (totalHoursUsed * 60)
            viewHolder.timeinSpan.text = "${totalHoursUsed.toString()}h${minutesLeft}m"
        }else{
            viewHolder.timeinSpan.text = "${totalTimeUsed}m"
        }
        viewHolder.appIcon.setImageDrawable(mCustomUsageStatsList[position].appIcon)
        viewHolder.percentUsed.text = (totalTimeUsed / totalTimeAllApps * 100).toInt().toString() + "%"
    }

    override fun getItemCount(): Int = mCustomUsageStatsList.size

    fun setCustomUsageStatsList(customUsageStats: List<CustomUsageStats>) {
        mCustomUsageStatsList = customUsageStats
    }
}