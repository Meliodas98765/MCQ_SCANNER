package com.team8.mcq_scanner.app.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.team8.mcq_scanner.app.R;
import com.team8.mcq_scanner.app.managers.Utills;
import com.team8.mcq_scanner.app.models.Image;

import java.util.ArrayList;

public class UserAdapters extends RecyclerView.Adapter<UserAdapters.ViewHolder> {

    private final Context mContext;
    private final ArrayList<Image> mUsers;
    private final boolean isChat;
    private final FirebaseUser firebaseUser;
    private String theLastMsg, txtLastDate;
    private boolean isMsgSeen = false;
    private int unReadCount = 0;

    public UserAdapters(Context mContext, ArrayList<Image> usersList, boolean isChat) {
        this.mContext = mContext;
        this.mUsers = Utills.removeDuplicates(usersList);
        this.isChat = isChat;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_home, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Image user = mUsers.get(i);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);


        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}
