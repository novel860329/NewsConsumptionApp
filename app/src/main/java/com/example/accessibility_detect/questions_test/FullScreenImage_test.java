package com.example.accessibility_detect.questions_test;

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

public class FullScreenImage_test  extends Activity {


    @SuppressLint("NewApi")



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_full);

//          6/17
//        AssetManager assetManager = getAssets();
//        Bundle extras = getIntent().getExtras();
//        String filepath = extras.getString("imagebitmap");
//        String[] file_split = filepath.split("/");
//        filepath = file_split[4] + "/" + file_split[5];
//        Log.d("CueRecallFragment", filepath);
//        Bitmap bmp = null;
//        InputStream istr = null;
//        try {
//            istr = assetManager.open(filepath);
//            bmp = BitmapFactory.decodeStream(istr);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        if (bmp == null){
//            Log.d("CueRecallFragment", "bmp is null");
//        }

//
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
                FullScreenImage_test.this.finish();
            }
        });
        imgDisplay.setImageBitmap(bmp );

    }


}
