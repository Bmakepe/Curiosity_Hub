package com.makepe.curiosityhubls;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.makepe.curiosityhubls.Models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    EditText profileName, schoolNameET, levelGrade;
    EditText bioDescription;
    CircleImageView proPic;
    RelativeLayout changePic;
    TextView showMore, dateOfBirth;
    Dialog showMoreEditsDialog;
    CheckBox maleSelection, femaleSelection, otherSelection;
    Spinner gradeSpinner, districtSpinner;
    String gradeSelected, districtSelected;

    DatabaseReference ref;
    FirebaseUser user;
    StorageReference storageReference;
    ProgressDialog progressDialog;

    Uri proPicUri;
    StorageTask uploadTask;
    String myUri = "", profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_layout);

        profileName = findViewById(R.id.fullName);
        bioDescription = findViewById(R.id.edit_bio);
        proPic = findViewById(R.id.userImage);
        changePic = findViewById(R.id.changePicArea);
        showMore = findViewById(R.id.moreEdits);

        progressDialog = new ProgressDialog(this);
        iniMoreEditsDialog();

        user = FirebaseAuth.getInstance().getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference("Students").child(user.getUid());
        storageReference = FirebaseStorage.getInstance().getReference("StudentProfileImgs");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                assert user != null;
                profileName.setText(user.getName());
                bioDescription.setText(user.getBio());
                profilePic = user.getProfileImg();

                try{
                    Picasso.get().load(user.getProfileImg()).into(proPic);
                }catch (NullPointerException ignored){}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        changePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setAspectRatio(1,1)
                        .start(EditProfileActivity.this);
            }
        });

        findViewById(R.id.doneBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateUserDetails(user);

            }
        });

        findViewById(R.id.editBackBTN).setOnClickListener(view -> finish());

        showMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreEditsDialog.show();
            }
        });
    }

    private void iniMoreEditsDialog() {
        showMoreEditsDialog = new Dialog(this);
        showMoreEditsDialog.setContentView(R.layout.more_profile_edits);
        showMoreEditsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        showMoreEditsDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        showMoreEditsDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        gradeSpinner = showMoreEditsDialog.findViewById(R.id.editGrades);
        dateOfBirth = showMoreEditsDialog.findViewById(R.id.dateOfBirth);
        maleSelection = showMoreEditsDialog.findViewById(R.id.maleSelection);
        femaleSelection = showMoreEditsDialog.findViewById(R.id.femaleSelection);
        schoolNameET = showMoreEditsDialog.findViewById(R.id.schoolNameET);
        otherSelection = showMoreEditsDialog.findViewById(R.id.otherSelection);
        districtSpinner = showMoreEditsDialog.findViewById(R.id.editDistrict);

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getDetails(firebaseUser);
        selectGender();
        gradeSpinner.setOnItemSelectedListener(this);
        districtSpinner.setOnItemSelectedListener(this);

        final Calendar myCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                //function for picking the DOB and updating the label to show the DOB picked
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateDOB(myCalendar, dateOfBirth);

            }
        };

        dateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(EditProfileActivity.this,  date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();

            }
        });

        showMoreEditsDialog.findViewById(R.id.finishBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateUserDetails(firebaseUser);

                showMoreEditsDialog.dismiss();
            }
        });

        showMoreEditsDialog.findViewById(R.id.moreEditBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreEditsDialog.dismiss();
            }
        });
    }

    private void selectGender() {
        maleSelection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                femaleSelection.setChecked(false);
                otherSelection.setChecked(false);
            }
        });

        femaleSelection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                maleSelection.setChecked(false);
                otherSelection.setChecked(false);
            }
        });

        otherSelection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                maleSelection.setChecked(false);
                femaleSelection.setChecked(false);
            }
        });
    }

    private void updateDOB(Calendar myCalendar, TextView dateOfBirth) {
        //function to update the DOB label with the date you picked

        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        dateOfBirth.setText("" + sdf.format(myCalendar.getTime()));
    }

    private void getDetails(FirebaseUser firebaseUser) {

        assert firebaseUser != null;
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Students").child(firebaseUser.getUid());

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                assert user != null;
                schoolNameET.setText(user.getSchool());
                dateOfBirth.setText(user.getDateOfBirth());

                if(user.getGender().equals("Male")){
                    maleSelection.setChecked(true);
                }else if(user.getGender().equals("Female")){
                    femaleSelection.setChecked(true);
                }else {
                    otherSelection.setChecked(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateUserDetails(FirebaseUser user) {

        String name = profileName.getText().toString().trim();
        String bio = bioDescription.getText().toString().trim();
        String schoolName = schoolNameET.getText().toString().trim();
        String DOB = dateOfBirth.getText().toString().trim();
        final boolean maleChecked = maleSelection.isChecked();
        final boolean femaleChecked = femaleSelection.isChecked();
        final boolean otherSelectionChecked = otherSelection.isChecked();

        if(name.isEmpty()){

            profileName.setError("Enter Your Name Here");
            profileName.requestFocus();

        }else if(schoolName.isEmpty()){

            schoolNameET.setError("Enter The Name of The School Here");
            schoolNameET.requestFocus();

        }else if(bio.isEmpty()){

            bioDescription.setError("Tell Us About Yourself");
            bioDescription.requestFocus();

        }else if(!maleChecked && !femaleChecked && !otherSelectionChecked){
            Toast.makeText(this, "Please select a valid gender", Toast.LENGTH_SHORT).show();
        }else if(DOB.isEmpty()){
            Toast.makeText(this, "Please pick your date of birth", Toast.LENGTH_SHORT).show();
        }else if(!validateDistrict() | !validateGrade()){
            Toast.makeText(this, "Please ensure you have selected both the district and grade", Toast.LENGTH_SHORT).show();
        }else{
            progressDialog.setMessage("Updating Your Profile");
            progressDialog.show();

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Students");

            if(myUri != null) {
                final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                        + "." + getFileExtension(proPicUri));

                uploadTask = fileReference.putFile(proPicUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return fileReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            myUri = downloadUri.toString();

                            StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(profilePic);
                            picRef.delete();

                            HashMap<String, Object> result = new HashMap<>();
                            result.put("name", name);
                            result.put("bio", bio);
                            result.put("profileImg", myUri);
                            result.put("school", schoolName);
                            result.put("dateOfBirth", DOB);
                            result.put("grade", gradeSelected);
                            result.put("district", gradeSelected);

                            if (maleChecked) {
                                result.put("gender", "Male");
                            } else if (femaleChecked) {
                                result.put("gender", "Female");
                            } else if (otherSelectionChecked) {
                                result.put("gender", "Other");
                            }

                            ref.child(user.getUid()).updateChildren(result)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(EditProfileActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                                            profileName.setText("");
                                            finish();
                                            progressDialog.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }else{
                HashMap<String, Object> result = new HashMap<>();
                result.put("name", name);
                result.put("bio", bio);
                result.put("profileImg", profilePic);
                result.put("school", schoolName);
                result.put("dateOfBirth", DOB);
                result.put("grade", gradeSelected);

                if (maleChecked) {
                    result.put("gender", "Male");
                } else if (femaleChecked) {
                    result.put("gender", "Female");
                } else if (otherSelectionChecked) {
                    result.put("gender", "Other");
                }

                ref.child(user.getUid()).updateChildren(result)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(EditProfileActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                                profileName.setText("");
                                finish();
                                progressDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private String getFileExtension(Uri uri){
        try {
            ContentResolver contentResolver = getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getExtensionFromMimeType(contentResolver.getType(uri));
        }catch (NullPointerException ignored){}
        return null;
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            proPicUri = result.getUri();

            proPic.setImageURI(proPicUri);

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

        gradeSelected = adapterView.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}