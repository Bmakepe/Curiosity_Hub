package com.makepe.curiosityhubls;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.makepe.curiosityhubls.CustomClasses.Permissions;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileBuildActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "ProfileBuildActivity";
    private Button skipBtn, nextBtn;
    private TextView title, userAge;
    private int getYear;
    private String date;
    private CircleImageView std_pic;

    TextInputLayout fullName, biography;
    RadioGroup userGender;
    RadioButton selectedGender;

    Uri imageURI;
    String myUri = "";
    StorageTask uploadTask;

    FirebaseUser firebaseUser;
    StorageReference storageReference;
    DatabaseReference reference;

    DatePickerDialog.OnDateSetListener dateSetListener;
    Permissions permissions;

    ProgressDialog pd;

    Spinner roleSpinner;
    String roleSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_build);

        skipBtn = findViewById(R.id.skip_btn);
        nextBtn = findViewById(R.id.next_btn);
        title = findViewById(R.id.profile_title);
        fullName = findViewById(R.id.user_names);
        userGender = findViewById(R.id.gender_inputs);
        userAge = findViewById(R.id.date);
        std_pic = findViewById(R.id.proPic);
        biography = findViewById(R.id.user_bio);
        roleSpinner = findViewById(R.id.roleSpinner);
        roleSpinner.setOnItemSelectedListener(this);

        permissions = new Permissions(this);
        permissions.verifyPermissions();

        pd = new ProgressDialog(this);

        reference = FirebaseDatabase.getInstance().getReference("Students");
        storageReference = FirebaseStorage.getInstance().getReference("StudentProfileImgs");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        std_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .start(ProfileBuildActivity.this);
            }
        });

        userAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        ProfileBuildActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "onDateSet: date:" + month +"/" + day + "/" + year);

                date = month + "/" + day + "/" + year;
                getYear = year;
                userAge.setText(date);
            }
        };

    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)  {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            assert result != null;
            imageURI = result.getUri();

            std_pic.setImageURI(imageURI);
        }else{
            Toast.makeText(this, "Allow permission to proceed", Toast.LENGTH_SHORT).show();
        }
    }

    public void callNextSignUpScreen(View view) {

        if (!validateName() | !validateGender() | !validateAge() | !validateBio() | !validateRole()){
            return;
        }else{
            pd.setMessage("Loading...");
            pd.show();

            selectedGender = findViewById(userGender.getCheckedRadioButtonId());
            String _gender = selectedGender.getText().toString();

            if(imageURI != null){
                final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                + "." + getFileExtension(imageURI));

                uploadTask = fileReference.putFile(imageURI);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        return fileReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri downloadUri = task.getResult();
                            myUri = downloadUri.toString();

                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("name", fullName.getEditText().getText().toString().trim());
                            hashMap.put("gender", _gender);
                            hashMap.put("dateOfBirth", date);
                            hashMap.put("profileImg", myUri);
                            hashMap.put("userId", firebaseUser.getUid());
                            hashMap.put("bio", biography.getEditText().getText().toString().trim());
                            hashMap.put("grade", "");
                            hashMap.put("phoneNumber", firebaseUser.getPhoneNumber());
                            hashMap.put("school", "");
                            hashMap.put("role", roleSelected);
                            hashMap.put("district", "");

                            reference.child(firebaseUser.getUid()).setValue(hashMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            pd.dismiss();
                                            startActivity(new Intent(getApplicationContext(), ProfileContinuationActivity.class));
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ProfileBuildActivity.this, "Error Setting Up Profile", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileBuildActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }else{
                HashMap<Object, String> hashMap = new HashMap<>();
                hashMap.put("name", fullName.getEditText().getText().toString().trim());
                hashMap.put("gender", _gender);
                hashMap.put("dateOfBirth", date);
                hashMap.put("profileImg", "Default");
                hashMap.put("userId", firebaseUser.getUid());
                hashMap.put("bio", biography.getEditText().getText().toString().trim());
                hashMap.put("phoneNumber", firebaseUser.getPhoneNumber());
                hashMap.put("grade", "");
                hashMap.put("school", "");
                hashMap.put("role", roleSelected);

                reference.child(firebaseUser.getUid()).setValue(hashMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                pd.dismiss();
                                Toast.makeText(ProfileBuildActivity.this, "Successfully setup profile", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), ProfileContinuationActivity.class));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileBuildActivity.this, "Error Setting Up Profile", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }
        // Add Transition
        Pair[] pairs = new Pair[2];

        pairs[0] = new Pair<View, String>(skipBtn, "transition_btn_skip");
        pairs[1] = new Pair<View, String>(nextBtn, "transition_btn_next");

    }

    // Validations functions
    private boolean validateName() {
        String val = fullName.getEditText().getText().toString().trim();

        if (val.isEmpty()) {
            fullName.setError("Full Names are required");
            return false;
        } else {
            fullName.setError(null);
            fullName.setErrorEnabled(false);
            return true;
        }
    }

    // Validations functions
    private boolean validateRole() {
        if(roleSpinner.getSelectedItem().toString().trim().equals("-----Select Your Role-----")){
            Toast.makeText(this, "Please select your role", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateBio() {
        String val = biography.getEditText().getText().toString().trim();

        if (val.isEmpty()) {
            biography.setError("Tell Us More About Yourself");
            return false;
        } else {
            biography.setError(null);
            biography.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateGender() {

        if (userGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please Select Gender", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private boolean validateAge() {

        int current = Calendar.getInstance().get(Calendar.YEAR);
        int userAge = getYear;
        int isAgeValid = current - userAge;

        if (isAgeValid < 8){
            Toast.makeText(this, "Age is not Appropriate", Toast.LENGTH_SHORT).show();
            return false;
        }else {
            return true;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        roleSelected = adapterView.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}