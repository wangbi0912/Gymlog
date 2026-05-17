# GymLog

> 你的 AI 训练伙伴 | Your AI Training Partner

[![Version](https://img.shields.io/badge/version-0.1.0-lightgrey)](https://github.com/wangbi0912/GymLog)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B-brightgreen)](https://developer.android.com)

GymLog 是一款面向严肃训练者的 Android 健身打卡 App。核心理念：**用 AI 教练替代真人教练**。用户每次训练记录由 LLM 自动审查并给出周期化建议，数据完全本地存储，隐私自主。

GymLog is an Android workout tracker for serious lifters. Core concept: **AI coach replacing human coaches**. Every training session is reviewed by an LLM with periodized suggestions. All data is stored locally — your privacy, your control.

---

## Screenshots (Coming Soon)

## Features | 功能

### v0.1

- [x] **用户引导** — 3 步设置（身体数据 / 训练档案 / API Key）
- [x] **训练打卡** — 动作库搜索、组记录（热身/正式/力竭/降负）、休息计时器、RPE 评级、超级组
- [x] **AI 教练审查** — 支持 OpenAI / Anthropic / DeepSeek / Gemini 四种 LLM，审查建议分类展示、对话回复
- [x] **训练模板** — 4 个预设模板 + 用户自定义
- [x] **历史与分析** — 日历视图、趋势图表、历史搜索
- [x] **身体数据追踪** — 体重记录与趋势
- [x] **数据导出** — JSON / CSV 导出，WebDAV 备份
- [x] **桌面小组件** — 快速开始 / 连续打卡 / 本周摘要
- [x] **极简 UI** — 性冷淡风格设计系统，深色/浅色模式

### Roadmap

- [ ] 周期化训练计划引擎
- [ ] 体态分析（ML Kit Pose Detection）
- [ ] Health Connect 集成
- [ ] 训练伙伴模式（局域网同步）

## Tech Stack | 技术栈

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material3 (fully customized) |
| Architecture | Clean Architecture (Domain / Data / Presentation) |
| State | MVI + StateFlow |
| Database | Room 2.6 |
| DI | Hilt 2.51 |
| Background | WorkManager 2.9 |
| Network | Retrofit 2.11 + OkHttp 4.12 |
| Charts | Compose Canvas (custom-drawn) |
| Widgets | Glance 1.1 |
| Min SDK | 26 (Android 8.0) |

## Build | 构建

### Prerequisites | 前置条件

- JDK 17+
- Android SDK 34 (platform + build-tools)
- Gradle 8.7

### Command Line | 命令行

```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android/sdk

# Generate wrapper (first time)
gradle wrapper --gradle-version 8.7

# Build debug APK
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

### Install via ADB | 通过 ADB 安装

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Project Structure | 项目结构

```
app/src/main/java/com/gymlog/app/
├── di/                    # Hilt dependency injection
├── domain/
│   ├── model/             # Domain models & enums
│   ├── repository/        # Repository interfaces
│   └── usecase/           # Business logic (Dashboard, Review)
├── data/
│   ├── local/             # Room (entities, DAOs, database, presets)
│   ├── remote/            # LLM API client & prompt template
│   └── repository/        # Repository implementations
├── presentation/
│   ├── theme/             # Color, Type, Shape, Spacing
│   ├── components/        # Reusable composables
│   ├── navigation/        # NavGraph
│   ├── viewmodel/         # 10 ViewModels
│   └── screens/           # 9 screens (onboarding, dashboard, session, etc.)
└── worker/                # WorkManager workers & foreground service
```

## License

[MIT](LICENSE) © 2026 wangbi0912

---

**GymLog** — Train smart, not just hard. | 聪明训练，不只是刻苦。
