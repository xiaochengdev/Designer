package com.xc.designer.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xc.designer.R;
import com.xc.designer.bean.AnMessage;
import com.xc.designer.bean.LeMessage;
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

public class LeAndAnsFragment extends Fragment {
    private ProgressDialog progressDialog;
    private ListView listView;
    private TextView title;

    private List<AnMessage> ansMsgList;
    private List<String> dataList=new ArrayList<String>();
    private ArrayAdapter adapter;
    private LeMessage leMessage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragement_lemsg_anmsg_view,container,false);
        initView(view);
        Intent intent=getActivity().getIntent();
        Bundle data=intent.getExtras();
        leMessage=(LeMessage) data.get("leMsg");
        adapter=new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queryAnsMsg();
    }

    public void initView(View view){
        listView=(ListView)view.findViewById(R.id.ansmsg_list);
        title=(TextView)view.findViewById(R.id.lemsg_contents);
    }

    public void initEvent(){

    }

    private void queryAnsMsg(){
        Integer lid=leMessage.getNid();
        ansMsgList= DataSupport.where("lid=?",lid+"").find(AnMessage.class);
        if (ansMsgList.size()>0){
            dataList.clear();
            for (AnMessage anMessage:ansMsgList){
                dataList.add(anMessage.getContents());
            }
            adapter.notifyDataSetChanged();
        }else{
            String address="http://192.168.191.1:8080/Designer/msg_findAnswerMsgByLid?id="+lid;
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
                        Toast.makeText(getActivity(),"加载失败",Toast.LENGTH_SHORT).show();
                        //swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                result = Utility.handleAnMessageResponse(responseText);
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDailog();
                            queryAnsMsg();
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
