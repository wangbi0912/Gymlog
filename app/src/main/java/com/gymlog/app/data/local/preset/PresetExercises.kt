package com.gymlog.app.data.local.preset

import com.gymlog.app.data.local.entity.ExerciseEntity
import com.gymlog.app.domain.model.*
import java.util.UUID

object PresetExercises {

    fun getAll(): List<ExerciseEntity> = listOf(
        // Chest
        ex("杠铃平板卧推", BodyPart.CHEST, Equipment.BARBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("杠铃上斜卧推", BodyPart.CHEST, Equipment.BARBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("杠铃下斜卧推", BodyPart.CHEST, Equipment.BARBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("哑铃平板卧推", BodyPart.CHEST, Equipment.DUMBBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("哑铃上斜卧推", BodyPart.CHEST, Equipment.DUMBBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("哑铃飞鸟", BodyPart.CHEST, Equipment.DUMBBELL, ExerciseCategory.ISOLATION, ExerciseUnit.KG),
        ex("器械推胸", BodyPart.CHEST, Equipment.MACHINE, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("器械夹胸", BodyPart.CHEST, Equipment.MACHINE, ExerciseCategory.ISOLATION, ExerciseUnit.KG),
        ex("双杠臂屈伸", BodyPart.CHEST, Equipment.BODYWEIGHT, ExerciseCategory.COMPOUND, ExerciseUnit.REPS),
        ex("绳索夹胸", BodyPart.CHEST, Equipment.CABLE, ExerciseCategory.ISOLATION, ExerciseUnit.KG),

        // Back
        ex("引体向上", BodyPart.BACK, Equipment.BODYWEIGHT, ExerciseCategory.COMPOUND, ExerciseUnit.REPS),
        ex("杠铃划船", BodyPart.BACK, Equipment.BARBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("哑铃划船", BodyPart.BACK, Equipment.DUMBBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("高位下拉", BodyPart.BACK, Equipment.CABLE, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("坐姿划船", BodyPart.BACK, Equipment.CABLE, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("硬拉", BodyPart.BACK, Equipment.BARBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("罗马尼亚硬拉", BodyPart.BACK, Equipment.BARBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("直臂下拉", BodyPart.BACK, Equipment.CABLE, ExerciseCategory.ISOLATION, ExerciseUnit.KG),
        ex("面拉", BodyPart.BACK, Equipment.CABLE, ExerciseCategory.ISOLATION, ExerciseUnit.KG),
        ex("T杠划船", BodyPart.BACK, Equipment.BARBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),

        // Legs
        ex("杠铃深蹲", BodyPart.LEGS, Equipment.BARBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("前蹲", BodyPart.LEGS, Equipment.BARBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("腿举", BodyPart.LEGS, Equipment.MACHINE, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("哈克深蹲", BodyPart.LEGS, Equipment.MACHINE, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("保加利亚分腿蹲", BodyPart.LEGS, Equipment.DUMBBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("弓步蹲", BodyPart.LEGS, Equipment.DUMBBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("腿屈伸", BodyPart.LEGS, Equipment.MACHINE, ExerciseCategory.ISOLATION, ExerciseUnit.KG),
        ex("腿弯举", BodyPart.LEGS, Equipment.MACHINE, ExerciseCategory.ISOLATION, ExerciseUnit.KG),
        ex("罗马尼亚硬拉", BodyPart.LEGS, Equipment.BARBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("臀推", BodyPart.LEGS, Equipment.BARBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("提踵", BodyPart.LEGS, Equipment.MACHINE, ExerciseCategory.ISOLATION, ExerciseUnit.KG),

        // Shoulders
        ex("杠铃推举", BodyPart.SHOULDERS, Equipment.BARBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("哑铃推举", BodyPart.SHOULDERS, Equipment.DUMBBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("哑铃侧平举", BodyPart.SHOULDERS, Equipment.DUMBBELL, ExerciseCategory.ISOLATION, ExerciseUnit.KG),
        ex("哑铃前平举", BodyPart.SHOULDERS, Equipment.DUMBBELL, ExerciseCategory.ISOLATION, ExerciseUnit.KG),
        ex("蝴蝶机反向飞鸟", BodyPart.SHOULDERS, Equipment.MACHINE, ExerciseCategory.ISOLATION, ExerciseUnit.KG),
        ex("绳索侧平举", BodyPart.SHOULDERS, Equipment.CABLE, ExerciseCategory.ISOLATION, ExerciseUnit.KG),
        ex("阿诺德推举", BodyPart.SHOULDERS, Equipment.DUMBBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("器械推肩", BodyPart.SHOULDERS, Equipment.MACHINE, ExerciseCategory.COMPOUND, ExerciseUnit.KG),

        // Arms
        ex("杠铃弯举", BodyPart.ARMS, Equipment.BARBELL, ExerciseCategory.ISOLATION, ExerciseUnit.KG),
        ex("哑铃弯举", BodyPart.ARMS, Equipment.DUMBBELL, ExerciseCategory.ISOLATION, ExerciseUnit.KG),
        ex("锤式弯举", BodyPart.ARMS, Equipment.DUMBBELL, ExerciseCategory.ISOLATION, ExerciseUnit.KG),
        ex("绳索弯举", BodyPart.ARMS, Equipment.CABLE, ExerciseCategory.ISOLATION, ExerciseUnit.KG),
        ex("窄距卧推", BodyPart.ARMS, Equipment.BARBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("绳索下压", BodyPart.ARMS, Equipment.CABLE, ExerciseCategory.ISOLATION, ExerciseUnit.KG),
        ex("哑铃臂屈伸", BodyPart.ARMS, Equipment.DUMBBELL, ExerciseCategory.ISOLATION, ExerciseUnit.KG),
        ex("杠铃臂屈伸", BodyPart.ARMS, Equipment.BARBELL, ExerciseCategory.ISOLATION, ExerciseUnit.KG),
        ex("腕弯举", BodyPart.ARMS, Equipment.DUMBBELL, ExerciseCategory.ISOLATION, ExerciseUnit.KG),

        // Abs
        ex("卷腹", BodyPart.ABS, Equipment.BODYWEIGHT, ExerciseCategory.ISOLATION, ExerciseUnit.REPS),
        ex("举腿", BodyPart.ABS, Equipment.BODYWEIGHT, ExerciseCategory.ISOLATION, ExerciseUnit.REPS),
        ex("平板支撑", BodyPart.ABS, Equipment.BODYWEIGHT, ExerciseCategory.BODYWEIGHT, ExerciseUnit.SECONDS),
        ex("俄罗斯转体", BodyPart.ABS, Equipment.BODYWEIGHT, ExerciseCategory.ISOLATION, ExerciseUnit.REPS),
        ex("绳索卷腹", BodyPart.ABS, Equipment.CABLE, ExerciseCategory.ISOLATION, ExerciseUnit.KG),
        ex("仰卧起坐", BodyPart.ABS, Equipment.BODYWEIGHT, ExerciseCategory.ISOLATION, ExerciseUnit.REPS),

        // Cardio
        ex("跑步", BodyPart.CARDIO, Equipment.OTHER, ExerciseCategory.CARDIO, ExerciseUnit.MINUTES),
        ex("动感单车", BodyPart.CARDIO, Equipment.MACHINE, ExerciseCategory.CARDIO, ExerciseUnit.MINUTES),
        ex("椭圆机", BodyPart.CARDIO, Equipment.MACHINE, ExerciseCategory.CARDIO, ExerciseUnit.MINUTES),
        ex("划船机", BodyPart.CARDIO, Equipment.MACHINE, ExerciseCategory.CARDIO, ExerciseUnit.METERS),
        ex("跳绳", BodyPart.CARDIO, Equipment.OTHER, ExerciseCategory.CARDIO, ExerciseUnit.MINUTES),
        ex("游泳", BodyPart.CARDIO, Equipment.OTHER, ExerciseCategory.CARDIO, ExerciseUnit.METERS),
        ex("爬楼机", BodyPart.CARDIO, Equipment.MACHINE, ExerciseCategory.CARDIO, ExerciseUnit.MINUTES),

        // Full Body
        ex("高翻", BodyPart.FULL_BODY, Equipment.BARBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("抓举", BodyPart.FULL_BODY, Equipment.BARBELL, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("农夫行走", BodyPart.FULL_BODY, Equipment.DUMBBELL, ExerciseCategory.COMPOUND, ExerciseUnit.METERS),
        ex("壶铃摇摆", BodyPart.FULL_BODY, Equipment.OTHER, ExerciseCategory.COMPOUND, ExerciseUnit.KG),
        ex("波比跳", BodyPart.FULL_BODY, Equipment.BODYWEIGHT, ExerciseCategory.COMPOUND, ExerciseUnit.REPS),
    )

    private var counter = 0L
    private fun ex(name: String, bodyPart: BodyPart, equipment: Equipment, category: ExerciseCategory, defaultUnit: ExerciseUnit) =
        ExerciseEntity(
            id = "ex_${++counter}",
            name = name,
            bodyPart = bodyPart,
            equipment = equipment,
            category = category,
            defaultUnit = defaultUnit,
            isBuiltIn = true
        )
}
