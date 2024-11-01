package fcu.app.trafficviolationdetection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReportWebsite extends AppCompatActivity {

    WebView webview;
    WebSettings webSettings;
    String url = "https://suggest.police.taichung.gov.tw/traffic/";

    String Name="";
    String Id;
    String Address;
    String Number;
    String Email;
    String Job;
    String ReportAddress;
    String LicenseNumber1="";
    String LicenseNumber2="";
    String ReportReason;
    Integer gender;
    Integer LicenseType;
    String Date;
    String Qclass;
    String Cityarea;
    String Street;
    String ChtGender;
    String Choice;
    String reportId;
    String ReportPic;
    private int totalDocumentsToFetch = 0;
    private int fetchedDocumentsCount = 0;
    private FirebaseStorage storage;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ValueCallback<Uri[]> mUploadMessageLollipop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report_website);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        webview = findViewById(R.id.wb);
        webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);

        reportId = getIntent().getStringExtra("reportId");
        storage = FirebaseStorage.getInstance();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            getLocationAndLoadWebView();
        }

        fetchDataFromUsersReport();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocationAndLoadWebView();
        }
    }

    private void getLocationAndLoadWebView() {
        webview.setWebViewClient(new WebViewClient() {
            private boolean isFormFilled = false;

            @Override
            public void onPageFinished(WebView view, String url) {
                if (isFormFilled) {
                    return;
                }

                isFormFilled = true;

                if (ChtGender == null || ChtGender.equals("男")) {
                    gender = 1;
                } else if (ChtGender.equals("女")) {
                    gender = 2;
                }

                if (Choice == null || Choice.equals("身分證")) {
                    LicenseType = 1;
                } else if (Choice.equals("居留證")) {
                    LicenseType = 2;
                }

                new Handler().postDelayed(() -> {
                    view.evaluateJavascript("document.getElementById('OK').checked = true;", null);
                    view.evaluateJavascript("document.getElementById('nextstep').style.display = 'block';", null);
                    view.evaluateJavascript("document.getElementById('nextstep').click();", null);
                }, 1000);

                new Handler().postDelayed(() -> {
                    view.evaluateJavascript("window.location.href = 'traffic_write.jsp';", null);
                }, 2000);

                new Handler().postDelayed(() -> {
                    view.evaluateJavascript("document.getElementById('name').value = '" + Name + "';", null);
                    view.evaluateJavascript("document.getElementById('sub').value = '" + Id + "';", null);
                    view.evaluateJavascript("document.getElementById('address').value = '" + Address + "';", null);
                    view.evaluateJavascript("document.getElementById('liaisontel').value = '" + Number + "';", null);
                    view.evaluateJavascript("document.getElementById('email').value = '" + Email + "';", null);
                    view.evaluateJavascript("document.getElementById('job').value = '" + Job + "';", null);
                    view.evaluateJavascript("document.getElementById('inputaddress').value = '" + ReportAddress + "';", null);
                    view.evaluateJavascript("document.getElementById('licensenumber2').value = '" + LicenseNumber1 + "';", null);
                    view.evaluateJavascript("document.getElementById('licensenumber3').value = '" + LicenseNumber2 + "';", null);
                    view.evaluateJavascript("document.getElementById('detailcontent').value = '" + ReportReason + "';", null);
                    if (gender == 1) {
                        view.evaluateJavascript("document.getElementById('male').checked = true;", null);
                    } else if (gender == 2) {
                        view.evaluateJavascript("document.getElementById('female').checked = true;", null);
                    }
                    if (LicenseType == 1) {
                        view.evaluateJavascript("document.getElementById('taiwan').checked = true;", null);
                    } else if (LicenseType == 2) {
                        view.evaluateJavascript("document.getElementById('nottaiwan').checked = true;", null);
                    }
                    String script = "document.getElementById('violationdatetime').removeAttribute('readonly');" +
                            "document.getElementById('violationdatetime').value = '" + Date + "';" +
                            "document.getElementById('violationdatetime').setAttribute('readonly', '');";
                    view.evaluateJavascript(script, null);
                    view.evaluateJavascript("document.getElementById('qclass').value = '" + Qclass + "';", null);

                    String cityAreaScript = "var citySelect = document.getElementById('cityarea');" +
                            "var cityOptions = citySelect.options;" +
                            "for (var i = 0; i < cityOptions.length; i++) {" +
                            "  if (cityOptions[i].text === '" + Cityarea + "') {" +
                            "    citySelect.selectedIndex = i;" +
                            "    citySelect.dispatchEvent(new Event('change'));" +
                            "    break;" +
                            "  }" +
                            "}" +
                            "document.querySelector('#cityarea + .dropdown-menu .filter-option').textContent = '" + Cityarea + "';";
                    view.evaluateJavascript(cityAreaScript, null);

                    new Handler().postDelayed(() -> {
                        String streetScript = "var streetSelect = document.getElementById('street');" +
                                "var streetOptions = streetSelect.options;" +
                                "for (var i = 0; i < streetOptions.length; i++) {" +
                                "  if (streetOptions[i].text === '" + Street + "') {" +
                                "    streetSelect.selectedIndex = i;" +
                                "    streetSelect.dispatchEvent(new Event('change'));" +
                                "    break;" +
                                "  }" +
                                "}" +
                                "document.querySelector('#street + .dropdown-menu .filter-option').textContent = '" + Street + "';";
                        view.evaluateJavascript(streetScript, null);
                    }, 1000);

                }, 3000);
            }
        });

        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                mUploadMessageLollipop = filePathCallback;
                fetchImageAndUpload();
                return true;
            }
        });

        webview.loadUrl(url);
    }

    private void fetchDataFromUsersReport() {
        db.collection("users_report")
                .whereEqualTo("report_id", reportId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        totalDocumentsToFetch = queryDocumentSnapshots.size();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String userId = document.getString("user_id");
                            fetchUserDetails(userId);
                            fetchReportDetails(reportId);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("UserReport", "Error fetching users_report", e));
    }

    private void fetchUserDetails(String userId) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Name = documentSnapshot.getString("username");
                        Id = documentSnapshot.getString("ID_number");
                        Address = documentSnapshot.getString("user_address");
                        Number = documentSnapshot.getString("phone_num");
                        Email = documentSnapshot.getString("user_email");
                        ChtGender = documentSnapshot.getString("gender");
                        Choice = documentSnapshot.getString("ID_choice");
                    }
                    checkIfAllDataFetched();
                })
                .addOnFailureListener(e -> Toast.makeText(ReportWebsite.this, "Error fetching user details", Toast.LENGTH_SHORT).show());
    }

    private void fetchReportDetails(String reportId) {
        db.collection("report")
                .document(reportId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ReportAddress = documentSnapshot.getString("ViolationsAddress");
                        LicenseNumber1 = documentSnapshot.getString("ViolationsCarElph");
                        LicenseNumber2 = documentSnapshot.getString("ViolationsCarNum");
                        ReportReason = documentSnapshot.getString("ViolationsFact");
                        Qclass = documentSnapshot.getString("Violations");
                        Cityarea = documentSnapshot.getString("ViolationsArea");
                        Street = documentSnapshot.getString("ViolationsStreet");
                        Date = documentSnapshot.getString("Date");
                        ReportPic = documentSnapshot.getString("ViolationsPic");
                    }
                    checkIfAllDataFetched();
                })
                .addOnFailureListener(e -> Toast.makeText(ReportWebsite.this, "Error fetching report details", Toast.LENGTH_SHORT).show());
    }

    private void checkIfAllDataFetched() {
        fetchedDocumentsCount++;
        if (fetchedDocumentsCount >= totalDocumentsToFetch) {
            getLocationAndLoadWebView();
        }
    }

    private void fetchImageAndUpload() {
        if (ReportPic != null) {
            StorageReference photoref = storage.getReferenceFromUrl(ReportPic);
            try {
                final File file = File.createTempFile("image", ".jpg");
                photoref.getFile(file).addOnSuccessListener(taskSnapshot -> uploadFileToWebView(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadFileToWebView(File file) {
        Uri fileUri = Uri.fromFile(file);
        if (mUploadMessageLollipop != null) {
            mUploadMessageLollipop.onReceiveValue(new Uri[]{fileUri});
            mUploadMessageLollipop = null;
        }
    }
}
