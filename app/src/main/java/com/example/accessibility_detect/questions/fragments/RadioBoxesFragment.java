package com.example.accessibility_detect.questions.fragments;

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
import com.example.accessibility_detect.questions.QuestionActivity;
import com.example.accessibility_detect.questions.questionmodels.AnswerOptions;
import com.example.accessibility_detect.questions.questionmodels.QuestionsItem;

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
import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;

import static android.content.Context.MODE_PRIVATE;
import static labelingStudy.nctu.minuku.config.Constants.multitask_following;
import static labelingStudy.nctu.minuku.config.Constants.others;
import static labelingStudy.nctu.minuku.config.SharedVariables.CanFillEsm;
import static labelingStudy.nctu.minuku.config.SharedVariables.Q24Answer;
import static labelingStudy.nctu.minuku.config.SharedVariables.Q25Answer;
import static labelingStudy.nctu.minuku.config.SharedVariables.Q28Answer;
import static labelingStudy.nctu.minuku.config.SharedVariables.Q7Answer;
import static labelingStudy.nctu.minuku.config.SharedVariables.Q8Answer;
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTime;
import static labelingStudy.nctu.minuku.config.SharedVariables.isFinish;

/**
 * This fragment provide the RadioButton/Single Options.
 */
public class RadioBoxesFragment extends Fragment
{
    private final ArrayList<RadioButton> radioButtonArrayList = new ArrayList<>();
    private boolean isFirstLoad = true; // 是否第一次加载
    private boolean screenVisible = false;
    private static QuestionsItem radioButtonTypeQuestion;
    private FragmentActivity mContext;
    private Button nextOrFinishButton;
    //private Button previousButton;
    private TextView questionRBTypeTextView;
    private RadioGroup radioGroupForChoices;
    private boolean atLeastOneChecked = false;
    appDatabase db;
    private String questionId = "";
    private int currentPagePosition = 0;
    private int clickedRadioButtonPosition = -1;
    private String qState = "0";
    private String TAG ="RadioBoxesFragment";
    int relatedId;
    String appName = "";
    String enterTime ="";
    private EditText editText_answer;
    String notiId = "";
    public RadioBoxesFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_radio_boxes, container, false);

        db = appDatabase.getDatabase(getActivity());

        nextOrFinishButton = rootView.findViewById(R.id.nextOrFinishButton);
        //previousButton = rootView.findViewById(R.id.previousButton);
        questionRBTypeTextView = rootView.findViewById(R.id.questionRBTypeTextView);
        radioGroupForChoices = rootView.findViewById(R.id.radioGroupForChoices);

        nextOrFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(!pageRecord.contains(currentPagePosition))
