package com.makepe.curiosityhubls;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.makepe.curiosityhubls.Privacy.BlockedActivity;

public class RegisterActivity extends AppCompatActivity {

    CountryCodePicker codePicker;
    EditText phone_Number;
    Button btnRegister;
    ProgressBar progressDialog;
    CheckBox termsCheck;

    FirebaseUser firebaseUser;

    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        codePicker = findViewById(R.id.code_Picker);
        phone_Number = findViewById(R.id.phone_Number);
        btnRegister = findViewById(R.id.signUpBtn);
        progressDialog = findViewById(R.id.signUpProgress);
        termsCheck = findViewById(R.id.terms_conditions);

        try {
            Intent reIntent = getIntent();
            final String changeNum = reIntent.getExtras().getString("change_number");
            if (changeNum.equals("yes")) {
                progressDialog.setVisibility(View.GONE);
            }
        }catch (NullPointerException ignored){}

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobile;
                final boolean acceptTerms = termsCheck.isChecked();

                mobile = phone_Number.getText().toString().trim();
                codePicker.registerCarrierNumberEditText(phone_Number);

                String number = codePicker.getFullNumberWithPlus();//store number with + apprehended at the beginning of the number

                if(mobile.isEmpty() || mobile.length()< 8 ){
                    phone_Number.setError("Enter a valid number");
                    phone_Number.requestFocus();
                    return;
                }else if(!acceptTerms) {
                    Toast.makeText(RegisterActivity.this, "Please Accept The Terms", Toast.LENGTH_SHORT).show();
                }else{
                    progressDialog.setVisibility(View.VISIBLE);
                    btnRegister.setVisibility(View.GONE);
                    Intent intent = new Intent(RegisterActivity.this, VerifyPhoneActivity.class);
                    intent.putExtra("number", number);//parse number to the next activity for processing
                    startActivity(intent);
                }

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}