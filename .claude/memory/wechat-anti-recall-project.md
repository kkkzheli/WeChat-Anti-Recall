---
name: wechat-anti-recall-project
description: 微信防撤回 Android 项目 - kkkzheli 开发的 WeChat-Anti-Recall 项目完整记录
metadata:
  type: project
---

# WeChat-Anti-Recall 项目

## 项目信息
- **名称**: WeChat-Anti-Recall (Anti Recall)
- **作者**: kkkzheli
- **包名**: kkkzheli.antirecall.wechat
- **目标 Android**: 6.0-14 (API 23-34)
- **compileSdk**: 34
- **minSdk**: 23
- **targetSdk**: 34
- **GitHub**: https://github.com/kkkzheli/WeChat-Anti-Recall
- **版本**: v1.0.0 (tagged & pushed)

## 核心功能
1. **通知捕获**: NotificationListenerService 捕获 com.tencent.mm 通知
2. **防撤回**: 消息在微信撤回前已保存到 Room 数据库
3. **持久化存储**: Room 数据库永久保存消息记录
4. **保活机制**: 前台服务 + 开机启动接收器
5. **特殊消息高亮**: 语音通话、转账、红包用鲜艳颜色显示

## 技术实现
- Kotlin + Jetpack Compose + Material3
- Room 数据库 (KSP codegen)
- StateFlow + collectAsStateWithLifecycle (Compose 优先)
- CoroutineScope for service async work
- StatusBarNotification API for notification capture (compileSdk 34 compatible)

## API 细节
- `onNotificationPosted(sbn: StatusBarNotification?, record: RankingMap?)` - compileSdk 34 新签名
- `onNotificationRemoved(sbn: StatusBarNotification?, record: RankingMap?, reason: Int)`
- `viewModel.contactNames.collectAsStateWithLifecycle()` - 不用 LiveData collect

## 权限需求
- RECEIVE_BOOT_COMPLETED (开机启动)
- FOREGROUND_SERVICE / FOREGROUND_SERVICE_SPECIAL_USE
- SYSTEM_ALERT_WINDOW (悬浮窗)
- REQUEST_IGNORE_BATTERY_OPTIMIZATIONS (忽略电池优化)
- POST_NOTIFICATIONS (通知权限 - 核心)

## 构建状态
- **编译错误**: 已全部修复 (零错误)
- **APK**: app-debug.apk (18MB)，已安装到设备 7XKFPVIZOJDEEIBI
- **Git**: master 分支已推送，v1.0.0 tag 已创建
- **图标**: 经典 PNG 启动图标 (mipmap-*/ic_launcher.png)；启动画面标记是 drawable/ic_splash_mark.xml（勿混淆，ic_launcher_foreground 已删除）

## 安装到手机
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 使用步骤
1. 安装 APK
2. 授予通知权限 (系统设置 → 通知访问 → Anti Recall)
3. 授予前台服务权限
4. 打开 App，微信消息会自动捕获并记录

## 注意事项
- 项目不修改微信任何文件
- 消息存储在本地 Room 数据库
- 保活需要用户手动设置 (系统限制)
- 需要 Android 6.0+ (API 23+)

---
**最后更新**: 2026-08-22
**作者**: kkkzheli
**状态**: ✅ 编译通过，可发布
