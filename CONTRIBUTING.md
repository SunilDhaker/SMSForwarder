# Contributing

## Setup
1. Install Android Studio and Android SDK.
2. Open this project and run a Gradle sync.
3. Verify local build and tests:
```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

## Pull Request Guidelines
- Keep changes focused and small.
- Add or update tests for behavior changes.
- Avoid introducing hardcoded personal data (phone numbers, webhook URLs, tokens).
- Keep UI strings and docs generic and reusable.

## Coding Notes
- Kotlin + Compose style should remain consistent with existing code.
- Prefer non-breaking migrations for Room schema changes.
- Make sure sensitive values are not logged.

