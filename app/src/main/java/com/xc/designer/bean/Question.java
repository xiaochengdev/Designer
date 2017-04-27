package com.xc.designer.bean;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/4/2.
 */

public class Question extends DataSupport implements Serializable {
    private Integer qid;
    private String question;
    private String ansoptions;
    private String answer;
    private String suggestion;
    private String analyzes;

    public Integer getQid() {
        return qid;
    }

    public void setQid(Integer qid) {
        this.qid = qid;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnsoptions() {
        return ansoptions;
    }

    public void setAnsoptions(String ansoptions) {
        this.ansoptions = ansoptions;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getAnalyzes() {
        return analyzes;
    }

    public void setAnalyzes(String analyzes) {
        this.analyzes = analyzes;
    }
}
