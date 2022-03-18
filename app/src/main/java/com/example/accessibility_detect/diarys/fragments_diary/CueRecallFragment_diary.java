package com.example.accessibility_detect.diarys.fragments_diary;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.example.accessibility_detect.MainActivity;
import com.example.accessibility_detect.R;
import com.example.accessibility_detect.diarys.QuestionActivity_diary;
import com.example.accessibility_detect.diarys.questionmodels_diary.AnswerOptions_diary;
import com.example.accessibility_detect.diarys.questionmodels_diary.QuestionsItem_diary;
import com.example.accessibility_detect.questions.FullScreenImage;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;

import static com.warkiz.widget.SizeUtils.dp2px;
import static labelingStudy.nctu.minuku.config.SharedVariables.CanFillDiary;
import static labelingStudy.nctu.minuku.config.SharedVariables.NoEsm;
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTime;
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTimeLong;
import static labelingStudy.nctu.minuku.config.SharedVariables.isDFinish;

/**
 * This fragment provide the Checkbox/Multiple related Options/Choices.
 */
public class CueRecallFragment_diary extends Fragment
{
    private String TAG = "CheckBoxesFragment";
    private final ArrayList<CheckBox> checkBoxArrayList = new ArrayList<>();
    private TextView questionPositionTV;
    private int atLeastOneChecked = 0;
    private FragmentActivity mContext;
    private Button nextOrFinishButton;
    //private Button previousButton;
    private TextView questionCBTypeTextView;
    private LinearLayout checkboxesLinearLayout;
    private TextView questionCBGroupTextView;
    private appDatabase db;
    private Gson gson;
    private String questionId = "";
    private int currentPagePosition = 0;
    private int clickedCheckBoxPosition = 0;
    private String qState = "0";
    private String choose;
    private boolean isFirstLoad = true; // 是否第一次加载
    String appName="";
    String enterTime = "";
    int relatedId;
    private EditText editText_answer;
    String notiId = "";
    public CueRecallFragment_diary()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.diary_cue_recall, container, false);

        db = appDatabase.getDatabase(getActivity());

        gson = new Gson();
        nextOrFinishButton = rootView.findViewById(R.id.diary_nextOrFinishButton);
        //previousButton = rootView.findViewById(R.id.previousButton);
        questionCBTypeTextView = rootView.findViewById(R.id.diary_questionCRTypeTextView);

        checkboxesLinearLayout = rootView.findViewById(R.id.diary_cuerecallLinearLayout);

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
//                nextOrFinishButton.setEnabled(false);
                if (currentPagePosition == ((QuestionActivity_diary) mContext).getTotalQuestionsSize()) {
                    /* Here, You go back from where you started OR If you want to go next Activity just change the Intent*/
                    CanFillDiary = false;
                    UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
                    if(userRecord != null) {
                        db.userDataRecordDao().updateCanFillDiary(userRecord.get_id(), false);
                    }
                    isDFinish = "2";
                    startActivity(MainIntent);
                    mContext.finish();
                }
                else if(NoEsm){
                    ((QuestionActivity_diary) mContext).nextQuestion(3);
                }
                else {
                    ((QuestionActivity_diary) mContext).nextQuestion(1);
                }
            }
        });
        //previousButton.setOnClickListener(view -> mContext.onBackPressed());

        return rootView;
    }
    public Integer check_radio_answer(String questionId){
        String none,second,third,fourth,other;
        none = db.questionWithAnswersDao().isChecked(questionId,"11");
        second = db.questionWithAnswersDao().isChecked(questionId,"1");
        third = db.questionWithAnswersDao().isChecked(questionId,"2");
        fourth = db.questionWithAnswersDao().isChecked(questionId,"3");
        other = db.questionWithAnswersDao().isChecked(questionId,"4");


        if(none!=null) {
            Log.d("qskip"," radioBox first :  "+ none);
            if (none.equals("1")) {
                return 1;   //都沒有
            }
        }
        return 0;
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

    /*This method get called only when the fragment get visible, and here states of checkbox(s) retained*/
//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser)
//    {
//        super.setUserVisibleHint(isVisibleToUser);
//
//        atLeastOneChecked = 0;
//
//        if (isVisibleToUser)
//        {
//            for (int i = 0; i < checkBoxArrayList.size(); i++)
//            {
//                CheckBox checkBox = checkBoxArrayList.get(i);
//                String cbPosition = String.valueOf(i);
//                String[] data = new String[]{questionId, cbPosition};
//                Log.d(TAG,"setUserVisibleHint i = "+ i + " data = "+ data.toString());
//                Observable.just(data)
//                        .map(this::getTheStateOfCheckBox)
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
//
//                                Log.d(TAG,"setUserVisibleHint: qState = "+qState);
//                                if (qState.equals("1"))
//                                {
//                                    checkBox.setChecked(true);
//                                    atLeastOneChecked = atLeastOneChecked + 1;
//
//                                    if (!nextOrFinishButton.isEnabled())
//                                    {
//                                        nextOrFinishButton.setEnabled(true);
//                                    }
//                                } else if(qState.equals("0"))
//                                {
//                                    checkBox.setChecked(false);
//                                }else{
//                                    editText_answer.setText(qState);
//                                    atLeastOneChecked = atLeastOneChecked + 1;
//                                }
//                            }
//                        });
//            }
//        }
//    }


    private String getTheStateOfCheckBox(String[] data)
    {
        return db.questionWithAnswersDao().isChecked(data[0], data[1]);
    }

    private void saveActionsOfCheckBox()
    {
        choose = "";
        for (int i = 0; i < checkBoxArrayList.size(); i++) {
            CheckBox checkBox = checkBoxArrayList.get(i);
            if (i == clickedCheckBoxPosition)
            {
                if (checkBox.isChecked()) {
                    atLeastOneChecked = atLeastOneChecked + 1;

                    String cbPosition = String.valueOf(checkBoxArrayList.indexOf(checkBox));
                }
                else {
                    atLeastOneChecked = atLeastOneChecked - 1;
                    if (atLeastOneChecked <= 0)
                        atLeastOneChecked = 0;

                    String cbPosition = String.valueOf(checkBoxArrayList.indexOf(checkBox));
                }
            }
            if (checkBox.isChecked()) {
//                    String[] data = new String[]{"1", questionId, cbPosition};
                if (currentPagePosition == 2) {
                    Log.d(TAG, "You click " + checkBox.getText().toString() + " to true");
                    choose = choose + checkBox.getText().toString() + " || ";
                    String[] data = new String[]{"1", questionId, choose};
                    insertAnswerInDatabase(data);
                }
            }
            {
//                    String[] data = new String[]{"0", questionId, cbPosition};
//                    insertAnswerInDatabase(data);
            }
        }
        if (atLeastOneChecked != 0)
        {
            nextOrFinishButton.setEnabled(true);
        } else
        {
            nextOrFinishButton.setEnabled(false);
        }
    }

    private void insertChoiceInDatabaseForOther(String[] data)
    {
        Observable.just(data)
                .map(new Function<String[], Object>() {
                    @Override
                    public Object apply(String[] data1) throws Exception {
                        return CueRecallFragment_diary.this.insertingInDbForOther(data1);
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

    private void insertAnswerInDatabase(String[] data)
    {
        Observable.just(data)
                .map(new Function<String[], Object>() {
                    @Override
                    public Object apply(String[] data1) throws Exception {
                        return CueRecallFragment_diary.this.insertingInDb(data1);
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
//        String answerTime = getReadableTime(new Date().getTime());
//        db.questionWithAnswersDao().updateQuestionWithChoice(data[0], data[1], data[2]);
//        Log.d("qskip"," insertingInDb : selectionState : "+data[0] +"; questionId : "+data[1]+"; optionId : "+ data[2]);
//        //String selectState, String questionId, String optionId
//        db.questionWithAnswersDao().updateQuestionWithDetectedTime(answerTime ,data[1], data[2]);
//        //answerTime
//        // insertFinalAnswer(data[1],data[2], data[0],answerTime);

        return "";
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mContext = (FragmentActivity) getActivity();
        QuestionsItem_diary checkBoxTypeQuestion = null;
        String extraInfo = "";
        if (getArguments() != null)
        {
//            checkBoxTypeQuestion = getArguments().getParcelable("question");
            checkBoxTypeQuestion = QuestionActivity_diary.questionsItems.get(getArguments().getInt("page_position"));
            relatedId = getArguments().getInt("relatedIdF");
            enterTime = getArguments().getString("enterTimeForF");
            questionId = String.valueOf(checkBoxTypeQuestion != null ? checkBoxTypeQuestion.getId() : 0);
            currentPagePosition = getArguments().getInt("page_position") + 1;
            notiId = getArguments().getString("notiId");
        }
        String apppendText = "";
//        if(notiId.equals("1")){
//            if(lastESM_time == 0) {
//                apppendText = "現在 (" + getReadableTime(nowESM_time) + ")之前可能接觸到新聞";
//            }
//            else{
//                apppendText = "從前一次回答問卷 (" + getReadableTime(lastESM_time) + " ) 至現在 (" + getReadableTime(nowESM_time) + ")可能接觸到新聞";
//            }
//        }
        String title = checkBoxTypeQuestion != null ? checkBoxTypeQuestion.getQuestionName():"";
        //setTextWithSpan(title,questionCBTypeTextView);
        /*Disable the button until any choice got selected*/
//        nextOrFinishButton.setEnabled(false);

        List<AnswerOptions_diary> checkBoxChoices = Objects.requireNonNull(checkBoxTypeQuestion).getAnswerOptions();
        checkBoxArrayList.clear();
//        if(checkBoxTypeQuestion != null) {
//            TextView textView = getTextView();
//            textView.setText(checkBoxTypeQuestion.getAnswerOptionsGroup());
//            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
//            textView.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
//            textView.setPadding(10, 0, 10, 0);
//            checkboxesLinearLayout.addView(textView);
//        }

        Cursor transCursor = db.finalAnswerDao().getDataWithOptionName("Cue Recall");
        int rows = transCursor.getCount();
        if(rows!=0) {
            transCursor.moveToFirst();
            for (int i = 0; i < rows; i++) {
                String optionpos = transCursor.getString(6);
                Long createTime = transCursor.getLong(9);
                Long now = getReadableTimeLong(System.currentTimeMillis());
                if(createTime/100 == now/100) { //同一天
                    Log.d(TAG, "ESM option: " + optionpos);
                    if (optionpos.contains("的手機畫面")) {//圖片
                        String filename = getFileName(optionpos);
                        Log.d(TAG, "File name: " + filename);
                        if (!filename.equals("")) {
                            CheckBox checkBox = new CheckBox(mContext);
                            checkBox.setText(optionpos);
                            checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                            checkBox.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
                            checkBox.setPadding(10, 40, 10, 40);  //10 40 10 40 //0, 20, 0, 20
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.leftMargin = 25;
                            checkBox.setLayoutParams(params);

                            ImageView iv = new ImageView(mContext);
                            params = new LinearLayout.LayoutParams(
                                    300, 400);
                            params.leftMargin = 100;
                            params.bottomMargin = 30;
                            iv.setLayoutParams(params);
                            Glide.with(this)
                                    .asBitmap()
                                    .load(filename)
                                    .into(iv);
                            iv.setClickable(true);
                            iv.setFocusable(true);

                            iv.setOnClickListener(new View.OnClickListener() {
                                //@Override
                                public void onClick(View v) {
                                    Log.v(TAG, " click");

                                    Intent intent = new Intent(mContext, FullScreenImage.class);

                                    Bundle extras = new Bundle();
                                    extras.putString("imagebitmap", filename);
                                    intent.putExtras(extras);
                                    startActivity(intent);
                                }
                            });

                            View view = new View(mContext);
                            view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.divider));
                            view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                            checkboxesLinearLayout.addView(checkBox);
                            checkboxesLinearLayout.addView(iv);
                            checkboxesLinearLayout.addView(view);
                            checkBoxArrayList.add(checkBox);

                            checkBox.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view1) {
                                    CheckBox buttonView = (CheckBox) view1;
                                    clickedCheckBoxPosition = checkBoxArrayList.indexOf(buttonView);
                                    CueRecallFragment_diary.this.saveActionsOfCheckBox();
                                }
                            });
                        } else {
                            CheckBox checkBox = new CheckBox(mContext);
                            checkBox.setText(optionpos + "\n載入圖片失敗");
                            checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                            checkBox.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
                            checkBox.setPadding(10, 40, 10, 40);  //10 40 10 40 //0, 20, 0, 20
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.leftMargin = 25;
                            checkBox.setLayoutParams(params);

                            View view = new View(mContext);
                            view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.divider));
                            view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                            checkboxesLinearLayout.addView(checkBox);
                            checkboxesLinearLayout.addView(view);
                            checkBoxArrayList.add(checkBox);

                            checkBox.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view1) {
                                    CheckBox buttonView = (CheckBox) view1;
                                    clickedCheckBoxPosition = checkBoxArrayList.indexOf(buttonView);
                                    CueRecallFragment_diary.this.saveActionsOfCheckBox();
                                }
                            });
                        }
                    }
                    else {//文字
                        CheckBox checkBox = new CheckBox(mContext);
                        checkBox.setText(optionpos);
                        checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        checkBox.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
                        checkBox.setPadding(10, 40, 10, 40);  //10 40 10 40 //0, 20, 0, 20
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        params.leftMargin = 25;
                        checkBox.setLayoutParams(params);

                        View view = new View(mContext);
                        view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.divider));
                        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                        checkboxesLinearLayout.addView(checkBox);
                        checkboxesLinearLayout.addView(view);
                        checkBoxArrayList.add(checkBox);

                        checkBox.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view1) {
                                CheckBox buttonView = (CheckBox) view1;
                                clickedCheckBoxPosition = checkBoxArrayList.indexOf(buttonView);
                                CueRecallFragment_diary.this.saveActionsOfCheckBox();
                            }
                        });
                    }
                }
                transCursor.moveToNext();
            }

        }
        else{
            title = "沒有擷取到問卷內容，請按下一頁繼續";
            NoEsm = true;
        }
        questionCBTypeTextView.setText(title);
        transCursor.close();
