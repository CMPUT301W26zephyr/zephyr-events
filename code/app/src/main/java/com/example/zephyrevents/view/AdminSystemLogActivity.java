package com.example.zephyrevents.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zephyrevents.R;
import com.example.zephyrevents.model.SystemLog;
import com.example.zephyrevents.repository.RepositoryCallback;
import com.example.zephyrevents.repository.SystemLogRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminSystemLogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_system_log);

        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        TextView title = findViewById(R.id.toolbar_title);
        if (title != null) title.setText("System Logs");
        findViewById(R.id.btn_cancel).setVisibility(View.GONE);
        findViewById(R.id.toolbar_back).setOnClickListener(v -> finish());

        RecyclerView recycler = findViewById(R.id.recycler_logs);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        new SystemLogRepository().getAllLogs(new RepositoryCallback<List<SystemLog>>() {
            @Override
            public void onSuccess(List<SystemLog> result) {
                recycler.setAdapter(new LogAdapter(result));
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminSystemLogActivity.this, "Failed to load logs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class LogAdapter extends RecyclerView.Adapter<LogAdapter.VH> {
        private final List<SystemLog> logs;
        private final SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy HH:mm:ss.SSS", Locale.getDefault());

        public LogAdapter(List<SystemLog> logs) { this.logs = logs; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_system_log, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            SystemLog log = logs.get(position);
            holder.type.setText(log.getActionType());
            holder.desc.setText(log.getDescription());
            holder.actor.setText("Actor: " + log.getActorName());
            holder.time.setText(format.format(new Date(log.getTimestamp())));
        }

        @Override
        public int getItemCount() { return logs.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView type, time, desc, actor;
            VH(View v) {
                super(v);
                type = v.findViewById(R.id.log_type);
                time = v.findViewById(R.id.log_timestamp);
                desc = v.findViewById(R.id.log_desc);
                actor = v.findViewById(R.id.log_actor);
            }
        }
    }
}