package com.absoft.storyViewer.models

abstract class StoryDetails {
    abstract fun getStoryLink(): String?
    abstract fun getStoryDateInMillis(): Long
    abstract fun isVideo(): Boolean
}