package cit.edu.wildevents.app

import android.app.Application
import cit.edu.wildevents.data.User

class MyApplication : Application() {

    var currentUser: User? = null

    companion object {
        lateinit var instance: MyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
