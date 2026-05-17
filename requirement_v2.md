# Role & Expertise
你是一位拥有10年经验的Android首席架构师兼极简主义UI设计师，且精通全栈的android 编程。你精通：
- Jetpack Compose + Material Design 3 的深层定制
- Clean Architecture（Domain/Data/Presentation分层）
- LLM API 集成与 Prompt Engineering
- 离线优先（Offline-First）架构设计
- 极简主义（Minimalist）视觉体系

---

# Product Overview
「GymLog」是一款面向严肃训练者的Android健身打卡App。核心理念：**用AI教练替代真人教练**。

- 用户每次训练 = 一次数据记录，LLM作为"AI教练"自动审查并给出周期化建议
- 用户自备 LLM API Key（OpenAI / Claude / DeepSeek / Gemini），数据完全本地存储，隐私自主
- 目标用户：有系统训练习惯的中级/高级健身者（非初次入门），追求渐进式超负荷和训练数据化管理

---

# Design Philosophy: 极简功能主义

视觉规范（在v1基础上细化）：

## 色彩系统
- **主色**：黑白灰体系
  - 背景：`#FAFAFA`（浅色模式）/ `#121212`（深色模式）
  - 表面卡片：`#FFFFFF` / `#1E1E1E`
  - 正文：`#1A1A1A` / `#E0E0E0`
  - 辅助文字：`#757575` / `#9E9E9E`
  - 分割线/禁用态：`#E0E0E0` / `#333333`
- **强调色**：仅使用一种极低饱和度色彩，仅用于关键交互状态（选中、完成、警示）
  - 主强调色：`#8B9D8B`（鼠尾草绿，代表"完成/通过"）
  - 警示色：`#C49B8B`（低饱和暖色，代表"需关注"）—— 仅用于 Issues 标记
  - 禁止使用高饱和度红/绿/蓝
- **数据可视化配色**：单色系 + 透明度层级，如 `#333333` @ 100% / 70% / 40% / 15%

## 字体
- 全系统无衬线：首选 Inter（英文+数字），CJK回退 Noto Sans SC
- 字重限制：300(Light)、400(Regular)、500(Medium)、600(SemiBold，仅用于页面主标题)
- 字号阶梯（sp）：11 / 13 / 15 / 17 / 20 / 24 / 32（严格控制，不允许出现中间值）
- 行高：1.5x（正文）、1.2x（标题）

## 间距系统（4dp基准）
- 基准单位：4dp
- xs=4dp, sm=8dp, md=16dp, lg=24dp, xl=32dp, xxl=48dp
- 卡片内边距：24dp
- 页面水平边距：20dp
- 列表项间距：2dp（分割线）或 16dp（卡片间距）
- 所有间距必须是4的整数倍

## 形状
- 小圆角：4dp（按钮、输入框、卡片）
- 直角：0dp（底部操作栏、全宽按钮）
- 禁止 > 8dp 的圆角

## 动画
- 仅允许 0.15s 淡入淡出（Crossfade）
- 列表项出现：0.2s 从下往上平移 + 淡入，每项延迟 0.03s（Staggered）
- 页面转场：无动画（直接替换）
- 骨架屏闪烁：Shimmer 1.5s 循环
- 禁止弹性动画、共享元素转场、路径动画

## 质感
- 扁平化为主
- 卡片：0.5dp  elevation（仅作层次区分，不可见明显阴影）
- 禁止渐变背景、毛玻璃、纹理

## 图标
- 线性图标（Outlined style），1.5dp 描边，无填充
- 统一 24dp 尺寸
- 仅使用黑白灰着色，禁止彩色图标

---

# Core Features（MVP v2）

## 1. 用户引导（Onboarding）
用户首次启动时完成 3 步设置（可跳过，但跳过后功能受限）：

**Step 1 — 身体数据**
- 性别、出生年份、身高(cm)、体重(kg)
- 体重可后续在「身体数据」页面更新，形成趋势图

**Step 2 — 训练档案**
- 训练年限（<6个月 / 6个月-1年 / 1-2年 / 2-5年 / 5年+）
- 训练目标（增肌 / 增力 / 减脂保留肌肉 / 耐力 / 综合）
- 训练频率（每周几天）
- 常练部位（多选）

**Step 3 — API Key 配置**
- 选择模型提供商并填入 API Key
- 支持「稍后配置」，但训练记录页顶部显示持久化提示条

