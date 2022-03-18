package com.example.accessibility_detect.questions.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.accessibility_detect.MainActivity;
import com.example.accessibility_detect.R;
import com.example.accessibility_detect.questions.QuestionActivity;
import com.example.accessibility_detect.questions.questionmodels.AnswerOptions;
import com.example.accessibility_detect.questions.questionmodels.QuestionsItem;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.model.DataRecord.QuestionDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.QuestionWithAnswersDataRecord;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;

import static com.warkiz.widget.SizeUtils.dp2px;
import static labelingStudy.nctu.minuku.config.Constants.multitask_following;
import static labelingStudy.nctu.minuku.config.Constants.multitasking;
import static labelingStudy.nctu.minuku.config.Constants.others;
import static labelingStudy.nctu.minuku.config.SharedVariables.CanFillEsm;
import static labelingStudy.nctu.minuku.config.SharedVariables.EnterQ1_first;
import static labelingStudy.nctu.minuku.config.SharedVariables.Q25Answer;
import static labelingStudy.nctu.minuku.config.SharedVariables.answerid;
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTime;
import static labelingStudy.nctu.minuku.config.SharedVariables.isFinish;

/**
 * This fragment provide the Checkbox/Multiple related Options/Choices.
 */
public class CheckBoxesFragment extends Fragment
{
    private String TAG = "CheckBoxesFragment";
    private final ArrayList<CheckBox> checkBoxArrayList = new ArrayList<>();
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
    private boolean isFirstLoad = true; // 是否第一次加载
    String appName="";
    String enterTime = "";
    int relatedId;
    private EditText editText_answer;
    String notiId = "";
    public CheckBoxesFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_check_boxes, container, false);

        db = appDatabase.getDatabase(getActivity());

        gson = new Gson();
        nextOrFinishButton = rootView.findViewById(R.id.nextOrFinishButton);
        //previousButton = rootView.findViewById(R.id.previousButton);
        questionCBTypeTextView = rootView.findViewById(R.id.questionCBTypeTextView);

        checkboxesLinearLayout = rootView.findViewById(R.id.checkboxesLinearLayout);

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
                if (currentPagePosition == ((QuestionActivity) mContext).getTotalQuestionsSize()) {
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
                else if(currentPagePosition == 1){
                    int ans = check_radio_answer("1");
                    if (ans == 1) {//否
                        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
                        if(userRecord != null) {
                            db.userDataRecordDao().updateCanFillESM(userRecord.get_id(), false);
                        }
                        CanFillEsm = false;
                        isFinish = "2";
                        startActivity(MainIntent);
                        mContext.finish();
                    }else {
                        nextOrFinishButton.setEnabled(false);
                        ((QuestionActivity) mContext).nextQuestion(2);
                    }
                }
                else if(currentPagePosition == 25) {
                    ((QuestionActivity) mContext).nextQuestion(2);
                }
                else {
                    ((QuestionActivity) mContext).nextQuestion(1);
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
        Log.d(TAG, "how many checked before " + atLeastOneChecked);
        for (int i = 0; i < checkBoxArrayList.size(); i++)
        {
            if(currentPagePosition >= 27 && (currentPagePosition - 27) % multitask_following == 0)
                Log.d(TAG, i + " checked: " + checkBoxArrayList.get(i).isChecked());
            if (i == clickedCheckBoxPosition)
            {
                if(currentPagePosition >= 27 && (currentPagePosition - 27) % multitask_following == 0)
                    Log.d(TAG, "click " + i);
                CheckBox checkBox = checkBoxArrayList.get(i);
                if (checkBox.isChecked())
                {
                    String cbPosition = String.valueOf(checkBoxArrayList.indexOf(checkBox));
                    if(checkBoxArrayList.indexOf(checkBox) == 4 && currentPagePosition >= 27 && (currentPagePosition - 27) % multitask_following == 0)
                        cbPosition = "5";
                    String[] data = new String[]{"1", questionId, cbPosition};
                    insertAnswerInDatabase(data);
                    if (currentPagePosition == 25) {
                        Log.d(TAG, "You click " + checkBox.getText().toString() + " to true");
                        Q25Answer.put(checkBox.getText().toString(), checkBox.getText().toString());
                    }
                    if(currentPagePosition >= 27 && (currentPagePosition - 27) % multitask_following == 0){
                        if(checkBox.getText().toString().equals("沒有使用其他媒體")){
                            for(int j = 0; j < checkBoxArrayList.size() - 1; j++){
                                if(checkBoxArrayList.get(j).isChecked()){
                                    atLeastOneChecked = atLeastOneChecked - 1;
                                    if (atLeastOneChecked <= 0)
                                        atLeastOneChecked = 0;
                                }
                                editText_answer.setText("");
                                checkBoxArrayList.get(j).setChecked(false);
                                String[] d = new String[]{"-1", questionId, String.valueOf(j)};
                                insertAnswerInDatabase(d);
                            }
                        }
                        else{
                            if(checkBoxArrayList.get(4).isChecked()){
                                atLeastOneChecked = atLeastOneChecked - 1;
                                if (atLeastOneChecked <= 0)
                                    atLeastOneChecked = 0;
                            }
                            checkBoxArrayList.get(4).setChecked(false);
                            String[] d = new String[]{"-1", questionId, String.valueOf(5)};
                            insertAnswerInDatabase(d);
                        }
                    }
                    atLeastOneChecked = atLeastOneChecked + 1;
                }
                else
                {
                    atLeastOneChecked = atLeastOneChecked - 1;
                    if (atLeastOneChecked <= 0) {
                        atLeastOneChecked = 0;
                    }
//                    if (!editText_answer.getText().equals("") && atLeastOneChecked == 0) {
//                        atLeastOneChecked = 1;
//                    }
                    String cbPosition = String.valueOf(checkBoxArrayList.indexOf(checkBox));
                    if(checkBoxArrayList.indexOf(checkBox) == 4 && currentPagePosition >= 27 && (currentPagePosition - 27) % multitask_following == 0)
                        cbPosition = "5";
                    String[] data = new String[]{"-1", questionId, cbPosition};
                    insertAnswerInDatabase(data);
                    if(currentPagePosition == 25) {
                        Log.d(TAG, "You click " + checkBox.getText().toString() + " to false");
                        Q25Answer.remove(checkBox.getText().toString());
                    }
                }
//                Log.d(TAG, "Q25 content: " + Q25Answer);
            }
        }
//        if(currentPagePosition >= 27 && (currentPagePosition - 27) % multitask_following == 0) {
//            for (int i = 0; i < checkBoxArrayList.size(); i++) {
//                CheckBox checkBox = checkBoxArrayList.get(i);
//                if (checkBox.isChecked()) {
//                    if (i != 4) {
//                        if(checkBoxArrayList.get(4).isChecked()) {
//                            atLeastOneChecked = atLeastOneChecked - 1;
//                            if (atLeastOneChecked <= 0)
//                                atLeastOneChecked = 0;
//                        }
//                        checkBoxArrayList.get(4).setChecked(false);
//                        String[] data = new String[]{"-1", questionId, "5"};
//                        insertAnswerInDatabase(data);
//                    }
//                    else{
//                        for(int j = 0; j < checkBoxArrayList.size() - 1; j++){
//                            if(checkBoxArrayList.get(j).isChecked()) {
//                                atLeastOneChecked = atLeastOneChecked - 1;
//                                if (atLeastOneChecked <= 0)
//                                    atLeastOneChecked = 0;
//                            }
//                            editText_answer.setText("");
//                            checkBoxArrayList.get(j).setChecked(false);
//                            String[] data = new String[]{"-1", questionId, String.valueOf(j)};
//                            insertAnswerInDatabase(data);
//                        }
//                    }
//                }
//            }
//        }
        Log.d(TAG, "how many checked after " + atLeastOneChecked);
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
                        return CheckBoxesFragment.this.insertingInDbForOther(data1);
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
                        return CheckBoxesFragment.this.insertingInDb(data1);
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
        //String selectState, String questionId, String optionId
        db.questionWithAnswersDao().updateQuestionWithDetectedTime(answerTime ,data[1], data[2]);
        //answerTime
        // insertFinalAnswer(data[1],data[2], data[0],answerTime);

        return "";
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        mContext = (FragmentActivity) getActivity();
        QuestionsItem checkBoxTypeQuestion = null;
        String extraInfo = "";
        if (getArguments() != null)
        {
            CSVHelper.storeToCSV("ESM_debug.csv", "getArguments() is not null");
//            checkBoxTypeQuestion = getArguments().getParcelable("question");
            checkBoxTypeQuestion = QuestionActivity.questionsItems.get(getArguments().getInt("page_position"));
            relatedId = getArguments().getInt("relatedIdF");
            enterTime = getArguments().getString("enterTimeForF");
            currentPagePosition = getArguments().getInt("page_position") + 1;
            notiId = getArguments().getString("notiId");
            questionId = String.valueOf(checkBoxTypeQuestion != null ? checkBoxTypeQuestion.getId() : 0);
        }
        else{
            CSVHelper.storeToCSV("ESM_debug.csv", "getArguments() is null");
        }
        Long nowESM_time = mContext.getSharedPreferences("test", Context.MODE_PRIVATE).getLong("Now_Esm_Time", 0);
        Long lastESM_time = 0L;
        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
        if(userRecord != null){
            lastESM_time = userRecord.getLastESMTime_for_Q1();
        }
//        Long lastESM_time = mContext.getSharedPreferences("test", Context.MODE_PRIVATE).getLong("Last_Esm_Time", 0);

        String apppendText = "";
        if(notiId.equals("1")){
            if(currentPagePosition == 1 || currentPagePosition == 3) {
                if (lastESM_time == 0) {
                    apppendText = getReadableTime(nowESM_time) + " 前可能接觸到新聞";
                } else {
                    apppendText = "從前一次回答問卷 (" + getReadableTime(lastESM_time) + " ) 至現在 (" + getReadableTime(nowESM_time) + ")可能接觸到新聞";
                }
            }
        }

        Log.d(TAG, "Q25 answer size = " + Q25Answer.size());
        if(currentPagePosition >= 27 && (currentPagePosition - 27) % multitask_following == 0) {
            int index = (currentPagePosition - 27) / multitask_following;
            if (index < Q25Answer.size()) {
                String option = (new ArrayList<String>(Q25Answer.values())).get(index);
                checkBoxTypeQuestion.setQuestionName("請問你在「" + option + "」時，同時還使用哪些其他媒體嗎?（可複選）");
            }
        }
        String title;
        if(currentPagePosition == 1 || currentPagePosition == 3){
            title = checkBoxTypeQuestion != null ? apppendText+'\n'+checkBoxTypeQuestion.getQuestionName():"";
        }else{
            title = checkBoxTypeQuestion != null ? checkBoxTypeQuestion.getQuestionName():"";
        }

        //setTextWithSpan(title,questionCBTypeTextView);
        questionCBTypeTextView.setText(title);
        //questionCBTypeTextView.setText(checkBoxTypeQuestion != null ? checkBoxTypeQuestion.getQuestionName()+'\n'+apppendText:"");
        /*Disable the button until any choice got selected*/
        nextOrFinishButton.setEnabled(false);

//        if(currentPagePosition == 6){
//            BackToQ6 = true;
//        }
        if(checkBoxTypeQuestion == null){
            CSVHelper.storeToCSV("ESM_debug.csv", "checkBoxTypeQuestion is null");
        }
        else{
            CSVHelper.storeToCSV("ESM_debug.csv", "checkBoxTypeQuestion is not null");
        }
        List<AnswerOptions> checkBoxChoices = Objects.requireNonNull(checkBoxTypeQuestion).getAnswerOptions();
        checkBoxArrayList.clear();
//        if(checkBoxTypeQuestion != null) {
//            TextView textView = getTextView();
//            textView.setText(checkBoxTypeQuestion.getAnswerOptionsGroup());
//            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
//            textView.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
//            textView.setPadding(10, 0, 10, 0);
//            checkboxesLinearLayout.addView(textView);
//        }
        int option_counter = 0;

        for (AnswerOptions choice : checkBoxChoices)
        {
            //     Boolean notShowAnswer = false;

//            if(appName.equals(map)){
//
//                if(choice.getAnswerId().equals("62")||choice.getAnswerId().equals("63")){
//                    notShowAnswer = true;
//                }
//            }else if(appName.equals(crowdsource)){
//                if(choice.getAnswerId().equals("56")||choice.getAnswerId().equals("57")||choice.getAnswerId().equals("58")||
//                        choice.getAnswerId().equals("59")||choice.getAnswerId().equals("60") ||choice.getAnswerId().equals("61")){
//                    notShowAnswer = true;
//                }
//            }
            if(choice.getName().equals(others)){
                editText_answer = new EditText(mContext);
                editText_answer.setHint("其他(請說明)");
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = 25;
                checkboxesLinearLayout.addView(editText_answer, params);
                editText_answer.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s.length() >= 1 && !s.toString().equals("")) {
                            nextOrFinishButton.setEnabled(true);
                        } else {
                            nextOrFinishButton.setEnabled(false);
                        }
                        if(editText_answer.getText().toString().equals("")){
                            String[] data = new String[]{"-1", questionId, editText_answer.getText().toString(), others};
                            if(currentPagePosition == 25) {
                                Log.d(TAG, "You click " + editText_answer.getText().toString() + " to true");
                                Q25Answer.remove(others);
                            }
                            insertChoiceInDatabaseForOther(data);
                        }
                        else {
                            String[] data = new String[]{"1", questionId, editText_answer.getText().toString(), others};
                            if(currentPagePosition == 25) {
                                Log.d(TAG, "You click " + editText_answer.getText().toString() + " to true");
                                Q25Answer.put(others, editText_answer.getText().toString());
                            }
                            insertChoiceInDatabaseForOther(data);

                            if(currentPagePosition >= 27 && (currentPagePosition - 27) % multitask_following == 0) {
                                if (checkBoxArrayList.get(4).isChecked()) {
                                    atLeastOneChecked = atLeastOneChecked - 1;
                                    if (atLeastOneChecked <= 0)
                                        atLeastOneChecked = 0;
                                }
                                checkBoxArrayList.get(4).setChecked(false);
                                String[] d = new String[]{"-1", questionId, "5"};
                                insertAnswerInDatabase(d);
                            }
                        }
//                        String[] data = new String[]{editText_answer.getText().toString(), questionId, String.valueOf(0)};
//                        insertAnswerInDatabase(data);

                    }
                });
            }
            else {
                if (questionId.equals("25")) {
                    if (option_counter == 0) {
                        TextView textView = getTextView();
                        textView.setText(multitasking[0]);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        textView.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
                        textView.setPadding(30, 40, 10, 10);
                        checkboxesLinearLayout.addView(textView);
                    } else if (option_counter == 3) {
                        TextView textView = getTextView();
                        textView.setText(multitasking[1]);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        textView.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
                        textView.setPadding(30, 40, 10, 10);
                        checkboxesLinearLayout.addView(textView);
                    } else if (option_counter == 8) {
                        TextView textView = getTextView();
                        textView.setText(multitasking[2]);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        textView.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
                        textView.setPadding(30, 40, 10, 10);
                        checkboxesLinearLayout.addView(textView);
                    } else if (option_counter == 12) {
                        TextView textView = getTextView();
                        textView.setText(multitasking[3]);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        textView.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
                        textView.setPadding(30, 40, 10, 10);
                        checkboxesLinearLayout.addView(textView);
                    } else if (option_counter == 14) {
                        TextView textView = getTextView();
                        textView.setText(multitasking[4]);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        textView.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
                        textView.setPadding(30, 40, 10, 10);
                        checkboxesLinearLayout.addView(textView);
                    } else if (option_counter == 17) {
                        TextView textView = getTextView();
                        textView.setText(multitasking[5]);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                        textView.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
                        textView.setPadding(30, 40, 10, 10);
                        checkboxesLinearLayout.addView(textView);
                    }
                    option_counter++;
                }
                CheckBox checkBox = new CheckBox(mContext);
                checkBox.setText(choice.getName());
                checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                checkBox.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
                checkBox.setPadding(10, 40, 10, 40);  //10 40 10 40 //0, 20, 0, 20
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = 25;

                View view = new View(mContext);
                view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.divider));
                view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                checkboxesLinearLayout.addView(checkBox, params);
                checkboxesLinearLayout.addView(view);
                checkBoxArrayList.add(checkBox);

