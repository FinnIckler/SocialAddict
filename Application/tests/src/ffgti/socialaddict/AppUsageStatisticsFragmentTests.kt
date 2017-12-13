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

import ffgti.socialaddict.AppUsageStatisticsFragment.StatsUsageInterval

import android.app.usage.UsageStatsManager
import android.test.ActivityInstrumentationTestCase2
import android.test.UiThreadTest
import junit.framework.Assert

/**
 * Tests for [com.example.android.socialaddict.AppUsageStatisticsFragment].
 */
class AppUsageStatisticsFragmentTests : ActivityInstrumentationTestCase2<AppUsageStatisticsActivity>(AppUsageStatisticsActivity::class.java) {

    private var mTestActivity: AppUsageStatisticsActivity? = null

    private var mTestFragment: AppUsageStatisticsFragment? = null

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()

        // Starts the activity under test using the default Intent with:
        // action = {@link Intent#ACTION_MAIN}
        // flags = {@link Intent#FLAG_ACTIVITY_NEW_TASK}
        // All other fields are null or empty.
        mTestActivity = activity
        mTestFragment = mTestActivity!!.supportFragmentManager.fragments[0] as AppUsageStatisticsFragment
    }

    /**
     * Test if the test fixture has been set up correctly.
     */
    fun testPreconditions() {
        mTestFragment!!.mUsageStatsManager
        mTestFragment!!.mUsageListAdapter
        mTestFragment!!.mRecyclerView
        mTestFragment!!.mLayoutManager
        mTestFragment!!.mOpenUsageSettingButton
        mTestFragment!!.mSpinner
    }

    fun testStatsUsageInterval_getValue() {
        Assert.assertTrue(StatsUsageInterval.DAILY === StatsUsageInterval.getValue("Today"))
        Assert.assertTrue(StatsUsageInterval.WEEKLY === StatsUsageInterval.getValue("Weekly"))
        Assert.assertTrue(StatsUsageInterval.MONTHLY === StatsUsageInterval.getValue("Monthly"))
        Assert.assertTrue(StatsUsageInterval.YEARLY === StatsUsageInterval.getValue("Yearly"))
        Assert.assertNull(StatsUsageInterval.getValue("NonExistent"))
    }

    fun testGetUsageStatistics() {
        val usageStatsList = mTestFragment!!
                .getUsageStatistics(UsageStatsManager.INTERVAL_DAILY)

        // Whether the usageStatsList has any UsageStats depends on if the app is granted
        // the access to App usage statistics.
        // Only check non null here.
        Assert.assertNotNull(usageStatsList)
    }

    @UiThreadTest
    fun testUpdateAppsList() {
        val usageStatsList = mTestFragment!!
                .getUsageStatistics(UsageStatsManager.INTERVAL_DAILY)
        mTestFragment!!.updateAppsList(usageStatsList)

        // The result depends on if the app is granted the access to App usage statistics.
        if (usageStatsList.isEmpty()) {
            Assert.assertTrue(mTestFragment!!.mUsageListAdapter!!.itemCount == 0)
        } else {
            Assert.assertTrue(mTestFragment!!.mUsageListAdapter!!.itemCount > 0)
        }
    }
}