//        for (AnswerOptions_diary choice : checkBoxChoices)
//        {
//            //     Boolean notShowAnswer = false;
//
////            if(appName.equals(map)){
////
////                if(choice.getAnswerId().equals("62")||choice.getAnswerId().equals("63")){
////                    notShowAnswer = true;
////                }
////            }else if(appName.equals(crowdsource)){
////                if(choice.getAnswerId().equals("56")||choice.getAnswerId().equals("57")||choice.getAnswerId().equals("58")||
////                        choice.getAnswerId().equals("59")||choice.getAnswerId().equals("60") ||choice.getAnswerId().equals("61")){
////                    notShowAnswer = true;
////                }
////            }
//            if(choice.getName().equals(others)){
//                editText_answer = new EditText(mContext);
//                editText_answer.setHint(others);
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                params.leftMargin = 25;
//                checkboxesLinearLayout.addView(editText_answer, params);
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
//                        if(editText_answer.getText().toString().equals("")){
//                            String[] data = new String[]{"0", questionId, editText_answer.getText().toString(), others};
//                            insertChoiceInDatabaseForOther(data);
//                        }
//                        else {
//                            String[] data = new String[]{"1", questionId, editText_answer.getText().toString(), others};
//                            insertChoiceInDatabaseForOther(data);
//                        }
////                        String[] data = new String[]{editText_answer.getText().toString(), questionId, String.valueOf(0)};
////                        insertAnswerInDatabase(data);
//
//                    }
//                });
//            }
//            else{
//                CheckBox checkBox = new CheckBox(mContext);
//                checkBox.setText(choice.getName());
//                checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
//                checkBox.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
//                checkBox.setPadding(10 ,40, 10, 40);  //10 40 10 40 //0, 20, 0, 20
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                params.leftMargin = 25;
//
//                View view = new View(mContext);
//                view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.divider));
//                view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
//                checkboxesLinearLayout.addView(checkBox, params);
//                checkboxesLinearLayout.addView(view);
//                checkBoxArrayList.add(checkBox);
//
//                checkBox.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view1) {
//                        CheckBox buttonView = (CheckBox) view1;
//                        clickedCheckBoxPosition = checkBoxArrayList.indexOf(buttonView);
//                        CueRecallFragment_diary.this.saveActionsOfCheckBox();
//                    }
//                });
//
//            }
//
//
//            /*As user comes back for any modification in choices, "setUserVisibleHint" fragment lifecycle method get called, and "checkBox.setChecked(true)"
//             * statement will be executed as many times as previously user checked.
//             * On that, this below block will get executed automatically,
//             * where this method(saveActionsOfCheckBox()) also executed which is unnecessary.
//             * That's why we follow "setOnClickListener" instead of "setOnCheckedChangeListener".*/
//
//            /*checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
//            {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
//                {
//                    clickedCheckBoxPosition = checkBoxArrayList.indexOf(buttonView);
//                    saveActionsOfCheckBox();
//                }
//            });*/
//        }
        /** edit text **/




        /* If the current question is last in the questionnaire then
        the "Next" button will change into "Finish" button*/
        if (currentPagePosition == ((QuestionActivity_diary) mContext).getTotalQuestionsSize())
        {
            nextOrFinishButton.setText(R.string.finish);
        } else
        {
            nextOrFinishButton.setText(R.string.next);
        }
    }
    private TextView getTextView() {
        TextView textView = new TextView(getContext());
        int padding = dp2px(getContext(), 10);
        textView.setPadding(padding, padding, padding, 0);
        return textView;
    }
    private void setTextWithSpan(String questionPosition, TextView tv)
    {
        int slashPosition = questionPosition.indexOf("/");

        Spannable spanText = new SpannableString(questionPosition);
        spanText.setSpan(new RelativeSizeSpan(0.7f), slashPosition, questionPosition.length(), 0);
        questionPositionTV.setText(spanText);
    }

    /*First, clear the table, if any previous data saved in it. Otherwise, we get repeated data.*/
    //    public void insertFinalAnswer(String questionId,String optionId,String selectState,String answerTime){
