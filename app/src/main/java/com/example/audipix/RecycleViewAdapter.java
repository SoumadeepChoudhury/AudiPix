package com.example.audipix;

import static android.content.Context.ACTIVITY_SERVICE;
import static androidx.core.app.ActivityCompat.startActivityForResult;
import static androidx.core.content.ContextCompat.startActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context ctx;
    private ArrayList<Object> Items;
    private final int recordingViewType=0;
    private final int textViewType=1;
    private final int imageViewType=2;
    private String existingContent="";

    public RecycleViewAdapter(Context ctx, ArrayList<Object> Items) {
        this.ctx = ctx;
        this.Items = Items;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==recordingViewType) {
            LayoutInflater recordingLayout = LayoutInflater.from(ctx);
            View cardView = recordingLayout.inflate(R.layout.recording_card_view, parent, false);
            return new ViewHolder(cardView);
        } else if(viewType==textViewType){
            LayoutInflater textLayout=LayoutInflater.from(ctx);
            View cardView=textLayout.inflate(R.layout.text_card_view,parent,false);
            return new ViewHolder(cardView);
        } else if(viewType==imageViewType){
            LayoutInflater imageLayout=LayoutInflater.from(ctx);
            View cardView=imageLayout.inflate(R.layout.image_card_view,parent,false);
            return new ViewHolder(cardView);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if(Items.get(position) instanceof Recording){
            return recordingViewType;
        }else if(Items.get(position) instanceof Text){
            return textViewType;
        } else if (Items.get(position) instanceof Image) {
            return imageViewType;
        }else return -1;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder.getItemViewType()==recordingViewType) {
            Recording recording = (Recording) Items.get(position);


            FloatingActionButton record = holder.itemView.findViewById(R.id.record);
            TextView titleTxt = holder.itemView.findViewById(R.id.titleTxt);
            LottieAnimationView recordAnimation = holder.itemView.findViewById(R.id.recordAnimation);
            ImageView deleteCardItem = holder.itemView.findViewById(R.id.deleteCardItem);


            if (!Objects.equals(recording.fileName, "")) {
                titleTxt.setText(recording.fileName);
            } else {
                titleTxt.setText("");
            }

            titleTxt.setOnClickListener(new View.OnClickListener() {
                public String extension = ".mp3";

                @Override
                public void onClick(View view) {
                    View editViewBox = LayoutInflater.from(ctx).inflate(R.layout.edit_name, null);
                    TextInputEditText newText = editViewBox.findViewById(R.id.editName);
                    String fileName = titleTxt.getText().toString();
                    fileName = fileName.replace(extension, "");
                    newText.setText(fileName);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setView(editViewBox)
                            .setCancelable(true)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    File file = new File(recording.masterDirectory + "/" + recording.folderName + "/" + recording.fileName);
                                    file.renameTo(new File(recording.masterDirectory + "/" + recording.folderName + "/" + newText.getText().toString() + extension));
                                    recording.fileName = newText.getText().toString() + extension;
                                    titleTxt.setText(newText.getText()+extension);
                                    notifyDataSetChanged();
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

            deleteCardItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
                    alert.setTitle("Are You Sure?")
                            .setMessage("The " + (titleTxt.getText().toString().isEmpty()?"recording card": titleTxt.getText()) + " will get permanently deleted.")
                            .setCancelable(true)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    boolean isDeleted = recording.deleteRecording();
                                    if (isDeleted) {
                                        Toast.makeText(ctx, "Successfully Deleted " + titleTxt.getText(), Toast.LENGTH_SHORT).show();
                                        Items.remove(holder.getAdapterPosition());
                                        notifyDataSetChanged();
                                    }
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

            record.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Starting recording
                    if (recording.isRecorded) {
                        if (recording.isPlaying) {
                            recording.pausePlayingRecording();
                            record.setImageResource(R.drawable.play);
                            recordAnimation.pauseAnimation();
                        } else {
                            recording.playRecording(record, recordAnimation);
                            record.setImageResource(R.drawable.pause);
                            recordAnimation.playAnimation();
                        }
                    } else {
                        if (!recording.isRecording) {
                            String recordedFileName = recording.startRecording();
                            if (!Objects.equals(recordedFileName, "")) {
                                titleTxt.setText(recordedFileName);
                                recordAnimation.playAnimation();
                                record.setImageResource(R.drawable.pause);
                            }
                        } else {
                            recording.stopRecording();
                            recordAnimation.pauseAnimation();
                            record.setImageResource(R.drawable.play);
                        }
                    }
                }
            });
        }
        else if(holder.getItemViewType()==textViewType){
            Text text=(Text) Items.get(position);
            TextView titleTxt=holder.itemView.findViewById(R.id.titleTxt);
            ImageView deleteItem=holder.itemView.findViewById(R.id.deleteCardItem);
            EditText editText = holder.itemView.findViewById(R.id.editText);
            FloatingActionButton save=holder.itemView.findViewById(R.id.save);
            editText.setBackgroundResource(0);
            titleTxt.setText(!text.isWritten?"":text.fileName);
            if(text.isWritten){
                text.readFile();
                existingContent=text.content;
            }
            editText.setText(!text.isWritten?"":text.content,TextView.BufferType.EDITABLE);

            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if(hasFocus) {
                        editText.setBackgroundResource(R.drawable.border_for_edittext_box);
                        save.setVisibility(View.VISIBLE);
                        text.mic.setVisibility(View.INVISIBLE);
                        text.edit.setVisibility(View.INVISIBLE);
                        text.image.setVisibility(View.INVISIBLE);
                        save.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                editText.clearFocus();
                                save.setVisibility(View.INVISIBLE);
                                text.mic.setVisibility(View.VISIBLE);
                                text.edit.setVisibility(View.VISIBLE);
                                text.image.setVisibility(View.VISIBLE);
                                text.content=editText.getText().toString();
                                if(!text.content.isEmpty() && !text.content.equals(existingContent)){
                                    text.writeFile();
                                }
                                if(text.isWritten)
                                    titleTxt.setText(text.fileName);
                                else titleTxt.setText("");
                            }
                        });
                    }
                    else{
                        Log.d("ClickedEditText","Removed Focus");
                    editText.setBackgroundResource(0);
                    }
                }
            });

            deleteItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert=new AlertDialog.Builder(ctx);
                    alert.setTitle("Are You Sure")
                            .setMessage("The "+(text.fileName.isEmpty()?"text card":text.fileName)+" will be permanently deleted.")
                            .setCancelable(true)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    text.deleteFile();
                                    if(text.isDeleted){
                                        Toast.makeText(ctx, "Successfully Deleted "+text.fileName, Toast.LENGTH_SHORT).show();
                                        Items.remove(holder.getAdapterPosition());
                                        notifyDataSetChanged();
                                    }
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

            titleTxt.setOnClickListener(new View.OnClickListener() {
                public String extension=".txt";
                @Override
                public void onClick(View view) {
                    View editViewBox=LayoutInflater.from(ctx).inflate(R.layout.edit_name,null);
                    TextInputEditText newText=editViewBox.findViewById(R.id.editName);
                    String fileName=titleTxt.getText().toString().replace(extension,"");
                    newText.setText(fileName);
                    AlertDialog.Builder builder=new AlertDialog.Builder(ctx);
                    builder.setView(editViewBox)
                            .setCancelable(true)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    File file=new File(text.masterDirectory+"/"+text.folderName+"/"+text.fileName);
                                    file.renameTo(new File(text.masterDirectory+"/"+text.folderName+"/"+newText.getText()+extension));
                                    titleTxt.setText(newText.getText()+extension);
                                    text.fileName=newText.getText()+extension;
                                    notifyDataSetChanged();
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
        else if(holder.getItemViewType()==imageViewType) {
            Image image=(Image) Items.get(position);

            TextView titleTxt=holder.itemView.findViewById(R.id.titleTxt);
            ImageView deleteItem=holder.itemView.findViewById(R.id.deleteCardItem);
            ImageView imageView=holder.itemView.findViewById(R.id.imageView);
            FloatingActionButton capture=holder.itemView.findViewById(R.id.capture);


            //TODO: Fix Other homeObjects Property is set in another.
            if(!image.fileName.isEmpty()){
                imageView.setVisibility(View.VISIBLE);
                titleTxt.setText(image.fileName);
                imageView.setImageURI(Uri.fromFile(new File(image.masterDirectory+"/"+image.folderName+"/"+image.fileName)));
                capture.setVisibility(View.INVISIBLE);
            }else{
                titleTxt.setText("");
                imageView.setImageURI(null);
                imageView.setVisibility(View.INVISIBLE);
                capture.setVisibility(View.VISIBLE);
            }

            titleTxt.setOnClickListener(new View.OnClickListener() {
                public String extension=".jpeg";
                @Override
                public void onClick(View view) {
                    View editViewBox=LayoutInflater.from(ctx).inflate(R.layout.edit_name,null);
                    TextInputEditText newText=editViewBox.findViewById(R.id.editName);
                    String fileName=titleTxt.getText().toString().replace(extension,"");
                    newText.setText(fileName);
                    AlertDialog.Builder builder=new AlertDialog.Builder(ctx);
                    builder.setView(editViewBox)
                            .setCancelable(true)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    File file=new File(image.masterDirectory+"/"+image.folderName+"/"+image.fileName);
                                    file.renameTo(new File(image.masterDirectory+"/"+image.folderName+"/"+newText.getText()+extension));
                                    titleTxt.setText(newText.getText()+extension);
                                    image.fileName=newText.getText()+extension;
                                    notifyDataSetChanged();
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
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent preview=new Intent(ctx,ImagePreview.class);
                    preview.putExtra("title",titleTxt.getText());
                    preview.putExtra("file",image.masterDirectory+"/"+image.folderName+"/"+image.fileName);
                    ContextCompat.startActivity(ctx,preview,null);
                }
            });
            image.chooser(ctx,capture,titleTxt,imageView);
            deleteItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert=new AlertDialog.Builder(ctx);
                    alert.setTitle("Are You Sure?")
                            .setMessage("The "+(image.fileName.isEmpty()?"image card":image.fileName)+" will be permanently deleted.")
                            .setCancelable(true)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    boolean result=image.deleteImage();
                                    if(result) {
                                        Items.remove(holder.getAdapterPosition());
                                        notifyDataSetChanged();
                                    }
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

    @Override
    public int getItemCount() {
        return Items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
