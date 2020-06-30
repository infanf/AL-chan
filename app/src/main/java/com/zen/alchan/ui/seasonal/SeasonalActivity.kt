package com.zen.alchan.ui.seasonal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.util.toRange
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zen.alchan.R
import com.zen.alchan.data.response.SeasonalAnime
import com.zen.alchan.helper.Constant
import com.zen.alchan.helper.changeStatusBarColor
import com.zen.alchan.helper.enums.BrowsePage
import com.zen.alchan.helper.enums.ResponseStatus
import com.zen.alchan.helper.enums.SeasonalCategory
import com.zen.alchan.helper.utils.AndroidUtility
import com.zen.alchan.helper.utils.DialogUtility
import com.zen.alchan.helper.utils.Utility
import com.zen.alchan.ui.base.BaseActivity
import com.zen.alchan.ui.browse.BrowseActivity
import kotlinx.android.synthetic.main.activity_seasonal.*
import kotlinx.android.synthetic.main.layout_loading.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import type.MediaFormat
import type.MediaSort
import type.MediaStatus

class SeasonalActivity : BaseActivity() {

    private val viewModel by viewModel<SeasonalViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seasonal)

        changeStatusBarColor(AndroidUtility.getResValueFromRefAttr(this, R.attr.themeCardColor))
        setSupportActionBar(toolbarLayout)
        supportActionBar?.apply {
            title = getString(R.string.seasonal_chart)
            setDisplayHomeAsUpEnabled(true)
        }

        if (viewModel.selectedSort == null) {
            viewModel.selectedYear = Utility.getCurrentYear()
            viewModel.selectedSeason = Utility.getCurrentSeason()
            viewModel.selectedSort = MediaSort.POPULARITY_DESC
            viewModel.selectedOnList = null
            viewModel.selectedIsAdult = false
        }

        initLayout()
        setupObserver()
    }

    private fun setupObserver() {
        viewModel.seasonalAnimeTvResponse.observe(this, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> seasonalTvLoading.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    seasonalTvLoading.visibility = View.GONE

                    if (!viewModel.tvHasNextPage) {
                        return@Observer
                    }

                    viewModel.tvHasNextPage = it.data?.page?.pageInfo?.hasNextPage ?: false
                    viewModel.tvPage += 1
                    viewModel.tvIsInit = true
                    viewModel.tvList.addAll(ArrayList(viewModel.seasonalAnimeTvData.value))

                    if (viewModel.tvHasNextPage) {
                        viewModel.getSeasonalAnime(SeasonalCategory.TV)
                    } else {
                        seasonalTvRecyclerView.adapter = SeasonalRvAdapter(this, viewModel.tvList, handleAdapterAction())
                        seasonalNoTvText.visibility = if (viewModel.tvList.isNullOrEmpty()) View.VISIBLE else View.GONE
                    }
                }
                ResponseStatus.ERROR -> {
                    seasonalTvLoading.visibility = View.GONE
                    DialogUtility.showToast(this, it.message)
                    seasonalNoTvText.visibility = if (viewModel.tvList.isNullOrEmpty()) View.VISIBLE else View.GONE
                }
            }
        })

        viewModel.seasonalAnimeTvShortResponse.observe(this, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> seasonalTvShortLoading.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    seasonalTvShortLoading.visibility = View.GONE

                    if (!viewModel.tvShortHasNextPage) {
                        return@Observer
                    }

                    viewModel.tvShortHasNextPage = it.data?.page?.pageInfo?.hasNextPage ?: false
                    viewModel.tvShortPage += 1
                    viewModel.tvShortIsInit = true
                    viewModel.tvShortList.addAll(ArrayList(viewModel.seasonalAnimeTvShortData.value))

                    if (viewModel.tvShortHasNextPage) {
                        viewModel.getSeasonalAnime(SeasonalCategory.TV_SHORT)
                    } else {
                        seasonalTvShortRecyclerView.adapter = SeasonalRvAdapter(this, viewModel.tvShortList, handleAdapterAction())
                        seasonalNoTvShortText.visibility = if (viewModel.tvShortList.isNullOrEmpty()) View.VISIBLE else View.GONE
                    }
                }
                ResponseStatus.ERROR -> {
                    seasonalTvShortLoading.visibility = View.GONE
                    DialogUtility.showToast(this, it.message)
                    seasonalNoTvShortText.visibility = if (viewModel.tvShortList.isNullOrEmpty()) View.VISIBLE else View.GONE
                }
            }
        })

        viewModel.seasonalAnimeMovieResponse.observe(this, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> seasonalMovieLoading.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    seasonalMovieLoading.visibility = View.GONE

                    if (!viewModel.movieHasNextPage) {
                        return@Observer
                    }

                    viewModel.movieHasNextPage = it.data?.page?.pageInfo?.hasNextPage ?: false
                    viewModel.moviePage += 1
                    viewModel.movieIsInit = true
                    viewModel.movieList.addAll(ArrayList(viewModel.seasonalAnimeMovieData.value))

                    if (viewModel.movieHasNextPage) {
                        viewModel.getSeasonalAnime(SeasonalCategory.MOVIE)
                    } else {
                        seasonalMovieRecyclerView.adapter = SeasonalRvAdapter(this, viewModel.movieList, handleAdapterAction())
                        seasonalNoMovieText.visibility = if (viewModel.movieList.isNullOrEmpty()) View.VISIBLE else View.GONE
                    }
                }
                ResponseStatus.ERROR -> {
                    seasonalMovieLoading.visibility = View.GONE
                    DialogUtility.showToast(this, it.message)
                    seasonalNoMovieText.visibility = if (viewModel.movieList.isNullOrEmpty()) View.VISIBLE else View.GONE
                }
            }
        })

        viewModel.seasonalAnimeOthersResponse.observe(this, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> seasonalOthersLoading.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    seasonalOthersLoading.visibility = View.GONE

                    if (!viewModel.othersHasNextPage) {
                        return@Observer
                    }

                    viewModel.othersHasNextPage = it.data?.page?.pageInfo?.hasNextPage ?: false
                    viewModel.othersPage += 1
                    viewModel.othersIsInit = true
                    viewModel.othersList.addAll(ArrayList(viewModel.seasonalAnimeOthersData.value))

                    if (viewModel.othersHasNextPage) {
                        viewModel.getSeasonalAnime(SeasonalCategory.OTHERS)
                    } else {
                        seasonalOthersRecyclerView.adapter = SeasonalRvAdapter(this, viewModel.othersList, handleAdapterAction())
                        seasonalNoOthersText.visibility = if (viewModel.othersList.isNullOrEmpty()) View.VISIBLE else View.GONE
                    }
                }
                ResponseStatus.ERROR -> {
                    seasonalOthersLoading.visibility = View.GONE
                    DialogUtility.showToast(this, it.message)
                    seasonalNoOthersText.visibility = if (viewModel.othersList.isNullOrEmpty()) View.VISIBLE else View.GONE
                }
            }
        })

        viewModel.addAnimeToPlanningResponse.observe(this, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    if (it.data != null) {
                        viewModel.updateList(it.data)
                        when (it.data.saveMediaListEntry?.media?.format) {
                            MediaFormat.TV -> seasonalTvRecyclerView.adapter?.notifyDataSetChanged()
                            MediaFormat.TV_SHORT -> seasonalTvShortRecyclerView.adapter?.notifyDataSetChanged()
                            MediaFormat.MOVIE -> seasonalMovieRecyclerView.adapter?.notifyDataSetChanged()
                            MediaFormat.OVA, MediaFormat.ONA, MediaFormat.SPECIAL -> seasonalOthersRecyclerView.adapter?.notifyDataSetChanged()
                        }
                    }
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(this, it.message)
                }
            }
        })

        if (!viewModel.tvIsInit) {
            viewModel.getSeasonalAnime(SeasonalCategory.TV)
        } else {
            seasonalTvRecyclerView.adapter = SeasonalRvAdapter(this, viewModel.tvList, handleAdapterAction())
            seasonalNoTvText.visibility = if (viewModel.tvList.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        if (!viewModel.tvShortIsInit) {
            viewModel.getSeasonalAnime(SeasonalCategory.TV_SHORT)
        } else {
            seasonalTvShortRecyclerView.adapter = SeasonalRvAdapter(this, viewModel.tvShortList, handleAdapterAction())
            seasonalNoTvShortText.visibility = if (viewModel.tvShortList.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        if (!viewModel.movieIsInit) {
            viewModel.getSeasonalAnime(SeasonalCategory.MOVIE)
        } else {
            seasonalMovieRecyclerView.adapter = SeasonalRvAdapter(this, viewModel.movieList, handleAdapterAction())
            seasonalNoMovieText.visibility = if (viewModel.movieList.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        if (!viewModel.othersIsInit) {
            viewModel.getSeasonalAnime(SeasonalCategory.OTHERS)
        } else {
            seasonalOthersRecyclerView.adapter = SeasonalRvAdapter(this, viewModel.othersList, handleAdapterAction())
            seasonalNoOthersText.visibility = if (viewModel.othersList.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun initLayout() {
        seasonalRefreshLayout.setOnRefreshListener {
            seasonalRefreshLayout.isRefreshing = false
            viewModel.refresh()
        }

        seasonalYearText.text = viewModel.selectedYear?.toString()
        seasonalSeasonText.text = viewModel.selectedSeason?.name
        seasonalSortText.text = viewModel.mediaSortArray[viewModel.mediaSortList.indexOf(viewModel.selectedSort)]

        if (viewModel.showAdultContent) {
            seasonalShowAdultText.visibility = View.VISIBLE
            seasonalShowAdultCheckBox.visibility = View.VISIBLE
        } else {
            seasonalShowAdultText.visibility = View.GONE
            seasonalShowAdultCheckBox.visibility = View.GONE
        }

        seasonalYearText.setOnClickListener {
            val yearList = ArrayList((Utility.getCurrentYear() + 1 downTo Constant.FILTER_EARLIEST_YEAR).map { it.toString() })
            yearList.add(0, "TBA")
            MaterialAlertDialogBuilder(this)
                .setItems(yearList.toTypedArray()) { _, which ->
                    if (which == 0) {
                        viewModel.selectedStatus = MediaStatus.NOT_YET_RELEASED
                        seasonalYearText.text = yearList[which]
                    } else {
                        viewModel.selectedStatus = null
                        viewModel.selectedYear = yearList[which].toInt()
                        seasonalYearText.text = viewModel.selectedYear?.toString()
                    }

                    viewModel.resetHasNextPage()
                    viewModel.refresh()
                }
                .show()
        }

        seasonalSeasonText.setOnClickListener {
            val seasonArray = Constant.SEASON_LIST.map { it.name }.toTypedArray()
            MaterialAlertDialogBuilder(this)
                .setItems(seasonArray) { _, which ->
                    viewModel.selectedSeason = Constant.SEASON_LIST[which]
                    seasonalSeasonText.text = viewModel.selectedSeason?.name

                    if (viewModel.selectedStatus != null) {
                        return@setItems
                    }

                    viewModel.resetHasNextPage()
                    viewModel.refresh()
                }
                .show()
        }

        seasonalSortText.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setItems(viewModel.mediaSortArray) { _, which ->
                    viewModel.selectedSort = viewModel.mediaSortList[which]
                    seasonalSortText.text = viewModel.mediaSortArray[which]
                    viewModel.resetHasNextPage()
                    viewModel.refresh()
                }
                .show()
        }

        seasonalHideOnListCheckBox.setOnClickListener {
            if (seasonalHideOnListCheckBox.isChecked) {
                viewModel.selectedOnList = false
                seasonalShowOnListCheckBox.isChecked = false
            } else {
                viewModel.selectedOnList = null
            }
            viewModel.resetHasNextPage()
            viewModel.refresh()
        }

        seasonalHideOnListText.setOnClickListener {
            seasonalHideOnListCheckBox.performClick()
        }

        seasonalShowOnListCheckBox.setOnClickListener {
            if (seasonalShowOnListCheckBox.isChecked) {
                viewModel.selectedOnList = true
                seasonalHideOnListCheckBox.isChecked = false
            } else {
                viewModel.selectedOnList = null
            }
            viewModel.resetHasNextPage()
            viewModel.refresh()
        }

        seasonalShowOnListText.setOnClickListener {
            seasonalShowOnListCheckBox.performClick()
        }

        seasonalShowAdultCheckBox.setOnClickListener {
            viewModel.selectedIsAdult = seasonalShowAdultCheckBox.isChecked
            viewModel.resetHasNextPage()
            viewModel.refresh()
        }

        seasonalShowAdultText.setOnClickListener {
            seasonalShowAdultCheckBox.performClick()
        }
    }

    private fun handleAdapterAction() = object : SeasonalRvAdapter.SeasonalListener {
        override fun openDetail(seasonalAnime: SeasonalAnime) {
            val dialog = SeasonalDialog()
            val bundle = Bundle()
            bundle.putString(SeasonalDialog.SEASONAL_ANIME_DETAIL, viewModel.gson.toJson(seasonalAnime))
            dialog.arguments = bundle
            dialog.show(supportFragmentManager, null)
        }

        override fun openAnime(id: Int) {
            val intent = Intent(this@SeasonalActivity, BrowseActivity::class.java)
            intent.putExtra(BrowseActivity.TARGET_PAGE, BrowsePage.ANIME.name)
            intent.putExtra(BrowseActivity.LOAD_ID, id)
            startActivity(intent)
        }

        override fun addToPlanning(id: Int) {
            viewModel.addToPlanning(id)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}
