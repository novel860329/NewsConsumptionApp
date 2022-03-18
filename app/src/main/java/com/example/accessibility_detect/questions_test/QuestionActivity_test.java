package com.example.accessibility_detect.questions_test;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import com.example.accessibility_detect.questions_test.fragments_test.CheckBoxesFragment_test;
import com.example.accessibility_detect.questions_test.fragments_test.CueRecallFragment_test;
import com.example.accessibility_detect.questions_test.fragments_test.ImageCropFragment_test;
import com.example.accessibility_detect.questions_test.fragments_test.RadioBoxesFragment_test;
import com.example.accessibility_detect.questions_test.fragments_test.SeekBarsFragment_test;
import com.example.accessibility_detect.questions_test.questionmodels_test.AnswerOptions_test;
import com.example.accessibility_detect.questions_test.questionmodels_test.QuestionDataModel_test;
import com.example.accessibility_detect.questions_test.questionmodels_test.QuestionsItem_test;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.model.DataRecord.FinalAnswerDataRecord;
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
import static labelingStudy.nctu.minuku.Utilities.CSVHelper.CSV_ESM;
import static labelingStudy.nctu.minuku.config.Constants.PICTURE_DIRECTORY_PATH;
import static labelingStudy.nctu.minuku.config.Constants.multitask_following;
import static labelingStudy.nctu.minuku.config.SharedVariables.BackToQ6;
import static labelingStudy.nctu.minuku.config.SharedVariables.CanFillDiary;
import static labelingStudy.nctu.minuku.config.SharedVariables.CanFillEsm;
import static labelingStudy.nctu.minuku.config.SharedVariables.EnterQ1_first;
import static labelingStudy.nctu.minuku.config.SharedVariables.NotiIdActiveSurvey;
import static labelingStudy.nctu.minuku.config.SharedVariables.NotiIdRandomMCNotiSurvey;
import static labelingStudy.nctu.minuku.config.SharedVariables.NotiIdRandomReminder;
import static labelingStudy.nctu.minuku.config.SharedVariables.Q25Answer;
import static labelingStudy.nctu.minuku.config.SharedVariables.Q28Answer;
import static labelingStudy.nctu.minuku.config.SharedVariables.Q7Answer;
import static labelingStudy.nctu.minuku.config.SharedVariables.answerid;
import static labelingStudy.nctu.minuku.config.SharedVariables.extraForQ;
import static labelingStudy.nctu.minuku.config.SharedVariables.isDFinish;
import static labelingStudy.nctu.minuku.config.SharedVariables.isFinish;
import static labelingStudy.nctu.minuku.config.SharedVariables.nowESM_time;
import static labelingStudy.nctu.minuku.config.SharedVariables.response_time;
import static labelingStudy.nctu.minuku.config.SharedVariables.startAnswerTimeLong;
import static labelingStudy.nctu.minuku.config.SharedVariables.submitTime;
import static labelingStudy.nctu.minuku.config.SharedVariables.timeForQ;
import static labelingStudy.nctu.minuku.config.SharedVariables.todayMCount;

//import com.crashlytics.android.Crashlytics;
//import io.fabric.sdk.android.Fabric;

public class QuestionActivity_test extends AppCompatActivity
{
    public static String TAG = "QuestionActivity";
    final ArrayList<Fragment> fragmentArrayList = new ArrayList<>();
    public static List<QuestionsItem_test> questionsItems = new ArrayList<>();
    appDatabase db;
    //private TextView questionToolbarTitle;
    private TextView questionPositionTV;
    public static String totalQuestions = "1";
    private Gson gson;
    private SharedPreferences pref;
    private ViewPager questionsViewPager;
    public String appName = "";
    Long finishAnswerTime = Long.valueOf(-1);
    public String usertaskType = "";
    private Integer relatedIdForQ = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
//        Fabric.with(this, new Crashlytics());
        super.onCreate(savedInstanceState);
//        CSVHelper.storeToCSV("ESM_debug.csv", "Question Activity onCreate");
        pref = getSharedPreferences("test", MODE_PRIVATE);
        pref.edit().putBoolean("NewEsm", false).apply();
        pref.edit().putLong("Now_Esm_Time", System.currentTimeMillis()).apply();
        pref.edit().putLong("respondTime",System.currentTimeMillis()).apply();

        if(facebook || youtube || instagram || news || line || chrome || messenger || ptt || Utils.google || line_mes) {
            pref.edit().putBoolean("Question_interrupt", true).apply();
        }
        else{
            pref.edit().putBoolean("Question_interrupt", false).apply();
        }

//        CSVHelper.storeToCSV("ESM_debug.csv", "Question interrupt is " + pref.getBoolean("Question_interrupt", false));

        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar

        setContentView(R.layout.activity_question);

