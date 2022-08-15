package com.makepe.curiosityhubls.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makepe.curiosityhubls.Models.Schools;
import com.makepe.curiosityhubls.R;
import com.makepe.curiosityhubls.ViewProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SchoolsAdapter extends RecyclerView.Adapter<SchoolsAdapter.ViewHolder> {

    Context context;
    List<Schools> schoolsList;

    public SchoolsAdapter(Context context, List<Schools> schoolsList) {
        this.context = context;
        this.schoolsList = schoolsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.schools_layout, parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Schools schools = schoolsList.get(position);

        String area = schools.getArea();
        String desc = schools.getDescription();
        String imgPro = schools.getImgProfile();
        String schoolName = schools.getSchool();
        String district = schools.getLocation();
        String receiverID = schools.getSchool_ID();

        holder.schoolName.setText(schoolName);
        holder.schoolArea.setText(area);
        holder.schoolDistrict.setText(district);

        try{
            Picasso.get().load(imgPro).into(holder.school_logo);
        }catch (Exception e){
            Toast.makeText(context, "Could not retrieve school picture", Toast.LENGTH_SHORT).show();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ViewProfileActivity.class);
                intent.putExtra("uid", receiverID);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return schoolsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView school_logo;
        TextView schoolDistrict, schoolLocation, schoolName, schoolArea;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            school_logo = itemView.findViewById(R.id.school_logo);
            schoolName = itemView.findViewById(R.id.schoolName);
            schoolDistrict = itemView.findViewById(R.id.SchoolDistrict);
            schoolArea = itemView.findViewById(R.id.schoolArea);

        }
    }
}
