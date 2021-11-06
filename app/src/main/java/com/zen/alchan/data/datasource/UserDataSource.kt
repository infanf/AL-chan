package com.zen.alchan.data.datasource

import com.apollographql.apollo.api.Response
import com.zen.alchan.data.response.anilist.MediaListTypeOptions
import com.zen.alchan.data.response.anilist.NotificationOption
import io.reactivex.Observable
import io.reactivex.Single
import type.ScoreFormat
import type.UserStaffNameLanguage
import type.UserStatisticsSort
import type.UserTitleLanguage

interface UserDataSource {
    fun getViewerQuery(sort: List<UserStatisticsSort>): Observable<Response<ViewerQuery.Data>>
//    fun getFollowingAndFollowerCount(userId: Int): Observable<Response<FollowingAndFollowersCountQuery.Data>>
    fun updateAniListSettings(
        titleLanguage: UserTitleLanguage,
        staffNameLanguage: UserStaffNameLanguage,
        activityMergeTime: Int,
        displayAdultContent: Boolean,
        airingNotifications: Boolean
    ): Single<Response<UpdateUserMutation.Data>>
    fun updateListSettings(
        scoreFormat: ScoreFormat,
        rowOrder: String,
        animeListOptions: MediaListTypeOptions,
        mangaListOptions: MediaListTypeOptions
    ): Single<Response<UpdateUserMutation.Data>>
    fun updateNotificationsSettings(
        notificationOptions: List<NotificationOption>
    ): Single<Response<UpdateUserMutation.Data>>
}