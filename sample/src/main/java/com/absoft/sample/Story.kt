package com.absoft.sample

import android.os.Parcelable
import com.absoft.storyViewer.models.StoryDetails
import com.absoft.storyViewer.models.UserDetails
import kotlinx.android.parcel.Parcelize

@Parcelize
class Story(private val link : String?, private val date : Long) : StoryDetails(), Parcelable {

    override fun getStoryLink(): String? {
        return link
    }

    override fun getStoryDateInMillis(): Long {
        return date
    }

    override fun isVideo(): Boolean {
        return link?.contains(".mp4") ?: false
    }
}