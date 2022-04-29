package com.team8.mcq_scanner.app.models;
import java.util.ArrayList;

public class Test {
    private String testName;
    private String createdAt;
    private String question;
    private ArrayList<String> Choice;
    private String key;

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getquestion() {
        return question;
    }

    public void setquestion(String question) {
        this.question = question;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "Tests{" +
                "testName='" + testName + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", question='" + question + '\'' +
                ", key='" + key + '\'' +
                '}';
    }

    public ArrayList<String> getChoice() {
        return Choice;
    }

    public void setChoice(ArrayList<String> choice) {
        Choice = choice;
    }
}
