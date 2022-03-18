package com.example.accessibility_detect.questions;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.accessibility_detect.R;

import java.io.FileInputStream;

public class FullScreenImage  extends AppCompatActivity {

    String TAG = "EsmResult";
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
                FullScreenImage.this.finish();
            }
        });
        imgDisplay.setImageBitmap(bmp );

    }

    public void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    public void onResume(){
        super.onResume();
        Log.d(TAG, "onResume");
    }

    public void onPause(){
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.d(TAG, "onStop");
    }
}
