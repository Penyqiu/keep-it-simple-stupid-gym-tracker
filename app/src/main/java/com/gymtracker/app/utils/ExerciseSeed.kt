package com.gymtracker.app.utils

import com.gymtracker.app.data.db.entity.ExerciseEntity

object ExerciseSeed {
    val exercises = listOf(
        ExerciseEntity(name = "Bench Press", muscleGroup = "Chest"),
        ExerciseEntity(name = "Incline Bench Press", muscleGroup = "Chest"),
        ExerciseEntity(name = "Decline Bench Press", muscleGroup = "Chest"),
        ExerciseEntity(name = "Dumbbell Fly", muscleGroup = "Chest"),
        ExerciseEntity(name = "Push-Up", muscleGroup = "Chest"),
        ExerciseEntity(name = "Cable Crossover", muscleGroup = "Chest"),

        ExerciseEntity(name = "Pull-Up", muscleGroup = "Back"),
        ExerciseEntity(name = "Barbell Row", muscleGroup = "Back"),
        ExerciseEntity(name = "Dumbbell Row", muscleGroup = "Back"),
        ExerciseEntity(name = "Lat Pulldown", muscleGroup = "Back"),
        ExerciseEntity(name = "Seated Cable Row", muscleGroup = "Back"),
        ExerciseEntity(name = "Deadlift", muscleGroup = "Back"),
        ExerciseEntity(name = "Hyperextension", muscleGroup = "Back"),

        ExerciseEntity(name = "Overhead Press", muscleGroup = "Shoulders"),
        ExerciseEntity(name = "Dumbbell Lateral Raise", muscleGroup = "Shoulders"),
        ExerciseEntity(name = "Front Raise", muscleGroup = "Shoulders"),
        ExerciseEntity(name = "Rear Delt Fly", muscleGroup = "Shoulders"),
        ExerciseEntity(name = "Arnold Press", muscleGroup = "Shoulders"),
        ExerciseEntity(name = "Upright Row", muscleGroup = "Shoulders"),

        ExerciseEntity(name = "Barbell Curl", muscleGroup = "Biceps"),
        ExerciseEntity(name = "Dumbbell Curl", muscleGroup = "Biceps"),
        ExerciseEntity(name = "Hammer Curl", muscleGroup = "Biceps"),
        ExerciseEntity(name = "Concentration Curl", muscleGroup = "Biceps"),
        ExerciseEntity(name = "Cable Curl", muscleGroup = "Biceps"),

        ExerciseEntity(name = "Tricep Dip", muscleGroup = "Triceps"),
        ExerciseEntity(name = "Tricep Pushdown", muscleGroup = "Triceps"),
        ExerciseEntity(name = "Overhead Tricep Extension", muscleGroup = "Triceps"),
        ExerciseEntity(name = "Skull Crusher", muscleGroup = "Triceps"),
        ExerciseEntity(name = "Close-Grip Bench Press", muscleGroup = "Triceps"),

        ExerciseEntity(name = "Squat", muscleGroup = "Legs"),
        ExerciseEntity(name = "Leg Press", muscleGroup = "Legs"),
        ExerciseEntity(name = "Lunges", muscleGroup = "Legs"),
        ExerciseEntity(name = "Leg Extension", muscleGroup = "Legs"),
        ExerciseEntity(name = "Leg Curl", muscleGroup = "Legs"),
        ExerciseEntity(name = "Romanian Deadlift", muscleGroup = "Legs"),
        ExerciseEntity(name = "Bulgarian Split Squat", muscleGroup = "Legs"),
        ExerciseEntity(name = "Calf Raise", muscleGroup = "Legs"),
        ExerciseEntity(name = "Hip Thrust", muscleGroup = "Legs"),
        ExerciseEntity(name = "Hack Squat", muscleGroup = "Legs"),

        ExerciseEntity(name = "Plank", muscleGroup = "Core"),
        ExerciseEntity(name = "Crunch", muscleGroup = "Core"),
        ExerciseEntity(name = "Russian Twist", muscleGroup = "Core"),
        ExerciseEntity(name = "Leg Raise", muscleGroup = "Core"),
        ExerciseEntity(name = "Ab Wheel Rollout", muscleGroup = "Core"),
        ExerciseEntity(name = "Cable Crunch", muscleGroup = "Core"),

        ExerciseEntity(name = "Barbell Shrug", muscleGroup = "Traps"),
        ExerciseEntity(name = "Dumbbell Shrug", muscleGroup = "Traps"),

        ExerciseEntity(name = "Wrist Curl", muscleGroup = "Forearms"),
        ExerciseEntity(name = "Reverse Wrist Curl", muscleGroup = "Forearms"),

        ExerciseEntity(name = "Running", muscleGroup = "Cardio"),
        ExerciseEntity(name = "Cycling", muscleGroup = "Cardio"),
        ExerciseEntity(name = "Jump Rope", muscleGroup = "Cardio")
    )
}
