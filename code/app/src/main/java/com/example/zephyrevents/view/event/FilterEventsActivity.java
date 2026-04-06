package com.example.zephyrevents.view.event;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.zephyrevents.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;

public class FilterEventsActivity extends AppCompatActivity {

    public static final String EXTRA_ANYTIME = "filter_anytime";
    public static final String EXTRA_RANGE_START_MS = "filter_range_start_ms";
    public static final String EXTRA_RANGE_END_MS = "filter_range_end_ms";
    public static final String EXTRA_ONLY_WITH_SPACE = "filter_only_with_space";

    private final Calendar startCal = Calendar.getInstance();
    private final Calendar endCal = Calendar.getInstance();

    private CalendarView calStart, calEnd;
    private TimePicker timeStart, timeEnd;
    private HorizontalScrollView scrollFilterRange;
    private View columnFilterEnd;

    private RadioButton rbAnytime, rbCustom;
    private SwitchMaterial switchOnlyWithSpace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_events);

        // Immersive Edge-to-Edge Mode
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        // Setup Top Bar
        TextView title = findViewById(R.id.toolbar_title);
        if (title != null) title.setText("Filter Events");
        findViewById(R.id.btn_cancel).setVisibility(View.GONE);
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        // Views
        RadioGroup radioDate = findViewById(R.id.radioDate);
        rbAnytime = findViewById(R.id.rbAnytime);
        rbCustom = findViewById(R.id.rbCustom);
        switchOnlyWithSpace = findViewById(R.id.switchOnlyWithSpace);
        LinearLayout customDateSelection = findViewById(R.id.customDateSelection);

        calStart = findViewById(R.id.calendar_filter_start);
        timeStart = findViewById(R.id.time_filter_start);
        calEnd = findViewById(R.id.calendar_filter_end);
        timeEnd = findViewById(R.id.time_filter_end);

        scrollFilterRange = findViewById(R.id.scroll_filter_range);
        columnFilterEnd = findViewById(R.id.column_filter_end);

        timeStart.setIs24HourView(false);
        timeEnd.setIs24HourView(false);

        // Clear Filters Text Action
        TextView clearFilters = findViewById(R.id.btn_clear_filters);
        clearFilters.setOnClickListener(v -> {
            // 1. Reset Radio buttons & Switch
            rbAnytime.setChecked(true);
            switchOnlyWithSpace.setChecked(false);

            // 2. Reset Calendars to Current Date/Time
            long now = System.currentTimeMillis();
            startCal.setTimeInMillis(now);
            startCal.set(Calendar.SECOND, 0);
            endCal.setTimeInMillis(now);
            endCal.set(Calendar.SECOND, 0);

            // Unbind listeners temporarily so the minutes don't overwrite
            timeStart.setOnTimeChangedListener(null);
            timeEnd.setOnTimeChangedListener(null);

            timeStart.setHour(startCal.get(Calendar.HOUR_OF_DAY));
            timeStart.setMinute(startCal.get(Calendar.MINUTE));
            timeEnd.setHour(endCal.get(Calendar.HOUR_OF_DAY));
            timeEnd.setMinute(endCal.get(Calendar.MINUTE));

            calStart.setDate(now, false, true);
            calEnd.setDate(now, false, true);

            attachTimeListener(timeStart, startCal, 1);
            attachTimeListener(timeEnd, endCal, 2);

            // 3. Stage the result so if they press the physical back button, the filters apply
            Intent out = new Intent();
            out.putExtra(EXTRA_ANYTIME, true);
            out.putExtra(EXTRA_RANGE_START_MS, -1L);
            out.putExtra(EXTRA_RANGE_END_MS, -1L);
            out.putExtra(EXTRA_ONLY_WITH_SPACE, false);
            setResult(RESULT_OK, out);

            Toast.makeText(this, "Filters reset", Toast.LENGTH_SHORT).show();
            // NOTE: We do NOT call finish() here so the user remains on the screen!
        });

        // Dynamic Radio Button UI
        radioDate.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isCustom = (checkedId == R.id.rbCustom);
            customDateSelection.setVisibility(isCustom ? View.VISIBLE : View.GONE);
            updateRadioUI(isCustom);
        });

        // Dynamic Switch UI
        switchOnlyWithSpace.setOnCheckedChangeListener((buttonView, isChecked) -> updateSwitchColors());

        // Extract passed Intents to restore previous user choices!
        boolean isAnytime = getIntent().getBooleanExtra(EXTRA_ANYTIME, true);
        long savedStart = getIntent().getLongExtra(EXTRA_RANGE_START_MS, -1L);
        long savedEnd = getIntent().getLongExtra(EXTRA_RANGE_END_MS, -1L);
        boolean savedSpace = getIntent().getBooleanExtra(EXTRA_ONLY_WITH_SPACE, false);

        // Restore Switch
        switchOnlyWithSpace.setChecked(savedSpace);
        updateSwitchColors();

        // Restore Radio Buttons
        if (isAnytime) {
            rbAnytime.setChecked(true);
            updateRadioUI(false);
        } else {
            rbCustom.setChecked(true);
            updateRadioUI(true);
        }

        // Set baseline min dates safely so past days aren't un-clickable if scrolling
        long today = System.currentTimeMillis() - 1000;
        calStart.setMinDate(today);
        calEnd.setMinDate(today);

        // Restore exact Calendar and TimePicker states
        restoreCalendarState(savedStart, savedEnd);

        // Apply Button
        findViewById(R.id.btnApplyFilter).setOnClickListener(v -> {
            Intent out = new Intent();
            boolean anytime = rbAnytime.isChecked();
            out.putExtra(EXTRA_ANYTIME, anytime);

            if (!anytime) {
                if (startCal.getTimeInMillis() > endCal.getTimeInMillis()) {
                    Toast.makeText(this, "Start date must be before End date", Toast.LENGTH_SHORT).show();
                    return;
                }
                out.putExtra(EXTRA_RANGE_START_MS, startCal.getTimeInMillis());
                out.putExtra(EXTRA_RANGE_END_MS, endCal.getTimeInMillis());
            } else {
                out.putExtra(EXTRA_RANGE_START_MS, -1L);
                out.putExtra(EXTRA_RANGE_END_MS, -1L);
            }

            out.putExtra(EXTRA_ONLY_WITH_SPACE, switchOnlyWithSpace.isChecked());
            setResult(RESULT_OK, out);
            finish();
        });
    }

    private void updateRadioUI(boolean isCustomSelected) {
        if (isCustomSelected) {
            rbCustom.setBackgroundResource(R.drawable.bg_button_filled);
            rbCustom.setTextColor(ContextCompat.getColor(this, R.color.white));
            rbAnytime.setBackgroundResource(R.drawable.bg_button_outline);
            rbAnytime.setTextColor(ContextCompat.getColor(this, R.color.primary_red));
        } else {
            rbAnytime.setBackgroundResource(R.drawable.bg_button_filled);
            rbAnytime.setTextColor(ContextCompat.getColor(this, R.color.white));
            rbCustom.setBackgroundResource(R.drawable.bg_button_outline);
            rbCustom.setTextColor(ContextCompat.getColor(this, R.color.primary_red));
        }
    }

    private void updateSwitchColors() {
        if (switchOnlyWithSpace.isChecked()) {
            switchOnlyWithSpace.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_red)));
        } else {
            switchOnlyWithSpace.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
        }
    }

    private void restoreCalendarState(long savedStart, long savedEnd) {
        if (savedStart > 0) {
            startCal.setTimeInMillis(savedStart);
        } else {
            startCal.setTimeInMillis(System.currentTimeMillis());
            startCal.set(Calendar.SECOND, 0);
        }

        if (savedEnd > 0) {
            endCal.setTimeInMillis(savedEnd);
        } else {
            endCal.setTimeInMillis(System.currentTimeMillis());
            endCal.set(Calendar.SECOND, 0);
        }

        setupDateAndTime(calStart, timeStart, startCal, 1);
        setupDateAndTime(calEnd, timeEnd, endCal, 2);
    }

    private void setupDateAndTime(CalendarView calView, TimePicker timePicker, Calendar tracker, int type) {
        // Detach listener before setting exact minutes so it doesn't instantly overwrite
        timePicker.setOnTimeChangedListener(null);
        timePicker.setHour(tracker.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(tracker.get(Calendar.MINUTE));

        calView.setDate(tracker.getTimeInMillis(), false, true);

        calView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            tracker.set(Calendar.YEAR, year);
            tracker.set(Calendar.MONTH, month);
            tracker.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            long selectedMillis = tracker.getTimeInMillis();

            if (type == 1) { // Start Calendar
                if (endCal.getTimeInMillis() < selectedMillis) {
                    endCal.setTimeInMillis(selectedMillis);
                    calEnd.setDate(selectedMillis, false, true);

                    timeEnd.setOnTimeChangedListener(null);
                    timeEnd.setHour(tracker.get(Calendar.HOUR_OF_DAY));
                    timeEnd.setMinute(tracker.get(Calendar.MINUTE));
                    attachTimeListener(timeEnd, endCal, 2);
                }

                if (scrollFilterRange != null && columnFilterEnd != null) {
                    scrollFilterRange.postDelayed(() ->
                            scrollFilterRange.smoothScrollTo(columnFilterEnd.getLeft(), 0), 300);
                }

            } else if (type == 2) { // End Calendar
                if (startCal.getTimeInMillis() > selectedMillis) {
                    startCal.setTimeInMillis(selectedMillis);
                    calStart.setDate(selectedMillis, false, true);

                    timeStart.setOnTimeChangedListener(null);
                    timeStart.setHour(tracker.get(Calendar.HOUR_OF_DAY));
                    timeStart.setMinute(tracker.get(Calendar.MINUTE));
                    attachTimeListener(timeStart, startCal, 1);
                }
            }
        });

        attachTimeListener(timePicker, tracker, type);
    }

    private void attachTimeListener(TimePicker timePicker, Calendar tracker, int type) {
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            tracker.set(Calendar.HOUR_OF_DAY, hourOfDay);
            tracker.set(Calendar.MINUTE, minute);
        });
    }
}