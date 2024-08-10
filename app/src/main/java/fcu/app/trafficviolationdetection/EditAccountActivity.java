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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditAccountActivity extends AppCompatActivity {
    private TextView showEmail;
    private EditText changePassword;
    private EditText confirmChangedPassword;
    private EditText oldPassword;
    private Button Buttonchangepass;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        showEmail = findViewById(R.id.EditEmail);
        changePassword = findViewById(R.id.newPassword);
        confirmChangedPassword = findViewById(R.id.ConfirmNewPass);
        oldPassword = findViewById(R.id.oldPassword);
        Buttonchangepass = findViewById(R.id.editPassword);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if (user != null){
            showEmail.setText(user.getEmail());
        }
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPass = oldPassword.getText().toString();
                String newPass = changePassword.getText().toString();
                String confirmPass = confirmChangedPassword.getText().toString();

                if (oldPass.isEmpty() || newPass.isEmpty() ||confirmPass.isEmpty()) {
                    if(oldPass.isEmpty()){
                        oldPassword.setError("請輸入你的舊密碼");
                    }
                    if (newPass.isEmpty()) {
                        changePassword.setError("請輸入您要設置的新密碼");
                    }
                    if (confirmPass.isEmpty()) {
                        confirmChangedPassword.setError("請再次輸入您要設置的新密碼");
                    }
                    return;
                }

                if (newPass.equals(oldPass)) {
                    changePassword.setError("新密碼不能与舊密碼相同");
                    return;
                }

                if (!newPass.equals(confirmPass)) {
                    changePassword.setError("輸入的密碼不一致");
                    confirmChangedPassword.setError("輸入的密碼不一致");
                    return;
                }

                String checked_pass = Check_password(newPass);
                if (!checked_pass.equals("true")) {
                    changePassword.setError("密碼必須大於8位，必須由英文字母和數字組成");
                    confirmChangedPassword.setError("密碼必須大於8位，必須由英文字母和數字組成");
                    return;
                }else {
                    updatePassword(user, oldPass, newPass);
                }
            }
        };
        Buttonchangepass.setOnClickListener(listener);
    }
    private String Check_password(String password){
        if(password.length()<8){
            changePassword.setError("輸入的密碼必須大於8位");
            confirmChangedPassword.setError("輸入的密碼必須大於8位");
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

    private void updatePassword(FirebaseUser user, String oldPass, String newPass) {
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);

        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(EditAccountActivity.this, "密碼已成功更新", Toast.LENGTH_SHORT).show();
                                mAuth.signOut();
                                Intent intent = new Intent(EditAccountActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finishAffinity();
                            } else {
                                Toast.makeText(EditAccountActivity.this, "更新密碼失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    oldPassword.setError("舊密碼輸入錯誤，請仔細檢查");
                }
            }
        });
    }
}