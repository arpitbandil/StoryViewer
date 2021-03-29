package com.absoft.sample

import android.os.Parcelable
import com.absoft.storyViewer.models.UserDetails

import kotlinx.android.parcel.Parcelize

@Parcelize
class User (private val username : String?, private val profile : String?, private val story : ArrayList<Story>) : UserDetails<Story>(), Parcelable {
    override fun getStories(): ArrayList<Story> {
        return story
    }

    override fun getUserName(): String? {
        return username
    }

    override fun getUserProfilePic(): String? {
        return profile
    }
}