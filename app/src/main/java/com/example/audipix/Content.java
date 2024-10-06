package com.example.audipix;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class Content extends AppCompatActivity {
    static String filePath = "";
    //Initialising Content List
    ArrayList<Object> ContentList;
    RecyclerView cardDisplayRecyclerView;
    RecycleViewAdapter recyclerViewAdapter;
    String masterDirectory;
    String folderName;

    public ActivityResultLauncher<Intent> startForCameraResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode()==RESULT_OK){
                Image image=(Image) ContentList.get(ContentList.size()-1);
                try {
                    Files.move(Paths.get(Content.filePath),Paths.get(image.masterDirectory+"/"+image.folderName+"/"+image.fileName),REPLACE_EXISTING);
                    Files.deleteIfExists(Paths.get(Content.filePath));
                    Content.filePath = "";
                    image.imageView.setVisibility(View.VISIBLE);
                    image.titleTxt.setText(image.fileName);
                    image.imageView.setImageURI(Uri.fromFile(new File(masterDirectory + "/" + folderName + "/" + image.fileName)));
                    image.capture.setVisibility(View.INVISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else{
                try {
                    Files.deleteIfExists(Paths.get(Content.filePath));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    });

    public ActivityResultLauncher<Intent> startForGalleryResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode()==RESULT_OK && result.getData()!=null){
                Image image=(Image) ContentList.get(ContentList.size()-1);
                SimpleDateFormat format = new SimpleDateFormat("HH.mm.ss", Locale.getDefault());
                String dateTime = format.format(new Date());
                String fileName=masterDirectory+"/"+folderName+"/"+"Image" + dateTime + ".png";
                image.fileName="Image" + dateTime + ".png";
                try {
                    Uri selectedImageUri = result.getData().getData();
                    String filePath = getRealPathFromURI(selectedImageUri);
                    Files.copy(Paths.get(filePath),Paths.get(fileName),REPLACE_EXISTING);
                    image.imageView.setVisibility(View.VISIBLE);
                    image.titleTxt.setText(image.fileName);
                    image.imageView.setImageURI(Uri.fromFile(new File(masterDirectory + "/" + folderName + "/" + image.fileName)));
                    image.capture.setVisibility(View.INVISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    });

    public String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    public boolean createFolder(String path){
        File dir=new File(path);
        boolean isCreated=false;
        if(!dir.exists()) {
            isCreated=dir.mkdir();
            if(isCreated)
                Toast.makeText(this, "Successful", Toast.LENGTH_SHORT).show();
        }
        return isCreated;
    }
    public boolean deleteFolder(File folder){
        try {
            boolean res=false;
            File[] files = folder.listFiles();
            if (files != null) { //some JVMs return null for empty dirs
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteFolder(f);
                    } else {
                        res=f.delete();
                    }
                }
            }
            res=folder.delete();
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Removing Title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();

        setContentView(R.layout.activity_content);

        //Initialising LinearLayout Card Content
        cardDisplayRecyclerView=findViewById(R.id.cardDisplayRecyclerView);
        cardDisplayRecyclerView.setHasFixedSize(true);
        cardDisplayRecyclerView.setLayoutManager(new LinearLayoutManager(Content.this));

        ContentList =new ArrayList<>();
        recyclerViewAdapter=new RecycleViewAdapter(Content.this, ContentList);
        cardDisplayRecyclerView.setAdapter(recyclerViewAdapter);



        //Getting the details from previous activity.
        Intent insideFolder=getIntent();
        folderName=insideFolder.getStringExtra("folderName");
        masterDirectory=insideFolder.getStringExtra("masterDirectory");

        File dir=new File(masterDirectory+"/"+folderName);
        File[] files=dir.listFiles();



        //Setting the title name as the selected folder name.
        TextView selectedFolderTextView=findViewById(R.id.folderName);
        selectedFolderTextView.setText(folderName.replace(".c1p2l3",""));

        //Setting up the back button fo going into the previous activity.
        ImageView back=findViewById(R.id.backButton);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Initialising the various modes of content creation
        ImageView mic=findViewById(R.id.mic);
        ImageView txt=findViewById(R.id.txt);
        ImageView image=findViewById(R.id.image);
        ImageView delete_all=findViewById(R.id.delete_all);

        FloatingActionButton floatingMic=findViewById(R.id.floatingMic);
        FloatingActionButton floatingImage=findViewById(R.id.floatingImage);
        FloatingActionButton floatingEdit=findViewById(R.id.floatingEdit);

        assert files != null;
        if(files.length>0){
            mic.setVisibility(View.INVISIBLE);
            txt.setVisibility(View.INVISIBLE);
            image.setVisibility(View.INVISIBLE);
            delete_all.setVisibility(View.VISIBLE);
            floatingImage.setVisibility(View.VISIBLE);
            floatingEdit.setVisibility(View.VISIBLE);
            floatingMic.setVisibility(View.VISIBLE);
            for(int i=0;i<files.length;i++) {
                String[] file=files[i].toString().split("/");
                String fileName=file[file.length-1];
                if(fileName.contains(".mp3")) {
                    ContentList.add(new Recording(true,masterDirectory,folderName,fileName));
                }
                else if(fileName.contains(".txt")){
                    ContentList.add(new Text(masterDirectory,folderName,fileName,floatingMic,floatingEdit,floatingImage));
                }
                else if (fileName.contains(".png")){
                    ContentList.add(new Image(masterDirectory,folderName,fileName,startForCameraResult,startForGalleryResult));
                }
            }
            recyclerViewAdapter.notifyDataSetChanged();

        }else{
            mic.setVisibility(View.VISIBLE);
            txt.setVisibility(View.VISIBLE);
            image.setVisibility(View.VISIBLE);
        }


        floatingMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentList.add(new Recording(false,masterDirectory,folderName,""));
                recyclerViewAdapter.notifyDataSetChanged();
                cardDisplayRecyclerView.scrollToPosition(ContentList.size()-1);
            }
        });
        floatingEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentList.add(new Text(masterDirectory,folderName,"",floatingMic,floatingEdit,floatingImage));
                recyclerViewAdapter.notifyDataSetChanged();
                cardDisplayRecyclerView.scrollToPosition(ContentList.size()-1);
            }
        });
        floatingImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentList.add(new Image(masterDirectory,folderName,"",startForCameraResult,startForGalleryResult));
                recyclerViewAdapter.notifyDataSetChanged();
                cardDisplayRecyclerView.scrollToPosition(ContentList.size()-1);
            }
        });
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Making buttons Visible and invisible
                delete_all.setVisibility(View.VISIBLE);
                txt.setVisibility(View.INVISIBLE);
                image.setVisibility(View.INVISIBLE);
                mic.setVisibility(View.INVISIBLE);
                floatingImage.setVisibility(View.VISIBLE);
                floatingEdit.setVisibility(View.VISIBLE);
                floatingMic.setVisibility(View.VISIBLE);

                ContentList.add(new Recording(false,masterDirectory,folderName,""));
                recyclerViewAdapter.notifyDataSetChanged();
                cardDisplayRecyclerView.scrollToPosition(ContentList.size()-1);

            }
        });

        txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete_all.setVisibility(View.VISIBLE);
                txt.setVisibility(View.INVISIBLE);
                image.setVisibility(View.INVISIBLE);
                mic.setVisibility(View.INVISIBLE);
                floatingImage.setVisibility(View.VISIBLE);
                floatingEdit.setVisibility(View.VISIBLE);
                floatingMic.setVisibility(View.VISIBLE);

                ContentList.add(new Text(masterDirectory,folderName,"",floatingMic,floatingEdit,floatingImage));
                recyclerViewAdapter.notifyDataSetChanged();
                cardDisplayRecyclerView.scrollToPosition(ContentList.size()-1);
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete_all.setVisibility(View.VISIBLE);
                txt.setVisibility(View.INVISIBLE);
                image.setVisibility(View.INVISIBLE);
                mic.setVisibility(View.INVISIBLE);
                floatingImage.setVisibility(View.VISIBLE);
                floatingEdit.setVisibility(View.VISIBLE);
                floatingMic.setVisibility(View.VISIBLE);


                ContentList.add(new Image(masterDirectory,folderName,"",startForCameraResult,startForGalleryResult));
                recyclerViewAdapter.notifyDataSetChanged();
                cardDisplayRecyclerView.scrollToPosition(ContentList.size()-1);
            }
        });

        delete_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert=new AlertDialog.Builder(Content.this);
                alert.setTitle("Are You Sure?")
                                .setMessage("All the data in this folder will be permanently deleted.")
                                        .setCancelable(true)
                                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        deleteFolder(new File(masterDirectory+"/"+folderName));
                                                        createFolder(masterDirectory+"/"+folderName);
                                                        ContentList.clear();
                                                        recyclerViewAdapter.notifyDataSetChanged();
                                                        delete_all.setVisibility(View.INVISIBLE);
                                                        txt.setVisibility(View.VISIBLE);
                                                        image.setVisibility(View.VISIBLE);
                                                        mic.setVisibility(View.VISIBLE);
                                                        floatingImage.setVisibility(View.INVISIBLE);
                                                        floatingEdit.setVisibility(View.INVISIBLE);
                                                        floatingMic.setVisibility(View.INVISIBLE);
                                                    }
                                                })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .show();


            }
        });
    }


}