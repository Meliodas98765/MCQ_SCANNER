package com.team8.mcq_scanner.app.adapters;


import static com.team8.mcq_scanner.app.managers.Constants.EXTRA_IMGPATH;
import static com.team8.mcq_scanner.app.managers.Constants.REF_TASKS;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
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
import com.google.firebase.database.FirebaseDatabase;
import com.team8.mcq_scanner.app.R;
import com.team8.mcq_scanner.app.managers.Utills;
import com.team8.mcq_scanner.app.models.Image;
import com.team8.mcq_scanner.app.models.Tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.ViewHolder> {

    private final Context mContext;
    private final ArrayList<Tasks> mUsers;
    private final boolean isChat;
    private final FirebaseUser firebaseUser;
    private String theLastMsg, txtLastDate;
    private boolean isMsgSeen = false;
    private int unReadCount = 0;
    private DatabaseReference reference;

    public TasksAdapter(Context mContext, ArrayList<Tasks> usersList, boolean isChat) {
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
        final Tasks user = mUsers.get(i);

        viewHolder.txtUsername.setText(user.getImgName());
        Utills.setProfileImage(mContext,user.getImgUri(),viewHolder.imageView);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Creating Test for "+user.getImgName(), Toast.LENGTH_SHORT).show();
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

    private void createPopupMenu(ViewHolder viewHolder, View v, Tasks user) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), viewHolder.moreMenu);
        popupMenu.inflate(R.menu.tasks_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final int id = item.getItemId();
                switch (id){
                    case R.id.navigation_delete:
                        // TODO delete TASKS
                        deleteTask(user);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void deleteTask(Tasks user) {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(REF_TASKS);
        reference1.child(user.getImgId()).setValue("");
    }

    private void createTestTask(Image user) {
        ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Creating..");
        progressDialog.show();
        reference = FirebaseDatabase.getInstance().getReference(REF_TASKS);
        HashMap<String,String> hashMap = new HashMap<>();

        String id = reference.push().getKey();
        assert id != null;
        hashMap.put("imgId",id);
        hashMap.put(EXTRA_IMGPATH, user.getImgUri());
        hashMap.put("userId", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        hashMap.put("createdAt", Utills.getDateTime());

        reference.child(id).setValue(hashMap);
        progressDialog.dismiss();
    }

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
