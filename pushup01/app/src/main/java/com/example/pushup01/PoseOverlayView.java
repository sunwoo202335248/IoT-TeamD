package com.example.pushup01;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

public class PoseOverlayView extends View {

    private Pose pose;
    private int imageWidth = 1;
    private int imageHeight = 1;

    private Paint landmarkPaint;

    public PoseOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        landmarkPaint = new Paint();
        landmarkPaint.setColor(0xFFFF0000); // 빨간색
        landmarkPaint.setStyle(Paint.Style.FILL);
        landmarkPaint.setStrokeWidth(8f);
    }

    public void setPose(Pose pose, int imageWidth, int imageHeight) {
        this.pose = pose;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        invalidate(); // 다시 그리기 요청
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (pose == null) return;

        float scaleX = getWidth() / (float) imageWidth;
        float scaleY = getHeight() / (float) imageHeight;

        // 여기서 오프셋을 조절해 보세요
        float offsetX = 230f;  // 오른쪽으로 10px 이동 (음수면 왼쪽)
        float offsetY = -275f; // 위로 15px 이동 (음수면 위)

        canvas.translate(offsetX, offsetY);

        for (PoseLandmark landmark : pose.getAllPoseLandmarks()) {
            float x = landmark.getPosition().x * scaleX;
            float y = landmark.getPosition().y * scaleY;
            canvas.drawCircle(x, y, 8f, landmarkPaint);
        }
    }
}
