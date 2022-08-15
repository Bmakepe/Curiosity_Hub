package com.makepe.curiosityhubls;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.util.HashMap;
import java.util.Objects;

public class ProfileContinuationActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final String TAG = "ProfileBuildActivity";

    Button skipBtn, nextBtn;
    TextView title;
    ImageView previous;
    Spinner gradeSpinner, districtSpinner;

    TextInputLayout std_school;

    FirebaseUser user;
    DatabaseReference ref;
    private ProgressDialog progressDialog;

    String gradeSelected, districtSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_continuation);

        skipBtn = findViewById(R.id.skip_btn);
        nextBtn = findViewById(R.id.finish_btn);
        title = findViewById(R.id.profile_title);
        previous = findViewById(R.id.previous_screen);
        std_school = findViewById(R.id.std_school);
        gradeSpinner = findViewById(R.id.gradeSpinner);
        districtSpinner = findViewById(R.id.districtSpinner);
        gradeSpinner.setOnItemSelectedListener(this);
        districtSpinner.setOnItemSelectedListener(this);

        progressDialog = new ProgressDialog(this);
        user = FirebaseAuth.getInstance().getCurrentUser();

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userDetailsUpload();
            }
        });
    }

    public void callPreviousSignUpScreen(View view) {

        if (!validateSchool()){
            return;
        }

        Intent intent = new Intent(getApplicationContext(), ProfileBuildActivity.class);

        // Add Transition
        Pair[] pairs = new Pair[2];

        pairs[0] = new Pair<View, String>(skipBtn, "transition_btn_skip");
        pairs[1] = new Pair<View, String>(nextBtn, "transition_btn_next");
        //pairs[3] = new Pair<View, String>(title, "transition_title_text");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(ProfileContinuationActivity.this, pairs);
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }

    }

    public void userDetailsUpload (){
        if(!validateSchool() | !validateDistrict() | !validateGrade()){
            return;
        }else{

            progressDialog.setMessage("Please Wait");
            progressDialog.show();

            ref = FirebaseDatabase.getInstance().getReference("Students");

            HashMap<String, Object> result = new HashMap<>();
            result.put("school", Objects.requireNonNull(std_school.getEditText()).getText().toString().trim());
            result.put("grade", gradeSelected);
            result.put("district", districtSelected);

            ref.child(user.getUid()).updateChildren(result)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            startActivity(new Intent(ProfileContinuationActivity.this, MainActivity.class));
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(ProfileContinuationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Validations functions
    private boolean validateSchool() {
        String val = std_school.getEditText().getText().toString().trim();

        if (val.isEmpty()) {
            std_school.setError("School cannot be empty");
            return false;
        } else {
            std_school.setError(null);
            std_school.setErrorEnabled(false);
            return true;
        }
    }

    // Validations functions
    private boolean validateDistrict() {
        if(districtSpinner.getSelectedItem().toString().trim().equals("-----Select District-----")){
            Toast.makeText(this, "Please select your District", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Validations functions
    private boolean validateGrade() {
        if(gradeSpinner.getSelectedItem().toString().trim().equals("-----Select Grade Level-----")){
            Toast.makeText(this, "Please select your Grade", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        gradeSelected = adapterView.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}