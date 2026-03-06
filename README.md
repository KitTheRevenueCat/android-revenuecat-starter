# android-revenuecat-starter

A minimal but complete Android sample app demonstrating [RevenueCat](https://www.revenuecat.com/) SDK integration using Jetpack Compose.

## What's inside

- **RevenueCat SDK 9.1.0** — configured and initialized via `MyApplication`
- **Paywall screen** — fetches your current offering, lists available packages, handles purchases and restores
- **Home screen** — checks entitlement status on load, shows a Premium badge or an Upgrade CTA
- **ViewModel + StateFlow** — clean architecture without Hilt complexity
- **BuildConfig injection** — API key loaded from `local.properties`, never hardcoded

## Tech stack

| Layer | Library |
|-------|---------|
| UI | Jetpack Compose (BOM 2024.12.01) |
| Navigation | Navigation Compose 2.8.5 |
| State | ViewModel + StateFlow |
| Purchases | RevenueCat `purchases:9.1.0` |
| Min SDK | 24 |
| Target/Compile SDK | 35 |

---

## Quick start

### 1. Clone the repo

```bash
git clone https://github.com/your-org/android-revenuecat-starter.git
cd android-revenuecat-starter
```

### 2. Add your RevenueCat API key

Copy the example properties file and fill in your key:

```bash
cp local.properties.example local.properties
```

Open `local.properties` and replace the placeholder:

```
REVENUECAT_API_KEY=goog_your_actual_key_here
```

> Get your key from the [RevenueCat dashboard](https://app.revenuecat.com) → **Project Settings → API Keys**.  
> Use the **Google Play public SDK key** (starts with `goog_`).

### 3. Configure your RevenueCat project

In the RevenueCat dashboard:

1. Create an **Entitlement** named `premium`
2. Create a **Product** in Google Play and attach it to the entitlement
3. Create an **Offering** with at least one package pointing to that product

### 4. Run the app

Open the project in Android Studio (Hedgehog or newer) and run on a device or emulator (API 24+).

```bash
./gradlew assembleDebug
```

---

## Project structure

```
app/src/main/java/com/kit/revenuecat/
├── MyApplication.kt          # RevenueCat SDK initialization
├── MainActivity.kt           # NavHost + routing
└── ui/
    ├── HomeScreen.kt         # Entitlement check, upgrade CTA
    ├── PaywallScreen.kt      # Offering display + purchase flow
    ├── PaywallViewModel.kt   # State management
    └── theme/
        └── Theme.kt          # Material 3 theming
```

---

## How it works

### Initialization (`MyApplication.kt`)
```kotlin
Purchases.logLevel = LogLevel.DEBUG
Purchases.configure(
    PurchasesConfiguration.Builder(context, BuildConfig.REVENUECAT_API_KEY).build()
)
```

### Fetching offerings (`PaywallViewModel.kt`)
```kotlin
Purchases.sharedInstance.getOfferingsWith(
    onError = { error -> /* handle */ },
    onSuccess = { offerings -> /* show packages */ }
)
```

### Making a purchase (`PaywallViewModel.kt`)
```kotlin
val params = PurchaseParams.Builder(activity, pkg).build()
Purchases.sharedInstance.purchaseWith(
    purchaseParams = params,
    onError = { error, userCancelled -> /* handle or ignore cancel */ },
    onSuccess = { _, customerInfo ->
        val isActive = customerInfo.entitlements["premium"]?.isActive == true
    }
)
```

### Checking entitlement (`HomeScreen.kt`)
```kotlin
Purchases.sharedInstance.getCustomerInfoWith(
    onError = { error -> /* handle */ },
    onSuccess = { customerInfo ->
        val isPremium = customerInfo.entitlements["premium"]?.isActive == true
    }
)
```

---

## License

MIT — use freely, ship fast.
