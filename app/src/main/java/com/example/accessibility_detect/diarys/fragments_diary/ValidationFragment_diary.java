package com.example.accessibility_detect.diarys.fragments_diary;

/**
 * Created by chiaenchiang on 03/01/2019.
 */

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
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
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.accessibility_detect.MainActivity;
import com.example.accessibility_detect.R;
import com.example.accessibility_detect.diarys.QuestionActivity_diary;
import com.example.accessibility_detect.diarys.questionmodels_diary.QuestionsItem_diary;

import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import labelingStudy.nctu.minuku.DBHelper.appDatabase;
import labelingStudy.nctu.minuku.model.DataRecord.UserDataRecord;

import static labelingStudy.nctu.minuku.config.SharedVariables.CanFillDiary;
import static labelingStudy.nctu.minuku.config.SharedVariables.D15Answer;
import static labelingStudy.nctu.minuku.config.SharedVariables.D15_number;
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTime;
import static labelingStudy.nctu.minuku.config.SharedVariables.isDFinish;


public class ValidationFragment_diary extends Fragment {
    private String TAG = "ValidationFragment";
    private FragmentActivity mContext;
    private Button nextOrFinishButton;
    private LinearLayout OuterLinearLayout, InnerLinearLayout;
    private TextView textview_title;
    private EditText[] ed;
    private RadioButton rb;
    private boolean RadioCheck = false;
//    private EditText editText_answer;
    private String qState = "";
    private appDatabase db;
    private boolean isFirstLoad = true; // 是否第一次加载
    private boolean f = false;
    String appName="";
    String enterTime = "";
    int relatedId;
    private boolean enable_first = true;
    private String questionId = "";
    private int currentPagePosition = 0;
    String notiId = "";
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.diary_validationrecall, container, false);

        db = appDatabase.getDatabase(getActivity());

        nextOrFinishButton = rootView.findViewById(R.id.diary_nextOrFinishButton);

        //previousButton = rootView.findViewById(R.id.previousButton);
        textview_title = (TextView) rootView.findViewById(R.id.diary_textview_title);
//        editText_answer = (EditText) rootView.findViewById(R.id.diary_editText_answer);
        OuterLinearLayout = (LinearLayout) rootView.findViewById(R.id.diary_outer_linearlayout);

//        InnerLinearLayout = (LinearLayout) rootView.findViewById(R.id.diary_validation_linearlayout);

        nextOrFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                D13Answer.clear();
