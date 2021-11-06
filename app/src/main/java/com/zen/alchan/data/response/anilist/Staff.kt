package com.zen.alchan.data.response.anilist

data class Staff(
    val id: Int = 0,
    val name: StaffName = StaffName(),
    val language: String = "",
    val image: StaffImage = StaffImage(),
    val description: String = "",
    val siteUrl: String = ""
)