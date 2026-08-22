# WeChat-Anti-Recall (微信防撤回)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Platform](https://img.shields.io/badge/platform-Android%206.0--16.0-blue)](https://www.android.com/)

[**简体中文**](README.md) | [**English**](README-en-US.md) | **繁體中文**

**微信訊息防撤回工具**

> 本專案不修改微信原始 APK，僅透過 Android 通知監聽機制實現防撤回功能。

## 運作原理

當微信發送新訊息時，會先產生一則通知推送至系統通知欄。Anti Recall 透過 `NotificationListenerService` 即時監聽這則通知，在微信發出撤回指令、訊息通知被系統移除之前，將通知內容（發送者、文字內容、時間）儲存到本機資料庫。這樣即使對方撤回了訊息，你仍然可以在應用程式內檢視到原始內容。

支援偵測的特殊訊息類型：
- **語音通話邀請** — 對方發起語音通話時的通知
- **視訊通話邀請** — 對方發起視訊通話時的通知
- **紅包** — 收到微信紅包的通知
- **轉帳** — 收到微信支付轉帳的通知

## 功能特色

- **即時攔截** — 透過 NotificationListenerService 監聽微信通知，在撤回生效前捕獲訊息
- **本機儲存** — 使用 Room 資料庫永久儲存捕獲的訊息，支援離線檢視
- **全文搜尋** — 支援按訊息內容、聯絡人名稱、時間範圍進行模糊搜尋
- **分類篩選** — 按聯絡人或群組分組，支援拼音排序
- **重點提醒** — 對語音通話、視訊通話、轉帳、紅包等特殊訊息推播高優先級通知
- **保活服務** — 背景持續執行，配合自動啟動管理保持服務活躍
- **開機重啟** — 接收 BOOT_COMPLETED 廣播，裝置重啟後自動恢復服務
- **Material Design 3** — 採用 Jetpack Compose + Material3 UI，支援深色模式
- **不需 Root** — 僅需通知權限即可使用，適用於 Android 6.0 - 16.0

## 系統需求

- Android 6.0 (API 23) 以上
- 不需 Root 權限
- 需授予「通知使用權」給 Anti Recall

## 安裝步驟

### 從原始碼建構

```bash
git clone https://github.com/kkkzheli/WeChat-Anti-Recall.git
cd WeChat-Anti-Recall
./gradlew assembleDebug
```

產生的 APK 位於 `app/build/outputs/apk/debug/` 目錄。

### 安裝到裝置

1. 下載或建構好 APK 檔案
2. 在目標裝置上允許「安裝未知應用程式」
3. 安裝 APK：
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## 使用方法

### 第一步：授予通知權限

安裝完成後首次開啟應用程式，會提示需要通知權限。按提示前往系統設定開啟「Anti Recall 的通知使用權」。

路徑通常為：**設定 > 安全/隱私 > 通知使用權 > Anti Recall → 開啟**

### 第二步：開始防撤回

授予權限後，Anti Recall 會自動開始在背景監聽微信訊息通知。當微信好友撤回訊息時，你可以在應用程式的「訊息記錄」頁面檢視到被撤回的原始內容。

### 第三步：管理訊息

- **搜尋** — 點擊搜尋框輸入關鍵字，支援訊息內容、聯絡人、時間的組合搜尋
- **篩選** — 按聯絡人或群組類別過濾訊息列表
- **清空記錄** — 設定頁可一鍵清空所有已儲存的訊息記錄

## 權限說明

| 權限 | 用途 |
|------|------|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | 監聽系統通知（核心功能） |
| `POST_NOTIFICATIONS` (Android 13+) | 傳送重要訊息提醒通知 |
| `FOREGROUND_SERVICE` | 保活前台服務 |
| `RECEIVE_BOOT_COMPLETED` | 裝置重啟後自動恢復服務 |

> 注意：需要在系統設定中手動授予「通知使用權」，僅執行時權限不夠。

## 免責聲明

- 本專案與騰訊公司（Tencent）或微信（WeChat）沒有任何關聯關係
- 本工具僅供個人學習和研究用途
- 請勿用於任何商業目的或侵犯他人隱私的行為
- 使用應遵守所在國家/地區的相關法律法規
- 開發者不對使用本工具造成的任何後果負責

## 授權條款

本專案採用 [MIT License](LICENSE) 開放原始碼協議。

## 更新日誌

詳見 [CHANGELOG.md](CHANGELOG.md)。

## 相關連結

- [GitHub 倉庫](https://github.com/kkkzheli/WeChat-Anti-Recall)
- [提交 Issue](https://github.com/kkkzheli/WeChat-Anti-Recall/issues)

---

Made by **kkkzheli** · Built with ❤️ on Android
