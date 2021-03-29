package com.absoft.storyViewer.models

abstract class UserDetails<T>  where  T : StoryDetails {
    abstract fun getUserProfilePic(): String?
    abstract fun getUserName(): String?
    abstract fun getStories(): ArrayList<T>
}