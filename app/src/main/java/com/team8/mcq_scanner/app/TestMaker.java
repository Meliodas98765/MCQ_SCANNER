package com.team8.mcq_scanner.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.team8.mcq_scanner.app.adapters.TestAdapter;
import com.team8.mcq_scanner.app.adapters.TestMakerAdapters;
import com.team8.mcq_scanner.app.models.Test;
import com.team8.mcq_scanner.app.models.Tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TestMaker extends AppCompatActivity implements View.OnClickListener {

    private static ArrayList<Test> testsArrayList;
    private static String Key, currentUser;
    private static getAnswerList getAnswerList;
    private LinearLayoutManager layoutManager;
    private TestMakerAdapters testMakerAdapters;
    private String TAF = "TestMaker";
    private Button SubmitButton, ClearButton;

    @SuppressLint("UseSupportActionBar")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_maker);
        Toolbar toolbar = new Toolbar(this);
        setSupportActionBar(toolbar);

        SubmitButton = findViewById(R.id.SubmitButton);
        ClearButton = findViewById(R.id.ClearButton);
        SubmitButton.setOnClickListener(this);
        ClearButton.setOnClickListener(this);
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Intent data = getIntent();
        String createdAt = data.getStringExtra("TestCreatedAt");
        String testName = data.getStringExtra("TestName");
        String key = data.getStringExtra("TestKey");
        String[] q = data.getStringExtra("TestQuestions").split(key);
        String[] c = data.getStringExtra("TestChoices").split(key);
        TextView textHeader = findViewById(R.id.textHeader);
        textHeader.setText(testName);
        List<Test> testsList = new ArrayList<>();
        Log.d(TAF, q.length +" "+ c.length);
        for (int i = 1; i < Math.min(q.length,c.length); i++) {
            Test tests = new Test();
            tests.setTestName(testName);
            if (!q[i].isEmpty() || q[i] == null) {
                tests.setquestion(q[i]);
            }else {
                tests.setquestion("UNDETECTED QUESTION!!");
            }
            Log.d(TAF,q[i]);
            ArrayList<String> Choice = new ArrayList<>();
            String[] ch = c[i].split(currentUser);
            for (int j = 1; j < ch.length; j++) {
                Log.d(TAF,ch[j]+"---");
                if (!ch[j].isEmpty()) {
                    Choice.add(ch[j]);
                }else {
                    Choice.add("UNDETECTED OPTION");
                }
            }
            tests.setChoice(Choice);
            testsList.add(tests);
        }
        testsArrayList = new ArrayList<>();
        for (Test t :
                testsList) {

            Log.d(TAF,t.getTestName());
            Log.d(TAF,t.getquestion());
            Log.d(TAF,t.getChoice().toString());
            Optional<Test> check = Optional.ofNullable(t);
            if (check.isPresent()) {
                testsArrayList.add(t);
            }else {
                Log.d(TAF,"Skipped");
            }

        }
        final RecyclerView testRV = findViewById(R.id.testRV);
        testRV.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        testRV.setLayoutManager(layoutManager);
        testRV.setClickable(false);
        testRV.setScrollingTouchSlop(1);

        if (!testsArrayList.isEmpty()){
            testMakerAdapters = new TestMakerAdapters(this,testsArrayList,true,getAnswerList);
            testRV.setAdapter(testMakerAdapters);
        }

    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.SubmitButton){
            new getAnswerList() {
                @Override
                public void answerListCallback(String[] answerList) {
                    if (answerList.length == testsArrayList.size()){
                        uploadAnswers();
                    }else {

                    }
                }
            };
        }else if (R.id.ClearButton == id){

        }
    }


    public interface getAnswerList{
        public void answerListCallback(String[] answerList);
    }
}