//                    pageRecord.add(currentPagePosition);
                Log.d(TAG, "now:" + currentPagePosition);
                Log.d(TAG, "size:" + ((QuestionActivity) mContext).getTotalQuestionsSize());
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
                if (currentPagePosition == ((QuestionActivity) mContext).getTotalQuestionsSize()) {
                    /* Here, You go back from where you started OR If you want to go next Activity just change the Intent*/
//                    Intent returnIntent = new Intent();
//                    if (check_radio_answer("1") == 1) {
//                        returnIntent.putExtra("isMobileCrowdsource", false);
//                    }
//                    mContext.setResult(Activity.RESULT_OK, returnIntent);
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
                        ((QuestionActivity) mContext).nextQuestion(1);
                    }
                }
                else if(currentPagePosition == 7){//第9題，否的話跳4題，是的話跳下一題
                    if (Q7Answer == 0) {//是
                        ((QuestionActivity) mContext).nextQuestion(4);
                    }else {
                        ((QuestionActivity) mContext).nextQuestion(1);
                    }
                }
                else if(currentPagePosition == 8){
                    ((QuestionActivity) mContext).nextQuestion(2);
                }
                else if(currentPagePosition == 24){
                    if(Q24Answer == 0){
                        ((QuestionActivity) mContext).nextQuestion(1);
                    }
                    else{
                        UserDataRecord userRecord = db.userDataRecordDao().getLastRecord();
                        if(userRecord != null) {
                            db.userDataRecordDao().updateCanFillESM(userRecord.get_id(), false);
                        }
                        CanFillEsm = false;
                        isFinish = "2";
                        startActivity(MainIntent);
                        mContext.finish();
                    }
                }
                else if(currentPagePosition >= 28 && (currentPagePosition - 28) % multitask_following == 0){
                    if(Q28Answer.get((currentPagePosition - 28) % multitask_following) == 0){
                        ((QuestionActivity) mContext).nextQuestion(2);
                    }
                    else{
                        ((QuestionActivity) mContext).nextQuestion(3);
                    }
                }
                else {
                    ((QuestionActivity) mContext).nextQuestion(1);
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
            Log.d(TAG, i + " " + clickedRadioButtonPosition);
            if (i == clickedRadioButtonPosition)
            {
                RadioButton radioButton = radioButtonArrayList.get(i);
                Log.d(TAG, "radioButton Text: " + radioButton.getText());
                if (radioButton.isChecked())
                {
                    if(currentPagePosition == 24)
                    {
                        Q24Answer = i;
                    }
                    if(currentPagePosition >= 28 && (currentPagePosition - 28) % multitask_following == 0)
                    {
                        Q28Answer.put((currentPagePosition - 28) % multitask_following, i);
                    }
                    if(currentPagePosition == 8)
                    {
                        Q8Answer = radioButton.getText().toString();
                        editText_answer.setText("");
                        Log.d(TAG, "Q8 answer: " + Q8Answer);
                    }
                    if(currentPagePosition == 12){
                        editText_answer.setText("");
                    }
                    if(currentPagePosition == 7){
                        Q7Answer = i;
                    }
                    Log.d(TAG, "radioButton " + i + "isChecked");
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
                        return RadioBoxesFragment.this.insertingInDbForOther(data1);
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
                        return RadioBoxesFragment.this.insertingInDb(data1);
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
            CSVHelper.storeToCSV("ESM_debug.csv", "getArguments() is not null");
//            radioButtonTypeQuestion = getArguments().getParcelable("question");
            radioButtonTypeQuestion = QuestionActivity.questionsItems.get(getArguments().getInt("page_position"));
            questionId = String.valueOf(radioButtonTypeQuestion != null ? radioButtonTypeQuestion.getId() : 0);
            currentPagePosition = getArguments().getInt("page_position") + 1;
            enterTime = getArguments().getString("enterTimeForF");
            relatedId = getArguments().getInt("relatedIdF");
            notiId = getArguments().getString("notiId");
        }
        else{
            CSVHelper.storeToCSV("ESM_debug.csv", "getArguments() is null");
        }
        Long nowESM_time = mContext.getSharedPreferences("test", MODE_PRIVATE).getLong("Now_Esm_Time", 0);
        Long lastESM_time = mContext.getSharedPreferences("test", MODE_PRIVATE).getLong("Last_Esm_Time", 0);
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
        if(currentPagePosition == 10){
            String option = Q8Answer;
            radioButtonTypeQuestion.setQuestionName("請問你同不同意「" + option + "」對這則新聞的想法或態度？");
        }
        else if(currentPagePosition >= 28 && (currentPagePosition - 28) % multitask_following == 0) {
            int index = (currentPagePosition - 28) / multitask_following;
            if (index < Q25Answer.size()) {
                String option = (new ArrayList<String>(Q25Answer.values())).get(index);
                radioButtonTypeQuestion.setQuestionName("請問在當時，「" + option + "」與「用手機看新聞」哪個是主要活動? (需要優先完成、比較重要的活動)");
                radioButtonTypeQuestion.getAnswerOptions().get(0).setName(option);
                db.questionWithAnswersDao().updateQuestionName(option, questionId, "0");
            }
        }
        else if(currentPagePosition >= 30 && (currentPagePosition - 30) % multitask_following == 0) {
            int index = (currentPagePosition - 30) / multitask_following;
            if (index < Q25Answer.size()) {
                String option = (new ArrayList<String>(Q25Answer.values())).get(index);
                radioButtonTypeQuestion.setQuestionName("請問在當時，「用手機看新聞」是為了「" + option + "」嗎?");
            }
        }
        else if(currentPagePosition >= 32 && (currentPagePosition - 32) % multitask_following == 0) {
            int index = (currentPagePosition - 32) / multitask_following;
            if (index < Q25Answer.size()) {
                String option = (new ArrayList<String>(Q25Answer.values())).get(index);
                radioButtonTypeQuestion.setQuestionName("對你來說，請問「" + option + "」時，你感受到<b><u>無聊</u></b>的程度為何？");
            }
        }
        else if(currentPagePosition >= 33 && (currentPagePosition - 33) % multitask_following == 0) {
            int index = (currentPagePosition - 33) / multitask_following;
            if (index < Q25Answer.size()) {
                String option = (new ArrayList<String>(Q25Answer.values())).get(index);
                radioButtonTypeQuestion.setQuestionName("對你來說，請問「" + option + "」時，你感受到<b><u>焦慮</u></b>的程度為何？");
            }
        }
        else if(currentPagePosition >= 34 && (currentPagePosition - 34) % multitask_following == 0) {
            int index = (currentPagePosition - 34) / multitask_following;
            if (index < Q25Answer.size()) {
                String option = (new ArrayList<String>(Q25Answer.values())).get(index);
                radioButtonTypeQuestion.setQuestionName("對你來說，請問「" + option + "」時，你感受到<b><u>開心</u></b>的程度為何？");
            }
        }
        else if(currentPagePosition >= 35 && (currentPagePosition - 35) % multitask_following == 0) {
            int index = (currentPagePosition - 35) / multitask_following;
            if (index < Q25Answer.size()) {
                String option = (new ArrayList<String>(Q25Answer.values())).get(index);
                radioButtonTypeQuestion.setQuestionName("對你來說，請問「" + option + "」時，你感受到<b><u>滿意</u></b>的程度為何？");
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            questionRBTypeTextView.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY));
        }
        else{
            questionRBTypeTextView.setText(Html.fromHtml(title));
        }
        try {
            if(radioButtonTypeQuestion == null){
                CSVHelper.storeToCSV("ESM_debug.csv", "radioButtonTypeQuestion is null");
            }
            else{
                CSVHelper.storeToCSV("ESM_debug.csv", "radioButtonTypeQuestion is not null");
            }
            List<AnswerOptions> choices = radioButtonTypeQuestion.getAnswerOptions();
            radioButtonArrayList.clear();

            for (AnswerOptions choice : choices)
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

                            if(s.length() >= 1 && !s.toString().equals("0")) {
                                radioGroupForChoices.clearCheck();
                                Log.d(TAG, "Clear check");
                                for (int i = 0; i < radioButtonArrayList.size(); i++) {
                                    RadioButton radioButton = radioButtonArrayList.get(i);
                                    {
                                        if (radioButton.isChecked())
                                            Log.d(TAG, "radioButton " + i + " is Checked");
                                        else
                                            Log.d(TAG, "radioButton " + i + " is not Checked");

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
                                Q8Answer = editText_answer.getText().toString();
                            }
                            //                        String[] data = new String[]{editText_answer.getText().toString(), questionId, String.valueOf(0)};
                            //                        insertChoiceInDatabase(data);

                        }
                    });
                }else{
                    RadioButton rb = new RadioButton(mContext);
                    String option_name = choice.getName();
                    Log.d(TAG, "HTML string: " + option_name);
                    rb.setText(option_name);
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
                                RadioBoxesFragment.this.saveActionsOfRadioBox();
                            }
                        }
                    });
                }


            }
            //        RadioBoxesFragment.this.saveActionsOfRadioBox();
        }
        catch (Exception e){
            e.printStackTrace();
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
        if (currentPagePosition == ((QuestionActivity) mContext).getTotalQuestionsSize())
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