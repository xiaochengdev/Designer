package com.xc.designer.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.xc.designer.activity.ExamOpActivity;
import com.xc.designer.bean.Exam;
import com.xc.designer.bean.Question;
import com.xc.designer.util.HttpUtil;
import com.xc.designer.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/4/3.
 */

public class ExampaperFragment extends Fragment {
    private ProgressDialog progressDialog;
    private List<Question> questionList;
    private Exam exam;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    public void queryQuestions(){
        questionList= DataSupport.findAll(Question.class);
        if (questionList.size()>0){
            /*dataList.clear();
            for (Exam exam:examList){
                dataList.add(exam.getTitle());
            }
            adapter.notifyDataSetChanged();*/
        }else{
            String address="http://192.168.191.1:8080/Designer/question_findQuestionsByIds?ids="+exam.getQuestions();
            queryFromServer(address);
        }
    }

    public void queryFromServer(String address){
        showProgressDailog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDailog();
                        Toast.makeText(getActivity(),"加载失败",Toast.LENGTH_SHORT).show();
                        //swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                result= Utility.handleExamResponse(responseText);
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDailog();
                            queryQuestions();
                            //swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    private void showProgressDailog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    private void closeProgressDailog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
