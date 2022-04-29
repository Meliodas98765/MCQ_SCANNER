package com.team8.mcq_scanner.app;


import static com.team8.mcq_scanner.app.managers.Constants.EXTRA_EMAIL;
import static com.team8.mcq_scanner.app.managers.Constants.EXTRA_CREATED_AT;
import static com.team8.mcq_scanner.app.managers.Constants.EXTRA_USER_ID;
import static com.team8.mcq_scanner.app.managers.Constants.EXTRA_PASSWORD;
import static com.team8.mcq_scanner.app.managers.Constants.EXTRA_USERNAME;
import static com.team8.mcq_scanner.app.managers.Constants.EXTRA_VERSION;
import static com.team8.mcq_scanner.app.managers.Constants.REF_USERS;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.team8.mcq_scanner.app.managers.Utills;

import java.util.ArrayList;
import java.util.HashMap;

public class RegisterActivity extends BaseActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private EditText mTxtEmail, mTxtName;
    private EditText mTxtPassword;
    private EditText mTxtConfirmPassword;
    private TextView mTxtExistingUser;
    private boolean firstTimeEmail = true;
    private boolean firstTimePass = true;
    private boolean firstTimeUserName = true;
    private Intent qData = new Intent();
    private ArrayList<String> userNameQ = new ArrayList<>();
    private boolean ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mTxtName = findViewById(R.id.txtUsername);
        mTxtEmail = findViewById(R.id.txtEmail);
        mTxtPassword = findViewById(R.id.txtPassword);
        mTxtConfirmPassword = findViewById(R.id.txtConfirmPassword);
        mTxtExistingUser = findViewById(R.id.txtExistingUser);

        final Button mBtnRegister = findViewById(R.id.btnSignUp);

        mTxtEmail.setOnFocusChangeListener(this);
        mTxtPassword.setOnFocusChangeListener(this);
        mTxtConfirmPassword.setOnFocusChangeListener(this);
        mBtnRegister.setOnClickListener(this);
        mTxtExistingUser.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();

        mTxtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!isValidEmail(charSequence)) {
                    mTxtEmail.setError("Invalid Email");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mTxtEmail.requestFocus();
            }
        });

        mTxtConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void afterTextChanged(Editable editable) {
                if (isValidPass(editable.toString())) {
                    if (!editable.toString().equals(mTxtPassword.getText().toString())) {
                        mTxtConfirmPassword.setError("Password do not match");
                    } else {
                        mTxtConfirmPassword.setError(null);
                    }
                }
            }
        });
    }

    public static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()) {
                return false;
            }
            return true;
        } else {
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()) {
                return true;
            }
            return false;
        }
    }

    public static boolean isValidUserName(CharSequence target) {
        return !TextUtils.isEmpty(target);
    }

    public static boolean isValidMobileNo(CharSequence target) {
        return !TextUtils.isEmpty(target) && target.length() == 10;
    }

    public static boolean isValidPass(String target) {
//        Pattern pass_pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\\\S+$).{8,20}$");
//        Matcher pass_matcher = pass_pattern.matcher(target);
        return !TextUtils.isEmpty(target) && target.length() >= 8 && target.length() <= 20;
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.btnSignUp) {
            String strEmail = mTxtEmail.getText().toString().trim();
            String strPassword = mTxtPassword.getText().toString().trim();
            String strConfirmPassword = mTxtConfirmPassword.getText().toString().trim();
            String strName = mTxtName.getText().toString().trim();

            if (TextUtils.isEmpty(strEmail)
                    || TextUtils.isEmpty(strPassword)
                    || TextUtils.isEmpty(strConfirmPassword)
                    || TextUtils.isEmpty(strName)
            ) {
                Toast.makeText(mActivity, "All fields required", Toast.LENGTH_SHORT).show();
            } else if (!Utills.isValidEmail(strEmail)) {
                Toast.makeText(mActivity, "Invalid Email", Toast.LENGTH_SHORT).show();
            }
            //else if (!Utills.isValidUserName(strUsername)) {

            //    screens.showToast("Username already exists");
            //}
            if (isValidPass(strPassword) && isValidPass(strConfirmPassword)) {
                if (strConfirmPassword.equals(strPassword))
                    register(strEmail, strPassword, strName);
            }
        } else if (id == R.id.txtExistingUser) {
            finish();
        }
    }

    final ActivityResultLauncher<Intent> progressLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                qData = result.getData();
                assert qData != null;
                ok = qData.getBooleanExtra("ok", false);
            }
        }
    });

    private void register(final String email, final String password, String name) {
        showProgress();

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    assert firebaseUser != null;
                    String userId = firebaseUser.getUid();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(EXTRA_USER_ID, userId);
                    hashMap.put(EXTRA_EMAIL, email);
                    hashMap.put(EXTRA_USERNAME, name);
                    hashMap.put(EXTRA_PASSWORD, password);
                    hashMap.put(EXTRA_CREATED_AT, Utills.getDateTime());
                    hashMap.put(EXTRA_VERSION, BuildConfig.VERSION_NAME);

                    reference = FirebaseDatabase.getInstance().getReference(REF_USERS).child(userId);
                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            hideProgress();
                            final Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgress();
                Utills.getErrors(e);
                Toast.makeText(mActivity, "Please try again", Toast.LENGTH_SHORT).show();
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                hideProgress();
            }
        });
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        final int id = view.getId();

        if (id == R.id.txtEmail) {
            firstTimeEmail = false;
            firstTimeUserName = false;
            firstTimePass = false;
            mTxtEmail.setHint("#example@domain.com");
            mTxtPassword.setHint("");
        } else if (id == R.id.txtPassword) {
            if (!firstTimePass) {
                if (!isValidEmail(mTxtEmail.getText().toString()) && !firstTimeEmail) {
                    mTxtEmail.setError("Invalid Email");
                }
                firstTimePass = false;
                firstTimeUserName = false;
                firstTimeEmail = false;
                mTxtEmail.setHint("");
                mTxtPassword.setHint("*************");
            }
        }
    }
}
