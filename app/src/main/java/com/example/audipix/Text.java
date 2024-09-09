package com.example.audipix;


import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Text {
    public String masterDirectory;
    public String folderName;
    public String fileName;
    public String content;
    public FloatingActionButton mic;
    public FloatingActionButton edit;
    public FloatingActionButton image;
    public boolean isWritten;
    public boolean isDeleted;

    public Text(String masterDirectory, String folderName, String fileName, FloatingActionButton mic, FloatingActionButton edit, FloatingActionButton image) {
        this.masterDirectory = masterDirectory;
        this.folderName = folderName;
        this.fileName = fileName;
        this.content="";
        this.mic=mic;
        this.edit=edit;
        this.image=image;
        this.isWritten= !this.fileName.isEmpty();
        this.isDeleted=false;
    }

    public void writeFile(){
        if(!this.isWritten) {
            SimpleDateFormat format = new SimpleDateFormat("HH.mm.ss", Locale.getDefault());
            String dateTime = format.format(new Date());
            this.fileName = "Text" + dateTime + ".txt";
        }

        try {
            File file=new File(this.masterDirectory+"/"+this.folderName+"/"+this.fileName);
            FileOutputStream writer=new FileOutputStream(file);
            writer.write(content.getBytes());
            writer.close();
            this.isWritten=true;
        } catch (Exception e) {
            this.fileName="";
            this.isWritten=false;
            e.printStackTrace();
        }
    }


    public void readFile(){
        try {
            File file=new File(this.masterDirectory+"/"+this.folderName+"/"+this.fileName);
            FileInputStream reader=new FileInputStream(file);
            byte[] arr=new byte[(int)file.length()];
            reader.read(arr);
            reader.close();
            this.content=new String(arr);
        } catch (Exception e) {
            e.printStackTrace();
            this.content="";
        }
    }


    public void deleteFile(){
        try{
            if(!this.fileName.isEmpty()) {
                File file = new File(this.masterDirectory + "/" + this.folderName + "/" + this.fileName);
                if (file.exists()) {
                    file.delete();
                    this.isDeleted = true;
                }
            }else{
                this.isDeleted=true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
