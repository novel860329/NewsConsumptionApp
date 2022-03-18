package com.example.accessibility_detect.diarys.fragments_diary;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.accessibility_detect.MainActivity;
import com.example.accessibility_detect.R;
import com.example.accessibility_detect.diarys.QuestionActivity_diary;
import com.example.accessibility_detect.diarys.questionmodels_diary.AnswerOptions_diary;
import com.example.accessibility_detect.diarys.questionmodels_diary.QuestionsItem_diary;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.IndicatorStayLayout;
import com.warkiz.widget.IndicatorType;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;
import com.warkiz.widget.TickMarkType;

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
import static labelingStudy.nctu.minuku.config.SharedVariables.D15Answer;
import static labelingStudy.nctu.minuku.config.SharedVariables.D15_number;
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTime;
import static labelingStudy.nctu.minuku.config.SharedVariables.isDFinish;

/**
 * Created by chiaenchiang on 16/11/2018.
 */

public class OneSeekBarsFragment_diary extends Fragment implements SeekBar.OnSeekBarChangeListener {
    private String TAG = "OneSeekBarFragment";
    private boolean isFirstLoad = true; // 是否第一次加载
    private final ArrayList<IndicatorSeekBar> seekBarsArrayList = new ArrayList<>();
    private boolean screenVisible = false;
    private QuestionsItem_diary radioButtonTypeQuestion;
    private FragmentActivity mContext;
    private Button nextOrFinishButton;
    //private Button previousButton;
    private TextView questionOSKTypeTextView;
    private RadioGroup radioGroupForChoices;
    private SeekBar seekBarGroup;
    private int atLeastOneChecked = 0;
    private List<Boolean> checkedBefore = new ArrayList<>();
    String appName = "";
    String enterTime = "";
    appDatabase db;
    private String questionId = "";
    private int currentPagePosition = 0;
    private int clickedRadioButtonPosition = 0;
    private String qState = "0";
    private LinearLayout OneseekBarsLinearLayout;
    private TextView distance;
    LinearLayout content;
    int relatedId;
    private int clickedSeekBarPosition = 0;
    String notiId = "";

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG,"onCreateView");
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.diary_one_seek_bar, container, false);

        db = appDatabase.getDatabase(getActivity());

        nextOrFinishButton = rootView.findViewById(R.id.diary_nextOrFinishButton);
        questionOSKTypeTextView = rootView.findViewById(R.id.diary_questionOSKTypeTextView);
        OneseekBarsLinearLayout = (LinearLayout) rootView.findViewById(R.id.diary_oneseekBarsLinearLayout);

        nextOrFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"nextOrFinishButton onClicked ");
