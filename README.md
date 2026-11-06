# Smart Dialer

A lightweight Android dialer app built with Kotlin (no server, everything on-device).

## Features

- T9 smart dialpad: type a number, matching contacts show up instantly (e.g. 726 -> RAM/SAM)
- Grouped call history with All / Missed / Incoming / Outgoing filters
- Contacts list with search
- Favorites (starred contacts) and speed dial
- Local spam blocking: blocklist stored on the device, calls rejected by `CallScreeningService`
- Caller hint on the dialpad (contact name or "Possible spam number")
- Dual SIM: choose which SIM to call from when the phone has two active SIMs
- WhatsApp / SMS shortcuts and quick block from any list (long press a row)
- Dark UI, works fully offline

## Install

Download the APK from the latest release (Releases section) and install it. Android 7.0 (API 24) or newer.

For automatic spam rejection, enable the app as the call screening app in Android settings
(Phone app -> Settings -> Caller ID & spam -> Call screening app).

## Build

The APK is built automatically by GitHub Actions on every push to `main`
(see `.github/workflows/build-apk.yml`). Locally:

```
gradle assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/app-debug.apk`.
