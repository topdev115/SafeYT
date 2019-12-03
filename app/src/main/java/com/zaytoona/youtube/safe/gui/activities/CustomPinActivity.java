package com.zaytoona.youtube.safe.gui.activities;

import android.app.ProgressDialog;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;
import com.zaytoona.pincode.managers.AppLockActivity;
import com.zaytoona.pincode.managers.LockManager;
import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.common.Constants;
import com.zaytoona.youtube.safe.common.General;
import com.zaytoona.youtube.safe.gui.businessobjects.SafetoonsMaterialDialog;

import java.util.concurrent.TimeUnit;

public class CustomPinActivity extends AppLockActivity {

    private LockManager<CustomPinActivity> mLockManager;

    private String phoneVerificationId;
    private String number;
    private FirebaseAuth mAuth;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            verificationCallbacks;

    private PhoneAuthProvider.ForceResendingToken resendToken;

    private ProgressDialog mProgressDialog;

    @Override
    public void showForgotDialog() {

        mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setTitle(getString(R.string.pin_code_forget_resetting));
        mProgressDialog.setMessage(null);
        mProgressDialog.setCancelable(false);

        View customView = getLayoutInflater().inflate(R.layout.activity_custom_pin, null, false);

        final EditText phoneText = customView.findViewById(R.id.phoneText);
        final EditText codeText = customView.findViewById(R.id.codeText);;
        final Button sendButton = customView.findViewById(R.id.sendButton);
        final CountryCodePicker ccp = customView.findViewById(R.id.ccp);

        ccp.registerCarrierNumberEditText(phoneText);

        mAuth = FirebaseAuth.getInstance();

        mLockManager = LockManager.getInstance();

        MaterialDialog.Builder builder = new SafetoonsMaterialDialog(this)
                .title(R.string.pin_code_forget_dialog_title)
                .positiveText(R.string.pin_code_forget_verify_code_button)
                .customView(customView, false)

                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // This is overridden below

                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });

        final MaterialDialog dialog = builder.build();
        dialog.show();

        final MDButton positiveButton = dialog.getActionButton(DialogAction.POSITIVE);

        positiveButton.setEnabled(false);

        positiveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String code = codeText.getText().toString();

                if(code == null || code.trim().equals("")) {
                    Toast.makeText(CustomPinActivity.this, R.string.pin_code_forget_valid_verification_code_msg, Toast.LENGTH_LONG).show();
                    return;
                }

                mProgressDialog.show();

                PhoneAuthCredential credential =
                        PhoneAuthProvider.getCredential(phoneVerificationId, code);

                signInWithCredential(credential, dialog);
            }
        });

        verificationCallbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {

                        // This is when the SMS is read automatically
                        // or the user already logged in to the app (unlikely scenario in our case)
                        signInWithCredential(credential, dialog);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            Toast.makeText(CustomPinActivity.this, R.string.pin_code_forget_invalid_credentials_msg, Toast.LENGTH_LONG).show();
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // SMS quota exceeded
                            Toast.makeText(CustomPinActivity.this, R.string.pin_code_forget_sms_quota_exceeded_msg, Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(CustomPinActivity.this, R.string.pin_code_forget_verification_failed_msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken token) {

                        phoneVerificationId = verificationId;
                        resendToken = token;
                        Toast.makeText(CustomPinActivity.this, R.string.pin_code_forget_verification_code_sent_msg, Toast.LENGTH_LONG).show();
                        sendButton.setText(R.string.pin_code_forget_resend_code_btn_text);
                        positiveButton.setEnabled(true);
                    }
                };

        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                number = ccp.getFullNumberWithPlus();

                if (ccp.isValidFullNumber() == false){
                    Toast.makeText(CustomPinActivity.this, R.string.pin_code_forget_invalid_phone_number_msg, Toast.LENGTH_LONG).show();
                    return;
                }
                PhoneAuthProvider.getInstance().verifyPhoneNumber(number,        // Phone number to verify
                        60,                 // Timeout duration
                        TimeUnit.SECONDS,   // Unit of timeout
                        CustomPinActivity.this,               // Activity (for callback binding)
                        verificationCallbacks);
            }

        });
    }

    @Override
    public void onPinFailure(int attempts) {

    }

    @Override
    public void onPinSuccess(int attempts) {

    }

    @Override
    public int getPinLength() {
        return super.getPinLength();//you can override this method to change the pin length from the default 4
    }

    private void signInWithCredential(PhoneAuthCredential credential, final MaterialDialog dialog) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(CustomPinActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            String phoneNumber = user.getPhoneNumber();

                            mAuth.getCurrentUser().
                                    unlink(PhoneAuthProvider.PROVIDER_ID)
                                    .addOnCompleteListener(CustomPinActivity.this, new OnCompleteListener<AuthResult>() {

                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {

                                            String deviceId = General.getDeviceId(CustomPinActivity.this);

                                            FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_DATABASE_TABLE_DEVICE_USERS).child(deviceId).setValue(null);

                                            if (mLockManager.getAppLock().isPasscodeSet()) {
                                                mLockManager.getAppLock().setPasscode(null);
                                            }

                                            mAuth.signOut();

                                            dialog.dismiss();

                                            // Remove all other activities & go to root (KidMainActivity)
                                            finishAffinity();

                                            mProgressDialog.dismiss();

                                            finish();

                                        }
                                    });
                        } else {
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {

                                // The verification code entered was invalid
                                mProgressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), R.string.pin_code_forget_verification_code_invalid_msg, Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                });
    }
}