//                if(!pageRecord.contains(currentPagePosition))
//                    pageRecord.add(currentPagePosition);
                Intent MainIntent = new Intent(mContext, MainActivity.class);

                for (int i = 0; i < seekBarsArrayList.size(); i++)
                {
                    {
                        IndicatorSeekBar seekBar = seekBarsArrayList.get(0);
                        String result = new Integer(seekBar.getProgress()).toString();
                        if(result.equals("0")){
                            String[] data = new String[]{"0", questionId, String.valueOf(i)};
                            if((currentPagePosition == 17 || currentPagePosition == 20)&& D15_number >= 1) {
                                insertAnswerInDatabase(data);
                            }
                            if((currentPagePosition == 18 || currentPagePosition == 21 || currentPagePosition == 23) && D15_number >= 2) {
                                Log.d(TAG, "Insert 0 ");
                                insertAnswerInDatabase(data);
                            }
                            if((currentPagePosition == 19 || currentPagePosition == 22 || currentPagePosition == 24 || currentPagePosition == 25)
                                    && D15_number >= 3){
                                insertAnswerInDatabase(data);
                            }
                        }
                    }
                }
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
                else if(D15_number == 1 && currentPagePosition == 17){
                    ((QuestionActivity_diary) mContext).nextQuestion(3);
                }
                else if(D15_number == 2 && currentPagePosition == 18){
                    ((QuestionActivity_diary) mContext).nextQuestion(2);
                }
                else if(D15_number == 1 && currentPagePosition == 20){
                    ((QuestionActivity_diary) mContext).nextQuestion(6);
                }
                else if(D15_number == 2 && currentPagePosition == 21){
                    ((QuestionActivity_diary) mContext).nextQuestion(2);
                }
                else if(D15_number <= 2 && currentPagePosition == 23){
                    ((QuestionActivity_diary) mContext).nextQuestion(3);
                }
                else
                {
                    ((QuestionActivity_diary) mContext).nextQuestion(1);
                }
            }
        });
        //previousButton.setOnClickListener(view -> mContext.onBackPressed());
        // Inflate the layout for this fragment
        return rootView;

    }
    public Integer check_radio_answer(String questionId){
        String first,second,third,fourth,other;
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


    @SuppressLint("ResourceAsColor")
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG,"onActivityCreated ");
        mContext = (FragmentActivity) getActivity();
        String extraInfo = "";
        QuestionsItem_diary seekBarTypeQuestion = null;

        if (getArguments() != null)
        {
//            seekBarTypeQuestion = getArguments().getParcelable("question");
            seekBarTypeQuestion = QuestionActivity_diary.questionsItems.get(getArguments().getInt("page_position"));
            questionId = String.valueOf(seekBarTypeQuestion != null ? seekBarTypeQuestion.getId() : 0);
            currentPagePosition = getArguments().getInt("page_position") + 1;
            enterTime = getArguments().getString("enterTimeForF");
            relatedId = getArguments().getInt("relatedIdF");
            notiId = getArguments().getString("notiId");
        }

        String apppendText = "";

        Log.d(TAG, "current page: " + currentPagePosition);
        if(currentPagePosition == 17 && D15_number >= 1){
            seekBarTypeQuestion.setQuestionName("請問你對於[" + D15Answer.get(0) + "]這個事件與<b><u>你自己的生活</u></b>多相關？0表示兩者沒有關聯，100表示極度相關");
        }
        if(currentPagePosition == 18 && D15_number >= 2){
            seekBarTypeQuestion.setQuestionName("請問你對於[" + D15Answer.get(1) + "]這個事件與<b><u>你自己的生活</u></b>多相關？0表示兩者沒有關聯，100表示極度相關");
        }
        if(currentPagePosition == 19 && D15_number >= 3){
            seekBarTypeQuestion.setQuestionName("請問你對於[" + D15Answer.get(2) + "]這個事件與<b><u>你自己的生活</u></b>多相關？0表示兩者沒有關聯，100表示極度相關");
        }

        if(currentPagePosition == 20 && D15_number >= 1){
            seekBarTypeQuestion.setQuestionName("請問你對於[" + D15Answer.get(0) + "]這個事件與<b><u>整體社會</u></b>多相關？0表示兩者沒有關聯，100表示極度相關");
        }
        if(currentPagePosition == 21 && D15_number >= 2){
            seekBarTypeQuestion.setQuestionName("請問你對於[" + D15Answer.get(1) + "]這個事件與<b><u>整體社會</u></b>多相關？0表示兩者沒有關聯，100表示極度相關");
        }
        if(currentPagePosition == 22 && D15_number >= 3){
            seekBarTypeQuestion.setQuestionName("請問你對於[" + D15Answer.get(2) + "]這個事件與<b><u>整體社會</u></b>多相關？0表示兩者沒有關聯，100表示極度相關");
        }

        if(currentPagePosition == 23 && D15_number >= 2){
            seekBarTypeQuestion.setQuestionName("請問你覺得[" + D15Answer.get(0) + "]與[" + D15Answer.get(1) + "]相關的程度有多強？0表示兩者沒有關聯，100表示兩者極度相關，可視為同一個事件。");
        }
        if(currentPagePosition == 24 && D15_number >= 3){
            seekBarTypeQuestion.setQuestionName("請問你覺得[" + D15Answer.get(0) + "]與[" + D15Answer.get(2) + "]相關的程度有多強？0表示兩者沒有關聯，100表示兩者極度相關，可視為同一個事件。");
        }
        if(currentPagePosition == 25 && D15_number >= 3){
            seekBarTypeQuestion.setQuestionName("請問你覺得[" + D15Answer.get(1) + "]與[" + D15Answer.get(2) + "]相關的程度有多強？0表示兩者沒有關聯，100表示兩者極度相關，可視為同一個事件。");
        }

        //  questionSKTypeTextView.setText(seekBarTypeQuestion != null ? seekBarTypeQuestion.getQuestionName() + '\n' + apppendText : "");
        String title = seekBarTypeQuestion != null ? seekBarTypeQuestion.getQuestionName():"";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            questionOSKTypeTextView.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY));
        }
        else{
            questionOSKTypeTextView.setText(Html.fromHtml(title));
        }
        //questionRBTypeTextView.setText(radioButtonTypeQuestion.getQuestionName()+'\n'+apppendText);
        // setTextWithSpan(title,questionSKTypeTextView);
