package com.example.accessibility_detect.diarys.questionmodels_diary;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data_diary
{
    @SerializedName("questions")
    private List<QuestionsItem_diary> questions;

    public List<QuestionsItem_diary> getQuestions()
    {
        return questions;
    }

    public void setQuestions(List<QuestionsItem_diary> questions)
    {
        this.questions = questions;
    }
}