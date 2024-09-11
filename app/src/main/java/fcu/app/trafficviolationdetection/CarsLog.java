package fcu.app.trafficviolationdetection;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CarsLog extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView item;
    private List<items> carsLog;
    private ItemAdapter adapter;
    private FirebaseAuth mAuth;
    private ImageButton back;
    private RadioGroup radioGroup;
    private RadioButton radioUnreported;
    private RadioButton radioReported;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cars_log);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        item = findViewById(R.id.logs);
        item.setLayoutManager(new LinearLayoutManager(this));

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        back = findViewById(R.id.back);
        radioGroup = findViewById(R.id.radioGroup2);
        radioUnreported = findViewById(R.id.radioUnreported);
        radioReported = findViewById(R.id.radioReported);

        carsLog = new ArrayList<>();
        adapter = new ItemAdapter(this, carsLog);
        item.setAdapter(adapter);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            checkUsersCarLog(userId);
        } else {
            Toast.makeText(this, "用戶沒有登入！", Toast.LENGTH_SHORT).show();
        }

        back.setOnClickListener(v -> finish());

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean filterStatus = checkedId == R.id.radioReported;
            filterLog(filterStatus); // 根据选中的 RadioButton 过滤数据
        });

        // 设置默认选项
        radioUnreported.setChecked(true);
    }

    private void loadCarLogFromFirestore(List<String> reportIds) {
        db.collection("report")
                .whereIn(FieldPath.documentId(), reportIds)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        carsLog.clear(); // 清空旧数据
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("FirestoreData", "Document data: " + document.getData());

                            String carDate = document.getString("Date");
                            String carPlate = document.getString("ViolationsCarElph") + document.getString("ViolationsCarNum");
                            String carRule = document.getString("Violations");
                            String reportId = document.getString("reportId");
                            boolean reportedStatus = document.getBoolean("ReportStatus") != null && document.getBoolean("ReportStatus");

                            carsLog.add(new items(carDate, carPlate, carRule, reportId, reportedStatus));
                        }
                        filterLog(radioGroup.getCheckedRadioButtonId() == R.id.radioReported); // 根据选中的 RadioButton 过滤数据
                    } else {
                        Toast.makeText(CarsLog.this, "Error getting documents: " + task.getException(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkUsersCarLog(String userId) {
        db.collection("users_report")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> usersReportID = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            usersReportID.add(document.getString("report_id"));
                        }
                        if (usersReportID.isEmpty()) {
                            Toast.makeText(this, "沒有違規記錄", Toast.LENGTH_SHORT).show();
                        } else {
                            loadCarLogFromFirestore(usersReportID);
                        }
                    } else {
                        Toast.makeText(this, "發生錯誤", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterLog(boolean reported) {
        List<items> filteredList = new ArrayList<>();
        for (items item : carsLog) {
            if (item.isReportedStatus() == reported) {
                filteredList.add(item);
            }
        }
        adapter.updateData(filteredList); // 更新适配器以显示过滤后的列表
        Log.d("FilterLog", "Filtering with status: " + reported);
        Log.d("ItemCount", "Number of items after filtering: " + filteredList.size());
    }
}
