package com.example.habittracker;

public class Habit {
    public final long id;
    public final String name;
    public final boolean doneOnSelectedDay;
    public final int currentStreak;
    public final int bestStreak;

    public Habit(long id, String name, boolean doneOnSelectedDay, int currentStreak, int bestStreak) {
        this.id = id;
        this.name = name;
        this.doneOnSelectedDay = doneOnSelectedDay;
        this.currentStreak = currentStreak;
        this.bestStreak = bestStreak;
    }
}
