package com.example.accessibility_detect.diarys;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.accessibility_detect.R;
import com.example.accessibility_detect.Utils;
import com.example.accessibility_detect.diarys.fragments_diary.CheckBoxesFragment_diary;
import com.example.accessibility_detect.diarys.fragments_diary.MultiSeekBarsFragment_diary;
import com.example.accessibility_detect.diarys.fragments_diary.OneSeekBarsFragment_diary;
import com.example.accessibility_detect.diarys.fragments_diary.RadioBoxesFragment_diary;
import com.example.accessibility_detect.diarys.fragments_diary.SeekBarsFragment_diary;
import com.example.accessibility_detect.diarys.fragments_diary.TextFragment_diary;
import com.example.accessibility_detect.diarys.fragments_diary.ValidationFragment_diary;
import com.example.accessibility_detect.diarys.questionmodels_diary.AnswerOptions_diary;
import com.example.accessibility_detect.diarys.questionmodels_diary.QuestionDataModel_diary;
import com.example.accessibility_detect.diarys.questionmodels_diary.QuestionsItem_diary;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.model.DataRecord.FinalAnswerDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.NewsDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.QuestionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.QuestionWithAnswersDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;
import labelingStudy.nctu.minuku.service.NotificationListenService;

import static com.example.accessibility_detect.Utils.Crop_Uri;
import static com.example.accessibility_detect.Utils.chrome;
import static com.example.accessibility_detect.Utils.cueRecallImg;
import static com.example.accessibility_detect.Utils.facebook;
import static com.example.accessibility_detect.Utils.instagram;
import static com.example.accessibility_detect.Utils.line;
import static com.example.accessibility_detect.Utils.line_mes;
import static com.example.accessibility_detect.Utils.messenger;
import static com.example.accessibility_detect.Utils.news;
import static com.example.accessibility_detect.Utils.ptt;
import static com.example.accessibility_detect.Utils.youtube;
import static labelingStudy.nctu.minuku.Utilities.CSVHelper.CSV_Diary;
import static labelingStudy.nctu.minuku.config.Constants.PICTURE_DIRECTORY_PATH;
import static labelingStudy.nctu.minuku.config.SharedVariables.CanFillDiary;
import static labelingStudy.nctu.minuku.config.SharedVariables.CanFillEsm;
import static labelingStudy.nctu.minuku.config.SharedVariables.D11_Answer;
import static labelingStudy.nctu.minuku.config.SharedVariables.D15Answer;
import static labelingStudy.nctu.minuku.config.SharedVariables.D15_number;
import static labelingStudy.nctu.minuku.config.SharedVariables.D32_Answer;
import static labelingStudy.nctu.minuku.config.SharedVariables.D32_No;
import static labelingStudy.nctu.minuku.config.SharedVariables.NoEsm;
import static labelingStudy.nctu.minuku.config.SharedVariables.NotiIdActiveSurvey;
import static labelingStudy.nctu.minuku.config.SharedVariables.NotiIdRandomMCNotiSurvey;
import static labelingStudy.nctu.minuku.config.SharedVariables.NotiIdRandomReminder;
import static labelingStudy.nctu.minuku.config.SharedVariables.answerid;
import static labelingStudy.nctu.minuku.config.SharedVariables.extraForQ;
import static labelingStudy.nctu.minuku.config.SharedVariables.isDFinish;
import static labelingStudy.nctu.minuku.config.SharedVariables.isFinish;
import static labelingStudy.nctu.minuku.config.SharedVariables.nowESM_time;
import static labelingStudy.nctu.minuku.config.SharedVariables.startAnswerTimeLong;
import static labelingStudy.nctu.minuku.config.SharedVariables.timeForQ;
import static labelingStudy.nctu.minuku.config.SharedVariables.todayMCount;

public class QuestionActivity_diary extends AppCompatActivity
{
    public static String TAG = "QuestionActivity_diary";
    final ArrayList<Fragment> fragmentArrayList = new ArrayList<>();
    public static List<QuestionsItem_diary> questionsItems = new ArrayList<>();
    appDatabase db;
    //private TextView questionToolbarTitle;
    private TextView questionPositionTV;
    public static String totalQuestions = "1";
    private Gson gson;
    private SharedPreferences pref;
    private ViewPager questionsViewPager;
    public String appName = "";
    public static Long finishAnswerTime = Long.valueOf(0);
    public String usertaskType = "";
    private Integer relatedIdForQ = 0;
    private long respondTime = 0L;
    private long nowDiary_time;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        db = appDatabase.getDatabase(QuestionActivity_diary.this);
        pref = getSharedPreferences("test", MODE_PRIVATE);
        // 2/14 回顧問卷alarm沒trigger
        UserDataRecord userDataRecord = db.userDataRecordDao().getLastRecord();
        int MinHour = 0;
        int MaxHour = 0;
        boolean isdiary = false;
        if(userDataRecord != null){
            MinHour = Integer.parseInt(userDataRecord.getquestionnaire_startTime());
            MaxHour = Integer.parseInt(userDataRecord.getquestionnaire_endTime());
            isdiary = userDataRecord.getDiaryClick();
        }

