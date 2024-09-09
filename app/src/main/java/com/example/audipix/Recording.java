package com.example.audipix;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Recording {

    MediaRecorder mediaRecorder = new MediaRecorder();
    MediaPlayer mediaPlayer=new MediaPlayer();
    boolean isRecording;
    boolean isRecorded;
    boolean isPlaying;
    String fileName;
    String masterDirectory;
    String folderName;
    int currentPlayingPosition;
    boolean isPaused;


    public Recording(boolean isRecorded,String masterDirectory,String folderName,String fileName) {
        this.isRecording=false;
        this.isRecorded=isRecorded;
        this.isPlaying=false;
        this.isPaused=false;
        this.fileName=fileName;
        this.masterDirectory=masterDirectory;
        this.folderName=folderName;
        this.currentPlayingPosition=0;
        try {
            if(isRecorded){
                mediaPlayer.setDataSource(masterDirectory + "/" + folderName + "/" + fileName);
//                mediaPlayer.prepare();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public String startRecording(){
        if(!isRecorded)
            try {
            SimpleDateFormat format = new SimpleDateFormat("HH.mm.ss", Locale.getDefault());
            String dateTime = format.format(new Date());
            this.fileName="Recording"+dateTime+".mp3";
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            File file = new File(this.masterDirectory+"/"+this.folderName+"/"+fileName);
            mediaRecorder.setOutputFile(file);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mediaRecorder.prepare();
            mediaRecorder.start();
            this.isRecording=true;
            this.isRecorded=false;
        }catch(Exception e){
            e.printStackTrace();
        }

        return this.fileName;
    };

    public boolean stopRecording(){
        boolean isStopped=false;
        if(!isRecorded)
            try {
                isStopped=true;
                mediaRecorder.stop();
                this.isRecording=false;
                this.isRecorded=true;
                mediaRecorder.release();
                mediaPlayer.setDataSource(masterDirectory + "/" + folderName + "/" + fileName);
        } catch (Exception e){
            e.printStackTrace();
        }
        return isStopped;
    };

    public void playRecording(FloatingActionButton record,LottieAnimationView recordAnimation){
        if(!isPlaying) {
            try {
                if(isPaused) {
                    mediaPlayer.seekTo(currentPlayingPosition);
                    mediaPlayer.start();
                    isPaused=false;
                }else {
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }
                this.isPlaying = true;
                this.isRecording = false;
                this.isRecorded = true;


                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        stopPlaying();
                        record.setImageResource(R.drawable.play);
                        recordAnimation.pauseAnimation();
                    }
                });
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    };

    public boolean pausePlayingRecording(){
        isPaused=true;
        mediaPlayer.pause();
        currentPlayingPosition=mediaPlayer.getCurrentPosition();
        this.isPlaying=false;
        this.isRecording=false;
        this.isRecorded=true;
        return true;
    }

    public void stopPlaying(){
        isPaused=false;
        mediaPlayer.stop();
        this.isPlaying=false;
        this.isRecording=false;
        this.isRecorded=true;
    }

    public boolean deleteRecording(){
        boolean isDeleted=false;
        if(this.isRecorded) {
            File file = new File(this.masterDirectory + "/" + this.folderName + "/" + this.fileName);
            if (file.exists()) {
                isDeleted = file.delete();
                this.isRecorded = false;
                this.isPlaying = false;
                this.isRecording = false;
                this.fileName="";
                mediaPlayer.release();
                currentPlayingPosition=0;
                isPaused=false;
                mediaRecorder.release();
            }
        }else{
            isDeleted=true;
        }
        return isDeleted;
    }

}
