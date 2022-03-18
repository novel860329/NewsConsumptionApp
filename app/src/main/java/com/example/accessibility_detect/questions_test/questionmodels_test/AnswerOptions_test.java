package com.example.accessibility_detect.questions_test.questionmodels_test;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class AnswerOptions_test implements Parcelable
{
    public static final Creator<AnswerOptions_test> CREATOR = new Creator<AnswerOptions_test>()
    {
        @Override
        public AnswerOptions_test createFromParcel(Parcel in)
        {
            return new AnswerOptions_test(in);
        }

        @Override
        public AnswerOptions_test[] newArray(int size)
        {
            return new AnswerOptions_test[size];
        }
    };
    @SerializedName("answer_id")
    private String answerId;
    @SerializedName("name")
    private String name;


    protected AnswerOptions_test(Parcel in)
    {
        answerId = in.readString();
        name = in.readString();
    }

    public String getAnswerId()
    {
        return answerId;
    }

    public void setAnswerId(String answerId)
    {
        this.answerId = answerId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(answerId);
        dest.writeString(name);
    }
}