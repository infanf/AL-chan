package com.zen.alchan.helper.pojo

import com.zen.alchan.data.entity.AppSetting
import com.zen.alchan.type.MediaSort

data class StaffMediaListAdapterComponent(
    val appSetting: AppSetting = AppSetting(),
    val mediaSort: MediaSort = MediaSort.POPULARITY_DESC
)