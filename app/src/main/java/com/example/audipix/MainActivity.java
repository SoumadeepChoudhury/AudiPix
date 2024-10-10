package com.example.audipix;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String PERMISSION_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final int PERMISSION_REQ_CODE=100;
    final String masterDirectory = (Build.VERSION.SDK_INT>=Build.VERSION_CODES.R?Environment.getExternalStorageDirectory()+"/AudiPix":"/storage/emulated/0/AudiPix");
    String selectedFolder= masterDirectory; //pointer for current folder selected
    boolean longItemClick=false;
    boolean longItemClick_C=false;
    ArrayList<Items> listOfItems_view;
    ArrayList<Items> listOfCompletedItems_view;
    ArrayList<String> listOfFolders=new ArrayList<>();

    TextView textView;

    FloatingActionButton addSubDirectory;

    ListView listOfSubDirectory;
    ListView listOfCompletedDirectory;

    ItemAdapter itemAdapter;
    ItemAdapter completedAdapter;

    LottieAnimationView empty_folder_animation_view;


    ImageView deleteAll;

    boolean isHome=true;
    boolean isCompleted=false;
    boolean isPermitted=false;



    public boolean checkPermission(){
        boolean isGranted = false;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            isGranted=Environment.isExternalStorageManager();
        }else{
            if(ActivityCompat.checkSelfPermission(this,PERMISSION_RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,PERMISSION_CAMERA)==PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,PERMISSION_READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,PERMISSION_WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                isGranted=true;
            }
        }
        return isGranted;
    }

    @SuppressLint("SuspiciousIndentation")
    public void requestPermission(){

        if(checkPermission()){
            Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
            if(ActivityCompat.checkSelfPermission(MainActivity.this,PERMISSION_RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(MainActivity.this,PERMISSION_CAMERA)!=PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{PERMISSION_RECORD_AUDIO,PERMISSION_CAMERA},PERMISSION_REQ_CODE);
            if(new File(masterDirectory).exists()){
                listOfFolders = getFolders(masterDirectory);
            }else {
                createFolder(masterDirectory);
            }
            isPermitted=true;
        } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_RECORD_AUDIO) && !ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_CAMERA) && !ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_READ_EXTERNAL_STORAGE) && !ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_WRITE_EXTERNAL_STORAGE)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("This app requires RECORD_AUDIO, CAMERA, MANAGE_EXTERNAL_STORAGE permission for particular feature to work s expected.")
                    .setTitle("Permission Required")
                    .setCancelable(false)
                    .setPositiveButton("Ok", ((dialog, which) -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            //Android is R or above
                            try {
                                Intent intent = new Intent();
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                                intent.setData(uri);
                                storageActivityResultLauncher.launch(intent);
                            } catch (Exception e) {
                                Intent intent = new Intent();
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                storageActivityResultLauncher.launch(intent);
                            }
                        } else {
                            ActivityCompat.requestPermissions(this, new String[]{PERMISSION_RECORD_AUDIO, PERMISSION_CAMERA, PERMISSION_READ_EXTERNAL_STORAGE, PERMISSION_WRITE_EXTERNAL_STORAGE}, PERMISSION_REQ_CODE);
                        }
                    })).setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()));
            builder.show();


        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                //Android is R or above
                try {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                    intent.setData(uri);
                    storageActivityResultLauncher.launch(intent);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    storageActivityResultLauncher.launch(intent);
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{PERMISSION_RECORD_AUDIO, PERMISSION_CAMERA, PERMISSION_READ_EXTERNAL_STORAGE, PERMISSION_WRITE_EXTERNAL_STORAGE}, PERMISSION_REQ_CODE);
            }

        }

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==PERMISSION_REQ_CODE){
            if(permissions.length>=2){
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if(new File(masterDirectory).exists()){
                        listOfFolders = getFolders(masterDirectory);
                    } else if(createFolder(masterDirectory))
                        Toast.makeText(this, "Master Directory created...", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this, "Error in creating master directory. Contact the developer...", Toast.LENGTH_SHORT).show();

                    isPermitted=true;
                }
            }
        }
    }

    final private ActivityResultLauncher<Intent> storageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
                            if(Environment.isExternalStorageManager()){
                                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                                if(ActivityCompat.checkSelfPermission(MainActivity.this,PERMISSION_RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(MainActivity.this,PERMISSION_CAMERA)!=PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{PERMISSION_RECORD_AUDIO, PERMISSION_CAMERA}, PERMISSION_REQ_CODE);
                                }
                                createFolder(masterDirectory);
                                isPermitted=true;
                            }else{
                                Toast.makeText(MainActivity.this, "Permission required", Toast.LENGTH_SHORT).show();
                                isPermitted=false;
                            }
                        }else {
                            if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.MANAGE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
                                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                                createFolder(masterDirectory);
                                isPermitted=true;
                            }else {
                                Toast.makeText(MainActivity.this, "Permission required", Toast.LENGTH_SHORT).show();
                                isPermitted=false;
                            }
                        }
                }
            }
    );


    public ArrayList<String> getFolders(String path){
        ArrayList<String> files=new ArrayList<>();
        File listFiles=new File(path);
        File[] fileItems=listFiles.listFiles();
        assert fileItems != null;
        for (File fileItem : fileItems) files.add(fileItem.getName());
        return files;
    }

    public boolean createFolder(String path){
        File dir=new File(path);
        boolean isCreated=false;
        if(!dir.exists()) {
            isCreated=dir.mkdir();
            if(isCreated)
                Toast.makeText(this, "Successful", Toast.LENGTH_SHORT).show();
        }else isCreated=true;
        return isCreated;
    }

    public boolean deleteFolder(File folder){
        try {
            boolean res=false;
            File[] files = folder.listFiles();
            if (files != null) { //some JVMs return null for empty dirs
                for (File f : files) {
                    if(isHome) {
                        if(f.getName().contains(".c1p2l3")){
                            continue;
                        }
                        if (f.isDirectory()) {
                            deleteFolder(f);
                        } else {
                            res = f.delete();
                        }
                    } else if(isCompleted){
                        if(!f.getName().contains(".c1p2l3")){
                            continue;
                        }
                        if (f.isDirectory()) {
                            deleteFolder(f);
                        } else {
                            res = f.delete();
                        }
                    }
                }
            }
            if(!folder.getName().equals("AudiPix"))
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

        setContentView(R.layout.activity_main);

        //Requesting permissions
        requestPermission();

        //Delete All
        deleteAll=findViewById(R.id.delete_all);

        //TextView
        textView=findViewById(R.id.textView);
        SpannableString sp=new SpannableString("AudiPix");
        sp.setSpan(new StyleSpan(Typeface.BOLD), 0, sp.length(), 0);
        textView.setText(sp);

        //Fragment Button
        ImageView home=findViewById(R.id.home);
        ImageView completed=findViewById(R.id.completed);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isHome){
                    isHome=true;
                    isCompleted=false;
                    itemAdapter.notifyDataSetChanged();
                    listOfSubDirectory.setVisibility(View.VISIBLE);
                    addSubDirectory.setVisibility(View.VISIBLE);
                    home.setImageResource(R.drawable.home);
                    listOfCompletedDirectory.setVisibility(View.INVISIBLE);
                    completed.setImageResource(R.drawable.done_outline);
                    SpannableString sp=new SpannableString("AudiPix");
                    sp.setSpan(new StyleSpan(Typeface.BOLD), 0, sp.length(), 0);
                    textView.setText(sp);
                    if(listOfItems_view.isEmpty()){ listOfSubDirectory.setVisibility(View.INVISIBLE);empty_folder_animation_view.setVisibility(View.VISIBLE); deleteAll.setVisibility(View.INVISIBLE); }
                    else {listOfSubDirectory.setVisibility(View.VISIBLE);empty_folder_animation_view.setVisibility(View.INVISIBLE);deleteAll.setVisibility(View.VISIBLE);}

                }
            }
        });

        completed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isCompleted){
                    isCompleted=true;
                    isHome=false;
                    completedAdapter.notifyDataSetChanged();
                    listOfSubDirectory.setVisibility(View.INVISIBLE);
                    addSubDirectory.setVisibility(View.INVISIBLE);
                    listOfCompletedDirectory.setVisibility(View.VISIBLE);
                    completed.setImageResource(R.drawable.done);
                    home.setImageResource(R.drawable.home_outline);
                    textView.setText("Complete");
                    if(listOfCompletedItems_view.isEmpty()){ listOfCompletedDirectory.setVisibility(View.INVISIBLE);empty_folder_animation_view.setVisibility(View.VISIBLE); deleteAll.setVisibility(View.INVISIBLE);}
                    else {empty_folder_animation_view.setVisibility(View.INVISIBLE);deleteAll.setVisibility(View.VISIBLE);listOfCompletedDirectory.setVisibility(View.VISIBLE);}


                }
            }
        });

        //Animate
        empty_folder_animation_view=findViewById(R.id.empty_folder_animation_view);


        listOfCompletedDirectory=findViewById(R.id.listOfCompletedDirectory);
        if(isHome) listOfCompletedDirectory.setVisibility(View.INVISIBLE);


        //ListView of Folder names
        listOfSubDirectory=findViewById(R.id.listOfSubDirectory);
        if(isPermitted) {
            listOfFolders = getFolders(masterDirectory);
        }
        listOfItems_view = new ArrayList<>();
        listOfCompletedItems_view=new ArrayList<>();
        for (int i = 0; i < listOfFolders.size(); i++) {
            if(!listOfFolders.get(i).contains(".c1p2l3"))
                listOfItems_view.add(new Items(R.drawable.folder, listOfFolders.get(i), R.drawable.edit,R.drawable.done_outline));
            else
                listOfCompletedItems_view.add(new Items(R.drawable.folder, listOfFolders.get(i), R.drawable.edit,R.drawable.revert));
        }

        itemAdapter = new ItemAdapter(this, R.layout.list_items, listOfItems_view,listOfCompletedItems_view,masterDirectory,"home",listOfSubDirectory,empty_folder_animation_view,deleteAll);
        completedAdapter = new ItemAdapter(this,R.layout.list_items,listOfCompletedItems_view,listOfItems_view,masterDirectory,"complete",listOfCompletedDirectory,empty_folder_animation_view,deleteAll);

        listOfCompletedDirectory.setAdapter(completedAdapter);
        listOfSubDirectory.setAdapter(itemAdapter);
        listOfSubDirectory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!longItemClick) {
                    TextView txt = view.findViewById(R.id.text);
                    selectedFolder = txt.getText().toString();

                    Intent insideFolder = new Intent(MainActivity.this, Content.class);
                    insideFolder.putExtra("folderName", selectedFolder);
                    insideFolder.putExtra("masterDirectory", masterDirectory);

                    startActivity(insideFolder);
                }
            }
        });
        listOfSubDirectory.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                longItemClick=true;
                TextView txt = view.findViewById(R.id.text);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                selectedFolder = txt.getText().toString();
                File folder = new File(masterDirectory + "/" + selectedFolder);
                if (folder.exists()) {
                    builder.setTitle("Are you Sure?")
                            .setMessage("Confirm if you want to delete the folder.")
                            .setCancelable(true)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    boolean isDeleted = deleteFolder(folder);
                                    if (isDeleted) {
                                        listOfItems_view.remove(position);
                                        itemAdapter.notifyDataSetChanged();
                                        longItemClick=false;
                                        if(itemAdapter.getCount()==0) {
//                                            listOfSubDirectory.setBackgroundResource(R.drawable.empty_folder);
                                            empty_folder_animation_view.setVisibility(View.VISIBLE);
                                            deleteAll.setVisibility(View.INVISIBLE);
                                        }
                                        Toast.makeText(MainActivity.this, "Folder Deleted...", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Folder not deleted...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    longItemClick=false;
                                }
                            }).show();

                } else {
                    Toast.makeText(MainActivity.this, "Folder doesn't exists...", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        listOfCompletedDirectory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!longItemClick_C) {
                    TextView txt = view.findViewById(R.id.text);
                    selectedFolder = txt.getText().toString();

                    Intent insideFolder = new Intent(MainActivity.this, Content.class);
                    insideFolder.putExtra("folderName", selectedFolder+".c1p2l3");
                    insideFolder.putExtra("masterDirectory", masterDirectory);

                    startActivity(insideFolder);
                }
            }
        });
        listOfCompletedDirectory.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                longItemClick_C=true;
                TextView txt = view.findViewById(R.id.text);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                selectedFolder = txt.getText().toString();
                File folder = new File(masterDirectory + "/" + selectedFolder+".c1p2l3");
                if (folder.exists()) {
                    builder.setTitle("Are you Sure?")
                            .setMessage("Confirm if you want to delete the folder.")
                            .setCancelable(true)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    boolean isDeleted = deleteFolder(folder);
                                    if (isDeleted) {
                                        listOfCompletedItems_view.remove(position);
                                        completedAdapter.notifyDataSetChanged();
                                        longItemClick_C=false;
                                        if(completedAdapter.getCount()==0) {
                                            empty_folder_animation_view.setVisibility(View.VISIBLE);
                                            deleteAll.setVisibility(View.INVISIBLE);
                                        }
                                        Toast.makeText(MainActivity.this, "Folder Deleted...", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Folder not deleted...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    longItemClick_C=false;
                                }
                            }).show();

                } else {
                    Toast.makeText(MainActivity.this, "Folder doesn't exists...", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        if(listOfFolders.isEmpty()) {
            empty_folder_animation_view.setVisibility(View.VISIBLE);
            deleteAll.setVisibility(View.INVISIBLE);
        }else{
            empty_folder_animation_view.setVisibility(View.INVISIBLE);
        }
        if(isHome){
            if(listOfItems_view.isEmpty()) {listOfSubDirectory.setVisibility(View.INVISIBLE);empty_folder_animation_view.setVisibility(View.VISIBLE); deleteAll.setVisibility(View.INVISIBLE); }
            else {listOfSubDirectory.setVisibility(View.VISIBLE);empty_folder_animation_view.setVisibility(View.INVISIBLE);deleteAll.setVisibility(View.VISIBLE);}
        }
        if(isCompleted){
            if(listOfCompletedItems_view.isEmpty()){ listOfCompletedDirectory.setVisibility(View.INVISIBLE);empty_folder_animation_view.setVisibility(View.VISIBLE); deleteAll.setVisibility(View.INVISIBLE);}
            else {empty_folder_animation_view.setVisibility(View.INVISIBLE);deleteAll.setVisibility(View.VISIBLE);listOfCompletedDirectory.setVisibility(View.VISIBLE);}
        }


        //DeleteAll Functionality
        deleteAll=findViewById(R.id.delete_all);
        deleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Are You Sure?")
                        .setMessage("All Files and folders will be deleted.")
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean isDeleted=deleteFolder(new File(masterDirectory));
                                if(isDeleted) {
                                    empty_folder_animation_view.setVisibility(View.VISIBLE);
                                    createFolder(masterDirectory);
                                    if(isHome) {
                                        itemAdapter.clear();
                                        itemAdapter.notifyDataSetChanged();
                                    }
                                    else if(isCompleted) {
                                        completedAdapter.clear();
                                        completedAdapter.notifyDataSetChanged();
                                    }
                                    selectedFolder=masterDirectory;
                                    deleteAll.setVisibility(View.INVISIBLE);
                                    Toast.makeText(MainActivity.this, "All Deleted...", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();

            }
        });

        addSubDirectory=findViewById(R.id.floatingActionButton);
        addSubDirectory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating New Folders (SubDirectory).
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss", Locale.getDefault());
                String dateTime = format.format(new Date());
                String folderName= masterDirectory +"/"+dateTime;

                boolean isCreated=createFolder(folderName);
                if(isCreated) {
                    empty_folder_animation_view.setVisibility(View.INVISIBLE);
                    selectedFolder = folderName;
                    deleteAll.setVisibility(View.VISIBLE);
                    listOfSubDirectory.setVisibility(View.VISIBLE);
                    itemAdapter.add(new Items(R.drawable.folder,dateTime,R.drawable.edit,R.drawable.done_outline));
                    itemAdapter.notifyDataSetChanged();
                }else Toast.makeText(MainActivity.this, "Error in folder creation...", Toast.LENGTH_SHORT).show();
            }
        });


    }
}