package com.example.accessibility_detect.diarys;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.accessibility_detect.R;

import java.io.FileInputStream;

public class FullScreenImage_diary  extends Activity {


    @SuppressLint("NewApi")



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_full);

        Bundle extras = getIntent().getExtras();
        String filepath = extras.getString("imagebitmap");
        Bitmap bmp = null;
        try{
            FileInputStream fis = new FileInputStream(filepath);
            bmp  = BitmapFactory.decodeStream(fis);
        }catch(Exception e){
            e.printStackTrace();
        }


        ImageView imgDisplay;
        Button btnClose;


        imgDisplay = (ImageView) findViewById(R.id.imgDisplay);
        btnClose = (Button) findViewById(R.id.btnClose);


        btnClose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FullScreenImage_diary.this.finish();
            }
        });
        imgDisplay.setImageBitmap(bmp );

    }


}