//                for(int i = 0; i < D7Answer.size(); i++){
//                    if(!ed[i].getText().toString().equals("")) {
//                        D9Answer.put(ed[i].getText().toString(), ed[i].getText().toString());
//                    }
//                }
//                Log.d(TAG, "D9Answer = " + D9Answer);
                if(currentPagePosition == 15 && D15_number != 0) {
                    Log.d(TAG, "D15 number: " + D15Answer.get(0) + " " + D15Answer.get(1) + " " + D15Answer.get(2));
                }

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
                else if (currentPagePosition == 15) {
                    if (D15_number == 0) {
                        ((QuestionActivity_diary) mContext).nextQuestion(28);
                    }
                    else {
                        ((QuestionActivity_diary) mContext).nextQuestion(2);
                    }
                }
            }
        });
        //previousButton.setOnClickListener(view -> mContext.onBackPressed());

        return rootView;
    }
    public Integer check_radio_answer(String questionId){
        String first,second,third,fourth,other;
        first = db.questionWithAnswersDao().isChecked(questionId,"0");
        second = db.questionWithAnswersDao().isChecked(questionId,"1");
        third = db.questionWithAnswersDao().isChecked(questionId,"2");
        fourth = db.questionWithAnswersDao().isChecked(questionId,"3");
        other = db.questionWithAnswersDao().isChecked(questionId,"4");


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
        if(third!=null) {
            Log.d("qskip"," radioBox third :  "+third);
            if (third.equals("1")) {
                return 2;
            }
        }
        if(fourth!=null) {
            Log.d("qskip"," radioBox fourth :  "+fourth);
            if (fourth.equals("1")) {
                return 3;
            }
        }
        if(other!=null){
            if(other.length()>=1){
                return 4;
            }
        }
        return 0;
    }
    private void insertAnswerInDatabase(String[] data)
    {
        Observable.just(data)
                .map(new Function<String[], Object>() {
                    @Override
                    public Object apply(String[] data1) throws Exception {
                        return ValidationFragment_diary.this.insertingInDb(data1);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    private String insertingInDb(String[] data)
    {   String answerTime = getReadableTime(new Date().getTime());
        db.questionWithAnswersDao().updateQuestionWithChoice(data[0], data[1], data[2]);
        // String LastAnswerTime = db.questionWithAnswersDao().getLastTimeDetectedTime(data[1], data[2]);
        db.questionWithAnswersDao().updateQuestionWithDetectedTime(answerTime ,data[1], data[2]);
        // insertFinalAnswer(data[1],data[2], data[0],answerTime);
        return "";
    }
//    public void insertFinalAnswer(String questionId,String optionId,String selectState,String answerTime){
//        //第幾題、第幾個選項、第幾個解答、回答時間
//        Integer ansId = MapAnswerPositiontoId(Integer.parseInt(questionId),Integer.parseInt(optionId));
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

    private void saveActionsOfRadioBox()
    {
        Log.d(TAG, "saveActionsOfRadioBox");
        if(currentPagePosition == 15) {
            D15_number = 0;
            D15Answer.set(0, "");
            D15Answer.set(1, "");
            D15Answer.set(2, "");
        }

        for(int i  = 0; i < ed.length; i++){
//            Log.d(TAG, "Insert " + ed[i]);
            String Ans_title = ed[i].getText().toString();
            if(!Ans_title.equals("")) {
//                rb.setChecked(false);
                String[] data = new String[]{Ans_title, questionId, String.valueOf(i)};
                insertAnswerInDatabase(data);
                if(currentPagePosition == 15) {
                    D15_number++;
                }
            }
            else{
                String[] data = new String[]{"-1", questionId, String.valueOf(i)};
                insertAnswerInDatabase(data);
            }
            Log.d(TAG, i + " " + Ans_title);
            if(currentPagePosition == 15) {
                D15Answer.set(i, Ans_title);
            }
        }
        if(ed[0].getText().toString().equals("")){
            ed[1].setEnabled(false);
            ed[2].setEnabled(false);
        }
        else if(!ed[0].getText().toString().equals("") && ed[1].getText().toString().equals("")){
            ed[1].setEnabled(true);
            ed[2].setEnabled(false);
        }
        else if(!ed[0].getText().toString().equals("") && !ed[1].getText().toString().equals("")){
            ed[2].setEnabled(true);
        }

//        if(ed[1].getText().toString().equals("")){
//            ed[2].setEnabled(false);
//        }
//        else{
//            ed[2].setEnabled(true);
//        }
        if(rb!=null) {
//            if (rb.isChecked()) {
//                D21_number = 0;
//                D21Answer.clear();
//                nextOrFinishButton.setEnabled(true);
//                for (int i = 0; i < ed.length; i++) {
//                    if (!ed[i].getText().toString().equals("")) {
//                        ed[i].setText("");
//                        String[] data = new String[]{"-1", questionId, String.valueOf(i)};
//                        insertAnswerInDatabase(data);
//                    }
//                }
//                String[] data = new String[]{"都沒有", questionId, String.valueOf(3)};
//                insertAnswerInDatabase(data);
//            } else {
//                String[] data = new String[]{"-1", questionId, String.valueOf(3)};
//                insertAnswerInDatabase(data);
//            }
        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        TextView textView = getTextView();
//        textView.setText(choice.getName());
//        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
//        textView.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
//        textView.setPadding(10, 0, 10, 0);
        mContext = (FragmentActivity) getActivity();
        QuestionsItem_diary textTypeQuestion = null;
        if (getArguments() != null)
        {
//            textTypeQuestion = getArguments().getParcelable("question");
            textTypeQuestion = QuestionActivity_diary.questionsItems.get(getArguments().getInt("page_position"));
            relatedId = getArguments().getInt("relatedIdF");
            enterTime = getArguments().getString("enterTimeForF");
            questionId = String.valueOf(textTypeQuestion != null ? textTypeQuestion.getId() : 0);
            currentPagePosition = getArguments().getInt("page_position") + 1;
            notiId = getArguments().getString("notiId");
        }
//        String ans = db.questionWithAnswersDao().isChecked("1","0");
        String apppendText = "";

        String title = textTypeQuestion != null ? textTypeQuestion.getQuestionName():"";
//        textview_title.setText(textTypeQuestion != null ? textTypeQuestion.getQuestionName():"");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textview_title.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY));
        }
        else{
            textview_title.setText(Html.fromHtml(title));
        }
        /*Disable the button until any choice got selected*/
        if(currentPagePosition == 15)
            nextOrFinishButton.setEnabled(false);
//        editText_answer.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                Log.d(TAG, "s =" + s + "?");
//                if (s.length() >= 1 && !s.toString().equals("")) {
//                    nextOrFinishButton.setEnabled(true);
//                } else {
//                    nextOrFinishButton.setEnabled(false);
//                }
//                String[] data = new String[]{editText_answer.getText().toString(), questionId, String.valueOf(0)};
//                insertAnswerInDatabase(data);
//            }
//        });
        LinearLayout linear = null;
        ed = new EditText[3];
        for(int i = 0; i < 3; i++){
            linear = new LinearLayout(mContext);
            linear.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linear.setOrientation(LinearLayout.HORIZONTAL);

            TextView tv = new TextView(mContext);
            if(currentPagePosition == 15){
                if(i == 0){
                    tv.setText("最重要事件: ");
                }
                if(i == 1){
                    tv.setText("第二重要事件: ");
                }
                if(i == 2){
                    tv.setText("第三重要事件: ");
                }
            }

            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            tv.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
            tv.setPadding(10, 40, 10, 40);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 25;
            tv.setLayoutParams(params);

            ed[i] = new EditText(mContext);

            if(D15Answer.size() != 0 && currentPagePosition == 15) ed[i].setText(D15Answer.get(i));

            Log.d(TAG, "OnActivityCreated: " + ed[i].getText().toString());
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            params.leftMargin = 16;
            params.rightMargin = 25;
            ed[i].setLayoutParams(params);
            ed[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.d(TAG, "AfterTextChanged");
                    ValidationFragment_diary.this.saveActionsOfRadioBox();
                    if (s.length() >= 1 && !s.toString().equals("")) {
                        Log.d(TAG, "word");
                        f = true;
                        nextOrFinishButton.setEnabled(true);
                        if(rb != null && currentPagePosition == 15)rb.setChecked(false);
                    } else {
                        if(currentPagePosition == 15 && D15_number == 0) {
                            f = false;
                            nextOrFinishButton.setEnabled(false);
                        }
                        Log.d(TAG, "rb is not checked");
                        if(rb != null && currentPagePosition == 15){
                            if(rb.isChecked()){
                                Log.d(TAG, "rb is checked");
                                f = true;
                                nextOrFinishButton.setEnabled(true);
                            }
                        }
                    }
                }
            });
            linear.addView(tv);
            linear.addView(ed[i]);
            OuterLinearLayout.addView(linear);
        }

        if(currentPagePosition == 15) {
            rb = new RadioButton(mContext);
            rb.setText("都沒有");
            rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            rb.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
            rb.setPadding(10, 40, 10, 40);
            rb.setChecked(RadioCheck);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 25;
            rb.setLayoutParams(params);

            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(TAG, "radio button is changed");
                    RadioCheck = isChecked;
                    Log.d(TAG, "Radio Check: " + RadioCheck);
                    if (isChecked) {
                        D15_number = 0;
                        D15Answer.set(0, "");
                        D15Answer.set(1, "");
                        D15Answer.set(2, "");
                        f = true;
                        nextOrFinishButton.setEnabled(true);
                        for (int i = 0; i < ed.length; i++) {
                            if (!ed[i].getText().toString().equals("")) {
                                ed[i].setText("");
                                String[] data = new String[]{"-1", questionId, String.valueOf(i)};
                                insertAnswerInDatabase(data);
                            }
                        }
                        String[] data = new String[]{"都沒有", questionId, String.valueOf(3)};
                        insertAnswerInDatabase(data);
                    } else {
                        String[] data = new String[]{"-1", questionId, String.valueOf(3)};
                        insertAnswerInDatabase(data);
                    }
                }
            });
            OuterLinearLayout.addView(rb);
            if(ed[0].getText().toString().equals(""))ed[1].setEnabled(false);
            if(ed[1].getText().toString().equals(""))ed[2].setEnabled(false);
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

        // editText_answer.requestFocus();
//        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Service.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(editText_answer, 0);
    }

    private String getTheStateOfSeekBar(String[] data)
    {
        String str = db.questionWithAnswersDao().isChecked(data[0], data[1]);
        if(str.equals("-1"))
            return "";
        return str;
    }
    public void onDestroyView() {
        super.onDestroyView();
        isFirstLoad = true;
    }
    @Override
    public void onResume() {
        super.onResume();
        if (isFirstLoad) {
            String cbPosition = String.valueOf(0);
            String[] data = new String[]{questionId, cbPosition};
            Observable.just(data)
                    .map(this::getTheStateOfSeekBar)
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
//                            seekBar.setProgress(Float.parseFloat(qState));
//                            editText_answer.setText(qState);
                            Log.d(TAG, "onComplete");
                            boolean flag = false;
                            for(int i = 0; i < 3; i++) {
                                if(currentPagePosition == 15) {
                                    if (D15_number != 0) {
//                                        Log.d(TAG, D13Answer.get(i));
//                                    ed[i].setText(D13Answer.get(i));
                                    }
                                }
                            }
                            if(currentPagePosition == 15) {
                                nextOrFinishButton.setEnabled(f);
                            }
                        }
                    });
        }
    }
//    private TextView getTextView() {
//        TextView textView = new TextView(getContext());
//        int padding = dp2px(getContext(), 10);
//        textView.setPadding(padding, padding, padding, 0);
//        return textView;
//    }
}
