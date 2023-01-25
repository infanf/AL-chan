package com.zen.alchan.data.repository

import android.net.Uri
import com.zen.alchan.data.converter.convert
import com.zen.alchan.data.datasource.UserDataSource
import com.zen.alchan.data.entity.AppSetting
import com.zen.alchan.data.entity.MediaFilter
import com.zen.alchan.data.manager.UserManager
import com.zen.alchan.helper.enums.AppTheme
import com.zen.alchan.helper.enums.MediaType
import com.zen.alchan.helper.enums.Source
import com.zen.alchan.data.entity.ListStyle
import com.zen.alchan.data.response.NotificationData
import com.zen.alchan.data.response.anilist.*
import com.zen.alchan.helper.enums.Favorite
import com.zen.alchan.helper.extensions.moreThanADay
import com.zen.alchan.helper.pojo.NullableItem
import com.zen.alchan.helper.pojo.SaveItem
import com.zen.alchan.helper.utils.NotInStorageException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import type.*

class DefaultUserRepository(
    private val userDataSource: UserDataSource,
    private val userManager: UserManager
) : UserRepository {

    private val _refreshFavoriteTrigger = PublishSubject.create<User>()
    override val refreshFavoriteTrigger: Observable<User>
        get() = _refreshFavoriteTrigger

    private val _unreadNotificationCount = BehaviorSubject.createDefault(0)
    override val unreadNotificationCount: Observable<Int>
        get() = _unreadNotificationCount

    override fun getIsLoggedInAsGuest(): Observable<Boolean> {
        return Observable.just(userManager.isLoggedInAsGuest)
    }

    override fun getIsAuthenticated(): Observable<Boolean> {
        return Observable.just(userManager.isAuthenticated)
    }

    override fun getViewer(source: Source?, sort: List<UserStatisticsSort>): Observable<User> {
        return when (source) {
            Source.CACHE -> getViewerFromCache()
            Source.NETWORK -> getViewerFromNetwork(sort)
            else -> {
                val savedItem = userManager.viewerData
                if (savedItem == null || savedItem.saveTime.moreThanADay()) {
                    getViewerFromNetwork(sort)
                } else {
                    Observable.just(savedItem.data)
                }
            }
        }
    }

    private fun getViewerFromCache(): Observable<User> {
        val savedItem = userManager.viewerData?.data
        return if (savedItem != null) Observable.just(savedItem) else Observable.error(NotInStorageException())
    }

    private fun getViewerFromNetwork(sort: List<UserStatisticsSort>): Observable<User> {
        return userDataSource.getViewerQuery(sort).map {
            val newViewer = it.data?.convert()
            if (newViewer != null) {
                userManager.viewerData = SaveItem(newViewer)
                _unreadNotificationCount.onNext(newViewer.unreadNotificationCount)
            }
            newViewer
        }
    }

    override fun loginAsGuest() {
        userManager.isLoggedInAsGuest = true
    }

    override fun logoutAsGuest() {
        userManager.isLoggedInAsGuest = false
    }

    override fun logout() {
        userManager.bearerToken = null
        userManager.viewerData = null
        userManager.followingCount = null
        userManager.followersCount = null
    }

    override fun saveBearerToken(newBearerToken: String?) {
        userManager.bearerToken = newBearerToken
    }

    override fun getFollowingAndFollowersCount(userId: Int, source: Source?): Observable<Pair<Int, Int>> {
        when (source) {
            Source.CACHE -> {
                return Observable.create { emitter ->
                    val savedFollowingCount = userManager.followingCount ?: 0
                    val savedFollowersCount = userManager.followersCount ?: 0
                    emitter.onNext(savedFollowingCount to savedFollowersCount)
                    emitter.onComplete()
                }
            }
            else -> {
                return userDataSource.getFollowingAndFollowersCount(userId).map {
                    val newFollowingCount = it.data?.following?.pageInfo?.total ?: 0
                    val newFollowersCount = it.data?.followers?.pageInfo?.total ?: 0
                    userManager.followingCount = newFollowingCount
                    userManager.followersCount = newFollowersCount
                    newFollowingCount to newFollowersCount
                }.onErrorReturn {
                    val savedFollowingCount = userManager.followingCount ?: 0
                    val savedFollowersCount = userManager.followersCount ?: 0
                    savedFollowingCount to savedFollowersCount
                }
            }
        }
    }

    override fun getFollowing(userId: Int, page: Int): Observable<Pair<PageInfo, List<User>>> {
        return userDataSource.getFollowing(userId, page).map {
            it.data?.convert()
        }
    }

    override fun getFollowers(userId: Int, page: Int): Observable<Pair<PageInfo, List<User>>> {
        return userDataSource.getFollowers(userId, page).map {
            it.data?.convert()
        }
    }

    override fun toggleFollow(userId: Int): Observable<Boolean> {
        return userDataSource.toggleFollow(userId).toObservable().map {
            it.data?.toggleFollow?.isFollowing
        }
    }

    override fun getUserStatistics(
        userId: Int,
        sort: UserStatisticsSort
    ): Observable<UserStatisticTypes> {
        return userDataSource.getUserStatistics(userId, listOf(sort)).map {
            it.data?.convert()?.statistics
        }
    }

    override fun getFavorites(userId: Int, page: Int): Observable<Favourites> {
        return userDataSource.getFavorites(userId, page).map {
            it.data?.convert()
        }
    }

    override fun updateFavoriteOrder(ids: List<Int>, favorite: Favorite): Observable<Favourites> {
        return userDataSource.updateFavoriteOrder(ids, favorite).toObservable()
            .map {
                val newFavorites = it.data?.convert()
                if (newFavorites != null) {
                    val user = userManager.viewerData?.data
                    user?.let {
                        user.favourites = newFavorites
                        userManager.viewerData = SaveItem(user)
                        _refreshFavoriteTrigger.onNext(user)
                    }
                }
                newFavorites
            }
    }

    override fun toggleFavorite(
        animeId: Int?,
        mangaId: Int?,
        characterId: Int?,
        staffId: Int?,
        studioId: Int?
    ): Completable {
        return userDataSource.toggleFavorite(animeId, mangaId, characterId, staffId, studioId)
            .flatMapCompletable {
                getViewer(Source.NETWORK)
                    .flatMapCompletable {
                        _refreshFavoriteTrigger.onNext(it)
                        Completable.complete()
                    }
            }

    }

    override fun getAppSetting(): Observable<AppSetting> {
        return Observable.just(userManager.appSetting)
    }

    override fun setAppSetting(newAppSetting: AppSetting?): Observable<Unit> {
        return Observable.create {
            try {
                userManager.appSetting = newAppSetting ?: AppSetting()
                it.onNext(Unit)
                it.onComplete()
            } catch (e: Exception) {
                it.onError(e)
            }
        }
    }

    override fun getAppTheme(): AppTheme {
        return userManager.appSetting.appTheme
    }

    override fun updateAniListSettings(
        titleLanguage: UserTitleLanguage,
        staffNameLanguage: UserStaffNameLanguage,
        activityMergeTime: Int,
        displayAdultContent: Boolean,
        airingNotifications: Boolean
    ): Observable<User> {
        return userDataSource.updateAniListSettings(
            titleLanguage,
            staffNameLanguage,
            activityMergeTime,
            displayAdultContent,
            airingNotifications
        )
            .toObservable()
            .map {
                val newViewer = it.data?.convert()
                if (newViewer != null) {
                    userManager.viewerData = SaveItem(newViewer)
                }
                newViewer
            }
    }

    override fun updateListSettings(
        scoreFormat: ScoreFormat,
        rowOrder: String,
        animeListOptions: MediaListTypeOptions,
        mangaListOptions: MediaListTypeOptions,
        disabledListActivity: List<ListActivityOption>
    ): Observable<User> {
        return userDataSource.updateListSettings(
            scoreFormat, rowOrder, animeListOptions, mangaListOptions, disabledListActivity
        )
            .toObservable()
            .map {
                val newViewer = it.data?.convert()
                if (newViewer != null) {
                    userManager.viewerData = SaveItem(newViewer)
                }
                newViewer
            }
    }

    override fun updateNotificationsSettings(notificationOptions: List<NotificationOption>): Observable<User> {
        return userDataSource.updateNotificationsSettings(notificationOptions)
            .toObservable()
            .map {
                val newViewer = it.data?.convert()
                if (newViewer != null) {
                    userManager.viewerData = SaveItem(newViewer)
                }
                newViewer
            }
    }

    override fun getNotifications(
        page: Int,
        typeIn: List<NotificationType>?,
        resetNotificationCount: Boolean
    ): Observable<NotificationData> {
        return userDataSource.getNotifications(page, typeIn, resetNotificationCount).map {
            it.data?.convert()
        }
    }

    override fun getLatestUnreadNotificationCount(): Observable<Int> {
        return userDataSource.getUnreadNotificationCount().map {
            it.data?.viewer?.unreadNotificationCount ?: 0
        }
    }

    override fun clearUnreadNotificationCount() {
        _unreadNotificationCount.onNext(0)
    }

    override fun getLastNotificationId(): Observable<Int> {
        return Observable.just(userManager.lastNotificationId ?: 0)
    }

    override fun setLastNotificationId(lastNotificationId: Int) {
        userManager.lastNotificationId = lastNotificationId
    }
}