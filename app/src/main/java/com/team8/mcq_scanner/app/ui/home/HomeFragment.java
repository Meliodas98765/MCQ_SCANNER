package com.team8.mcq_scanner.app.ui.home;

import static com.team8.mcq_scanner.app.managers.Constants.EXTRA_IMGPATH;
import static com.team8.mcq_scanner.app.managers.Constants.REF_IMAGES;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.team8.mcq_scanner.app.OpenCvScanner;
import com.team8.mcq_scanner.app.R;
import com.team8.mcq_scanner.app.adapters.HomeAdapters;
import com.team8.mcq_scanner.app.databinding.FragmentHomeBinding;
import com.team8.mcq_scanner.app.managers.Utills;
import com.team8.mcq_scanner.app.models.Image;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private DatabaseReference reference;
    private ArrayList<Image> imageArrayList;
    private HomeAdapters homeAdapters;
    private FirebaseAuth auth;
    private Uri imgUri;
    private File fileUri;
    private StorageTask uploadTask;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private LinearLayoutManager layoutManager;
    private static String TAF = "HomeFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final RecyclerView textView = binding.rv;
        final FloatingActionButton fab = binding.fab;
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference(REF_IMAGES);

        reference = FirebaseDatabase.getInstance().getReference(REF_IMAGES);
        reference.keepSynced(true);
        imageArrayList = new ArrayList<>();
        textView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        textView.setLayoutManager(layoutManager);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChildren()){
                    imageArrayList = new ArrayList<>();
                    for (DataSnapshot datasnapshot :
                            snapshot.getChildren()) {
                        Image user = datasnapshot.getValue(Image.class);
                        imageArrayList.add(user);
                    }
                }
                if (!imageArrayList.isEmpty()){
                    homeAdapters = new HomeAdapters(getContext(),imageArrayList,true);
                    textView.setAdapter(homeAdapters);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intentLauncher.launch(intent);
    }

    final ActivityResultLauncher<Intent> intentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                if (fileUri != null) { // Image Capture
                    imgUri = Uri.fromFile(fileUri);
                } else { // Pick from Gallery
                    Intent data = result.getData();
                    assert data != null;
                    imgUri = data.getData();
                    if (uploadTask != null && uploadTask.isInProgress()) {
                        Toast.makeText(getContext(), R.string.msgUploadInProgress, Toast.LENGTH_SHORT).show();
                    } else {
                        getImageNameAlertDialog();
                    }
                }
            }
        }
    });

    private void getImageNameAlertDialog() {
        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(getContext());
        builder.setTitle("Image Name:");

        // set the custom layout
        final View customLayout
                = LayoutInflater.from(getContext())
                .inflate(
                        R.layout.ald_test_name,
                        null);
        builder.setView(customLayout);

        // add a button
        builder
                .setPositiveButton(
                        "Upload",
                        new DialogInterface.OnClickListener() {

                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {

                                // send data from the
                                // AlertDialog to the Activity
                                EditText editText
                                        = customLayout
                                        .findViewById(
                                                R.id.editText);
                                String ImageName = editText.getText().toString();
                                if (!ImageName.isEmpty()) {
                                    uploadImage(editText.getText().toString()+ "." + Utills.getExtension(getContext(), imgUri));
                                }else{
                                    uploadImage("Image-" + System.currentTimeMillis() + "." + Utills.getExtension(getContext(), imgUri));
                                }
                            }
                        });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {

                        // send data from the
                        // AlertDialog to the Activity
                        Log.d(TAF, "Image Upload Cancelled");
                    }
                });
        // create and show
        // the alert dialog
        AlertDialog dialog
                = builder.create();
        dialog.show();

    }

    private void uploadImage(String s) {
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage(getString(R.string.msg_image_upload));
        pd.show();
        final StorageReference fileReference;
        if (imgUri != null) {
            String fileName = s;
            fileReference = storageReference.child(fileName);

            uploadTask = fileReference.putFile(imgUri);

            uploadTask
                    .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            return fileReference.getDownloadUrl();
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                final Uri downloadUri = task.getResult();
                                final String mUrl = downloadUri.toString();

                                updateDatabase(mUrl,fileName);
                            } else {
                                Toast.makeText(getContext(), "Failed to Upload Image", Toast.LENGTH_SHORT).show();
                            }
                            pd.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Utills.getErrors(e);
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(getContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDatabase(String mUrl, String fileName) {
        reference = FirebaseDatabase.getInstance().getReference(REF_IMAGES);

        String id = reference.push().getKey();
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put(EXTRA_IMGPATH,mUrl);
        hashMap.put("imgId",id);
        hashMap.put("userId", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        hashMap.put("imgName", fileName);
        hashMap.put("createdAt", Utills.getDateTime());

        reference.child(id).setValue(hashMap);
    }
}