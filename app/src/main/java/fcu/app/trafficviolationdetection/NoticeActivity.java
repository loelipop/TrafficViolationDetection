package fcu.app.trafficviolationdetection;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NoticeActivity extends AppCompatActivity {
    private CheckBox Confirm_one;
    private CheckBox Confirm_two;
    private Button Go_register;
    private boolean isBothChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notice);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Confirm_one = findViewById(R.id.checkBox_confirm1);
        Confirm_two = findViewById(R.id.checkBox_confirm2);
        Go_register = findViewById(R.id.go_register);

        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(Confirm_one.isChecked() && Confirm_two.isChecked()){
                    isBothChecked = true;
                }else{
                    isBothChecked = false;
                }
            }
        };

        View.OnClickListener listener1 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBothChecked){
                    Intent intent = new Intent();
                    intent.setClass(NoticeActivity.this,EmailRegisterActivity.class);
                    NoticeActivity.this.startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(NoticeActivity.this, "請將同意事項勾選完整", Toast.LENGTH_SHORT).show();
                }
            }
        };

        Confirm_one.setOnCheckedChangeListener(listener);
        Confirm_two.setOnCheckedChangeListener(listener);
        Go_register.setOnClickListener(listener1);
    }
}