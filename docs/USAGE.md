# Usage Guide

## Install

1. Download the latest signed APK from the release page.
2. Install it on the Android device that will receive SMS messages.
3. Grant the requested permissions on first launch.

## Create A Rule

1. Open `Rules`.
2. Tap `Add Rule`.
3. Give the rule a name.
4. Add a sender regex, body regex, or both.
5. Choose one or more targets:
   - SMS number
   - Webhook URL
6. Save the rule and enable it.

## Regex Matching

- `senderRegex` matches the SMS sender/origin
- `bodyRegex` matches the SMS content
- Both are case-insensitive
- If both are present, both must match

## Body Rewriting

You can rewrite the message body before forwarding:

- Use a single regex replacement for simple transforms
- Use multiple replacements when you need to remove or normalize several patterns

## Reliability

Android can aggressively stop background work. For better delivery:

1. Keep the foreground service enabled
2. Exempt the app from battery optimization where possible
3. Review the `Reliability` screen after installing

## Logs And History

- `History` shows forwarding attempts and their outcomes
- `Logs` helps you inspect why a message matched or failed
- `Tester` lets you validate regexes without waiting for a real SMS

## Webhook Payload

Webhook targets receive JSON with these fields:

- `receivedAt`
- `sender`
- `originalBody`
- `transformedBody`
- `ruleName`
- `idempotencyKey`

## Signed Release Builds

For maintainers, signed APKs are produced with:

```bash
./gradlew assembleRelease
```

Signing values come from `keystore.properties` or CI environment variables.
