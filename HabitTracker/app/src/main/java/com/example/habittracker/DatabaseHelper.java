package com.example.habittracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "habits.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE habits (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL)");
        db.execSQL("CREATE TABLE completions (" +
                "habit_id INTEGER NOT NULL, " +
                "day TEXT NOT NULL, " +               // ISO date, e.g. 2026-11-11
                "PRIMARY KEY (habit_id, day), " +
                "FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing to migrate yet.
    }

    public void addHabit(String name) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        getWritableDatabase().insert("habits", null, v);
    }

    public void deleteHabit(long id) {
        getWritableDatabase().delete("habits", "id = ?", new String[]{String.valueOf(id)});
    }

    public void setCompleted(long habitId, LocalDate day, boolean done) {
        SQLiteDatabase db = getWritableDatabase();
        if (done) {
            ContentValues v = new ContentValues();
            v.put("habit_id", habitId);
            v.put("day", day.toString());
            db.insertWithOnConflict("completions", null, v, SQLiteDatabase.CONFLICT_IGNORE);
        } else {
            db.delete("completions", "habit_id = ? AND day = ?",
                    new String[]{String.valueOf(habitId), day.toString()});
        }
    }

    /** All habits, with "done" status for the selected day and streaks as of today. */
    public List<Habit> getHabits(LocalDate selected) {
        SQLiteDatabase db = getReadableDatabase();
        LocalDate today = LocalDate.now();
        List<Habit> result = new ArrayList<>();
        try (Cursor c = db.rawQuery("SELECT id, name FROM habits ORDER BY id", null)) {
            while (c.moveToNext()) {
                long id = c.getLong(0);
                TreeSet<LocalDate> days = loadDays(db, id);
                result.add(new Habit(id, c.getString(1),
                        days.contains(selected),
                        currentStreak(days, today),
                        bestStreak(days)));
            }
        }
        return result;
    }

    private TreeSet<LocalDate> loadDays(SQLiteDatabase db, long habitId) {
        TreeSet<LocalDate> days = new TreeSet<>();
        try (Cursor c = db.rawQuery("SELECT day FROM completions WHERE habit_id = ?",
                new String[]{String.valueOf(habitId)})) {
            while (c.moveToNext()) {
                days.add(LocalDate.parse(c.getString(0)));
            }
        }
        return days;
    }

    /**
     * Consecutive days ending today. If today isn't logged yet, the streak
     * still counts through yesterday so it doesn't show 0 first thing in the morning.
     */
    static int currentStreak(Set<LocalDate> days, LocalDate today) {
        LocalDate d = days.contains(today) ? today : today.minusDays(1);
        int count = 0;
        while (days.contains(d)) {
            count++;
            d = d.minusDays(1);
        }
        return count;
    }

    static int bestStreak(TreeSet<LocalDate> days) {
        int best = 0, run = 0;
        LocalDate prev = null;
        for (LocalDate d : days) {
            run = (prev != null && prev.plusDays(1).equals(d)) ? run + 1 : 1;
            best = Math.max(best, run);
            prev = d;
        }
        return best;
    }
}