//                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        // TODO Auto-generated method stub
////                        CheckBox buttonView = (CheckBox) buttonView;
//                        clickedCheckBoxPosition = checkBoxArrayList.indexOf(buttonView);
//                        CheckBoxesFragment.this.saveActionsOfCheckBox();
//                    }
//                });

                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view1) {
                        Log.d(TAG, "Clicked");
                        CheckBox buttonView = (CheckBox) view1;
                        clickedCheckBoxPosition = checkBoxArrayList.indexOf(buttonView);
                        CheckBoxesFragment.this.saveActionsOfCheckBox();
                    }
                });

            }

            /*As user comes back for any modification in choices, "setUserVisibleHint" fragment lifecycle method get called, and "checkBox.setChecked(true)"
             * statement will be executed as many times as previously user checked.
             * On that, this below block will get executed automatically,
             * where this method(saveActionsOfCheckBox()) also executed which is unnecessary.
             * That's why we follow "setOnClickListener" instead of "setOnCheckedChangeListener".*/

            /*checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    clickedCheckBoxPosition = checkBoxArrayList.indexOf(buttonView);
                    saveActionsOfCheckBox();
                }
            });*/
        }
        /** edit text **/
//            catch (Exception e){
//                e.printStackTrace();
//            }
        Log.d(TAG, "First enter Q1: " + EnterQ1_first);
        if(currentPagePosition == 1 && EnterQ1_first) {
            preClick(lastESM_time);
            EnterQ1_first = false;
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

    private void preClick(long lastESM_time){
        HashSet<String> UsedApp = new HashSet<String>();
        Cursor transCursor = db.SessionDataRecordDao().getAll();
        int rows = transCursor.getCount();

        if(rows!=0) {
            transCursor.moveToFirst();
            for (int i = 0; i < rows; i++) {
                long start_timestamp = transCursor.getLong(1);
                Log.d(TAG, start_timestamp + " " + lastESM_time);
                if(start_timestamp > lastESM_time){
                    UsedApp.add(transCursor.getString(7));
                    Log.d(TAG, "Add " + transCursor.getString(7));
                }
                transCursor.moveToNext();
            }
        }

        transCursor = db.notificationDataRecordDao().getAll();
        rows = transCursor.getCount();

        if(rows!=0) {
            transCursor.moveToFirst();
            for (int i = 0; i < rows; i++) {
                long start_timestamp = transCursor.getLong(1);
                Log.d(TAG, start_timestamp + " " + lastESM_time);
                if(start_timestamp > lastESM_time){
                    UsedApp.add("Notification");
                    Log.d(TAG, "Add Notification");
                }
                transCursor.moveToNext();
            }
        }

        for(int i = 0; i < UsedApp.size(); i++) {
            Log.d(TAG, UsedApp.toArray()[i].toString());
        }
        {
            for (int i = 0; i < checkBoxArrayList.size(); i++) {
                CheckBox checkBox = checkBoxArrayList.get(i);
                String AppMapping = "";
                if (checkBox.getText().equals("Facebook")) {
                    AppMapping = "Facebook";
                } else if (checkBox.getText().equals("FB Messenger")) {
                    AppMapping = "Messenger";
                } else if (checkBox.getText().equals("LINE 聊天室")) {
                    AppMapping = "LineChat";
                } else if (checkBox.getText().equals("LINE Today")) {
                    AppMapping = "LineToday";
                } else if (checkBox.getText().equals("Instagram")) {
                    AppMapping = "Instagram";
                } else if (checkBox.getText().equals("新聞App")) {
                    AppMapping = "NewsApp";
                } else if (checkBox.getText().equals("推播通知")) {
                    AppMapping = "Notification";
                } else if (checkBox.getText().equals("Youtube")) {
                    AppMapping = "Youtube";
                } else if (checkBox.getText().equals("PTT")) {
                    AppMapping = "PTT";
                } else if (checkBox.getText().equals("Google新聞/Google Now")) {
                    AppMapping = "googleNews";
                } else if (checkBox.getText().equals("網頁瀏覽器")) {
                    AppMapping = "Chrome";
                }
                Log.d(TAG, checkBox.getText() + " " + AppMapping);

                if (UsedApp.contains(AppMapping)) {
                    checkBox.setChecked(true);
                    clickedCheckBoxPosition = i;
                    CheckBoxesFragment.this.saveActionsOfCheckBox();
                    Log.d(TAG, checkBox.getText() + " check set true " + clickedCheckBoxPosition);
                }
            }
        }
//            catch (Exception e){
//                e.printStackTrace();
//            }
    }
    private TextView getTextView() {
        TextView textView = new TextView(getContext());
        int padding = dp2px(getContext(), 10);
        textView.setPadding(padding, padding, padding, 0);
        return textView;
    }
    private void setTextWithSpan(String questionPosition, TextView tv)
    {
        int slashPosition = questionPosition.indexOf("您");

        Spannable spanText = new SpannableString(questionPosition);
        spanText.setSpan(new RelativeSizeSpan(0.6f), slashPosition, questionPosition.length(), 0);
        tv.setText(spanText);
    }

    private String InsertNewQuestions(String ans_choice, int question_id){
        String following_questions = "{'status':true,'message':'The data was fetched successfully','data':{" +
                "'questions':[" +

                "{'id':" + question_id + "," + "'question_type_id':3," + "'question_type_name':'CheckBox'," +
                "'question_name':'請問你在「" + ans_choice + "」時，同時還使用哪些其他媒體嗎?（可複選）'," +
                "'question_item':[{'answer_id':'" + (++answerid) + "','name':'平板'}," +
                "{'answer_id':'" + (++answerid) + "','name':'筆記型或桌上型電腦'}," +
                "{'answer_id':'" + (++answerid) + "','name':'電視'}," +
                "{'answer_id':'" + (++answerid) + "','name':'電視遊樂器'}," +
                "{'answer_id':'" + (++answerid) + "','name':'沒有使用其他媒體'}]}," +

                "{'id':" + question_id + "," + "'question_type_id':1," + "'question_type_name':'Radio'," +
                "'question_name':'請問在當時，「" + ans_choice + "」與「用手機看新聞」哪個是主要活動?'," +
                "'question_item':[{'answer_id':'" + (++answerid) + "','name':'" + ans_choice + "'}," +
                "{'answer_id':'" + (++answerid) + "','name':'用手機看新聞'}]}," +

                "{'id':" + question_id + "," + "'question_type_id':1," + "'question_type_name':'Radio'," +
                "'question_name':'請問在當時，「" + ans_choice + "」與「用手機看新聞」是為了完成同一個目標嗎?'," +
                "'question_item':[{'answer_id':'" + (++answerid) + "','name':'是'}," +
                "{'answer_id':'" + (++answerid) + "','name':'否'}" +
                "]}";
        return following_questions;
    }

    private void preparingQuestionInsertionInDb(List<QuestionsItem> questionsItems)
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
    private String insertingQuestionInDb(List<QuestionDataRecord> questionEntities)
    {
//        db.questionDao().deleteAllQuestions();
        db.questionDao().insertAllQuestions(questionEntities);
        return "";
    }
    private void preparingInsertionInDb(List<QuestionsItem> questionsItems, int relatedId)
    {
        ArrayList<QuestionWithAnswersDataRecord> questionWithChoicesEntities = new ArrayList<>();

        for (int i = 0; i < questionsItems.size(); i++)
        {
            List<AnswerOptions> answerOptions = questionsItems.get(i).getAnswerOptions();

            for (int j = 0; j < answerOptions.size(); j++)
            {
                QuestionWithAnswersDataRecord questionWithAnswersDataRecord = new QuestionWithAnswersDataRecord();
                questionWithAnswersDataRecord.setQuestionId(String.valueOf(questionsItems.get(i).getId()));
                questionWithAnswersDataRecord.setAnswerChoice(answerOptions.get(j).getName());
                questionWithAnswersDataRecord.setAnswerChoicePosition(String.valueOf(j));
                questionWithAnswersDataRecord.setAnswerChoiceId(answerOptions.get(j).getAnswerId());
                questionWithAnswersDataRecord.setAnswerChoiceState("0");
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
//        db.questionWithAnswersDao().deleteAllChoicesOfQuestion();
        db.questionWithAnswersDao().insertAllChoicesOfQuestion(questionWithChoicesEntities);
        return "";
    }
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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFirstLoad = true;
    }
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
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
                                    if(editText_answer != null) {
                                        editText_answer.setText(qState);
                                    }
                                    atLeastOneChecked = atLeastOneChecked + 1;
                                }
                            }
                        });
            }
        }
    }
}