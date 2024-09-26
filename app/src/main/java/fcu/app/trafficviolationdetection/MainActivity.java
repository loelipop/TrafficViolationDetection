package fcu.app.trafficviolationdetection;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private ImageButton PersonalInfo;
    private Button ViolationLog;
    private Button ViolateTrafficLaw;
    private Button GoCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        PersonalInfo = findViewById(R.id.personal_info);
        ViolationLog = findViewById(R.id.button2);
        ViolateTrafficLaw = findViewById(R.id.button);
        GoCamera = findViewById(R.id.goCamera);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.personal_info) {
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, PersonalInfoActivity.class);
                    MainActivity.this.startActivity(intent);
                }else if (v.getId() == R.id.button2){
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, CarsLog.class);
                    MainActivity.this.startActivity(intent);
                }else if (v.getId() == R.id.button){
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, ViolateTrafficLaw.class);
                    MainActivity.this.startActivity(intent);
                } else if (v.getId() == R.id.goCamera) {
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, CameraActivity.class);
                    MainActivity.this.startActivity(intent);
                }
            }
        };
        PersonalInfo.setOnClickListener(listener);
        ViolationLog.setOnClickListener(listener);
        ViolateTrafficLaw.setOnClickListener(listener);
        GoCamera.setOnClickListener(listener);
    }
}