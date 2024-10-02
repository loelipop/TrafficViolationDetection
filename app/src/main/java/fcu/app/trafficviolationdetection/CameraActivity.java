package fcu.app.trafficviolationdetection;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {
    private PreviewView previewView;
    private TextView timestamp;
    private Button btnRecord;
    private VideoCapture<Recorder> videoCapture;
    private Recording currentRecording;
    private SimpleDateFormat sdf;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private StringBuilder routeData;
    private File routeFile;
    private String currentFileName;
    private Uri currentFileUri;

    private static final String TAG = "Dashcam";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        previewView = findViewById(R.id.previewView);
        timestamp = findViewById(R.id.timestamp);
        btnRecord = findViewById(R.id.btnRecord);

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        routeData = new StringBuilder();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // 记录当前位置到routeData
                    long currentTimeMillis = System.currentTimeMillis();

                    routeData.append(location.getLatitude())
                            .append(",")
                            .append(location.getLongitude())
                            .append("\n");
                }
            }
        };
        updateTimestamp();
        startCamera();

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentRecording != null){
                    stopRecording();
                }else {
                    startRecording();
                }
            }
        };
        btnRecord.setOnClickListener(listener);
    }
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                Recorder recorder = new Recorder.Builder().build();
                videoCapture = VideoCapture.withOutput(recorder);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Failed to bind camera use cases", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void startRecording() {
        if (videoCapture == null) return;

        // 在记录新的路径数据之前清空routeData
        routeData.setLength(0);
        // 获取当前用户的UID并将其作为文件的第一行
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            routeData.append("UID: ").append(uid).append("\n");
        }

        // 生成记录路线的文件
        try {
            routeFile = new File(getExternalFilesDir(null), "route_" + sdf.format(new Date()) + ".txt");
            if (!routeFile.exists()) {
                routeFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 生成输出文件名
        currentFileName = "dashcam_" + sdf.format(new Date()) + ".mp4";

        // 准备MediaStoreOutputOptions
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, currentFileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Dashcam");

        MediaStoreOutputOptions outputOptions = new MediaStoreOutputOptions.Builder(
                getContentResolver(),
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
                .setContentValues(contentValues)
                .build();

        // Start recording and assign the URI when the recording is finalized
        currentRecording = videoCapture.getOutput()
                .prepareRecording(this, outputOptions)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(this), videoRecordEvent -> {
                    if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                        currentFileUri = ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri();
                        onRecordingEvent(videoRecordEvent);
                    }
                });

        btnRecord.setText("結束錄影");
        btnRecord.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_dark));

        // 开始记录位置更新
        startLocationUpdates();
    }

    private void stopRecording() {
        if (currentRecording != null) {
            currentRecording.stop();
            currentRecording = null;
            stopLocationUpdates();
            saveRouteData(); // 保存路线数据到文件
            uploadRouteAndVideo(); // 上传路线数据和视频

            btnRecord.setText("開始錄影");
            btnRecord.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000); // 每1秒更新一次位置
        locationRequest.setFastestInterval(500); // 最快更新间隔为1秒
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void saveRouteData() {
        try (FileOutputStream fos = new FileOutputStream(routeFile);
             OutputStreamWriter writer = new OutputStreamWriter(fos)) {
            writer.write(routeData.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadRouteAndVideo() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            StorageReference storageReference = firebaseStorage.getReference().child(uid).child("video and route");

            // 检查视频文件的URI是否为空
            if (currentFileUri != null) {
                // 上传视频文件
                StorageReference videoRef = storageReference.child(currentFileName);
                UploadTask videoUploadTask = videoRef.putFile(currentFileUri);
                videoUploadTask.addOnSuccessListener(taskSnapshot -> {
                    Log.e(TAG, "影片上傳成功");
                }).addOnFailureListener(e -> {
                    Toast.makeText(CameraActivity.this, "影片上傳失败", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "影片上傳失败: " + e.getMessage(), e);
                    finish();
                });
            }

            // 上传路线文件
            StorageReference routeRef = storageReference.child(routeFile.getName());
            UploadTask routeUploadTask = routeRef.putFile(Uri.fromFile(routeFile));
            routeUploadTask.addOnSuccessListener(taskSnapshot -> {
                new AlertDialog.Builder(this)
                        .setMessage("影片上傳成功，請到違規車輛偵測記錄檢查是否有新增違規車輛")
                        .setPositiveButton("關閉", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .show();

            }).addOnFailureListener(e -> {
                Log.e(TAG, "路线数据上傳失败: " + e.getMessage(), e);
                new AlertDialog.Builder(this)
                        .setMessage("影片上傳失敗，很遺憾我們無法重新上傳影片，敬請見諒")
                        .setPositiveButton("關閉", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .show();
            });
        } else {
            Toast.makeText(this, "未登錄", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "未登錄");
            finish();
        }
    }


    private void onRecordingEvent(VideoRecordEvent event) {
        if (event instanceof VideoRecordEvent.Finalize) {
            VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) event;
            if (finalizeEvent.getError() == VideoRecordEvent.Finalize.ERROR_NONE) {
                Log.d(TAG, "Recording finalized successfully.");

                // 上传视频和路线数据
                uploadRouteAndVideo();
            } else {
                Log.e(TAG, "Recording failed with error: " + finalizeEvent.getError());
            }
        } else if (event instanceof VideoRecordEvent.Start) {
            Log.d(TAG, "Recording started");
        } else if (event instanceof VideoRecordEvent.Status) {
            Log.d(TAG, "Recording status: " + ((VideoRecordEvent.Status) event).getRecordingStats());
        } else if (event instanceof VideoRecordEvent.Pause) {
            Log.d(TAG, "Recording paused");
        } else if (event instanceof VideoRecordEvent.Resume) {
            Log.d(TAG, "Recording resumed");
        }
    }

    private void updateTimestamp() {
        final Runnable updateText = new Runnable() {
            @Override
            public void run() {
                String currentTime = sdf.format(new Date());
                timestamp.setText(currentTime);
                timestamp.postDelayed(this, 1000);
            }
        };
        timestamp.post(updateText);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentRecording != null) {
            stopRecording();
        }
    }
}