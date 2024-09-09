package com.example.audipix;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class ItemAdapter extends ArrayAdapter<Items> {
    final private Context ctx;
    final private int res;
    final private String masterDirectory;
    ArrayList<Items> homeObjects;
    ArrayList<Items> completedObjects;
    String location;
    ListView listOfDirectory;
    LottieAnimationView empty_animation;
    ImageView delete_all;


    public ItemAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Items> homeObjects, @NonNull ArrayList<Items> completedObjects, String masterDirectory, @NonNull String location, ListView listOfDirectory, LottieAnimationView empty_animation,ImageView delete_all) {
        super(context, resource, homeObjects);
        this.ctx=context;
        this.res=resource;
        this.masterDirectory=masterDirectory;
        this.location=location;
        this.listOfDirectory=listOfDirectory;
        this.empty_animation=empty_animation;
        this.delete_all=delete_all;
        if(this.location.equals("home")) {
            this.homeObjects = homeObjects;
            this.completedObjects = completedObjects;
        }
        else{
            this.homeObjects=completedObjects;
            this.completedObjects=homeObjects;
        }
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater=LayoutInflater.from(ctx);
        convertView=layoutInflater.inflate(res,parent,false);

        ImageView imgView=convertView.findViewById(R.id.image);
        TextView txtView=convertView.findViewById(R.id.text);
        ImageView editBtn=convertView.findViewById(R.id.edit);
        ImageView complete=convertView.findViewById(R.id.complete);
        Log.d("ObjectDetails","Object: "+getItem(position).getText());

        imgView.setImageResource(getItem(position).getImage());
        String title=getItem(position).getText();
        if (Objects.equals(location, "complete")){
            title=title.replace(".c1p2l3","");
        }
        txtView.setText(title);
        editBtn.setImageResource(getItem(position).getEdit());
        complete.setImageResource(getItem(position).getAction());

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View editViewBox = LayoutInflater.from(ctx).inflate(R.layout.edit_name, null);
                TextInputEditText newText=editViewBox.findViewById(R.id.editName);
                newText.setText(txtView.getText().toString());
                AlertDialog.Builder builder=new AlertDialog.Builder(ctx);
                builder
                        .setView(editViewBox)
                        .setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newTextEntered=newText.getText().toString();
                                File fileToChange=new File(masterDirectory+"/"+txtView.getText()+(Objects.equals(location, "complete") ?".c1p2l3":""));
                                boolean isChanged=fileToChange.renameTo(new File(masterDirectory+"/"+newTextEntered+(Objects.equals(location, "complete") ?".c1p2l3":"")));
                                if(isChanged) {
                                    (Objects.equals(location, "complete") ?completedObjects:homeObjects).get(position).setText(newTextEntered);
                                    txtView.setText(newTextEntered);
                                    Toast.makeText(ctx, "Renamed successfully...", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(ctx, "Rename Unsuccessful", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        });

        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert=new AlertDialog.Builder(ctx);
                alert.setTitle("Confirm")
                        .setMessage("Please confirm if you want to transfer "+txtView.getText()+" to "+(location=="complete"?"Home":"Completed")+" section.")
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(Objects.equals(location, "home")) {
                                    File file = new File(masterDirectory + "/" + txtView.getText());
                                    boolean isTransfered = file.renameTo(new File(masterDirectory + "/" + txtView.getText() + ".c1p2l3"));
                                    if (isTransfered) {
                                        homeObjects.get(position).setAction(R.drawable.revert);
                                        homeObjects.get(position).setText(txtView.getText()+".c1p2l3");
                                        completedObjects.add(homeObjects.get(position));
                                        homeObjects.remove(position);
                                        if(homeObjects.isEmpty()) {listOfDirectory.setVisibility(View.INVISIBLE);empty_animation.setVisibility(View.VISIBLE); delete_all.setVisibility(View.INVISIBLE); }
                                        else {listOfDirectory.setVisibility(View.VISIBLE);empty_animation.setVisibility(View.INVISIBLE);delete_all.setVisibility(View.VISIBLE);}
                                        notifyDataSetChanged();
                                    }
                                }
                                else if(Objects.equals(location, "complete")){
                                    File file = new File(masterDirectory + "/" + txtView.getText()+".c1p2l3");
                                    boolean isTransfered = file.renameTo(new File(masterDirectory + "/" + txtView.getText()));
                                    if (isTransfered) {
                                        completedObjects.get(position).setAction(R.drawable.done_outline);
                                        completedObjects.get(position).setText(txtView.getText().toString());
                                        homeObjects.add(completedObjects.get(position));
                                        completedObjects.remove(position);
                                        if(completedObjects.isEmpty()){ listOfDirectory.setVisibility(View.INVISIBLE);empty_animation.setVisibility(View.VISIBLE); delete_all.setVisibility(View.INVISIBLE);}
                                        else {empty_animation.setVisibility(View.INVISIBLE);delete_all.setVisibility(View.VISIBLE);listOfDirectory.setVisibility(View.VISIBLE);}
                                        notifyDataSetChanged();
                                    }
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        }).show();
            }
        });

        return convertView;
    }
}
