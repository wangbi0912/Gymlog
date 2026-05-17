package com.gymlog.app.data.local.preset

import com.gymlog.app.data.local.entity.TemplateEntity
import com.gymlog.app.data.local.entity.TemplateExerciseEntity
import com.gymlog.app.domain.model.SetType

object PresetTemplates {

    data class PresetTemplate(
        val template: TemplateEntity,
        val exercises: List<TemplateExerciseEntity>
    )

    fun getAll(): List<PresetTemplate> {
        var tCounter = 0L
        var eCounter = 0L
        fun tId() = "tpl_${++tCounter}"
        fun eId() = "tpe_${++eCounter}"

        return listOf(
            // Push Day
            PresetTemplate(
                template = TemplateEntity(id = tId(), name = "推日", tag = "PPL", estimatedDurationMin = 60),
                exercises = listOf(
                    tpe(eId(), tId(), "ex_1", 0, 4, 8, 60f, SetType.WORKING),
                    tpe(eId(), tId(), "ex_4", 1, 4, 10, 30f, SetType.WORKING),
                    tpe(eId(), tId(), "ex_32", 2, 4, 12, 12f, SetType.WORKING),
                    tpe(eId(), tId(), "ex_8", 3, 3, 15, null, SetType.WORKING),
                    tpe(eId(), tId(), "ex_33", 4, 3, 12, 25f, SetType.WORKING),
                    tpe(eId(), tId(), "ex_44", 5, 3, 15, null, SetType.WORKING),
                )
            ),
            // Pull Day
            PresetTemplate(
                template = TemplateEntity(id = tId(), name = "拉日", tag = "PPL", estimatedDurationMin = 60),
                exercises = listOf(
                    tpe(eId(), tId(), "ex_15", 0, 4, 6, 80f, SetType.WORKING),
                    tpe(eId(), tId(), "ex_11", 1, 4, 8, null, SetType.WORKING),
                    tpe(eId(), tId(), "ex_14", 2, 4, 10, 50f, SetType.WORKING),
                    tpe(eId(), tId(), "ex_43", 3, 4, 10, 20f, SetType.WORKING),
                    tpe(eId(), tId(), "ex_18", 4, 3, 15, 30f, SetType.WORKING),
                    tpe(eId(), tId(), "ex_44", 5, 3, 12, 12f, SetType.WORKING),
                )
            ),
            // Leg Day
            PresetTemplate(
                template = TemplateEntity(id = tId(), name = "腿日", tag = "PPL", estimatedDurationMin = 70),
                exercises = listOf(
                    tpe(eId(), tId(), "ex_21", 0, 4, 8, 80f, SetType.WORKING),
                    tpe(eId(), tId(), "ex_23", 1, 4, 10, 100f, SetType.WORKING),
                    tpe(eId(), tId(), "ex_28", 2, 4, 12, 40f, SetType.WORKING),
                    tpe(eId(), tId(), "ex_27", 3, 4, 12, 30f, SetType.WORKING),
                    tpe(eId(), tId(), "ex_30", 4, 3, 12, 60f, SetType.WORKING),
                    tpe(eId(), tId(), "ex_31", 5, 4, 15, 80f, SetType.WORKING),
                )
            ),
            // Upper Body
            PresetTemplate(
                template = TemplateEntity(id = tId(), name = "上肢训练", tag = "上下肢分化", estimatedDurationMin = 60),
                exercises = listOf(
                    tpe(eId(), tId(), "ex_1", 0, 4, 8, 70f, SetType.WORKING),
                    tpe(eId(), tId(), "ex_11", 1, 3, 8, null, SetType.WORKING),
                    tpe(eId(), tId(), "ex_32", 2, 4, 10, 25f, SetType.WORKING),
                    tpe(eId(), tId(), "ex_14", 3, 4, 10, 50f, SetType.WORKING),
                    tpe(eId(), tId(), "ex_4", 4, 3, 12, 30f, SetType.WORKING),
                    tpe(eId(), tId(), "ex_43", 5, 3, 12, 20f, SetType.WORKING),
                )
            ),
        )
    }

    private fun tpe(id: String, tplId: String, exId: String, order: Int, sets: Int, reps: Int?, weight: Float?, setType: SetType) =
        TemplateExerciseEntity(
            id = id,
            templateId = tplId,
            exerciseId = exId,
            sortOrder = order,
            targetSets = sets,
            targetReps = reps,
            targetWeightKg = weight,
            targetSetType = setType
        )
}
