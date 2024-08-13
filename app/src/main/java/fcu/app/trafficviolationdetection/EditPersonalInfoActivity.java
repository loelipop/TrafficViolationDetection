package fcu.app.trafficviolationdetection;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class EditPersonalInfoActivity extends AppCompatActivity {
    private EditText changeName;
    private EditText changeAddress;
    private EditText changePhone;
    private TextView changeAccount;
    private Button ConfirmChanges;
    private TextView EInfoGender;
    private TextView EInfoIDchoice;
    private TextView EInfoIDnum;
    private TextView EInfoEmail;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String originalUsername, originalPhoneNum, originalUserAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_personal_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        changeName = findViewById(R.id.EditName);
        changeAddress = findViewById(R.id.EditAddress);
        changePhone = findViewById(R.id.EditPhone);
        EInfoGender = findViewById(R.id.Gender);
        EInfoIDchoice = findViewById(R.id.IDchoice);
        EInfoIDnum = findViewById(R.id.IDnum);
        EInfoEmail = findViewById(R.id.Email);
        changeAccount = findViewById(R.id.EditEmail);
        ConfirmChanges = findViewById(R.id.EditInfo);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (user != null){
            String uid = user.getUid();
            DocumentReference docRef = db.collection("users").document(uid);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()){
                            changeName.setText(document.getString("username"));
                            EInfoGender.setText(document.getString("gender"));
                            EInfoIDchoice.setText(document.getString("ID_choice"));
                            EInfoIDnum.setText(document.getString("ID_number"));
                            changeAddress.setText(document.getString("user_address"));
                            changePhone.setText(document.getString("phone_num"));
                            EInfoEmail.setText(document.getString("user_email"));

                            originalUsername = document.getString("username");
                            originalPhoneNum = document.getString("phone_num");
                            originalUserAddress = document.getString("user_address");

                        }else{
                            Toast.makeText(EditPersonalInfoActivity.this, "找不到個人資料", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(EditPersonalInfoActivity.this, "發生問題", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.EditEmail){
                    Intent intent = new Intent();
                    intent.setClass(EditPersonalInfoActivity.this, EditAccountActivity.class);
                    EditPersonalInfoActivity.this.startActivity(intent);
                }else {
                    String name = changeName.getText().toString();
                    String address = changeAddress.getText().toString();
                    String phone = changePhone.getText().toString();
                    String user_id = user.getUid();

                    Map<String, Object> updates = new HashMap<>();

                    if (!name.equals(originalUsername)) {
                        updates.put("username", name);
                    }
                    if (!phone.equals(originalPhoneNum)) {
                        updates.put("phone_num", phone);
                    }
                    if (!address.equals(originalUserAddress)) {
                        updates.put("user_address", address);
                    }

                    if (!updates.isEmpty()){
                        db.collection("users").document(user_id)
                                .set(updates, SetOptions.merge())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(EditPersonalInfoActivity.this, "資料已更新", Toast.LENGTH_SHORT).show();
                                            //Intent intent = new Intent(ACTION_CLOSE_PERSONAL_INFO_ACTIVITY);
                                           // sendBroadcast(intent);
                                            //intent.setClass(EditPersonalInfoActivity.this, PersonalInfoActivity.class);
                                            //EditPersonalInfoActivity.this.startActivity(intent);
                                            finish();
                                        }else {
                                            Toast.makeText(EditPersonalInfoActivity.this, "資料更新失敗", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }else {
                        Toast.makeText(EditPersonalInfoActivity.this, "沒有更新", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        ConfirmChanges.setOnClickListener(listener);
        changeAccount.setOnClickListener(listener);
    }
}