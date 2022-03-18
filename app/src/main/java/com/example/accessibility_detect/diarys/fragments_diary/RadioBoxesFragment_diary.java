package com.example.accessibility_detect.diarys.fragments_diary;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.accessibility_detect.MainActivity;
import com.example.accessibility_detect.R;
import com.example.accessibility_detect.diarys.QuestionActivity_diary;
import com.example.accessibility_detect.diarys.questionmodels_diary.AnswerOptions_diary;
import com.example.accessibility_detect.diarys.questionmodels_diary.QuestionsItem_diary;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;

import static labelingStudy.nctu.minuku.config.Constants.others;
import static labelingStudy.nctu.minuku.config.SharedVariables.CanFillDiary;
import static labelingStudy.nctu.minuku.config.SharedVariables.D15Answer;
import static labelingStudy.nctu.minuku.config.SharedVariables.D15_number;
import static labelingStudy.nctu.minuku.config.SharedVariables.D32_Answer;
import static labelingStudy.nctu.minuku.config.SharedVariables.D11_Answer;
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTime;
import static labelingStudy.nctu.minuku.config.SharedVariables.isDFinish;

/**
 * This fragment provide the RadioButton/Single Options.
 */
public class RadioBoxesFragment_diary extends Fragment
{
    private final ArrayList<RadioButton> radioButtonArrayList = new ArrayList<>();
    private boolean isFirstLoad = true; // 是否第一次加载
    private boolean screenVisible = false;
    private static QuestionsItem_diary radioButtonTypeQuestion;
    private FragmentActivity mContext;
    private Button nextOrFinishButton;
    //private Button previousButton;
    private TextView questionRBTypeTextView;
    private RadioGroup radioGroupForChoices;
    private boolean atLeastOneChecked = false;
    appDatabase db;
    private String questionId = "";
    private int currentPagePosition = 0;
    private int clickedRadioButtonPosition = 0;
    private String qState = "0";
    private String TAG ="RadioBoxesFragment";
    int relatedId;
    String appName = "";
    String enterTime ="";
    private EditText editText_answer;
    String notiId = "";
    public RadioBoxesFragment_diary()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.diary_radio_boxes, container, false);

        db = appDatabase.getDatabase(getActivity());

        nextOrFinishButton = rootView.findViewById(R.id.diary_nextOrFinishButton);
        //previousButton = rootView.findViewById(R.id.previousButton);
        questionRBTypeTextView = rootView.findViewById(R.id.diary_questionRBTypeTextView);
        radioGroupForChoices = rootView.findViewById(R.id.diary_radioGroupForChoices);

        nextOrFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(!pageRecord.contains(currentPagePosition))
//                    pageRecord.add(currentPagePosition);
                Log.d(TAG, "now:" + currentPagePosition);
                Log.d(TAG, "size:" + ((QuestionActivity_diary) mContext).getTotalQuestionsSize());
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
//                    Intent returnIntent = new Intent();
//                    if (check_radio_answer("1") == 1) {
//                        returnIntent.putExtra("isMobileCrowdsource", false);
//                    }
//                    mContext.setResult(Activity.RESULT_OK, returnIntent);
                    CanFillDiary = false;
                    UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
                    if(userRecord != null) {
                        db.userDataRecordDao().updateCanFillDiary(userRecord.get_id(), false);
                    }
                    isDFinish = "2";
                    startActivity(MainIntent);
                    mContext.finish();
                }
