package com.example.accessibility_detect.questions_test.questionmodels_test;

import com.google.gson.annotations.SerializedName;

public class QuestionDataModel_test
{
    @SerializedName("data")
    private Data_test data;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public Data_test getData()
    {
        return data;
    }

    public void setData(Data_test data)
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