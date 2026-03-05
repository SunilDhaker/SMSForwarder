# SMS Forwarder

Android app that receives SMS messages, matches them against rules, optionally transforms content, and forwards to SMS numbers or webhooks.

## Features
- Rule-based matching with sender/body regex
- Optional text replacement before forwarding
- Multiple forward targets per rule (SMS and webhook)
- Forwarding history and logs
- Regex tester screen for quick rule validation
- Foreground service controls for reliability on modern Android versions

## Tech Stack
- Kotlin + Jetpack Compose
- Room for local persistence
- WorkManager for background processing
- OkHttp for webhook delivery

## Requirements
- Android Studio (latest stable recommended)
- Android SDK installed locally
- Android 11+ device/emulator (`minSdk = 30`)

## Quick Start
1. Open the project in Android Studio.
2. Let Gradle sync.
3. Run the `app` module on a device/emulator.
4. Grant SMS and notification permissions.
5. Add your own forwarding rule from the `Rules` tab.

No personal or pre-seeded forwarding rules are included by default.

## Build and Test
```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

## Release Signing
1. Create a release keystore (one-time):
```bash
keytool -genkeypair -v -keystore release.keystore -alias smsforwarder -keyalg RSA -keysize 2048 -validity 10000
```
2. Create local signing config:
```bash
cp keystore.properties.example keystore.properties
```
3. Fill `keystore.properties` with real credentials.
4. Build release APK:
```bash
./gradlew assembleRelease
```

The build script reads signing values from either:
- `keystore.properties` (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`)
- or CI environment variables:
  `ANDROID_SIGNING_STORE_FILE`, `ANDROID_SIGNING_STORE_PASSWORD`, `ANDROID_SIGNING_KEY_ALIAS`, `ANDROID_SIGNING_KEY_PASSWORD`

## Permissions
- `RECEIVE_SMS`: receive incoming SMS
- `SEND_SMS`: forward via SMS target
- `INTERNET`: forward via webhook target
- `RECEIVE_BOOT_COMPLETED`: restore foreground service after reboot (when enabled)
- `POST_NOTIFICATIONS`: show reliability/forwarding notification status

## Security Notes
- Avoid committing real phone numbers, webhook URLs, or tokens.
- Webhook secrets should be rotated if ever exposed.
- Forwarded content may contain sensitive data; use carefully and lawfully.

## License
MIT. See [LICENSE](LICENSE).
