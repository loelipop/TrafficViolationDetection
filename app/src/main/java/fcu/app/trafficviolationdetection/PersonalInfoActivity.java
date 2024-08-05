package fcu.app.trafficviolationdetection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PersonalInfoActivity extends AppCompatActivity {
    private TextView InfoName;
    private TextView InfoGender;
    private TextView InfoIDchoice;
    private TextView InfoIDnum;
    private TextView InfoAddress;
    private TextView InfoPhone;
    private TextView InfoEmail;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Button Logout;
    private ImageButton Back;
    private ImageButton GoEditInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_personal_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        InfoName = findViewById(R.id.textViewName);
        InfoGender = findViewById(R.id.tvGender);
        InfoIDchoice = findViewById(R.id.textViewIDchoice);
        InfoIDnum = findViewById(R.id.textViewIDnum);
        InfoAddress = findViewById(R.id.textViewAddress);
        InfoPhone = findViewById(R.id.textViewPhone);
        InfoEmail = findViewById(R.id.tvEmail);
        Logout = findViewById(R.id.logout);
        Back = findViewById(R.id.back);
        GoEditInfo = findViewById(R.id.editInfo);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null){
            String uid = currentUser.getUid();

            DocumentReference docRef = db.collection("users").document(uid);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()){
                            InfoName.setText(document.getString("username"));
                            InfoGender.setText(document.getString("gender"));
                            InfoIDchoice.setText(document.getString("ID_choice"));
                            InfoIDnum.setText(document.getString("ID_number"));
                            InfoAddress.setText(document.getString("user_address"));
                            InfoPhone.setText(document.getString("phone_num"));
                            InfoEmail.setText(document.getString("user_email"));
                        }else{
                            Toast.makeText(PersonalInfoActivity.this, "找不到個人資料", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(PersonalInfoActivity.this, "發生問題", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.logout){
                    mAuth.signOut();
                    Toast.makeText(PersonalInfoActivity.this, "登出成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PersonalInfoActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else if (v.getId() == R.id.back) {
                    finish();
                } else if (v.getId() == R.id.editInfo) {
                    Intent intent = new Intent();
                    intent.setClass(PersonalInfoActivity.this, EditPersonalInfoActivity.class);
                    PersonalInfoActivity.this.startActivity(intent);
                }

            }
        };
        Logout.setOnClickListener(listener);
        Back.setOnClickListener(listener);
        GoEditInfo.setOnClickListener(listener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null){
            String uid = currentUser.getUid();

            DocumentReference docRef = db.collection("users").document(uid);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()){
                            InfoName.setText(document.getString("username"));
                            InfoGender.setText(document.getString("gender"));
                            InfoIDchoice.setText(document.getString("ID_choice"));
                            InfoIDnum.setText(document.getString("ID_number"));
                            InfoAddress.setText(document.getString("user_address"));
                            InfoPhone.setText(document.getString("phone_num"));
                            InfoEmail.setText(document.getString("user_email"));
                        }else{
                            Toast.makeText(PersonalInfoActivity.this, "找不到個人資料", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(PersonalInfoActivity.this, "發生問題", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}