package com.makepe.curiosityhubls.Privacy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.curiosityhubls.R;

public class BlockedActivity extends AppCompatActivity {

    TextView cautionMessage;
    ImageView backBTN;

    DatabaseReference cautions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked);

        cautionMessage = findViewById(R.id.cautionMessage);
        backBTN = findViewById(R.id.cautionBackBTN);

        cautions = FirebaseDatabase.getInstance().getReference("Announcements").child("Cautions");
        cautions.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String message = "" + snapshot.child("cautionMessage").getValue();

                    cautionMessage.setText(message);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}