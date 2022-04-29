package com.team8.mcq_scanner.app.adapters;


import static com.team8.mcq_scanner.app.managers.Constants.EXTRA_IMGPATH;
import static com.team8.mcq_scanner.app.managers.Constants.REF_TASKS;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.team8.mcq_scanner.app.OpenCvScanner;
import com.team8.mcq_scanner.app.R;
import com.team8.mcq_scanner.app.TestMaker;
import com.team8.mcq_scanner.app.managers.Utills;
import com.team8.mcq_scanner.app.models.Image;
import com.team8.mcq_scanner.app.models.Test;
import com.team8.mcq_scanner.app.models.Tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Objects;

public class TestMakerAdapters extends RecyclerView.Adapter<TestMakerAdapters.ViewHolder> {

    private final Context mContext;
    private final ArrayList<Test> mUsers;
    private final boolean isChat;
    private final FirebaseUser firebaseUser;
    private String theLastMsg, txtLastDate;
    private boolean isMsgSeen = false;
    private int unReadCount = 0;
    private DatabaseReference reference;
    private static final String TAF = "OpenCvActivity";
    private static List<Integer> idList;
    private static String[] AnswerArrayList;
    private static List[] IdArray;
    private static TestMaker.getAnswerList getAnswerList;

    public TestMakerAdapters(Context mContext, ArrayList<Test> usersList, boolean isChat, TestMaker.getAnswerList getAnswerListCallback) {
        this.mContext = mContext;
        this.mUsers = Utills.removeDuplicates(usersList);
        this.isChat = isChat;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        AnswerArrayList = new String[usersList.size()];
        IdArray = new List[usersList.size()];
        this.getAnswerList = getAnswerListCallback;
    }
    // TODO create TestMakeAdapter
    // Image
    // Test Instructions
    // Test

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.list_test, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @SuppressLint("RecyclerView") int i) {
        final Test user = mUsers.get(i);

        Log.d(TAF,user.getquestion());
        viewHolder.txtUsername.setText(user.getquestion().trim());

        int id = 123546;
        ArrayList<String> ch = user.getChoice();
        idList = new ArrayList<>();
        for (int j = 0; j < ch.size(); j++) {

            RadioButton rb = new RadioButton(mContext);
            rb.setText(ch.get(j));
            rb.setId(id);
            idList.add(id);
            viewHolder.ChoiceGrp.addView(rb, new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT));
            id += 14;
        }
        IdArray[i] = idList;
        viewHolder.ChoiceGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (IdArray[i].contains(checkedId)){
                    AnswerArrayList[i] = ch.get(idList.indexOf(checkedId));
                }
            }
        });
        getAnswerList.answerListCallback(AnswerArrayList);
    }
    

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView txtUsername;
        private RadioGroup ChoiceGrp;
        private Button Submit,Clear;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtUsername = itemView.findViewById(R.id.Question);
            ChoiceGrp = itemView.findViewById(R.id.ChoiceGrp);
            Submit = itemView.findViewById(R.id.SubmitButton);
            Clear = itemView.findViewById(R.id.ClearButton);
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

}
