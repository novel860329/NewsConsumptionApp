package com.example.accessibility_detect.questions_test.fragments_test;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.accessibility_detect.MainActivity;
import com.example.accessibility_detect.R;
import com.example.accessibility_detect.myimagecrop.CropImage;
import com.example.accessibility_detect.myimagecrop.CropImageView;
import com.example.accessibility_detect.questions_test.QuestionActivity_test;
import com.example.accessibility_detect.questions_test.questionmodels_test.QuestionsItem_test;

import java.util.ArrayList;
import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import labelingStudy.nctu.minuku.DBHelper.appDatabase;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.example.accessibility_detect.Utils.Crop_Uri;
import static com.example.accessibility_detect.Utils.cueRecallImg;
import static com.example.accessibility_detect.Utils.finish_crop;
import static labelingStudy.nctu.minuku.config.SharedVariables.BackToQ6;
import static labelingStudy.nctu.minuku.config.SharedVariables.CanFillEsm;
import static labelingStudy.nctu.minuku.config.SharedVariables.Q25Answer;
import static labelingStudy.nctu.minuku.config.SharedVariables.getReadableTime;
import static labelingStudy.nctu.minuku.config.SharedVariables.isFinish;

/**
 * This fragment provide the RadioButton/Single Options.
 */
public class ImageCropFragment_test extends Fragment
{
    private final ArrayList<RadioButton> radioButtonArrayList = new ArrayList<>();
    private boolean isFirstLoad = true; // 是否第一次加载
    private boolean screenVisible = false;
    private Uri uri;
    private CropImageView mCropView;
    private ImageView imageView;
    private QuestionsItem_test imageCropTypeQuestion;
    private FragmentActivity mContext;
    private Button nextOrFinishButton;
    //private Button previousButton;
    private TextView questionICTypeTextView;
    private RadioGroup radioGroupForChoices;
    private boolean atLeastOneChecked = false;
    appDatabase db;
    private String questionId = "";
    private int currentPagePosition = 0;
    private int clickedRadioButtonPosition = -1;
    private String qState = "0";
    private String TAG ="ImageCropFragment";
    private String fileName = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.example.accessibility_detect/files/crop_rest.jpg";
    int relatedId;
    String appName = "";
    String enterTime ="";
    private EditText editText_answer;
    String notiId = "";
    public ImageCropFragment_test()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
//        6/17
//        AssetManager assetManager = getContext().getAssets();
//        Bitmap bmp = null;
//        InputStream istr = null;
//        if(cueRecallImg != null) {
//            String[] file_split = String.valueOf(cueRecallImg).split("/");
//            String filepath = file_split[4] + "/" + file_split[5];
//            try {
//                istr = assetManager.open(filepath);
//                bmp = BitmapFactory.decodeStream(istr);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                FileOutputStream out = new FileOutputStream(fileName);
//                if (out != null) {
//                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
//                    out.flush();
//                    out.close();
//                }
//            }catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                if (bmp != null) {
//                    bmp.recycle();
//                }
//            }
//        }

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.image_crop, container, false);

        db = appDatabase.getDatabase(getActivity());

        nextOrFinishButton = rootView.findViewById(R.id.nextOrFinishButton);
        //previousButton = rootView.findViewById(R.id.previousButton);
        questionICTypeTextView = rootView.findViewById(R.id.questionICTypeTextView);

        imageView = rootView.findViewById(R.id.image_view);
        imageView.setImageURI(cueRecallImg);
//        imageView.setImageURI(Uri.fromFile(new File(fileName)));
        imageView.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {
                Log.v(TAG, " click");
                startCrop(cueRecallImg);
            }
        });
//        radioGroupForChoices = rootView.findViewById(R.id.radioGroupForChoices);
//        mCropView = (CropImageView) rootView.findViewById(R.id.cropImageView);
        nextOrFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(!pageRecord.contains(currentPagePosition))
