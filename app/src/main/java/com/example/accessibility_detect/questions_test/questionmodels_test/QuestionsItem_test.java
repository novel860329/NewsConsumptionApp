package com.example.accessibility_detect.questions_test.questionmodels_test;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class QuestionsItem_test implements Parcelable
{
    public static final Creator<QuestionsItem_test> CREATOR = new Creator<QuestionsItem_test>()
    {
        @Override
        public QuestionsItem_test createFromParcel(Parcel in)
        {
            QuestionsItem_test questionItem = new QuestionsItem_test();
            questionItem.questionTypeName = in.readString();
            questionItem.questionName = in.readString();
            questionItem.id = in.readInt();
            Bundle b = in.readBundle(AnswerOptions_test.class.getClassLoader());
            questionItem.answerOptions = b.getParcelableArrayList("q_items");
            questionItem.answerOptionsGroup = b.getParcelable("q_items_group");
            questionItem.questionTypeId = in.readInt();

            return questionItem;
        }

        @Override
        public QuestionsItem_test[] newArray(int size)
        {
            return new QuestionsItem_test[size];
        }
    };
    @SerializedName("question_type_name")
    private String questionTypeName;
    @SerializedName("question_name")
    private String questionName;
    @SerializedName("id")
    private int id;
    @SerializedName("question_item_group")
    private String answerOptionsGroup;
    @SerializedName("question_item")
    private List<AnswerOptions_test> answerOptions;

    @SerializedName("question_type_id")
    private int questionTypeId;

    public String getQuestionTypeName()
    {
        return questionTypeName;
    }

    public void setQuestionTypeName(String questionTypeName)
    {
        this.questionTypeName = questionTypeName;
    }

    public String getQuestionName()
    {
        return questionName;
    }

    public void setQuestionName(String questionName)
    {
        this.questionName = questionName;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public List<AnswerOptions_test> getAnswerOptions()
    {
        return answerOptions;
    }
    public String getAnswerOptionsGroup()
    {
        return answerOptionsGroup;
    }
    public void setAnswerOptions(List<AnswerOptions_test> answerOptions)
    {
        this.answerOptions = answerOptions;
    }

    public int getQuestionTypeId()
    {
        return questionTypeId;
    }

    public void setQuestionTypeId(int questionTypeId)
    {
        this.questionTypeId = questionTypeId;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(questionTypeName);
        dest.writeString(questionName);
        dest.writeInt(id);
        Bundle b = new Bundle();
        b.putParcelableArrayList("items", (ArrayList<? extends Parcelable>) answerOptions);
        dest.writeBundle(b);
        dest.writeInt(questionTypeId);
    }
}