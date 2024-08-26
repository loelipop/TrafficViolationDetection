package fcu.app.trafficviolationdetection;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class CarsLog extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView item;
    private List<items> carsLog;
    private RecyclerView.Adapter adapter;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;




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
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        carsLog = new ArrayList<>();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            String userId = currentUser.getUid();
            checkUserscarLog(userId);
        }else{
            Toast.makeText(this, "用戶沒有登入", Toast.LENGTH_SHORT).show();
        }



        ItemAdapter adapter = new ItemAdapter(this, carsLog);
        item.setAdapter(adapter);
    }

    private void loadCarLogFromFirestore(List<String> reportIds){
        db.collection("report")
                .whereIn(FieldPath.documentId(), reportIds)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String carDate = document.getString("Date");
                            String carPlate = document.getString("ViolationsCarElph") + document.getString("ViolationsCarNum");
                            String carRule = document.getString("Violations");
                            String reportId = document.getString("reportId");

                            carsLog.add(new items(carDate, carPlate, carRule, reportId));
                        }
                        item.getAdapter().notifyDataSetChanged();
                    } else {
                        Toast.makeText(CarsLog.this, "Error getting documents: " + task.getException(), Toast.LENGTH_LONG).show();
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
}


