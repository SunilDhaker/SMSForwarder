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

