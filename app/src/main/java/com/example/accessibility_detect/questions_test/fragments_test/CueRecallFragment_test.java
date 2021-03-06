package com.example.accessibility_detect.questions_test.fragments_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import com.example.accessibility_detect.questions_test.FullScreenImage_test;
import com.example.accessibility_detect.questions_test.QuestionActivity_test;
import com.example.accessibility_detect.questions_test.adapters_test.MyWebView_test;
import com.example.accessibility_detect.questions_test.questionmodels_test.QuestionsItem_test;
import com.google.gson.Gson;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
import static labelingStudy.nctu.minuku.config.SharedVariables.CanFillEsm;
import static labelingStudy.nctu.minuku.config.SharedVariables.dateToStamp;
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTime;
import static labelingStudy.nctu.minuku.config.SharedVariables.isFinish;

/**
 * This fragment provide the RadioButton/Single Options.
 */
@SuppressWarnings("unchecked")
public class CueRecallFragment_test extends Fragment
{
//    private ArrayList<String> FileNameArrayList = new ArrayList<>();
    private final ArrayList<RadioButton> radioButtonArrayList = new ArrayList<>();
    private boolean isFirstLoad = true; // ?????????????????????
    private boolean screenVisible = false;
    private boolean NoCue = false;
    private QuestionsItem_test radioButtonTypeQuestion;
    private FragmentActivity mContext;
    private Button nextOrFinishButton;
    MyWebView_test webView;
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
    public CueRecallFragment_test()
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
                    if(choose.equals("?????????")){
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
        if(questionId.equals("1")) {//?????????
            String first = db.questionWithAnswersDao().isChecked(questionId,"0");
            String second = db.questionWithAnswersDao().isChecked(questionId,"1");
            Log.d(TAG, "???: " + first + " ??????" + second);
            if (first != null) {
                Log.d(TAG, " radioBox first :  " + first);
                if (first.equals("1")) {
                    return 0;   // ???
                }
            }
            if (second != null) {
                Log.d(TAG, " radioBox second :  " + second);
                if (second.equals("1")) {
                    return 1;  //???
                }
            }
        }
        else if(questionId.equals("2")){//0:??????, 1:mes, 2:line, 3:ptt, 4:noti
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
                        return CueRecallFragment_test.this.insertingInDbForOther(data1);
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
                        return CueRecallFragment_test.this.insertingInDb(data1);
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
//        //????????????????????????????????????????????????????????????
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
            radioButtonTypeQuestion = getArguments().getParcelable("question");// question type = CueRecall
            questionId = String.valueOf(radioButtonTypeQuestion != null ? radioButtonTypeQuestion.getId() : 0);// ?????????ID = 2
            currentPagePosition = getArguments().getInt("page_position") + 1;// ??????????????????
            enterTime = getArguments().getString("enterTimeForF");// ?????????????????????
            relatedId = getArguments().getInt("relatedIdF");// ???????????????
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
        if(notiId.equals("-1")){
            if(currentPagePosition == 1 || currentPagePosition == 3) {
                if (lastESM_time == (nowESM_time - 18000000)) {
                    apppendText = getReadableTime(nowESM_time) + " ????????????????????????";
                } else {
                    apppendText = "???????????????????????? (" + getReadableTime(lastESM_time) + " ) ????????? (" + getReadableTime(nowESM_time) + ")?????????????????????";
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
                    } else if (AllOptions.get(key)[0].equals("Notification")) {
                        setNotificationContent(AllOptions.get(key));
                    }
                }
            }
//            else{
//                NoRecallOptions = true;
//                title = apppendText + "\n" + "????????????????????????????????????????????????????????????????????????????????????????????????";
//            }
        }
//        else{
//            NoRecallOptions = true;
//            title = apppendText + "\n" + "????????????????????????????????????????????????????????????????????????????????????????????????";
//        }
        String Radio_text = "?????????";
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
                    Log.d(TAG, "radio button is ?????????");
                    clickedRadioButtonPosition = radioButtonArrayList.indexOf(buttonView);
                    CueRecallFragment_test.this.saveActionsOfRadioBox();
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
                Set<Map.Entry<String, String[]>> entries ;
                List<Map.Entry<String, String[]>> listEntries;

                if(choose.matches("??????.*???????????????")){
                    File f = null;
                    for(int i = 0; i < radioButtonArrayList.size() - 1; i++){
                        entries = AllOptions.entrySet();
                        listEntries = new ArrayList<Map.Entry<String, String[]>>(entries);
                        Log.d(TAG, "FileNameArrayList: " + listEntries.get(i).getValue()[2]);
                        RadioButton radioButton = radioButtonArrayList.get(i);
                        if(radioButton.isChecked()){
                            Log.d(TAG, "radio button text: " + radioButton.getText().toString());
                            f = new File(listEntries.get(i).getValue()[2]); // AllOption
                            Log.d(TAG, "AllOptions data: " + listEntries.get(i).getValue()[2]);
                            Log.d(TAG, "chosed image file path: " + f);
//                            6/17
//                            f = new File("file:///android_asset/esm/" + listEntries.get(i).getValue()[2]); // AllOption
//                            cueRecallImg = Uri.parse("file:///android_asset/esm/" + listEntries.get(i).getValue()[2]);
//                            Crop_Uri = cueRecallImg;
//                            Log.d(TAG, "cue recall uri: " + cueRecallImg.toString());
                        }
                    }
                    if(f != null){
//                        6/17
                        cueRecallImg = Uri.fromFile(f);
                        Crop_Uri = cueRecallImg;
                        Log.d(TAG, "cue recall uri: " + cueRecallImg.toString());
                    }
                }
                else{
                    cueRecallImg = null;
                }
//                nextOrFinishButton.setEnabled(false);
                if (currentPagePosition == ((QuestionActivity_test) mContext).getTotalQuestionsSize() || NoCue) {
                    /* Here, You go back from where you started OR If you want to go next Activity just change the Intent*/
                    CanFillEsm = false;
                    isFinish = "2";
                    startActivity(MainIntent);
                    mContext.finish();
                }
                else if(!choose.matches("??????.*???????????????")){
                    Log.d(TAG, "jump three page");
                    ((QuestionActivity_test) mContext).nextQuestion(3);
                }
                else {
                    ((QuestionActivity_test) mContext).nextQuestion(2);
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
        if (currentPagePosition == ((QuestionActivity_test) mContext).getTotalQuestionsSize())
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
        String Radio_text = "???" + content[1] + "???PTT?????????????????????:\n" + content[2];
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
                    CueRecallFragment_test.this.saveActionsOfRadioBox();
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
            String Radio_text = "???" + content[1] + "???" + content[3] + "???????????????????????????:\n?????????????????????....";
            RadioButton rb = new RadioButton(mContext);
            rb.setText(Radio_text);
            rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            rb.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
            rb.setPadding(10, 20, 10, 20);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 25;
            rb.setLayoutParams(params);

            webView = new MyWebView_test(mContext);

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
                    Log.d(TAG, "????????????: " + title);
                    rb.setText("???" + content[1] + "???" + content[3] + "???????????????????????????:\n" + title);
                    //WebTitle.setText(title);
                    // ????????????JS?????????????????? ????????????
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
                        CueRecallFragment_test.this.saveActionsOfRadioBox();
                    }
                }
            });
        }
    }

    private void getNotification(Long lastESM_time, Long nowESM_time){
        String FakeTime = "2021-06-30 15:35:05";
//        HashMap<String, String[]> NotificationOptionName = new HashMap<>();
        String title = "????????????";
        String text = "????????????5??????????????????6?????????3?????????????????????";
        String packagename = "?????????";
        String reason = "";
        AllOptions.put(FakeTime + text, new String[] {"Notification", packagename, text, FakeTime, title});
//                        FileNameArrayList.add("NA");
//                        Log.d(TAG, n_text_col + " " + getReadableTime(timestamp) + " " + title_col);
    }
    private void setNotificationContent(String[] ChoiceOption){
//        Log.d(TAG, "SetNotiContent");
        String[] content = ChoiceOption;

        String Radio_text = content[3] + " " + content[4] + "??????" + content[1] + "?????????????????????\n" + content[2];
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
                    CueRecallFragment_test.this.saveActionsOfRadioBox();
                }
            }
        });
    }

    private void getImages(String[] recall_kind, Long last_esm_time, Long now_esm_time) {
//        Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
        String last_esm = getReadableTime(last_esm_time);//2020-05-12 14:20:15
        String now_esm = getReadableTime(now_esm_time);
//        String ESMtime = pref.getString("ESMtime","NA");//2020-05-15 14:29:35
        Log.d(TAG, "Fixed date: 2021-06-30");
//        String ESMdate = ESMtime.split(" ")[0];

        String Fixed_date = "2021-06-30";
        ArrayList<String> FileList = new ArrayList<>();
        File directory = mContext.getFilesDir();
        Log.d(TAG, directory.toString());
//        File directory = new File(Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.example.accessibility_detect/");
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (directory.listFiles() != null) {
                for (int i = 0; i < files.length; i++) {
                    int file_len = files[i].getName().length();
                    Log.d(TAG, "FileName: " + files[i].getName());
                    if(files[i].getName().substring(file_len - 3, file_len).equals("jpg")) {
                        String filename = directory.toString() + "/" + files[i].getName();
                        Log.d(TAG, "FileName:" + filename);
                        String[] File_split = files[i].getName().split("-");
                        String time = File_split[0] + ":" + File_split[1] + ":" + File_split[2];
                        String Radio_text = "??????" + Fixed_date + " " + time + "???????????????";
                        AllOptions.put(Fixed_date + " " + time, new String[]{"Image", Radio_text, filename});
                    }
                }
            }
        }
        else{
            Log.d(TAG, "directory not exist");
        }
        // 6/17
        //        String[] images = null;
