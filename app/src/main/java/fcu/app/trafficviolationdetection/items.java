package fcu.app.trafficviolationdetection;

public class items {
    private String carDate;
    private String carPlate;
    private String carRule;
    private String reportId;
    private boolean reportedStatus; // Ensure this is a boolean

    public items(String carDate, String carPlate, String carRule, String reportId, boolean reportedStatus) {
        this.carDate = carDate;
        this.carPlate = carPlate;
        this.carRule = carRule;
        this.reportId = reportId;
        this.reportedStatus = reportedStatus; // Ensure this is assigned correctly
    }

    public String getCarDate() { return carDate; }
    public String getCarPlate() { return carPlate; }
    public String getCarRule() { return carRule; }
    public String getReportId() { return reportId; }
    public boolean isReportedStatus() { return reportedStatus; } // Ensure this returns a boolean
}

