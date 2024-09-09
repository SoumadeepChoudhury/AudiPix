package com.example.audipix;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.Objects;

public class ImagePreview extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Removing Title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();

        setContentView(R.layout.image_preview);

        ImageView back=findViewById(R.id.back);
        TextView title=findViewById(R.id.filename);
        ImageView image_preview=findViewById(R.id.image_preview);

        Intent intent=getIntent();
        title.setText(intent.getStringExtra("title"));
        image_preview.setImageURI(Uri.fromFile(new File(intent.getStringExtra("file"))));

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