        toolBarInit();

        db = appDatabase.getDatabase(QuestionActivity_test.this);
        gson = new Gson();
        startAnswerTimeLong = new Date().getTime();

//        todayMCount = 0;
//        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
//        if(userRecord != null){
//            String esm_str = userRecord.getESM_number();
//            String[] EsmResponse = esm_str.split(",");
//            todayMCount = Integer.parseInt(EsmResponse[EsmResponse.length - 1]);
////            allMCount = Integer.parseInt(userRecord.getTotal_ESM_number());
//        }

//        todayMCount = pref.getInt(todayMCountString,todayMCount);
//        allMCount = pref.getInt(allMCountString,allMCount);
        if (getIntent().getExtras() != null)
        {
            Bundle bundle = getIntent().getExtras();
            parsingData(bundle,appName);
        }
        cueRecallImg = null;
        Crop_Uri = null;
        EnterQ1_first = true;
        BackToQ6 = false;
        Q25Answer.clear();
        Q28Answer.clear();
        Log.d(TAG,"appName : "+appName);
        Log.d(TAG,"usertaskType : "+usertaskType);
//        Log.d(TAG,"relatedId : "+relatedId);
    }

    private void toolBarInit()
    {
        Toolbar questionToolbar = findViewById(R.id.questionToolbar);
        questionToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        questionToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                QuestionActivity_test.this.onBackPressed();
            }
        });

        //questionToolbarTitle = questionToolbar.findViewById(R.id.questionToolbarTitle);
        questionPositionTV = questionToolbar.findViewById(R.id.questionPositionTV);

        //questionToolbarTitle.setText("Questions");
    }

    /*This method decides how many Question-Screen(s) will be created and
    what kind of (Multiple/Single choices) each Screen will be.*/
    private void parsingData(Bundle bundle, String appName)
    {
//        CSVHelper.storeToCSV("ESM_debug.csv", "Question Activity parsingData");
        //        relatedIdForQ++;
//        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
//        if(userRecord != null) {
////            relatedIdForQ = userRecord.getquestionnaireID();
////            Log.d(TAG, "relatedIdForQ: " + relatedIdForQ);
//        }
//        relatedIdForQ = pref.getInt("ESMID", 0);
//        relatedIdForQ = pref.getInt("QuestionnaireID", -1);
        // 12/16紀錄ESM的第三種狀態(沒回答消失)
        db.finalAnswerDao().deleteDataByID(-1);
//        CSVHelper.storeToCSV("ESM_debug.csv", "Delete data by questionnaire id " + relatedIdForQ);
        /////
//        db.userDataRecordDao().updateQuestionnaireID(_id, relatedIdForQ);
//        pref.edit().putInt("QuestionnaireID", relatedIdForQ).apply();
//        CSVHelper.storeToCSV(CSV_ESM, "Questionnaire id = " + relatedIdForQ);

        boolean isDestroy = pref.getBoolean("IsDestroy",true);
////        CSVHelper.storeToCSV(CSV_ESM, "isDestroy: " + isDestroy);
        boolean isDiaryDestroy = pref.getBoolean("IsDiaryDestroy",true);
        if(!isDestroy && isDiaryDestroy){
            Log.d(TAG, "Did not destroy (test)");
            CSVHelper.storeToCSV(CSV_ESM, "previous ESM is not destroy");
            insertFinalAnswer(isDestroy);
        }
        if(isDestroy && !isDiaryDestroy){
            Log.d(TAG, "Did not destroy(diary)(test)");
            CSVHelper.storeToCSV(CSV_ESM, "previous Diary is not destroy");
            UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
            if(userRecord != null) {
                db.userDataRecordDao().updateCanFillDiary(userRecord.get_id(), false);
            }
            CanFillDiary = false;
//            insertDiaryFinalAnswer(isDiaryDestroy);
        }
        pref.edit().putBoolean("IsDiaryDestroy", true).apply();
        pref.edit().putBoolean("IsDestroy", false).apply();
//
//        Log.d(TAG, "isDestroy: " + isDestroy);
        answerid = 221;
        QuestionDataModel_test questionDataModel = new QuestionDataModel_test();

        //respondTime = System.currentTimeMillis();

//        CSVHelper.storeToCSV(CSV_ESM, "responseTime: " + System.currentTimeMillis());

        submitTime = 0;
        isFinish = "1";
        nowESM_time = pref.getLong("ESM_send", 0);
        response_time = pref.getLong("respondTime",System.currentTimeMillis());

        pref.edit().putLong("ESM_SendDestroy", nowESM_time).apply();

        CSVHelper.storeToCSV(CSV_ESM, "get json question");
        String json_string = bundle.getString("json_questions");
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(json_string);
        }catch(Exception e){
            e.printStackTrace();
        }
