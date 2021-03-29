package com.absoft.storyViewer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import com.absoft.storyViewer.models.StoryDetails
import com.absoft.storyViewer.models.UserDetails
import java.util.*

object StoriesViewer {
    /** The key used to pass data source to [StoriesActivity].  */
    const val STORIES_DATA = "STORIES_DATA"
    const val ARRAY_STORIES = "ARRAY_STORIES"

    fun <T, E : StoryDetails> setStoriesArray(list: ArrayList<T>): ActivityBuilder<T, E> where  T : UserDetails<E>, T : Parcelable {
        return ActivityBuilder(list)
    }

    class ActivityBuilder<T, E : StoryDetails>(private val mSource: ArrayList<T>) where  T : UserDetails<E>, T : Parcelable?  {

        private fun getIntent(context: Context): Intent {
            return getIntent(context, StoriesActivity::class.java)
        }

        private fun getIntent(context: Context, cls: Class<*>?): Intent {
            val intent = Intent()
            intent.setClass(context, cls!!)
            val bundle = Bundle()
            bundle.putParcelableArrayList(STORIES_DATA, mSource)
            intent.putExtra(ARRAY_STORIES, bundle)
            return intent
        }

        fun start(activity: Activity) {
            activity.startActivity(getIntent(activity))
        }
    }
}