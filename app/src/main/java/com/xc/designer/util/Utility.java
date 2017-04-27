package com.xc.designer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.xc.designer.bean.AnMessage;
import com.xc.designer.bean.Document;
import com.xc.designer.bean.Exam;
import com.xc.designer.bean.LeMessage;
import com.xc.designer.bean.Question;
import com.xc.designer.bean.User;
import com.xc.designer.bean.Video;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/3/20.
 */

public class Utility {
    public static boolean handleVideoResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONObject result=new JSONObject(response);
                String videolist=result.getString("videolist");
                JSONArray allVideos=new JSONArray(videolist);
                for (int i=0;i<allVideos.length();i++){
                    JSONObject videoObject=allVideos.getJSONObject(i);
                    Video video=new Video();
                    video.setVid(Integer.valueOf(videoObject.getString("id").toString()));
                    video.setName(videoObject.getString("name"));
                    video.setPath(videoObject.getString("path"));
                    video.setDescr(videoObject.getString("descr"));
                    video.save();
                }
                return true;
            }catch(JSONException e){
                e.printStackTrace();
            }catch (Exception e){
                e.getMessage();
            }
        }
        return false;
    }
    public static boolean handleDocResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONObject result=new JSONObject(response);
                String doclist=result.getString("doclist");
                JSONArray allDocuments=new JSONArray(doclist);
                for (int i=0;i<allDocuments.length();i++){
                    JSONObject docObject=allDocuments.getJSONObject(i);
                    Document document=new Document();
                    document.setDid(Integer.valueOf(docObject.getString("id").toString()));
                    document.setName(docObject.getString("name"));
                    document.setPath(docObject.getString("path"));
                    document.setDescr(docObject.getString("descr"));
                    document.save();
                }
                return true;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleExamResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONObject result=new JSONObject(response);
                String examlist=result.getString("examlist");
                JSONArray allExams=new JSONArray(examlist);
                for (int i=0;i<allExams.length();i++){
                    JSONObject examObject=allExams.getJSONObject(i);
                    Exam exam=new Exam();
                    exam.setEid(Integer.valueOf(examObject.getString("id").toString()));
                    exam.setTitle(examObject.getString("title"));
                    exam.setDescr(examObject.getString("descr"));
                    exam.setMscore(examObject.getInt("mscore"));
                    exam.setEtime(examObject.getInt("etime"));
                    exam.setQuestions(examObject.getString("questions"));
                    exam.save();
                }
                return true;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleQuestionResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONObject result=new JSONObject(response);
                String questionlist=result.getString("question");
                JSONArray questions=new JSONArray(questionlist);
                for (int i=0;i<questions.length();i++){
                    JSONObject questionObject=questions.getJSONObject(i);
                    Question question=new Question();
                    question.setQid(Integer.valueOf(questionObject.getString("id").toString()));
                    question.setAnalyzes(questionObject.getString("analyzes"));
                    question.setAnsoptions(questionObject.getString("ansoptions"));
                    question.setAnswer(questionObject.getString("answer"));
                    question.setQuestion(questionObject.getString("question"));
                    question.setSuggestion(questionObject.getString("suggestion"));
                    question.save();
                }
                return true;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleLeMessageResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONObject result=new JSONObject(response);
                String leMessageList=result.getString("leMessage");
                JSONArray leMessages=new JSONArray(leMessageList);
                for (int i=0;i<leMessages.length();i++){
                    JSONObject leMessageObject=leMessages.getJSONObject(i);
                    LeMessage leMessage=new LeMessage();
                    leMessage.setNid(leMessageObject.getInt("lid"));
                    leMessage.setTitle(leMessageObject.getString("title"));
                    leMessage.setContents(leMessageObject.getString("contents"));
                    leMessage.setUserid(leMessageObject.getInt("userid"));
                    leMessage.setUsername(leMessageObject.getString("username"));
                    leMessage.setFdate(leMessageObject.getString("fdate"));
                    leMessage.save();
                }
                return true;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleAnMessageResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONObject result=new JSONObject(response);
                String anMessageList=result.getString("anMessage");
                JSONArray anMessages=new JSONArray(anMessageList);
                for (int i=0;i<anMessages.length();i++){
                    JSONObject anMessageObject=anMessages.getJSONObject(i);
                    AnMessage anMessage=new AnMessage();
                    anMessage.setTitle(anMessageObject.getString("title"));
                    anMessage.setNid(anMessageObject.getInt("mid"));
                    anMessage.setContents(anMessageObject.getString("contents"));
                    anMessage.setUserid(anMessageObject.getInt("userid"));
                    anMessage.setUsername(anMessageObject.getString("username"));
                    anMessage.setFdate(anMessageObject.getString("fdate"));
                    anMessage.setLid(anMessageObject.getInt("lid"));
                    anMessage.setVideoid(anMessageObject.getInt("videoId"));
                    anMessage.setDocid(anMessageObject.getInt("docId"));
                    anMessage.save();
                }
                return true;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    public static User getUserInfo(Context context){
        SharedPreferences pref=context.getSharedPreferences("user",Context.MODE_PRIVATE);
        User user=new User();
        user.setUid(pref.getInt("id",0));
        user.setName(pref.getString("name",""));
        user.setPwd(pref.getString("pwd",""));
        user.setSex(pref.getString("sex",""));
        user.setEmail(pref.getString("email",""));
        user.setBirth(pref.getString("birth",""));
        user.setDescr(pref.getString("descr",""));
        user.setVideos(pref.getString("videos",""));
        user.setDocuments(pref.getString("documents",""));
        return user;
    }

    public static void saveUser(Context context,User user){
        SharedPreferences  sharedPreferences=context.getSharedPreferences("user",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt("id",user.getUid());
        editor.putString("name",user.getName());
        editor.putString("pwd",user.getPwd());
        editor.putString("sex",user.getSex());
        editor.putString("email",user.getEmail());
        editor.putString("birth",user.getBirth());
        editor.putString("descr",user.getDescr());
        editor.putString("videos",user.getVideos());
        editor.putString("documents",user.getDocuments());
        editor.apply();
    }

    public static void saveSearchRecord(Context context,String search){
        SharedPreferences  sharedPreferences=context.getSharedPreferences("search",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("search",search);
        editor.apply();
    }
    public static String getSearchRecord(Context context){
        SharedPreferences pref=context.getSharedPreferences("search",Context.MODE_PRIVATE);
        String record=pref.getString("search","");
        return record;
    }
}
