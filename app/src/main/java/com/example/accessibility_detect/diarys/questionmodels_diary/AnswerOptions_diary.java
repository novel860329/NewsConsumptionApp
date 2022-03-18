package com.example.accessibility_detect.diarys.questionmodels_diary;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class AnswerOptions_diary implements Parcelable
{
    public static final Creator<AnswerOptions_diary> CREATOR = new Creator<AnswerOptions_diary>()
    {
        @Override
        public AnswerOptions_diary createFromParcel(Parcel in)
        {
            return new AnswerOptions_diary(in);
        }

        @Override
        public AnswerOptions_diary[] newArray(int size)
        {
            return new AnswerOptions_diary[size];
        }
    };
    @SerializedName("answer_id")
    private String answerId;
    @SerializedName("name")
    private String name;


    protected AnswerOptions_diary(Parcel in)
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