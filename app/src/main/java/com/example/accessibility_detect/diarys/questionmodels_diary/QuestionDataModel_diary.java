package com.example.accessibility_detect.diarys.questionmodels_diary;

import com.google.gson.annotations.SerializedName;

public class QuestionDataModel_diary
{
    @SerializedName("data")
    private Data_diary data;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public Data_diary getData()
    {
        return data;
    }

    public void setData(Data_diary data)
    {
        this.data = data;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public boolean isStatus()
    {
        return status;
    }

    public void setStatus(boolean status)
    {
        this.status = status;
    }
}