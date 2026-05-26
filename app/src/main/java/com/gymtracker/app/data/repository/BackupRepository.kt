package com.gymtracker.app.data.repository

import android.content.Context
import android.net.Uri
import com.gymtracker.app.data.db.dao.BodyWeightDao
import com.gymtracker.app.data.db.dao.ExerciseDao
import com.gymtracker.app.data.db.dao.SessionDao
import com.gymtracker.app.data.db.dao.WorkoutPlanDao
import com.gymtracker.app.data.db.entity.*
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val sessionDao: SessionDao,
    private val workoutPlanDao: WorkoutPlanDao,
    private val exerciseDao: ExerciseDao,
    private val bodyWeightDao: BodyWeightDao
) {
    suspend fun exportToJson(context: Context, uri: Uri): Boolean {
        return try {
            val root = JSONObject()
            root.put("version", 1)
            root.put("exportedAt", System.currentTimeMillis())

            root.put("customExercises", JSONArray().also { arr ->
                exerciseDao.getCustomExercisesOnce().forEach { e ->
                    arr.put(JSONObject().apply {
                        put("id", e.id); put("name", e.name); put("muscleGroup", e.muscleGroup)
                    })
                }
            })

            root.put("plans", JSONArray().also { arr ->
                workoutPlanDao.getAllPlansOnce().forEach { p ->
                    arr.put(JSONObject().apply {
                        put("id", p.id); put("name", p.name)
                        put("createdAt", p.createdAt); put("daysOfWeek", p.daysOfWeek)
                    })
                }
            })

            root.put("planExercises", JSONArray().also { arr ->
                workoutPlanDao.getAllPlanExercises().forEach { pe ->
                    arr.put(JSONObject().apply {
                        put("id", pe.id); put("planId", pe.planId)
                        put("exerciseId", pe.exerciseId); put("orderIndex", pe.orderIndex)
                    })
                }
            })

            root.put("sessions", JSONArray().also { arr ->
                sessionDao.getAllSessionsOnce().forEach { s ->
                    arr.put(JSONObject().apply {
                        put("id", s.id)
                        put("planId", s.planId ?: JSONObject.NULL)
                        put("planName", s.planName ?: JSONObject.NULL)
                        put("startedAt", s.startedAt)
                        put("finishedAt", s.finishedAt ?: JSONObject.NULL)
                        put("notes", s.notes)
                    })
                }
            })

            root.put("sets", JSONArray().also { arr ->
                sessionDao.getAllSetsOnce().forEach { ws ->
                    arr.put(JSONObject().apply {
                        put("id", ws.id); put("sessionId", ws.sessionId)
                        put("exerciseId", ws.exerciseId); put("exerciseName", ws.exerciseName)
                        put("setNumber", ws.setNumber); put("weight", ws.weight)
                        put("reps", ws.reps); put("isCompleted", ws.isCompleted)
                        put("timestamp", ws.timestamp)
                    })
                }
            })

            root.put("bodyWeights", JSONArray().also { arr ->
                bodyWeightDao.getAllEntriesOnce().forEach { bw ->
                    arr.put(JSONObject().apply {
                        put("id", bw.id); put("weight", bw.weight); put("recordedAt", bw.recordedAt)
                    })
                }
            })

            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(root.toString(2)) }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun importFromJson(context: Context, uri: Uri): Boolean {
        return try {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                ?: return false
            val root = JSONObject(json)

            sessionDao.deleteAllSessions()
            workoutPlanDao.deleteAllPlans()
            exerciseDao.deleteAllCustomExercises()
            bodyWeightDao.deleteAllEntries()

            root.optJSONArray("customExercises")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    exerciseDao.insertForRestore(
                        ExerciseEntity(id = o.getLong("id"), name = o.getString("name"),
                            muscleGroup = o.getString("muscleGroup"), isCustom = true)
                    )
                }
            }

            root.optJSONArray("plans")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    workoutPlanDao.insertPlanForRestore(
                        WorkoutPlanEntity(id = o.getLong("id"), name = o.getString("name"),
                            createdAt = o.getLong("createdAt"), daysOfWeek = o.optString("daysOfWeek", ""))
                    )
                }
            }

            root.optJSONArray("planExercises")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    workoutPlanDao.insertPlanExerciseForRestore(
                        PlanExerciseEntity(id = o.getLong("id"), planId = o.getLong("planId"),
                            exerciseId = o.getLong("exerciseId"), orderIndex = o.getInt("orderIndex"))
                    )
                }
            }

            root.optJSONArray("sessions")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    sessionDao.insertSessionForRestore(
                        WorkoutSessionEntity(
                            id = o.getLong("id"),
                            planId = if (o.isNull("planId")) null else o.getLong("planId"),
                            planName = if (o.isNull("planName")) null else o.getString("planName"),
                            startedAt = o.getLong("startedAt"),
                            finishedAt = if (o.isNull("finishedAt")) null else o.getLong("finishedAt"),
                            notes = o.optString("notes", "")
                        )
                    )
                }
            }

            root.optJSONArray("sets")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    sessionDao.insertSetForRestore(
                        WorkoutSetEntity(
                            id = o.getLong("id"), sessionId = o.getLong("sessionId"),
                            exerciseId = o.getLong("exerciseId"), exerciseName = o.getString("exerciseName"),
                            setNumber = o.getInt("setNumber"), weight = o.getDouble("weight"),
                            reps = o.getInt("reps"), isCompleted = o.getBoolean("isCompleted"),
                            timestamp = o.getLong("timestamp")
                        )
                    )
                }
            }

            root.optJSONArray("bodyWeights")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    bodyWeightDao.insertForRestore(
                        BodyWeightEntity(id = o.getLong("id"), weight = o.getDouble("weight"),
                            recordedAt = o.getLong("recordedAt"))
                    )
                }
            }

            true
        } catch (e: Exception) {
            false
        }
    }
}
