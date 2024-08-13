package fcu.app.trafficviolationdetection;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ScrollView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText Name;
    private EditText ID;
    private EditText Address;
    private EditText Phone;
    private RadioButton Men;
    private RadioButton Women;
    private RadioButton Citizen;
    private RadioButton Residence;
    private RadioGroup Gender;
    private RadioGroup IdChoice;
    private Button Confirm_Register;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String user_gender;
    private String ID_choice;
    private Map<String, Object> user = new HashMap<>();
    private boolean isRegistered = false;

    private boolean checkCitizenID(String inputString) {
        inputString = inputString.toUpperCase();
        if (!inputString.matches("^[A-Z][1-2][0-9]{8}$")) return false;

        int[] weights = {1, 9, 8, 7, 6, 5, 4, 3, 2, 1, 1};
        int[] digits = new int[11];
        Map<Character, Integer> letterToDigitMap = new HashMap<>();
        letterToDigitMap.put('A', 10);
        letterToDigitMap.put('B', 11);
        letterToDigitMap.put('C', 12);
        letterToDigitMap.put('D', 13);
        letterToDigitMap.put('E', 14);
        letterToDigitMap.put('F', 15);
        letterToDigitMap.put('G', 16);
        letterToDigitMap.put('H', 17);
        letterToDigitMap.put('I', 34);
        letterToDigitMap.put('J', 18);
        letterToDigitMap.put('K', 19);
        letterToDigitMap.put('L', 20);
        letterToDigitMap.put('M', 21);
        letterToDigitMap.put('N', 22);
        letterToDigitMap.put('O', 35);
        letterToDigitMap.put('P', 23);
        letterToDigitMap.put('Q', 24);
        letterToDigitMap.put('R', 25);
        letterToDigitMap.put('S', 26);
        letterToDigitMap.put('T', 27);
        letterToDigitMap.put('U', 28);
        letterToDigitMap.put('V', 29);
        letterToDigitMap.put('W', 32);
        letterToDigitMap.put('X', 30);
        letterToDigitMap.put('Y', 31);
        letterToDigitMap.put('Z', 33);

        char firstLetter =inputString.charAt(0);
        int firstDigit = letterToDigitMap.get(firstLetter);
        digits[0] = firstDigit / 10;
        digits[1] = firstDigit % 10;

        for (int i = 1; i < 10; i++) {
            digits[i+1] = Character.getNumericValue(inputString.charAt(i));
        }

        int sum = 0;
        for (int i = 0; i < 11; i++) {
            sum += digits[i] * weights[i];
        }
        return sum % 10 == 0;
    }

    private boolean checkResidenceID(String inputString) {
        inputString = inputString.toUpperCase();
        if (!inputString.matches("^[A-Z][8-9][0-9]{8}$")) return false;

        int[] weights = {1, 9, 8, 7, 6, 5, 4, 3, 2, 1, 1};
        int[] digits = new int[11];
        Map<Character, Integer> letterToDigitMap = new HashMap<>();
        letterToDigitMap.put('A', 10);
        letterToDigitMap.put('B', 11);
        letterToDigitMap.put('C', 12);
        letterToDigitMap.put('D', 13);
        letterToDigitMap.put('E', 14);
        letterToDigitMap.put('F', 15);
        letterToDigitMap.put('G', 16);
        letterToDigitMap.put('H', 17);
        letterToDigitMap.put('I', 34);
        letterToDigitMap.put('J', 18);
        letterToDigitMap.put('K', 19);
        letterToDigitMap.put('L', 20);
        letterToDigitMap.put('M', 21);
        letterToDigitMap.put('N', 22);
        letterToDigitMap.put('O', 35);
        letterToDigitMap.put('P', 23);
        letterToDigitMap.put('Q', 24);
        letterToDigitMap.put('R', 25);
        letterToDigitMap.put('S', 26);
        letterToDigitMap.put('T', 27);
        letterToDigitMap.put('U', 28);
        letterToDigitMap.put('V', 29);
        letterToDigitMap.put('W', 32);
        letterToDigitMap.put('X', 30);
        letterToDigitMap.put('Y', 31);
        letterToDigitMap.put('Z', 33);

        char firstLetter =inputString.charAt(0);
        int firstDigit = letterToDigitMap.get(firstLetter);
        digits[0] = firstDigit / 10;
        digits[1] = firstDigit % 10;

        for (int i = 1; i < 10; i++) {
            digits[i+1] = Character.getNumericValue(inputString.charAt(i));
        }

        int sum = 0;
        for (int i = 0; i < 11; i++) {
            sum += digits[i] * weights[i];
        }
        return sum % 10 == 0;
    }

    private void register(String email, String uid, String Name, String Gender, String ID_choice, String ID_num, String Address, String Phone){
        user.put("ID_choice",ID_choice);
        user.put("ID_number",ID_num);
        user.put("gender",Gender);
        user.put("phone_num",Phone);
        user.put("user_address",Address);
        user.put("user_email",email);
        user.put("username",Name);

        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        isRegistered = true;
                        Toast.makeText(RegisterActivity.this, "個人資料上傳成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.setClass(RegisterActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        RegisterActivity.this.startActivity(intent);
                        finish();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Name = findViewById(R.id.input_name);
        ID = findViewById(R.id.input_id);
        Address = findViewById(R.id.input_address);
        Phone = findViewById(R.id.input_phone);
        Men = findViewById(R.id.men);
        Women = findViewById(R.id.women);
        Citizen = findViewById(R.id.citizen_id);
        Residence = findViewById(R.id.residence_id);
        Gender = findViewById(R.id.gender);
        IdChoice=findViewById(R.id.choice_id);
        Confirm_Register = findViewById(R.id.email_register);
        user_gender = "";
        ID_choice = "";
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        RadioGroup.OnCheckedChangeListener rgListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                if(radioGroup.getId() == R.id.gender){
                    RadioButton gen = findViewById(id);
                    user_gender = gen.getText().toString();
                }
                if(radioGroup.getId() == R.id.choice_id){
                    RadioButton choice = findViewById(id);
                    ID_choice = choice.getText().toString();
                }
            }
        };
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = Name.getText().toString();
                String idnum = ID.getText().toString();
                String address = Address.getText().toString();
                String phone = Phone.getText().toString();
                String email = mAuth.getCurrentUser().getEmail();
                String uid = mAuth.getCurrentUser().getUid();


                if (name.isEmpty() || idnum.isEmpty() || address.isEmpty() || phone.isEmpty() || user_gender.isEmpty() || ID_choice.isEmpty()) {
                    if (name.isEmpty()) {
                        Name.setError("請輸入您的姓名");
                    }

                    if (idnum.isEmpty()) {
                        ID.setError("請輸入您的證件號碼");
                    }

                    if (address.isEmpty()) {
                        Address.setError("請輸入您的聯絡地址");
                    }

                    if (phone.isEmpty()) {
                        Phone.setError("請輸入您的聯絡電話");
                    }

                    if (user_gender.isEmpty()) {
                        Toast.makeText(RegisterActivity.this, "請選擇性別", Toast.LENGTH_SHORT).show();
                    }

                    if (ID_choice.isEmpty()) {
                        Toast.makeText(RegisterActivity.this, "請選擇證件類型", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (ID_choice.equals("身份證")) {
                        if (!checkCitizenID(idnum)) {
                            ID.setError("身份證格式錯誤");
                        } else {
                            register(email, uid, name, user_gender, ID_choice, idnum, address, phone);
                        }
                    } else if (ID_choice.equals("居留證")) {
                        if (!checkResidenceID(idnum)) {
                            ID.setError("居留證格式錯誤");
                        } else {
                            register(email, uid, name, user_gender, ID_choice, idnum, address, phone);
                        }
                    }
                }

            }
        };
        Gender.setOnCheckedChangeListener(rgListener);
        IdChoice.setOnCheckedChangeListener(rgListener);
        Confirm_Register.setOnClickListener(listener);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mAuth.signOut();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isRegistered && !isFinishing()){
            mAuth.signOut();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isRegistered){
            mAuth.signOut();
        }
    }
}