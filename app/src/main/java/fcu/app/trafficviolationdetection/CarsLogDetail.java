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
            Intent intent = new Intent(CarsLogDetail.this, ReportWebsite.class);
            startActivity(intent);
        });

        delete.setOnClickListener(v -> {
            if (reportId != null) {
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

                            // Update UI
                            carPlate.setText(carPlateStr);
                            time.setText(timeStr);
                            location.setText(locationStr);
                            law.setText(lawStr);

                            // Load image
                            String photoPath = document.getString("ViolationsPic");
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
    // 刪除子集合中的所有文件
    private void deleteSubCollection(String reportId) {
        db.collection("report").document(reportId).collection("violations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // 刪除每個子文件
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(CarsLogDetail.this, "子集合文件刪除成功", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(CarsLogDetail.this, "刪除子集合文件失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                        // 子集合刪除完畢，刪除父文件
                        deleteReport(reportId);
                    } else {
                        Toast.makeText(CarsLogDetail.this, "獲取子集合文件失敗: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 刪除父文件（報告文件）並跳回 CarsLog 頁面
    private void deleteReport(String reportId) {
        db.collection("report").document(reportId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CarsLogDetail.this, "報告刪除成功", Toast.LENGTH_SHORT).show();

                    // 刪除成功後跳轉回 CarsLog 頁面
                    Intent intent = new Intent(CarsLogDetail.this, CarsLog.class);
                    startActivity(intent);

                    // 結束當前 CarsLogDetail 活動
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CarsLogDetail.this, "刪除報告失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
