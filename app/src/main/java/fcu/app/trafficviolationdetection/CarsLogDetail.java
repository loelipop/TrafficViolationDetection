package fcu.app.trafficviolationdetection;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;

public class CarsLogDetail extends AppCompatActivity {

    private ImageButton backbutton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private TextView carPlate;
    private TextView time;
    private TextView location;
    private TextView law;
    private ImageView photo;
    private Button report;
    private Button delete; // Declare delete button
    private String reportId; // Variable for reportId
    private String photoPath;
    private boolean reportStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cars_log_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backbutton = findViewById(R.id.back);
        carPlate = findViewById(R.id.carPlate);
        time = findViewById(R.id.time);
        location = findViewById(R.id.location);
        law = findViewById(R.id.law);
        photo = findViewById(R.id.photo);
        report = findViewById(R.id.EditInfo);
        delete = findViewById(R.id.DeleteInfo); // Initialize delete button

        backbutton.setOnClickListener(v -> finish());
        report.setOnClickListener(v -> {
            updateReportStatus(reportId, true);
            Intent intent = new Intent(CarsLogDetail.this, ReportWebsite.class);
            startActivity(intent);
        });

        delete.setOnClickListener(v -> {
            if (reportId != null) {
                deletePhotoFromStorage(photoPath);
                deleteSubCollection(reportId);  // 刪除報告的子集合
            } else {
                Toast.makeText(this, "無法找到報告ID", Toast.LENGTH_SHORT).show();
            }
        });

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Get reportId
        reportId = getIntent().getStringExtra("reportId");
        Log.d("CarsLogDetail", "Received reportId: " + reportId); // Check reportId

        if (reportId != null) {
            loadCarLogFromFirestore(reportId); // Load data using reportId
        } else {
            Toast.makeText(this, "Report ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCarLogFromFirestore(String reportId) {
        db.collection("report")
                .document(reportId) // Use the reportId to query the specific document
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // Extract data
                            String carPlateStr = document.getString("ViolationsCarElph") + document.getString("ViolationsCarNum");
                            String timeStr = document.getString("Date");
                            String locationStr = document.getString("ViolationsArea") + " " + document.getString("ViolationsStreet") + " " + document.getString("ViolationsAddress");
                            String lawStr = document.getString("Violations");
                            reportStatus = document.getBoolean("ReportStatus");

                            if (Boolean.TRUE.equals(reportStatus)) {
                                // ReportStatus 是 true，執行某些操作
                                report.setVisibility(View.GONE);
                            } else {
                                // ReportStatus 是 false 或者是 null，執行其他操作
                                report.setVisibility(View.VISIBLE);
                            }

                            // Update UI
                            carPlate.setText(carPlateStr);
                            time.setText(timeStr);
                            location.setText(locationStr);
                            law.setText(lawStr);

                            // Load image
                            photoPath = document.getString("ViolationsPic");
                            if (photoPath != null && !photoPath.isEmpty()) {
                                loadImageFromFirebaseStorage(photoPath);
                            }
                        } else {
                            Toast.makeText(CarsLogDetail.this, "No document found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CarsLogDetail.this, "Error getting document: " + task.getException(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadImageFromFirebaseStorage(String photoPath) {
        // Get the StorageReference for the image
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoPath);

        // Load image into ImageView using Glide
        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this)
                    .load(uri)  // Load the downloaded URL
                    .into(photo);  // Load image into ImageView (photo)
        }).addOnFailureListener(exception -> {
            Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show();
        });
    }
    private void deleteSubCollection(String reportId) {
        // 首先删除 violations 子集合中的所有文件
        db.collection("report").document(reportId).collection("violations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // 删除每个子文件
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(CarsLogDetail.this, "刪除成功", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(CarsLogDetail.this, "刪除失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                        // 子集合刪除完畢，刪除父文件和 users_report 中的文件
                        deleteUsersReport(reportId);  // 删除用户报告
                        deleteReport(reportId);        // 删除报告
                    } else {
                        Toast.makeText(CarsLogDetail.this, "獲取失敗: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 删除 users_report 中的文件
    private void deleteUsersReport(String reportId) {
        db.collection("users_report")
                .whereEqualTo("report_id", reportId) // 根据 report_id 查询
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            Log.d("CarsLogDetail", "No documents found in users_report for report_id: " + reportId);
                        } else {
                            Log.d("CarsLogDetail", "Documents found in users_report for report_id: " + reportId);
                        }
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // 删除与该报告相关的用户报告文件
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(CarsLogDetail.this, "刪除成功", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(CarsLogDetail.this, "删除失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(CarsLogDetail.this, "獲取ID失敗: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 刪除父文件（報告文件）並跳回 CarsLog 頁面
    private void deleteReport(String reportId) {
        db.collection("report").document(reportId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CarsLogDetail.this, "刪除成功", Toast.LENGTH_SHORT).show();

                    // 刪除成功後跳轉回 CarsLog 頁面
                    Intent intent = new Intent(CarsLogDetail.this, CarsLog.class);
                    startActivity(intent);

                    // 結束當前 CarsLogDetail 活動
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CarsLogDetail.this, "刪除失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // 删除 Firebase Storage 中的照片的代码
    private void deletePhotoFromStorage(String photoPath) {
        // 创建 StorageReference
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoPath);

        // 删除文件
        photoRef.delete()
                .addOnSuccessListener(aVoid -> {
                    // 删除成功的操作
                    Toast.makeText(CarsLogDetail.this, "照片删除成功", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(exception -> {
                    // 删除失败的操作
                    Toast.makeText(CarsLogDetail.this, "照片删除失败: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateReportStatus(String reportId, boolean isReported) {
        db.collection("report")
                .document(reportId) // 使用 reportId 来定位特定文档
                .update("ReportStatus", isReported) // 替换 "isReported" 为你实际的布尔字段名
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CarsLogDetail.this, "狀態更新成功。", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CarsLogDetail.this, "狀態更新失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
