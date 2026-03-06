package com.kit.revenuecat

import android.app.Application
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize RevenueCat with the API key from BuildConfig (injected from local.properties)
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(
            PurchasesConfiguration.Builder(
                context = this,
                apiKey = BuildConfig.REVENUECAT_API_KEY
            ).build()
        )
    }
}
