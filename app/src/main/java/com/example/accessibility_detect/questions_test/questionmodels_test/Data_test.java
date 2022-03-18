package com.example.accessibility_detect.questions_test.questionmodels_test;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data_test
{
    @SerializedName("questions")
    private List<QuestionsItem_test> questions;

    public List<QuestionsItem_test> getQuestions()
    {
        return questions;
    }

    public void setQuestions(List<QuestionsItem_test> questions)
    {
        this.questions = questions;
    }
}