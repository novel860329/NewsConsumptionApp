package com.example.accessibility_detect.questions.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.example.accessibility_detect.MainActivity;
import com.example.accessibility_detect.R;
import com.example.accessibility_detect.questions.FullScreenImage;
import com.example.accessibility_detect.questions.QuestionActivity;
import com.example.accessibility_detect.questions.adapters.MyWebView;
import com.example.accessibility_detect.questions.questionmodels.QuestionsItem;
import com.google.gson.Gson;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.model.DataRecord.NewsDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;

import static android.content.Context.MODE_PRIVATE;
import static com.example.accessibility_detect.Utils.Crop_Uri;
import static com.example.accessibility_detect.Utils.cueRecallImg;
import static com.example.accessibility_detect.Utils.finish_crop;
import static labelingStudy.nctu.minuku.config.Constants.DATE_FORMAT_for_storing;
import static labelingStudy.nctu.minuku.config.Constants.PICTURE_DIRECTORY_PATH;
import static labelingStudy.nctu.minuku.config.SharedVariables.CanFillEsm;
import static labelingStudy.nctu.minuku.config.SharedVariables.dateToStamp;
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTime;
import static labelingStudy.nctu.minuku.config.SharedVariables.isFinish;

/**
 * This fragment provide the RadioButton/Single Options.
 */
@SuppressWarnings("unchecked")
public class CueRecallFragment extends Fragment
{
//    private ArrayList<String> FileNameArrayList = new ArrayList<>();
    private final ArrayList<RadioButton> radioButtonArrayList = new ArrayList<>();
    private boolean isFirstLoad = true; // 是否第一次加载
    private boolean screenVisible = false;
    private boolean NoCue = false;
    private static QuestionsItem radioButtonTypeQuestion;
    private FragmentActivity mContext;
    private Button nextOrFinishButton;
    MyWebView webView;
    //private Button previousButton;
    private TextView questionCRTypeTextView;
    private SharedPreferences pref;
    private RadioGroup radioGroupForChoices;
    private LinearLayout ImageViewForCueRecall;
    private String choose;
    TreeMap<String, String[]> AllOptions = new TreeMap<>(Collections.reverseOrder());
    private boolean atLeastOneChecked = false;
    boolean NoRecallOptions = false;
    boolean isImageFitToScreen = false;
    appDatabase db;
    ArrayList<String> ImageCopy = new ArrayList<>();
    private String questionId = "";
    private int currentPagePosition = 0;
    private int clickedRadioButtonPosition = 0;
    private String qState = "-1";
    private String TAG ="CueRecallFragment";
    int relatedId;
    private Gson gson;
    long LastImgTimeInSecond = 0;
    int SampleSecond = 3;
    String appName = "";
    String enterTime ="";
    private EditText editText_answer;
    String notiId = "";
    public CueRecallFragment()
    {
        // Required empty public constructor
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_cue_recall, container, false);

        db = appDatabase.getDatabase(getActivity());

        gson = new Gson();

        nextOrFinishButton = rootView.findViewById(R.id.nextOrFinishButton);
        //previousButton = rootView.findViewById(R.id.previousButton);
        questionCRTypeTextView = rootView.findViewById(R.id.questionCRTypeTextView);

//        ImageViewForCueRecall = rootView.findViewById(R.id.ImageViewForCueRecall);

        radioGroupForChoices = rootView.findViewById(R.id.radioGroupForCueRecall);

        //previousButton.setOnClickListener(view -> mContext.onBackPressed());

        return rootView;
    }


    /*This method get called only when the fragment get visible, and here states of Radio Button(s) retained*/

    //    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser)
//    {
//        super.setUserVisibleHint(isVisibleToUser);
//
//        if (isVisibleToUser)
//        {
//            screenVisible = true;
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
//        }
//    }
    public String[] KindOfRecall(){
        String[] RecallKind = new String[11];
        //FB, Messenger, Line, LineToday, IG, NewsApp, Notification, Youtube, PTT, Google, browser;
        for(int i = 0; i < RecallKind.length; i++){
            RecallKind[i] = db.questionWithAnswersDao().isChecked("1",String.valueOf(i));
            if(RecallKind[i] == null){
                RecallKind[i] = "0";
            }
            Log.d(TAG, " radioBox recall kind :  " + RecallKind[i]);
        }
        return RecallKind;
//        List<Long> array =new ArrayList<>();
//        array.add(first_Id);
//        array.add(second_Id);
//        array.add(third_Id);
//        array.add(fourth_Id);
//        array.add(other_id);
//        Log.d("qskip"," radioBox first id:  "+first_Id);
//        Log.d("qskip"," radioBox second id:  "+second_Id);
//        Log.d("qskip"," radioBox third id:  "+third_Id);
//        Log.d("qskip"," radioBox fourth id:  "+fourth_Id);
    }

    private String getTheStateOfRadioBox(String[] data)
    {
        return db.questionWithAnswersDao().isChecked(data[0], data[1]);
    }

    private void saveActionsOfRadioBox()
    {
        for (int i = 0; i < radioButtonArrayList.size(); i++)
        {
            Log.d(TAG, i + " " + clickedRadioButtonPosition);
            if (i == clickedRadioButtonPosition)
            {
                RadioButton radioButton = radioButtonArrayList.get(i);
                Log.d(TAG, "radioButton Text: " + radioButton.getText());
                if (radioButton.isChecked())
                {
                    Log.d(TAG, "radioButton " + i + "isChecked");
                    atLeastOneChecked = true;

                    String cbPosition = String.valueOf(radioButtonArrayList.indexOf(radioButton));

                    choose = radioButton.getText().toString();
                    if(choose.equals("都沒有")){
                        NoCue = true;
                    }
                    else{
                        NoCue = false;
                    }
                    Log.d(TAG, "You choose " + choose);
                    String[] data = new String[]{choose, questionId, String.valueOf(0)};
                    insertChoiceInDatabase(data);

                } else
                {
                    Log.d(TAG, "radioButton " + i + "is not Checked");
                    String cbPosition = String.valueOf(radioButtonArrayList.indexOf(radioButton));

//                    String[] data = new String[]{"0", questionId, cbPosition};
//                    insertChoiceInDatabase(data);
                }

            }
        }

        if (atLeastOneChecked)
        {
            nextOrFinishButton.setEnabled(true);
        } else
        {
            nextOrFinishButton.setEnabled(false);
        }
    }

    public Integer check_radio_answer(String questionId){
        if(questionId.equals("1")) {//第一題
            String first = db.questionWithAnswersDao().isChecked(questionId,"0");
            String second = db.questionWithAnswersDao().isChecked(questionId,"1");
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
        }
        else if(questionId.equals("2")){//0:截圖, 1:mes, 2:line, 3:ptt, 4:noti
            String messenger = db.questionWithAnswersDao().isChecked(questionId,"1");
            String line = db.questionWithAnswersDao().isChecked(questionId,"2");
            String noti = db.questionWithAnswersDao().isChecked(questionId,"6");
            String ptt = db.questionWithAnswersDao().isChecked(questionId,"8");
            Log.d(TAG, "Checked: " + messenger + " " + line + " " + noti + " " + ptt);
            if(messenger != null){
                Log.d(TAG, " radioBox messenger :  " + messenger);
                if (messenger.equals("1")) {
                    return 1;
                }
            }
            if(line != null){
                Log.d(TAG, " radioBox line :  " + line);
                if (line.equals("1")) {
                    return 2;
                }
            }
            if(ptt != null){
                Log.d(TAG, " radioBox ptt :  " + ptt);
                if (ptt.equals("1")) {
                    return 3;
                }
            }
            if(noti != null){
                Log.d(TAG, " radioBox noti :  " + noti);
                if (noti.equals("1")) {
                    return 4;
                }
            }
        }
        return 0;
    }