//                else if(currentPagePosition == 1){
//                    int ans = check_radio_answer(String.valueOf(currentPagePosition));
//                    if (ans == 0) {//否
//                        ((QuestionActivity_diary) mContext).nextQuestion(5);
//                    }else {
//                        ((QuestionActivity_diary) mContext).nextQuestion(1);
//                    }
//                }
                else if(currentPagePosition == 6){ //看資料庫來不來的及更新，來不及用變數存
                    int ans = check_radio_answer(String.valueOf(currentPagePosition));
                    if (ans == 1) {//否
                        ((QuestionActivity_diary) mContext).nextQuestion(9);
                    }else {
                        ((QuestionActivity_diary) mContext).nextQuestion(1);
                    }
                }
                else if(currentPagePosition == 11){ //看資料庫來不來的及更新，來不及用變數存
                    if (D11_Answer == 1) {//否
                        ((QuestionActivity_diary) mContext).nextQuestion(4);
                    }else {
                        ((QuestionActivity_diary) mContext).nextQuestion(1);
                    }
                }
                else if(currentPagePosition == 27  && D15_number == 1){
                    ((QuestionActivity_diary) mContext).nextQuestion(5);
                }
                else if(currentPagePosition == 29 && D15_number == 2){
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


    private String getTheStateOfRadioBox(String[] data)
    {
        return db.questionWithAnswersDao().isChecked(data[0], data[1]);
    }

    private void saveActionsOfRadioBox()
    {
        for (int i = 0; i < radioButtonArrayList.size(); i++)
        {
            if (i == clickedRadioButtonPosition)
            {
                RadioButton radioButton = radioButtonArrayList.get(i);
                if (radioButton.isChecked())
                {
                    Log.d(TAG, "radioButton " + i + "isChecked");

                    if(currentPagePosition == 11){
                        D11_Answer = i;
                    }
                    if(currentPagePosition == 2 || currentPagePosition == 8 || currentPagePosition == 9 ){
                        editText_answer.setText("");
                    }

                    atLeastOneChecked = true;

                    String cbPosition = String.valueOf(radioButtonArrayList.indexOf(radioButton));

                    String[] data = new String[]{"1", questionId, cbPosition};
                    insertChoiceInDatabase(data);

                } else
                {
                    Log.d(TAG, "radioButton " + i + "is not Checked");
                    String cbPosition = String.valueOf(radioButtonArrayList.indexOf(radioButton));

                    String[] data = new String[]{"-1", questionId, cbPosition};
                    insertChoiceInDatabase(data);
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
        {//第一題
            if (questionId.equals("1")) {
                String first = db.questionWithAnswersDao().isChecked(questionId, "0");

                if (first != null) {
                    Log.d(TAG, " radioBox first :  " + first);
                    if (first.equals("1")) {
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
                        return RadioBoxesFragment_diary.this.insertingInDbForOther(data1);
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
                        return RadioBoxesFragment_diary.this.insertingInDb(data1);
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
        if (getArguments() != null)
        {
//            radioButtonTypeQuestion = getArguments().getParcelable("question");
            radioButtonTypeQuestion = QuestionActivity_diary.questionsItems.get(getArguments().getInt("page_position"));
            questionId = String.valueOf(radioButtonTypeQuestion != null ? radioButtonTypeQuestion.getId() : 0);
            currentPagePosition = getArguments().getInt("page_position") + 1;
            enterTime = getArguments().getString("enterTimeForF");
            relatedId = getArguments().getInt("relatedIdF");
            notiId = getArguments().getString("notiId");
        }
        String apppendText = "";

//        Log.d(TAG, "D7 answer size = " + D7Answer.size());
//        if(currentPagePosition >= 11 && currentPagePosition < 55 && (currentPagePosition - 11) % validation_following == 0) {
//            int index = (currentPagePosition - 11) / validation_following;
//            if (index < D9Answer.size()) {
//                String option = (new ArrayList<String>(D9Answer.values())).get(index);
//                radioButtonTypeQuestion.setQuestionName("針對「"+ option + "」，這則新聞的主要內容與哪個主題相關？");
//            }
//        }
//        else if(currentPagePosition >= 13 && currentPagePosition < 55  && (currentPagePosition - 13) % validation_following == 0) {
//            int index = (currentPagePosition - 11) / validation_following;
//            if (index < D9Answer.size()) {
//                String option = (new ArrayList<String>(D9Answer.values())).get(index);
//                radioButtonTypeQuestion.setQuestionName("針對「" + option + "」，請問你查證或搜尋這些的原因是？");
//            }
//        }
//        else if(currentPagePosition >= 14 && currentPagePosition < 55 && (currentPagePosition - 14) % validation_following == 0) {
//            int index = (currentPagePosition - 11) / validation_following;
//            if (index < D9Answer.size()) {
//                String option = (new ArrayList<String>(D9Answer.values())).get(index);
//                radioButtonTypeQuestion.setQuestionName("針對「" + option + "」，請問你怎麼查證或搜尋這些新聞？");
//            }
//        }
        if(currentPagePosition == 26 && D15_number >= 1){
            radioButtonTypeQuestion.setQuestionName("請問你對於[" + D15Answer.get(0) + "]這個事件的想法或感覺有多正向？");
        }
        if(currentPagePosition == 28 && D15_number >= 2){
            radioButtonTypeQuestion.setQuestionName("請問你對於[" + D15Answer.get(1) + "]這個事件的想法或感覺有多正向？");
        }
        if(currentPagePosition == 30 && D15_number >= 3){
            radioButtonTypeQuestion.setQuestionName("請問你對於[" + D15Answer.get(2) + "]這個事件的想法或感覺有多正向？");
        }
        if(currentPagePosition == 27 && D15_number >= 1){
            radioButtonTypeQuestion.setQuestionName("請問你對於[" + D15Answer.get(0) + "]這個事件的想法或感覺有多負向？");
        }
        if(currentPagePosition == 29 && D15_number >= 2){
            radioButtonTypeQuestion.setQuestionName("請問你對於[" + D15Answer.get(1) + "]這個事件的想法或感覺有多負向？");
        }
        if(currentPagePosition == 31 && D15_number >= 3){
            radioButtonTypeQuestion.setQuestionName("請問你對於[" + D15Answer.get(2) + "]這個事件的想法或感覺有多負向？");
        }

        if(currentPagePosition == 35 && !D32_Answer.get(0).equals("")){
            radioButtonTypeQuestion.setQuestionName("針對[" + D32_Answer.get(0) + "]，你覺得你一起談論或討論的對象跟你的看法相不相同？");
        }
        if(currentPagePosition == 38 && !D32_Answer.get(1).equals("")){
            radioButtonTypeQuestion.setQuestionName("針對[" + D32_Answer.get(1) + "]，你覺得你一起談論或討論的對象跟你的看法相不相同？");
        }
        if(currentPagePosition == 41 && !D32_Answer.get(2).equals("")){
            radioButtonTypeQuestion.setQuestionName("針對[" + D32_Answer.get(2) + "]，你覺得你一起談論或討論的對象跟你的看法相不相同？");
        }

        String title = radioButtonTypeQuestion != null ? radioButtonTypeQuestion.getQuestionName():"";
        Log.d(TAG,"title : "+title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            questionRBTypeTextView.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY));
        }
        else{
            questionRBTypeTextView.setText(Html.fromHtml(title));
        }
       //questionRBTypeTextView.setText(radioButtonTypeQuestion.getQuestionName()+'\n'+apppendText);
        //setTextWithSpan(title,questionRBTypeTextView);
//        questionRBTypeTextView.setText(title);
        List<AnswerOptions_diary> choices = radioButtonTypeQuestion.getAnswerOptions();
        radioButtonArrayList.clear();

        for (AnswerOptions_diary choice : choices)
        {
            if(choice.getName().equals(others)){
                editText_answer = new EditText(mContext);
                editText_answer.setHint("其他(請說明)");
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = 25;
                radioGroupForChoices.addView(editText_answer, params);
                editText_answer.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s.length() >= 1 && !s.toString().equals("0")) {
                            nextOrFinishButton.setEnabled(true);
                        } else {
                            nextOrFinishButton.setEnabled(false);
                        }
                        if(!editText_answer.getText().toString().equals("")) {
                            radioGroupForChoices.clearCheck();
                            for (int i = 0; i < radioButtonArrayList.size(); i++) {
                                RadioButton radioButton = radioButtonArrayList.get(i);
                                {
                                    if (radioButton.isChecked())
                                        Log.d(TAG, "radioButton " + i + "is Checked");
                                    else
                                        Log.d(TAG, "radioButton " + i + "is not Checked");
                                    String cbPosition = String.valueOf(radioButtonArrayList.indexOf(radioButton));

                                    String[] data = new String[]{"-1", questionId, cbPosition};
                                    insertChoiceInDatabase(data);
                                }
                            }
                        }

                        if(editText_answer.getText().toString().equals("")){
                            Log.d(TAG, "edit text is space");
                            atLeastOneChecked = false;
                            nextOrFinishButton.setEnabled(false);
                            String[] data = new String[]{"-1", questionId, editText_answer.getText().toString(), others};
                            insertChoiceInDatabaseForOther(data);
                        }
                        else {
                            Log.d(TAG, "edit text is not space");
                            String[] data = new String[]{"1", questionId, editText_answer.getText().toString(), others};
                            insertChoiceInDatabaseForOther(data);
                        }
//                        String[] data = new String[]{editText_answer.getText().toString(), questionId, String.valueOf(0)};
//                        insertChoiceInDatabase(data);

                    }
                });
            }
            else{
                RadioButton rb = new RadioButton(mContext);
                rb.setText(choice.getName());
                rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                rb.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
                rb.setPadding(10, 40, 10, 40);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = 25;
                rb.setLayoutParams(params);

                View view = new View(mContext);
                view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.divider));
                view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));

                radioGroupForChoices.addView(rb);
                radioGroupForChoices.addView(view);
                radioButtonArrayList.add(rb);
                atLeastOneChecked = false;
                rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (screenVisible) {
                            Log.d(TAG, "radio button is changed");
                            clickedRadioButtonPosition = radioButtonArrayList.indexOf(buttonView);
                            RadioBoxesFragment_diary.this.saveActionsOfRadioBox();
                        }
                    }
                });
            }


        }

        if (atLeastOneChecked)
        {
            nextOrFinishButton.setEnabled(true);
        } else
        {
            nextOrFinishButton.setEnabled(false);
        }

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
                                } else{
                                    if(editText_answer != null) {
                                        editText_answer.setText(qState);
                                    }
                                }
                            }
                        });
            }
            isFirstLoad = false;
        }
    }
}