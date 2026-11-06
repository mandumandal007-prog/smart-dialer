# CallCard Dialer

A lightweight, offline Android dialer with a full-screen **calling card** UI, dual-SIM choice, T9 smart search, call-log tools and a local block list. No analytics, no ads, no network calls — everything stays on the phone.

Built as a plain-Android (no AndroidX) app, so the APK is tiny (~110 KB) and installs on Android 7.0 (API 24) and above.

## Download

Grab `CallCardDialer.apk` from this repo and install it (allow "install from unknown sources" when prompted).

## Features

**Keypad**
- Big keypad with DTMF touch tones and haptics (both can be switched off)
- Long-press `0` for `+`, long-press backspace to clear
- T9 smart search: type `5465` and see "LINK" style name matches above the number
- One-tap save of an unknown number to contacts

**Calling card**
- Full-screen card: avatar with pulsing rings, name, number, live status and timer
- Call button becomes an end-call button once the call starts
- Mute, speaker, message and block shortcuts right on the card
- Post-call summary with duration

**Recents**
- Full device call log with incoming / outgoing / missed / rejected markers
- Tap to call, long-press for call, SMS, copy, save, block, delete
- Clear the whole log in one go

**Contacts**
- Alphabetical list with letter headers and avatars
- Search by name, number or T9
- Contact detail page: call, message, edit, copy, share, block

**Settings**
- Three themes: Dark, Light, AMOLED black
- Touch tones, vibration, T9 toggle
- Preferred SIM for outgoing calls (dual SIM)
- Show/hide the calling card before dialing
- Blocked numbers manager
- Permission dashboard with per-permission grant buttons

## Permissions used

| Permission | Why |
|---|---|
| `CALL_PHONE` | Place calls |
| `READ_PHONE_STATE` | Detect call connect / end |
| `READ_PHONE_NUMBERS` | SIM and own-number details |
| `READ_CONTACTS` / `WRITE_CONTACTS` | Names in lists, save new numbers |
| `READ_CALL_LOG` / `WRITE_CALL_LOG` | Recents tab, delete entries |
| `ANSWER_PHONE_CALLS` | End a call from the card |
| `SEND_SMS` | Quick reply shortcut |
| `VIBRATE`, `MODIFY_AUDIO_SETTINGS` | Haptics, speaker and mic toggles |

Every permission is requested at runtime with a plain-language reason, and the app keeps working with whatever you allow.

## Build from source

Requirements: JDK 17, Android SDK build-tools 34.0.0, platform android-34.

```bash
aapt2 compile --dir res -o build/res.zip
aapt2 link -o build/base.apk -I $ANDROID_HOME/platforms/android-34/android.jar \
  --manifest AndroidManifest.xml -R build/res.zip --java build/gen --auto-add-overlay
javac -source 11 -target 11 -cp $ANDROID_HOME/platforms/android-34/android.jar \
  -d build/obj $(find src build/gen -name "*.java")
d8 --min-api 24 --lib $ANDROID_HOME/platforms/android-34/android.jar --output build/dex $(find build/obj -name "*.class")
cd build/dex && zip ../base.apk classes.dex && cd ..
zipalign -p -f 4 base.apk aligned.apk
apksigner sign --ks debug.keystore --out CallCardDialer.apk aligned.apk
```

## Notes

- The app is a normal dialer, not a system phone replacement: Android's own in-call screen handles active calls unless the app is set as the default phone app. The calling card shows before the call, and the timer/status stay live when you return to it.
- Call recording is deliberately not included.
- The signing key in this repo is a debug key — generate your own before publishing anywhere.

## License

MIT