> **设计意图**：训练档案是LLM生成个性化建议的核心上下文，不能省略。

---

## 2. 动作库（Exercise Library）
内置 80+ 常用动作，预填充到 Room 数据库：

### 数据结构
```
Exercise:
  - id, name, bodyPart, equipment, category, isBuiltIn
  - category: 复合动作 / 孤立动作 / 自重 / 有氧 / 柔韧
  - equipment: 杠铃 / 哑铃 / 绳索 / 器械 / 自重 / 其他
  - bodyPart: 胸 / 背 / 腿 / 肩 / 臂 / 腹 / 有氧 / 全身
  - defaultUnit: kg / lbs / 秒 / 分钟 / 米 / 次(自重)
```

### 交互
- 添加动作时，搜索栏实时过滤（Room FTS4 全文搜索）
- 支持用户创建自定义动作（标记 `isBuiltIn=false`）
- 每个动作记录「最近一次」数据（重量×次数），在下次选择该动作时显示为参考
- 每个动作详情页：历史训练趋势图（该动作的容量/最大重量随时间变化）

---

## 3. 训练打卡（Training Session）—— 核心体验

### 3.1 训练前
- **快速开始**：首页点击「+」直接进入空白训练
- **从模板开始**：选择预设模板（推/拉/腿 / 5×5 / 用户自定义模板）
- **从上一次继续**：显示「上周同部位训练」摘要，一键复用动作列表

### 3.2 训练中（实时记录界面）
这是App使用时长最长的页面，设计优先级最高：

**当前训练摘要**
- 顶部固定栏：已训练时长（自动计时，从第一个动作开始）、总容量、已完成组数
- 训练计时器：开始训练时自动启动，支持暂停

**动作卡片（可展开/折叠）**
- 每个动作一张卡片，显示：
  - 动作名称
  - 目标组数/次数（可选，来自模板时预填）
  - 已完成组列表，每组一行：
    - 组序号 | 重量 | 次数 | 该组类型标记（热身/正式/力竭/降负）
  - **「+ 添加组」按钮**：点击后弹出快速输入面板
- 动作卡片内显示该动作的「上次训练」数据（灰色小字，如 "上次: 80kg × 8"）
- 长按动作卡片可「添加备注」（如 "肩前束有不适感"）

**组输入面板（底部弹出）**
- 三个核心字段：重量、次数、组类型
- 组类型：热身组(W) / 正式组(R) / 力竭组(F) / 降负组(D) —— 默认正式组
- 重量输入框默认填充上一组的重量（减少重复输入）
- 次数输入框默认填充上一组的次数
- 支持 kg ↔ lbs 实时切换（全局设置）

**快捷操作**
- 完成一组后自动启动休息计时器（可配置：30s / 60s / 90s / 120s / 180s / 自定义）
- 休息计时器在屏幕顶部以进度条形式展示，振动提醒 + 可选音效提醒
- 支持「跳过休息」按钮

**超级组/巨型组**
- 支持将两个或多个动作标记为「超级组」（Superset）
- 超级组内的动作交替进行，共用一个休息计时器
- UI 上以连接线 + 缩进表示超级组关系

**RPE 评级**
- 每个动作完成后可标记 RPE（1-10）
- RPE 指南始终可查看（点击 RPE 标签弹出说明）：
  - 1-2：非常轻松，可做 10+ 次
  - 3-4：轻松，可做 6-8 次
  - 5-6：中等，可做 4-6 次
  - 7-8：困难，可做 2-4 次
  - 9：非常困难，可做 1 次
  - 10：极限，无法再多做一次
- RPE 仅在训练组标记，热身组不记录 RPE

### 3.3 训练结束
- 点击「完成训练」触发确认：
  - 显示本次训练摘要（时长、总容量、动作数、总组数）
  - 提示为本次训练写一条备注（可选）
  - 底部弹窗询问"是否提交AI审查？"（默认勾选）
- 保存后自动跳转回首页

---

## 4. AI 教练审查系统

### 4.1 审查触发与状态
```
状态机：
  PENDING → QUEUED → REVIEWING → COMPLETED
                                   ├── APPROVED（得分 ≥ 70）
                                   └── NEEDS_ATTENTION（得分 < 70 或检测到风险项）
```
- PENDING：训练已保存，等待用户选择是否送审
- QUEUED：已提交审查请求，等待网络可用（离线队列）
- REVIEWING：正在请求LLM
- COMPLETED：审查完成，显示结果
- FAILED：网络错误或API错误，用户可手动重试

