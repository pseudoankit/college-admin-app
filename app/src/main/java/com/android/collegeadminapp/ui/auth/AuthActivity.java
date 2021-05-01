package com.android.collegeadminapp.ui.auth;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.collegeadminapp.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "AuthA";
    private FirebaseAuth mAuth;
    String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    EditText etPhone,etOtp;
    Button btnOtp,btnVerify;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        etOtp = findViewById(R.id.etOtp);
        etPhone = findViewById(R.id.etPhone);
        btnVerify = findViewById(R.id.btnVerify);
        btnOtp = findViewById(R.id.btnOtp);

        mAuth = FirebaseAuth.getInstance();
        initFireBaseCallbacks();

        btnOtp.setOnClickListener(view -> {
            sendOtp();
        });
        btnVerify.setOnClickListener(view -> {
            loginUser();
        });
    }

    private void loginUser() {
        String otp = etOtp.getText().toString();
        PhoneAuthCredential cd= PhoneAuthProvider.getCredential(mVerificationId,otp);
        signInWithPhoneAuthCredential(cd);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userPhoneNumber = mAuth.getCurrentUser().getPhoneNumber();
                        FirebaseUser user = task.getResult().getUser();
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                        }
                    }
                });
    }

    private void sendOtp() {
//        String phoneNumber = vb.etLoginMobileNo.getText().toString();
        String phoneNumber = "+11234567890";
        PhoneAuthOptions opt = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                .build();
        PhoneAuthProvider.verifyPhoneNumber(opt);
    }

    void initFireBaseCallbacks() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
                Toast.makeText(AuthActivity.this, "onVerificationCompleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.d(TAG, "onVerificationFailed: "+e.toString());
                Toast.makeText(AuthActivity.this, "onVerificationFailed" + e.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                mVerificationId = s;
                mResendToken = forceResendingToken;
            }
        };
    }

}
