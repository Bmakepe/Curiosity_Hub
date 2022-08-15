package com.makepe.curiosityhubls.CustomClasses;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class Permissions {
    Context context;

    private final String[] requiredPermissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    public static final int PERMISSIONS_MULTIPLE_REQUEST = 123;

    public Permissions(Context context) {
        this.context = context;
    }

    public void verifyPermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            ActivityCompat.requestPermissions((Activity) context, requiredPermissions, PERMISSIONS_MULTIPLE_REQUEST);
        }else{
            Toast.makeText(context, "Permissions Checked", Toast.LENGTH_SHORT).show();
        }
    }
}
