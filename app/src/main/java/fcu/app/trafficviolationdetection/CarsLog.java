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

        item = findViewById(R.id.logs);
        item.setLayoutManager(new LinearLayoutManager(this));

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        carsLog = new ArrayList<>();
        loadCarLogFromFirestore();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            item.setPadding(systemBars.left, item.getPaddingTop(), systemBars.right, item.getPaddingBottom());
            return insets;
        });

        ItemAdapter adapter = new ItemAdapter(this, carsLog);
        item.setAdapter(adapter);
    }

    private void loadCarLogFromFirestore(){
        db.collection("report").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String carDate = document.getString("carDate");
                            String carPlate = document.getString("carPlate");
                            String carRule = document.getString("carRule");
                            String reportId = document.getString("reportId");

                            carsLog.add(new items(carDate, carPlate, carRule, reportId));
                        }
                        item.getAdapter().notifyDataSetChanged();
                    } else {
                        Toast.makeText(CarsLog.this, "Error getting documents: " + task.getException(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}


