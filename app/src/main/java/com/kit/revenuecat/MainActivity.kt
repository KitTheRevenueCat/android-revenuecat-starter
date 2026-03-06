package com.kit.revenuecat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kit.revenuecat.ui.HomeScreen
import com.kit.revenuecat.ui.PaywallScreen
import com.kit.revenuecat.ui.theme.RevenueCatStarterTheme

object Routes {
    const val HOME = "home"
    const val PAYWALL = "paywall"
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RevenueCatStarterTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.HOME,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Routes.HOME) {
                            HomeScreen(
                                onNavigateToPaywall = {
                                    navController.navigate(Routes.PAYWALL)
                                }
                            )
                        }
                        composable(Routes.PAYWALL) {
                            PaywallScreen(
                                onDismiss = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
