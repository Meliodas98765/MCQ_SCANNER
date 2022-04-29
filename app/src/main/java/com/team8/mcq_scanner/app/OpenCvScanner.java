package com.team8.mcq_scanner.app;

import static androidx.core.app.ActivityCompat.requestPermissions;
import static com.team8.mcq_scanner.app.managers.Constants.EXTRA_CHOICES;
import static com.team8.mcq_scanner.app.managers.Constants.EXTRA_CREATED_AT;
import static com.team8.mcq_scanner.app.managers.Constants.EXTRA_KEY;
import static com.team8.mcq_scanner.app.managers.Constants.EXTRA_QUESTION;
import static com.team8.mcq_scanner.app.managers.Constants.EXTRA_TEST_NAME;
import static com.team8.mcq_scanner.app.managers.Constants.REF_QNA;
import static com.team8.mcq_scanner.app.managers.Constants.REF_TESTS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import com.team8.mcq_scanner.app.managers.Utills;
import com.team8.mcq_scanner.app.models.Tests;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class OpenCvScanner {

    // Get GrayScale Image
    // Gaussian Blur
    // adaptiveThreshold
    // getStructuringElements
    // dilate
    // findContours

    private static Bitmap grayBitmap, bitmap;
    private static final String TAF = "OpenCvActivity";
    private static final int INTERNET_REQUEST_CODE = 3452;
    private static Context mContext;
    private static String testName;
    private static StorageReference storageRef = FirebaseStorage.getInstance().getReference(),islandRef;

    public OpenCvScanner(Context context, String text) {
        this.mContext = context;
        this.testName = text;
    }

    private static void getPermissions(Activity context) {
        String[] permission =
                {
                        Manifest.permission.INTERNET,
                        Manifest.permission.WRITE_SETTINGS
                };
        for (String permissions :
                permission) {
            if (ActivityCompat.checkSelfPermission(context, permissions)
                    != PackageManager.PERMISSION_DENIED) {
                try {
                    requestPermissions(context, new String[]{permissions}, INTERNET_REQUEST_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // TODO Convert URI to Bitmap
    public static void getBitmapFromUri(String uri, ContentResolver contentResolver, String imgId, RelativeLayout textLayout, RelativeLayout running) throws IOException {
        //Toast.makeText(mContext, "IN", Toast.LENGTH_SHORT).show();
        getPermissions((Activity) mContext);
        if (Settings.System.canWrite(mContext) == false) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + mContext.getPackageName()));
            mContext.startActivity(intent);
        }
        LoadBitMapFromUrl fromUrl = new LoadBitMapFromUrl(imgId,running,textLayout);


        fromUrl.execute(uri);
        if (fromUrl.getStatus() == AsyncTask.Status.PENDING) {
            //Toast.makeText(mContext, "Not Started", Toast.LENGTH_SHORT).show();
        } else if (fromUrl.getStatus() == AsyncTask.Status.RUNNING) {
            //Toast.makeText(mContext, "Running", Toast.LENGTH_SHORT).show();
        } else if (fromUrl.getStatus() == AsyncTask.Status.FINISHED) {
            try {
                bitmap = fromUrl.get();
                if ( bitmap!= null) {
                    getSubImages(bitmap, mContext, imgId, running, textLayout);
                } else {
                    Toast.makeText(mContext, "Bitmap Null", Toast.LENGTH_SHORT).show();
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
    private static class LoadBitMapFromUrl extends AsyncTask<String, Void, Bitmap> {

        private Bitmap myBitmap = null;
        private static String imgId;
        @SuppressLint("StaticFieldLeak")
        private static RelativeLayout r1,r2;
        public LoadBitMapFromUrl(String imgId, RelativeLayout running, RelativeLayout textLayout) {
            LoadBitMapFromUrl.imgId = imgId;
            r1 = running;
            r2 = textLayout;
        }

        @Override
        protected void onPreExecute() {
            r1.setVisibility(View.VISIBLE);
            r2.setVisibility(View.GONE);
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                InputStream inputStream = new java.net.URL(urls[0]).openStream();
                myBitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bitmap = myBitmap;
            return myBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap unused) {
            //Toast.makeText(mContext, "Loading Completed", Toast.LENGTH_SHORT).show();
            if (myBitmap != null) {
                getSubImages(myBitmap, mContext, imgId,r1,r2);
            } else {
                Toast.makeText(mContext, "Bitmap Null", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(unused);
        }
    }
    private static void getImageFromURL(String fileName) throws IOException {
        islandRef = storageRef.child("images/"+fileName);

        File localFile = File.createTempFile("images", "jpg");
        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

    }
    // TODO getSubImages
    public static void getSubImages(Bitmap bitmap, Context context, String imgId, RelativeLayout r1, RelativeLayout r2){

        Mat Rgba = new Mat();
        Mat grayMat = new Mat();

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inDither = false;
        o.inSampleSize = 4;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        grayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

//        Utils.bitmapToMat(bitmap, Rgba);
//        Imgproc.cvtColor(Rgba, grayMat, Imgproc.COLOR_RGB2GRAY);
//        Imgproc.GaussianBlur(grayMat, grayMat, new Size(9,9),0);
//        Imgproc.adaptiveThreshold(grayMat, grayMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 15, 30);
//        Mat kernal = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(10,10));
//        Imgproc.dilate(grayMat, grayMat, kernal);
//        Utils.matToBitmap(grayMat,grayBitmap);
//        Log.d(TAF,"Image Processed..");
//        Log.d(TAF,"Creating Task");
//        Toast.makeText(context, "Image Processed", Toast.LENGTH_SHORT).show();
//        Toast.makeText(context, "Creating Task", Toast.LENGTH_SHORT).show();
        createTest(bitmap,imgId,r1,r2);
//        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(grayMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//        hierarchy.release();
//
//        int ROI_number = 0;
//
//
//        for (MatOfPoint mat :
//                contours) {
//            double area = Imgproc.contourArea(mat);
//            if (area > 100){
//                Rect rect = Imgproc.boundingRect(mat);
//                Bitmap ROI = Bitmap.createBitmap(rect.width,rect.height, Bitmap.Config.ARGB_8888);
//                Canvas canvas = new Canvas(ROI);
//                Paint paint = new Paint();
//                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//                canvas.drawBitmap(bitmap,0,0,null);
//                canvas.drawRect(rect.x, -rect.y, rect.x+rect.width, -rect.y-rect.height, paint);
//                // TODO extract text from ROI and append to choices or questions
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        //getStringFromROI(ROI, ROI_number, context,imgId);
//                        String text = getStringFromROIStandAlone(ROI,ROI_number);
//
//                        if (text != null) {
//                            Log.d(TAF, text);
//                        }else{
//                            Log.d(TAF, "NO text");
//                        }
//                        if (ROI_number % 2 == 0){
//
//                        }
//                    }
//                },100);
//            }
//        }
    }

    private static void createTest1(Bitmap grayBitmap,String imgId) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(grayBitmap);
        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance().getCloudTextRecognizer();
        textRecognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                String resultText = firebaseVisionText.getText();
                StringBuilder choices = new StringBuilder();
                StringBuilder questions = new StringBuilder();
                HashMap<String, String> hashMap = new HashMap<>();
                String key = FirebaseDatabase.getInstance().getReference().push().getKey();

                hashMap.put(EXTRA_TEST_NAME, "TEST");
                hashMap.put(EXTRA_CREATED_AT, Utills.getDateTime());
                hashMap.put(EXTRA_KEY, key);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }
    private static void createTest(Bitmap grayBitmap, String imgId, RelativeLayout r1, RelativeLayout r2) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        InputImage image = InputImage.fromBitmap(grayBitmap,0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Log.d(TAF,"Recognizer Initialized");
        //Toast.makeText(mContext, "Recognizer Initialized", Toast.LENGTH_SHORT).show();

        Task<Text> task = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                Log.d(TAF, "Text Recognized successfully");
                //Toast.makeText(mContext, "Text Recognized successfully", Toast.LENGTH_SHORT).show();

                StringBuilder choices = new StringBuilder();
                StringBuilder questions = new StringBuilder();
                HashMap<String, String> hashMap = new HashMap<>();
                String key = FirebaseDatabase.getInstance().getReference().push().getKey();
                //Toast.makeText(mContext, text.getText(), Toast.LENGTH_SHORT).show();
                hashMap.put(EXTRA_TEST_NAME, testName);
                hashMap.put(EXTRA_CREATED_AT, Utills.getDateTime());
                hashMap.put(EXTRA_KEY, key);
                int ROI_number = 0;
                for (Text.TextBlock block :
                        text.getTextBlocks()) {
                    if (ROI_number % 2 != 0) {
                        StringBuilder aLinee = new StringBuilder();
                        for (Text.Line line :
                                block.getLines()) {
                            String aLine = line.getText();
                            Log.d(TAF,aLine+"#########");

                            aLinee.append(userId).append(aLine);

                            Log.d(TAF,aLine);
                            //aLinee.append(userId).append(aLine);
                        }
                        choices.append(key).append(aLinee);
                    } else {
                        questions.append(key).append(block.getText());
                    }
                    ROI_number += 1;

                }
                hashMap.put(EXTRA_CHOICES, choices.toString());
                hashMap.put(EXTRA_QUESTION, questions.toString());
                uploadTest(hashMap, imgId, userId,r1,r2);
                Log.d(TAF, "Waiting 100 ms");
                //Toast.makeText(mContext, "Wait 100 ms", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    private static String getStringFromROIStandAlone(Bitmap ROI, int ROI_number){
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        InputImage image = InputImage.fromBitmap(ROI,0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        final String[] ROItext = {""};
        Task<Text> task = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (ROI_number % 2 == 0){
                            String aLinee = "";
                            for (Text.TextBlock block :
                                    text.getTextBlocks()) {
                                for (Text.Line line :
                                        block.getLines()) {
                                    String aLine = line.getText();
                                    if (aLine.split(" ")[0].split("\\)")[0].matches("\\d+")){
                                        aLinee = aLinee + userId + aLine;
                                    }else{
                                        aLinee = aLinee +" "+ aLine;
                                    }
                                }
                            }
                            ROItext[0] = aLinee;
                        }else {
                            ROItext[0] = text.getText();
                        }
                    }
                }, 100);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
        return ROItext[0];
    }

    private static void getStringFromROI(Bitmap ROI, int ROI_number, Context context,String imgId) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(ROI);
        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance().getCloudTextRecognizer();
        textRecognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                String resultText = firebaseVisionText.getText();
                List<String> choices = new ArrayList<String>();
                List<String> questions = new ArrayList<String>();
                Tests tests = new Tests();

                if (ROI_number % 2  == 0){
                    for (FirebaseVisionText.TextBlock block :
                            firebaseVisionText.getTextBlocks()) {
                        String blockText = block.getText();
                        Float blockConfidence = block.getConfidence();

                        for (FirebaseVisionText.Line line :
                                block.getLines()) {
                            String lineText = line.getText();
                            Float lineConfidence = line.getConfidence();
                            choices.add(lineText);
                        }
                    }
                }else {
                    questions.add(resultText);
                }

//                tests.setAnswers(choices);
//                tests.setQuestions(questions);
//                uploadTest(tests,imgId,userId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }
    private static void uploadTest(HashMap<String,String> hashMap,HashMap<String,HashMap<String,String>> hashMapHashMap,String imgId, String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(REF_QNA).child(userId).child(imgId);

        Log.d(TAF,"Test Uploading..");
        //Toast.makeText(mContext, "Test Uploading..", Toast.LENGTH_SHORT).show();
        reference.setValue(hashMap);
        reference.child(REF_TESTS).setValue(hashMapHashMap);
        Log.d(TAF,"Test Uploaded successfully");
        Toast.makeText(mContext, "Test Uploaded Successfully", Toast.LENGTH_SHORT).show();
    }

    // TODO Upload Data Properly
    private static void uploadTest(HashMap<String, String> hashMap, String imgId, String userId, RelativeLayout r1, RelativeLayout r2) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(REF_QNA).child(userId).child(imgId);
        Log.d(TAF,"Test Uploading..");
        //Toast.makeText(mContext, "Test Uploading..", Toast.LENGTH_SHORT).show();
        reference.setValue(hashMap);
        Log.d(TAF,"Test Uploaded successfully");
        Toast.makeText(mContext, "Test Uploaded Successfully", Toast.LENGTH_SHORT).show();
        r1.setVisibility(View.GONE);
        r2.setVisibility(View.VISIBLE);
    }
//    private static void uploadTest(Tests tests,String imgId, String userId) {
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(REF_QNA).child(userId).child(imgId);
//        String key = reference.push().getKey();
//
//        HashMap<String,String> hashMap = new HashMap<>();
//        hashMap.put(EXTRA_CREATED_AT, Utills.getDateTime());
//        hashMap.put(EXTRA_TEST_NAME,tests.getTestName());
//        hashMap.put(EXTRA_KEY,key);
//        reference.setValue(hashMap);
//        for (int i = 0; i < tests.getQuestions().size(); i++) {
//            HashMap<String,String> hashMap1 = new HashMap<>();
//            hashMap1.put(EXTRA_QUESTION,tests.getQuestions().get(i));
//            StringBuilder answers = new StringBuilder();
//            for (int j = 0; j < tests.getAnswers().size(); j++) {
//                answers.append(key).append(tests.getAnswers().get(j));
//            }
//            hashMap1.put(EXTRA_CHOICES, answers.toString());
//            hashMap1.put(EXTRA_QUESTION, tests.getQuestions().get(i));
//            reference.child(REF_TESTS).child(String.valueOf(i+1)).setValue(hashMap1);
//        }
//    }
}
