# WeChat-Anti-Recall (微信防撤回)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Platform](https://img.shields.io/badge/platform-Android%206.0--16.0-blue)](https://www.android.com/)

**EN / [简体中文](#简体介绍) / [繁體中文](#繁體介紹)**

---

## English {#英语}

A WeChat anti-recall tool that captures notifications from WeChat before they can be recalled -- preserving the original message content even after the sender recalls it.

> This project does NOT modify WeChat's original APK. It uses Android's notification listener mechanism to achieve the anti-recall functionality.

### How It Works

When WeChat sends a new message, it first generates a notification pushed to the system notification bar. Anti Recall uses `NotificationListenerService` to monitor these notifications in real time, saving the notification content (sender, text content, timestamp) to a local database before WeChat issues its recall command and the system removes the notification. This way, even if a message is recalled, you can still view the original content in the app.

Supported special message types:

- **Voice Call Invites** -- Notification when someone initiates a voice call
- **Video Call Invites** -- Notification when someone initiates a video call
- **Red Packets** -- Notification for receiving WeChat red packets
- **Transfers** -- Notification for receiving WeChat payment transfers

### Features

- **Real-time interception** -- Captures WeChat notifications via NotificationListenerService before recall takes effect
- **Local storage** -- Persists captured messages using Room database; works offline
- **Full-text search** -- Search across message content, contact names, and time ranges
- **Category filtering** -- Group messages by contact or group chat, with name cleaning ([N] prefix stripped)
- **Priority alerts** -- Pushes high-priority notifications for special message types (voice/video calls, transfers, red packets)
- **Keep-alive service** -- Runs persistently in background with auto-start management support
- **Boot restart** -- Listens for BOOT_COMPLETED to restore service after device reboot
- **Material Design 3** -- Modern UI built with Jetpack Compose + Material3, including dark mode
- **No root required** -- Only needs notification permission; supports Android 6.0--16.0

### System Requirements

- Android 6.0 (API 23) or higher
- No root privileges needed
- Notification access permission must be granted to Anti Recall

### Installation

#### Build from source

```bash
git clone https://github.com/kkkzheli/WeChat-Anti-Recall.git
cd WeChat-Anti-Recall
./gradlew assembleDebug
```

The generated APK will be located at `app/build/outputs/apk/debug/`.

#### Install to device

1. Download or build the APK file
2. Allow "Install unknown apps" on your target device
3. Install the APK:

   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Usage Guide

#### Step 1: Grant notification permission

On first launch after installation, the app will prompt you to grant notification permission. Follow the on-screen instructions to enable "Notification Access" for Anti Recall in system settings.

Typical path: **Settings > Security & Privacy > Notification Access > Anti Recall → Enable**

#### Step 2: Start anti-recall

After granting permission, Anti Recall will automatically start monitoring WeChat message notifications in the background. When a WeChat contact recalls a message, you can view the original (pre-recall) content in the app's "Message History" screen.

#### Step 3: Manage messages

- **Search** — Tap the search box to enter keywords; supports combined searches across message content, contacts, and timestamps
- **Filter** — Filter the message list by contact or group chat category
- **Clear records** — Use the settings page to clear all saved message records at once

### Permissions

| Permission | Purpose |
|------------|---------|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Monitor system notifications (core feature) |
| `POST_NOTIFICATIONS` (Android 13+) | Send important message alert notifications |
| `FOREGROUND_SERVICE` | Keep-alive foreground service |
| `RECEIVE_BOOT_COMPLETED` | Automatically restore service after device reboot |

> Note: You must manually grant "Notification Access" in system settings. Runtime permissions alone are not sufficient.

### Disclaimer

- This project is not affiliated with Tencent or WeChat in any way
- This tool is intended for personal learning and research purposes only
- Do not use for any commercial purposes or for violating others' privacy
- Users should comply with applicable laws and regulations in their jurisdiction
- The developer is not responsible for any consequences arising from the use of this tool

### License

This project is open-sourced under the [MIT License](LICENSE).

### Changelog

See below (下方更新日志).

### Links

- [GitHub Repository](https://github.com/kkkzheli/WeChat-Anti-Recall)
- [Report an Issue](https://github.com/kkkzheli/WeChat-Anti-Recall/issues)

---

<a id="简体介绍"></a>

## 简体中文

**[\U0001F1EB EN](#english-) | 简体中文 | [\U0001F1FC 繁体中文](#繁体介绍-)**

微信消息防撤回工具 — — 通过监听通知栏消息，在微信撤回之前捕获并保存消息内容。

> 本项目不修改微信原始 APK，仅通过 Android 通知监听机制实现防撤回功能。

### 工作原理

当微信发送新消息时，会先产生一条通知推送至系统通知栏。Anti Recall 通过 `NotificationListenerService` 实时监听这条通知，在微信发出撤回指令、消息通知被系统移除之前，将通知内容（发送者、文本内容、时间）保存到本地数据库。这样即使对方撤回了消息，你仍然可以在应用内查看到原始内容。

支持检测的特殊消息类型：

- **语音通话邀请** — — 对方发起语音通话时的通知
- **视频通话邀请** — — 对方发起视频通话时的通知
- **红包** — — 收到微信红包的通知
- **转账** — — 收到微信支付转账的通知

### 功能特性

- **实时拦截** — — 通过 NotificationListenerService 监听微信通知，在撤回生效前捕获消息
- **本地存储** — — 使用 Room 数据库持久化保存捕获的消息，支持离线查看
- **全文搜索** — — 支持按消息内容、联系人名称、时间范围进行模糊搜索
- **分类筛选** — — 按联系人 / 群聊分组，自动清理 [N] 前缀，支持拼音排序
- **重点提醒** — — 对语音通话、视频通话、转账、红包等特殊消息推送高优先级通知
- **保活服务** — — 后台持续运行，配合自启动管理保持服务活跃
- **开机重启** — — 接收 BOOT_COMPLETED 广播，设备重启后自动恢复服务
- **Material Design 3** — — 采用 Jetpack Compose + Material3 UI，支持深色模式
- **无需 Root** — — 仅需通知权限即可使用，适用于 Android 6.0 – 16.0

### 系统要求

- Android 6.0 (API 23) 及以上
- 不需要 Root 权限
- 需要授予「通知使用权」给 Anti Recall

### 安装步骤

#### 从源码构建

```bash
git clone https://github.com/kkkzheli/WeChat-Anti-Recall.git
cd WeChat-Anti-Recall
./gradlew assembleDebug
```

生成的 APK 位于 `app/build/outputs/apk/debug/` 目录。

#### 安装到设备

1. 下载或构建好 APK 文件
2. 在目标设备上允许「安装未知应用」
3. 安装 APK：

   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### 使用说明

#### 第一步：授予通知权限

安装完成后首次打开应用，会提示需要通知权限。按提示前往系统设置开启「Anti Recall 的通知使用权」。

路径通常为：**设置 > 安全/隐私 > 通知使用权 > Anti Recall → 开启**

#### 第二步：开始防撤回

授予权限后，Anti Recall 会自动开始在后台监听微信消息通知。当微信好友撤回消息时，你可以在应用的「消息记录」页面查看到被撤回的原始内容。

#### 第三步：管理消息

- **搜索** — — 点击搜索框输入关键词，支持消息内容、联系人、时间的组合搜索
- **筛选** — — 按联系人或群聊类别过滤消息列表
- **清空记录** — — 设置页可一键清空所有已保存的消息记录

### 权限说明

| 权限 | 用途 |
|------|------|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | 监听系统通知（核心功能） |
| `POST_NOTIFICATIONS` (Android 13+) | 发送重要消息提醒通知 |
| `FOREGROUND_SERVICE` | 保活前台服务 |
| `RECEIVE_BOOT_COMPLETED` | 设备重启后自动恢复服务 |

> 注意：需要在系统设置中手动授予「通知使用权」，仅运行时权限不够。

### 免责声明

- 本项目与腾讯公司（Tencent）或微信（WeChat）无任何关联关系
- 本工具仅供个人学习和研究用途
- 请勿用于任何商业目的或侵犯他人隐私的行为
- 使用者应遵守所在国家/地区的法律法规
- 开发者不对使用本工具造成的任何后果负责

### 许可证

本项目采用 [MIT License](LICENSE) 开源协议。

### 相关链接

- [GitHub 仓库](https://github.com/kkkzheli/WeChat-Anti-Recall)
- [提交 Issue](https://github.com/kkkzheli/WeChat-Anti-Recall/issues)

---

<a id="繁体介绍"></a>

## 繁體中文

**[\U0001F1EB English](#english-) | [\U0001F1E8 简体中文](#簡體介紹) | 繁體中文**

微信訊息防撤回工具 — — 透過監聽通知欄訊息，在微信撤回之前捕獲並儲存訊息內容。

> 本專案不修改微信原始 APK，僅透過 Android 通知監聽機制實現防撤回功能。

### 運作原理

當微信發送新訊息時，會先產生一則通知推送至系統通知欄。Anti Recall 透過 `NotificationListenerService` 即時監聽這則通知，在微信發出撤回指令、訊息通知被系統移除之前，將通知內容（發送者、文字內容、時間）儲存到本機資料庫。這樣即使對方撤回了訊息，你仍然可以在應用程式內檢視到原始內容。

支援偵測的特殊訊息類型：

- **語音通話邀請** — — 對方發起語音通話時的通知
- **視訊通話邀請** — — 對方發起視訊通話時的通知
- **紅包** — — 收到微信紅包的通知
- **轉帳** — — 收到微信支付轉帳的通知

### 功能特色

- **即時攔截** — — 透過 NotificationListenerService 監聽微信通知，在撤回生效前捕獲訊息
- **本機儲存** — — 使用 Room 資料庫永久儲存捕獲的訊息，支援離線檢視
- **全文搜尋** — — 支援按訊息內容、聯絡人名稱、時間範圍進行模糊搜尋
- **分類篩選** — — 按聯絡人或群組分類，自動清理 [N] 前綴，支援拼音排序
- **重點提醒** — — 對語音通話、視訊通話、轉帳、紅包等特殊訊息推播高優先級通知
- **保活服務** — — 背景持續執行，配合自動啟動管理保持服務活躍
- **開機重啟** — — 接收 BOOT_COMPLETED 廣播，裝置重啟後自動恢復服務
- **Material Design 3** — — 採用 Jetpack Compose + Material3 UI，支援深色模式
- **不需 Root** — — 僅需通知權限即可使用，適用於 Android 6.0 – 16.0

### 系統需求

- Android 6.0 (API 23) 以上
- 不需 Root 權限
- 需授予「通知使用權」給 Anti Recall

### 安裝步驟

#### 從原始碼建構

```bash
git clone https://github.com/kkkzheli/WeChat-Anti-Recall.git
cd WeChat-Anti-Recall
./gradlew assembleDebug
```

產生的 APK 位於 `app/build/outputs/apk/debug/` 目錄。

#### 安裝到裝置

1. 下載或建構好 APK 檔案
2. 在目標裝置上允許「安裝未知應用程式」
3. 安裝 APK：

   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### 使用方法

#### 第一步：授予通知權限

安裝完成後首次開啟應用程式，會提示需要通知權限。按提示前往系統設定開啟「Anti Recall 的通知使用權」。

路徑通常為：**設定 > 安全/隱私 > 通知使用權 > Anti Recall → 開啟**

#### 第二步：開始防撤回

授予權限後，Anti Recall 會自動開始在背景監聽微信訊息通知。當微信好友撤回訊息時，你可以在應用程式的「訊息記錄」頁面檢視到被撤回的原始內容。

#### 第三步：管理訊息

- **搜尋** — — 點擊搜尋框輸入關鍵字，支援訊息內容、聯絡人、時間的組合搜尋
- **篩選** — — 按聯絡人或群組類別過濾訊息列表
- **清空記錄** — — 設定頁可一鍵清空所有已儲存的訊息記錄

### 權限說明

| 權限 | 用途 |
|------|------|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | 監聽系統通知（核心功能） |
| `POST_NOTIFICATIONS` (Android 13+) | 傳送重要訊息提醒通知 |
| `FOREGROUND_SERVICE` | 保活前台服務 |
| `RECEIVE_BOOT_COMPLETED` | 裝置重啟後自動恢復服務 |

> 注意：需要在系統設定中手動授予「通知使用權」，僅執行時權限不夠。

### 免責聲明

- 本專案與騰訊公司（Tencent）或微信（WeChat）沒有任何關聯關係
- 本工具僅供個人學習和研究用途
- 請勿用於任何商業目的或侵犯他人隱私權的行為
- 使用者應遵守所在國家/地區的相關法律法規
- 開發者不對使用本工具造成的任何後果負責

### 授權條款

本專案採用 [MIT License](LICENSE) 開放原始碼協議。

### 相關連結

- [GitHub 倉庫](https://github.com/kkkzheli/WeChat-Anti-Recall)
- [提交 Issue](https://github.com/kkkzheli/WeChat-Anti-Recall/issues)

---

## Changelog / 更新日誌 / Changelog

| Version | Date | Notes |
|---------|------|-------|
| v1.4 | 2026-08-22 | Fixed NotificationListenerService override signature (critical bug fix), simplified service startup for HyperOS/Android 15, date picker with cancel/confirm buttons, rounded corners + ripple containment, fade transitions, [N] prefix cleanup, fuzzy matching |
| v1.3 | 2026-08-21 | Back button fix, multi-select filter chips, keep-alive service, boot restart, Material Design 3 UI |
| v1.2 | 2026-08-21 | Filter selection, back navigation, notification capture improvements |

---

Made by **kkkzheli** · Built with ❤️ on Android
