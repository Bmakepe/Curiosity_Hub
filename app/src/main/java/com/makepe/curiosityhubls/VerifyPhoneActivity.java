package com.makepe.curiosityhubls;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyPhoneActivity extends AppCompatActivity {

    EditText otpED;
    Button verifyOTP_BTN;
    TextView resendOTP, changeNum, subHeading;
    FirebaseAuth mAuth;
    ProgressDialog verify;
    private String mVerificationId;

    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        otpED = findViewById(R.id.otpEdtTxt);
        verifyOTP_BTN = findViewById(R.id.verifyBtn);
        changeNum = findViewById(R.id.change_number);
        resendOTP = findViewById(R.id.resendOTP);
        subHeading = findViewById(R.id.subHeading);

        mAuth = FirebaseAuth.getInstance();
        verify = new ProgressDialog(this);

        Intent intent = getIntent();
        final String number = intent.getExtras().getString("number");//retrive number from previous activity

        subHeading.setText("We are automatically detecting a SMS send to your mobile number " + number);
        sendVerificationCode(number);//send the verification code to number which has been entered

        verifyOTP_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = otpED.getText().toString().trim();//get verification code from edit text

                if(code.isEmpty()||code.length() < 6){
                    otpED.setError("Enter valid code");
                    otpED.requestFocus();
                    return;
                }
                verifyOTP_BTN.setVisibility(View.GONE);
                //pd.setVisibility(View.VISIBLE);

                verifyVerificationCode(code);// verify verification code

                Intent intent = new Intent(VerifyPhoneActivity.this, ProfileBuildActivity.class);
                intent.putExtra("phoneNumber", number);//parse number to next activity for storage in database
                Toast.makeText(VerifyPhoneActivity.this, number + " is the number that has been registered and stored for parsing", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });

        resendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//if OTP has not been send, click to generate another code
                Toast.makeText(VerifyPhoneActivity.this, "Your OTP will be sent again", Toast.LENGTH_SHORT).show();
                resendOTP(number, mResendToken);//function to generate another OTP
            }
        });

        changeNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VerifyPhoneActivity.this, RegisterActivity.class);
                intent.putExtra("change_number", "yes");//parse number the next activity for processing
                startActivity(intent);
                finish();
            }
        });
    }

    private void resendOTP(String number, PhoneAuthProvider.ForceResendingToken token) {//resend OTP function
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks,
                token
        );
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            //get verification automatically as the sms comes in and verify
            String code = phoneAuthCredential.getSmsCode();

            if(code != null){
                otpED.setText(code);
                verifyVerificationCode(code);
            }

        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {

            //toast error message if number can not be verified
            Toast.makeText(VerifyPhoneActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            //this is for resending the generated verification code
            mVerificationId = s;
            mResendToken = forceResendingToken;

        }
    };

    private void verifyVerificationCode(String code) {
        //function to verify the code that has been send

        verify.setMessage("Verifying... Please Wait...");
        verify.show();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerifyPhoneActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){//executes when the verification code is correct
                            Intent intent = new Intent(VerifyPhoneActivity.this, ProfileBuildActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }else{
                            String message = "Something is wrong, we will fix it soon...";

                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                message = "Invalid code entered";
                            }

                            /*Snackbar snackbar = Snackbar.make(findViewById(R.id.parent), message, Snackbar.LENGTH_LONG);
                            snackbar.setAction("Dismiss", new View.OnClickListener(){
                                @Override
                                public void onClick(View v) {

                                }
                            });
                            snackbar.show();*/
                        }
                        verify.dismiss();
                    }
                });
    }

    private void sendVerificationCode(String number) {

        //function to send the verification code
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks
        );
    }
}