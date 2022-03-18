package com.example.accessibility_detect.diarys.fragments_diary;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.accessibility_detect.MyLinearLayoutManager;
import com.example.accessibility_detect.R;
import com.example.accessibility_detect.RecyclerViewAdapter;
import com.example.accessibility_detect.diarys.QuestionActivity_diary;
import com.example.accessibility_detect.diarys.questionmodels_diary.QuestionsItem_diary;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import labelingStudy.nctu.minuku.DBHelper.appDatabase;

import static android.content.Context.MODE_PRIVATE;
import static labelingStudy.nctu.minuku.config.SharedVariables.CanFillEsm;
import static labelingStudy.nctu.minuku.config.SharedVariables.appNameForQ;
import static labelingStudy.nctu.minuku.config.SharedVariables.dateToStamp;
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTime;
import static labelingStudy.nctu.minuku.config.SharedVariables.isDFinish;

public class ChoosePictureFragment_diary extends Fragment {
    private String TAG = "Choose Picture";
    private boolean isFirstLoad = true; // 是否第一次加载
    public static ImageView imageShow;
    private TextView questionRBTypeTextView;
    private FragmentActivity mContext;
    appDatabase db;
    private QuestionsItem_diary radioButtonTypeQuestion;
    private Button nextOrFinishButton;
    private Button confirm;
    private TextView title;
    public static TextView name;
    private ArrayList<String> mImages = new ArrayList<>();
    private ArrayList<Boolean> mCheck = new ArrayList<>();
    private SharedPreferences pref;
    private String picture_time;
    private String questionId = "";
    private int currentPagePosition = 0;
    private int clickedRadioButtonPosition = 0;
    private String qState = "0";
    private boolean isDiary = false;
    private boolean screenVisible = false;

    int relatedId;
    String appName = "";
    String enterTime ="";
    private EditText editText_answer;
    String notiId = "";

    public ChoosePictureFragment_diary()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_choose_picture, container, false);
        db = appDatabase.getDatabase(getActivity());
        nextOrFinishButton = rootView.findViewById(R.id.diary_nextOrFinishButton);
        questionRBTypeTextView = rootView.findViewById(R.id.diary_questionTypeTextView);
        imageShow = (ImageView)rootView.findViewById(R.id.diary_image_show_frag);
//        title = (TextView)rootView.findViewById(R.id.title_frag);
        name = (TextView)rootView.findViewById(R.id.diary_name_frag);
//        confirm = (Button)rootView.findViewById(R.id.btn_confirm_frag);
        nextOrFinishButton.setOnClickListener(confirmClick);
        return rootView;
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
        String extraInfo = "";
        mContext = (FragmentActivity) getActivity();
        pref = mContext.getSharedPreferences("test",MODE_PRIVATE);
        Log.d(TAG, "ChoosePic: " + isDiary);
        if (getArguments() != null)
        {
            radioButtonTypeQuestion = getArguments().getParcelable("question");
            questionId = String.valueOf(radioButtonTypeQuestion != null ? radioButtonTypeQuestion.getId() : 0);
            currentPagePosition = getArguments().getInt("page_position") + 1;
            appName = appNameForQ;
            enterTime = getArguments().getString("enterTimeForF");
            relatedId = getArguments().getInt("relatedIdF");
            notiId = getArguments().getString("notiId");
        }
        String apppendText = "";
        String title = apppendText+'\n' + "請選擇一張最後看到而且有印象的新聞";
        Log.d(TAG, title);
        questionRBTypeTextView.setText(title);
        pref.edit().putInt("position", 0).apply();
        getImages();
        //nextOrFinishButton.setEnabled(true);
    }

    Button.OnClickListener confirmClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            {
//                boolean newESM = pref.getBoolean("NewEsm", false);
//                Log.d(TAG, "NEW ESM: " + newESM);
//                if(newESM) {
//                    int EsmClick = pref.getInt("Esm_click", 0);
//                    EsmClick++;
//                    pref.edit().putInt("Esm_click", EsmClick).apply();
//                }
                String ChooseImage = mContext.getSharedPreferences("test", MODE_PRIVATE).getString("ChooseImage", "");

                String[] data = new String[]{"1", questionId, ChooseImage};
                Log.d(TAG, "test data: " + data[0] + " " + data[1] + " " + data[2]);
                insertChoiceInDatabase(data);

                //Log.d(TAG, "Chosse img: " + ChooseImage);
                if (mImages.size() != 0) {
                    if (ChooseImage.equals("")) {
                        CanFillEsm = false;
                        isDFinish = "2";
                        mContext.finish();
                    } else {
                        //pref.edit().putString("FilePath", ChooseImage).apply();
                        Set<String> DiaryPicture = pref.getStringSet("DiaryPicture", new HashSet<String>());
                        List<String> Diary_List = new ArrayList<String>(DiaryPicture);
                        Diary_List.add(ChooseImage);
                        DiaryPicture = new HashSet<String>(Diary_List);
                        pref.edit().putStringSet("DiaryPicture", DiaryPicture).apply();
                    }
                }
                else{
                    CanFillEsm = false;
                    isDFinish = "2";
                    mContext.finish();
                }
            }

