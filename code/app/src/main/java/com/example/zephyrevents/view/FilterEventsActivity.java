package com.example.zephyrevents.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CalendarView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.widget.NestedScrollView;
import com.example.zephyrevents.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FilterEventsActivity extends AppCompatActivity {

    public static final String EXTRA_ANYTIME = "filter_anytime";
    public static final String EXTRA_RANGE_START_MS = "filter_range_start_ms";
    public static final String EXTRA_RANGE_END_MS = "filter_range_end_ms";
    public static final String EXTRA_ONLY_WITH_SPACE = "filter_only_with_space";

    private static final int PICK_START = 0;
    private static final int PICK_END = 1;

    private long customStartMs = -1L;
    private long customEndMs = -1L;

    private int pickMode = PICK_START;
    private int selYear;
    private int selMonth;
    private int selDay;

    private CalendarView calendarAvailability;
    private TimePicker timePickerAvailability;
    private EditText etStartDate;
    private EditText etEndDate;

    private final SimpleDateFormat dfDisplay =
            new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_events);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        calendarAvailability = findViewById(R.id.calendarAvailability);
        timePickerAvailability = findViewById(R.id.timePickerAvailability);
        NestedScrollView scrollFilter = findViewById(R.id.scrollFilterContent);

        timePickerAvailability.setIs24HourView(false);

        View.OnTouchListener keepScrollForWheels = (v, event) -> {
            int a = event.getActionMasked();
            if (a == MotionEvent.ACTION_DOWN) {
                scrollFilter.requestDisallowInterceptTouchEvent(true);
            } else if (a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_CANCEL) {
                scrollFilter.requestDisallowInterceptTouchEvent(false);
            }
            return false;
        };
        timePickerAvailability.setOnTouchListener(keepScrollForWheels);

        long initial = System.currentTimeMillis();
        syncSelectionFromMillis(initial);
        calendarAvailability.setDate(initial, false, true);

        etStartDate.setOnClickListener(v -> {
            pickMode = PICK_START;
            long ref = customStartMs >= 0 ? customStartMs : calendarAvailability.getDate();
            syncSelectionFromMillis(ref);
            calendarAvailability.setDate(ref, true, true);
        });
        etEndDate.setOnClickListener(v -> {
            pickMode = PICK_END;
            long ref = customEndMs >= 0 ? customEndMs : calendarAvailability.getDate();
            syncSelectionFromMillis(ref);
            calendarAvailability.setDate(ref, true, true);
        });

        calendarAvailability.post(() -> {
            calendarAvailability.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                selYear = year;
                selMonth = month;
                selDay = dayOfMonth;
                applyActiveField();
            });
            timePickerAvailability.setOnTimeChangedListener((view, hourOfDay, minute) -> applyActiveField());
        });

        View backBtn = findViewById(R.id.btnBack);
        backBtn.setOnClickListener(v -> finish());
        backBtn.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                finish();
            }
            return true;
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        RadioGroup radioDate = findViewById(R.id.radioDate);
        LinearLayout customDateSelection = findViewById(R.id.customDateSelection);
        radioDate.setOnCheckedChangeListener((group, checkedId) ->
                customDateSelection.setVisibility(checkedId == R.id.rbCustom ? View.VISIBLE : View.GONE));

        findViewById(R.id.btnApplyFilter).setOnClickListener(v -> {
            Intent out = new Intent();
            CompoundButton rbAnytime = findViewById(R.id.rbAnytime);
            boolean anytime = rbAnytime != null && rbAnytime.isChecked();
            out.putExtra(EXTRA_ANYTIME, anytime);

            if (!anytime) {
                if (customStartMs < 0 || customEndMs < 0 || customStartMs > customEndMs) {
                    android.widget.Toast.makeText(this, "Pick a valid start and end date", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                out.putExtra(EXTRA_RANGE_START_MS, customStartMs);
                out.putExtra(EXTRA_RANGE_END_MS, customEndMs);
            } else {
                out.putExtra(EXTRA_RANGE_START_MS, -1L);
                out.putExtra(EXTRA_RANGE_END_MS, -1L);
            }
            CompoundButton spaceSwitch = findViewById(R.id.switchOnlyWithSpace);
            out.putExtra(EXTRA_ONLY_WITH_SPACE, spaceSwitch != null && spaceSwitch.isChecked());
            setResult(RESULT_OK, out);
            finish();
        });

        findViewById(R.id.btnClearFilter).setOnClickListener(v -> {
            customStartMs = -1L;
            customEndMs = -1L;
            etStartDate.setText("");
            etEndDate.setText("");
            long now = System.currentTimeMillis();
            syncSelectionFromMillis(now);
            calendarAvailability.setDate(now, false, true);

            Intent out = new Intent();
            out.putExtra(EXTRA_ANYTIME, true);
            out.putExtra(EXTRA_RANGE_START_MS, -1L);
            out.putExtra(EXTRA_RANGE_END_MS, -1L);
            out.putExtra(EXTRA_ONLY_WITH_SPACE, false);
            setResult(RESULT_OK, out);
            finish();
        });
    }

    private void syncSelectionFromMillis(long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        selYear = c.get(Calendar.YEAR);
        selMonth = c.get(Calendar.MONTH);
        selDay = c.get(Calendar.DAY_OF_MONTH);
        timePickerAvailability.setHour(c.get(Calendar.HOUR_OF_DAY));
        timePickerAvailability.setMinute(c.get(Calendar.MINUTE));
    }

    private void applyActiveField() {
        Calendar c = Calendar.getInstance();
        c.set(selYear, selMonth, selDay, timePickerAvailability.getHour(), timePickerAvailability.getMinute(), 0);
        c.set(Calendar.MILLISECOND, 0);
        long ms = c.getTimeInMillis();
        if (pickMode == PICK_START) {
            customStartMs = ms;
            etStartDate.setText(dfDisplay.format(new Date(ms)));
        } else {
            customEndMs = ms;
            etEndDate.setText(dfDisplay.format(new Date(ms)));
        }
    }
}
