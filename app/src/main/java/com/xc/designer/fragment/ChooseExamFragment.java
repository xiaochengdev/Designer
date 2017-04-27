package com.xc.designer.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.xc.designer.R;
import com.xc.designer.activity.ExamOpActivity;
import com.xc.designer.bean.Exam;
import com.xc.designer.util.HttpUtil;
import com.xc.designer.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/4/2.
 */

public class ChooseExamFragment extends Fragment {

    private ListView exam_list_view;
    private ProgressDialog progressDialog;

    private List<String> dataList=new ArrayList<String>();
    private List<Exam> examList;

    private ArrayAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_exam,container,false);
        exam_list_view=(ListView) view.findViewById(R.id.exam_list);
        adapter=new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_1,dataList);
        exam_list_view.setAdapter(adapter);
        exam_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Exam exam=examList.get(position);
                Intent intent=new Intent(getActivity(), ExamOpActivity.class);
                intent.putExtra("exam",exam);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queryExam();
    }


    public void queryExam(){
        examList= DataSupport.findAll(Exam.class);
        if (examList.size()>0){
            dataList.clear();
            for (Exam exam:examList){
                dataList.add(exam.getTitle());
            }
            adapter.notifyDataSetChanged();
        }else{
            String address="http://192.168.191.1:8080/Designer/exam_findAll";
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
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
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
                            queryExam();
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