//        questionOSKTypeTextView.setText(title);
        /*Disable the button until any choice got selected*/


        List<AnswerOptions_diary> seekBarChoices = Objects.requireNonNull(seekBarTypeQuestion).getAnswerOptions();

        seekBarsArrayList.clear();

        for (AnswerOptions_diary choice : seekBarChoices) {
            Log.d(TAG, "Create seek bar");
            IndicatorSeekBar seekBar = new IndicatorSeekBar(mContext);
            TextView textView = getTextView();
            textView.setText(choice.getName());
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            textView.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
            textView.setPadding(10, 0, 10, 0);

//                String[] tokens = choice.getName().split(" ---- ");
            String[] arr = {"0", "", "20", "", "40", "", "60", "", "80", "", "100"};
            seekBar = IndicatorSeekBar
                    .with(getContext())
                    .max(100)
                    .min(0)
                    .tickTextsArray(arr)
                    .tickCount(11)
                    .showTickTexts(true)
                    .tickTextsColor(R.color.white)
                    .tickTextsSize(13)//sp
                    .showTickMarksType(TickMarkType.NONE)
                    .tickMarksColor(R.color.color_gray)
                    .indicatorColor(ContextCompat.getColor(getContext(),R.color.colorPrimaryDark))
                    .indicatorTextColor(Color.parseColor("#ffffff"))
                    .showIndicatorType(IndicatorType.ROUNDED_RECTANGLE)
                    .thumbColor(ContextCompat.getColor(getContext(),R.color.colorPrimary))
                    .thumbSize(14)
                    .trackProgressColor(getResources().getColor(R.color.colorAccent, null))
                    .trackProgressSize(4)  //4
                    .trackBackgroundColor(getResources().getColor(R.color.color_gray, null))
                    .trackBackgroundSize(2)
                    .build();
            OneseekBarsLinearLayout.addView(textView);


            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            params.leftMargin = 12;

            View view = new View(mContext);
            view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.divider));
            view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
            IndicatorStayLayout StayLayout = new IndicatorStayLayout(getContext());
            StayLayout.attachTo(seekBar);
            OneseekBarsLinearLayout.addView(StayLayout, params);
            seekBarsArrayList.add(seekBar);

            seekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
                @Override
                public void onSeeking(SeekParams p) {

                }

                @Override
                public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                    clickedSeekBarPosition = seekBarsArrayList.indexOf(seekBar);
                    Log.d(TAG, "clickedSeekBarPosition: " + clickedSeekBarPosition);
                    saveActionsOfSeekBar();
                }
            });
        }

