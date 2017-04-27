package com.xc.designer.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.xc.designer.R;
import com.xc.designer.activity.LeaveOpActivity;
import com.xc.designer.adapter.CommentAdapter;
import com.xc.designer.bean.AnMessage;
import com.xc.designer.bean.LeMessage;
import com.xc.designer.interfaces.Note;
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
 * Created by Administrator on 2017/4/12.
 */

public class ChooseLeavemsgFragment extends Fragment {
    private ProgressDialog progressDialog;
    private RecyclerView listView;
    private SwipeRefreshLayout refreshLayout;

    private CommentAdapter adapter;
    private List<LeMessage> leMessageList;
    //private List<AnMessage> anMessageList=new ArrayList<AnMessage>();
    private List<Note> dataList=new ArrayList<Note>();

    private LeMessage leMessage;
    private int current=0;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_leavemsg,container,false);
        initView(view);

        DataSupport.deleteAll(LeMessage.class);
        //adapter=new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_1,dataList);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        listView.setLayoutManager(layoutManager);
        adapter=new CommentAdapter(dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
       /* listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                leMessage=leMessageList.get(position);
                Intent intent=new Intent(getActivity(),LeaveOpActivity.class);
                intent.putExtra("leMsg",leMessage);
                startActivity(intent);
            }
        });*/
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DataSupport.deleteAll(LeMessage.class);
                queryLeMsg();
            }
        });
        queryLeMsg();
    }

    private void initView(View view){
        listView=(RecyclerView) view.findViewById(R.id.leavemsg_list);
        refreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.lemsg_refresh);
    }

    private void queryLeMsg(){
        leMessageList= DataSupport.findAll(LeMessage.class);
        if (leMessageList.size()>0||current>0){
            current=0;
            dataList.clear();
            for (LeMessage leMessage:leMessageList){
                //dataList.add(leMessage.getContents());
                dataList.add(leMessage);
            }
            adapter.notifyDataSetChanged();
            refreshLayout.setRefreshing(false);
        }else{
            current++;
            String address="http://192.168.191.1:8080/Designer/msg_findLeaveMsg";
            queryFromServer(address);
        }
    }

    private void queryFromServer(String address){
        showProgressDailog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDailog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                        //swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                result = Utility.handleLeMessageResponse(responseText);
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDailog();
                            queryLeMsg();
                            refreshLayout.setRefreshing(false);
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
