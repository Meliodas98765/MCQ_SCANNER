package com.team8.mcq_scanner.app;

import static com.team8.mcq_scanner.app.managers.Constants.REF_USERS;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.team8.mcq_scanner.app.models.User;

import java.util.Objects;

public class LoginActivity extends BaseActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private EditText mTxtEmail;
    private EditText mTxtPassword;
    private boolean firstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mActivity = this;

        mTxtEmail = findViewById(R.id.txtEmail);
        mTxtPassword = findViewById(R.id.txtPassword);
        final Button mBtnSignUp = findViewById(R.id.btnSignUp);
        final TextView mTxtNewUser = findViewById(R.id.txtNewUser);

        auth = FirebaseAuth.getInstance();

        mBtnSignUp.setOnClickListener(this);
        mTxtNewUser.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.btnSignUp) {
            String strEmail = mTxtEmail.getText().toString().trim();
            String strPassword = mTxtPassword.getText().toString().trim();

            if (TextUtils.isEmpty(strEmail) || TextUtils.isEmpty(strPassword)) {
                Toast.makeText(mActivity, "All fields required", Toast.LENGTH_SHORT).show();
            } else {
                login(strEmail, strPassword);
            }
        } else if (id == R.id.txtNewUser) {
            Intent intent = new Intent(this,RegisterActivity.class);
            startActivity(intent);
        }
    }

    private void login(String email, String password) {
        showProgress();

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                hideProgress();
                if (task.isSuccessful()) {
                    String currentID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                    checkIfAccountIsActive(currentID);
                } else {
                    Toast.makeText(mActivity, "Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgress();
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                hideProgress();
            }
        });
    }

    private void checkIfAccountIsActive(String currentID) {
        Query q = FirebaseDatabase.getInstance().getReference(REF_USERS);
        q.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()){
                    User saf;
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.hasChildren()){
                        saf = snapshot.child(currentID).getValue(User.class);
                        assert saf != null;
                        final Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            }
        });
    }
    public static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)){
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()){
                return false;
            }
            return true;
        }else {
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()){
                return true;
            }
            return false;
        }
    }
    @Override
    public void onFocusChange(View view, boolean b) {
        final int id = view.getId();

        if (id == R.id.txtEmail){
            firstTime = false;
            mTxtEmail.setHint("#example@domain.com");
            mTxtPassword.setHint("");
        }else if (id == R.id.txtPassword){
            if (!firstTime) {
                if (!isValidEmail(mTxtEmail.getText().toString())) {
                    mTxtEmail.setError("Invalid Email");
                }
            }firstTime = false;
            mTxtEmail.setHint("");
            mTxtPassword.setHint("*************");
        }
    }
}

