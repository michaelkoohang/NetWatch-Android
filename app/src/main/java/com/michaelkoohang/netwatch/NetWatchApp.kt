package com.michaelkoohang.netwatch

import android.app.Application
import android.content.Context
import io.realm.Realm
import io.realm.RealmConfiguration

class NetWatchApp : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: NetWatchApp? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Set up database.
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .name("netwatch.realm")
                .build()
        Realm.setDefaultConfiguration(realmConfig)
    }
}