### 4.2 离线策略
- 保存训练后，审查请求存入 Room（`ReviewRequest` 表）
- WorkManager 周期性检查队列（NetworkType.CONNECTED 约束）
- 离线时训练正常保存，审查在恢复网络后自动执行
- 用户可手动触发审查重试

### 4.3 审查结果展示
- 质量分：超大号数字（96sp, Light字重），居中
  - ≥80：默认颜色（表示良好）
  - 60-79：低饱和暖色（表示有改进空间）
  - <60：较深暖色（需要关注）
- 总体评价：引用样式，左侧 2dp 竖线，`#8B9D8B` 强调色
- 分类卡片（可折叠）：
  - ✅ 亮点（Strengths）
  - ⚠️ 需关注（Issues）
  - 💡 建议（Suggestions）
  - 🏷️ 标签（Tags）
- 每项建议可点击展开，进入「对话」视图

### 4.4 审查对话（Review Thread）
- 用户可对每条 AI 建议进行回复：
  - "已采纳"（Resolve）
  - "忽略"（Dismiss）
  - 文字回复（Reply）
- 对话历史存储在本地
- 下次审查时，历史回复作为上下文的一部分发送给LLM（让AI知道用户对上次建议的态度）

### 4.5 审查上下文（发送给LLM的数据）
- 本次训练完整数据
- 最近7天训练摘要（容量、部位分布）
- 最近30天同部位训练趋势（动作选择、容量变化、RPE趋势）
- 用户对上次建议的处理结果
- 用户训练档案

---

## 5. 模板系统

### 5.1 模板来源
- **系统预设**：推/拉/腿 × 3、上肢/下肢 × 2、全身 × 2、5×5 基础力量
- **用户自定义**：从任意历史训练「保存为模板」
- **从模板创建训练**：选择模板后进入训练界面，所有动作预填充

### 5.2 模板内容
- 动作列表（含目标组数/次数/重量/组类型）
- 超级组关系
- 模板名称、标签（如"增力期""减脂期"）
- 预计时长

---

## 6. 历史与分析

### 6.1 日历视图
- 月份选择器（左右滑动切换月份）
- 日历网格：
  - 有训练日：填充色块（颜色深浅 = 该日总容量 / 该月最大日容量）
  - 无训练日：空白或极浅灰色
  - 今日：4dp 描边突出
  - 连续打卡天数超过7天：底部出现细微点状标记
- 点击某日：下方展开当日训练卡片列表

### 6.2 统计卡片（首页 Dashboard）
- **本周摘要**：训练次数 / 总容量 / 总组数
- **本月摘要**：同上 + 相比上月变化百分比
- **连续打卡**：天数（大字）
- **身体数据趋势**：体重变化（最近30天，折线图）

### 6.3 趋势图表
- 使用 Compose Canvas 自绘，保持性冷淡风格
- 图表类型：
  - **容量趋势**：柱状图（按周/月），单色填充
  - **部位分布**：百分比堆叠柱状图（过去30天各部位占比）
  - **体重趋势**：折线图，带 7日移动平均线
  - **1RM 趋势**：折线图（基于 Epley 公式估算）
- 图表交互：点击数据点显示 Tooltip

### 6.4 历史列表
- 默认按月份分组，显示每次训练摘要
- **搜索与筛选**：按部位、动作名称、日期范围筛选
- **排序**：日期（默认）/ 容量 / 时长

---

## 7. 身体数据追踪

### 7.1 身体指标
- 体重（核心指标，支持每日记录）
- 可选：胸围、腰围、臀围、臂围、大腿围
- 体重输入支持快速 ±0.5 步进

### 7.2 趋势展示
- 体重折线图（7日移动平均）
- 与训练容量的叠加视图（双Y轴，关联体重变化与训练负荷）

---

## 8. API Key 与模型管理

同v1，增强：
- **连接测试**：设置页提供「测试连接」按钮，发送最小化请求验证API Key有效性
- **用量估算**：根据历史训练频率估算每月API调用次数和预估费用
- **模型推荐**：内置模型列表（gpt-4o-mini / claude-3-haiku 等性价比选项 + gpt-4o / claude-sonnet 等高精度选项），标注推荐标签