//            startActivity(ESMintent);

            mImages.clear();
            mCheck.clear();
            ((QuestionActivity_diary) mContext).nextQuestion(6);
//            if (currentPagePosition == ((QuestionActivity) mContext).getTotalQuestionsSize())
//            {
//                mContext.finish();
//            }
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
        String ESMtime = pref.getString("ESMtime","NA");//2020-04-11 14:29:35
        Log.d(TAG,"ESMtime: " + ESMtime);
        String ESMdate = ESMtime.split(" ")[0];
        File imgFile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "News_Consumption/" + ESMdate);
        Log.d(TAG, "Path exists: " + imgFile);
//        CSVHelper.storeToCSV("RecycleView.csv","Path exists: " + imgFile);
        ArrayList<String> ImageCopy = new ArrayList<>();
        ArrayList<Boolean> CheckCopy = new ArrayList<>();
        mImages.clear();
        mCheck.clear();
        if(true) {
            if (imgFile.exists()) {
//            Toast.makeText(this, imgFile + "exists", Toast.LENGTH_LONG).show();
                File[] files = imgFile.listFiles();
                if (imgFile.listFiles() != null) {
                    for (int i = 0; i < files.length; i++) {
                        Log.d(TAG, "FileName:" + files[i].getName());
                        try {
                            if (!files[i].getName().split("\\.")[1].equals("csv")) {
                                if (ESMtime.equals("NA")) {
                                    ImageCopy.add(imgFile.toString() + "/" + files[i].getName());
                                    CheckCopy.add(false);
                                } else if (isInTenMinute(imgFile.toString() + "/" + files[i].getName(), ESMtime.split(" ")[1])) {
                                    ImageCopy.add(imgFile.toString() + "/" + files[i].getName());
                                    CheckCopy.add(false);
                                }
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                    Collections.sort(ImageCopy);
                    Collections.reverse(ImageCopy);
                }
                int s = ImageCopy.size();
                if (s != 0) {
                    Glide.with(this)
                            .asBitmap()
                            .load(ImageCopy.get(0))
                            .into(imageShow);
                    pref.edit().putString("ChooseImage", ImageCopy.get(0)).apply();
                    try {
                        String[] mImages_split = ImageCopy.get(0).split("/|-|\\.");
                        picture_time = mImages_split[mImages_split.length - 6] + ":" + mImages_split[mImages_split.length - 5]
                                + ":" + mImages_split[mImages_split.length - 4];
                        name.setText(picture_time);
                        Log.d(TAG, "time: " + picture_time);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    //mCheck.set(0, true);
                    mImages = ImageCopy;
                    mCheck = CheckCopy;
                    initRecyclerView();
                } else {
                    String apppendText = "";
                    if(notiId.equals("1")){
                        apppendText = "您於 " + enterTime + " 可能接觸到新聞";
                    }
                    questionRBTypeTextView.setText(apppendText + "\n因為這期間內沒有任何照片\n請按下一頁結束問卷");
                    name.setText("");
                    imageShow.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                }
            } else {
                Log.d(TAG, "Path NOT exists: " + imgFile);
                String apppendText = "";
                if(notiId.equals("1")){
                    apppendText = "您於 " + enterTime + " 可能接觸到新聞";
                }
                questionRBTypeTextView.setText(apppendText + "\n因為這期間內沒有任何照片\n請按下一頁結束問卷");
                name.setText("");
                imageShow.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
            }
        }
    }
    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: init recyclerview");
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = mContext.findViewById(R.id.recyclerView_frag);
        recyclerView.setLayoutManager(new MyLinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mContext, mImages, mCheck, 1);
        recyclerView.setAdapter(adapter);
    }

    private boolean isInTenMinute(String filename, String ESMtime){
        boolean result = false;
        String[] filname_split = filename.split("/");
        String date = filname_split[filname_split.length - 2];//2020-05-16
        String time = filname_split[filname_split.length - 1];//07-35-48.125-facebook.jpg
        String[] time_split = time.split("-|\\.");
        time = time_split[0] + ":" + time_split[1] + ":" + time_split[2];
        Long filetime = 0L;
        try {
            filetime = Long.parseLong(dateToStamp(date + " " + time));
        }catch(Exception e){
            e.printStackTrace();
        }
        Long nowESM_time = pref.getLong("Now_Esm_Time", 0);
        Long lastESM_time = pref.getLong("Last_Esm_Time", 0);
        Log.d(TAG, "After compute: " + filetime + " " + lastESM_time);
        if(filetime > lastESM_time && filetime < nowESM_time){
            result = true;
        }
        Log.d(TAG, "result: " + result);
        return result;
    }

    private void insertChoiceInDatabase(String[] data)
    {
        Observable.just(data)
                .map(new Function<String[], Object>() {
                    @Override
                    public Object apply(String[] data1) throws Exception {
                        return ChoosePictureFragment_diary.this.insertingInDb(data1);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    private String insertingInDb(String[] data)
    {
        String answerTime = getReadableTime(new Date().getTime());
        db.questionWithAnswersDao().updateQuestionPicture(data[0], data[1], data[2]);
        Log.d("qskip"," insertingInDb : selectionState : "+data[0] +"; questionId : "+data[1]+"; optionId : "+ data[2]);
        db.questionWithAnswersDao().updateQuestionPictureTime(answerTime ,data[1], data[2]);
        // updateFinalAnswer(data[1],data[2], data[0],answerTime);
        return "";
    }

    private String getTheStateOfPicture(String[] data)
    {
        return db.questionWithAnswersDao().isChecked(data[0], data[1]);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFirstLoad = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFirstLoad) {
            // 将数据加载逻辑放到onResume()方法中
            String[] data = new String[]{questionId, "0"};
            Observable.just(data)
                    .map(this::getTheStateOfPicture)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>()
                    {
                        @Override
                        public void onSubscribe(Disposable d)
                        {

                        }

                        @Override
                        public void onNext(String s)
                        {
                            qState = s;
                        }

                        @Override
                        public void onError(Throwable e)
                        {

                        }

                        @Override
                        public void onComplete()
                        {
                            Log.d(TAG,"setUserVisibleHint: qState = "+qState);
                        }
                    });
//            for (int i = 0; i < radioButtonArrayList.size(); i++)
//            {
//                RadioButton radioButton = radioButtonArrayList.get(i);
//                String cbPosition = String.valueOf(i);
//                String[] data = new String[]{questionId, cbPosition};
//                Log.d(TAG,"setUserVisibleHint i = "+ i + " data = "+ data.toString());
//
//                Observable.just(data)
//                        .map(this::getTheStateOfRadioBox)
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(new Observer<String>()
//                        {
//                            @Override
//                            public void onSubscribe(Disposable d)
//                            {
//
//                            }
//
//                            @Override
//                            public void onNext(String s)
//                            {
//                                qState = s;
//                            }
//
//                            @Override
//                            public void onError(Throwable e)
//                            {
//
//                            }
//
//                            @Override
//                            public void onComplete()
//                            {
//                                Log.d(TAG,"setUserVisibleHint: qState = "+qState);
//                                if (qState.equals("1"))
//                                {
//                                    radioButton.setChecked(true);
//                                } else if(qState.equals("0"))
//                                {
//                                    radioButton.setChecked(false);
//                                } else{
//                                    editText_answer.setText(qState);
//                                }
//                            }
//                        });
//            }
            isFirstLoad = false;
        }
    }
}