//        Log.d(TAG, "now esm time: " + nowESM_time);
//        Long lastESM_time = getSharedPreferences("test",MODE_PRIVATE).getLong("Last_Esm_Time", 0);

        try {
            List<String> cont = Question_continue();
            JSONObject data = jsonObj.getJSONObject("data");
            JSONArray ques = data.getJSONArray("questions");

            for(int j = 0; j < cont.size(); j++){
                JSONObject jobject = null;
                try {
                    jobject = new JSONObject(cont.get(j));
                }catch (JSONException e){
                    e.printStackTrace();
                }
                ques.put(jobject);
            }
            data.remove("questions");
            data.put("questions",ques);
            jsonObj.remove("data");
            jsonObj.put("data", data);
        }catch(Exception e){
            e.printStackTrace();
        }
        json_string = jsonObj.toString();
        Log.d(TAG, json_string);

//        CSVHelper.storeToCSV(CSV_ESM, "get questionDataModel");

        questionDataModel = gson.fromJson(json_string, QuestionDataModel_test.class);
        relatedIdForQ = -1;
        String notiId = bundle.getString("notiId","");
//        Log.d(TAG, "testing: " + relatedIdForQ + " " + notiId);
        questionsItems = questionDataModel.getData().getQuestions();

        totalQuestions = "35";
//        CSVHelper.storeToCSV(CSV_ESM, "set question to db");

//        totalQuestions = String.valueOf(questionsItems.size());
        String questionPosition = "1/" + totalQuestions;
        setTextWithSpan(questionPosition);

        preparingQuestionInsertionInDb(questionsItems);
