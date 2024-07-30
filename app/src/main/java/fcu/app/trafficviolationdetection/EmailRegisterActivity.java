package fcu.app.trafficviolationdetection;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class EmailRegisterActivity extends AppCompatActivity {
    private EditText Email;
    private EditText Password;
    private EditText ConfirmPassword;
    private Button RegisterEmail;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_email_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Email = findViewById(R.id.input_email);
        Password = findViewById(R.id.input_password);
        ConfirmPassword = findViewById(R.id.retype_password);
        RegisterEmail = findViewById(R.id.email_register);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Email.getText().toString();
                String password = Password.getText().toString();
                String repassword = ConfirmPassword.getText().toString();

                if (email.isEmpty() || password.isEmpty() || repassword.isEmpty()) {
                    if(email.isEmpty()){
                        Email.setError("請輸入電子郵件");
                    }
                    if (password.isEmpty()) {
                        Password.setError("請輸入您要設置的密碼");
                    }
                    if (repassword.isEmpty()) {
                        ConfirmPassword.setError("請再次輸入您要設置的密碼");
                    }
                    return;
                }

                if (!password.equals(repassword)) {
                    Password.setError("輸入的密碼不一致");
                    ConfirmPassword.setError("輸入的密碼不一致");
                    return;
                }



                String checked_pass = Check_password(password);
                if (!checked_pass.equals("true")) {
                    Password.setError("密碼必須大於8位，必須由英文字母和數字組成");
                    ConfirmPassword.setError("密碼必須大於8位，必須由英文字母和數字組成");
                    return;
                }

                checkEmailExists(email, new OnEmailCheckListener() {
                    @Override
                    public void onEmailCheck(boolean emailExists) {
                        if (emailExists) {
                            Email.setError("此賬號已存在");
                        } else {
                            // 邮箱不存在，进行注册
                            registerUser(email, password);
                        }
                    }
                });
            }
        };

        RegisterEmail.setOnClickListener(listener);
    }
    private String Check_password(String password){
        if(password.length()<8){
            Password.setError("輸入的密碼必須大於8位");
            ConfirmPassword.setError("輸入的密碼必須大於8位");
        }

        boolean hasLetter = false;
        boolean hasNumber = false;

        for(char c: password.toCharArray()){
            if(Character.isLetter(c)){
                hasLetter = true;
            }else if (Character.isDigit(c)){
                hasNumber = true;
            }
        }
        if(!hasLetter || !hasNumber){
            return "false";
        }

        return "true";
    }

    // 检查邮箱是否存在
    private void checkEmailExists(String email, final OnEmailCheckListener listener) {
        db.collection("users").whereEqualTo("user_email", email).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean emailExists = !task.getResult().isEmpty();
                            listener.onEmailCheck(emailExists);
                        } else {
                            listener.onEmailCheck(true); // 如果查询失败，默认认为邮箱存在以防止意外错误
                        }
                    }
                });
    }

    // 注册用户
    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EmailRegisterActivity.this, "賬號注冊成功", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            intent.setClass(EmailRegisterActivity.this, RegisterActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(EmailRegisterActivity.this, "賬號注冊失敗", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // 定义接口用于回调
    private interface OnEmailCheckListener {
        void onEmailCheck(boolean emailExists);
    }
}