//        for (int i = 0; i < seekBarsArrayList.size(); i++) {
//            String[] data = new String[]{"0", questionId, String.valueOf(i)};
//            if((currentPagePosition == 15 || currentPagePosition == 18)&& D13_number >= 1) {
//                insertAnswerInDatabase(data);
//            }
//            if((currentPagePosition == 16 || currentPagePosition == 19 || currentPagePosition == 21) && D13_number >= 2) {
//                Log.d(TAG, "Insert 0 ");
//                insertAnswerInDatabase(data);
//            }
//            if((currentPagePosition == 17 || currentPagePosition == 20 || currentPagePosition == 22 || currentPagePosition == 23)
//                    && D13_number >= 3){
//                insertAnswerInDatabase(data);
//            }
//        }


        /* If the current question is last in the questionnaire then
        the "Next" button will change into "Finish" button*/
        if (currentPagePosition == ((QuestionActivity_diary) mContext).getTotalQuestionsSize()) {
            nextOrFinishButton.setText(R.string.finish);
        } else {
            nextOrFinishButton.setText(R.string.next);
        }
        nextOrFinishButton.setEnabled(true);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {


    }

    private void setTextWithSpan(String questionPosition, TextView tv)
    {
        int slashPosition = questionPosition.indexOf("您");

        Spannable spanText = new SpannableString(questionPosition);
        spanText.setSpan(new RelativeSizeSpan(0.6f), slashPosition, questionPosition.length(), 0);
        tv.setText(spanText);
    }

    private String getTheStateOfSeekBar(String[] data)
    {

        Log.d(TAG,"setUserVisibleHint: getTheStateOfSeekBar = "+data[0]+" : " + data[1]);
        Log.d(TAG,"setUserVisibleHint: result  = " + db.questionWithAnswersDao().isChecked(data[0], data[1]));
        return db.questionWithAnswersDao().isChecked(data[0], data[1]);
    }




    private void saveActionsOfSeekBar(){
        Log.d(TAG,"saveActionsOfSeekBar size : "+ seekBarsArrayList.size());
        for (int i = 0; i < seekBarsArrayList.size(); i++)
        {
            if (i == clickedSeekBarPosition)
            {
                IndicatorSeekBar seekBar = seekBarsArrayList.get(i);
                String sbPosition = String.valueOf(seekBarsArrayList.indexOf(seekBar));
                String result = new Integer(seekBar.getProgress()).toString();
                String[] data = new String[]{result, questionId, sbPosition};
                Log.d(TAG,"saveActionsOfSeekBar data : "+ data[0] + " " + data[1] + " " + data[2]);
                insertAnswerInDatabase(data);
            }
        }


    }
    private void checkIfNextSetTrue(){
        int count = 0 ;
        for(int i = 0; i < checkedBefore.size(); i++ ){
            if(checkedBefore.get(i)){
                count++;
            }
        }
        nextOrFinishButton.setEnabled(true);
        Log.d(TAG,"count =  "+count);
        Log.d(TAG,"nextOrFinishButton is "+nextOrFinishButton.isEnabled());
    }



    private void insertAnswerInDatabase(String[] data)
    {
        Log.d(TAG,"insertAnswerInDatabase ");
        Observable.just(data)
                .map(new Function<String[], Object>() {
                    @Override
                    public Object apply(String[] data1) throws Exception {
                        return OneSeekBarsFragment_diary.this.insertingInDb(data1);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    private String insertingInDb(String[] data)
    {
        Log.d(TAG,"insertingInDb ");
        String answerTime = getReadableTime(new Date().getTime());
        db.questionWithAnswersDao().updateQuestionWithChoice(data[0], data[1], data[2]);
        // String LastAnswerTime = db.questionWithAnswersDao().getLastTimeDetectedTime(data[1], data[2]);
        db.questionWithAnswersDao().updateQuestionWithDetectedTime(answerTime ,data[1], data[2]);
        //insertFinalAnswer(data[1],data[2], data[0],answerTime);
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


    private TextView getTextView() {
        TextView textView = new TextView(getContext());
        int padding = dp2px(getContext(), 10);
        textView.setPadding(padding, padding, padding, 0);
        return textView;
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
            atLeastOneChecked = 0;
            for (int i = 0; i < seekBarsArrayList.size(); i++)
            {
                IndicatorSeekBar seekBar = seekBarsArrayList.get(i);
                String cbPosition = String.valueOf(i);

                String[] data = new String[]{questionId, cbPosition};
                Log.d(TAG,"setUserVisibleHint i = "+ i + " data = "+ data[0]+" "+ data[1]);
                Observable.just(data)
                        .map(this::getTheStateOfSeekBar)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<String>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(String s) {
                                qState = s;
                                Log.d(TAG, "onNext: " + qState);
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {
                                seekBar.setProgress(Float.parseFloat(qState));
                                Log.d(TAG,"setUserVisibleHint: qState = "+qState);
//                                    if (!nextOrFinishButton.isEnabled()) {
//                                        nextOrFinishButton.setEnabled(true);
//                                    }

                            }
                        });
            }
        }
    }
}