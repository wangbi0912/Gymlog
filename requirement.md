# Role & Expertise
你是一位拥有10年经验的Android首席架构师兼极简主义UI设计师。你精通：
- Jetpack Compose + Material Design 3 的深层定制
- Clean Architecture（Domain/Data/Presentation分层）
- LLM API 集成与 Prompt Engineering
- 离线优先（Offline-First）架构设计
- 性冷淡/极简主义（Minimalist）视觉体系

---

# Project Overview
开发一款名为「GymLog」的Android健身打卡App。核心理念借鉴 **GitHub Gerrit 代码审查机制**：
- 用户的每次训练记录 = 一次「Commit/PR」
- LLM = 「Reviewer」，自动审查训练内容并给出建议
- 用户必须自备 LLM API Key（OpenAI / Claude / DeepSeek / Gemini），数据隐私完全自主

---

# Design Philosophy: 「性冷淡风格」
视觉规范必须严格遵守：
1. **色彩**：仅使用黑白灰 + 一种极低饱和度的强调色（如 #E8E4E1 暖灰或 #D4DDE1 冷灰）。禁止高饱和度色彩。
2. **字体**：全系统使用无衬线字体（Inter / Roboto / Noto Sans），字重仅用 300(Light)、400(Regular)、500(Medium)。
3. **留白**：卡片内边距 ≥ 24dp，页面边距 ≥ 20dp，元素间距 ≥ 16dp。
4. **形状**：全部使用小圆角（4dp）或直角，禁止大圆角。
5. **动画**：仅允许0.15s的淡入淡出，禁止弹性动画、转场动画。
6. **质感**：扁平化，无阴影或仅使用1dp极淡阴影（alpha 0.05）。
7. **图标**：线性图标（Outline style），1.5dp描边，无填充。

---

# Core Features（MVP）

## 1. 训练打卡（Training Log）
- 每次训练创建一条「Session Record」：
  - 日期时间（自动）
  - 训练部位（胸/背/腿/肩/臂/有氧/全身）
  - 动作列表（动作名 + 组数 + 次数 + 重量/时长）
  - 主观感受（RPE 1-10，可选文字备注）
  - 健身房地点（可选，GPS或手动）
- 支持「模板」：保存常用训练计划，一键导入

## 2. LLM 审查系统（Gerrit-Style Review）
每次保存训练记录后，触发LLM审查流程：
- **Review Request**：将本次训练数据 + 最近7天/30天历史数据 → 格式化为Prompt
- **Review Status**：Pending / Reviewing / Approved / Needs Improvement
- **Review Comment**：LLM返回结构化建议：
  - `overall`：总体评价（1-2句话）
  - `strengths`：本次亮点
  - `issues`：潜在问题（如容量突增、休息不足、动作不平衡）
  - `suggestions`：下次训练建议（具体可执行）
  - `score`：训练质量分（0-100，可选）
- **Review Thread**：用户可对每条建议进行「Resolve / Reply」，形成对话线程（本地存储）

## 3. API Key 管理
- 用户在设置页配置自己的 API Key（本地加密存储，Android Keystore）
- 支持选择模型提供商（OpenAI / Anthropic / DeepSeek / Gemini）
- 支持自定义 Base URL（兼容第三方代理或自托管）
- 支持选择模型版本（gpt-4o / claude-3-sonnet 等）

## 4. 历史与分析
- 日历视图：标记训练日，颜色深浅表示训练强度
- 统计卡片：周/月/年维度（总容量、总组数、频率热力图）
- 趋势图表：性冷淡风格的折线图/柱状图（使用 Compose Canvas 自绘，禁用彩色图表库）

## 5. 数据与隐私
- **完全本地存储**：Room Database，无需用户账号
- **导出**：支持导出 JSON / CSV
- **备份**：可选 WebDAV / 本地文件 备份

---

# Technical Architecture

## 分层结构（Clean Architecture）

app/
├── presentation/          # Compose UI + ViewModel + State
│   ├── theme/             # 性冷淡配色系统（Color.kt, Type.kt）
│   ├── components/        # 原子化组件（Atomic Design）
│   └── screens/
├── domain/                # UseCase + Repository Interface + Model
│   ├── model/             # Session, Exercise, Review, Comment
│   ├── repository/        # 接口定义
│   └── usecase/           # 业务用例（SaveSession, RequestReview等）
├── data/                  # Repository实现 + 数据源
│   ├── local/             # Room（Entity + DAO）
│   ├── remote/            # LLM API Client（Retrofit/OkHttp）
│   └── repository/
└── di/                    # Hilt Module


## 关键技术决策
- **UI**：Jetpack Compose 100%，禁用 XML Layout
- **状态管理**：MVI（单向数据流），StateFlow
- **数据库**：Room，支持 FTS（全文搜索动作库）
- **后台任务**：WorkManager 处理 LLM 异步请求（避免主线程阻塞）
- **网络**：OkHttp + Retrofit，支持自定义拦截器（注入API Key）
- **本地加密**：EncryptedSharedPreferences + Android Keystore
- **图表**：自绘 Canvas（确保风格统一），禁用 MPAndroidChart
- **依赖注入**：Hilt

---

# LLM Prompt Template（系统级）

