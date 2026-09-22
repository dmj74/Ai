package com.titanali.app

import android.app.Application
import com.titanali.app.ai.AiProvider
import com.titanali.app.ai.ProviderRegistry
import com.titanali.app.data.db.AppDatabase
import com.titanali.app.data.repo.SettingsRepo
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class TitanaliApp : Application() {

    val http: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    lateinit var db: AppDatabase
        private set

    lateinit var settings: SettingsRepo
        private set

    val providers: Map<String, AiProvider> by lazy {
        ProviderRegistry.create(http) { settings.settings.value.ollamaHost }
    }

    fun provider(id: String): AiProvider = providers[id] ?: providers.values.first()

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.get(this)
        settings = SettingsRepo(this)
    }
}
