# WeChat-Anti-Recall (微信防撤回)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Platform](https://img.shields.io/badge/platform-Android%206.0--16.0-blue)](https://www.android.com/)

**微信消息防撤回工具** — 通过监听通知栏消息，在微信撤回之前捕获并保存消息内容。

> 本项目不修改微信原始 APK，仅通过 Android 通知监听机制实现防撤回功能。

## 工作原理

当微信发送新消息时，会先产生一条通知推送至系统通知栏。Anti Recall 通过 `NotificationListenerService` 实时监听这条通知，在微信发出撤回指令、消息通知被系统移除之前，将通知内容（发送者、文本内容、时间）保存到本地数据库。这样即使对方撤回了消息，你仍然可以在应用内查看到原始内容。

支持检测的特殊消息类型：
- **语音通话邀请** — 对方发起语音通话时的通知
- **视频通话邀请** — 对方发起视频通话时的通知
- **红包** — 收到微信红包的通知
- **转账** — 收到微信支付转账的通知

## 功能特性

- **实时拦截** — 通过 NotificationListenerService 监听微信通知，在撤回生效前捕获消息
- **本地存储** — 使用 Room 数据库持久化保存捕获的消息，支持离线查看
- **全文搜索** — 支持按消息内容、联系人名称、时间范围进行模糊搜索
- **分类筛选** — 按联系人 / 群聊分组，支持拼音排序
- **重点提醒** — 对语音通话、视频通话、转账、红包等特殊消息推送高优先级通知
- **保活服务** — 后台持续运行，配合自启动管理保持服务活跃
- **开机重启** — 接收 BOOT_COMPLETED 广播，设备重启后自动恢复服务
- **Material Design 3** — 采用 Jetpack Compose + Material3 UI，支持深色模式
- **无需 Root** — 仅需通知权限即可使用，适用于 Android 6.0 - 16.0

## 系统要求

- Android 6.0 (API 23) 及以上
- 不需要 Root 权限
- 需要授予「通知使用权」给 Anti Recall

## 安装步骤

### 从源码构建

```bash
git clone https://github.com/kkkzheli/WeChat-Anti-Recall.git
cd WeChat-Anti-Recall
./gradlew assembleDebug
```

生成的 APK 位于 `app/build/outputs/apk/debug/` 目录。

### 安装到设备

1. 下载或构建好 APK 文件
2. 在目标设备上允许「安装未知应用」
3. 安装 APK：
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## 使用说明

### 第一步：授予通知权限

安装完成后首次打开应用，会提示需要通知权限。按提示前往系统设置开启「Anti Recall 的通知使用权」。

路径通常为：**设置 > 安全/隐私 > 通知使用权 > Anti Recall → 开启**

### 第二步：开始防撤回

授予权限后，Anti Recall 会自动开始在后台监听微信消息通知。当微信好友撤回消息时，你可以在应用的「消息记录」页面查看到被撤回的原始内容。

### 第三步：管理消息

- **搜索** — 点击搜索框输入关键词，支持消息内容、联系人、时间的组合搜索
- **筛选** — 按联系人或群聊类别过滤消息列表
- **清空记录** — 设置页可一键清空所有已保存的消息记录

## 权限说明

| 权限 | 用途 |
|------|------|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | 监听系统通知（核心功能） |
| `POST_NOTIFICATIONS` (Android 13+) | 发送重要消息提醒通知 |
| `FOREGROUND_SERVICE` | 保活前台服务 |
| `RECEIVE_BOOT_COMPLETED` | 设备重启后自动恢复服务 |

> 注意：需要在系统设置中手动授予「通知使用权」，仅运行时权限不够。

## 免责声明

- 本项目与腾讯公司（Tencent）或微信（WeChat）无任何关联关系
- 本工具仅供个人学习和研究用途
- 请勿用于任何商业目的或侵犯他人隐私的行为
- 使用者应遵守所在国家/地区的法律法规
- 开发者不对使用本工具造成的任何后果负责

## 许可证

本项目采用 [MIT License](LICENSE) 开源协议。

## 更新日志

详见 [CHANGELOG.md](CHANGELOG.md)。

## 相关链接

- [GitHub 仓库](https://github.com/kkkzheli/WeChat-Anti-Recall)
- [提交 Issue](https://github.com/kkkzheli/WeChat-Anti-Recall/issues)

---

Made by **kkkzheli** · Built with ❤️ on Android
