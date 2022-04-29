package com.team8.mcq_scanner.app.adapters;


import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.team8.mcq_scanner.app.R;
import com.team8.mcq_scanner.app.TestMaker;
import com.team8.mcq_scanner.app.managers.Utills;
import com.team8.mcq_scanner.app.models.Tests;

import java.util.ArrayList;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.ViewHolder> {

    private final Context mContext;
    private final ArrayList<Tests> mUsers;
    private final boolean isChat;
    private final FirebaseUser firebaseUser;
    private String theLastMsg, txtLastDate;
    private boolean isMsgSeen = false;
    private int unReadCount = 0;
    private DatabaseReference reference;

    public TestAdapter(Context mContext, ArrayList<Tests> usersList, boolean isChat) {
        this.mContext = mContext;
        this.mUsers = Utills.removeDuplicates(usersList);
        this.isChat = isChat;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_dashboard, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Tests user = mUsers.get(i);

        viewHolder.txtUsername.setText(user.getTestName());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, TestMaker.class);
                intent.putExtra("TestName",user.getTestName());
                intent.putExtra("TestCreatedAt",user.getCreatedAt());
                intent.putExtra("TestQuestions",user.getquestion());
                intent.putExtra("TestChoices",user.getchoices());
                intent.putExtra("TestKey",user.getKey());
                mContext.startActivity(intent);
            }
        });
        viewHolder.moreMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPopupMenu(viewHolder,v,user);
            }
        });
        viewHolder.moreMenuLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPopupMenu(viewHolder,v,user);
            }
        });
    }

    private void createPopupMenu(ViewHolder viewHolder, View v, Tests user) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), viewHolder.moreMenu);
        popupMenu.inflate(R.menu.tasks_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final int id = item.getItemId();
                switch (id){
                    case R.id.navigation_upload_answers:
                        uploadAnswers();
                        break;
                    case R.id.navigation_delete:
                        return true;
                    default:
                        return false;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void uploadAnswers() {
        Intent intent = new Intent(mContext,TestMaker.class);
        ((Activity) mContext).startActivityForResult(intent,34526);
    }

    onActivityResultFrom
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView txtUsername;
        public final ImageView imageView, moreMenu;
        private final LinearLayout moreMenuLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtUsername = itemView.findViewById(R.id.Image_name);
            imageView = itemView.findViewById(R.id.Image);
            moreMenu = itemView.findViewById(R.id.MoreMenu);
            moreMenuLayout = itemView.findViewById(R.id.moremenulayout);
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}
