package com.absoft.storyViewer

import android.app.Application
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache

class StoryViewerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (simpleCache == null) {
            simpleCache = SimpleCache(
                cacheDir,
                LeastRecentlyUsedCacheEvictor(90 * 1024 * 1024),
                ExoDatabaseProvider(this)
            )
        }
    }

    companion object {
        var simpleCache: SimpleCache? = null
    }
}