package com.example.habittracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class HabitAdapter extends ArrayAdapter<Habit> {

    public HabitAdapter(Context context) {
        super(context, 0);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_habit, parent, false);
        }
        Habit h = getItem(position);
        CheckBox check = convertView.findViewById(R.id.habitCheck);
        TextView name = convertView.findViewById(R.id.habitName);
        TextView streak = convertView.findViewById(R.id.streakText);

        check.setChecked(h.doneOnSelectedDay);
        name.setText(h.name);
        String days = h.currentStreak == 1 ? "1 day" : h.currentStreak + " days";
        streak.setText("\uD83D\uDD25 " + days + "\nbest: " + h.bestStreak);
        return convertView;
    }
}