---

## 9. 提醒与通知

- **训练提醒**：可配置每周固定几天、固定时间推送通知
- **审查完成通知**：后台审查完成后推送
- **连续打卡提醒**：若连续N天未训练（可配置），推送提醒
- 通知内容极简：纯文字，无图标，无大段文案

---

## 10. 数据导入/导出与备份

同v1，增强：
- **导出格式**：JSON（完整数据）/ CSV（训练摘要）
- **导入**：支持导入 GymLog JSON 格式（用于换机迁移）
- **自动备份**：可配置每N天自动导出JSON到指定目录
- **WebDAV 备份**：配置 WebDAV 地址后一键备份/恢复
- 所有备份文件不包含 API Key（安全设计）

---

## 11. 桌面小组件（Widget）
- **快速开始**：2×1 小组件，点击直接进入空白训练页
- **连续打卡**：1×1 小组件，显示连续打卡天数和本周训练次数
- **本周摘要**：3×2 小组件，显示本周训练天数/总容量
- Widget 配色跟随系统深色/浅色模式

---

# Data Models（核心实体）

```kotlin
// --- 用户 ---
data class UserProfile(
    val gender: Gender,
    val birthYear: Int,
    val heightCm: Float,
    val yearsOfTraining: TrainingExperience,
    val goal: TrainingGoal,
    val weeklyFrequency: Int
)

// --- 动作库 ---
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,          // UUID
    val name: String,
    val bodyPart: BodyPart,
    val equipment: Equipment,
    val category: ExerciseCategory,
    val defaultUnit: ExerciseUnit,
    val isBuiltIn: Boolean,
    val isHidden: Boolean                 // 用户可隐藏不常用的内置动作
)

// --- 训练模板 ---
@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val tag: String?,                     // 如 "增力期"
    val estimatedDurationMin: Int?,
    val createdAt: Long
)

@Entity(tableName = "template_exercises")
data class TemplateExerciseEntity(
    @PrimaryKey val id: String,
    val templateId: String,
    val exerciseId: String,
    val sortOrder: Int,
    val targetSets: Int?,
    val targetReps: Int?,
    val targetWeightKg: Float?,
    val targetSetType: SetType?,
    val supersetGroupId: String?          // 超级组分组ID
)

// --- 训练记录 ---
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val startTime: Long,
    val endTime: Long?,                   // 完成后填写
    val bodyPart: BodyPart,
    val overallRpe: Int?,                 // 整体RPE
    val note: String?,
    val gymLocation: String?,
    val templateId: String?,              // 若从模板创建
    val status: SessionStatus             // IN_PROGRESS / COMPLETED
)

@Entity(tableName = "session_exercises")
data class SessionExerciseEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val exerciseId: String,
    val sortOrder: Int,
    val supersetGroupId: String?,
    val note: String?,
    val overallRpe: Int?
)

@Entity(tableName = "session_sets")
data class SessionSetEntity(
    @PrimaryKey val id: String,
    val sessionExerciseId: String,
    val setNumber: Int,
    val weightKg: Float?,                 // null 表示自重
    val reps: Int?,
    val durationSec: Int?,                // 有氧/计时动作
    val distanceM: Float?,                // 有氧距离
    val setType: SetType,                 // WARMUP / WORKING / FAILURE / DROPSET
    val rpe: Int?,
    val isCompleted: Boolean
)

// --- AI审查 ---
@Entity(tableName = "review_requests")
data class ReviewRequestEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val status: ReviewStatus,             // PENDING / QUEUED / REVIEWING / COMPLETED / FAILED
    val provider: LLMProvider,
    val modelName: String,
    val requestPrompt: String,            // 实际发送的完整Prompt（用于调试）
    val rawResponse: String?,             // LLM原始返回
    val overallComment: String?,
    val score: Int?,
    val tags: List<String>?,
    val createdAt: Long,
    val completedAt: Long?,
    val errorMessage: String?,
    val retryCount: Int
)

@Entity(tableName = "review_items")
data class ReviewItemEntity(
    @PrimaryKey val id: String,
    val reviewRequestId: String,
    val category: ReviewCategory,         // STRENGTH / ISSUE / SUGGESTION
    val content: String,
    val sortOrder: Int,
    val userAction: UserAction?,          // RESOLVED / DISMISSED / null
    val userReply: String?
)

// --- 身体数据 ---
@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey val id: String,
    val date: Long,
    val weightKg: Float,
    val chestCm: Float?,
    val waistCm: Float?,
    val hipCm: Float?,
    val armCm: Float?,
    val thighCm: Float?
)

// --- 提醒 ---
data class ReminderConfig(
    val enabled: Boolean,
    val daysOfWeek: Set<Int>,             // 1=周一 ... 7=周日
    val timeHour: Int,
    val timeMinute: Int,
    val inactivityReminderDays: Int       // 连续不训练N天后提醒，默认5
)
```

