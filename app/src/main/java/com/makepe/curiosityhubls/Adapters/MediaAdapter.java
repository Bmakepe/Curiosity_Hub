package com.makepe.curiosityhubls.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.makepe.curiosityhubls.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    Context context;
    ArrayList<String> mediaList;
    Uri picUri;

    public MediaAdapter(Context context, ArrayList<String> mediaList) {
        this.context = context;
        this.mediaList = mediaList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.media_item, null, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        picUri = Uri.parse(mediaList.get(position));
        Picasso.get().load(picUri).into(holder.mMedia);

        inifullScreenDialog(holder, position, picUri);

        holder.removeBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "You are removing item at position " + position, Toast.LENGTH_SHORT).show();
                mediaList.remove(position);
                MediaAdapter.this.notifyDataSetChanged();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "You have clicked on the item at position " + position, Toast.LENGTH_SHORT).show();
                holder.fullScreenDialog.show();
            }
        });
    }

    private void inifullScreenDialog(ViewHolder holder, int position, Uri uriPic) {
        holder.fullScreenDialog = new Dialog(context);
        holder.fullScreenDialog.setContentView(R.layout.fullscreen_image_view);
        holder.fullScreenDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        holder.fullScreenDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        holder.fullScreenDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        ImageView fullScreenPic = holder.fullScreenDialog.findViewById(R.id.fullScreenPic);

        Picasso.get().load(uriPic).into(fullScreenPic);

        holder.fullScreenDialog.findViewById(R.id.fullScreenBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.fullScreenDialog.dismiss();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mMedia, removeBTN;
        Dialog fullScreenDialog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mMedia = itemView.findViewById(R.id.media);
            removeBTN = itemView.findViewById(R.id.removeItem);
        }
    }
}
