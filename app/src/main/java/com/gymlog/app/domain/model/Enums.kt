package com.gymlog.app.domain.model

enum class Gender { MALE, FEMALE, OTHER }

enum class TrainingExperience(val label: String) {
    LESS_THAN_6M("<6个月"),
    SIX_M_TO_1Y("6个月-1年"),
    ONE_TO_2Y("1-2年"),
    TWO_TO_5Y("2-5年"),
    OVER_5Y("5年+")
}

enum class TrainingGoal(val label: String) {
    HYPERTROPHY("增肌"),
    STRENGTH("增力"),
    FAT_LOSS("减脂保留肌肉"),
    ENDURANCE("耐力"),
    GENERAL("综合")
}

enum class BodyPart(val label: String) {
    CHEST("胸"),
    BACK("背"),
    LEGS("腿"),
    SHOULDERS("肩"),
    ARMS("臂"),
    ABS("腹"),
    CARDIO("有氧"),
    FULL_BODY("全身")
}

enum class Equipment(val label: String) {
    BARBELL("杠铃"),
    DUMBBELL("哑铃"),
    CABLE("绳索"),
    MACHINE("器械"),
    BODYWEIGHT("自重"),
    OTHER("其他")
}

enum class ExerciseCategory(val label: String) {
    COMPOUND("复合动作"),
    ISOLATION("孤立动作"),
    BODYWEIGHT("自重"),
    CARDIO("有氧"),
    FLEXIBILITY("柔韧")
}

enum class ExerciseUnit(val label: String) {
    KG("kg"),
    LBS("lbs"),
    SECONDS("秒"),
    MINUTES("分钟"),
    METERS("米"),
    REPS("次")
}

enum class SetType(val label: String, val shortLabel: String) {
    WARMUP("热身组", "W"),
    WORKING("正式组", "R"),
    FAILURE("力竭组", "F"),
    DROPSET("降负组", "D")
}

enum class SessionStatus { IN_PROGRESS, COMPLETED }

enum class ReviewStatus { PENDING, QUEUED, REVIEWING, COMPLETED, FAILED }

enum class ReviewCategory { STRENGTH, ISSUE, SUGGESTION }

enum class UserAction { RESOLVED, DISMISSED }

enum class LLMProvider(val label: String, val baseUrl: String) {
    OPENAI("OpenAI", "https://api.openai.com"),
    ANTHROPIC("Anthropic", "https://api.anthropic.com"),
    DEEPSEEK("DeepSeek", "https://api.deepseek.com"),
    GEMINI("Gemini", "https://generativelanguage.googleapis.com")
}
