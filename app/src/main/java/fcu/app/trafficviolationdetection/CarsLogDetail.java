package fcu.app.trafficviolationdetection;

import android.content.Intent;
import android.os.Bundle;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

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

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        };

        View.OnClickListener listener2 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(CarsLogDetail.this, ReportWebsite.class);
                CarsLogDetail.this.startActivity(intent);
            }
        };

        backbutton.setOnClickListener(listener);
        report.setOnClickListener(listener2);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            String userId = currentUser.getUid();
            checkUserscarLog(userId);
        }else{
            Toast.makeText(this, "用戶沒有登入！", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCarLogFromFirestore(List<String> reportIds){
        db.collection("report")
                .whereIn(FieldPath.documentId(), reportIds)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String carPlateStr = document.getString("ViolationsCarElph") + document.getString("ViolationsCarNum");
                            String timeStr = document.getString("Date");
                            String locationStr = document.getString("ViolationsArea") + document.getString("ViolationsStreet") + document.getString("ViolationsAddress");
                            String lawStr = document.getString("Violations");
                            String reportId = document.getString("reportId");

                            carPlate.setText(carPlateStr);
                            time.setText(timeStr);
                            location.setText(locationStr);
                            law.setText(lawStr);

                            String photoPath = document.getString("ViolationsPic");
                            if (photoPath != null && !photoPath.isEmpty()) {
                                loadImageFromFirebaseStorage(photoPath); // 調用方法來加載圖片
                            }
                        }
                    } else {
                        Toast.makeText(CarsLogDetail.this, "Error getting documents: " + task.getException(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkUserscarLog(String userId){
        db.collection("users_report")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        List<String> users_reportID = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()){
                            users_reportID.add(document.getString("report_id"));
                        }
                        if (users_reportID.isEmpty()){
                            Toast.makeText(this, "沒有違規記錄", Toast.LENGTH_SHORT).show();
                        }else {
                            loadCarLogFromFirestore(users_reportID);
                        }
                    }else{
                        Toast.makeText(this, "發生錯誤", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadImageFromFirebaseStorage(String photoPath) {
        // 獲取圖片的 StorageReference
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoPath);

        // 使用 Glide 加載圖片到 ImageView photo 中
        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this)
                    .load(uri)  // 使用下載的 URL
                    .into(photo);  // 將圖片加載到 ImageView (photo)
        }).addOnFailureListener(exception -> {
            Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show();
        });
    }
}