定义LLM审查的Prompt模板，需支持变量注入：

```kotlin
object ReviewPromptTemplate {
    fun build(
        currentSession: Session,
        history: List<<Session>,
        userProfile: UserProfile,
        provider: LLMProvider
    ): String = """
    你是一位专业的力量训练教练和运动科学顾问。请审查以下训练记录，给出专业建议。
    
    ## 用户档案
    - 训练年限：${userProfile.yearsOfTraining}
    - 目标：${userProfile.goal}
    - 近期状态：${userProfile.recentStatus}
    
    ## 本次训练记录（${currentSession.date}）
    - 部位：${currentSession.bodyPart}
    - 动作：
    ${currentSession.exercises.joinToString("\n") { "    - ${it.name}: ${it.sets}组 x ${it.reps}次 @ ${it.weight}kg" }}
    - 主观RPE：${currentSession.rpe}/10
    - 备注：${currentSession.notes}
    
    ## 近期历史（最近7天）
    ${history.takeLast(7).joinToString("\n") { "- ${it.date}: ${it.bodyPart}, ${it.totalVolume}kg 容量" }}
    
    ## 输出要求（严格JSON格式）
    {
      "overall": "string",
      "strengths": ["string"],
      "issues": ["string"],
      "suggestions": ["string"],
      "score": number,
      "tags": ["string"]
    }
    
    注意：
    1. 建议必须具体、可执行，避免空泛
    2. 如果检测到容量突增（相比上周同部位 >20%），必须警告受伤风险
    3. 如果连续3次同部位训练，建议调整动作顺序或引入变式
    4. 语气专业但亲切，避免过度批评
    """.trimIndent()
}

UI/UX 详细规范
页面结构
首页（Dashboard）
顶部：本月训练次数 + 连续打卡天数（大字，Light字重）
中部：最近审查状态（Pending/Approved卡片）
底部：「+ 开始训练」主按钮（全宽，48dp高，直角，#333333背景，白色文字）
训练记录页（Session Editor）
顶部：日期选择器（极简，无图标）
动作列表：可折叠卡片，每组显示为 深蹲  4×8  @ 80kg
底部悬浮：「保存并提交审查」按钮
审查详情页（Review Detail）
顶部：质量分（超大号数字，居中，Light字重）
中部：总体评价（引用样式，左侧2dp竖线）
列表：Strengths / Issues / Suggestions（使用不同前缀图标，但同色）
底部：Resolve / Reply 操作栏
历史页（History）
顶部：月份选择器
日历网格：训练日标记为深灰色圆点，非训练日为浅灰
下方：该月训练列表
设置页（Settings）
API Key 输入框（密文，带显示切换）
模型选择（单选列表）
数据导出/备份
关于（极简，无Logo，纯文字）
交互细节
列表项左滑可「编辑/删除」（Material SwipeToDismiss，极淡背景色）
下拉刷新：极简线性进度条（2dp高度，灰色）
空状态：居中文字「暂无记录」，无插图
加载状态：骨架屏（Shimmer），灰白渐变

Data Models（核心实体）

Extensibility Plan（扩展路线图）
架构必须预留以下扩展点，当前Prompt中需体现接口设计：
多模态输入：预留 MediaAttachment 接口，未来支持训练视频上传给LLM分析动作姿态
社交功能：预留 ShareTarget 接口，未来支持导出「审查报告」为图片分享
可穿戴设备：预留 HealthDataSource 接口，未来接入心率、睡眠数据
AI 教练升级：预留 CoachPersona 配置，支持切换不同风格教练（严格/温和/力量举/健美）
训练计划生成：预留 ProgramGenerator UseCase，LLM根据历史数据自动生成周期化计划
营养追踪：预留 NutritionLog 模块，与训练数据交叉分析
Deliverables Requirement
请输出以下内容：
完整模块依赖图（各层之间的依赖关系）
核心Compose组件代码（主题配置 + 首页Dashboard + 审查卡片）
Room数据库Schema（Entity + DAO + Database）
LLM Repository实现（含错误处理、重试机制、流式响应支持）
审查状态机设计（StateFlow状态流转图）
性冷淡配色代码（Color.kt完整定义）
扩展接口预留代码（多模态/可穿戴/社交的接口定义）
请确保所有代码使用 Kotlin，遵循官方 Kotlin Coding Conventions，Compose 使用 Material3 但完全覆盖为性冷淡风格。


---

## 💡 使用建议

1. **直接复制**：将上方 Prompt 完整复制给 Claude 3.5 Sonnet / GPT-4 / DeepSeek V3，它会直接生成可运行的架构代码。
2. **迭代细化**：如果某部分不满意（比如LLM Prompt模板），可以单独截取该模块让模型优化。
3. **扩展控制**：Prompt 最后的 **Extensibility Plan** 已经帮你预留了6个扩展方向，后续只需实现对应接口即可。
4. **视觉统一**：`theme/` 部分的 Color.kt 定义是关键，一旦确定，整个App的性冷淡风格就锁死了。

如果你需要，我可以进一步帮你把这个 Prompt 中的某个具体模块（比如 **LLM Repository 实现** 或 **性冷淡 Compose 主题代码**）展开成可直接粘贴到 Android Studio 的完整代码。