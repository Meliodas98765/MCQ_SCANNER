package com.team8.mcq_scanner.app.adapters;


import static com.team8.mcq_scanner.app.managers.Constants.EXTRA_IMGPATH;
import static com.team8.mcq_scanner.app.managers.Constants.REF_TASKS;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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
import com.team8.mcq_scanner.app.managers.Utills;
import com.team8.mcq_scanner.app.models.Image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class HomeAdapters extends RecyclerView.Adapter<HomeAdapters.ViewHolder> {

    private final Context mContext;
    private final ArrayList<Image> mUsers;
    private final boolean isChat;
    private final FirebaseUser firebaseUser;
    private String theLastMsg, txtLastDate;
    private final boolean isMsgSeen = false;
    private final int unReadCount = 0;
    private DatabaseReference reference;
    private static final String TAF = "OpenCvActivity";

    public HomeAdapters(Context mContext, ArrayList<Image> usersList, boolean isChat) {
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

        viewHolder.txtUsername.setText(user.getImgName());
        Utills.setProfileImage(mContext,user.getImgUri(),viewHolder.imageView);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utills.openFullImageViewActivity(v,user.getImgUri(),user.getImgName(),mContext);
            }
        });
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                createPopupMenu(viewHolder,v,user);
                return true;
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

    private void createPopupMenu(ViewHolder viewHolder, View v, Image user) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), viewHolder.moreMenu);
        popupMenu.inflate(R.menu.home_img_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final int id = item.getItemId();
                switch (id){
                    case R.id.navigation_create_test:
                        //createTestTask(user);
                        //Toast.makeText(mContext, "here", Toast.LENGTH_SHORT).show();
                        getTestNameAlertDialog(user,viewHolder);
                        //Toast.makeText(mContext, "Bitmap Created Successfully", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.navigation_view:
                        Utills.openFullImageViewActivity(v,user.getImgUri(),user.getImgName(),mContext);
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

    private void getTestNameAlertDialog(Image user, ViewHolder viewHolder) {
        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(mContext);
        builder.setTitle("Test Name:");

        // set the custom layout
        final View customLayout
                = LayoutInflater.from(mContext)
                .inflate(
                        R.layout.ald_test_name,
                        null);
        builder.setView(customLayout);

        // add a button
        builder
                .setPositiveButton(
                        "Create",
                        new DialogInterface.OnClickListener() {

                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which)
                            {

                                // send data from the
                                // AlertDialog to the Activity
                                EditText editText
                                        = customLayout
                                        .findViewById(
                                                R.id.editText);
                                String TestName = editText.getText().toString();
                                if (!TestName.isEmpty()) {
                                    OpenCvScanner openCvScanner = new OpenCvScanner(mContext, editText.getText().toString());
                                }else {
                                    OpenCvScanner openCvScanner = new OpenCvScanner(mContext, "Test-"+user.getImgName());
                                }
                                try {
                                    OpenCvScanner.getBitmapFromUri(user.getImgUri(),mContext.getContentResolver(), user.getImgId(),viewHolder.textLayout,viewHolder.Running);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Log.d(TAF,"Bitmap Created successfully");
                            }
                        });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which)
                    {

                        // send data from the
                        // AlertDialog to the Activity
                        Log.d(TAF,"Create Cancelled");
                    }
                });
        // create and show
        // the alert dialog
        AlertDialog dialog
                = builder.create();
        dialog.show();
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
        private final RelativeLayout textLayout;
        private final RelativeLayout Running;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtUsername = itemView.findViewById(R.id.Image_name);
            imageView = itemView.findViewById(R.id.Image);
            moreMenu = itemView.findViewById(R.id.MoreMenu);
            moreMenuLayout = itemView.findViewById(R.id.moremenulayout);
            textLayout = itemView.findViewById(R.id.textLayout);
            Running = itemView.findViewById(R.id.Running);
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}