//    public int getLargestIndex(List<Long> arrayList){
//        if ( arrayList == null || arrayList.size() == 0 ) return -1; // null or empty
//
//        int largest = 0;
//        for ( int i = 1; i < arrayList.size(); i++ )
//        {
//            if ( arrayList.get(i) > arrayList.get(largest) ) largest = i;
//        }
//        Log.d("qskip"," radioBox largest :  "+largest);
//        return largest; // position of the first largest found
//    }



    private void insertChoiceInDatabaseForOther(String[] data)
    {
        Observable.just(data)
                .map(new Function<String[], Object>() {
                    @Override
                    public Object apply(String[] data1) throws Exception {
                        return CueRecallFragment.this.insertingInDbForOther(data1);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }
    private String insertingInDbForOther(String[] data)
    {
        String answerTime = getReadableTime(new Date().getTime());
        db.questionWithAnswersDao().updateQuestionOther(data[0], data[1], data[2], data[3]);
        Log.d("qskip"," insertingInDb : selectionState : "+data[0] +"; questionId : "+data[1]+"; optionId : "+ data[2] + " choice: " + data[3]);
        db.questionWithAnswersDao().updateQuestionOtherTime(answerTime ,data[1], data[2], data[3]);
        // updateFinalAnswer(data[1],data[2], data[0],answerTime);
        return "";
    }

    private void insertChoiceInDatabase(String[] data)
    {
        Observable.just(data)
                .map(new Function<String[], Object>() {
                    @Override
                    public Object apply(String[] data1) throws Exception {
                        return CueRecallFragment.this.insertingInDb(data1);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    private String insertingInDb(String[] data)
    {
        String answerTime = getReadableTime(new Date().getTime());
        db.questionWithAnswersDao().updateQuestionWithChoice(data[0], data[1], data[2]);
        Log.d("qskip"," insertingInDb : selectionState : "+data[0] +"; questionId : "+data[1]+"; optionId : "+ data[2]);
        db.questionWithAnswersDao().updateQuestionWithDetectedTime(answerTime ,data[1], data[2]);
        // updateFinalAnswer(data[1],data[2], data[0],answerTime);
        return "";
    }

//    public void updateFinalAnswer(String questionId, String optionId, String selectState, String answerTime){
//        //第幾題、第幾個選項、第幾個解答、回答時間
//        Integer ansId = MapAnswerPositiontoId(Integer.parseInt(questionId),Integer.parseInt(optionId));
//
//
//
//        FinalAnswerDataRecord finalAnswerDataRecord = new FinalAnswerDataRecord();
//        finalAnswerDataRecord.setAnswerChoice(optionId);
//        finalAnswerDataRecord.setanswerId(String.valueOf(ansId));
//        finalAnswerDataRecord.setAnswerChoiceState(selectState);
//        finalAnswerDataRecord.setdetectedTime(answerTime);
//        finalAnswerDataRecord.setQuestionId(questionId);
//        finalAnswerDataRecord.setsyncStatus(0);
//        finalAnswerDataRecord.setRelatedId(relatedId);
//        finalAnswerDataRecord.setcreationIme(new Date().getTime());
//        db.finalAnswerDao().insertAll(finalAnswerDataRecord);
//
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
        mContext = (FragmentActivity) getActivity();
        pref = mContext.getSharedPreferences("test", MODE_PRIVATE);
        if (getArguments() != null)
        {
//            radioButtonTypeQuestion = getArguments().getParcelable("question");// question type = CueRecall
            radioButtonTypeQuestion = QuestionActivity.questionsItems.get(getArguments().getInt("page_position"));
            questionId = String.valueOf(radioButtonTypeQuestion != null ? radioButtonTypeQuestion.getId() : 0);// 這題的ID = 2
            currentPagePosition = getArguments().getInt("page_position") + 1;// 現在是第幾頁
            enterTime = getArguments().getString("enterTimeForF");// 跳出問卷的時間
            relatedId = getArguments().getInt("relatedIdF");// 第幾個問卷
            notiId = getArguments().getString("notiId");// 1

        }
        Long nowESM_time = pref.getLong("Now_Esm_Time", 0);
        Long lastESM_time = nowESM_time - 18000000;
        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
        if(userRecord != null){
            lastESM_time = userRecord.getLastESMTime_for_Q1();
        }
        if(lastESM_time == 0)lastESM_time = nowESM_time - 18000000;
//        Long lastESM_time = pref.getLong("Last_Esm_Time", nowESM_time - 18000000);
        String apppendText = "";
        if(notiId.equals("1")){
            if(currentPagePosition == 1 || currentPagePosition == 3) {
                if (lastESM_time == (nowESM_time - 18000000)) {
                    apppendText = getReadableTime(nowESM_time) + " 前可能接觸到新聞";
                } else {
                    apppendText = "從前一次回答問卷 (" + getReadableTime(lastESM_time) + " ) 至現在 (" + getReadableTime(nowESM_time) + ")可能接觸到新聞";
                }
            }
        }
        String title;
        if(currentPagePosition == 1 || currentPagePosition == 3){
            title = radioButtonTypeQuestion != null ? apppendText+'\n'+radioButtonTypeQuestion.getQuestionName():"";
        }else{
            title = radioButtonTypeQuestion != null ? radioButtonTypeQuestion.getQuestionName():"";
        }
        Log.d(TAG,"title : "+title);
        //questionRBTypeTextView.setText(radioButtonTypeQuestion.getQuestionName()+'\n'+apppendText);
        //setTextWithSpan(title,questionRBTypeTextView);
        finish_crop = false;
        radioButtonArrayList.clear();
        AllOptions.clear();
        String[] recall_kind = KindOfRecall();
        String app = "";

        Log.d(TAG, "Get Images");
        LastImgTimeInSecond = 0;
        getImages(recall_kind, lastESM_time, nowESM_time);

        for(int i = 0; i < recall_kind.length; i++){
            if(i == 0)app = "Facebook";
            else if(i == 1) app = "Messenger";//webview
            else if(i == 2) app = "LineChat";//webview
            else if(i == 3) app = "LineToday";
            else if(i == 4) app = "Instagram";
            else if(i == 5) app = "NewsApp";
            else if(i == 6) app = "Notification";//title text
            else if(i == 7) app = "Youtube";
            else if(i == 8) app = "PTT";//title text
            else if(i == 9) app = "googleNews";
            else if(i == 10) app = "Chrome";
            if(recall_kind[i].equals("1")){
                Log.d(TAG, app);
//                if(!app.equals("Notification") && !app.equals("Messenger") && !app.equals("LineChat")
//                    && !app.equals("PTT")){
//                    Log.d(TAG, "Get Images");
//                    LastImgTimeInSecond = 0;
//                    getImages(app, lastESM_time, nowESM_time);
//                    //                    if(ImageCopy != null) {
////                        Collections.sort(ImageCopy);
////                        Collections.reverse(ImageCopy);
////                        int s = ImageCopy.size();
////                        if (s != 0) {
////                            for (int j = 0; j < s; j++) {
////                                String[] temp = ImageCopy.get(j).split("/");
////                                String[] fileName_split = temp[temp.length - 1].split("-|\\.");
////                                String[] fileDate_split = temp[temp.length - 2].split("-");
////                                String Radio_text = fileDate_split[0] + "-" + fileDate_split[1] + "-" + fileDate_split[2] + " " + fileName_split[0] + ":" + fileName_split[1] + ":" + fileName_split[2];
////                                Log.d(TAG, Radio_text);
//////                                AllOptions.put(Radio_text, new String[]{"Image", ImageCopy.get(j)});
//////                                setImageContent(Radio_text, ImageCopy.get(j));
////                            }
////                        }
////                    }
//                }
                if(app.equals("Notification")){
                    Log.d(TAG, "Get Notification");
                    getNotification(lastESM_time, nowESM_time);
//                    for(int j = 0; j < option.size(); j++){
//                        AllOptions.put(option.get(i)[3], option.get(i));
//                    }
//                    setNotificationContent(option);
                }
                else if(app.equals("Messenger") || app.equals("LineChat")){
                    Log.d(TAG, "Get Session");
                    getSessionURL(lastESM_time, nowESM_time, app);
//                    for(int j = 0; j < option.size(); j++){
//                        AllOptions.put(option.get(i)[1], option.get(i));
//                    }
//                    setSessionURLContent(option);
                }
                else if(app.equals("PTT")){
                    Log.d(TAG, "Get PTT");
                    getPTTtitle(lastESM_time, nowESM_time);
//                    for(int j = 0; j < option.size(); j++){
//                        AllOptions.put(option.get(i)[1], option.get(i));
//                    }
//                    setPTTtitleContent(option);
                }
            }
        }

        if(AllOptions != null) {
            Log.d(TAG, "Not null");
            if(AllOptions.size() != 0) {
                Log.d(TAG, "Not zero");
//                NoRecallOptions = false;
                Set<String> set = AllOptions.keySet();
                LastImgTimeInSecond = 0;
                for (String key : set) {
                    if (AllOptions.get(key)[0].equals("Image")) {
                        setImageContent(AllOptions.get(key)[1], AllOptions.get(key)[2]);
                    } else if (AllOptions.get(key)[0].equals("Session")) {
                        setSessionURLContent(AllOptions.get(key));
                    } else if (AllOptions.get(key)[0].equals("Notification")) {
                        setNotificationContent(AllOptions.get(key));
                    } else if (AllOptions.get(key)[0].equals("PTT")) {
                        setPTTtitleContent(AllOptions.get(key));
                    }
                }
            }
//            else{
//                NoRecallOptions = true;
//                title = apppendText + "\n" + "很抱歉，系統沒有抓到您選的平台相關活動截圖，請按下按鈕完成此問卷";
//            }
        }
//        else{
//            NoRecallOptions = true;
//            title = apppendText + "\n" + "很抱歉，系統沒有抓到您選的平台相關活動截圖，請按下按鈕完成此問卷";
//        }
        String Radio_text = "都沒有";
        RadioButton rb = new RadioButton(mContext);
        rb.setText(Radio_text);
        rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        rb.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
        rb.setPadding(10, 20, 10, 20);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 25;
        params.bottomMargin = 25;
        rb.setLayoutParams(params);

        radioGroupForChoices.addView(rb);
        radioButtonArrayList.add(rb);
        rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (screenVisible) {
                    Log.d(TAG, "radio button is 都沒有");
                    clickedRadioButtonPosition = radioButtonArrayList.indexOf(buttonView);
                    CueRecallFragment.this.saveActionsOfRadioBox();
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            questionCRTypeTextView.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY));
        }
        else{
            questionCRTypeTextView.setText(Html.fromHtml(title));
        }
//        List<AnswerOptions> choices = radioButtonTypeQuestion.getAnswerOptions();

//        for (AnswerOptions choice : choices)
//        {
//            if(choice.getName().equals(others)){
//                editText_answer = new EditText(mContext);
//                editText_answer.setHint(others);
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                params.leftMargin = 25;
//                radioGroupForChoices.addView(editText_answer, params);
//                editText_answer.addTextChangedListener(new TextWatcher() {
//                    @Override
//                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                    }
//
//                    @Override
//                    public void onTextChanged(CharSequence s, int start, int before, int count) {
//                    }
//
//                    @Override
//                    public void afterTextChanged(Editable s) {
//                        if (s.length() >= 1 && !s.toString().equals("0")) {
//                            nextOrFinishButton.setEnabled(true);
//                        } else {
//                            nextOrFinishButton.setEnabled(false);
//                        }
//
//                        radioGroupForChoices.clearCheck();
//                        for (int i = 0; i < radioButtonArrayList.size(); i++)
//                        {
//                            RadioButton radioButton = radioButtonArrayList.get(i);
//                            {
//                                Log.d(TAG, "radioButton " + i + "is not Checked");
//                                String cbPosition = String.valueOf(radioButtonArrayList.indexOf(radioButton));
//
//                                String[] data = new String[]{"0", questionId, cbPosition};
//                                insertChoiceInDatabase(data);
//                            }
//                        }
//
//                        if(editText_answer.getText().toString().equals("")){
//                            atLeastOneChecked = false;
//                            nextOrFinishButton.setEnabled(false);
//                            String[] data = new String[]{"0", questionId, editText_answer.getText().toString(), others};
//                            insertChoiceInDatabaseForOther(data);
//                        }
//                        else {
//                            String[] data = new String[]{"1", questionId, editText_answer.getText().toString(), others};
//                            insertChoiceInDatabaseForOther(data);
//                        }
////                        String[] data = new String[]{editText_answer.getText().toString(), questionId, String.valueOf(0)};
////                        insertChoiceInDatabase(data);
//
//                    }
//                });
//            }else{
//                RadioButton rb = new RadioButton(mContext);
//                rb.setText(choice.getName());
//                rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
//                rb.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
//                rb.setPadding(10, 40, 10, 40);
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                params.leftMargin = 25;
//                rb.setLayoutParams(params);
//
//
//                View view = new View(mContext);
//                view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.divider));
//                view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
//
//                radioGroupForChoices.addView(rb);
//                radioGroupForChoices.addView(view);
//                radioButtonArrayList.add(rb);
//                atLeastOneChecked = false;
//                rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        if (screenVisible) {
//                            Log.d(TAG, "radio button is changed");
//                            clickedRadioButtonPosition = radioButtonArrayList.indexOf(buttonView);
//                            CueRecallFragment.this.saveActionsOfRadioBox();
//                        }
//                    }
//                });
//            }
//
//
//        }
        nextOrFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(!pageRecord.contains(currentPagePosition))
//                    pageRecord.add(currentPagePosition);

//                    int question_id = 23;
//                    for(int i = 0; i < Q22Answer.size(); i++){
//                        String s = InsertNewQuestions(Q22Answer.get(i), question_id);
//                        QuestionDataModel questionDataModel = new QuestionDataModel();
//                        questionDataModel = gson.fromJson(s, QuestionDataModel.class);
//                        List<QuestionsItem> folloingItems = new ArrayList<>();
//                        folloingItems = questionDataModel.getData().getQuestions();
//                        preparingQuestionInsertionInDb(folloingItems);
//                        Log.d(TAG,"parsingData in fragment :"+ relatedId);
//                        preparingInsertionInDb(folloingItems, relatedId);
//                        for(int j = 0; j < folloingItems.size(); j++){
//                            QuestionsItem question = folloingItems.get(i);
//                            questionsItems.add(question);
//                        }
//                        question_id++;
//                    }
//                    totalQuestions = String.valueOf(questionsItems.size());
//                    String questionPosition = "1/" + totalQuestions;
//                    ((QuestionActivity) mContext).nextQuestion(1);
//                }
//                if(currentPagePosition == 26 + (Q22Answer.size() - 1) * multitask_following){
//                    Intent returnIntent = new Intent();
//                    if (check_radio_answer("1") == 1) {
//                        returnIntent.putExtra("isMobileCrowdsource", true);
//                    }
//                    mContext.setResult(Activity.RESULT_OK, returnIntent);
//                    mContext.finish();
//                }
//                else
                Intent MainIntent = new Intent(mContext, MainActivity.class);

                if(choose.matches("您於.*的手機畫面")){
                    File f = null;
                    for(int i = 0; i < radioButtonArrayList.size() - 1; i++){
                        Set<Map.Entry<String, String[]>> entries = AllOptions.entrySet();
                        List<Map.Entry<String, String[]>> listEntries = new ArrayList<Map.Entry<String, String[]>>(entries);
                        Log.d(TAG, "FileNameArrayList: " + listEntries.get(i).getValue()[2]);
                        RadioButton radioButton = radioButtonArrayList.get(i);
                        if(radioButton.isChecked()){
                            Log.d(TAG, "radio button text: " + radioButton.getText().toString());
                            f = new File(listEntries.get(i).getValue()[2]); // AllOption
                        }
                    }
                    if(f != null){
                        cueRecallImg = Uri.fromFile(f);
                        Crop_Uri = cueRecallImg;
                        Log.d(TAG, "cue recall uri: " + cueRecallImg.toString());
                    }
                }
                else{
                    cueRecallImg = null;
                }
//                nextOrFinishButton.setEnabled(false);
                if (currentPagePosition == ((QuestionActivity) mContext).getTotalQuestionsSize() || NoCue) {
                    /* Here, You go back from where you started OR If you want to go next Activity just change the Intent*/
                    UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
                    if(userRecord != null) {
                        db.userDataRecordDao().updateCanFillESM(userRecord.get_id(), false);
                    }
                    CanFillEsm = false;
                    isFinish = "2";
                    startActivity(MainIntent);
                    mContext.finish();
                }
                else if(!choose.matches("您於.*的手機畫面")){
                    Log.d(TAG, "jump three page");
                    ((QuestionActivity) mContext).nextQuestion(3);
                }
                else {
                    ((QuestionActivity) mContext).nextQuestion(2);
                }
            }
        });
//        CueRecallFragment.this.saveActionsOfRadioBox();
        if (atLeastOneChecked)
        {
            nextOrFinishButton.setEnabled(true);
        } else
        {
            nextOrFinishButton.setEnabled(false);
        }

        /* If the current question is last in the questionnaire then
        the "Next" button will change into "Finish" button*/
        if (currentPagePosition == ((QuestionActivity) mContext).getTotalQuestionsSize())
        {
            nextOrFinishButton.setText(R.string.finish);
        } else
        {
            nextOrFinishButton.setText(R.string.next);
        }
    }
    private ArrayList<String[]> getPTTtitle(Long lastESM_time, Long nowESM_time){
        ArrayList<String[]> ChoiceOption = new ArrayList<>();
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
                        for (int j = 0; j < newsarr.size(); j++) {
                            String content = newsarr.get(j).getcontent();
                            ChoiceOption.add(new String[]{"PTT", getReadableTime(timestamp), content});
                            AllOptions.put(getReadableTime(timestamp)+content, new String[]{"PTT", getReadableTime(timestamp), content});
//                            FileNameArrayList.add("NA");
//                            Log.d(TAG, "ptt title: " + getReadableTime(timestamp) + " " + content);
                        }
                    }
                    transCursor.moveToNext();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
        }
        return ChoiceOption;
    }
    private void setPTTtitleContent(String[] ChoiceOption){
//        Log.d(TAG, "SetPTTContent");
        String[] content = ChoiceOption;
//        Log.d(TAG, "session url: " + content[1] + " " + content[2]);//time, url
        String Radio_text = "於" + content[1] + "在PTT上看到以下標題:\n" + content[2];
        RadioButton rb = new RadioButton(mContext);
        rb.setText(Radio_text);
        rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        rb.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
        rb.setPadding(10, 20, 10, 20);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 25;
        params.bottomMargin = 25;
        rb.setLayoutParams(params);

        radioGroupForChoices.addView(rb);
        radioButtonArrayList.add(rb);
        rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (screenVisible) {
                    Log.d(TAG, "radio button is changed");
                    clickedRadioButtonPosition = radioButtonArrayList.indexOf(buttonView);
                    CueRecallFragment.this.saveActionsOfRadioBox();
                }
            }
        });
    }

    private ArrayList<String[]> getSessionURL(Long lastESM_time, Long nowESM_time, String app_name){
        ArrayList<String[]> ChoiceOption = new ArrayList<>();
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
                    if (timestamp > lastESM_time && timestamp < nowESM_time && app_col.equals(app_name)
                    ) {
                        List<NewsDataRecord> newsarr = db.SessionDataRecordDao().getNewsData(sid);
                        for(int j = 0; j < newsarr.size(); j++){
                            String content = newsarr.get(j).getcontent();//url
                            ChoiceOption.add(new String[]{"Session", getReadableTime(timestamp), content, app_name});
                            AllOptions.put(getReadableTime(timestamp) + content, new String[]{"Session", getReadableTime(timestamp), content, app_name});
//                            FileNameArrayList.add("NA");
//                            Log.d(TAG, "session: " + getReadableTime(timestamp) + " " + content + app_name);
                        }
                    }
                    transCursor.moveToNext();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
        }
        return ChoiceOption;
    }
    private void setSessionURLContent(String[] ChoiceOption){
//        Log.d(TAG, "SetSessionContent");
        String[] content = ChoiceOption;

//        Log.d(TAG, "session url: " + content[1] + " " + content[2]  + " " + content[3]);//time, url,app
        if(!content[0].equals("") || !content[1].equals("")) {
            String Radio_text = "於" + content[1] + "在" + content[3] + "上看到以下網頁標題:\n獲取網頁標題中....";
            RadioButton rb = new RadioButton(mContext);
            rb.setText(Radio_text);
            rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            rb.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
            rb.setPadding(10, 20, 10, 20);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 25;
            rb.setLayoutParams(params);

            webView = new MyWebView(mContext);

            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setAppCacheEnabled(false);

            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            } else {
                webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }

            webView.loadUrl(content[2]);

            webView.setWebViewClient(new WebViewClient());

            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onReceivedTitle(WebView view, String title) {
                    Log.d(TAG, "網站標題: " + title);
                    rb.setText("於" + content[1] + "在" + content[3] + "上看到以下網頁標題:\n" + title);
                    //WebTitle.setText(title);
                    // 還有設計JS界面的對話框 警示框等
                }
            });
            radioGroupForChoices.addView(rb);
