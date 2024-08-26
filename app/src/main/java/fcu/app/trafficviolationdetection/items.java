package fcu.app.trafficviolationdetection;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class items {
    private String carDate;
    private String carPlate;
    private String carRule;
    private String reportId;

    public items(String carDate, String carPlate, String carRule, String reportId){
        this.carDate = carDate;
        this.carPlate = carPlate;
        this.carRule = carRule;
        this.reportId = reportId;
    }

    public String getCarDate() {return carDate; }

    public String getCarPlate() {return carPlate; }

    public String getCarRule() {return carRule; }

    public String getReportId() {return reportId; }
}