package com.example.facedetctor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView PickImage;
    Button chooseButton;
    public static int Pic_choose = 190;
    Bitmap bitmap;
    Bitmap mutableBitmap;
    Canvas canvas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PickImage = findViewById(R.id.faceImage);
        chooseButton = findViewById(R.id.buttonChooser);

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imgIntent = new Intent();
                imgIntent.setType("image/*");
                imgIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(imgIntent, Pic_choose);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Pic_choose) {
            PickImage.setImageURI(data.getData());

        }
        FirebaseVisionImage image;
        try {

            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
            mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            canvas = new Canvas(mutableBitmap);

            image = FirebaseVisionImage.fromFilePath(getApplicationContext(), data.getData());
            FirebaseVisionFaceDetectorOptions highAccuracyOpts =
                    new FirebaseVisionFaceDetectorOptions.Builder()
                            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                            .build();
            FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(highAccuracyOpts);
            Task<List<FirebaseVisionFace>> result =
                    detector.detectInImage(image)
                            .addOnSuccessListener(
                                    new OnSuccessListener<List<FirebaseVisionFace>>() {
                                        @Override
                                        public void onSuccess(List<FirebaseVisionFace> faces) {
                                            // Task completed successfully
                                            // ...
                                            for (FirebaseVisionFace face : faces) {
                                                Rect bounds = face.getBoundingBox();
                                                Paint paint = new Paint();
                                                paint.setColor(Color.RED);
                                                paint.setStyle(Paint.Style.STROKE);
                                                canvas.drawRect(bounds, paint);
                                                PickImage.setImageBitmap(mutableBitmap);

                                                // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                                // nose available):
                                                FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
                                                if (leftEar != null) {
                                                    FirebaseVisionPoint leftEarPos = leftEar.getPosition();
                                                    Rect rectL = new Rect((int) (leftEarPos.getX() - 30),
                                                            (int) (leftEarPos.getY() - 30), (int) (leftEarPos.getX() + 30),
                                                            (int) (leftEarPos.getY() + 30));
                                                    canvas.drawRect(rectL, paint);
                                                    PickImage.setImageBitmap(mutableBitmap);
                                                }

                                                // If contour detection was enabled:
                                                // If classification was enabled:
                                                Paint paint1=new Paint();
                                                paint1.setColor(Color.WHITE);
                                                paint1.setTextSize(20);
                                                if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                                    float smileProb = face.getSmilingProbability();
                                                    if (smileProb > 0.5) {
                                                        canvas.drawText("Smiling", bounds.exactCenterY(), bounds.exactCenterX(), paint1);
                                                        PickImage.setImageBitmap(mutableBitmap);
                                                    }else{
                                                        canvas.drawText("Not  Smiling", bounds.exactCenterY(), bounds.exactCenterX(), paint1);
                                                        PickImage.setImageBitmap(mutableBitmap);
                                                    }

                                                }
                                                if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                                    float rightEyeOpenProb = face.getRightEyeOpenProbability();
                                                    if (rightEyeOpenProb>0.5){
                                                        canvas.drawText("Open Right Eye",bounds.exactCenterX(),bounds.exactCenterY(),paint1);
                                                        PickImage.setImageBitmap(mutableBitmap);
                                                    }else{
                                                        canvas.drawText("Not Open Right Eye",bounds.exactCenterX(),bounds.exactCenterY(),paint1);
                                                        PickImage.setImageBitmap(mutableBitmap);
                                                    }
                                                }

                                                // If face tracking was enabled:
                                                if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                                                    int id = face.getTrackingId();
                                                }
                                            }
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                        }
                                    });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}