//        Log.d(TAG,"parsingData :"+relatedIdForQ);
        preparingInsertionInDb(questionsItems,relatedIdForQ);

        for (int i = 0; i < questionsItems.size(); i++)
        {
            QuestionsItem_test question = questionsItems.get(i);

            if (question.getQuestionTypeName().equals("CheckBox"))
            {
//                CSVHelper.storeToCSV(CSV_ESM, "set Checkbox question");
                CheckBoxesFragment_test checkBoxesFragment = new CheckBoxesFragment_test();
                Bundle checkBoxBundle = new Bundle();
                checkBoxBundle.putParcelable("question", question);
                checkBoxBundle.putInt("page_position", i);
                checkBoxBundle.putString("appNameForF",appName);
                checkBoxBundle.putInt("relatedIdF",relatedIdForQ);
                checkBoxBundle.putString("enterTimeForF",timeForQ);
                checkBoxBundle.putString("notiId", "-1");
                checkBoxesFragment.setArguments(checkBoxBundle);
                fragmentArrayList.add(checkBoxesFragment);
            }

            if (question.getQuestionTypeName().equals("Radio"))
            {
//                CSVHelper.storeToCSV(CSV_ESM, "set Radiobox question");
                RadioBoxesFragment_test radioBoxesFragment = new RadioBoxesFragment_test();
                Bundle radioButtonBundle = new Bundle();
                radioButtonBundle.putParcelable("question", question);
                radioButtonBundle.putInt("page_position", i);
                radioButtonBundle.putString("appNameForF",appName);
                radioButtonBundle.putInt("relatedIdF",relatedIdForQ);
                radioButtonBundle.putString("enterTimeForF",timeForQ);
                radioButtonBundle.putString("notiId","-1");
                radioBoxesFragment.setArguments(radioButtonBundle);
                fragmentArrayList.add(radioBoxesFragment);
            }
            if (question.getQuestionTypeName().equals("SeekBar"))
            {
//                CSVHelper.storeToCSV(CSV_ESM, "set Seekbar question");

                SeekBarsFragment_test seekBarsFragment = new SeekBarsFragment_test();
                Bundle seekBarBundle = new Bundle();
                seekBarBundle.putParcelable("question", question);
                seekBarBundle.putInt("page_position", i);
                seekBarBundle.putString("appNameForF",appName);
                seekBarBundle.putInt("relatedIdF",relatedIdForQ);
                seekBarBundle.putString("enterTimeForF",timeForQ);
                seekBarBundle.putString("notiId","-1");
                seekBarsFragment.setArguments(seekBarBundle);
                fragmentArrayList.add(seekBarsFragment);
            }
            if (question.getQuestionTypeName().equals("CueRecall"))
            {
//                CSVHelper.storeToCSV(CSV_ESM, "set CueRecall question");

                CueRecallFragment_test cueFragment = new CueRecallFragment_test();
                Bundle cueBundle = new Bundle();
                cueBundle.putParcelable("question", question);
                cueBundle.putInt("page_position", i);
                cueBundle.putString("appNameForF",appName);
                cueBundle.putInt("relatedIdF",relatedIdForQ);
                cueBundle.putString("enterTimeForF",timeForQ);
                cueBundle.putString("notiId","-1");
                cueFragment.setArguments(cueBundle);
                fragmentArrayList.add(cueFragment);
            }
            if (question.getQuestionTypeName().equals("ImageCrop"))
            {
//                CSVHelper.storeToCSV(CSV_ESM, "set ImageCrop question");

                ImageCropFragment_test cueFragment = new ImageCropFragment_test();
                Bundle cueBundle = new Bundle();
                cueBundle.putParcelable("question", question);
                cueBundle.putInt("page_position", i);
                cueBundle.putString("appNameForF",appName);
                cueBundle.putInt("relatedIdF",relatedIdForQ);
                cueBundle.putString("enterTimeForF",timeForQ);
                cueBundle.putString("notiId","-1");
                cueFragment.setArguments(cueBundle);
                fragmentArrayList.add(cueFragment);
            }
//            if (question.getQuestionTypeName().equals("Text"))
//            {
//                TextFragment textFragment = new TextFragment();
//                Bundle textBundle = new Bundle();
//                textBundle.putParcelable("question", question);
//                textBundle.putInt("page_position", i);
//                textBundle.putString("appNameForF",appName);
//                textBundle.putInt("relatedIdF",relatedIdForQ);
//                textBundle.putString("enterTimeForF",timeForQ);
//                textBundle.putString("notiId",notiId);
//                textFragment.setArguments(textBundle);
//                fragmentArrayList.add(textFragment);
//            }
//            if (question.getQuestionTypeName().equals("Choose"))
//            {
//                ChoosePictureFragment textFragment = new ChoosePictureFragment();
//                Bundle textBundle = new Bundle();
//                textBundle.putParcelable("question", question);
//                textBundle.putInt("page_position", i);
//                textBundle.putString("appNameForF",appName);
//                textBundle.putInt("relatedIdF",relatedIdForQ);
//                textBundle.putString("enterTimeForF",timeForQ);
//                textBundle.putString("notiId",notiId);
//                textFragment.setArguments(textBundle);
//                fragmentArrayList.add(textFragment);
//            }
//            if (question.getQuestionTypeName().equals("Thank"))
//            {
//                ThankYouFragment textFragment = new ThankYouFragment();
//                fragmentArrayList.add(textFragment);
//            }
            //i = skipConditions(i);
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

        Log.d("Checkoffset","offset : "+ offset);

        totalQuestions = "35";
        if(Q25Answer.size() != 0) {
            int total = Integer.parseInt(totalQuestions) + multitask_following * (Q25Answer.size() - 1);
            totalQuestions = String.valueOf(total);
        }
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
        return Integer.parseInt(totalQuestions);
    }

    private void preparingQuestionInsertionInDb(List<QuestionsItem_test> questionsItems)
    {
        List<QuestionDataRecord> questionEntities = new ArrayList<>();

        for (int i = 0; i < questionsItems.size(); i++)
        {
            QuestionDataRecord questionDataRecord = new QuestionDataRecord();
            questionDataRecord.setQuestionId(questionsItems.get(i).getId());
            questionDataRecord.setQuestion(questionsItems.get(i).getQuestionName());
            questionDataRecord.setStoreToFinal(false);

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

    private void preparingInsertionInDb(List<QuestionsItem_test> questionsItems, int relatedId)
    {
        ArrayList<QuestionWithAnswersDataRecord> questionWithChoicesEntities = new ArrayList<>();

        for (int i = 0; i < questionsItems.size(); i++)
        {
            List<AnswerOptions_test> answerOptions = questionsItems.get(i).getAnswerOptions();

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
        Log.d(TAG, "Back");
        if (questionsViewPager.getCurrentItem() == 0)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name);
            builder.setMessage("回上頁將會清除目前填答結果，且將無法填答。確定返回上一頁？");
            builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    CanFillEsm = false;
                    QuestionActivity_test.this.finish();
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
            if((item + 1) == 2){//3回1
                questionsViewPager.setCurrentItem(0);
                currentQuestionPosition = String.valueOf(1);
            }
            else if((item + 1) == 4) {//5回3
                questionsViewPager.setCurrentItem(2);
                currentQuestionPosition = String.valueOf(3);
            }
            else if((item + 1) == 5){//6
                BackToQ6 = false;
                if(cueRecallImg == null){
                    questionsViewPager.setCurrentItem(2);
                    currentQuestionPosition = String.valueOf(3);
                }
                else{
                    questionsViewPager.setCurrentItem(item);
                    currentQuestionPosition = String.valueOf(item + 1);
                }
            }
            else if((item + 1) == 9) //第10題回8
            {
                questionsViewPager.setCurrentItem(7);
                currentQuestionPosition = String.valueOf(8);
            }
            else if((item + 1) == 10) //第11題回7
            {
                if(Q7Answer == 1){
                    questionsViewPager.setCurrentItem(item);
                    currentQuestionPosition = String.valueOf(item + 1);
                }
                else{
                    questionsViewPager.setCurrentItem(6);
                    currentQuestionPosition = String.valueOf(7);
                }
            }
            else if((item + 1) == 26) //第27題 回去第25題
            {
                questionsViewPager.setCurrentItem(24);
                currentQuestionPosition = String.valueOf(25);
            }
            else if((item + 1) >= 29 && ((item + 1) - 29) % multitask_following == 0) //第30題 回去第28題
            {
                questionsViewPager.setCurrentItem((item + 1) - 2);
                currentQuestionPosition = String.valueOf((item + 1) - 1);
            }
            else if((item + 1) >= 30 && ((item + 1) - 30) % multitask_following == 0) //第31題 回去第30 or 28題
            {
                if(Q28Answer.get(((item + 1) - 30) % multitask_following) == 0){
                    questionsViewPager.setCurrentItem(item);
                    currentQuestionPosition = String.valueOf((item + 1));
                }
                else {
                    questionsViewPager.setCurrentItem((item + 1) - 3);
                    currentQuestionPosition = String.valueOf((item + 1) - 2);
                }
            }
            else{
                if((item + 1) == 6){ //7
                    BackToQ6 = true;
                }
                questionsViewPager.setCurrentItem(item);
                currentQuestionPosition = String.valueOf(item + 1);
            }
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
        String first,second;
        first = db.questionWithAnswersDao().isChecked(questionId,"0");
        second = db.questionWithAnswersDao().isChecked(questionId,"1");

        if(first!=null) {
            Log.d("qskip"," radioBox first :  "+first);
            if (first.equals("1")) {
                return 0;   // 是
            }
        }
        if(second!=null) {
            Log.d("qskip"," radioBox second :  "+second);
            if (second.equals("1")) {
                return 1;  //否
            }
        }
        return 0;
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
//        CSVHelper.storeToCSV(CSV_ESM, "onDestroy");
        super.onDestroy();
        Log.d(TAG, "onDestroy(test)");
        pref.edit().putBoolean("IsDestroy", true).apply();
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
        Log.d(TAG, "isFinish: " + isFinish);

        if(isFinish.equals("2")) {
            CanFillEsm = false;
            finishAnswerTime = new Date().getTime();
            CSVHelper.storeToCSV(CSV_ESM, "ESM is finish");
            Log.d(TAG, "This is finish");
            Toast.makeText(this, "感謝您的填答 !", Toast.LENGTH_LONG).show();
            todayMCount = 0;
//            UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
//            if(userRecord != null){
//                String esm_str = userRecord.getESM_number();
//                String[] EsmResponse = esm_str.split(",");
//                todayMCount = Integer.parseInt(EsmResponse[EsmResponse.length - 1]);
//    //            allMCount = Integer.parseInt(userRecord.getTotal_ESM_number());
//            }
//            todayMCount += 1;
//            allMCount += 1;
//            pref.edit().putLong("Last_Esm_Time", System.currentTimeMillis()).apply();

//            UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
//            if(userRecord != null){
//                String update_esm = "";
//                String esm_str = userRecord.getESM_number();
//                String[] EsmResponse = esm_str.split(",");
//                EsmResponse[EsmResponse.length - 1] = todayMCount.toString();
//                for(int i = 0; i < EsmResponse.length; i++){
//                    if(!EsmResponse[i].equals("")){
//                        update_esm = update_esm + EsmResponse[i] + ",";
//                    }
//                }
//                Log.d(TAG, "ESM number string: " + update_esm);
//                long _id = userRecord.get_id();
//                db.userDataRecordDao().updateESM(_id, update_esm);
//                db.userDataRecordDao().updateLastESMTime_for_Q1(_id, System.currentTimeMillis());
//            }
//            updateResponse();
//            pref.edit().putInt(todayMCountString,todayMCount).apply();//今天回答幾封
//            pref.edit().putInt(allMCountString,allMCount).apply();//全部回答幾封
        }
        else{
            CSVHelper.storeToCSV(CSV_ESM, "ESM is not finish");
            Log.d(TAG, "This is not finish");
        }
//        insertFinalAnswer(true);
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
    public void insertFinalAnswer(boolean isDestroy){
        Log.d(TAG, "Test insert final answer");
        CSVHelper.storeToCSV(CSV_ESM, "Insert final answer");
        appDatabase db = appDatabase.getDatabase(getApplicationContext());
        Cursor transCursor = db.questionWithAnswersDao().getAllQuestionsWithChoices("-1");
        int rows = transCursor.getCount();

        if(rows!=0) {
            transCursor.moveToFirst();
            for (int i = 0; i < rows; i++) {
//                Log.d(TAG, "Insert final answer");
                String detectedTime = transCursor.getString(2);
                String questionId = transCursor.getString(3);
                String optionpos = transCursor.getString(5);
                String answerChoice = transCursor.getString(4);
                String optionId = transCursor.getString(6);
                String answerChoiceState = transCursor.getString(7);
                Integer related = transCursor.getInt(8);

                String[] Esmcount = getESMcount().split(",");


                FinalAnswerDataRecord finalAnswerDataRecord = new FinalAnswerDataRecord();
                if (isDestroy) {
                    finalAnswerDataRecord.setGenerateTime(nowESM_time);//問卷產生時間
                } else {
                    CSVHelper.storeToCSV(CSV_ESM, "previous ESM generate time: " + String.valueOf(pref.getLong("ESM_SendDestroy", -1)));
                    finalAnswerDataRecord.setGenerateTime(pref.getLong("ESM_SendDestroy", -1));
                }
                finalAnswerDataRecord.setRespondTime(response_time); //點進問卷的時間
                finalAnswerDataRecord.setSubmitTime(finishAnswerTime);// onDestroy時間
                finalAnswerDataRecord.setisFinish(isFinish);
                finalAnswerDataRecord.setQuesType("ESM");
                finalAnswerDataRecord.setreplyCount(Esmcount[0]);

                if (isDestroy) {
                    finalAnswerDataRecord.settotalCount(Esmcount[1]);
                    CSVHelper.storeToCSV(CSV_ESM, "Total ESM count: " + Esmcount[1]);
                } else {
                    finalAnswerDataRecord.settotalCount(String.valueOf(Integer.parseInt(Esmcount[1]) - 1));
                    CSVHelper.storeToCSV(CSV_ESM, "Total ESM count: " + String.valueOf(Integer.parseInt(Esmcount[1]) - 1));

                }
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

                String fileName = "";
                String updatefileName = "";
//                if(questionId.equals("3")){
//                    if(answerChoiceState.matches("您於.*的手機畫面")) {
//                        String[] Ans_split = answerChoiceState.split(" "); //您於2020-09-26 10:51:24的手機畫面
//                        String date = Ans_split[0].split("於")[1]; // 2020-09-26
//                        String time = Ans_split[1].split("的")[0].replace(":", "-"); //10-51-24
//                        Log.d(TAG, "Question id = 3, Ans: " + date + "-" + time);
//                        if(cueRecallImg != null){
//                            fileName = cueRecallImg.toString();
//                            Log.d(TAG, "cue recall image name: " + fileName);
//                            String[] fileName_split = fileName.split("/");
//                            String[] fileName_temp = fileName_split[fileName_split.length - 1].split("-");
//                            String fileName_Crop = "";
//                            if(fileName_split[fileName_split.length - 1].contains("Upload")){
//                                fileName_Crop = fileName_temp[0] + "-" + fileName_temp[1] + "-" + fileName_temp[2] + "-" + fileName_temp[3] + "-" + fileName_temp[4] + "-ESM-crop-" + fileName_temp[5];
//                            }
//                            else{
//                                fileName_Crop = fileName_temp[0] + "-" + fileName_temp[1] + "-" + fileName_temp[2] + "-" + fileName_temp[3] + "-ESM-crop-" + fileName_temp[4];
//                            }
//                            fileName = Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + date + "/" + fileName_Crop;
//                            Log.d(TAG, "cue recall image name: " + fileName);
////                            StoreCropImage(fileName);
//                        }
//                        if (!date.equals("") && !time.equals("")) {
//                            updatefileName = updateImgName(date, time, String.valueOf(related));
//                        }
//                        String previousEsm = pref.getString("ESM_Image", "");
//                        String TwoEsmImagePath = "";
//                        if(previousEsm.equals("")){
//                            TwoEsmImagePath = updatefileName + "," + fileName;
//                        }
//                        else{
//                            TwoEsmImagePath = previousEsm + "," + updatefileName + "," + fileName;
//                        }
//                        Log.d("Connectivity", "Two path: " + TwoEsmImagePath);
//                        pref.edit().putString("ESM_Image", TwoEsmImagePath).apply();
//                    }

                transCursor.moveToNext();
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        CSVHelper.storeToCSV(CSV_ESM, "Finish insert final answer");

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
    public void insertDiaryFinalAnswer(boolean isDiaryDestroy){
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
                Long nowDiary_time = getSharedPreferences("test",MODE_PRIVATE).getLong("Now_Diary_Time", 0);
                FinalAnswerDataRecord finalAnswerDataRecord = new FinalAnswerDataRecord();
                finalAnswerDataRecord.setGenerateTime(nowDiary_time);
                finalAnswerDataRecord.setRespondTime(pref.getLong("DiaryrespondTime",0));
                if(isDiaryDestroy) {
                    finalAnswerDataRecord.setSubmitTime(finishAnswerTime);// onDestroy時間
                }
                else{
                    finalAnswerDataRecord.setSubmitTime(-1L);// onDestroy時間
                }
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
        db.repsonseDataRecordDao().updateData(relatedIdForQ,getReadableTime(startAnswerTimeLong),getReadableTime(finishAnswerTime),true);
        String str = "";
        str += "startTime : "+getReadableTime(startAnswerTimeLong)+ " finishTime : "+getReadableTime(finishAnswerTime);
//        CSVHelper.storeToCSV("response.csv",str);
        // db.repsonseDataRecordDao().insertAll(responseDataRecord);
//        relatedId++;
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

    public String updateImgName(String dir, String partial_imgName, String ESM_ID){
//        File root_directory = new File(Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH);
        String FilePath = Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + dir;
        String return_string = "";
        String new_name = "";
        File dirfile = new File(FilePath);
        File[] files = dirfile.listFiles();
        if(files != null){
            for (int i = 0; i < files.length; i++) {
                String filename = files[i].getName();
                if(filename.contains(partial_imgName) && !filename.contains("crop")){
                    Log.d(TAG, "FileName:" + files[i].getName());//09-04-35-46-Facebook.jpg
                    String[] imgName_split = files[i].getName().split("-");
                    if(files[i].getName().contains("Upload")){
                        new_name = imgName_split[0] + "-" + imgName_split[1] + "-" + imgName_split[2] +
                                "-" + imgName_split[3] + "-" + imgName_split[4] +  "-ESM-" + imgName_split[5];
                    }
                    else{
                        new_name = imgName_split[0] + "-" + imgName_split[1] + "-" + imgName_split[2] +
                                "-" + imgName_split[3] + "-ESM-" + imgName_split[4];
                    }

                    File to = new File(FilePath, new_name);
                    boolean rename = files[i].renameTo(to);
                    Log.d(TAG, "New filename: " + new_name + " ESM id = " + ESM_ID);
                    db.MyDataRecordDao().updateFilenameByFileName(dir + "/" + files[i].getName(), dir + "/" + new_name, ESM_ID);
                    updateImgInDB(dir + "/" + files[i].getName(), dir + "/" + new_name);
                }
            }
        }
        if(new_name.equals("")){
            return_string = "";
        }
        else{
            return_string = FilePath + "/" + new_name;
        }
        return return_string;
    }
    public void updateImgInDB(String fileName, String newName){
        db.NewsDataRecordDao().updateFilenameByFileName(fileName, newName);
        db.activityRecognitionDataRecordDao().updateFilenameByFileName(fileName, newName);
        db.AppTimesDataRecordDao().updateFilenameByFileName(fileName, newName);
        db.appUsageDataRecordDao().updateFilenameByFileName(fileName, newName);
        db.batteryDataRecordDao().updateFilenameByFileName(fileName, newName);
        db.connectivityDataRecordDao().updateFilenameByFileName(fileName, newName);
        db.notificationDataRecordDao().updateFilenameByFileName(fileName, newName);
        db.ringerDataRecordDao().updateFilenameByFileName(fileName, newName);
        db.sensorDataRecordDao().updateFilenameByFileName(fileName, newName);
        db.telephonyDataRecordDao().updateFilenameByFileName(fileName, newName);
        db.transportationModeDataRecordDao().updateFilenameByFileName(fileName, newName);
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
        CSVHelper.storeToCSV(CSV_ESM, "TodayResponseSet: " + TodayResponseSet);
        String[] TodayResponse = TodayResponseSet.split(",");

//        String TotalResponseSet = pref.getString("TotalResponse", "");
        Log.d(TAG, TotalResponseSet);
        CSVHelper.storeToCSV(CSV_ESM, "TotalResponseSet: " + TotalResponseSet);
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

    public List<String> Question_continue(){
        List<String> question_list = new ArrayList<>(Arrays.asList());
        int question_id = 36;
        for(int j = 0; j < 7; j++){
            String Q36 = "{'id':" + question_id + "," + "'question_type_id':3," + "'question_type_name':'CheckBox'," +
                    "'question_name':'請問你在「」時，同時還使用其他媒體嗎？（可複選）'," +
                    "'question_item':[{'answer_id':'" + (++answerid) + "','name':'平板'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'筆記型或桌上型電腦'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'電視'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'電視遊樂器'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'其他'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'沒有使用其他媒體'}]}";
            question_list.add(Q36);
            question_id ++;

            String Q37 = "{'id':" + question_id + "," + "'question_type_id':1," + "'question_type_name':'Radio'," +
                    "'question_name':'請問在當時，「」與「用手機看新聞」哪個是主要活動? (需要優先完成、比較重要的活動)'," +
                    "'question_item':[{'answer_id':'" + (++answerid) + "','name':' '}," +
                    "{'answer_id':'" + (++answerid) + "','name':'用手機看新聞'}]}";
            question_list.add(Q37);
            question_id++;

            String Q38 = "{'id':" + question_id + "," + "'question_type_id':1," + "'question_type_name':'Radio'," +
                    "'question_name':'空白頁'," +
                    "'question_item':[]}";
            question_list.add(Q38);
            question_id++;

            String Q39 = "{'id':" + question_id + "," + "'question_type_id':1," + "'question_type_name':'Radio'," +
                    "'question_name':'請問在當時，「用手機看新聞」是為了「」嗎?'," +
                    "'question_item':[{'answer_id':'" + (++answerid) + "','name':'是'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'否'}]}";
            question_list.add(Q39);
            question_id++;

            String Q40 = "{'id':" + question_id + "," + "'question_type_id':4," + "'question_type_name':'SeekBar'," +
                    "'question_name':'請問你「」花了多少時間?'," +
                    "'question_item':[{'answer_id':'" + (++answerid) + "','name':'小時 (0 ~ 12)'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'分鐘 (0 ~ 59)'}]}";
            question_list.add(Q40);
            question_id++;

            String Q41 = "{'id':" + question_id + "," + "'question_type_id':1," + "'question_type_name':'Radio'," +
                    "'question_name':'對你來說，請問「」時，你感受到<b><u>無聊</u></b>的程度為何？'," +
                    "'question_item':[{'answer_id':'" + (++answerid) + "','name':'完全不無聊'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'不無聊'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'不太無聊'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'沒有不無聊、也沒有無聊'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'有點無聊'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'無聊'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'很無聊'}]}";
            question_list.add(Q41);
            question_id++;

            String Q42 = "{'id':" + question_id + "," + "'question_type_id':1," + "'question_type_name':'Radio'," +
                    "'question_name':'對你來說，請問「」時，你感受到<b><u>焦慮</u></b>的程度為何？'," +
                    "'question_item':[{'answer_id':'" + (++answerid) + "','name':'完全不焦慮'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'不焦慮'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'不太焦慮'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'沒有不焦慮、也沒有焦慮'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'有點焦慮'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'焦慮'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'很焦慮'}]}";
            question_list.add(Q42);
            question_id++;

            String Q43 = "{'id':" + question_id + "," + "'question_type_id':1," + "'question_type_name':'Radio'," +
                    "'question_name':'對你來說，請問「」時，你感受到<b><u>開心</u></b>的程度為何？'," +
                    "'question_item':[{'answer_id':'" + (++answerid) + "','name':'完全不開心'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'不開心'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'不太開心'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'沒有不開心、也沒有開心'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'有點開心'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'開心'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'很開心'}]}";
            question_list.add(Q43);
            question_id++;

            String Q44 = "{'id':" + question_id + "," + "'question_type_id':1," + "'question_type_name':'Radio'," +
                    "'question_name':'對你來說，請問「」時，你感受到<b><u>滿意</u></b>的程度為何？'," +
                    "'question_item':[{'answer_id':'" + (++answerid) + "','name':'完全不滿意'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'不滿意'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'不太滿意'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'沒有不滿意、也沒有滿意'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'有點滿意'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'滿意'}," +
                    "{'answer_id':'" + (++answerid) + "','name':'很滿意'}]}";
            question_list.add(Q44);
            question_id++;
        }
//        question_list.add("{'id':" + question_id + "," + "'question_type_id':5," + "'question_type_name':'Thank'," +
//                "'question_name':' '," +
//                "'question_item':[]}");
        return question_list;
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
//            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Crop_Uri);
//            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, fileName , "cropped");
            //second argument of FileOutputStream constructor indicates whether
            //to append or create new file if one exists
            FileOutputStream out = new FileOutputStream(fileName);
//            if (out != null) {
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//                out.flush();
//                out.close();
//                Log.d(TAG, fileName.toString() + " is saved");
//            }
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            if(bitmap != null) {
                bitmap.recycle();
            }
        }
    }

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
        Log.d(TAG, "onRestart(test)");
        boolean f = getSharedPreferences("test",MODE_PRIVATE).getBoolean("NewEsm", false);
        if(f) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    public void onResume(){
        super.onResume();
        Log.d(TAG, "onResume(test)");
    }

    public void onPause(){
        super.onPause();
        pref.edit().putBoolean("IsDestroy", false).apply(); //3/20
        Log.d(TAG, "onPause(test)");
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.d(TAG, "onStop(test)");
    }
}