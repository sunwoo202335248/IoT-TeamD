package com.example.pushup01;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.media.Image;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.*;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private PoseDetector poseDetector;
    private ExecutorService cameraExecutor;
    private int pushUpCount = 0;
    private boolean isDown = false;
    private TextView counterView;
    private PreviewView previewView;

    private PoseOverlayView poseOverlayView;

    private float count = 0;
    private int direction = 0; // 0: 내려가는 중, 1: 올라가는 중
    private int form = 0;
    private String feedback = "";

    private TextView feedbackView;


    private long lastCountTime = 0; // 디바운스용 타임스탬프

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        counterView = findViewById(R.id.counterTextView);
        previewView = findViewById(R.id.previewView);
        poseOverlayView = findViewById(R.id.poseOverlay);
        feedbackView = findViewById(R.id.feedbackTextView);



        AccuratePoseDetectorOptions options =
                new AccuratePoseDetectorOptions.Builder()
                        .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
                        .build();

        poseDetector = PoseDetection.getClient(options);
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1001);
        } else {
            startCamera(); // 권한 있을 때만 호출
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    try {
                        @SuppressWarnings("UnsafeOptInUsageError")
                        Image mediaImage = imageProxy.getImage();
                        if (mediaImage != null) {
                            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                            poseDetector.process(image)
                                    .addOnSuccessListener(pose -> {

                                        poseOverlayView.setPose(pose, image.getWidth(), image.getHeight());

                                        // 랜드마크 가져오기
                                        PoseLandmark lShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
                                        PoseLandmark lElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
                                        PoseLandmark lWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
                                        PoseLandmark lHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
                                        PoseLandmark lKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);

                                        if (lShoulder != null && lElbow != null && lWrist != null && lHip != null) {
                                            // Angle 계산
                                            float elbow = findAngle(lShoulder, lElbow, lWrist);      // 11,13,15
                                            float shoulder = findAngle(lElbow, lShoulder, lHip);     // 13,11,23
                                            float hip = findAngle(lShoulder, lHip, lKnee); // 11,23,25

                                            // 보간 (elbow 기준)
                                            float per = map(elbow, 90, 160, 0, 100);        // 푸시업 진행률
                                            float bar = map(elbow, 90, 160, 380, 50);       // 진행 바 시각화 용

                                            Log.d("PoseDebug", "elbow=" + elbow + ", shoulder=" + shoulder + ", hip=" + hip + ", per=" + per);

                                            // 시작 폼 체크
                                            if (elbow > 160 && shoulder > 40 && hip > 160) {
                                                form = 1;
                                            }

                                            // 본격 푸시업 카운트
                                            if (form == 1) {
                                                if (per <= -10) {
                                                    if (elbow <= 90 && hip > 160) {
                                                        feedback = "Up";
                                                        if (direction == 0) {
                                                            count += 0.5;
                                                            direction = 1;
                                                        }
                                                    } else {
                                                        feedback = "Fix Form";
                                                    }
                                                }

                                                if (per >= 110) {
                                                    if (elbow > 160 && shoulder > 40 && hip > 160) {
                                                        feedback = "Down";
                                                        if (direction == 1) {
                                                            count += 0.5;
                                                            direction = 0;
                                                        }
                                                    } else {
                                                        feedback = "Fix Form";
                                                    }
                                                }
                                            }
                                            Log.e("counts", "counts:"+count);

                                            // UI 업데이트
                                            runOnUiThread(() -> {
                                                counterView.setText("Pushups: " + (int)count);
                                                feedbackView.setText(feedback);
                                            });
                                        } else {
                                            Log.w("PoseDetection", "필요한 랜드마크 일부 누락");
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("PoseDetection", "Failed: " + e.getMessage()))
                                    .addOnCompleteListener(task -> imageProxy.close());

                        } else {
                            imageProxy.close();
                        }
                    } catch (Exception e) {
                        imageProxy.close();
                        Log.e("Analyzer", "Exception in analyzer: " + e.getMessage());
                    }
                });

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (Exception e) {
                Log.e("CameraX", "Camera start failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (poseDetector != null) {
            poseDetector.close();
        }
        cameraExecutor.shutdown();
    }

    private float findAngle(PoseLandmark firstPoint, PoseLandmark midPoint, PoseLandmark lastPoint) {
        float x1 = firstPoint.getPosition().x;
        float y1 = firstPoint.getPosition().y;
        float x2 = midPoint.getPosition().x;
        float y2 = midPoint.getPosition().y;
        float x3 = lastPoint.getPosition().x;
        float y3 = lastPoint.getPosition().y;

        double angle = Math.toDegrees(
                Math.atan2(y3 - y2, x3 - x2) - Math.atan2(y1 - y2, x1 - x2)
        );

        angle = Math.abs(angle);
        if (angle > 180) {
            angle = 360 - angle;
        }
        return (float) angle;
    }

    // 선형 보간 함수 (Python의 np.interp와 동일)
    private float map(float value, float inMin, float inMax, float outMin, float outMax) {
        return outMin + ((value - inMin) * (outMax - outMin)) / (inMax - inMin);
    }

}


//reference
// https://aryanvij02.medium.com/push-ups-with-python-mediapipe-open-a544bd9b4351