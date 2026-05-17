package com.gymlog.app.data.remote

import com.gymlog.app.domain.model.*

object ReviewPromptTemplate {
    fun build(
        currentSession: TrainingSession,
        history7d: List<SessionSummary>,
        history30dSamePart: List<SessionSummary>,
        previousReviewActions: List<Pair<String, String>>,
        userProfile: UserProfile,
        bodyWeightTrend: String? = null
    ): String = """
你是一位拥有15年执教经验的力量训练教练（CSCS认证），擅长周期化训练设计和伤病预防。
请审查以下学员的训练记录，给出专业、具体、可执行的建议。

## 学员档案
- 训练年限：${userProfile.trainingExperience.label}
- 目标：${userProfile.goal.label}
- 训练频率：${userProfile.weeklyFrequency}天/周
- 身高：${userProfile.heightCm}cm
${if (bodyWeightTrend != null) "- 体重趋势：$bodyWeightTrend" else ""}

## 本次训练
- 日期：${formatEpoch(currentSession.startTime)}
- 时长：${currentSession.durationMin}分钟
- 部位：${currentSession.bodyPart.label}
- 动作与组数：
${currentSession.exercises.joinToString("\n") { ex ->
    "  - ${ex.exerciseName}：" + ex.sets.joinToString(" / ") { set ->
        when {
            set.setType == SetType.WARMUP -> "热身 ${set.weightKg}kg × ${set.reps}"
            set.setType == SetType.DROPSET -> "降负 ${set.weightKg}kg × ${set.reps}"
            set.setType == SetType.FAILURE -> "力竭 ${set.weightKg}kg × ${set.reps} (RPE ${set.rpe})"
            else -> "${set.weightKg}kg × ${set.reps} (RPE ${set.rpe ?: "-"})"
        }
    }
}}
- 总容量：${"%.0f".format(currentSession.totalVolumeKg)}kg
- 正式组数：${currentSession.workingSetCount}
- 主观感受：${currentSession.overallRpe ?: "-"}/10
- 备注：${currentSession.note ?: "无"}

## 近期训练历史
### 最近7天
${if (history7d.isEmpty()) "（无历史数据）" else history7d.joinToString("\n") {
    "- ${formatEpoch(it.date)} | ${it.bodyPart.label} | ${"%.0f".format(it.totalVolumeKg)}kg | ${it.workingSetCount}组 | RPE ${it.overallRpe ?: "-"}"
}}

### 最近30天同部位（${currentSession.bodyPart.label}）趋势
${if (history30dSamePart.isEmpty()) "（无历史数据）" else history30dSamePart.joinToString("\n") {
    "- ${formatEpoch(it.date)} | ${"%.0f".format(it.totalVolumeKg)}kg | 主项: ${it.mainExercise}"
}}

## 用户对上次建议的反馈
${
    if (previousReviewActions.isEmpty()) "（首次审查，无历史反馈）"
    else previousReviewActions.joinToString("\n") { "- [${it.first}] ${it.second}" }
}

## 输出要求（严格JSON格式，不含markdown代码块标记）
{
  "overall": "总体评价，1-2句话，肯定进步并指出关键问题",
  "strengths": ["本次训练的具体亮点，基于数据"],
  "issues": ["发现的问题，附数据支撑"],
  "suggestions": ["下次训练的具体建议，含组数/次数/重量调整方向"],
  "score": 75,
  "tags": ["容量适宜", "动作规范"]
}

## 审查准则
1. 容量突增（同部位周增幅 > 20%）必须警告并建议回退
2. 连续3次同部位训练无动作变化 → 建议引入变式
3. 复合动作占比过低（< 40%） → 提醒增加复合动作
4. RPE 持续偏高（平均 > 8.5） → 关注恢复不足的可能
5. 训练频率过低（7天内仅1次或更少） → 温和鼓励但不过度施压
6. 建议必须具体到组数/次数/重量区间，禁止空泛意见
7. 语气专业、平等，像教练与学员的对话，禁止说教感
8. 如果数据不足以做出判断，诚实说明而非编造
""".trimIndent()

    private fun formatEpoch(epoch: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(epoch))
    }
}
