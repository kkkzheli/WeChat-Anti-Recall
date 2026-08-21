# Changelog

All notable changes to WeChat-Anti-Recall will be documented in this file.

## [1.0.1] - 2026-08-22
### Fixed
- **修复前台服务崩溃**: `Bad notification for startForeground` - 通知通道不存在导致启动即闪退
- **实现通知通道创建**: NotificationHelper 现在实际创建 keep_alive 和 special_messages 通道
- **应用启动时初始化通道**: App.onCreate() 创建所有通知通道
- **修复 debug 包名不匹配**: 移除 applicationIdSuffix 避免 Activity 找不到
- **修复应用图标**: 使用用户提供的 PNG，移除破损的自适应图标 XML
- **compileSdk 升级至 34**: 适配 NotificationListenerService 新 API (compileSdk 33 → 34)

## [1.0.0] - 2026-08-21
### Added
- Initial release of Anti Recall
- WeChat notification capture via NotificationListenerService
- Message database with Room (searchable, filterable)
- Special message detection (voice calls, video calls, transfers, red packets)
- Priority notifications for special messages
- Keep-alive service for persistent background operation
- Boot receiver to restart service after device reboot
- Material3 Compose UI with dark mode support
- Search across messages, contacts, and timestamps
- Contact/group filter with pinyin sorting
- Clear all records function
- GitHub project integration
