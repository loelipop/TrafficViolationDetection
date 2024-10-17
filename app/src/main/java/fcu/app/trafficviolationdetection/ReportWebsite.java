package fcu.app.trafficviolationdetection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
    private int totalDocumentsToFetch = 0;
    private int fetchedDocumentsCount = 0;


    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final int FILECHOOSER_RESULTCODE = 1;
    private ValueCallback<Uri[]> mUploadMessageLollipop;

    //private FusedLocationProviderClient fusedLocationClient;

//    private Uri getExternalFileUri(String fileName) {
//        File file = new File("/sdcard/" + fileName);
//        return FileProvider.getUriForFile(this, "fcu.iecs.report.fileprovider", file);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_website);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        webview = findViewById(R.id.wb);
        webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true); // 开启 JavaScript 功能

        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
//        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
//            @Override
//            public void onSuccess(Location location) {
//                if (location != null) {
//                    double latitude = location.getLatitude();
//                    double longitude = location.getLongitude();
//                    getAddressFromLocation(latitude, longitude);
//                }
//            }
//        });

        webview.setWebViewClient(new WebViewClient() {
            private boolean isFormFilled = false;

            @Override
            public void onPageFinished(WebView view, String url) {
                if (isFormFilled) {
                    return;
                }

                isFormFilled = true;

                // 获取当前日期和时间
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date = dateFormat.format(currentDate);

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


                // 当页面加载完成后执行 JavaScript 来填写表单
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

                    // 修改 cityarea 的代码
                    String cityAreaScript =
                            "var citySelect = document.getElementById('cityarea');" +
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

                    // 延迟以确保 cityarea 变化生效，然后修改 street 的代码
                    new Handler().postDelayed(() -> {
                        String streetScript =
                                "var streetSelect = document.getElementById('street');" +
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

                    // 延迟触发文件选择
//                    new Handler().postDelayed(() -> {
//                        view.evaluateJavascript("document.getElementById('filename1').click();", null);
//                        view.evaluateJavascript("document.getElementById('filename1').click();", null);
//                    }, 2000);
                }, 3000);
            }
        });

//        webview.setWebChromeClient(new WebChromeClient() {
//            @Override
//            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
//                if (mUploadMessageLollipop != null) {
//                    mUploadMessageLollipop.onReceiveValue(null);
//                }
//                mUploadMessageLollipop = filePathCallback;
//
//                // 获取外部文件的 Uri
//                Uri fileUri = getExternalFileUri("IMG_20240529_175342.jpg");
//                if (fileUri != null) {
//                    filePathCallback.onReceiveValue(new Uri[]{fileUri});
//                } else {
//                    filePathCallback.onReceiveValue(null);
//                }
//                return true;
//            }
//        });

        webview.loadUrl(url); // 读取 URL 网站
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                ReportAddress = address.getThoroughfare(); // 路
                Cityarea = address.getSubLocality(); // 区
                Street = address.getFeatureName(); // 街
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchDataFromUsersReport() {
        db.collection("users_report")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        totalDocumentsToFetch = queryDocumentSnapshots.size();
                        Log.d("UserReport", "Total reports to fetch: " + totalDocumentsToFetch);
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String userId = document.getString("user_id");
                            String reportId = document.getString("report_id");

                            Log.d("UserReport", "Fetching userId: " + userId + ", reportId: " + reportId);
                            // Fetch user details
                            fetchUserDetails(userId);
                            // Fetch report details
                            fetchReportDetails(reportId);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("UserReport", "Error fetching users_report", e);
                    }
                });
    }

    private void fetchUserDetails(String userId) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Name = documentSnapshot.getString("username");
                            Id = documentSnapshot.getString("ID_number");
                            Address = documentSnapshot.getString("user_address");
                            Number = documentSnapshot.getString("phone_num");
                            Email = documentSnapshot.getString("user_email");
                            ChtGender = documentSnapshot.getString("gender");
                            Choice = documentSnapshot.getString("ID_choice");
                        }
                        checkIfAllDataFetched(); // 確認數據是否都獲取完成
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ReportWebsite.this, "Error fetching user details", Toast.LENGTH_SHORT).show();
                        checkIfAllDataFetched();
                    }
                });
    }

    private void fetchReportDetails(String reportId) {
        db.collection("report")
                .document(reportId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            ReportAddress = documentSnapshot.getString("ViolationsAddress");
                            LicenseNumber1 = documentSnapshot.getString("ViolationsCarElph");
                            LicenseNumber2 = documentSnapshot.getString("ViolationsCarNum");
                            ReportReason = documentSnapshot.getString("ViolationsFact");
                            Qclass = documentSnapshot.getString("Violations");
                            Cityarea = documentSnapshot.getString("ViolationsArea");
                            Street = documentSnapshot.getString("ViolationsStreet");
                            Date = documentSnapshot.getString("Date");
                        }
                        checkIfAllDataFetched(); // 確認數據是否都獲取完成
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ReportWebsite.this, "Error fetching report details", Toast.LENGTH_SHORT).show();
                        checkIfAllDataFetched();
                    }
                });
    }

    private void checkIfAllDataFetched() {
        fetchedDocumentsCount++;
        if (fetchedDocumentsCount >= totalDocumentsToFetch) {
            // 所有數據都加載完畢，這時可以加載 WebView
            getLocationAndLoadWebView();
        }
    }
}