//        try{
//            images = mContext.getAssets().list("esm");
//            FileList = new ArrayList<String>(Arrays.asList(images));
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }

//        for (int i = 0; i < ID_Fields.length; i++) {
//            try {
//                resArray[i] = ID_Fields[i].getInt(null);
//                Log.d(TAG, getResources().getResourceName(resArray[i]));
//                String drawable_name = getResources().getResourceName(resArray[i]).split("/")[1];
//                if (drawable_name.contains("consumption")) {
//                    FileList.add(drawable_name);
//                }
//            } catch (IllegalAccessException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//        List fileList = Arrays.asList(FileList);
//        Collections.sort(fileList, new Comparator<File>() {
//            @Override
//            public int compare(File o1, File o2) {
//                if (o1.isDirectory() && o2.isFile())
//                    return -1;
//                if (o1.isFile() && o2.isDirectory())
//                    return 1;
//                return o1.getName().compareTo(o2.getName());
//            }
//        });
//        int l = "consumption".length();
//        for (int i = 0; i < FileList.size(); i++) { //????????????
////            Log.d(TAG, FileList.get(i));
//            String[] File_split = FileList.get(i).split("-");
//            String time = File_split[0] + ":" + File_split[1] + ":" + File_split[2];
//            String Radio_text = "??????" + Fixed_date + " " + time + "???????????????";
//            AllOptions.put(Fixed_date + " " + time, new String[]{"Image", Radio_text, FileList.get(i)});
//        }
    }
    private void setImageContent(String Radio_text, String img){
//        Log.d(TAG, img);
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

                    Intent intent = new Intent(mContext, FullScreenImage_test.class);

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
                        CueRecallFragment_test.this.saveActionsOfRadioBox();
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

        String year = String.valueOf(cal.get(Calendar.YEAR));                      //?????????
        String month = String.valueOf(cal.get(Calendar.MONTH) + 1);           //?????????????????????????????????0~11 ???+1
        String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));

        if(month.length() == 1) month = "0" + month;
        if(day.length() == 1) day = "0" + day;
        return year + "-" + month + "-" + day;
    }

    private void setTextWithSpan(String questionPosition, TextView tv)
    {
        int slashPosition = questionPosition.indexOf("???");

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
            // ???????????????????????????onResume()?????????
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