---

# Technical Architecture

## 分层结构（与v1保持一致，做如下调整）

```
app/
├── presentation/
│   ├── theme/              # Color.kt, Type.kt, Shape.kt, Spacing.kt
│   ├── components/          # 原子化组件
│   │   ├── button/          # GymLogButton, GymLogIconButton
│   │   ├── card/            # GymLogCard, GymLogExpandableCard
│   │   ├── input/           # GymLogTextField, WeightInput, RepsInput
│   │   ├── chart/           # 自绘图表组件
│   │   ├── timer/           # RestTimerBar, WorkoutTimer
│   │   └── feedback/        # SkeletonLoading, EmptyState, ErrorState
│   ├── screens/
│   │   ├── onboarding/      # 引导页（3步）
│   │   ├── dashboard/       # 首页
│   │   ├── session/         # 训练记录（核心页面）
│   │   ├── review/          # AI审查详情
│   │   ├── history/         # 历史与分析
│   │   ├── exercise/        # 动作库管理
│   │   ├── body/            # 身体数据
│   │   ├── template/        # 模板管理
│   │   └── settings/        # 设置
│   └── widget/              # App Widget 配置
├── domain/
│   ├── model/               # 领域模型
│   ├── repository/          # 接口定义
│   └── usecase/
├── data/
│   ├── local/
│   │   ├── db/              # Room Database, DAO
│   │   ├── prefs/           # EncryptedSharedPreferences
│   │   └── preset/          # 预置数据（动作库、模板）
│   ├── remote/              # LLM API Client
│   └── repository/
└── di/                      # Hilt Module
```

## 关键架构决策（v2增强）

- **UI**：Jetpack Compose 100%
- **状态管理**：MVI（StateFlow + sealed class 状态表示）
- **数据库**：Room + FTS4（动作搜索）
- **后台任务**：WorkManager（审查队列 + 自动备份 + 通知调度）
- **网络**：OkHttp + Retrofit，每个 Provider 独立 Interceptor（注入不同认证头）
- **本地加密**：Android Keystore + EncryptedSharedPreferences（仅存储 API Key）
- **图表**：Compose Canvas 自绘，Theme 统一管理配色
- **依赖注入**：Hilt
- **前台服务**：训练计时使用 Foreground Service（防止被系统杀死导致计时丢失）
- **Widget**：Glance（Jetpack Compose 风格的 Widget 框架）

---

# LLM Prompt Template（v2 优化）

