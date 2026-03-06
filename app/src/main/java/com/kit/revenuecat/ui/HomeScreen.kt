package com.kit.revenuecat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.restorePurchasesWith

sealed class HomeState {
    object Loading : HomeState()
    data class Ready(val isPremium: Boolean) : HomeState()
    data class Error(val message: String) : HomeState()
}

@Composable
fun HomeScreen(
    onNavigateToPaywall: () -> Unit
) {
    var homeState by remember { mutableStateOf<HomeState>(HomeState.Loading) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Fetch customer info on load
    LaunchedEffect(Unit) {
        fetchCustomerInfo(
            onResult = { isPremium -> homeState = HomeState.Ready(isPremium) },
            onError = { message -> homeState = HomeState.Error(message) }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "RevenueCat Starter",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "A minimal RevenueCat integration demo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            when (val state = homeState) {
                is HomeState.Loading -> {
                    CircularProgressIndicator()
                }

                is HomeState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        homeState = HomeState.Loading
                        fetchCustomerInfo(
                            onResult = { isPremium -> homeState = HomeState.Ready(isPremium) },
                            onError = { message -> homeState = HomeState.Error(message) }
                        )
                    }) {
                        Text("Retry")
                    }
                }

                is HomeState.Ready -> {
                    // Premium badge or upgrade prompt
                    if (state.isPremium) {
                        PremiumBadge()
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "You have full access to all premium features.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Free Plan",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Upgrade to unlock all features",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onNavigateToPaywall,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Upgrade to Premium")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                homeState = HomeState.Loading
                                Purchases.sharedInstance.restorePurchasesWith(
                                    onError = { error ->
                                        homeState = HomeState.Error(error.message)
                                    },
                                    onSuccess = { customerInfo ->
                                        val isPremium =
                                            customerInfo.entitlements["premium"]?.isActive == true
                                        homeState = HomeState.Ready(isPremium)
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Restore Purchases")
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun PremiumBadge() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = "⭐ Premium",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * Helper to fetch customer info outside of a composable scope (called from LaunchedEffect).
 */
private fun fetchCustomerInfo(
    onResult: (isPremium: Boolean) -> Unit,
    onError: (message: String) -> Unit
) {
    Purchases.sharedInstance.getCustomerInfoWith(
        onError = { error -> onError(error.message) },
        onSuccess = { customerInfo ->
            val isPremium = customerInfo.entitlements["premium"]?.isActive == true
            onResult(isPremium)
        }
    )
}