//            //第幾題、第幾個選項、第幾個解答、回答時間
//        Integer ansId = MapAnswerPositiontoId(Integer.parseInt(questionId),Integer.parseInt(optionId));
////        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        FinalAnswerDataRecord finalAnswerDataRecord = new FinalAnswerDataRecord();
//        finalAnswerDataRecord.setAnswerChoice(optionId);
//        finalAnswerDataRecord.setAnswerChoiceState(selectState);
//        finalAnswerDataRecord.setanswerId(String.valueOf(ansId));
//        finalAnswerDataRecord.setdetectedTime(answerTime);
//        finalAnswerDataRecord.setQuestionId(questionId);
//        finalAnswerDataRecord.setsyncStatus(0);
//        finalAnswerDataRecord.setRelatedId(relatedId);
//        finalAnswerDataRecord.setcreationIme(new Date().getTime());
//        db.finalAnswerDao().insertAll(finalAnswerDataRecord);
//
//    }
//    public void updateFinalAnswer(){
//
//        Cursor transCursor = db.questionWithAnswersDao().getAllCursor();
//        List<FinalAnswerDataRecord> finalAnswerDataRecords = new ArrayList<FinalAnswerDataRecord>();
//        SharedPreferences pref =getActivity().getSharedPreferences("edu.nctu.minuku", MODE_PRIVATE);
//        String startAnswerTimeLong = pref.getString("startAnswerTimeLong","");
//        String finishAnswerTime = pref.getString("finishAnswerTime","");
//
//        int relatedId = pref.getInt("relatedIdForQM",-1);
//        int rows = transCursor.getCount();
//        if(rows!=0) {
//            FinalAnswerDataRecord finalAnswerDataRecord = new FinalAnswerDataRecord();
//
//            transCursor.moveToFirst();
//            for (int i = 0; i < rows; i++) {
//                String detectedTime = transCursor.getString(2);
//                String questionId = transCursor.getString(3);
//                String answerChoice = transCursor.getString(4);
//                String answerChoiceState = transCursor.getString(7);
//                if(answerChoice!="0") {
//                    finalAnswerDataRecord.setAnswerChoice(answerChoice);
//                    finalAnswerDataRecord.setAnswerChoiceState(answerChoiceState);
//                    finalAnswerDataRecord.setdetectedTime(detectedTime);
//                    finalAnswerDataRecord.setQuestionId(questionId);
//                    finalAnswerDataRecord.setStartAnswerTime(startAnswerTimeLong);
//                    finalAnswerDataRecord.setfinishAnswerTime(finishAnswerTime);
//                    finalAnswerDataRecord.setRelatedId(relatedId);
//                    finalAnswerDataRecord.setcreationIme(new Date().getTime());
//                }
//                transCursor.moveToNext();
//            }
//        }
//        db.finalAnswerDao().insertAll(finalAnswerDataRecords);
//    }
    private String getFileName(String Option_String){
        String result = "";
        String[] DateParser = Option_String.split(" ");
        Log.d(TAG, "DataParser: " + DateParser[0] + " " + DateParser[1]);
        String FileDate = DateParser[0].substring(DateParser[0].indexOf("於") + 1);//2020-06-01
        String FileTime = DateParser[1].substring(0, DateParser[1].indexOf("的")).replace(":","-");//21-31-27
        File imgFile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "News_Consumption/" + FileDate);
        if (imgFile.exists()) {
            File[] files = imgFile.listFiles();
            if (imgFile.listFiles() != null) {
                for (int i = 0; i < files.length; i++) {
                    Log.d(TAG, "FileName:" + files[i].getName());
                    try {
                        if (!files[i].getName().split("\\.")[1].equals("csv")) {
                            String FileToCompare = files[i].getName().split("\\.")[0];
                            if(FileToCompare.equals(FileTime)){
                                result = imgFile.toString() + "/" + files[i].getName();
                                break;
                            }
                        }
                    }catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFirstLoad = true;
    }
    public void onResume() {
        super.onResume();
        atLeastOneChecked = 0;
        if (isFirstLoad) {
            for (int i = 0; i < checkBoxArrayList.size(); i++)
            {
                CheckBox checkBox = checkBoxArrayList.get(i);
                String cbPosition = String.valueOf(i);
                String[] data = new String[]{questionId, cbPosition};
                Log.d(TAG,"setUserVisibleHint i = "+ i + " data = "+ data.toString());
                Observable.just(data)
                        .map(this::getTheStateOfCheckBox)
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
                                    checkBox.setChecked(true);
                                    atLeastOneChecked = atLeastOneChecked + 1;

                                    if (!nextOrFinishButton.isEnabled())
                                    {
                                        nextOrFinishButton.setEnabled(true);
                                    }
                                } else if(qState.equals("-1"))
                                {
                                    checkBox.setChecked(false);
                                }else{
//                                    checkBox.setChecked(false);
                                    atLeastOneChecked = atLeastOneChecked + 1;
                                }
                            }
                        });
            }
        }
    }
}