```kotlin
object ReviewPromptTemplate {
    fun build(
        currentSession: Session,
        history7d: List<SessionSummary>,
        history30dSamePart: List<SessionSummary>,
        previousReviewActions: List<UserActionSummary>,
        userProfile: UserProfile,
        bodyWeightTrend: String?,              // 最近30天体重变化描述
        provider: LLMProvider
    ): String = """
你是一位拥有15年执教经验的力量训练教练（CSCS认证），擅长周期化训练设计和伤病预防。
请审查以下学员的训练记录，给出专业、具体、可执行的建议。

## 学员档案
- 训练年限：${userProfile.trainingExperience}
- 目标：${userProfile.goal}
- 训练频率：${userProfile.weeklyFrequency}天/周
- 身高体重：${userProfile.heightCm}cm / 最新体重见上下文
${if (bodyWeightTrend != null) "- 体重趋势：$bodyWeightTrend" else ""}

## 本次训练
- 日期：${currentSession.date}
- 时长：${currentSession.durationMin}分钟
- 部位：${currentSession.bodyPart}
- 动作与组数：
${currentSession.exercises.joinToString("\n") { ex ->
    "  • ${ex.name}：" + ex.sets.joinToString(" / ") { set ->
        when {
            set.setType == SetType.WARMUP -> "热身 ${set.weightKg}kg×${set.reps}"
            set.setType == SetType.DROPSET -> "降负 ${set.weightKg}kg×${set.reps}"
            set.setType == SetType.FAILURE -> "力竭 ${set.weightKg}kg×${set.reps} (RPE ${set.rpe})"
            else -> "${set.weightKg}kg×${set.reps} (RPE ${set.rpe ?: "-"})"
        }
    }
}}
- 总容量：${currentSession.totalVolumeKg}kg
- 正式组数：${currentSession.workingSetCount}
- 主观感受：${currentSession.overallRpe}/10
- 备注：${currentSession.note ?: "无"}

## 近期训练历史
### 最近7天
${history7d.joinToString("\n") { "- ${it.date} | ${it.bodyPart} | ${it.totalVolumeKg}kg | ${it.workingSetCount}组 | RPE ${it.overallRpe}" }}

### 最近30天同部位（${currentSession.bodyPart}）趋势
${history30dSamePart.joinToString("\n") { "- ${it.date} | ${it.totalVolumeKg}kg | 主项动作: ${it.mainExercise}" }}

## 用户对上次建议的反馈
${
    if (previousReviewActions.isEmpty()) "（首次审查，无历史反馈）"
    else previousReviewActions.joinToString("\n") { "- [${it.action}] ${it.content}" }
}

## 输出要求（严格JSON格式，不要输出markdown代码块标记）
{
  "overall": "总体评价，1-2句话，肯定进步并指出关键问题",
  "strengths": ["本次训练的具体亮点，基于数据"],
  "issues": ["发现的问题，附数据支撑（如：相比上周同部位容量突增XX%，需警惕）"],
  "suggestions": ["下次训练的具体建议，含组数/次数/重量调整方向"],
  "score": 75,
  "tags": ["容量适宜", "动作规范", "需增加频率"]
}

## 审查准则
1. 容量突增（同部位周增幅 > 20%）→ 必须警告并建议回退
2. 连续3次同部位训练无动作变化 → 建议引入变式（不同握距/角度/器械）
3. 复合动作占比过低（< 40%） → 提醒增加复合动作
4. RPE 持续偏高（平均 > 8.5） → 关注恢复不足的可能
5. 训练频率过低（7天内仅1次或更少） → 温和鼓励但不过度施压
6. 建议必须具体到组数/次数/重量区间，禁止空泛意见
7. 语气专业、平等，像教练与学员的对话，禁止说教感
8. 如果数据不足以做出判断，诚实说明而非编造
""".trimIndent()
}
```

---

# UI 页面结构（v2 详细定义）

## 首页（Dashboard）
```
┌────────────────────────────┐
│  本月训练                  │  ← 顶部栏（仅文字，无背景色）
│  12 次                     │  ← 大号数字，Light字重
│                            │
│  ┌──────────────────────┐  │
│  │ 连续打卡  🔥         │  │  ← 小标签，极简
│  │ 8 天                 │  │
│  └──────────────────────┘  │
│                            │
│  ┌──────────────────────┐  │
│  │ 本周训练             │  │  ← 统计卡片组（横向滑动）
│  │ 3次 · 12,800kg · 89组│  │
│  └──────────────────────┘  │
│                            │
│  ┌──────────────────────┐  │
│  │ 最新审查 · 2小时前   │  │  ← 审查状态卡片
│  │ 得分 82 · Approved   │  │     (点击进入详情)
│  │ "容量控制得当..."     │  │
│  └──────────────────────┘  │
│                            │
│  ┌──────────────────────┐  │
│  │ 身体数据             │  │
│  │ 78.5kg  ↓0.3kg(7天)  │  │
│  └──────────────────────┘  │
│                            │
│         [开始训练]         │  ← 底部主按钮，fixed
└────────────────────────────┘
```