//                    pageRecord.add(currentPagePosition);
                Log.d(TAG, "now:" + currentPagePosition);
                Log.d(TAG, "size:" + ((QuestionActivity_test) mContext).getTotalQuestionsSize());
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
                if (currentPagePosition == ((QuestionActivity_test) mContext).getTotalQuestionsSize()) {
                    /* Here, You go back from where you started OR If you want to go next Activity just change the Intent*/
//                    Intent returnIntent = new Intent();
//                    if (check_radio_answer("1") == 1) {
//                        returnIntent.putExtra("isMobileCrowdsource", false);
//                    }
//                    mContext.setResult(Activity.RESULT_OK, returnIntent);
                    CanFillEsm = false;
                    isFinish = "2";
                    startActivity(MainIntent);
                    mContext.finish();
                }
                else {
                    // 把照片存在一個變數中
                    ((QuestionActivity_test) mContext).nextQuestion(1);
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
                        return ImageCropFragment_test.this.insertingInDbForOther(data1);
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
                        return ImageCropFragment_test.this.insertingInDb(data1);
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
            imageCropTypeQuestion = getArguments().getParcelable("question"); //type = image crop
            questionId = String.valueOf(imageCropTypeQuestion != null ? imageCropTypeQuestion.getId() : 0);
            currentPagePosition = getArguments().getInt("page_position") + 1;
            enterTime = getArguments().getString("enterTimeForF");
            relatedId = getArguments().getInt("relatedIdF");
            notiId = getArguments().getString("notiId");
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

        String title;
        if(currentPagePosition == 1 || currentPagePosition == 3){
            title = imageCropTypeQuestion != null ? apppendText+'\n'+imageCropTypeQuestion.getQuestionName():"";
        }else{
            title = imageCropTypeQuestion != null ? imageCropTypeQuestion.getQuestionName():"";
        }

        Log.d(TAG,"title : "+title);
        //questionRBTypeTextView.setText(radioButtonTypeQuestion.getQuestionName()+'\n'+apppendText);
        //setTextWithSpan(title,questionRBTypeTextView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            questionICTypeTextView.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY));
        }
        else{
            questionICTypeTextView.setText(Html.fromHtml(title));
        }
        if(cueRecallImg != null && !finish_crop && !BackToQ6){
            startCrop(cueRecallImg);
        }

        nextOrFinishButton.setEnabled(true);

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
    private void setTextWithSpan(String questionPosition, TextView tv)
    {
        int slashPosition = questionPosition.indexOf("您");

        Spannable spanText = new SpannableString(questionPosition);
        spanText.setSpan(new RelativeSizeSpan(0.6f), slashPosition, questionPosition.length(), 0);
        tv.setText(spanText);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
//        Log.d(TAG, "request code = " + requestCode + " result code = " + resultCode);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){
                Log.d(TAG, "get uri: " + result.getUri());
                Crop_Uri = result.getUri();
                finish_crop = true;
                imageView.setImageURI(result.getUri());
                Toast.makeText(mContext, "裁剪成功", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCrop(Uri imageuri){
//        Log.d(TAG, "Start crop image uri: " + fileName);
        Intent intent = CropImage.activity(imageuri) //這裡抓不到asset的uri
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(false)
                .setActivityTitle("選取新聞")
                .setInitialCropWindowRectangle(new Rect( 0, 0 , 350 , 600 ))
                .setAutoZoomEnabled(false)
                .setAllowFlipping(false)
                .setAllowRotation(false)
                .getIntent(mContext);
        startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
//        Toast.makeText(mContext, "請剪出您要回答的該則新聞", Toast.LENGTH_LONG).show();
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
//                                } else if(qState.equals("-1"))
//                                {
//                                    radioButton.setChecked(false);
//                                } else{
//                                    editText_answer.setText(qState);
//                                }
//                            }
//                        });
//            }
            isFirstLoad = false;
        }
    }
}