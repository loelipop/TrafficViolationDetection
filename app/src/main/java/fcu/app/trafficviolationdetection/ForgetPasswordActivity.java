package fcu.app.trafficviolationdetection;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ForgetPasswordActivity extends AppCompatActivity {
    private EditText email;
    private Button forget_password;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this); // 确保Firebase初始化
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forget_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        email = findViewById(R.id.email_forgetpass);
        forget_password = findViewById(R.id.confirmForgetpass);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = email.getText().toString().trim();

                if (Email.isEmpty()) {
                    Toast.makeText(ForgetPasswordActivity.this, "請輸入電子郵件", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
                    Toast.makeText(ForgetPasswordActivity.this, "請輸入有效的電子郵件", Toast.LENGTH_SHORT).show();
                    return;
                }
                db.collection("users").whereEqualTo("user_email",Email).get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful()){
                                            if(!task.getResult().isEmpty()){
                                                mAuth.sendPasswordResetEmail(Email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Toast.makeText(ForgetPasswordActivity.this, "郵件寄送成功", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(ForgetPasswordActivity.this, LoginActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }else{
                                                            Toast.makeText(ForgetPasswordActivity.this, "郵件寄送失敗", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }else{
                                                Toast.makeText(ForgetPasswordActivity.this, "查無此賬號，請重新填寫您的電子郵件", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });

            }
        };
        forget_password.setOnClickListener(listener);
    }
}
