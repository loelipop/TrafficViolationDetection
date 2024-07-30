package fcu.app.trafficviolationdetection;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private EditText email;
    private EditText password;
    private ImageView ShowNotPassword;
    private boolean isPasswordVisible = false;
    private Button Login;
    private TextView ForgetPass;
    private TextView Register;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        ShowNotPassword = findViewById(R.id.showpassword);
        Login = findViewById(R.id.login);
        ForgetPass = findViewById(R.id.forgetpassword);
        Register = findViewById(R.id.register);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser curent_user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        View.OnClickListener plistener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPasswordVisible){
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ShowNotPassword.setImageResource(R.drawable.dontshowpassword);
                }else{
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ShowNotPassword.setImageResource(R.drawable.showpassword);
                }
                isPasswordVisible = !isPasswordVisible;
                password.setSelection(password.getText().length());
            }
        };

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.login){
                    String Email = email.getText().toString();
                    String Password = password.getText().toString();
                    if(Email.isEmpty() || Password.isEmpty()){
                        Toast.makeText(LoginActivity.this,"登入失敗，請完整輸入您的Email和賬號密碼",Toast.LENGTH_SHORT).show();
                    }else{
                        mAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    String user_id = user.getUid();
                                    Toast.makeText(LoginActivity.this,"登入成功",Toast.LENGTH_SHORT).show();
                                    db.collection("users").document(user_id).get()
                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if(task.isSuccessful()){
                                                                DocumentSnapshot document = task.getResult();
                                                                if(document.exists()){
                                                                    Intent intent = new Intent();
                                                                    intent.setClass(LoginActivity.this, MainActivity.class);
                                                                    LoginActivity.this.startActivity(intent);
                                                                    finish();
                                                                }else{
                                                                    Intent intent = new Intent();
                                                                    intent.setClass(LoginActivity.this, RegisterActivity.class);
                                                                    LoginActivity.this.startActivity(intent);
                                                                    finish();
                                                                }
                                                            }
                                                        }
                                                    });

                                }else{
                                    Toast.makeText(LoginActivity.this,"登入失敗，請檢查您的email和密碼是否正確",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } else if (v.getId() == R.id.forgetpassword) {
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, ForgetPasswordActivity.class);
                    LoginActivity.this.startActivity(intent);
                } else if (v.getId() == R.id.register) {
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, NoticeActivity.class);
                    LoginActivity.this.startActivity(intent);
                    finish();
                }
            }
        };
        if(curent_user != null){
            Intent intent = new Intent();
            intent.setClass(LoginActivity.this, MainActivity.class);
            LoginActivity.this.startActivity(intent);
            finish();
        }else{
            ShowNotPassword.setOnClickListener(plistener);
            Login.setOnClickListener(listener);
            ForgetPass.setOnClickListener(listener);
            Register.setOnClickListener(listener);
        }

    }
}