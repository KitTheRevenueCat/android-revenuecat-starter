package com.kit.revenuecat.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.purchaseWith
import com.revenuecat.purchases.restorePurchasesWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class PaywallState {
    object Loading : PaywallState()
    data class Success(val offerings: Offerings) : PaywallState()
    data class Error(val message: String) : PaywallState()
}

sealed class PurchaseState {
    object Idle : PurchaseState()
    object InProgress : PurchaseState()
    data class PremiumActivated(val isActive: Boolean) : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}

class PaywallViewModel : ViewModel() {

    private val _paywallState = MutableStateFlow<PaywallState>(PaywallState.Loading)
    val paywallState: StateFlow<PaywallState> = _paywallState.asStateFlow()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    init {
        fetchOfferings()
    }

    fun fetchOfferings() {
        _paywallState.value = PaywallState.Loading
        Purchases.sharedInstance.getOfferingsWith(
            onError = { error ->
                _paywallState.value = PaywallState.Error(error.message)
            },
            onSuccess = { offerings ->
                _paywallState.value = PaywallState.Success(offerings)
            }
        )
    }

    fun purchasePackage(activity: Activity, pkg: Package) {
        _purchaseState.value = PurchaseState.InProgress
        val purchaseParams = PurchaseParams.Builder(activity, pkg).build()
        Purchases.sharedInstance.purchaseWith(
            purchaseParams = purchaseParams,
            onError = { error, userCancelled ->
                if (userCancelled) {
                    // User cancelled — reset to idle, no error shown
                    _purchaseState.value = PurchaseState.Idle
                } else {
                    _purchaseState.value = PurchaseState.Error(error.message)
                }
            },
            onSuccess = { storeTransaction, customerInfo ->
                val isPremiumActive = customerInfo.entitlements["premium"]?.isActive == true
                _purchaseState.value = PurchaseState.PremiumActivated(isPremiumActive)
            }
        )
    }

    fun restorePurchases() {
        _purchaseState.value = PurchaseState.InProgress
        Purchases.sharedInstance.restorePurchasesWith(
            onError = { error ->
                _purchaseState.value = PurchaseState.Error(error.message)
            },
            onSuccess = { customerInfo ->
                val isPremiumActive = customerInfo.entitlements["premium"]?.isActive == true
                _purchaseState.value = PurchaseState.PremiumActivated(isPremiumActive)
            }
        )
    }

    fun clearPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }
}
