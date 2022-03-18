package com.example.accessibility_detect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static labelingStudy.nctu.minuku.config.SharedVariables.CanFillDiary;

//import io.fabric.sdk.android.Fabric;

public class ChoosePicture extends AppCompatActivity {
    private String TAG = "Choose Picture";
    public static ImageView imageShow;
    private Button confirm;
    private TextView title;
    public static TextView name;
    private ArrayList<String> mImages = new ArrayList<>();
    private ArrayList<Boolean> mCheck = new ArrayList<>();
    private SharedPreferences pref;
    private String picture_time;

    protected void onCreate(final Bundle savedInstanceState) {
        Log.d(TAG, "Oncreate");
        super.onCreate(null);
//        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.choosepicture_main);
        pref = getSharedPreferences("test",MODE_PRIVATE);
        imageShow = (ImageView)findViewById(R.id.image_show);
        title = (TextView)findViewById(R.id.title);
        name = (TextView)findViewById(R.id.name);
        confirm = (Button)findViewById(R.id.btn_confirm);
        confirm.setOnClickListener(confirmClick);
        pref.edit().putInt("position", 0).apply();
        getImages();
    }

    Button.OnClickListener confirmClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            CanFillDiary = false;
            Intent ESMintent = new Intent(ChoosePicture.this, ESM.class);
{
                String ChooseImage = getSharedPreferences("test",MODE_PRIVATE).getString("ChooseImage","");
//                boolean newDiary = pref.getBoolean("NewDiary", false);
//                Log.d(TAG, "NEW DIARY: " + newDiary);
//                if(newDiary)
//                {
                    int DiaryClick = pref.getInt("Diary_click", 0);
                    DiaryClick++;
                    pref.edit().putInt("Diary_click", DiaryClick).apply();
//                }
                //Log.d(TAG, "Chosse img: " + ChooseImage);
                if(mImages.size() != 0) {
                    if (ChooseImage.equals("")) {
                        //pref.edit().putString("FilePath", "NA").apply();
                    }
                }
            }
            startActivity(ESMintent);
            mImages.clear();
            mCheck.clear();
            finish();
//                    mLabel.set(selectedPosition1, "bored");
//                    mLabel.set(selectedPosition2, "bored");
            /*if(selectedPosition1 != -1 && selectedPosition2 != -1) {

                LabelRetrieval();
                StartRetrieval();
                EndRetrieval();
                IndexRetrieval();
            }else{
                new AlertDialog.Builder(ChoosePicture.this)
                        .setMessage("Please choose an interval")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }*/
        }
    };

    public void getImages(){
        Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
//        String ESMtime = pref.getString("ESMtime","NA");//2020-04-11 14:29:35
//        Log.d(TAG,"ESMtime: " + ESMtime);
//        String ESMdate = ESMtime.split(" ")[0];
//        File imgFile = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES), "News_Consumption/" + ESMdate);
//        Log.d(TAG, "Path exists: " + imgFile);
//        CSVHelper.storeToCSV("RecycleView.csv","Path exists: " + imgFile);
        ArrayList<String> ImageCopy = new ArrayList<>();
        ArrayList<Boolean> CheckCopy = new ArrayList<>();
        mImages.clear();
        mCheck.clear();
       {
            pref.edit().putBoolean("DiaryClick", true).apply();
            title.setText("請選擇今天一則有去查證的新聞");
            Log.d(TAG, "This is Diary");
            try {
                Set<String> DiaryPicture = pref.getStringSet("DiaryPicture", new HashSet<String>());
                ImageCopy = new ArrayList<String>(DiaryPicture);
                for (int i = 0; i < ImageCopy.size(); i++) {
                    CheckCopy.add(false);
                }
                Collections.sort(ImageCopy);
                Collections.reverse(ImageCopy);
            }
            catch(Exception e){
                e.printStackTrace();
            }
            if(ImageCopy.size() != 0) {
                Glide.with(this)
                        .asBitmap()
                        .load(ImageCopy.get(0))
                        .into(imageShow);
                try {
                    String[] mImages_split = ImageCopy.get(0).split("/|-|\\.");
                    picture_time = mImages_split[mImages_split.length - 6] + ":" + mImages_split[mImages_split.length - 5]
                            + ":" + mImages_split[mImages_split.length - 4];
                    name.setText(picture_time);
                    Log.d(TAG, "time: " + picture_time);
                }catch(Exception e){
                    e.printStackTrace();
                }
                pref.edit().putString("ChooseImage", ImageCopy.get(0)).apply();
                //mCheck.set(0, true);
                mImages = ImageCopy;
                mCheck = CheckCopy;
                initRecyclerView();
            }
            else{
                title.setText("因為今日沒有回答問卷\n請按下確認鍵進入問卷");
                name.setText("");
                imageShow.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
            }
        }
    }
    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: init recyclerview");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new MyLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mImages, mCheck, 2);
        recyclerView.setAdapter(adapter);
    }

    private boolean isInTenMinute(String filename, String ESMtime){
        Log.d(TAG, "Before compute: " + filename + " " + ESMtime);
        int threshold = 10;
        boolean result = false;
        String[] filname_split = filename.split("-|\\.");
        String[] ESMtime_split = ESMtime.split(":");
        int filename_time = Integer.parseInt(filname_split[0]) * 60 + Integer.parseInt(filname_split[1]);//165643
        int ESM_time = Integer.parseInt(ESMtime_split[0]) * 60 + Integer.parseInt(ESMtime_split[1]);//170317
        Log.d(TAG, "After compute: " + filename_time + " " + ESM_time);
        if(ESM_time - filename_time < threshold && ESM_time - filename_time >= 0){
            result = true;
        }
        Log.d(TAG, "result: " + result);
        return result;
    }
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one
    }
    public void onRestart() {
        super.onRestart();

        Log.d(TAG, "onRestart");
        pref.edit().putInt("position", 0).apply();
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}

