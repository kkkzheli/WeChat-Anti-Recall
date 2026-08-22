# WeChat-Anti-Recall (WeChat Message Anti-Recall)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Platform](https://img.shields.io/badge/platform-Android%206.0--16.0-blue)](https://www.android.com/)

[**简体中文**](README.md) | **English** | [**繁體中文**](README-TW.md)

A WeChat anti-recall tool that captures notifications from WeChat before they can be recalled — preserving the original message content even after the sender recalls it.

> This project does NOT modify WeChat's original APK. It uses Android's notification listener mechanism to achieve the anti-recall functionality.

## How It Works

When WeChat sends a new message, it first generates a notification pushed to the system notification bar. Anti Recall uses `NotificationListenerService` to monitor these notifications in real time, saving the notification content (sender, text content, timestamp) to a local database before WeChat issues its recall command and the system removes the notification. This way, even if a message is recalled, you can still view the original content in the app.

Supported special message types:
- **Voice Call Invites** — Notification when someone initiates a voice call
- **Video Call Invites** — Notification when someone initiates a video call
- **Red Packets** — Notification for receiving WeChat red packets
- **Transfers** — Notification for receiving WeChat payment transfers

## Features

- **Real-time interception** — Captures WeChat notifications via NotificationListenerService before recall takes effect
- **Local storage** — Persists captured messages using Room database; works offline
- **Full-text search** — Search across message content, contact names, and time ranges
- **Category filtering** — Group messages by contact or group chat, with pinyin sorting
- **Priority alerts** — Pushes high-priority notifications for special message types (voice/video calls, transfers, red packets)
- **Keep-alive service** — Runs persistently in background with auto-start management support
- **Boot restart** — Listens for BOOT_COMPLETED to restore service after device reboot
- **Material Design 3** — Modern UI built with Jetpack Compose + Material3, including dark mode
- **No root required** — Only needs notification permission; supports Android 6.0–16.0

## System Requirements

- Android 6.0 (API 23) or higher
- No root privileges needed
- Notification access permission must be granted to Anti Recall

## Installation

### Build from source

```bash
git clone https://github.com/kkkzheli/WeChat-Anti-Recall.git
cd WeChat-Anti-Recall
./gradlew assembleDebug
```

The generated APK will be located at `app/build/outputs/apk/debug/`.

### Install to device

1. Download or build the APK file
2. Allow "Install unknown apps" on your target device
3. Install the APK:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Usage Guide

### Step 1: Grant notification permission

On first launch after installation, the app will prompt you to grant notification permission. Follow the on-screen instructions to enable "Notification Access" for Anti Recall in system settings.

Typical path: **Settings > Security & Privacy > Notification Access > Anti Recall → Enable**

### Step 2: Start anti-recall

After granting permission, Anti Recall will automatically start monitoring WeChat message notifications in the background. When a WeChat contact recalls a message, you can view the original (pre-recall) content in the app's "Message History" screen.

### Step 3: Manage messages

- **Search** — Tap the search box to enter keywords; supports combined searches across message content, contacts, and timestamps
- **Filter** — Filter the message list by contact or group chat category
- **Clear records** — Use the settings page to clear all saved message records at once

## Permissions

| Permission | Purpose |
|------------|---------|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Monitor system notifications (core feature) |
| `POST_NOTIFICATIONS` (Android 13+) | Send important message alert notifications |
| `FOREGROUND_SERVICE` | Keep-alive foreground service |
| `RECEIVE_BOOT_COMPLETED` | Automatically restore service after device reboot |

> Note: You must manually grant "Notification Access" in system settings. Runtime permissions alone are not sufficient.

## Disclaimer

- This project is not affiliated with Tencent or WeChat in any way
- This tool is intended for personal learning and research purposes only
- Do not use for any commercial purposes or for violating others' privacy
- Users should comply with applicable laws and regulations in their jurisdiction
- The developer is not responsible for any consequences arising from the use of this tool

## License

This project is open-sourced under the [MIT License](LICENSE).

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for all notable changes.

## Links

- [GitHub Repository](https://github.com/kkkzheli/WeChat-Anti-Recall)
- [Report an Issue](https://github.com/kkkzheli/WeChat-Anti-Recall/issues)

---

Made by **kkkzheli** · Built with ❤️ on Android
