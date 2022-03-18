package com.example.accessibility_detect.diarys.fragments_diary;

/**
 * Created by chiaenchiang on 03/01/2019.
 */

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTime;
import static labelingStudy.nctu.minuku.config.SharedVariables.isDFinish;


public class TextFragment_diary extends Fragment {
    private String TAG = "TextFragment";
    private FragmentActivity mContext;
    private Button nextOrFinishButton;
    private TextView textview_q_title;
    private EditText editText_answer;
    private String qState = "";
    private appDatabase db;
    private boolean isFirstLoad = true; // 是否第一次加载
    String appName="";
    String enterTime = "";
    int relatedId;
    private String questionId = "";
    private int currentPagePosition = 0;
    String notiId = "";
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.diary_text, container, false);

        db = appDatabase.getDatabase(getActivity());

        nextOrFinishButton = rootView.findViewById(R.id.diary_nextOrFinishButton);
        //previousButton = rootView.findViewById(R.id.previousButton);
        textview_q_title = (TextView) rootView.findViewById(R.id.diary_textview_q_title);
        editText_answer = (EditText) rootView.findViewById(R.id.diary_editText_answer);

        nextOrFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(currentPagePosition == 7){
//                    D7Answer.clear();
//                    int num = Integer.parseInt(editText_answer.getText().toString());
//                    for(int i = 1; i <= num; i++){
//                        D7Answer.add(String.valueOf(i));
//                    }
//                }
//                Log.d(TAG, "D7Answer size = " + D7Answer.size());
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
                if (currentPagePosition == 1) {
                    if (check_radio_answer("1") == 0) {//轉傳0則
                        ((QuestionActivity_diary) mContext).nextQuestion(5);
                    }
                    else {
                        ((QuestionActivity_diary) mContext).nextQuestion(1);
                    }
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
        String first,second,third,fourth,other;
        first = db.questionWithAnswersDao().isChecked(questionId,"0");

        if(first!=null) {
            Log.d("qskip"," radioBox first :  "+first);
            if (first.equals("0")) {
                return 0;   // 是
            }
            else{
                return 1;
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
                        return TextFragment_diary.this.insertingInDb(data1);
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

//        if(currentPagePosition != 4) editText_answer.setInputType(InputType.TYPE_CLASS_NUMBER);

        textview_q_title.setText(textTypeQuestion != null ? textTypeQuestion.getQuestionName():"");
        /*Disable the button until any choice got selected*/
        nextOrFinishButton.setEnabled(false);


        /* If the current question is last in the questionnaire then
        the "Next" button will change into "Finish" button*/
        if (currentPagePosition == ((QuestionActivity_diary) mContext).getTotalQuestionsSize())
        {
            nextOrFinishButton.setText(R.string.finish);
        } else
        {
            nextOrFinishButton.setText(R.string.next);
        }

        if(currentPagePosition == 1){
            editText_answer.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        editText_answer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "s =" + s + "?");
                if (s.length() >= 1 && !s.toString().equals("")) {
                    nextOrFinishButton.setEnabled(true);
                } else {
                    nextOrFinishButton.setEnabled(false);
                }
                String[] data = new String[]{editText_answer.getText().toString(), questionId, String.valueOf(0)};
                insertAnswerInDatabase(data);
            }
        });

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
                            editText_answer.setText(qState);
                            Log.d(TAG, "onComplete");
                            if(!editText_answer.getText().toString().equals("")) {
                                if (!nextOrFinishButton.isEnabled()) {
                                    nextOrFinishButton.setEnabled(true);
                                }
                            }
                            else{
                                nextOrFinishButton.setEnabled(false);
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
