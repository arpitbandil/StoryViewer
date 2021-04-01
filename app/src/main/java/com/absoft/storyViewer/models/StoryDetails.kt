package com.absoft.storyViewer.models

abstract class StoryDetails {
    abstract fun getStoryLink(): String?
    abstract fun getStoryDate(): String
    abstract fun isVideo(): Boolean
}