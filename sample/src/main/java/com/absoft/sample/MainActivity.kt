package com.absoft.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.absoft.storyViewer.StoriesViewer
import com.absoft.storyViewer.models.UserDetails

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.button).setOnClickListener {
            StoriesViewer.setStoriesArray(getArray()).start(this)
        }
    }

    private fun getArray(): ArrayList<User> {
        val arrayList = ArrayList<User>()
        repeat(10) {
        val storyUrls = ArrayList<Story>()

            storyUrls.add(
                Story(
                    "https://player.vimeo.com/external/422787651.sd.mp4?s=ec96f3190373937071ba56955b2f8481eaa10cce&profile_id=165&oauth2_token_id=57447761",
                    "0L"
                )
            )
            storyUrls.add(
                Story(
                    "https://images.pexels.com/photos/1433052/pexels-photo-1433052.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260",
                    "0L"
                )
            )
            storyUrls.add(
                Story(
                    "https://images.pexels.com/photos/1366630/pexels-photo-1366630.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260",
                    "0L"
                )
            )
            storyUrls.add(
                Story(
                    "https://images.pexels.com/photos/1067333/pexels-photo-1067333.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260",
                    "0L"
                )
            )
            arrayList.add(User("ADds", "https://randomuser.me/api/portraits/women/11.jpg", storyUrls))
        }

        return arrayList
    }
}