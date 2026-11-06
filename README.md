# Contacts (OnePlus style dialer)

A lightweight Android contacts + dialer app with a clean white UI and red accent, inspired by the
OnePlus Contacts app. Everything runs on-device, no server needed.

## UI

- White theme with red (#EB0029) accent, large "Contacts" header
- Top tabs: Contacts / Favorites / Recents with a red underline indicator
- Alphabet section headers in the contact list plus a tap-able A-Z index on the right edge
- Rounded search field, circular initial avatars
- Red floating action button opens a full dialpad with T9 search

## Features

- T9 smart dialpad: type a number, matching contacts show up instantly (726 -> RAM/SAM)
- Call history with All / Missed / Incoming / Outgoing filters, calls from one number grouped
- Favorites (starred contacts)
- Local spam blocking: on-device blocklist, calls rejected by `CallScreeningService`
- Caller hint on the dialpad (contact name or "Possible spam number")
- Dual SIM: pick which SIM to call from when two SIMs are active
- WhatsApp / SMS shortcuts and quick block from any row (long press)
- Works fully offline

## Install

Grab the APK from the latest release and install it. Android 7.0 (API 24) or newer.

For automatic spam rejection, set the app as the call screening app in Android settings
(Phone app -> Settings -> Caller ID & spam -> Call screening app).

## Build

The APK is built by GitHub Actions on every push to `main`
(see `.github/workflows/build-apk.yml`). Locally:

```
gradle assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/app-debug.apk`.