## 训练记录页（Session Editor）—— 核心页面
```
┌────────────────────────────┐
│  ⏱ 00:32:15   2,450kg     │  ← 顶栏：计时器 | 实时容量
│                            │
│  ┌─ 杠铃深蹲 ──────────┐  │  ← 动作卡片（折叠状态）
│  │ 上次: 80kg × 8 (12天前)│  │  ← 灰色参考数据
│  │ W  60kg  × 8          │  │  ← 热身组标记 W
│  │ 1  80kg  × 8  RPE 7   │  │  ← 正式组
│  │ 2  80kg  × 8  RPE 7.5 │  │
│  │ 3  80kg  × 7  RPE 8.5 │  │
│  │ [+ 添加组]            │  │
│  └────────────────────────┘  │
│                            │
│  ┌─ 腿举 ──────────────┐  │
│  │ 1  120kg × 12 RPE 6   │  │
│  │ [+ 添加组]            │  │
│  └────────────────────────┘  │
│                            │
│  [+ 添加动作]              │  ← 底部始终可见
│  [完成训练]                │
└────────────────────────────┘
```

### 休息计时器（组间）
```
┌────────────────────────────┐
│ ████████████░░░░  还剩 45s │  ← 顶部进度条，占据状态栏下方
│ 下一组: 杠铃深蹲 第4组     │
│ 上次: 80kg × 7             │
│                            │
│  [跳过]        [+15s]      │
└────────────────────────────┘
```

---

# Extensibility Plan（v2 扩展点）

架构必须预留以下扩展接口（v1中的扩展点全部保留，新增如下）：

## v1 保留
1. **多模态输入**：`MediaAttachment` 接口 → 训练视频上传
2. **社交功能**：`ShareTarget` 接口 → 审查报告导出为图片
3. **可穿戴设备**：`HealthDataSource` 接口 → 心率/睡眠数据
4. **AI 教练升级**：`CoachPersona` 配置 → 不同风格教练
5. **训练计划生成**：`ProgramGenerator` UseCase
6. **营养追踪**：`NutritionLog` 模块

## v2 新增
7. **训练伙伴模式**：`TrainingPartner` 接口 → 局域网实时同步训练数据（两人一起练，共享计时器）
8. **体态分析**：`PoseAnalysis` 接口 → 用设备摄像头实时分析动作姿态（基于 ML Kit Pose Detection）
9. **周期化计划引擎**：`PeriodizationEngine` 接口 → 根据目标自动生成4-12周周期化训练计划（线性周期/波动周期/区块周期）
10. **健身房设备集成**：`GymEquipment` 接口 → 蓝牙连接智能器械（如蓝牙哑铃）自动记录重量和次数
11. **健康数据同步**：`HealthConnect` 接口 → 与 Android Health Connect 双向同步训练和身体数据

---

# 开发优先级（MVP Scope）

## P0 — 必须实现（v2 MVP）
- [ ] 用户引导（Onboarding）
- [ ] 动作库（预置数据 + 搜索）
- [ ] 训练打卡（动作添加、组记录、热身/正式/力竭组、超级组）
- [ ] 休息计时器
- [ ] 训练计时器（Foreground Service）
- [ ] 模板系统（预设 + 自定义）
- [ ] AI 审查（含离线队列）
- [ ] 审查详情 + 对话线程
- [ ] 首页 Dashboard
- [ ] 日历视图 + 历史列表
- [ ] API Key 管理
- [ ] 数据导出 JSON/CSV

## P1 — 重要（v2.1）
- [ ] 身体数据追踪（体重 + 趋势图）
- [ ] 趋势图表（容量、部位分布、1RM）
- [ ] 历史搜索与筛选
- [ ] 桌面小组件
- [ ] 训练提醒通知

## P2 — 增强（v2.2+）
- [ ] WebDAV 备份
- [ ] 体态分析
- [ ] 周期化计划引擎
- [ ] 训练伙伴模式
- [ ] Health Connect 集成

---

# Deliverables（同v1要求）

请输出以下内容：
1. 完整模块依赖图（各层之间的依赖关系）
2. 核心Compose组件代码（主题配置 + 首页Dashboard + 训练记录页 + 审查卡片）
3. Room数据库完整Schema（Entity + DAO + Database + Migration）
4. LLM Repository实现（含多Provider适配、错误处理、重试机制、离线队列）
5. 审查状态机实现（StateFlow + sealed class）
6. 性冷淡配色完整代码（Color.kt, Type.kt, Shape.kt, Spacing.kt）
7. 扩展接口预留代码
8. 训练计时前台服务实现
9. Widget（Glance）基础实现

所有代码使用 Kotlin，遵循 Kotlin Coding Conventions，Compose 使用 Material3 但完全覆盖为自定义设计系统。