//        radioGroupForChoices.addView(view);

            radioButtonArrayList.add(rb);
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (screenVisible) {
                        Log.d(TAG, "radio button is changed");
                        clickedRadioButtonPosition = radioButtonArrayList.indexOf(buttonView);
                        CueRecallFragment.this.saveActionsOfRadioBox();
                    }
                }
            });
        }
    }

    private ArrayList<String[]> getNotification(Long lastESM_time, Long nowESM_time){
        ArrayList<String[]> ChoiceOption = new ArrayList<>();
//        HashMap<String, String[]> NotificationOptionName = new HashMap<>();
        Cursor transCursor = null;
        try {
            transCursor = db.notificationDataRecordDao().getUnsyncedDataAll(0);
            int rows = transCursor.getCount();
            if (rows != 0) {
                transCursor.moveToFirst();
                for (int i = 0; i < rows; i++) {
                    Long timestamp = transCursor.getLong(1);
                    String title_col = transCursor.getString(2);
                    String n_text_col = transCursor.getString(3);
                    String app_col = transCursor.getString(6);
                    String reason = transCursor.getString(8);
                    if (timestamp > lastESM_time && timestamp < nowESM_time && !app_col.equals("NewsConsumption") && reason.equals("POST")) {
                        ChoiceOption.add(new String[] {"Notification", app_col, n_text_col, getReadableTime(timestamp), title_col});
                        AllOptions.put(getReadableTime(timestamp) + n_text_col, new String[] {"Notification", app_col, n_text_col, getReadableTime(timestamp), title_col});
//                        FileNameArrayList.add("NA");
//                        Log.d(TAG, n_text_col + " " + getReadableTime(timestamp) + " " + title_col);
                    }
                    transCursor.moveToNext();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(transCursor != null){
                transCursor.close();
            }
        }
        return ChoiceOption;
    }
    private void setNotificationContent(String[] ChoiceOption){
//        Log.d(TAG, "SetNotiContent");
        String[] content = ChoiceOption;
//        Log.d(TAG, "Web url: " + content[1] + " " + content[2] + " " + content[3] + " " + content[4]);//app, url, time, people
        if(content[2].length() > 5) {
            if (content[2].substring(0, 5).equals("http:") || content[2].substring(0, 6).equals("https:")) {
                String Radio_text = content[3] + " \"" + content[4] + "\" 用" + content[1] + "傳給你以下網頁標題:\n獲取網頁標題中....";
                RadioButton rb = new RadioButton(mContext);
                rb.setText(Radio_text);
                rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                rb.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
                rb.setPadding(10, 20, 10, 20);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = 25;
                rb.setLayoutParams(params);
//                TextView WebTitle = new TextView(mContext);
//                LinearLayout.LayoutParams Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                Params.leftMargin = 120;
//                Params.rightMargin = 25;
//                Params.bottomMargin = 30;
//                WebTitle.setLayoutParams(Params);
//                WebTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
//                WebTitle.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
//                WebTitle.setText("獲取網頁標題中....");
                webView = new MyWebView(mContext);

                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setDomStorageEnabled(true);
                webView.getSettings().setAppCacheEnabled(false);

                webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                } else {
                    webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }

                webView.loadUrl(content[2]);

                webView.setWebViewClient(new WebViewClient());
                webView.setWebChromeClient(new WebChromeClient(){
                    @Override
                    public void onReceivedTitle(WebView view, String title) {
                        Log.d(TAG, "網站標題: " + title);
                        rb.setText(content[3] + " \"" + content[4] + "\" 用" + content[1] + "傳給你以下網頁標題:\n" + title);
                        // 還有設計JS界面的對話框 警示框等
                    }
                });
                radioGroupForChoices.addView(rb);
//                radioGroupForChoices.addView(WebTitle);
//        radioGroupForChoices.addView(view);

                radioButtonArrayList.add(rb);
                rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (screenVisible) {
                            Log.d(TAG, "radio button is changed");
                            clickedRadioButtonPosition = radioButtonArrayList.indexOf(buttonView);
                            CueRecallFragment.this.saveActionsOfRadioBox();
                        }
                    }
                });
            }
            else{
//                Log.d(TAG, "Web url: " + content[1] + " " + content[2] + " " + content[3]);
                String Radio_text = content[3] + " " + content[4] + "使用" + content[1] + "傳給你以下標題:\n" + content[2];
                RadioButton rb = new RadioButton(mContext);
                rb.setText(Radio_text);
                rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                rb.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
                rb.setPadding(10, 20, 10, 20);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = 25;
                params.bottomMargin = 25;
                rb.setLayoutParams(params);

                radioGroupForChoices.addView(rb);

                radioButtonArrayList.add(rb);
                rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (screenVisible) {
                            Log.d(TAG, "radio button is changed");
                            clickedRadioButtonPosition = radioButtonArrayList.indexOf(buttonView);
                            CueRecallFragment.this.saveActionsOfRadioBox();
                        }
                    }
                });
            }
        }
        else{
//            Log.d(TAG, "Web url: " + content[1] + " " + content[2] + " " + content[3]);
            if(!content[2].equals("")) {
                String Radio_text = content[3] + " " + content[4] + "使用" + content[1] + "傳給你以下標題\n" + content[2];
                RadioButton rb = new RadioButton(mContext);
                rb.setText(Radio_text);
                rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                rb.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
                rb.setPadding(10, 20, 10, 20);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = 25;
                params.bottomMargin = 25;
                rb.setLayoutParams(params);

                radioGroupForChoices.addView(rb);

                radioButtonArrayList.add(rb);
                rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (screenVisible) {
                            Log.d(TAG, "radio button is changed");
                            clickedRadioButtonPosition = radioButtonArrayList.indexOf(buttonView);
                            CueRecallFragment.this.saveActionsOfRadioBox();
                        }
                    }
                });
            }
        }
    }

    private void getImages(String[] recall_kind, Long last_esm_time, Long now_esm_time){
//        Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
        String last_esm = getReadableTime(last_esm_time);//2020-05-12 14:20:15
        String now_esm = getReadableTime(now_esm_time);
//        String ESMtime = pref.getString("ESMtime","NA");//2020-05-15 14:29:35
        Log.d(TAG,"ESMtime: " + now_esm);
//        String ESMdate = ESMtime.split(" ")[0];

        String index = last_esm.split(" ")[0];
        ArrayList<String> FileList = new ArrayList<>();
        while(true){
            Log.d(TAG, "index = " + index);
            FileList.add(index);
            if(index.equals(now_esm.split(" ")[0]))break;
            index = ReadableTimeAddDay(index, 1);
        }

        for(int k = 0; k < FileList.size(); k++) {
            File imgFile = new File(Environment.getExternalStorageDirectory().getPath() + PICTURE_DIRECTORY_PATH + FileList.get(k));
            Log.d(TAG, "Path exists: " + imgFile);
//            CSVHelper.storeToCSV("RecycleView.csv", "Path exists: " + imgFile);
//            ArrayList<Boolean> CheckCopy = new ArrayList<>();
            if (imgFile.exists()) {
//            Toast.makeText(this, imgFile + "exists", Toast.LENGTH_LONG).show();
                File[] files = imgFile.listFiles(); //在日期資料夾裡面的每一張圖
                if (imgFile.listFiles() != null) {
                    List fileList = Arrays.asList(files);
                    Collections.sort(fileList, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            if (o1.isDirectory() && o2.isFile())
                                return -1;
                            if (o1.isFile() && o2.isDirectory())
                                return 1;
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    for (int i = 0; i < files.length; i++) { //每一張圖
                        Log.d(TAG, "FileName:" + files[i].getName());//09-01-30-2-googleNews.jpg // 9/23
                        try {
                            if (!files[i].getName().split("\\.")[1].equals("csv") && !files[i].getName().split("\\.")[1].equals("txt")
                                    && !files[i].getName().contains("ESM")) {
                                String[] fileName_split = files[i].getName().split("-|\\.");//{09,01,30}
                                String app = "";
                                if(files[i].getName().contains("Upload")){
                                    app = fileName_split[5];
                                }
                                else{
                                    app = fileName_split[4];
                                }

                                for(int m = 0; m < recall_kind.length; m++) {
                                    String app_name = "";
                                    if (m == 0) app_name = "Facebook";
                                    else if (m == 1) app_name = "Messenger";//webview
                                    else if (m == 2) app_name = "LineChat";//webview
                                    else if (m == 3) app_name = "LineToday";
                                    else if (m == 4) app_name = "Instagram";
                                    else if (m == 5) app_name = "NewsApp";
                                    else if (m == 6) app_name = "Notification";//title text
                                    else if (m == 7) app_name = "Youtube";
                                    else if (m == 8) app_name = "PTT";//title text
                                    else if (m == 9) app_name = "googleNews";
                                    else if (m == 10) app_name = "Chrome";
                                    if ((app.equals(app_name) && recall_kind[m].equals("1")) || (m == 10 && recall_kind[m].equals("1") && !recall_kind[9].equals("1") && app.equals("googleNews"))) { // 9/23
                                        String filename = imgFile.toString() + "/" + files[i].getName();
                                        String[] filname_split = filename.split("/");
                                        String date = filname_split[filname_split.length - 2];//2020-05-16
                                        String time = filname_split[filname_split.length - 1];//07-35-48-2-facebook.jpg // 9/23
                                        String[] time_split = time.split("-|\\.");
                                        if (time.contains("Upload")) {
                                            time = time_split[1] + ":" + time_split[2] + ":" + time_split[3]; // 9/23 10-18-01
                                        } else {
                                            time = time_split[0] + ":" + time_split[1] + ":" + time_split[2]; // 9/23
                                        }
                                        if (isInTenMinute(date, time, last_esm_time, now_esm_time)) {
//                                    ImageCopy.add(imgFile.toString() + "/" + files[i].getName());
//                                        String[] temp = (imgFile.toString() + "/" + files[i].getName()).split("/");
//                                        String[] fileName_spl = temp[temp.length - 1].split("-|\\.");
//                                        String[] fileDate_spl = temp[temp.length - 2].split("-");
                                            String Radio_text = "您於" + date + " " + time + "的手機畫面";
                                            Log.d(TAG, Radio_text);
                                            String TakeTime = Radio_text.split(" ")[1];//07-35-58的手機畫面
                                            String[] str = TakeTime.substring(0, TakeTime.indexOf("的")).split(":");//07-35-58
                                            long ImgTimeInMs = getMilliFromDate(date + " " + str[0] + ":" + str[1] + ":" + str[2]);
                                            int ImgTimeInSecond = 3600 * Integer.parseInt(str[0]) + 60 * Integer.parseInt(str[1]) + Integer.parseInt(str[2]);
                                            Log.d(TAG, ImgTimeInSecond + " " + LastImgTimeInSecond);
                                            if (ImgTimeInMs - LastImgTimeInSecond >= SampleSecond * 1000) {
                                                AllOptions.put(date + " " + time, new String[]{"Image", Radio_text, filename});
//                                            FileNameArrayList.add(filename);
                                                LastImgTimeInSecond = ImgTimeInMs;
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    private void setImageContent(String Radio_text, String img){
        Log.d(TAG, "SetImgContent");
        String TakeTime = Radio_text.split(" ")[1];//07-35-58的手機畫面
        String[] str = TakeTime.substring(0, TakeTime.indexOf("的")).split(":");//07-35-58
        int ImgTimeInSecond = 3600 * Integer.parseInt(str[0]) + 60 * Integer.parseInt(str[1]) + Integer.parseInt(str[2]);
        if(true) {
            RadioButton rb = new RadioButton(mContext);
            rb.setText(Radio_text);
            rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            rb.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
            rb.setPadding(10, 20, 10, 20);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 25;
            rb.setLayoutParams(params);
//                                linear.addView(rb);

            ImageView iv = new ImageView(mContext);
            params = new LinearLayout.LayoutParams(
                    300, 400);
            params.leftMargin = 100;
            params.bottomMargin = 30;
            iv.setLayoutParams(params);
            Glide.with(this)
                    .asBitmap()
                    .load(img)
                    .into(iv);
            iv.setClickable(true);
            iv.setFocusable(true);

            iv.setOnClickListener(new View.OnClickListener() {
                //@Override
                public void onClick(View v) {
                    Log.v(TAG, " click");

                    Intent intent = new Intent(mContext, FullScreenImage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                    iv.buildDrawingCache();
                    Bitmap image = iv.getDrawingCache();

                    Bundle extras = new Bundle();
                    extras.putString("imagebitmap", img);
                    intent.putExtras(extras);
                    startActivity(intent);
                }
            });
//                                linear.addView(iv);

            View view = new View(mContext);
            view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.divider));
            view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));

            radioGroupForChoices.addView(rb);
            radioGroupForChoices.addView(iv);
//        radioGroupForChoices.addView(view);

            radioButtonArrayList.add(rb);
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (screenVisible) {
                        Log.d(TAG, "radio button is changed");
                        clickedRadioButtonPosition = radioButtonArrayList.indexOf(buttonView);
                        CueRecallFragment.this.saveActionsOfRadioBox();
                    }
                }
            });
//            LastImgTimeInSecond = ImgTimeInSecond;
        }
    }
    private boolean isInTenMinute(String filename_date, String filename_time, Long last_esm_time, Long now_esm_time){
        boolean result = false;
//        String[] filname_split = filename.split("/");
//        String date = filname_split[filname_split.length - 2];//2020-05-16
//        String time = filname_split[filname_split.length - 1];//07-35-48.125-facebook.jpg
//        String[] time_split = time.split("-|\\.");
//        time = time_split[0] + ":" + time_split[1] + ":" + time_split[2];
        Long filetime = 0L;
        try {
            filetime = Long.parseLong(dateToStamp(filename_date + " " + filename_time));
        }catch(Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, "After compute: " + filetime + " " + last_esm_time);
        if(filetime > last_esm_time && filetime < now_esm_time){
            result = true;
        }
        Log.d(TAG, "result: " + result);
        return result;
    }
    public static String ReadableTimeAddDay(String date, int a){
        String[] date_split = date.split("-");
        int Year = Integer.parseInt(date_split[0]);
        int Month = Integer.parseInt(date_split[1]);
        int Day = Integer.parseInt(date_split[2]);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Year);
        cal.set(Calendar.MONTH, Month - 1);
        cal.set(Calendar.DAY_OF_MONTH, Day);

        cal.add(Calendar.DAY_OF_MONTH, a);

        String year = String.valueOf(cal.get(Calendar.YEAR));                      //取出年
        String month = String.valueOf(cal.get(Calendar.MONTH) + 1);           //取出月，月份的編號是由0~11 故+1
        String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));

        if(month.length() == 1) month = "0" + month;
        if(day.length() == 1) day = "0" + day;
        return year + "-" + month + "-" + day;
    }

    private void setTextWithSpan(String questionPosition, TextView tv)
    {
        int slashPosition = questionPosition.indexOf("您");

        Spannable spanText = new SpannableString(questionPosition);
        spanText.setSpan(new RelativeSizeSpan(0.6f), slashPosition, questionPosition.length(), 0);
        tv.setText(spanText);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFirstLoad = true;
        if(webView != null) {
            webView.stopLoading();
            webView.getSettings().setJavaScriptEnabled(false);
            webView.clearHistory();
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFirstLoad) {
            // 将数据加载逻辑放到onResume()方法中
            screenVisible = true;
            for (int i = 0; i < radioButtonArrayList.size(); i++)
            {
                RadioButton radioButton = radioButtonArrayList.get(i);
                String cbPosition = String.valueOf(i);
                String[] data = new String[]{questionId, cbPosition};
                Log.d(TAG,"setUserVisibleHint i = "+ i + " data = "+ data.toString());

                Observable.just(data)
                        .map(this::getTheStateOfRadioBox)
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
                                if (qState.equals("1"))
                                {
                                    radioButton.setChecked(true);
                                } else if(qState.equals("-1"))
                                {
                                    radioButton.setChecked(false);
                                }
                                else{
//                                    radioButton.setChecked(false);
                                }
                            }
                        });
            }
            isFirstLoad = false;
        }
    }

    public long getMilliFromDate(String dateFormat) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_for_storing);
        try {
            date = formatter.parse(dateFormat);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println("Today is " + date);
        return date.getTime();
    }
}