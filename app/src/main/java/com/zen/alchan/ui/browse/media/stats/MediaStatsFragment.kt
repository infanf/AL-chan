package com.zen.alchan.ui.browse.media.stats


import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*

import com.zen.alchan.R
import com.zen.alchan.helper.enums.ResponseStatus
import com.zen.alchan.helper.utils.AndroidUtility
import com.zen.alchan.helper.utils.DialogUtility
import com.zen.alchan.ui.base.BaseFragment
import com.zen.alchan.ui.browse.media.MediaFragment
import kotlinx.android.synthetic.main.fragment_media_stats.*
import kotlinx.android.synthetic.main.layout_loading.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 */
class MediaStatsFragment : BaseFragment() {

    private val viewModel by viewModel<MediaStatsViewModel>()

    private var mediaData: MediaStatsQuery.Media? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_media_stats, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.mediaId = arguments?.getInt(MediaFragment.MEDIA_ID)

        setupObserver()
    }

    private fun setupObserver() {
        viewModel.mediaStatsData.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    viewModel.mediaData = it.data?.media
                    mediaData = it.data?.media
                    initLayout()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        if (viewModel.mediaData == null) {
            viewModel.getMediaStats()
        } else {
            mediaData = viewModel.mediaData
            initLayout()
        }
    }

    private fun initLayout() {
        handlePerformance()
        handleRankings()
        handleStatusDistribution()
        handleScoreDistribution()
    }

    private fun handlePerformance() {
        mediaAvgScoreText.text = "${mediaData?.averageScore?.toString() ?: "0"}%"
        mediaMeanScoreText.text = "${mediaData?.meanScore?.toString() ?: "0"}%"
        mediaPopularityText.text = mediaData?.popularity?.toString() ?: "0"
        mediaFavoritesText.text = mediaData?.favourites?.toString() ?: "0"
    }

    private fun handleRankings() {
        if (mediaData?.rankings?.isNullOrEmpty() == true) {
            mediaStatsRankingLayout.visibility = View.GONE
            return
        }

        mediaStatsRankingLayout.visibility = View.VISIBLE
        mediaStatsRankingRecyclerView.adapter = MediaStatsRankingRvAdapter(mediaData?.rankings!!)
    }

    private fun handleStatusDistribution() {
        if (mediaData?.stats?.statusDistribution?.isNullOrEmpty() == true) {
            mediaStatsStatusLayout.visibility = View.GONE
            return
        }

        mediaStatsStatusLayout.visibility = View.VISIBLE

        val statusDistributionList = ArrayList<StatusDistributionItem>()

        val colorList = arrayListOf(
            Color.parseColor("#f89963"),
            Color.parseColor("#05a9ff"),
            Color.parseColor("#69d83a"),
            Color.parseColor("#9256f3"),
            Color.parseColor("#f87aa5")
        )

        val pieEntries = ArrayList<PieEntry>()
        mediaData?.stats?.statusDistribution?.forEach {
            val pieEntry = PieEntry(it?.amount!!.toFloat(), it.status?.toString())
            pieEntries.add(pieEntry)
            statusDistributionList.add(StatusDistributionItem(it.status?.name!!, it.amount, colorList[statusDistributionList.size]))
        }

        val pieDataSet = PieDataSet(pieEntries, "Score Distribution")
        pieDataSet.colors = colorList

        val pieData = PieData(pieDataSet)
        pieData.setDrawValues(false)

        mediaStatsStatusPieChart.setHoleColor(ContextCompat.getColor(activity!!, android.R.color.transparent))
        mediaStatsStatusPieChart.setDrawEntryLabels(false)
        mediaStatsStatusPieChart.setTouchEnabled(false)
        mediaStatsStatusPieChart.description.isEnabled = false
        mediaStatsStatusPieChart.legend.isEnabled = false
        mediaStatsStatusPieChart.data = pieData
        mediaStatsStatusPieChart.invalidate()

        mediaStatsStatusRecyclerView.adapter = MediaStatsStatusRvAdapter(activity!!, statusDistributionList)
    }

    private fun handleScoreDistribution() {
        if (mediaData?.stats?.scoreDistribution?.isNullOrEmpty() == true) {
            mediaStatsScoreLayout.visibility = View.GONE
            return
        }

        mediaStatsScoreLayout.visibility = View.VISIBLE

        val colorList = arrayListOf(
            Color.parseColor("#d2492d"),
            Color.parseColor("#d2642c"),
            Color.parseColor("#d2802e"),
            Color.parseColor("#d29d2f"),
            Color.parseColor("#d2b72e"),
            Color.parseColor("#d3d22e"),
            Color.parseColor("#b8d22c"),
            Color.parseColor("#9cd42e"),
            Color.parseColor("#81d12d"),
            Color.parseColor("#63d42e")
        )

        val barEntries = ArrayList<BarEntry>()
        mediaData?.stats?.scoreDistribution?.forEach {
            val barEntry = BarEntry(it?.score?.toFloat()!!, it.amount?.toFloat()!!)
            barEntries.add(barEntry)
        }

        val barDataSet = BarDataSet(barEntries, "Score Distribution")
        barDataSet.colors = colorList

        val barData = BarData(barDataSet)
        barData.setValueTextColor(AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeContentColor))
        barData.barWidth = 3F

        mediaStatsScoreBarChart.axisLeft.setDrawGridLines(false)
        mediaStatsScoreBarChart.axisLeft.setDrawAxisLine(false)
        mediaStatsScoreBarChart.axisLeft.setDrawLabels(false)

        mediaStatsScoreBarChart.axisRight.setDrawGridLines(false)
        mediaStatsScoreBarChart.axisRight.setDrawAxisLine(false)
        mediaStatsScoreBarChart.axisRight.setDrawLabels(false)

        mediaStatsScoreBarChart.xAxis.setDrawGridLines(false)
        mediaStatsScoreBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        mediaStatsScoreBarChart.xAxis.setLabelCount(barEntries.size, true)
        mediaStatsScoreBarChart.xAxis.textColor = AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeContentColor)

        mediaStatsScoreBarChart.setTouchEnabled(false)
        mediaStatsScoreBarChart.description.isEnabled = false
        mediaStatsScoreBarChart.legend.isEnabled = false
        mediaStatsScoreBarChart.data = barData
        mediaStatsScoreBarChart.invalidate()
    }

    class StatusDistributionItem(val status: String, val amount: Int, val color: Int)
}
