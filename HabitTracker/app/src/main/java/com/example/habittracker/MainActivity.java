package com.example.habittracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE, MMM d, yyyy");

    private DatabaseHelper db;
    private HabitAdapter adapter;
    private TextView dateText;
    private Button nextButton;
    private LocalDate selectedDate = LocalDate.now();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        adapter = new HabitAdapter(this);

        dateText = findViewById(R.id.dateText);
        nextButton = findViewById(R.id.nextButton);
        Button prevButton = findViewById(R.id.prevButton);
        Button addButton = findViewById(R.id.addButton);
        ListView list = findViewById(R.id.habitList);

        list.setAdapter(adapter);
        list.setEmptyView(findViewById(R.id.emptyText));

        // Tap a habit to toggle it for the selected day
        list.setOnItemClickListener((parent, view, position, id) -> {
            Habit h = adapter.getItem(position);
            db.setCompleted(h.id, selectedDate, !h.doneOnSelectedDay);
            refresh();
        });

        // Long-press a habit to delete it
        list.setOnItemLongClickListener((parent, view, position, id) -> {
            Habit h = adapter.getItem(position);
            new AlertDialog.Builder(this)
                    .setTitle("Delete \"" + h.name + "\"?")
                    .setMessage("This also deletes its history.")
                    .setPositiveButton("Delete", (d, w) -> {
                        db.deleteHabit(h.id);
                        refresh();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

        prevButton.setOnClickListener(v -> {
            selectedDate = selectedDate.minusDays(1);
            refresh();
        });
        nextButton.setOnClickListener(v -> {
            selectedDate = selectedDate.plusDays(1);
            refresh();
        });
        dateText.setOnClickListener(v -> pickDate());
        addButton.setOnClickListener(v -> showAddDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        List<Habit> habits = db.getHabits(selectedDate);
        adapter.clear();
        adapter.addAll(habits);

        LocalDate today = LocalDate.now();
        String label = DATE_FORMAT.format(selectedDate);
        if (selectedDate.equals(today)) label = "Today \u2022 " + label;
        dateText.setText(label);
        nextButton.setEnabled(selectedDate.isBefore(today));
    }

    private void pickDate() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    selectedDate = LocalDate.of(year, month + 1, day);
                    refresh();
                },
                selectedDate.getYear(),
                selectedDate.getMonthValue() - 1,
                selectedDate.getDayOfMonth());
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis()); // no future dates
        dialog.show();
    }

    private void showAddDialog() {
        EditText input = new EditText(this);
        input.setHint("e.g. Read 20 minutes");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setSingleLine(true);

        new AlertDialog.Builder(this)
                .setTitle("New habit")
                .setView(input)
                .setPositiveButton("Add", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        db.addHabit(name);
                        refresh();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