        Long now = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        if (false){
            Toast.makeText(getApplicationContext(),"您今天已經點擊過回顧問卷",Toast.LENGTH_LONG).show();
            isDFinish = "1";
            this.finish();
        }
        else{
            if(MinHour < MaxHour) {

                if (c.get(Calendar.HOUR_OF_DAY) < (MaxHour - 1) && c.get(Calendar.HOUR_OF_DAY) >= MinHour) {
                    Log.d(TAG, "This is not diary time");
                    Log.d(TAG, "Hours now is: " + c.get(Calendar.HOUR_OF_DAY) + " Min: " + MinHour + " Max: " + (MaxHour - 1));
                    Toast.makeText(getApplicationContext(),"現在並非回顧問卷填寫時間",Toast.LENGTH_LONG).show();
                    this.finish();
                }
                else{

                    pref.edit().putBoolean("NewDiary", false).apply();
                    UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();

                    if(userRecord != null)
                    {
                        db.userDataRecordDao().updateDiaryClick(userRecord.get_id(), true);
//                        long _id = userRecord.get_id();
//                        int questionnaireID = userRecord.getquestionnaireID();
//                        Log.d("QuestionActivity","questionnaireID: " + questionnaireID);
//                        db.userDataRecordDao().updateQuestionnaireID(_id, questionnaireID + 1);
//                        pref.edit().putInt("DiaryID", questionnaireID + 1).apply();
                    }
                    if(facebook || youtube || instagram || news || line || chrome || messenger || ptt || Utils.google || line_mes) {
                        pref.edit().putBoolean("Question_interrupt", true).apply();
                    }
                    else{
                        pref.edit().putBoolean("Question_interrupt", false).apply();
                    }
//                    pref.edit().putLong("Now_Diary_Time", System.currentTimeMillis()).apply();
//                    if(pref.getBoolean("IsDiaryDestroy", true)){
//                        pref.edit().putLong("Diary_SendDestroy", System.currentTimeMillis()).apply();
//                    }
//                    pref.edit().putBoolean("NewDiary", true).apply();
//                    int DiaryID = pref.getInt("DiaryID", 0);
//                    String Tdiary_str = userRecord.getTotalDiary_number();
//                    int DiaryNum = Integer.parseInt(Tdiary_str);
//                    int DiaryClick = Integer.valueOf(userRecord.getDiary_number());
//
//                    FinalAnswerDataRecord finalAnswerDataRecord = new FinalAnswerDataRecord();
//                    finalAnswerDataRecord.setGenerateTime(pref.getLong("Now_Diary_Time", 0));
//                    finalAnswerDataRecord.setRespondTime(0L); //點進問卷的時間
//                    finalAnswerDataRecord.setSubmitTime(0L);// onDestroy時間
//                    finalAnswerDataRecord.setisFinish("0");
//                    finalAnswerDataRecord.setQuesType("Diary");
//                    finalAnswerDataRecord.setreplyCount(String.valueOf(DiaryClick));
//                    finalAnswerDataRecord.settotalCount(String.valueOf(DiaryNum));
//                    finalAnswerDataRecord.setAnswerChoicePos("0");
//                    finalAnswerDataRecord.setAnswerChoiceState("1");
//                    finalAnswerDataRecord.setanswerId(String.valueOf(0));
//                    finalAnswerDataRecord.setdetectedTime(getReadableTime(System.currentTimeMillis()));
//                    finalAnswerDataRecord.setQuestionId("0");
//                    finalAnswerDataRecord.setsyncStatus(0);
//                    finalAnswerDataRecord.setRelatedId(DiaryID);
//                    finalAnswerDataRecord.setAnswerChoice("");
//                    finalAnswerDataRecord.setcreationIme(new Date().getTime());
//                    db.finalAnswerDao().insertAll(finalAnswerDataRecord);

                    Log.d(TAG, "Diary Click is true");

                    requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
                    getSupportActionBar().hide(); //hide the title bar

                    setContentView(R.layout.diary_activity_question);

                    toolBarInit();


                    gson = new Gson();
                    startAnswerTimeLong = new Date().getTime();
                    if (getIntent().getExtras() != null)
                    {
                        Bundle bundle = getIntent().getExtras();
                        parsingData(bundle,appName);
                    }

                    Log.d(TAG,"appName : "+appName);
                    Log.d(TAG,"usertaskType : "+usertaskType);
    //        Log.d(TAG,"relatedId : "+relatedId);
                }
            }
            else if(MaxHour < MinHour){
                if(c.get(Calendar.HOUR_OF_DAY) >= MinHour || c.get(Calendar.HOUR_OF_DAY) < (MaxHour - 1)){
                    Log.d(TAG, "This is not diary time");
                    Log.d(TAG, "Hours now is: " + c.get(Calendar.HOUR_OF_DAY) + " Min: " + MinHour + " Max: " + (MaxHour - 1));
                    Toast.makeText(getApplicationContext(),"現在並非回顧問卷填寫時間",Toast.LENGTH_LONG).show();
                    this.finish();
                }
                else{
                    pref = getSharedPreferences("test", MODE_PRIVATE);
                    pref.edit().putBoolean("NewDiary", false).apply();
                    UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();

                    if(userRecord != null)
                    {
                        db.userDataRecordDao().updateDiaryClick(userRecord.get_id(), true);
//                        long _id = userRecord.get_id();
//                        int questionnaireID = userRecord.getquestionnaireID();
//                        Log.d("QuestionActivity","questionnaireID: " + questionnaireID);
//                        db.userDataRecordDao().updateQuestionnaireID(_id, questionnaireID + 1);
//                        pref.edit().putInt("DiaryID", questionnaireID + 1).apply();
                    }
                    if(facebook || youtube || instagram || news || line || chrome || messenger || ptt || Utils.google || line_mes) {
                        pref.edit().putBoolean("Question_interrupt", true).apply();
                    }
                    else{
                        pref.edit().putBoolean("Question_interrupt", false).apply();
                    }
//                    pref.edit().putLong("Now_Diary_Time", System.currentTimeMillis()).apply();
//                    if(pref.getBoolean("IsDiaryDestroy", true)){
//                        pref.edit().putLong("Diary_SendDestroy", System.currentTimeMillis()).apply();
//                    }
//                    pref.edit().putBoolean("NewDiary", true).apply();
//                    int DiaryID = pref.getInt("DiaryID", 0);
//                    String Tdiary_str = userRecord.getTotalDiary_number();
//                    int DiaryNum = Integer.parseInt(Tdiary_str);
//                    int DiaryClick = Integer.valueOf(userRecord.getDiary_number());
//
//                    FinalAnswerDataRecord finalAnswerDataRecord = new FinalAnswerDataRecord();
//                    finalAnswerDataRecord.setGenerateTime(pref.getLong("Now_Diary_Time", 0));
//                    finalAnswerDataRecord.setRespondTime(0L); //點進問卷的時間
//                    finalAnswerDataRecord.setSubmitTime(0L);// onDestroy時間
//                    finalAnswerDataRecord.setisFinish("0");
//                    finalAnswerDataRecord.setQuesType("Diary");
//                    finalAnswerDataRecord.setreplyCount(String.valueOf(DiaryClick));
//                    finalAnswerDataRecord.settotalCount(String.valueOf(DiaryNum));
//                    finalAnswerDataRecord.setAnswerChoicePos("0");
//                    finalAnswerDataRecord.setAnswerChoiceState("1");
//                    finalAnswerDataRecord.setanswerId(String.valueOf(0));
//                    finalAnswerDataRecord.setdetectedTime(getReadableTime(System.currentTimeMillis()));
//                    finalAnswerDataRecord.setQuestionId("0");
//                    finalAnswerDataRecord.setsyncStatus(0);
//                    finalAnswerDataRecord.setRelatedId(DiaryID);
//                    finalAnswerDataRecord.setAnswerChoice("");
//                    finalAnswerDataRecord.setcreationIme(new Date().getTime());
//                    db.finalAnswerDao().insertAll(finalAnswerDataRecord);

                    Log.d(TAG, "Diary Click is true");

                    requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
                    getSupportActionBar().hide(); //hide the title bar

                    setContentView(R.layout.diary_activity_question);

                    toolBarInit();


                    gson = new Gson();
                    startAnswerTimeLong = new Date().getTime();
                    if (getIntent().getExtras() != null)
                    {
                        Bundle bundle = getIntent().getExtras();
                        parsingData(bundle,appName);
                    }

                    Log.d(TAG,"appName : "+appName);
                    Log.d(TAG,"usertaskType : "+usertaskType);
    //        Log.d(TAG,"relatedId : "+relatedId);
                }
            }
        }
        //

//        pref = getSharedPreferences("test", MODE_PRIVATE);
//        pref.edit().putBoolean("NewDiary", false).apply();
//        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
//
//        if(userRecord != null)
//        {
//            db.userDataRecordDao().updateDiaryClick(userRecord.get_id(), true);
//        }
//        if(facebook || youtube || instagram || news || line || chrome || messenger || ptt || Utils.google || line_mes) {
//            pref.edit().putBoolean("Question_interrupt", true).apply();
//        }
//        else{
//            pref.edit().putBoolean("Question_interrupt", false).apply();
//        }
////        userRecord.setDiaryClick(true);
//        Log.d(TAG, "Diary Click is true");
////        pref.edit().putBoolean("DiaryClick", true).apply();
//        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
//        getSupportActionBar().hide(); //hide the title bar
//
//        setContentView(R.layout.diary_activity_question);
//
//        toolBarInit();
//
//
//        gson = new Gson();
//        startAnswerTimeLong = new Date().getTime();
//        if (getIntent().getExtras() != null)
//        {
//            Bundle bundle = getIntent().getExtras();
//            parsingData(bundle,appName);
//        }
//
//        Log.d(TAG,"appName : "+appName);
//        Log.d(TAG,"usertaskType : "+usertaskType);
////        Log.d(TAG,"relatedId : "+relatedId);
    }

    private void toolBarInit()
    {
        Toolbar questionToolbar = findViewById(R.id.diary_questionToolbar);
        questionToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        questionToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                QuestionActivity_diary.this.onBackPressed();
            }
        });

        //questionToolbarTitle = questionToolbar.findViewById(R.id.questionToolbarTitle);
        questionPositionTV = questionToolbar.findViewById(R.id.diary_questionPositionTV);

        //questionToolbarTitle.setText("Questions");
    }

    /*This method decides how many Question-Screen(s) will be created and
    what kind of (Multiple/Single choices) each Screen will be.*/
    private void parsingData(Bundle bundle, String appName)
    {
//        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
//        long _id = 0L;
//        if(userRecord != null) {
//            _id = userRecord.get_id();
////            relatedIdForQ = userRecord.getquestionnaireID();
//        }
        relatedIdForQ = pref.getInt("DiaryID", 0);
        nowDiary_time = getSharedPreferences("test",MODE_PRIVATE).getLong("Now_Diary_Time", 0);

//        relatedIdForQ = pref.getInt("QuestionnaireID", -1);
//        relatedIdForQ++;

        // 12/16紀錄ESM的第三種狀態(沒回答消失)
//        db.finalAnswerDao().deleteDataByID(relatedIdForQ);
        /////
//        db.userDataRecordDao().updateQuestionnaireID(_id, relatedIdForQ);
//        pref.edit().putInt("QuestionnaireID", relatedIdForQ).apply();
        Log.d(TAG, "Questionnaire id = " + relatedIdForQ);

        boolean isDestroy = pref.getBoolean("IsDestroy",true);
        boolean isDiaryDestroy = pref.getBoolean("IsDiaryDestroy",true);

        if(!isDestroy && isDiaryDestroy){
            Log.d(TAG, "Did not destroy");
            UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
            if(userRecord != null) {
                db.userDataRecordDao().updateCanFillESM(userRecord.get_id(), false);
            }
            CanFillEsm = false;
            CSVHelper.storeToCSV(CSV_Diary, "previous ESM is not destroy");
            insertESMFinalAnswer(isDestroy);
        }

        pref.edit().putBoolean("IsDiaryDestroy", false).apply();
        pref.edit().putBoolean("IsDestroy", true).apply();
        answerid = 185;
        NoEsm = false;

        isDFinish = "1";
        D32_No = false;
        D32_Answer.clear();
        D32_Answer.put(0, "");
        D32_Answer.put(1, "");
        D32_Answer.put(2, "");

        D15Answer.clear();
        D15Answer.add("");
        D15Answer.add("");
        D15Answer.add("");
//        DiaryrespondTime = System.currentTimeMillis();
        pref.edit().putLong("DiaryrespondTime",System.currentTimeMillis()).apply();
        QuestionDataModel_diary questionDataModel = new QuestionDataModel_diary();

        String json_string = bundle.getString("json_questions");
//        JSONObject jsonObj = null;
//        try {
//            jsonObj = new JSONObject(json_string);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//
//        try {
//            List<String> cont = Question_continue();
//            JSONObject data = jsonObj.getJSONObject("data");
//            JSONArray ques = data.getJSONArray("questions");
//
//            for(int j = 0; j < cont.size(); j++){
//                JSONObject jobject = null;
//                try {
//                    jobject = new JSONObject(cont.get(j));
//                }catch (JSONException e){
//                    e.printStackTrace();
//                }
//                ques.put(jobject);
//            }
//            data.remove("questions");
//            data.put("questions",ques);
//            jsonObj.remove("data");
//            jsonObj.put("data", data);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        json_string = jsonObj.toString();
//        Log.d(TAG, json_string);


        questionDataModel = gson.fromJson(json_string, QuestionDataModel_diary.class);
//        relatedIdForQ = bundle.getInt("relatedIdForQ",-1);
        String notiId = bundle.getString("notiId","");
        Log.d(TAG, "testing: " + relatedIdForQ + " " + notiId);
        questionsItems = questionDataModel.getData().getQuestions();

        totalQuestions = "43";
//        totalQuestions = String.valueOf(questionsItems.size());
        String questionPosition = "1/" + totalQuestions;
        setTextWithSpan(questionPosition);

        preparingQuestionInsertionInDb(questionsItems);
        Log.d(TAG,"parsingData :"+relatedIdForQ);
        preparingInsertionInDb(questionsItems,relatedIdForQ);

        for (int i = 0; i < questionsItems.size(); i++)
        {
            QuestionsItem_diary question = questionsItems.get(i);

            if (question.getQuestionTypeName().equals("CheckBox"))
            {
                CheckBoxesFragment_diary checkBoxesFragment = new CheckBoxesFragment_diary();
                Bundle checkBoxBundle = new Bundle();
                checkBoxBundle.putParcelable("question", question);
                checkBoxBundle.putInt("page_position", i);
                checkBoxBundle.putString("appNameForF",appName);
                checkBoxBundle.putInt("relatedIdF",relatedIdForQ);
                checkBoxBundle.putString("enterTimeForF",timeForQ);
                checkBoxBundle.putString("notiId",notiId);
                checkBoxesFragment.setArguments(checkBoxBundle);
                fragmentArrayList.add(checkBoxesFragment);
            }

            if (question.getQuestionTypeName().equals("Radio"))
            {
                RadioBoxesFragment_diary radioBoxesFragment = new RadioBoxesFragment_diary();
                Bundle radioButtonBundle = new Bundle();
                radioButtonBundle.putParcelable("question", question);
                radioButtonBundle.putInt("page_position", i);
                radioButtonBundle.putString("appNameForF",appName);
                radioButtonBundle.putInt("relatedIdF",relatedIdForQ);
                radioButtonBundle.putString("enterTimeForF",timeForQ);
                radioButtonBundle.putString("notiId",notiId);
                radioBoxesFragment.setArguments(radioButtonBundle);
                fragmentArrayList.add(radioBoxesFragment);
            }
            if (question.getQuestionTypeName().equals("SeekBar"))
            {
                SeekBarsFragment_diary seekBarsFragment = new SeekBarsFragment_diary();
                Bundle seekBarBundle = new Bundle();
                seekBarBundle.putParcelable("question", question);
                seekBarBundle.putInt("page_position", i);
                seekBarBundle.putString("appNameForF",appName);
                seekBarBundle.putInt("relatedIdF",relatedIdForQ);
                seekBarBundle.putString("enterTimeForF",timeForQ);
                seekBarBundle.putString("notiId",notiId);
                seekBarsFragment.setArguments(seekBarBundle);
                fragmentArrayList.add(seekBarsFragment);
            }
            if (question.getQuestionTypeName().equals("OneSeekBar"))
            {
                OneSeekBarsFragment_diary cueFragment = new OneSeekBarsFragment_diary();
                Bundle cueBundle = new Bundle();
                cueBundle.putParcelable("question", question);
                cueBundle.putInt("page_position", i);
                cueBundle.putString("appNameForF",appName);
                cueBundle.putInt("relatedIdF",relatedIdForQ);
                cueBundle.putString("enterTimeForF",timeForQ);
                cueBundle.putString("notiId",notiId);
                cueFragment.setArguments(cueBundle);
                fragmentArrayList.add(cueFragment);
            }
            if (question.getQuestionTypeName().equals("Text"))
            {
                TextFragment_diary textFragment = new TextFragment_diary();
                Bundle textBundle = new Bundle();
                textBundle.putParcelable("question", question);
                textBundle.putInt("page_position", i);
                textBundle.putString("appNameForF",appName);
                textBundle.putInt("relatedIdF",relatedIdForQ);
                textBundle.putString("enterTimeForF",timeForQ);
                textBundle.putString("notiId",notiId);
                textFragment.setArguments(textBundle);
                fragmentArrayList.add(textFragment);
            }
            if (question.getQuestionTypeName().equals("MultiSeekBar"))
            {
                MultiSeekBarsFragment_diary textFragment = new MultiSeekBarsFragment_diary();
                Bundle textBundle = new Bundle();
                textBundle.putParcelable("question", question);
                textBundle.putInt("page_position", i);
                textBundle.putString("appNameForF",appName);
                textBundle.putInt("relatedIdF",relatedIdForQ);
                textBundle.putString("enterTimeForF",timeForQ);
                textBundle.putString("notiId",notiId);
                textFragment.setArguments(textBundle);
                fragmentArrayList.add(textFragment);
            }
            if (question.getQuestionTypeName().equals("MultiText"))
            {
                ValidationFragment_diary textFragment = new ValidationFragment_diary();
                Bundle textBundle = new Bundle();
                textBundle.putParcelable("question", question);
                textBundle.putInt("page_position", i);
                textBundle.putString("appNameForF",appName);
                textBundle.putInt("relatedIdF",relatedIdForQ);
                textBundle.putString("enterTimeForF",timeForQ);
                textBundle.putString("notiId",notiId);
                textFragment.setArguments(textBundle);
                fragmentArrayList.add(textFragment);
            }
        }
        questionsViewPager = findViewById(R.id.pager);
        questionsViewPager.setOffscreenPageLimit(1);
        MyPagerAdapter mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, fragmentArrayList);
        questionsViewPager.setAdapter(mPagerAdapter);
    }

    public void nextQuestion(int offset) //default 1
    {
        int item = questionsViewPager.getCurrentItem() + offset;
        Log.d("Checkoffset","item : "+ item);
        questionsViewPager.setCurrentItem(item);

        String currentQuestionPosition = String.valueOf(item+1);

        Log.d("Checkoffset","currentQuestionPosition : "+ currentQuestionPosition);

        totalQuestions = "43";
        int total;
//        if(D9Answer.size() != 0) {
//            total = Integer.parseInt(totalQuestions) + validation_following * (D9Answer.size() - 1);
//            totalQuestions = String.valueOf(total);
//        }
//        else{
//            total = Integer.parseInt(totalQuestions);
//        }
//        if(currentQuestionPosition.equals("55"))currentQuestionPosition = String.valueOf(total - 3);
//        if(currentQuestionPosition.equals("56"))currentQuestionPosition = String.valueOf(total - 2);
//        if(currentQuestionPosition.equals("57"))currentQuestionPosition = String.valueOf(total - 1);
//        if(currentQuestionPosition.equals("58"))currentQuestionPosition = String.valueOf(total);

        String questionPosition = currentQuestionPosition + "/" + totalQuestions;
        setTextWithSpan(questionPosition);
    }
    public void prevQuestion(int offset) //default 1
    {
        int item = questionsViewPager.getCurrentItem() - offset;
        questionsViewPager.setCurrentItem(item);

        String currentQuestionPosition = String.valueOf(item - offset);

        String questionPosition = currentQuestionPosition + "/" + totalQuestions;
        setTextWithSpan(questionPosition);
    }


    public int getTotalQuestionsSize()
    {
        return questionsItems.size();
    }

    private void preparingQuestionInsertionInDb(List<QuestionsItem_diary> questionsItems)
    {
        List<QuestionDataRecord> questionEntities = new ArrayList<>();

        for (int i = 0; i < questionsItems.size(); i++)
        {
            QuestionDataRecord questionDataRecord = new QuestionDataRecord();
            questionDataRecord.setQuestionId(questionsItems.get(i).getId());
            questionDataRecord.setQuestion(questionsItems.get(i).getQuestionName());

            questionEntities.add(questionDataRecord);
        }
        insertQuestionInDatabase(questionEntities);
    }

    private void insertQuestionInDatabase(List<QuestionDataRecord> questionEntities)
    {
        Observable.just(questionEntities)
                .map(this::insertingQuestionInDb)
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    /*First, clear the table, if any previous data saved in it. Otherwise, we get repeated data.*/
    private String insertingQuestionInDb(List<QuestionDataRecord> questionEntities)
    {
        db.questionDao().deleteAllQuestions();
        db.questionDao().insertAllQuestions(questionEntities);
        return "";
    }

    private void preparingInsertionInDb(List<QuestionsItem_diary> questionsItems, int relatedId)
    {
        ArrayList<QuestionWithAnswersDataRecord> questionWithChoicesEntities = new ArrayList<>();

        for (int i = 0; i < questionsItems.size(); i++)
        {
            List<AnswerOptions_diary> answerOptions = questionsItems.get(i).getAnswerOptions();

            for (int j = 0; j < answerOptions.size(); j++)
            {
                QuestionWithAnswersDataRecord questionWithAnswersDataRecord = new QuestionWithAnswersDataRecord();
                questionWithAnswersDataRecord.setQuestionId(String.valueOf(questionsItems.get(i).getId()));
                questionWithAnswersDataRecord.setAnswerChoice(answerOptions.get(j).getName());
                questionWithAnswersDataRecord.setAnswerChoicePosition(String.valueOf(j));
                questionWithAnswersDataRecord.setAnswerChoiceId(answerOptions.get(j).getAnswerId());
                questionWithAnswersDataRecord.setAnswerChoiceState("-1");
                questionWithAnswersDataRecord.setcreationIme(new Date().getTime());
                questionWithAnswersDataRecord.setdetectedTime("0");
                questionWithAnswersDataRecord.setRelatedId(relatedId);

                questionWithChoicesEntities.add(questionWithAnswersDataRecord);
            }
        }

        insertQuestionWithChoicesInDatabase(questionWithChoicesEntities);
    }

    private void insertQuestionWithChoicesInDatabase(List<QuestionWithAnswersDataRecord> questionWithChoicesEntities)
    {
        Observable.just(questionWithChoicesEntities)
                .map(this::insertingQuestionWithChoicesInDb)
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    /*First, clear the table, if any previous data saved in it. Otherwise, we get repeated data.*/
    private String insertingQuestionWithChoicesInDb(List<QuestionWithAnswersDataRecord> questionWithChoicesEntities)
    {
        db.questionWithAnswersDao().deleteAllChoicesOfQuestion();
        db.questionWithAnswersDao().insertAllChoicesOfQuestion(questionWithChoicesEntities);
        return "";
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBackPressed()
    {
        if (questionsViewPager.getCurrentItem() == 0)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name);
            builder.setMessage("回上頁將會清除目前填答結果，且將無法填答。確定返回上一頁？");
            builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
                    if(userRecord != null) {
                        db.userDataRecordDao().updateCanFillDiary(userRecord.get_id(), false);
                    }
                    CanFillDiary = false;
                    CSVHelper.storeToCSV(CSV_Diary, "Diary back pressed");
                    QuestionActivity_diary.this.finish();
                }
            });

            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();

        }
        else
        {
            int item = questionsViewPager.getCurrentItem() - 1;
            Log.d(TAG, "item"+ item);
            String currentQuestionPosition;
            if((item + 1) == 5) {//第6題回1
                if(check_radio_answer("1") == 0){ //轉傳0則
                    questionsViewPager.setCurrentItem(0);
                    currentQuestionPosition = String.valueOf(1);
                }
                else{
                    questionsViewPager.setCurrentItem(item);
                    currentQuestionPosition = String.valueOf(item + 1);
                }
            }
//            else if((item + 1) == 8) {//第9題回5
//                if(D6_Answer == 0){
//                    questionsViewPager.setCurrentItem(item);
//                    currentQuestionPosition = String.valueOf(item + 1);
//                }
//                else {
//                    questionsViewPager.setCurrentItem(4);
//                    currentQuestionPosition = String.valueOf(5);
//                }
//            }
            else if((item + 1) == 14) {//第15題
                if(check_radio_answer("6") == 0){
                    if(D11_Answer == 0) {
                        questionsViewPager.setCurrentItem(item);
                        currentQuestionPosition = String.valueOf(item + 1);
                    }
                    else{
                        questionsViewPager.setCurrentItem(10);
                        currentQuestionPosition = String.valueOf(11);
                    }
                }
                else{
                    questionsViewPager.setCurrentItem(5);
                    currentQuestionPosition = String.valueOf(6);
                }
            }
//            else if((item + 1) == 13) {//第14題
//                if(check_radio_answer("1") == 0){
//                    questionsViewPager.setCurrentItem(12);
//                    currentQuestionPosition = String.valueOf(13);
//                }
//                else{
//                    questionsViewPager.setCurrentItem(0);
//                    currentQuestionPosition = String.valueOf(1);
//                }
//            }
            else if((item + 1) == 16) {//第17題
                questionsViewPager.setCurrentItem(14);
                currentQuestionPosition = String.valueOf(15);
            }
            else if((item + 1) == 19) {//第20題
                if(D15_number == 1){
                    questionsViewPager.setCurrentItem(16);
                    currentQuestionPosition = String.valueOf(17);
                }
                else if(D15_number == 2){
                    questionsViewPager.setCurrentItem(17);
                    currentQuestionPosition = String.valueOf(18);
                }
                else{
                    questionsViewPager.setCurrentItem(item);
                    currentQuestionPosition = String.valueOf(item + 1);
                }
            }
            else if((item + 1) == 22) {//第23題
                if(D15_number == 1){
                    questionsViewPager.setCurrentItem(19);
                    currentQuestionPosition = String.valueOf(20);
                }
                else if(D15_number == 2){
                    questionsViewPager.setCurrentItem(20);
                    currentQuestionPosition = String.valueOf(21);
                }
                else{
                    questionsViewPager.setCurrentItem(item);
                    currentQuestionPosition = String.valueOf(item + 1);
                }
            }
            else if((item + 1) == 25) {//第26題
                if(D15_number == 1){
                    questionsViewPager.setCurrentItem(19);
                    currentQuestionPosition = String.valueOf(20);
                }
                else if(D15_number == 2){
                    questionsViewPager.setCurrentItem(22);
                    currentQuestionPosition = String.valueOf(23);
                }
                else{
                    questionsViewPager.setCurrentItem(item);
                    currentQuestionPosition = String.valueOf(item + 1);
                }
            }
//            else if((item + 1) == 27) {//第28題
//                if(D14_number == 1){
//                    questionsViewPager.setCurrentItem(24);
//                    currentQuestionPosition = String.valueOf(25);
//                }
//                else if(D14_number == 2){
//                    questionsViewPager.setCurrentItem(25);
//                    currentQuestionPosition = String.valueOf(26);
//                }
//                else{
//                    questionsViewPager.setCurrentItem(26);
//                    currentQuestionPosition = String.valueOf(27);
//                }
//            }
            else if((item + 1) == 31) {//第32題
                if(D15_number == 0){
                    questionsViewPager.setCurrentItem(14);
                    currentQuestionPosition = String.valueOf(15);
                }
                else if(D15_number == 1){
                    questionsViewPager.setCurrentItem(26);
                    currentQuestionPosition = String.valueOf(27);
                }
                else if(D15_number == 2){
                    questionsViewPager.setCurrentItem(28);
                    currentQuestionPosition = String.valueOf(29);
                }
                else{
                    questionsViewPager.setCurrentItem(30);
                    currentQuestionPosition = String.valueOf(31);
                }
            }
            else if((item + 1) == 33) {//第34題
                questionsViewPager.setCurrentItem(31);
                currentQuestionPosition = String.valueOf(32);
            }
            else if((item + 1) == 36) {//第37題
                if(D32_Answer.get(0).equals("")){
                    questionsViewPager.setCurrentItem(31);
                    currentQuestionPosition = String.valueOf(32);
                }
                else{
                    questionsViewPager.setCurrentItem(item);
                    currentQuestionPosition = String.valueOf((item + 1));
                }
            }
            else if((item + 1) == 39) {//第40題
                if(D32_Answer.get(0).equals("") && D32_Answer.get(1).equals("")){
                    questionsViewPager.setCurrentItem(31);
                    currentQuestionPosition = String.valueOf(32);
                }
                else if(D32_Answer.get(1).equals("") && !D32_Answer.get(0).equals("")){
                    questionsViewPager.setCurrentItem(35);
                    currentQuestionPosition = String.valueOf(36);
                }
                else{
                    questionsViewPager.setCurrentItem(item);
                    currentQuestionPosition = String.valueOf((item + 1));
                }
            }
            else if((item + 1) == 42){//43
                if(D15_number == 0){
                    questionsViewPager.setCurrentItem(14);
                    currentQuestionPosition = String.valueOf(15);
                }
                else{
                    if(D32_No){
                        questionsViewPager.setCurrentItem(31);
                        currentQuestionPosition = String.valueOf(32);
                    }
                    else if(!D32_Answer.get(2).equals("")){
                        questionsViewPager.setCurrentItem(41);
                        currentQuestionPosition = String.valueOf(42);
                    }
                    else if(!D32_Answer.get(1).equals("")){
                        questionsViewPager.setCurrentItem(38);
                        currentQuestionPosition = String.valueOf(39);
                    }
                    else {
                        questionsViewPager.setCurrentItem(35);
                        currentQuestionPosition = String.valueOf(36);
                    }
                }
            }
            else{
                questionsViewPager.setCurrentItem(item);
                currentQuestionPosition = String.valueOf(item + 1);
            }
//            if((item + 1) == 4){//第5題要看第1題答案決定跳回哪一頁
//                if(NoEsm){
//                    questionsViewPager.setCurrentItem(1);
//                    currentQuestionPosition = String.valueOf(2);
//                }
//                else if(check_radio_answer("1") == 0){//有的話回上一題(4)
//                    questionsViewPager.setCurrentItem(3);
//                    currentQuestionPosition = String.valueOf(4);
//                }
//                else{//沒有的話回第1題
//                    questionsViewPager.setCurrentItem(0);
//                    currentQuestionPosition = String.valueOf(1);
//                }
//            }
//            else if((item + 1) == 54) //第55題 回去第6題
//            {
//                questionsViewPager.setCurrentItem(5);
//                currentQuestionPosition = String.valueOf(6);
//            }
//            else if((item + 1) == 55) //第56題 回去第6題
//            {
//                int Q5ans = check_radio_answer("5");
//                int Q6ans = check_radio_answer("6");
//                if(Q5ans == 0 && Q6ans == 0){
//                    questionsViewPager.setCurrentItem(13 + validation_following*(D9Answer.size() - 1));
//                    currentQuestionPosition = String.valueOf(14 + validation_following*(D9Answer.size() - 1));
//                }
//                else if(Q5ans == 0 && Q6ans == 1){
//                    questionsViewPager.setCurrentItem(54);
//                    currentQuestionPosition = String.valueOf(55);
//                }
//                else{
//                    questionsViewPager.setCurrentItem(4);
//                    currentQuestionPosition = String.valueOf(5);
//                }
//            }
//            else if((item + 1) == 8) { //第9題 回去第7題
//                questionsViewPager.setCurrentItem(6);
//                currentQuestionPosition = String.valueOf(7);
//            }
//            else if((item + 1) == 10) { //第11題 回去第9題
//                questionsViewPager.setCurrentItem(8);
//                currentQuestionPosition = String.valueOf(9);
//            }
//            else{
//                questionsViewPager.setCurrentItem(item);
//                currentQuestionPosition = String.valueOf(item + 1);
//            }

//            int total;
//            String total_str = "18";
//            if(D9Answer.size() != 0) {
//                total = Integer.parseInt(total_str) + validation_following * (D9Answer.size() - 1);
//                total_str = String.valueOf(total);
//            }
//            else{
//                total = Integer.parseInt(total_str);
//            }
//            if(currentQuestionPosition.equals("55"))currentQuestionPosition = String.valueOf(total - 3);
//            if(currentQuestionPosition.equals("56"))currentQuestionPosition = String.valueOf(total - 2);
//            if(currentQuestionPosition.equals("57"))currentQuestionPosition = String.valueOf(total - 1);
//            if(currentQuestionPosition.equals("58"))currentQuestionPosition = String.valueOf(total);

            String questionPosition = currentQuestionPosition + "/" + totalQuestions;
            setTextWithSpan(questionPosition);
//            int item = questionsViewPager.getCurrentItem() - 1;
//            int  itemCheck = -1 ;
//            if(pageRecord.contains(item+1)) {
//                questionsViewPager.setCurrentItem(item);
//                itemCheck = item;
//            }else{
//                if(pageRecord.size()!=0) {
//                    questionsViewPager.setCurrentItem(pageRecord.get(pageRecord.size() - 1)-1);
//                    itemCheck = pageRecord.get(pageRecord.size() - 1);
//                }else{
//                    super.onBackPressed();
//                }
//
//            }
//            String currentQuestionPosition = String.valueOf(itemCheck + 1);
//            String questionPosition = currentQuestionPosition + "/" + totalQuestions;
//            setTextWithSpan(questionPosition);
//            for(int tmp : pageRecord){
//                Log.d("qskip","pageRecord" + tmp);
//            }
//            Log.d("qskip","pageRecord size" + pageRecord.size());
//            if(pageRecord.size()!=0) {
//                pageRecord.remove(pageRecord.size() - 1);
//            }
        }
    }

    public Integer check_radio_answer(String questionId){
        if (questionId.equals("1")) {
            String first = db.questionWithAnswersDao().isChecked(questionId, "0");

            if (first != null) {
                Log.d(TAG, " radioBox first :  " + first);
                if (first.equals("0")) {
                    return 0;   // 是
                }
                else{
                    return 1;
                }
            }

//        else if(questionId.equals("2")){//0:截圖, 1:mes, 2:line, 3:ptt, 4:noti
//            String messenger = db.questionWithAnswersDao().isChecked(questionId,"1");
//            String line = db.questionWithAnswersDao().isChecked(questionId,"2");
//            String noti = db.questionWithAnswersDao().isChecked(questionId,"6");
//            String ptt = db.questionWithAnswersDao().isChecked(questionId,"8");
//            Log.d(TAG, "Checked: " + messenger + " " + line + " " + noti + " " + ptt);
//            if(messenger != null){
//                Log.d(TAG, " radioBox messenger :  " + messenger);
//                if (messenger.equals("1")) {
//                    return 1;
//                }
//            }
//            if(line != null){
//                Log.d(TAG, " radioBox line :  " + line);
//                if (line.equals("1")) {
//                    return 2;
//                }
//            }
//            if(ptt != null){
//                Log.d(TAG, " radioBox ptt :  " + ptt);
//                if (ptt.equals("1")) {
//                    return 3;
//                }
//            }
//            if(noti != null){
//                Log.d(TAG, " radioBox noti :  " + noti);
//                if (noti.equals("1")) {
//                    return 4;
//                }
//            }
//        }
            return 0;
        }
        else{
            String first = db.questionWithAnswersDao().isChecked(questionId, "0");
            String second = db.questionWithAnswersDao().isChecked(questionId, "1");

            Log.d(TAG, "有: " + first + " 沒有" + second);
            if (first != null) {
                Log.d(TAG, " radioBox first :  " + first);
                if (first.equals("1")) {
                    return 0;   // 是
                }
            }
            if (second != null) {
                Log.d(TAG, " radioBox second :  " + second);
                if (second.equals("1")) {
                    return 1;  //否
                }
            }
//        else if(questionId.equals("2")){//0:截圖, 1:mes, 2:line, 3:ptt, 4:noti
//            String messenger = db.questionWithAnswersDao().isChecked(questionId,"1");
//            String line = db.questionWithAnswersDao().isChecked(questionId,"2");
//            String noti = db.questionWithAnswersDao().isChecked(questionId,"6");
//            String ptt = db.questionWithAnswersDao().isChecked(questionId,"8");
//            Log.d(TAG, "Checked: " + messenger + " " + line + " " + noti + " " + ptt);
//            if(messenger != null){
//                Log.d(TAG, " radioBox messenger :  " + messenger);
//                if (messenger.equals("1")) {
//                    return 1;
//                }
//            }
//            if(line != null){
//                Log.d(TAG, " radioBox line :  " + line);
//                if (line.equals("1")) {
//                    return 2;
//                }
//            }
//            if(ptt != null){
//                Log.d(TAG, " radioBox ptt :  " + ptt);
//                if (ptt.equals("1")) {
//                    return 3;
//                }
//            }
//            if(noti != null){
//                Log.d(TAG, " radioBox noti :  " + noti);
//                if (noti.equals("1")) {
//                    return 4;
//                }
//            }
//        }
            return 0;
        }
    }
    private void setTextWithSpan(String questionPosition)
    {
        int slashPosition = questionPosition.indexOf("/");

        Spannable spanText = new SpannableString(questionPosition);
        spanText.setSpan(new RelativeSizeSpan(0.7f), slashPosition, questionPosition.length(), 0);
        questionPositionTV.setText(spanText);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

        if(isDFinish.equals("2")) {
            pref.edit().putBoolean("IsDiaryDestroy", true).apply();

//            UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
//            if(userRecord != null) {
//                db.userDataRecordDao().updateCanFillDiary(userRecord.get_id(), false);
//            }
            CanFillDiary = false;

            finishAnswerTime = new Date().getTime();
            CSVHelper.storeToCSV(CSV_Diary, "Diary is finish");
            Toast.makeText(this, "感謝您的填答 !", Toast.LENGTH_LONG).show();

            int DiaryClick = 0;
            UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
            if(userRecord != null) {
                db.userDataRecordDao().updateCanFillDiary(userRecord.get_id(), false);
                DiaryClick = Integer.valueOf(userRecord.getDiary_number());
            }
//            int DiaryClick = pref.getInt("Diary_click", 0);
            DiaryClick++;

            if(userRecord != null) {
                long _id = userRecord.get_id();
                db.userDataRecordDao().updateDiary(_id, String.valueOf(DiaryClick));
            }
//            pref.edit().putInt("Diary_click", DiaryClick).apply();
            insertFinalAnswer();
//            updateResponse();
        }
        else{
            CSVHelper.storeToCSV(CSV_Diary, "Diary is not finish");
        }

//        canFillQuestionnaire = false;
//        TextView text = (TextView)findViewById(R.id.control_questionnaire);
//        text.setText(R.string.no_questionnaire);
//        if(questionaireType == 0)
        {
//            Toast.makeText(this, "感謝您的填答 !", Toast.LENGTH_SHORT).show();
//            Toast.makeText(this, "若您有錄影片，請至實驗紀錄上傳影片 !", Toast.LENGTH_LONG).show();
            // Log.d("BootCompleteReceiver","In MainActivity to 感謝您的填答");
        }
//        else{
//            Toast.makeText(this, "感謝您的填答 !", Toast.LENGTH_LONG).show();
//        }
        //pageRecord.clear();
        extraForQ = "";
        Log.d("BootCompleteReceiver","In MainActivity todayMCount : "+todayMCount);

//        insertFinalAnswer();
//        updateResponse();
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                //cancelNotification();
//                insertFinalAnswer();
//                updateResponse();
//            }
//        });
    }
    public void cancelNotification(){
        // 主動執行問卷後，sample 的問卷會消失
        // random sample 後的情境資訊 並不會影響到主動執行的問卷
        if(NotificationListenService.checkAnyNotiExist(this,NotiIdRandomReminder)){
            NotificationListenService.cancelNotification(this,NotiIdRandomReminder);
        }
        NotificationListenService.cancelNotification(this,NotiIdActiveSurvey);
        NotificationListenService.cancelNotification(this,NotiIdRandomMCNotiSurvey);

        //NotificationListenService.cancelNotification(this,NotiIdRandomSurvey);
        // Log.d("BootCompleteReceiver","In MainActivity cancelNotification");
    }
    public String getESMcount(){
        String TodayResponseSet = "";
        String TotalResponseSet = "";
        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
        if(userRecord != null) {
            TodayResponseSet = userRecord.getESM_number();
            TotalResponseSet = userRecord.getTotal_ESM_number();
        }

//        String TodayResponseSet = pref.getString("TodayResponse", "");
        Log.d(TAG, TodayResponseSet);
        String[] TodayResponse = TodayResponseSet.split(",");

//        String TotalResponseSet = pref.getString("TotalResponse", "");
        Log.d(TAG, TotalResponseSet);
        String[] TotalResponse = TotalResponseSet.split(",");

        int esmTNum = 0;
        int esmNum = 0;

        for(int i = 0; i < TotalResponse.length; i++){
            esmNum += Integer.parseInt(TodayResponse[i]);
            esmTNum += Integer.parseInt(TotalResponse[i]);
        }

//        int esmTNum = 0;
//        int esmNum = 0;
//        int ResponseSize = TotalResponse.length + 1;
//        Log.d(TAG, "size: " + ResponseSize);
//        int i = 0;
//        if( ResponseSize == 1)i = 0;
//        if( ResponseSize > 1)i = 1;
//        for(; i < ResponseSize; i++){
//            Log.d(TAG, "i: " + i);
//            if(i == (ResponseSize - 1)){
//                String TodayRCount = String.valueOf(pref.getInt(todayMCountString, 0));
//                String TodayCount = String.valueOf(pref.getInt("Esm_Num", 0));
//                esmNum += Integer.parseInt(TodayRCount);
//                esmTNum += Integer.parseInt(TodayCount);
//            }
//            else {
//                Log.d(TAG, "In Else");
//                esmNum += Integer.parseInt(TodayResponse[i]);
//                esmTNum += Integer.parseInt(TotalResponse[i]);
//            }
//        }
        return String.valueOf(esmNum) + "," + String.valueOf(esmTNum);
    }
    public void StoreCropImage(String fileName){
        Bitmap bitmap = null;
//        if(imgFile == null){
//            Log.d(TAG, "imageFile is null");
//        }
//        if(bitmap == null){
//            Log.d(TAG, "bitmap is null");
//        }
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Crop_Uri);
//            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, fileName , "cropped");
            //second argument of FileOutputStream constructor indicates whether
            //to append or create new file if one exists
            FileOutputStream out = new FileOutputStream(fileName);
            if (out != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, out);
                out.flush();
                out.close();
                Log.d(TAG, fileName.toString() + " is saved");
            }
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(bitmap != null) {
                bitmap.recycle();
            }
        }
    }
    public void updateImgName(String dir, String partial_imgName, String ESM_ID){
//        File root_directory = new File(Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH);
        String FilePath = Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + dir;
        File dirfile = new File(FilePath);
        File[] files = dirfile.listFiles();
        if(files != null){
            for (int i = 0; i < files.length; i++) {
                String filename = files[i].getName();
                if(filename.contains(partial_imgName) && !filename.contains("crop")){
                    Log.d(TAG, "FileName:" + files[i].getName());
                    String[] imgName_split = files[i].getName().split("-");
                    String new_name = imgName_split[0] + "-" + imgName_split[1] + "-" + imgName_split[2] +
                            "-" + imgName_split[3] + "-ESM-" + imgName_split[4];
                    Log.d(TAG, "New filename: " + new_name);
                    File to = new File(FilePath, new_name);
                    boolean rename = files[i].renameTo(to);
                    db.NewsDataRecordDao().updateFilenameByFileName(files[i].getName(), new_name);
                    db.MyDataRecordDao().updateFilenameByFileName(files[i].getName(), new_name, ESM_ID);
                }
            }
        }
    }
    public void insertESMFinalAnswer(boolean isDestroy){
        CSVHelper.storeToCSV(CSV_Diary, "Insert ESM final answer");
        appDatabase db = appDatabase.getDatabase(getApplicationContext());
        Cursor transCursor = db.questionWithAnswersDao().getAllQuestionsWithChoices("-1");
        int rows = transCursor.getCount();

        if(rows!=0) {
            transCursor.moveToFirst();
            for (int i = 0; i < rows; i++) {
                Log.d(TAG, "Insert final answer");
                String detectedTime = transCursor.getString(2);
                String questionId = transCursor.getString(3);
                String optionpos = transCursor.getString(5);
                String answerChoice = transCursor.getString(4);
                String optionId = transCursor.getString(6);
                String answerChoiceState = transCursor.getString(7);
                Integer related = transCursor.getInt(8);

                String[] Esmcount = getESMcount().split(",");

                FinalAnswerDataRecord finalAnswerDataRecord = new FinalAnswerDataRecord();
                finalAnswerDataRecord.setGenerateTime(nowESM_time);//問卷產生時間
                finalAnswerDataRecord.setRespondTime(pref.getLong("respondTime",0)); //點進問卷的時間
                finalAnswerDataRecord.setSubmitTime(-1L);// onDestroy時間
                finalAnswerDataRecord.setisFinish(isFinish);
                finalAnswerDataRecord.setQuesType("ESM");
                finalAnswerDataRecord.setreplyCount(Esmcount[0]);
                finalAnswerDataRecord.settotalCount(Esmcount[1]);

                finalAnswerDataRecord.setAnswerChoicePos(optionpos);
                finalAnswerDataRecord.setAnswerChoiceState(answerChoiceState);
                finalAnswerDataRecord.setanswerId(String.valueOf(optionId));
                finalAnswerDataRecord.setdetectedTime(detectedTime);
                finalAnswerDataRecord.setQuestionId(questionId);
                finalAnswerDataRecord.setsyncStatus(0);
                finalAnswerDataRecord.setRelatedId(related);
                finalAnswerDataRecord.setAnswerChoice(answerChoice);
                finalAnswerDataRecord.setcreationIme(new Date().getTime());
                db.finalAnswerDao().insertAll(finalAnswerDataRecord);

                if(questionId.equals("3")){
                    if(answerChoiceState.matches("您於.*的手機畫面")) {
                        String[] Ans_split = answerChoiceState.split(" "); //您於2020-09-26 10:51:24的手機畫面
                        String date = Ans_split[0].split("於")[1]; // 2020-09-26
                        String time = Ans_split[1].split("的")[0].replace(":", "-"); //10-51-24
                        Log.d(TAG, "Question id = 3, Ans: " + date + "-" + time);
                        if(cueRecallImg != null){
                            String fileName = cueRecallImg.toString();
                            Log.d(TAG, "cue recall image name: " + fileName);
                            String[] fileName_split = fileName.split("/");
                            String[] fileName_temp = fileName_split[fileName_split.length - 1].split("-");
                            String fileName_Crop = fileName_temp[0] + "-" + fileName_temp[1] + "-" + fileName_temp[2] + "-" + fileName_temp[3] + "-ESM-crop-" + fileName_temp[4];
                            fileName = Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + date + "/" + fileName_Crop;
                            Log.d(TAG, "cue recall image name: " + fileName);
                            StoreCropImage(fileName);
                        }
                        if (!date.equals("") && !time.equals(""))
                            updateImgName(date, time, String.valueOf(related));
                    }
                }
                transCursor.moveToNext();
                try{
                    Thread.sleep(1);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        transCursor.close();
//        int[] quesid = new int[Q22Answer.size()];
//        for(int i = 0; i < Q22Answer.size(); i++){
//            quesid[i] = 27 + i * multitask_following;
//        }
//        for(int i = 0; i < Q22Answer.size(); i++) {
//            Cursor transCursor2 = db.questionWithAnswersDao().getQuestionsWithID(String.valueOf(quesid[i]));
//            rows = transCursor2.getCount();
//            if (rows != 0) {
//                transCursor2.moveToFirst();
//                String detectedTime_hour = transCursor2.getString(2);
//                String questionId_hour = transCursor2.getString(3);
//                String optionpos_hour = transCursor2.getString(5);
//                String answerChoice_hour = transCursor2.getString(4);
//                String optionId_hour = transCursor2.getString(6);
//                String answerChoiceState_hour = transCursor2.getString(7);
//                Integer related_hour = transCursor2.getInt(8);
//                transCursor2.moveToNext();
//
//                String detectedTime_min = transCursor2.getString(2);
//                String questionId_min = transCursor2.getString(3);
//                String optionpos_min = transCursor2.getString(5);
//                String answerChoice_min = transCursor2.getString(4);
//                String optionId_min = transCursor2.getString(6);
//                String answerChoiceState_min = transCursor2.getString(7);
//                Integer related_min = transCursor2.getInt(8);
//                transCursor2.moveToNext();
//
//                if (!(answerChoiceState_hour.equals("0") && answerChoiceState_min.equals("0"))) {
//                    FinalAnswerDataRecord finalAnswerDataRecord = new FinalAnswerDataRecord();
//                    finalAnswerDataRecord.setAnswerChoicePos(optionpos_hour);
//                    finalAnswerDataRecord.setAnswerChoiceState(answerChoiceState_hour);
//                    finalAnswerDataRecord.setanswerId(String.valueOf(optionId_hour));
//                    finalAnswerDataRecord.setdetectedTime(detectedTime_hour);
//                    finalAnswerDataRecord.setQuestionId(questionId_hour);
//                    finalAnswerDataRecord.setsyncStatus(0);
//                    finalAnswerDataRecord.setRelatedId(related_hour);
//                    finalAnswerDataRecord.setAnswerChoice(answerChoice_hour);
//                    finalAnswerDataRecord.setcreationIme(new Date().getTime());
//                    db.finalAnswerDao().insertAll(finalAnswerDataRecord);
//
//                    finalAnswerDataRecord = new FinalAnswerDataRecord();
//                    finalAnswerDataRecord.setAnswerChoicePos(optionpos_min);
//                    finalAnswerDataRecord.setAnswerChoiceState(answerChoiceState_min);
//                    finalAnswerDataRecord.setanswerId(String.valueOf(optionId_min));
//                    finalAnswerDataRecord.setdetectedTime(detectedTime_min);
//                    finalAnswerDataRecord.setQuestionId(questionId_min);
//                    finalAnswerDataRecord.setsyncStatus(0);
//                    finalAnswerDataRecord.setRelatedId(related_min);
//                    finalAnswerDataRecord.setAnswerChoice(answerChoice_min);
//                    finalAnswerDataRecord.setcreationIme(new Date().getTime());
//                    db.finalAnswerDao().insertAll(finalAnswerDataRecord);
//                }
//            }
//            transCursor.close();
//            transCursor2.close();
//        }
        // Log.d("BootCompleteReceiver","In MainActivity insertFinalAnswer");
    }
    public void insertFinalAnswer(){
        CSVHelper.storeToCSV(CSV_Diary, "Insert final answer");
        appDatabase db = appDatabase.getDatabase(getApplicationContext());
        Cursor transCursor = db.questionWithAnswersDao().getAllQuestionsWithChoices("-1");
        int rows = transCursor.getCount();
        if(rows!=0) {
            transCursor.moveToFirst();
            for (int i = 0; i < rows; i++) {
                Log.d(TAG, "Insert final answer");
                String detectedTime = transCursor.getString(2);
                String questionId = transCursor.getString(3);
                String optionpos = transCursor.getString(5);
                String answerChoice = transCursor.getString(4);
                String optionId = transCursor.getString(6);
                String answerChoiceState = transCursor.getString(7);
                Integer related = transCursor.getInt(8);

                UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();

                int d = 0;
                int DiaryClick = 0;
                if(userRecord != null) {
                    DiaryClick = Integer.valueOf(userRecord.getDiary_number());
                    d = Integer.valueOf(userRecord.getTotalDiary_number());
                }
//                int d = pref.getInt("Diary_Num", 0);//日誌通知數目
//                int DiaryClick = pref.getInt("Diary_click", 0);

                FinalAnswerDataRecord finalAnswerDataRecord = new FinalAnswerDataRecord();
                finalAnswerDataRecord.setGenerateTime(nowDiary_time);
                finalAnswerDataRecord.setRespondTime(pref.getLong("DiaryrespondTime",0));
                finalAnswerDataRecord.setSubmitTime(finishAnswerTime);// onDestroy時間
                finalAnswerDataRecord.setisFinish(isDFinish);
                finalAnswerDataRecord.setQuesType("Diary");
                finalAnswerDataRecord.setreplyCount(String.valueOf(DiaryClick));
                finalAnswerDataRecord.settotalCount(String.valueOf(d));
                finalAnswerDataRecord.setAnswerChoicePos(optionpos);
                finalAnswerDataRecord.setAnswerChoiceState(answerChoiceState);
                finalAnswerDataRecord.setanswerId(String.valueOf(optionId));
                finalAnswerDataRecord.setdetectedTime(detectedTime);
                finalAnswerDataRecord.setQuestionId(questionId);
                finalAnswerDataRecord.setsyncStatus(0);
                finalAnswerDataRecord.setRelatedId(related);
                finalAnswerDataRecord.setAnswerChoice(answerChoice);
                finalAnswerDataRecord.setcreationIme(new Date().getTime());
                db.finalAnswerDao().insertAll(finalAnswerDataRecord);

                transCursor.moveToNext();
                try{
                    Thread.sleep(1);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        transCursor.close();
//        int[] quesid = new int[Q22Answer.size()];
//        for(int i = 0; i < Q22Answer.size(); i++){
//            quesid[i] = 27 + i * multitask_following;
//        }
//        for(int i = 0; i < Q22Answer.size(); i++) {
//            Cursor transCursor2 = db.questionWithAnswersDao().getQuestionsWithID(String.valueOf(quesid[i]));
//            rows = transCursor2.getCount();
//            if (rows != 0) {
//                transCursor2.moveToFirst();
//                String detectedTime_hour = transCursor2.getString(2);
//                String questionId_hour = transCursor2.getString(3);
//                String optionpos_hour = transCursor2.getString(5);
//                String answerChoice_hour = transCursor2.getString(4);
//                String optionId_hour = transCursor2.getString(6);
//                String answerChoiceState_hour = transCursor2.getString(7);
//                Integer related_hour = transCursor2.getInt(8);
//                transCursor2.moveToNext();
//
//                String detectedTime_min = transCursor2.getString(2);
//                String questionId_min = transCursor2.getString(3);
//                String optionpos_min = transCursor2.getString(5);
//                String answerChoice_min = transCursor2.getString(4);
//                String optionId_min = transCursor2.getString(6);
//                String answerChoiceState_min = transCursor2.getString(7);
//                Integer related_min = transCursor2.getInt(8);
//                transCursor2.moveToNext();
//
//                if (!(answerChoiceState_hour.equals("0") && answerChoiceState_min.equals("0"))) {
//                    FinalAnswerDataRecord finalAnswerDataRecord = new FinalAnswerDataRecord();
//                    finalAnswerDataRecord.setAnswerChoicePos(optionpos_hour);
//                    finalAnswerDataRecord.setAnswerChoiceState(answerChoiceState_hour);
//                    finalAnswerDataRecord.setanswerId(String.valueOf(optionId_hour));
//                    finalAnswerDataRecord.setdetectedTime(detectedTime_hour);
//                    finalAnswerDataRecord.setQuestionId(questionId_hour);
//                    finalAnswerDataRecord.setsyncStatus(0);
//                    finalAnswerDataRecord.setRelatedId(related_hour);
//                    finalAnswerDataRecord.setAnswerChoice(answerChoice_hour);
//                    finalAnswerDataRecord.setcreationIme(new Date().getTime());
//                    db.finalAnswerDao().insertAll(finalAnswerDataRecord);
//
//                    finalAnswerDataRecord = new FinalAnswerDataRecord();
//                    finalAnswerDataRecord.setAnswerChoicePos(optionpos_min);
//                    finalAnswerDataRecord.setAnswerChoiceState(answerChoiceState_min);
//                    finalAnswerDataRecord.setanswerId(String.valueOf(optionId_min));
//                    finalAnswerDataRecord.setdetectedTime(detectedTime_min);
//                    finalAnswerDataRecord.setQuestionId(questionId_min);
//                    finalAnswerDataRecord.setsyncStatus(0);
//                    finalAnswerDataRecord.setRelatedId(related_min);
//                    finalAnswerDataRecord.setAnswerChoice(answerChoice_min);
//                    finalAnswerDataRecord.setcreationIme(new Date().getTime());
//                    db.finalAnswerDao().insertAll(finalAnswerDataRecord);
//                }
//            }
//            transCursor.close();
//            transCursor2.close();
//        }
        // Log.d("BootCompleteReceiver","In MainActivity insertFinalAnswer");
    }
    public void updateResponse(){
        appDatabase db = appDatabase.getDatabase(getApplicationContext());
//        responseDataRecord.setStartAnswerTime(getReadableTime(startAnswerTimeLong));
//        responseDataRecord.setFinishedTime(getReadableTime(finishAnswerTime));
//        responseDataRecord.setIfComplete(true);
        db.repsonseDataRecordDao().updateData(relatedIdForQ, getReadableTime(startAnswerTimeLong),getReadableTime(finishAnswerTime),true);
        String str = "";
        str += "startTime : "+getReadableTime(startAnswerTimeLong)+ " finishTime : "+getReadableTime(finishAnswerTime);
//        CSVHelper.storeToCSV("response.csv",str);
        // db.repsonseDataRecordDao().insertAll(responseDataRecord);
        //relatedId++;
        startAnswerTimeLong = Long.valueOf(0);
        finishAnswerTime = Long.valueOf(0);
        //  Log.d("BootCompleteReceiver","In MainActivity updateResponse");
    }
    // shared functions
    public static String getReadableTime(long time){

        SimpleDateFormat sdf_now = new SimpleDateFormat(Constants.DATE_FORMAT_for_storing);
        String currentTimeString = sdf_now.format(time);
        return currentTimeString;
    }

    public JSONObject Q5_URL(Long lastESM_time, Long nowESM_time) {
        JSONArray q_item = new JSONArray();
        JSONObject q = new JSONObject();
        List<String> URLtemp = new ArrayList<String>();
        Cursor transCursor = null;
        Cursor transCursor2 = null;
        try {
            transCursor = db.notificationDataRecordDao().getUnsyncedDataAll(0);
            int rows = transCursor.getCount();
            if (rows != 0) {
                transCursor.moveToFirst();
                for (int i = 0; i < rows; i++) {
                    JSONObject oneRow = new JSONObject();
                    Long timestamp = transCursor.getLong(1);
                    String title_col = transCursor.getString(2);
                    String n_text_col = transCursor.getString(3);
                    String app_col = transCursor.getString(6);
                    if (timestamp > lastESM_time && timestamp < nowESM_time && app_col.equals("com.facebook.orca")
                    ) {
                        answerid++;
                        String option_name = "於" + getReadableTime(timestamp) + "在Messenger中收到: \n" + n_text_col;
                        URLtemp.add(n_text_col);
                        Log.d(TAG, option_name);
                        oneRow.put("answer_id", String.valueOf(answerid));
                        oneRow.put("name", option_name);
                        q_item.put(oneRow);
                    }
                    transCursor.moveToNext();
                }
            }

            transCursor2 = db.SessionDataRecordDao().getUnsyncedData(0);
            rows = transCursor2.getCount();
            if (rows != 0) {
                transCursor2.moveToFirst();
                for (int i = 0; i < rows; i++) {
                    Long sid = transCursor2.getLong(0);
                    Long timestamp = transCursor2.getLong(1);
                    String data_type = transCursor2.getString(4);
                    String app_col = transCursor2.getString(7);
                    if (timestamp > lastESM_time && timestamp < nowESM_time && app_col.equals("Messenger")
                    ) {
                        List<NewsDataRecord> newsarr = db.SessionDataRecordDao().getNewsData(sid);
                        for(int j = 0; j < newsarr.size(); j++){
                            String content = newsarr.get(j).getcontent();//url
                            if(!URLtemp.contains(content)) {
                                JSONObject oneRow = new JSONObject();
                                answerid++;
                                String name = newsarr.get(j).getfileName();
                                String option_name = "於" + getReadableTime(timestamp) + "在Messenger中收到: \n" + content;
                                Log.d(TAG, option_name);
                                oneRow.put("answer_id", String.valueOf(answerid));
                                oneRow.put("name", option_name);
                                q_item.put(oneRow);
                            }
                        }
                    }
                    transCursor2.moveToNext();
                }
            }

            if(q_item.length() == 0){
                q.put("id", 5);
                q.put("question_type_id", 1);
                q.put("question_type_name", "Radio");
                q.put("question_name", "這段期間沒有擷取到新聞網址，請回上一頁重新選擇");
                q.put("question_item", q_item);
            }
            else{
                q.put("id", 5);
                q.put("question_type_id", 1);
                q.put("question_type_name", "Radio");
                q.put("question_name", "請選擇一個有印象的新聞網址");
                q.put("question_item", q_item);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
            if(transCursor2 != null){
                transCursor2.close();
            }
        }
        return q;
    }

    public JSONObject Q6_URL(Long lastESM_time, Long nowESM_time) {
        JSONArray q_item = new JSONArray();
        JSONObject q = new JSONObject();
        List<String> URLtemp = new ArrayList<String>();
        Cursor transCursor = null;
        Cursor transCursor2 = null;
        try {
            transCursor = db.notificationDataRecordDao().getUnsyncedDataAll(0);
            int rows = transCursor.getCount();
            if (rows != 0) {
                transCursor.moveToFirst();
                for (int i = 0; i < rows; i++) {
                    JSONObject oneRow = new JSONObject();
                    Long timestamp = transCursor.getLong(1);
                    String title_col = transCursor.getString(2);
                    String n_text_col = transCursor.getString(3);
                    String app_col = transCursor.getString(6);
                    if (timestamp > lastESM_time && timestamp < nowESM_time
                            && app_col.equals("jp.naver.line.android")
                    ) {
                        answerid++;
                        String option_name = "於" + getReadableTime(timestamp) + "在Line中收到: \n" + n_text_col;
                        Log.d(TAG, option_name);
                        oneRow.put("answer_id", String.valueOf(answerid));
                        oneRow.put("name", option_name);
                        q_item.put(oneRow);
                    }
                    transCursor.moveToNext();
                }
                q.put("id", 6);
                q.put("question_type_id", 1);
                q.put("question_type_name", "Radio");
                q.put("question_name", "請選擇一個有印象的新聞網址");
                q.put("question_item", q_item);
            }

            transCursor2 = db.SessionDataRecordDao().getUnsyncedData(0);
            rows = transCursor2.getCount();
            if (rows != 0) {
                transCursor2.moveToFirst();
                for (int i = 0; i < rows; i++) {
                    Long sid = transCursor2.getLong(0);
                    Long timestamp = transCursor2.getLong(1);
                    String data_type = transCursor2.getString(4);
                    String app_col = transCursor2.getString(7);
                    if (timestamp > lastESM_time && timestamp < nowESM_time && app_col.equals("LineChat")
                    ) {
                        List<NewsDataRecord> newsarr = db.SessionDataRecordDao().getNewsData(sid);
                        for(int j = 0; j < newsarr.size(); j++){
                            String content = newsarr.get(j).getcontent();//url
                            if(!URLtemp.contains(content)) {
                                JSONObject oneRow = new JSONObject();
                                answerid++;
                                String name = newsarr.get(j).getfileName();
                                String option_name = "於" + getReadableTime(timestamp) + "在Line中收到: \n" + content;
                                Log.d(TAG, option_name);
                                oneRow.put("answer_id", String.valueOf(answerid));
                                oneRow.put("name", option_name);
                                q_item.put(oneRow);
                            }
                        }
                    }
                    transCursor2.moveToNext();
                }
            }

            if(q_item.length() == 0){
                q.put("id", 6);
                q.put("question_type_id", 1);
                q.put("question_type_name", "Radio");
                q.put("question_name", "這段期間沒有擷取到新聞網址，請回上一頁重新選擇");
                q.put("question_item", q_item);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
            if(transCursor2 != null){
                transCursor2.close();
            }
        }
        return q;
    }

    public JSONObject Q7_Ptt(Long lastESM_time, Long nowESM_time) {
        JSONArray q_item = new JSONArray();
        JSONObject q = new JSONObject();
        Cursor transCursor = null;
        try {
            transCursor = db.SessionDataRecordDao().getUnsyncedData(0);
            int rows = transCursor.getCount();
            if (rows != 0) {
                transCursor.moveToFirst();
                for (int i = 0; i < rows; i++) {
                    Long sid = transCursor.getLong(0);
                    Long timestamp = transCursor.getLong(1);
                    String data_type = transCursor.getString(4);
                    String app_col = transCursor.getString(7);
                    if (timestamp > lastESM_time && timestamp < nowESM_time && app_col.equals("PTT")
                    ) {
                        List<NewsDataRecord> newsarr = db.SessionDataRecordDao().getNewsData(sid);
                        for(int j = 0; j < newsarr.size(); j++){
                            JSONObject oneRow = new JSONObject();
                            answerid++;
                            String content = newsarr.get(j).getcontent();
                            String option_name = "於" + getReadableTime(timestamp) + "在Ptt中收到: \n" + content;
                            Log.d(TAG, option_name);
                            oneRow.put("answer_id", String.valueOf(answerid));
                            oneRow.put("name", option_name);
                            q_item.put(oneRow);
                        }
                    }
                    transCursor.moveToNext();
                }
                q.put("id", 7);
                q.put("question_type_id", 1);
                q.put("question_type_name", "Radio");
                q.put("question_name", "請選擇一個有印象的新聞標題");
                q.put("question_item", q_item);
            }
            if(q_item.length() == 0){
                q.put("id", 7);
                q.put("question_type_id", 1);
                q.put("question_type_name", "Radio");
                q.put("question_name", "這段期間沒有擷取到新聞標題，請回上一頁重新選擇");
                q.put("question_item", q_item);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
        }
        return q;
    }

    public JSONObject Q8_NotiTitle(Long lastESM_time, Long nowESM_time) {
        JSONArray q_item = new JSONArray();
        JSONObject q = new JSONObject();
        Cursor transCursor = null;
        try {
            transCursor = db.notificationDataRecordDao().getUnsyncedDataAll(0);
            int rows = transCursor.getCount();
            if (rows != 0) {
                transCursor.moveToFirst();
                for (int i = 0; i < rows; i++) {
                    JSONObject oneRow = new JSONObject();
                    Long timestamp = transCursor.getLong(1);
                    String title_col = transCursor.getString(2);
                    String n_text_col = transCursor.getString(3);
                    String app_col = transCursor.getString(6);
                    if (timestamp > lastESM_time && timestamp < nowESM_time && !app_col.equals("com.example.accessibility_detect")
                            && !(app_col.equals("com.facebook.orca") || app_col.equals("jp.naver.line.android"))
                    ) {
                        answerid++;
                        String option_name = n_text_col + "\n" + getReadableTime(timestamp) + " " + title_col + " " + app_col;
                        Log.d(TAG, option_name);
                        oneRow.put("answer_id", String.valueOf(answerid));
                        oneRow.put("name", option_name);
                        q_item.put(oneRow);
                    }
                    transCursor.moveToNext();
                }
                q.put("id", 8);
                q.put("question_type_id", 1);
                q.put("question_type_name", "Radio");
                q.put("question_name", "請選擇一個有印象的新聞標題");
                q.put("question_item", q_item);
            }
            if(q_item.length() == 0){
                q.put("id", 8);
                q.put("question_type_id", 1);
                q.put("question_type_name", "Radio");
                q.put("question_name", "這段期間沒有擷取到新聞標題，請回上一頁重新選擇");
                q.put("question_item", q_item);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
        }
        return q;
    }

//    public List<String> Question_continue(){
//        List<String> question_list = new ArrayList<>(Arrays.asList(
////                "{'id':9," + "'question_type_id':3," + "'question_type_name':'CheckBox'," +
////                        "'question_name':'請問你針對這則新聞從事下列行為? (可複選)'," +
////                        "'question_item':[{'answer_id':'" + (++answerid) + "','name':'滑過去'}," +
////                                "{'answer_id':'" + (++answerid) + "','name':'點擊這則新聞'}," +
////                                "{'answer_id':'" + (++answerid) + "','name':'對這則新聞按讚、愛心'}," +
////                                "{'answer_id':'" + (++answerid) + "','name':'對這則新聞留言'}," +
////                                "{'answer_id':'" + (++answerid) + "','name':'分享這則新聞至社群媒體的即時動態 (如：Facebook和Instagram的即時動態)'}," +
////                                "{'answer_id':'" + (++answerid) + "','name':'私訊這則新聞給特定對象'}," +
////                                "{'answer_id':'" + (++answerid) + "','name':'分享這則新聞到社團或群組'}," +
////                                "{'answer_id':'" + (++answerid) + "','name':'搜尋這則新聞的相關資訊'}," +
////                                "{'answer_id':'" + (++answerid) + "','name':'查證這則新聞真實性'}," +
////                                "{'answer_id':'" + (++answerid) + "','name':'其他'}]}",
////                "{'id':10," + "'question_type_id':1," + "'question_type_name':'Radio'," +
////                        "'question_name':'請問這則新聞是誰分享的?'," +
////                        "'question_item':[{'answer_id':'" + (++answerid) + "','name':'新聞頻道'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'所追蹤的名人 (如：網紅)'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'伴侶 (包含夫妻、男女朋友、同居人)'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'家人或親戚'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'朋友'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'同事或同學'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'點頭之交'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'鄰居'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'陌生人'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'不知道'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'其他'}]}",
////                "{'id':11," + "'question_type_id':1," + "'question_type_name':'Radio'," +
////                        "'question_name':'請問這則新聞形式是屬於下列哪種?'," +
////                        "'question_item':[{'answer_id':'" + (++answerid) + "','name':'純文字'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'圖片'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'影片'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'有文字與圖片'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'有文字與影片'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'有文字、圖片與影片'}]}",
////                "{'id':12," + "'question_type_id':1," + "'question_type_name':'Radio'," +
////                        "'question_name':'請問你有沒有看完這則新聞的內容?'," +
////                        "'question_item':[{'answer_id':'" + (++answerid) + "','name':'有'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'沒有'}]}",
////                "{'id':13," + "'question_type_id':1," + "'question_type_name':'Radio'," +
////                        "'question_name':'請問這則新聞與哪個主題最相關?'," +
////                        "'question_item':[{'answer_id':'" + (++answerid) + "','name':'健康 (Health)'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'娛樂 (Entertainment)'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'你生活周遭的人、事、物 (People and events in your own community)'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'運動 (Sports)'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'科學與科技新知(Science and technology)'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'商業與財經 (Business and finance)'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'犯罪 (Crime)'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'政府與政治 (Government and politics)'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'不知道'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'其他'}]}",
////                "{'id':14," + "'question_type_id':3," + "'question_type_name':'CheckBox'," +
////                        "'question_name':'請問當時使用手機看新聞的原因有哪些? (可複選)'," +
////                        "'question_item':[{'answer_id':'" + (++answerid) + "','name':'為了獲得新聞資訊'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'因為手機提醒 (如：訊息或電話)'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'為了娛樂'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'為了放鬆或打發時間'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'為了學習或工作'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'為了社交'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'因為習慣'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'其他'}]}",
////                "{'id':15," + "'question_type_id':1," + "'question_type_name':'Radio'," +
////                        "'question_name':'請問你當時有多專注閱讀這則新聞?'," +
////                        "'question_item':[{'answer_id':'" + (++answerid) + "','name':'非常不專注'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'有點不專注'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'普通'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'有點專注'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'非常專注'}]}",
////                "{'id':16," + "'question_type_id':1," + "'question_type_name':'Radio'," +
////                        "'question_name':'請問你覺得了解這則新聞容不容易?'," +
////                        "'question_item':[{'answer_id':'" + (++answerid) + "','name':'非常不容易'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'有點不容易'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'普通'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'有點容易'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'非常容易'}]}",
////                "{'id':17," + "'question_type_id':1," + "'question_type_name':'Radio'," +
////                        "'question_name':'請問你覺得你有多了解該則新聞的重點?'," +
////                        "'question_item':[{'answer_id':'" + (++answerid) + "','name':'非常不了解'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'有點不了解'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'普通'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'有點了解'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'非常了解'}]}",
////                "{'id':18," + "'question_type_id':1," + "'question_type_name':'Radio'," +
////                        "'question_name':'請問你對這則新聞的感覺是正面或負面?'," +
////                        "'question_item':[{'answer_id':'" + (++answerid) + "','name':'非常負面'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'有點負面'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'普通'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'有點正面'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'非常正面'}]}",
////                "{'id':19," + "'question_type_id':1," + "'question_type_name':'Radio'," +
////                        "'question_name':'請問你相不相信這則新聞?'," +
////                        "'question_item':[{'answer_id':'" + (++answerid) + "','name':'非常不相信'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'有點不相信'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'普通'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'有點相信'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'非常相信'}]}",
////                "{'id':20," + "'question_type_id':1," + "'question_type_name':'Radio'," +
////                        "'question_name':'請問你同不同意:「你在看該則新聞時，有試圖找出新聞中的缺失 (例如：內容會誤導人、來源不明確)」?'," +
////                        "'question_item':[{'answer_id':'" + (++answerid) + "','name':'非常不同意'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'有點不同意'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'普通'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'有點同意'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'非常同意'}]}",
////                "{'id':21," + "'question_type_id':1," + "'question_type_name':'Radio'," +
////                        "'question_name':'請問當時你除了使用手機讀新聞外，同時還有做其他事情嗎?'," +
////                        "'question_item':[{'answer_id':'" + (++answerid) + "','name':'有'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'沒有'}]}",
////                "{'id':22," + "'question_type_id':3," + "'question_type_name':'CheckBox'," +
////                        "'question_name':'請問當時你除了使用手機讀新聞外，同時還有做下列哪些事情?'," +
////                        "'question_item':[{'answer_id':'" + (++answerid) + "','name':'在網路上從事家務或責任相關事務 (如買菜、掛號、處理公務)'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'在公共空間（如賣場或公務單位）從事家務或責任相關事務（如買菜、看病、處理公務）'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'在私人空間（如家裡）從事家務或責任相關事務（如洗衣、打掃）'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'搭大眾交通運輸工具'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'搭私人交通工具如汽車、機車'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'開汽車'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'騎機車或腳踏車'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'在公共空間（如公司）工作'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'在私人空間（如家裡）工作'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'在公共空間（如學校）學習'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'在私人空間（如家裡）學習'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'在公共空間（如餐廳）吃飯或喝東西'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'在私人空間（如家裡）吃飯或喝東西'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'在網路上從事娛樂、休閒活動'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'在公共空間（如戶外）從事娛樂、休閒活動，或運動'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'在私人空間（如家裡）從事娛樂、休閒活動，或運動'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'在網路上與家人、朋友、同學或同事聊天'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'在公共空間（如公司、學校）與家人、朋友、同學或同事聊天'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'在私人空間（如家裡）與家人、朋友、同學或同事面對面聊天'}," +
////                        "{'answer_id':'" + (++answerid) + "','name':'其他'}]}",
////                "{'id':23," + "'question_type_id':1," + "'question_type_name':'Radio'," +
////                "'question_name':'空頁面'," +
////                "'question_item':[]}"
//        ));
//        int question_id = 15;
//        for(int j = 0; j < 10; j++){
//            String Q12 = "{'id':" + question_id + "," + "'question_type_id':1," + "'question_type_name':'Radio'," +
//                    "'question_name':'針對第「」則你查證或搜尋的新聞，這則新聞的主要內容與哪個議題相關？'," +
//                    "'question_item':[{'answer_id':'" + (++answerid) + "','name':'健康 (Health)'}," +
//                    "{'answer_id':'" + (++answerid) + "','name':'娛樂 (Entertainment)'}," +
//                    "{'answer_id':'" + (++answerid) + "','name':'你生活周遭的人、事、物 (People and events in your own community)'}," +
//                    "{'answer_id':'" + (++answerid) + "','name':'運動 (Sports)'}," +
//                    "{'answer_id':'" + (++answerid) + "','name':'科學與科技新知(Science and technology)'}," +
//                    "{'answer_id':'" + (++answerid) + "','name':'商業與財經 (Business and finance)'}," +
//                    "{'answer_id':'" + (++answerid) + "','name':'犯罪 (Crime)'}," +
//                    "{'answer_id':'" + (++answerid) + "','name':'政府與政治 (Government and politics)'}," +
//                    "{'answer_id':'" + (++answerid) + "','name':'不知道'}," +
//                    "{'answer_id':'" + (++answerid) + "','name':'其他'}]}";
//            question_list.add(Q12);
//            question_id ++;
//
//            String Q13 = "{'id':" + question_id + "," + "'question_type_id':4," + "'question_type_name':'SeekBar'," +
//                    "'question_name':'針對第「」則你查證或搜尋的新聞，請問你看到新聞多久後查證？（若立即查證或搜尋資料，請填0分0秒）'," +
//                    "'question_item':[{'answer_id':'" + (++answerid) + "','name':'小時 (0 ~ 12)'}," +
//                    "{'answer_id':'" + (++answerid) + "','name':'分鐘 (0 ~ 59)'}]}";
//            question_list.add(Q13);
//            question_id++;
//
//            String Q14 = "{'id':" + question_id + "," + "'question_type_id':1," + "'question_type_name':'Radio'," +
//                    "'question_name':'針對第「」則你查證或搜尋的新聞，請問你查證或搜尋這些的原因是？'," +
//                    "'question_item':[{'answer_id':'" + (++answerid) + "','name':'新聞內容很可疑'}," +
//                    "{'answer_id':'" + (++answerid) + "','name':'新聞來源很可疑'}," +
//                    "{'answer_id':'" + (++answerid) + "','name':'希望可以獲得不同觀點'}]}";
//            question_list.add(Q14);
//            question_id++;
//
//            String Q15 = "{'id':" + question_id + "," + "'question_type_id':1," + "'question_type_name':'Radio'," +
//                    "'question_name':'針對第「」則你查證或搜尋的新聞，請問你怎麼查證或搜尋這些新聞？'," +
//                    "'question_item':[{'answer_id':'" + (++answerid) + "','name':'搜尋新聞內容'}," +
//                    "{'answer_id':'" + (++answerid) + "','name':'搜尋新聞內容的來源'}," +
//                    "{'answer_id':'" + (++answerid) + "','name':'搜尋分享新聞的人'}," +
//                    "{'answer_id':'" + (++answerid) + "','name':'參考新聞下方的留言'}]}";
//            question_list.add(Q15);
//            question_id++;
//        }
//        String Q16 = "{'id':" + question_id + "," + "'question_type_id':3," + "'question_type_name':'CheckBox'," +
//                "'question_name':'請問你沒有進一步查證或搜尋相關資訊的原因是？（可複選）'," +
//                "'question_item':[{'answer_id':'" + (++answerid) + "','name':'忘記了'}," +
//                "{'answer_id':'" + (++answerid) + "','name':'沒有心情'}," +
//                "{'answer_id':'" + (++answerid) + "','name':'沒有時間'}," +
//                "{'answer_id':'" + (++answerid) + "','name':'自己可以判斷'}," +
//                "{'answer_id':'" + (++answerid) + "','name':'手機上搜尋資訊太麻煩'}]}";
//        question_list.add(Q16);
//        question_id++;
//
//        String Q17 = "{'id':" + question_id + "," + "'question_type_id':5," + "'question_type_name':'Text'," +
//                "'question_name':'整體來說，請問你今天在臉書上總共對幾則新聞按讚？'," +
//                "'question_item':[{'answer_id':'" + (++answerid) + "','name':'臉書按讚數'}]}";
//        question_list.add(Q17);
//        question_id++;
//        String Q18 = "{'id':" + question_id + "," + "'question_type_id':5," + "'question_type_name':'Text'," +
//                "'question_name':'整體來說，請問你今天在臉書上總共分享幾則新聞？'," +
//                "'question_item':[{'answer_id':'" + (++answerid) + "','name':'臉書分享數'}]}";
//        question_list.add(Q18);
//        question_id++;
//        String Q19 = "{'id':" + question_id + "," + "'question_type_id':5," + "'question_type_name':'Text'," +
//                "'question_name':'整體來說，請問你今天在臉書上總共對幾則新聞留言？'," +
//                "'question_item':[{'answer_id':'" + (++answerid) + "','name':'臉書留言數'}]}";
//        question_list.add(Q19);
//        question_id++;
//
//        return question_list;
//    }

    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private final ArrayList<Fragment> fragments;
        public MyPagerAdapter(FragmentManager fragmentManager, int behavior, ArrayList<Fragment> fragments) {
            super(fragmentManager, behavior);
            this.fragments = fragments;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return this.fragments.size();
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        // Returns the page title for the top indicator
//        @Override
//        public CharSequence getPageTitle(int position) {
//            return "Page " + position;
//        }

    }

    public void onRestart() {
        super.onRestart();
        boolean f = getSharedPreferences("test",MODE_PRIVATE).getBoolean("NewDiary", false);
        if(f) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }
    public void onPause(){
        super.onPause();
        pref.edit().putBoolean("IsDiaryDestroy", true).apply();
        Log.d(TAG, "onPause");
    }
}