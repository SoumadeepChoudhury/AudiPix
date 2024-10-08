package com.example.audipix;

import static androidx.core.content.ContextCompat.getExternalFilesDirs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Image{
    String masterDirectory;
    String folderName;
    String fileName;
    boolean isCamera;
    boolean isGallery;
    ActivityResultLauncher<Intent> startForCameraResult;
    ActivityResultLauncher<Intent> startForGalleryResult;
    TextView titleTxt;
    ImageView imageView;
    FloatingActionButton capture;
    AlertDialog dialog;

    public Image(String masterDirectory, String folderName, String fileName, ActivityResultLauncher<Intent> startForCameraResult,ActivityResultLauncher<Intent> startForGalleryResult) {
        this.masterDirectory = masterDirectory;
        this.folderName = folderName;
        this.startForCameraResult = startForCameraResult;
        this.startForGalleryResult = startForGalleryResult;
        this.fileName=fileName;
    }


    public void writeImage(Context ctx){
        if(this.isCamera) {
            Content.filePath = "";
            SimpleDateFormat format = new SimpleDateFormat("HH.mm.ss", Locale.getDefault());
            String dateTime = format.format(new Date());
            this.fileName="Image" + dateTime + ".png";
            try {
                File[] strDir = getExternalFilesDirs(ctx,Environment.DIRECTORY_PICTURES);
                File output = File.createTempFile(this.fileName.replace(".png",""),".png",strDir[0]);
                Uri imageUri = FileProvider.getUriForFile(ctx,"com.example.audipix.fileprovider",output);
                Intent iCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                iCamera.putExtra("file",this.masterDirectory+"/"+this.folderName+"/"+this.fileName);
                iCamera.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                Content.filePath = output.getAbsolutePath();
                startForCameraResult.launch(iCamera);
            } catch (Exception e) {
                Log.d("FILE_PATH: ",e.getLocalizedMessage());
                throw new RuntimeException(e);
            }
        }else if(this.isGallery){
            Intent iGallery=new Intent(Intent.ACTION_PICK);
            iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startForGalleryResult.launch(iGallery);
        }
    }

    public boolean deleteImage(){
        if(!this.fileName.isEmpty()) {
            File file = new File(this.masterDirectory + "/" + this.folderName + "/" + this.fileName);
            if(file.exists()){
                return file.delete();
            }
        }else{
            return true;
        }
        return false;
    }


    public void chooser(Context ctx, FloatingActionButton capture, TextView titleTxt, ImageView imageView){
        this.titleTxt=titleTxt;
        this.imageView=imageView;
        this.capture=capture;
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater=LayoutInflater.from(ctx);
                View chooserView=inflater.inflate(R.layout.chooser_alert_box_layout,null);

                LinearLayout camera=chooserView.findViewById(R.id.camera);
                LinearLayout gallery=chooserView.findViewById(R.id.gallery);

                camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Camera
                        isCamera=true;
                        writeImage(ctx);
                        dialog.cancel();
                    }
                });

                gallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Gallery
                        isGallery=true;
                        writeImage(ctx);
                        dialog.cancel();
                    }
                });

                AlertDialog.Builder chooser=new AlertDialog.Builder(ctx);
                dialog=chooser.setTitle("Choose")
                        .setView(chooserView)
                        .setCancelable(true)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .create();
                dialog.show();
            }
        });
    }
}
