package com.zen.alchan.data.manager

import com.zen.alchan.data.entitiy.AppSetting
import com.zen.alchan.data.entitiy.MediaFilter
import com.zen.alchan.data.response.ProfileData
import com.zen.alchan.data.response.anilist.User
import com.zen.alchan.data.entitiy.ListStyle
import com.zen.alchan.helper.pojo.SaveItem

interface UserManager {
    var bearerToken: String?
    val isAuthenticated: Boolean
    var isLoggedInAsGuest: Boolean

    var animeListStyle: ListStyle
    var mangaListStyle: ListStyle
    var animeFilter: MediaFilter
    var mangaFilter: MediaFilter
    var appSetting: AppSetting

    var viewerData: SaveItem<User>?
    var profileData: SaveItem<ProfileData>?
}